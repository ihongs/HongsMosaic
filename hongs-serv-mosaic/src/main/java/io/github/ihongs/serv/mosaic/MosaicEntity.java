package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Cnst;
import io.github.ihongs.Core;
import io.github.ihongs.HongsException;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.db.Table;
import io.github.ihongs.serv.matrix.Data;
import io.github.ihongs.util.Synt;
import io.github.ihongs.util.Tool;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.document.Document;

/**
 * 数据基类
 * 按照用户存放
 * @author hong
 */
public abstract class MosaicEntity extends Data {

    private String unitId = null;
    private final Set<String> mcUrls = new LinkedHashSet();

    protected MosaicEntity(String conf, String form) {
        super(conf, form);
    }

    @Override
    public String getDbPath() {
        String path = "mosaic/" + getUnitId();
        path = Synt.declare(getParams().get("db-path"),path);

        // 进一步处理路径
        Map m = new HashMap();
        m.put("SERVER_ID", Core.SERVER_ID);
        m.put("CORE_PATH", Core.CORE_PATH);
        m.put("DATA_PATH", Core.DATA_PATH);
        m.put("UNIT_ID"  , getUnitId ( ) );
        path = Tool.inject(path, m);
        if ( ! new File(path).isAbsolute())
        path = Core.DATA_PATH + "/lucene/" + path;

        return path;
    }

    public String getUnitId() {
        if ( null == unitId ) {
            try {
                unitId = (String) ActionHelper.getInstance()
                                . getSessibute(Cnst.UID_SES);
            } catch (UnsupportedOperationException e ) {
                throw new NullPointerException("Call setUserId first");
            }
            if ( null == unitId ) {
                throw new NullPointerException("Call setUserId first");
            }
        }
        return unitId;
    }

    public void setUnitId(String cuId) {
        unitId = cuId;
    }

    /**
     * 保存记录
     * @param ctime
     * @param id
     * @param rd
     * @return 有更新为 1, 无更新为 0
     * @throws HongsException
     */
    @Override
    public int save(long ctime, String id, Map rd) throws HongsException {
        Table    table = getTable( );
        String   fid   = getFormId();
        String   nid   = getUnitId();
        String   uid   = getUserId();
        Object[] param = new String[] {id, fid, nid, "0"};
        String   where = "`id`=? AND `form_id`=? AND `unit_id=?` AND `etime`=?";

        // 获取旧的数据
        int st ;
        Map dd = get( id );
        if (! dd.isEmpty() ) {
            st = 2; // 更新
            if (table != null) {
                Map od = table.fetchCase( )
                    .filter( where, param )
                    .select("ctime, state")
                    .getOne( );
                if (! od.isEmpty()) {
                    if ( Synt.declare ( od.get("state"), 0  )  ==   0    ) {
                        throw new HongsException(0x1104, "记录已经删除了");
                    }
                    if ( Synt.declare ( od.get("ctime"), 0L )  >=  ctime ) {
                        throw new HongsException(0x1100, "等会儿, 不要急");
                    }
                }
            }
        } else {
            st = 1; // 创建
            if (table != null) {
                Map od = table.fetchCase( )
                    .filter( where, param )
                    .select("ctime, state, data")
                    .getOne( );
                if (! od.isEmpty()) {
                    if ( Synt.declare ( od.get("state"), 0  )  ==   0    ) {
                        throw new HongsException(0x1104, "记录已经删除了");
                    }
                    if ( Synt.declare ( od.get("ctime"), 0L )  >=  ctime ) {
                        throw new HongsException(0x1100, "等会儿, 不要急");
                    }
                    dd = (Map) io.github.ihongs.util.Data.toObject(od.get("data").toString());
                    st = 2; // 有快照也算更新
                }
            }
        }

        // 合并新旧数据
        int i  = 0;
        Map<String,Map> fields = getFields();
        for(String fn : fields . keySet( ) ) {
            if (  "id". equals(fn)) {
                dd.put(fn , id);
            } else
            if (rd.containsKey(fn)) {
                Object fr = rd.get(fn);
                Object fo = dd.get(fn);
                dd.put(fn , fr);
                // 跳过环境字段, 比如修改时间
                if (! canSkip (fn, fr, fo) ) {
                    i ++;
                }
            }
        }
        // 无更新不存储
        if (i == 0) {
          return 0;
        }

        //** 保存到数据库 **/

        if (table != null) {
            Map ud = new HashMap();
            ud.put("etime", ctime);

            Map nd = new HashMap();
            nd.put("ctime", ctime);
            nd.put("etime",   0  );
            nd.put("state",   st );
            nd.put(/***/"id", id );
            nd.put("form_id", fid);
            nd.put("unit_id", nid);
            nd.put("user_id", uid);
            nd.put("memo", rd.get("memo"));
            nd.put("name", dd.get("name"));
            nd.put("data", io.github.ihongs.util.Data.toString(dd, true));

            table.update(ud, where, param);
            table.insert(nd);
        }

        //** 保存到索引库 **/

        Document doc = new Document();
        dd.put(Cnst.ID_KEY, id);
        padDoc(doc, dd , null );
        setDoc(id, doc);

        return 1;
    }

    /**
     * 删除记录
     * @param ctime
     * @param id
     * @param rd
     * @return 有更新为 1, 无更新为 0
     * @throws HongsException
     */
    @Override
    public int drop(long ctime, String id, Map rd) throws HongsException {
        Table    table = getTable( );
        String   fid   = getFormId();
        String   nid   = getUnitId();
        String   uid   = getUserId();
        Object[] param = new String[] {id, fid, nid, "0"};
        String   where = "`id`=? AND `form_id`=? AND `unit_id`=? AND `etime`=?";

        /** 记录到数据库 **/

        if (table != null) {
            Map dd = table.fetchCase()
                .filter( where, param)
                .select("ctime, state, name, data")
                .getOne( );
            if (dd.isEmpty()) {
                 delDoc( id ); return 0; // 规避关系库无而搜索库有
//              throw new HongsException(0x1104, "找不到原始记录");
            }
            if ( Synt.declare ( dd.get("state"), 0  )  ==   0    ) {
                 delDoc( id ); return 0; // 删除是幂等的可重复调用
//              throw new HongsException(0x1100, "禁操作删除记录");
            }
            if ( Synt.declare ( dd.get("ctime"), 0L )  >=  ctime ) {
                throw new HongsException(0x1100, "等会儿, 不要急");
            }

            Map ud = new HashMap();
            ud.put("etime", ctime);

            Map nd = new HashMap();
            nd.put("ctime", ctime);
            nd.put("etime",   0  );
            nd.put("state",   0  );
            nd.put(/***/"id", id );
            nd.put("form_id", fid);
            nd.put("unit_id", nid);
            nd.put("user_id", uid);
            nd.put("memo", rd.get("memo"));
            nd.put("name", dd.get("name"));
            nd.put("data", dd.get("data"));

            table.update(ud, where, param);
            table.insert(nd);
        }

        //** 从索引库删除 **/

        delDoc(id);

        return 1;
    }

    /**
     * 恢复记录
     * @param ctime
     * @param id
     * @param rd
     * @return 有更新为 1, 无更新为 0
     * @throws HongsException
     */
    @Override
    public int redo(long ctime, String id, Map rd) throws HongsException {
        Table    table = getTable( );
        String   fid   = getFormId();
        String   nid   = getUnitId();
        String   uid   = getUserId();
        long     rtime = Synt.declare (rd.get("rtime"), 0L);
        Object[] param = new String[] {id, fid, nid, "0"  };
        String   where = "`id`=? AND `form_id`=? AND `unit_id`=? AND `etime`=?";
        Object[] para2 = new Object[] {id, fid, nid, rtime};
        String   wher2 = "`id`=? AND `form_id`=? AND `unit_id`=? AND `ctime`=?";

        //** 获取旧的数据 **/

        if (table == null) {
            throw new HongsException(0x1100, "资源不支持恢复");
        }
        Map ld = table.fetchCase()
            .filter( where, param)
            .select("ctime")
            .getOne( );
        if ( Synt.declare ( ld.get("ctime"), 0L )  >=  ctime ) {
            throw new HongsException(0x1100, "等会儿, 不要急");
        }
        Map dd = table.fetchCase()
            .filter( wher2, para2)
            .getOne( );
        if (dd.isEmpty()) {
            throw new HongsException(0x1100, "找不到恢复起源");
        }
        // 删除时保留的是删除前的快照, 即使为最终记录仍然可以恢复
        if ( Synt.declare ( dd.get("state"), 0  )  !=   0    ) {
        if ( Synt.declare ( dd.get("etime"), 0L )  ==   0L   ) {
            throw new HongsException(0x1100, "这已是最终记录");
        }}

        //** 保存到数据库 **/

        Map ud = new HashMap();
        ud.put("etime", ctime);

        dd.put("ctime", ctime);
        dd.put("rtime", rtime);
        dd.put("etime",   0  );
        dd.put("state",   3  );
        dd.put("form_id", fid);
        dd.put("unit_id", nid);
        dd.put("user_id", uid);
        dd.put("memo" , rd.get("memo"));

        table.update(ud , where, param);
        table.insert(dd);

        //** 保存到索引库 **/

        dd = (Map) io.github.ihongs.util.Data.toObject(dd.get("data").toString());

        Document doc = new Document(  );
        dd.put(Cnst.ID_KEY, id);
        padDoc(doc, dd , null );
        setDoc(id, doc);

        return 1;
    }

    /**
     * 外部回调
     * @param xtime
     * @param id
     * @param on
     * @return 有回调为 1, 无回调为 0
     * @throws HongsException
     */
    @Override
    public int call(long xtime, String id, String on) throws HongsException {
        String url = (String) getParams().get("callback");
        if (url == null || "".equals(url)) {
            return 0;
        }

        String fid = getFormId();
        String nid = getFormId();
        dcUrls.add(Tool.inject(url, Synt.mapOf(
            "form_id", fid,
            "unit_id", nid,
            "id"     , id ,
            "type"   , on ,
            "time"   , xtime
        )));

        return 1 ;
    }

}
