package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Cnst;
import io.github.ihongs.Core;
import io.github.ihongs.HongsException;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.db.Table;
import io.github.ihongs.serv.matrix.Data;
import io.github.ihongs.util.Dawn;
import io.github.ihongs.util.Syno;
import io.github.ihongs.util.Synt;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.document.Document;

/**
 * 模型基类
 * 按照用户存放
 * @author Hongs
 */
public abstract class MosaicEntity extends Data {

    private String siteId = null;
    private final Map<String, String> dcUrls = new LinkedHashMap();

    protected MosaicEntity(String conf, String form) {
        super(conf, form);
    }

    @Override
    public String getPartId() {
        return getFormId() + "." + getSiteId();
    }

    public String getSiteId() {
        if ( null == siteId ) {
            try {
                siteId = (String) ActionHelper.getInstance()
                                . getSessibute(Cnst.UID_SES);
            } catch (UnsupportedOperationException e ) {
                throw new NullPointerException("Call setSiteId first");
            }
            if ( null == siteId ) {
                throw new NullPointerException("Call setSiteId first");
            }
        }
        return siteId;
    }

    public void setSiteId(String cuId) {
        siteId = cuId;
    }

    /**
     * 添加记录
     * @param rd
     * @return
     * @throws HongsException
     */
    @Override
    public String add(Map rd) throws HongsException {
        String id = Core.newIdentity();
        Map    dd = rd;

        // 构建文档对象, 会重组 name,word 的取值, 用于下方记录
        dd.put(Cnst.ID_KEY , id);
        Document dc = padDoc(dd);

        // 保存到数据库
        Table table = getTable();
        if (table != null) {
            String   fid   = getFormId();
            String   sid   = getSiteId();
            String   uid   = getUserId();
            long     ctime = System.currentTimeMillis() / 1000;

            Map nd = new HashMap();
            nd.put("ctime", ctime);
            nd.put("etime",   0  );
            nd.put("state",   1  );
            nd.put(     "id", id );
            nd.put("form_id", fid);
            nd.put("site_id", sid);
            nd.put("user_id", uid);

            // 数据快照和日志标题
            nd.put("data", Dawn.toString(dd, true));
            nd.put("name",     cutText(dd, "name"));

            // 操作备注和终端代码
            if (rd.containsKey("memo")) {
                nd.put("memo", cutText(rd, "memo"));
            }
            if (rd.containsKey("meno")) {
                nd.put("meno", cutText(rd, "meno"));
            }

            table.insert(nd);
        }

        // 保存到索引库
        setDoc(id, dc);
        call(id, "create");
        return id;
    }

    /**
     * 保存记录
     *
     * 注意:
     * 更新不产生新节点,
     * 仅供内部持续补充.
     *
     * @param id
     * @param rd
     * @return
     * @throws HongsException
     */
    @Override
    public int set(String id, Map rd) throws HongsException {
        // 合并新旧数据
        int i  = 0;
    //  int t  = 2;
        Map dd = get(id);
    //  if (dd.isEmpty()) t = 1;
        Map<String,Map> fs = getFields();
        for(String fn : fs . keySet( ) ) {
            if (  "id". equals(fn)) {
                dd.put(fn , id);
            } else
            if (rd.containsKey(fn)) {
                Object fr = rd.get(fn);
                Object fo = dd.get(fn);
                dd.put(fn , fr);
                // 跳过环境字段, 比如修改时间
                if (! canSkip(fn,fr,fo)) {
                    i ++;
                }
            }
        }
        // 无更新不存储
        if (i == 0) {
          return 0;
        }

        // 构建文档对象, 会重组 name,word 的取值, 用于下方记录
        dd.put(Cnst.ID_KEY , id);
        Document dc = padDoc(dd);

        // 保存到数据库
        Table table = getTable();
        if (table != null) {
            String   fid   = getFormId();
            String   sid   = getSiteId();
            String   uid   = getUserId();
            long     ctime = System.currentTimeMillis() / 1000;
            Object[] param = new String[] {id, fid, "0"};
            String   where = "`id`=? AND `form_id`=? AND `site_id`=? AND `etime`=?";

            /**
             * 此类里的提交方法并不会将对应的操作记录数据进行提交,
             * 好在关系数据库事务内可查到前面插入但还未提交的记录.
             */
            Map nd = table.fetchCase()
                .filter( where,param )
                .select("ctime,state")
                .getOne( );
            if (nd.isEmpty()) {
                nd.put("ctime", ctime);
                nd.put("etime",   0  );
                nd.put("state",   1  );
                nd.put(     "id", id );
                nd.put("form_id", fid);
                nd.put("site_id", sid);
                nd.put("user_id", uid);
            } else {
                if ( Synt.declare ( nd.get("state"), 0  )  ==   0    ) {
                    throw new HongsException(0x1104, "记录已经删除了");
                }
            }

            // 数据快照和日志标题
            nd.put("data", Dawn.toString(dd, true));
            nd.put("name",     cutText(dd, "name"));

            // 操作备注和终端代码
            if (rd.containsKey("memo")) {
                nd.put("memo", cutText(rd, "memo"));
            }
            if (rd.containsKey("meno")) {
                nd.put("meno", cutText(rd, "meno"));
            }

            if (nd.containsKey("etime") == false ) {
                table.update(nd, where, param);
            } else {
                table.insert(nd);
            }
        }

        // 保存到索引库
        setDoc(id, dc);
        call(id, "update");
        return 1;
    }

    /**
     * 保存记录
     *
     * 注意:
     * 每次都产生新节点,
     * 有则更新无则添加.
     *
     * @param id
     * @param rd
     * @return 有更新为 1, 无更新为 0
     * @throws HongsException
     */
    @Override
    public int put(String id, Map rd) throws HongsException {
        // 合并新旧数据
        int i  = 0;
        int t  = 2;
        Map dd = get(id);
        if (dd.isEmpty()) t = 1;
        Map<String,Map> fs = getFields();
        for(String fn : fs . keySet( ) ) {
            if (  "id". equals(fn)) {
                dd.put(fn , id);
            } else
            if (rd.containsKey(fn)) {
                Object fr = rd.get(fn);
                Object fo = dd.get(fn);
                dd.put(fn , fr);
                // 跳过环境字段, 比如修改时间
                if (! canSkip(fn,fr,fo)) {
                    i ++;
                }
            }
        }
        // 无更新不存储
        if (i == 0) {
          return 0;
        }

        // 构建文档对象, 会重组 name,word 的取值, 用于下方记录
        dd.put(Cnst.ID_KEY , id);
        Document dc = padDoc(dd);

        // 保存到数据库
        Table table = getTable();
        if (table != null) {
            String   fid   = getFormId();
            String   sid   = getSiteId();
            String   uid   = getUserId();
            long     ctime = System.currentTimeMillis() / 1000;
            Object[] param = new String[] {id, fid, "0"};
            String   where = "`id`=? AND `form_id`=? AND `site_id`=? AND `etime`=?";

            //** 检查记录状态 **/

            if (t == 2) {
                Map od = table.fetchCase()
                    .filter( where,param )
                    .select("ctime,state")
                    .getOne( );
                if (! od.isEmpty()) {
                    if ( Synt.declare ( od.get("state"), 0  )  ==   0    ) {
                        throw new HongsException(0x1104, "记录已经删除了");
                    }
                    if ( Synt.declare ( od.get("ctime"), 0L )  >=  ctime ) {
                        throw new HongsException(0x1100, "等会儿, 不要急");
                    }
                }
            } else {
                Map od = table.fetchCase()
                    .filter( where,param )
                    .select("ctime,state,data")
                    .getOne( );
                if (! od.isEmpty()) {
                    if ( Synt.declare ( od.get("state"), 0  )  ==   0    ) {
                        throw new HongsException(0x1104, "记录已经删除了");
                    }
                    if ( Synt.declare ( od.get("ctime"), 0L )  >=  ctime ) {
                        throw new HongsException(0x1100, "等会儿, 不要急");
                    }

                    // 用快照补全数据
                    Map<Object, Object> bd = (Map) Dawn.toObject( (String) od.get("data"));
                    for (Map.Entry et : bd.entrySet()) {
                        Object k = et.getKey( );
                        if (dd.containsKey(k)
                        || !fs.containsKey(k) ) {
                            continue;
                        }
                        dd.put(k,et.getValue());
                    }
                    t = 2 ;
                }
            }

            //** 保存到数据库 **/

            Map ud = new HashMap();
            Map nd = new HashMap();
            ud.put("etime", ctime);
            nd.put("ctime", ctime);
            nd.put("etime",   0  );
            nd.put("state",   t  );
            nd.put(     "id", id );
            nd.put("form_id", fid);
            nd.put("site_id", sid);
            nd.put("user_id", uid);

            // 数据快照和日志标题
            nd.put("data", Dawn.toString(dd, true));
            nd.put("name",     cutText(dd, "name"));

            // 操作备注和终端代码
            if (rd.containsKey("memo")) {
                nd.put("memo", cutText(rd, "memo"));
            }
            if (rd.containsKey("meno")) {
                nd.put("meno", cutText(rd, "meno"));
            }

            table.update(ud, where, param);
            table.insert(nd);
        }

        // 保存到索引库
        setDoc(id, dc);
        call(id, "update");
        return 1;
    }

    /**
     * 删除记录
     * @param id
     * @param rd
     * @return 有更新为 1, 无更新为 0
     * @throws HongsException
     */
    @Override
    public int del(String id, Map rd) throws HongsException {
        Table table = getTable();
        if (table == null) {
            delDoc(id); return 1;
        }

        String   fid   = getFormId();
        String   sid   = getSiteId();
        String   uid   = getUserId();
        long     ctime = System.currentTimeMillis() / 1000;
        Object[] param = new String[] {id, fid, "0"};
        String   where = "`id`=? AND `form_id`=? AND `site_id`=? AND `etime`=?";

        //** 检查记录状态 **/

        Map od = table.fetchCase()
            .filter( where,param )
            .select("ctime,state,data,name")
            .getOne( );
        if (od.isEmpty()) {
             delDoc( id ); return 0; // 规避关系库无而搜索库有
        //  throw new HongsException(0x1104, "找不到原始记录");
        }
        if ( Synt.declare ( od.get("state"), 0  )  ==   0    ) {
             delDoc( id ); return 0; // 删除是幂等的可重复调用
        //  throw new HongsException(0x1100, "禁操作删除记录");
        }
        if ( Synt.declare ( od.get("ctime"), 0L )  >=  ctime ) {
            throw new HongsException(0x1100, "等会儿, 不要急");
        }

        //** 保存到数据库 **/

        Map ud = new HashMap();
        Map nd = new HashMap();
        ud.put("etime", ctime);
        nd.put("ctime", ctime);
        nd.put("etime",   0  );
        nd.put("state",   0  );
        nd.put(     "id", id );
        nd.put("form_id", fid);
        nd.put("site_id", sid);
        nd.put("user_id", uid);

        // 拷贝快照和日志标题
        nd.put("data", od.get("data"));
        nd.put("name", od.get("name"));

        // 操作备注和终端代码
        if (rd.containsKey("memo")) {
            nd.put("memo", cutText(rd, "memo"));
        }
        if (rd.containsKey("meno")) {
            nd.put("meno", cutText(rd, "meno"));
        }

        table.update(ud, where, param);
        table.insert(nd);

        //** 从索引库删除 **/

        delDoc(id);
        call(id, "delete");
        return 1;
    }

    /**
     * 恢复记录
     * @param id
     * @param rd
     * @return 有更新为 1, 无更新为 0
     * @throws HongsException
     */
    @Override
    public int rev(String id, Map rd) throws HongsException {
        Table table = getTable();
        if (table == null) {
            throw new HongsException(0x1100, "资源不支持恢复");
        }

        String   fid   = getFormId();
        String   sid   = getSiteId();
        String   uid   = getUserId();
        long     ctime = System.currentTimeMillis() / 1000 ;
        long     rtime = Synt.declare (rd.get("rtime"), 0L);
        Object[] param = new String[] {id, fid, "0" };
        String   where = "`id`=? AND `form_id`=? AND `site_id`=? AND `etime`=?";
        Object[] para2 = new Object[] {id, fid,rtime};
        String   wher2 = "`id`=? AND `form_id`=? AND `site_id`=? AND `ctime`=?";

        //** 获取旧的数据 **/

        Map od = table.fetchCase()
            .filter( where, param)
            .select("ctime")
            .getOne( );
        if (od.isEmpty()) {
        //  throw new HongsException(0x1104, "找不到原始记录");
        } else
        if ( Synt.declare ( od.get("ctime"), 0L )  >=  ctime ) {
            throw new HongsException(0x1100, "等会儿, 不要急");
        }
        Map nd = table.fetchCase()
            .filter( wher2, para2)
        //  .assort("ctime  DESC")
            .getOne( );
        if (nd.isEmpty()) {
            throw new HongsException(0x1100, "找不到恢复起源");
        }
        // 删除时保留的是删除前的快照, 即使为最终记录仍然可以恢复
        if ( Synt.declare ( nd.get("state"), 0  )  !=   0    ) {
        if ( Synt.declare ( nd.get("etime"), 0L )  ==   0L   ) {
            throw new HongsException(0x1100, "这已是最终记录");
        }}

        //** 保存到数据库 **/

        Map ud = new HashMap();
        ud.put("etime", ctime);
        nd.put("ctime", ctime);
        nd.put("rtime", rtime);
        nd.put("etime",   0  );
        nd.put("state",   3  );
        nd.put("form_id", fid);
        nd.put("site_id", sid);
        nd.put("user_id", uid);

        // 操作备注和终端代码
        if (rd.containsKey("memo")) {
            nd.put("memo", cutText(rd, "memo"));
        }
        if (rd.containsKey("meno")) {
            nd.put("meno", cutText(rd, "meno"));
        }

        table.update(ud, where, param);
        table.insert(nd);

        //** 保存到索引库 **/

        Map dd = (Map) Dawn.toObject((String) nd.get("data"));

        dd.put(Cnst.ID_KEY , id);
        Document dc = padDoc(dd);

        setDoc(id, dc);
        call(id, "revert");
        return 1;
    }

    /**
     * 外部回调
     * @param id
     * @param on
     * @return 有回调为 1, 无回调为 0
     * @throws HongsException
     */
    public int call(String id, String on) throws HongsException {
        String url = (String) getParams().get("callback");
        if (url == null || "".equals(url)) {
            return 0;
        }

        String fid = getFormId();
        String sid = getSiteId();
        dcUrls.put(id, Syno.inject(url, Synt.mapOf(
            "form_id", fid,
            "site_id", sid,
            "id"     , id ,
            "type"   , on
        )));

        return 1 ;
    }

    @Override
    public void close() {
        super . close();

        // 离开时通知第三方
        try {
            MosaicCaller dc = MosaicCaller.getInstance();
            for(String du : dcUrls.values()) {
                dc.add(du);
            }
        } catch (HongsException e) {
            throw e.toExemption( );
        } finally {
            dcUrls.clear();
        }
    }

}
