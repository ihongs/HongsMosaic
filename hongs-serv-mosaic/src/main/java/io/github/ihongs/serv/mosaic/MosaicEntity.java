package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Cnst;
import io.github.ihongs.Core;
import io.github.ihongs.HongsException;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.db.DB;
import io.github.ihongs.db.Model;
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
    public Model getModel() throws HongsException {
        String tn = Synt.declare(getParams().get("db-model"), "mosaic.data");
        if ("".equals(tn) || "none".equals(tn)) {
            return null;
        }
        return DB.getInstance("matrix").getModel(tn);
    }

    @Override
    public Table getTable() throws HongsException {
        String tn = Synt.declare(getParams().get("db-table"), "mosaic.data");
        if ("".equals(tn) || "none".equals(tn)) {
            return null;
        }
        return DB.getInstance("matrix").getTable(tn);
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
        Map    dd = new HashMap();
        padInf(dd , rd);

        // 构建文档对象
        dd.put(Cnst.ID_KEY , id);
        Document dc = padDoc(dd);

        // 保存到数据库
        Table table = getTable();
        long  ctime = System.currentTimeMillis() / 1000;
        if (table != null) {
            String   fid   = getFormId();
            String   uid   = getUserId();

            Map nd = new HashMap();
            nd.put("ctime", ctime);
            nd.put("etime",   0  );
            nd.put("state",   1  );
            nd.put(     "id", id );
            nd.put("form_id", fid);
            nd.put("user_id", uid);

            // 数据快照和日志标题
            nd.put("data", Dawn.toString(dd, true));
            nd.put("name",     getText(dd, "name"));

            // 操作备注和终端代码
            if (rd.containsKey("memo")) {
                nd.put("memo", getText(rd, "memo"));
            }
            if (rd.containsKey("meno")) {
                nd.put("meno", getText(rd, "meno"));
            }

            table.insert(nd);
        }

        // 保存到索引库
        setDoc(id, dc);
        call  (id, "create", ctime);
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
        Map dd = get(id);
        int t  = dd.isEmpty()? 1:2 ;
        int i  = padInf ( dd , rd );
        // 无更新不存储
        if (i == 0) {
          return 0;
        }

        // 构建文档对象
        dd.put(Cnst.ID_KEY , id);
        Document dc = padDoc(dd);

        // 保存到数据库
        Table table = getTable();
        long  ctime = System.currentTimeMillis() / 1000;
        if (table != null) {
            String   fid   = getFormId();
            String   uid   = getUserId();
            Object[] param = new String[] {id, fid, "0"};
            String   where = "`id`=? AND `form_id`=? AND `etime`=?";

            Map nd = table.fetchCase()
                .filter( where,param )
                .select("ctime,state")
                .getOne( );
            if (! nd.isEmpty()) {
                if (Synt.declare(nd.get("state"), 0 ) ==  0   ) {
                    throw new HongsException(404, "Data item '"+id+"' is removed in "+getDbName())
                        .setLocalizedContent("matrix.item.is.removed")
                        .setLocalizedContext("matrix");
                } /* 没有新增, 不必限时
                if (Synt.declare(nd.get("ctime"), 0L ) >= ctime) {
                    throw new HongsException(400, "Wait 1 second to put '"+id+"' in "+getDbName())
                        .setLocalizedContent("matrix.wait.one.second")
                        .setLocalizedContext("matrix");
                } */
            } else {
                nd.put("ctime", ctime);
                nd.put("etime",   0  );
                nd.put("state",   t  );
                nd.put(     "id", id );
                nd.put("form_id", fid);
                nd.put("user_id", uid);
            }

            // 数据快照和日志标题
            nd.put("data", Dawn.toString(dd, true));
            nd.put("name",     getText(dd, "name"));

            // 操作备注和终端代码
            if (rd.containsKey("memo")) {
                nd.put("memo", getText(rd, "memo"));
            }
            if (rd.containsKey("meno")) {
                nd.put("meno", getText(rd, "meno"));
            }

            if (nd.containsKey("etime") == false ) {
                table.update(nd, where, param);
            } else {
                table.insert(nd);
            }
        }

        // 保存到索引库
        setDoc(id, dc);
        call  (id, "update", ctime);
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
        Map dd = get(id);
        int t  = dd.isEmpty()? 1:2 ;
        int i  = padInf ( dd , rd );
        // 无更新不存储
        if (i == 0) {
          return 0;
        }

        // 构建文档对象
        dd.put(Cnst.ID_KEY , id);
        Document dc = padDoc(dd);

        // 保存到数据库
        Table table = getTable();
        long  ctime = System.currentTimeMillis() / 1000;
        if (table != null) {
            String   fid   = getFormId();
            String   uid   = getUserId();
            Object[] param = new String[] {id, fid, "0"};
            String   where = "`id`=? AND `form_id`=? AND `etime`=?";

            //** 检查记录状态 **/

            Map od = table.fetchCase()
                .filter( where,param )
                .select("ctime,state")
                .getOne( );
            if (! od.isEmpty()) {
                if (Synt.declare(od.get("state"), 0  ) ==  0   ) {
                    throw new HongsException(404, "Data item '"+id+"' is removed in "+getDbName())
                        .setLocalizedContent("matrix.item.is.removed")
                        .setLocalizedContext("matrix");
                }
                if (Synt.declare(od.get("ctime"), 0L ) >= ctime) {
                    throw new HongsException(400, "Wait 1 second to put '"+id+"' in "+getDbName())
                        .setLocalizedContent("matrix.wait.one.second")
                        .setLocalizedContext("matrix");
                }
            }

            //** 保存到数据库 **/

            Map ud = new HashMap();
            ud.put("etime", ctime);

            Map nd = new HashMap();
            nd.put("ctime", ctime);
            nd.put("etime",   0  );
            nd.put("state",   t  );
            nd.put(     "id", id );
            nd.put("form_id", fid);
            nd.put("user_id", uid);

            // 数据快照和日志标题
            nd.put("data", Dawn.toString(dd, true));
            nd.put("name",     getText(dd, "name"));

            // 操作备注和终端代码
            if (rd.containsKey("memo")) {
                nd.put("memo", getText(rd, "memo"));
            }
            if (rd.containsKey("meno")) {
                nd.put("meno", getText(rd, "meno"));
            }

            table.update(ud, where, param);
            table.insert(nd);
        }

        // 保存到索引库
        setDoc(id, dc);
        call  (id, "update", ctime);
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
            delDoc(id);
            return 1;
        }

        String   fid   = getFormId();
        String   uid   = getUserId();
        long     ctime = System.currentTimeMillis() / 1000;
        Object[] param = new String[] {id, fid, "0"};
        String   where = "`id`=? AND `form_id`=? AND `etime`=?";

        //** 检查记录状态 **/

        Map od = table.fetchCase()
            .filter( where,param )
            .select("ctime,state,data,name")
            .getOne( );
        if (od.isEmpty()) {
             delDoc( id ); return 0; // 规避关系库无而搜索库有
        }
        if (Synt.declare(od.get("state"), 0  ) ==  0   ) {
             delDoc( id ); return 0; // 删除是幂等的可重复调用
        }
        if (Synt.declare(od.get("ctime"), 0L ) >= ctime) {
            throw new HongsException(400, "Wait 1 second to del '"+id+"' in "+getDbName())
                .setLocalizedContent("matrix.wait.one.second")
                .setLocalizedContext("matrix");
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
        nd.put("user_id", uid);

        // 拷贝快照和日志标题
        nd.put("data", od.get("data"));
        nd.put("name", od.get("name"));

        // 操作备注和终端代码
        if (rd.containsKey("memo")) {
            nd.put("memo", getText(rd, "memo"));
        }
        if (rd.containsKey("meno")) {
            nd.put("meno", getText(rd, "meno"));
        }

        table.update(ud, where, param);
        table.insert(nd);

        //** 从索引库删除 **/

        delDoc(id);
        call  (id, "delete", ctime);
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
            throw new HongsException(405, "Data table for '"+getDbName()+"' is not exists")
                .setLocalizedContent("matrix.rev.unsupported")
                .setLocalizedContext("matrix");
        }

        String   fid   = getFormId();
        String   uid   = getUserId();
        long     ctime = System.currentTimeMillis() / 1000 ;
        long     rtime = Synt.declare (rd.get("rtime"), 0L);
        Object[] param = new String[] {id, fid, "0" };
        String   where = "`id`=? AND `form_id`=? AND `etime`=?";
        Object[] para2 = new Object[] {id, fid,rtime};
        String   wher2 = "`id`=? AND `form_id`=? AND `ctime`=?";

        //** 获取旧的数据 **/

        Map od = table.fetchCase()
            .filter( where, param)
            .select("ctime")
            .getOne( );
        if (od.isEmpty()) {
        //  throw new HongsException(404, "Can not find current '"+id+"' in "+getDbName())
        //      .setLocalizedContent("matrix.wait.one.second")
        //      .setLocalizedContext("matrix");
        } else
        if (Synt.declare(od.get("ctime"), 0L ) >= ctime) {
            throw new HongsException(400, "Wait 1 second to del '"+id+"' in "+getDbName())
                .setLocalizedContent("matrix.wait.one.second")
                .setLocalizedContext("matrix");
        }

        Map nd = table.fetchCase()
            .filter( wher2, para2)
        //  .assort("ctime  DESC")
            .getOne( );
        if (nd.isEmpty()) {
            throw new HongsException(404, "Empty '"+id+"' at '"+ctime+"' in "+getDbName())
                .setLocalizedContent("matrix.node.not.exists")
                .setLocalizedContext("matrix");
        }
        // 删除时保留的是删除前的快照, 即使为最终记录仍然可以恢复
        if (Synt.declare(nd.get("state"), 0  ) !=  0   ) {
        if (Synt.declare(nd.get("etime"), 0L ) ==  0L  ) {
            throw new HongsException(400, "Alive '"+id+"' at '"+ctime+"' in "+getDbName())
                .setLocalizedContent("matrix.node.is.current")
                .setLocalizedContext("matrix");
        }}

        //** 保存到数据库 **/

        Map ud = new HashMap();
        ud.put("etime", ctime);
        nd.put("ctime", ctime);
        nd.put("rtime", rtime);
        nd.put("etime",   0  );
        nd.put("state",   3  );
        nd.put("form_id", fid);
        nd.put("user_id", uid);

        // 操作备注和终端代码
        if (rd.containsKey("memo")) {
            nd.put("memo", getText(rd, "memo"));
        }
        if (rd.containsKey("meno")) {
            nd.put("meno", getText(rd, "meno"));
        }

        table.update(ud, where, param);
        table.insert(nd);

        //** 保存到索引库 **/

        Map dd = (Map) Dawn.toObject((String) nd.get("data"));

        dd.put(Cnst.ID_KEY , id);
        Document dc = padDoc(dd);

        setDoc(id, dc);
        call  (id, "revert", ctime);
        return 1;
    }

    /**
     * 外部回调
     * @param id
     * @param on
     * @param ct
     * @return 有回调为 1, 无回调为 0
     * @throws HongsException
     */
    public int call(String id, String on, long ct) throws HongsException {
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
