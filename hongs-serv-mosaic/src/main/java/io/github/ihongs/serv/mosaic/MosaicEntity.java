package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Cnst;
import io.github.ihongs.Core;
import io.github.ihongs.HongsException;
import io.github.ihongs.HongsExemption;
import io.github.ihongs.db.DB;
import io.github.ihongs.db.Model;
import io.github.ihongs.db.Table;
import io.github.ihongs.serv.matrix.Data;
import io.github.ihongs.util.Dist;
import io.github.ihongs.util.Synt;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;

/**
 * 模型基类
 * 按照用户存放
 * @author Hongs
 */
public class MosaicEntity extends Data {

    /**
     * 分站字段名
     */
    static public final String SITE_ID_KEY = "sd";

    /**
     * 分站ID
     */
    private String site = null;

    protected MosaicEntity(String conf, String form, String site) {
        super(conf , form);
        this. site = site ;
    }
    
    public static MosaicEntity getInstance(String conf, String form, String site) {
        String name = MosaicEntity.class.getName() +":"+ conf +":"+ form +":"+ site;
        return Core.getInstance().got(name, ()->new MosaicEntity(conf, form, site));
    }

    public String getSiteId() {
        return site;
    }

    @Override
    public Model getModel() throws HongsException {
        String tn = Synt.declare(getParams().get("db-model"), "mosaic.data");
        if ("".equals(tn) || "none".equals(tn)) {
            return null;
        }
        return DB.getInstance("mosaic").getModel(tn);
    }

    @Override
    public Table getTable() throws HongsException {
        String tn = Synt.declare(getParams().get("db-table"), "mosaic.data");
        if ("".equals(tn) || "none".equals(tn)) {
            return null;
        }
        return DB.getInstance("mosaic").getTable(tn);
    }

    @Override
    protected void padQry(BooleanQuery.Builder qr, Map rd) throws HongsException {
        // 限定站点范围
        String sd = getSiteId();
        if (null != sd && ! sd.isEmpty( )) {
            qr.add(new TermQuery(new Term("@"+SITE_ID_KEY, sd)), BooleanClause.Occur.MUST);
        }

        super.padQry( qr , rd );
    }

    @Override
    protected void padDoc(Document doc, Map map, Set rep) {
        // 写入分区标识
        String sd = getSiteId();
        if (null != sd && ! sd.isEmpty( )) {
            doc.add(new StringField("@"+SITE_ID_KEY, sd, Field.Store.NO));
            doc.add(new StoredField(/**/SITE_ID_KEY, sd));
        } else {
            throw new HongsExemption( "siteId required" );
        }

        super.padDoc(doc, map, rep);
    }

    /**
     * 添加记录
     * @param id
     * @param rd
     * @param ctime
     * @return
     * @throws HongsException
     */
    @Override
    public int add(String id, Map rd, long ctime) throws HongsException {
        Map dd = new HashMap();
        padInf(dd, rd);

        // 保存到文档库
        dd.put(Cnst.ID_KEY , id);
        Document dc = padDoc(dd);
        addDoc(id, dc);

        Table table = getTable();
        if (table == null) {
            return 1;
        }

        String   uid   = getUserId();
        String   fid   = getFormId();
        String   sid   = getSiteId();

        Map nd = new HashMap();
        nd.put("ctime", ctime);
        nd.put("etime",   0  );
        nd.put("state",   1  );
        nd.put(     "id", id );
        nd.put("form_id", fid);
        nd.put("site_id", sid);
        nd.put("user_id", uid);

        // 数据快照和日志标题
        nd.put("__data__", dd);
        nd.put("data", Dist.toString(dd,  true ));
        nd.put("name", this.getText (dd, "name"));

        // 操作备注和终端代码
        if (rd.containsKey("memo")) {
            nd.put("memo", getText(rd, "memo"));
        }
        if (rd.containsKey("meno")) {
            nd.put("meno", getText(rd, "meno"));
        }

        table.insert(nd);
        return 1;
    }

    /**
     * 更新记录
     *
     * 注意:
     * 有则更新无则添加,
     * 每次都产生新节点.
     *
     * @param id
     * @param rd
     * @param ctime
     * @return 有更新为 1, 无更新为 0
     * @throws HongsException
     */
    @Override
    public int put(String id, Map rd, long ctime) throws HongsException {
        Map dd = get(id);
        int t  = dd.isEmpty()? 1: 2;
        int i  = padInf(dd , rd);
        // 无更新不存储
        if (i  ==  0) {
            return 0;
        }

        // 保存到文档库
        dd.put(Cnst.ID_KEY , id);
        Document dc = padDoc(dd);
        setDoc(id, dc);

        Table table = getTable();
        if (table == null) {
            return 1;
        }

        String   uid   = getUserId();
        String   fid   = getFormId();
        String   sid   = getSiteId();
        Object[] param = new String[] {id, fid, sid, "0"};
        String   where = "`id`=? AND `form_id`=? AND `site_id`=? AND `etime`=?";

        Map od = table.fetchCase()
            .filter( where,param )
            .select("ctime,state")
            .getOne( );
        if (! od.isEmpty()) {
            if (Synt.declare(od.get("state"), 0  ) ==  0   ) {
                throw new HongsException(404, "@matrix:matrix.item.is.removed", getDbName(), id);
            }
            if (Synt.declare(od.get("ctime"), 0L ) >= ctime) {
                throw new HongsException(400, "@matrix:matrix.wait.one.second", getDbName(), id);
            }
        }

        Map ud = new HashMap();
        ud.put("etime", ctime);

        Map nd = new HashMap();
        nd.put("ctime", ctime);
        nd.put("etime",   0  );
        nd.put("state",   t  );
        nd.put(     "id", id );
        nd.put("form_id", fid);
        nd.put("site_id", sid);
        nd.put("user_id", uid);

        // 数据快照和日志标题
        nd.put("__data__", dd);
        nd.put("data", Dist.toString(dd,  true ));
        nd.put("name", this.getText (dd, "name"));

        // 操作备注和终端代码
        if (rd.containsKey("memo")) {
            nd.put("memo", getText(rd, "memo"));
        }
        if (rd.containsKey("meno")) {
            nd.put("meno", getText(rd, "meno"));
        }

        table.update(ud, where, param);
        table.insert(nd);

        return 1;
    }

    /**
     * 保存记录
     *
     * 注意:
     * 有则更新无则添加,
     * ctime = 0 仅更新.
     *
     * @param id
     * @param rd
     * @param ctime
     * @return 有更新为 1, 无更新为 0
     * @throws HongsException
     */
    @Override
    public int set(String id, Map rd, long ctime) throws HongsException {
        Map dd = get(id);
        int t  = dd.isEmpty()? 1: 2;
        int i  = padInf(dd , rd);
        // 无更新不存储
        if (i  ==  0) {
            return 0;
        }

        // 保存到文档库
        dd.put(Cnst.ID_KEY , id);
        Document dc = padDoc(dd);
        setDoc(id, dc);

        Table table = getTable();
        if (table == null) {
            return 1;
        }

        String   uid   = getUserId();
        String   fid   = getFormId();
        String   sid   = getSiteId();
        Object[] param = new String[] {id, fid, sid, "0"};
        String   where = "`id`=? AND `form_id`=? AND `site_id`=? AND `etime`=?";

        Map od = table.fetchCase()
            .filter( where,param )
            .select("ctime,state")
            .getOne( );
        if (! od.isEmpty()) {
            if (Synt.declare(od.get("state"), 0  ) ==  0   ) {
                throw new HongsException(404, "@matrix:matrix.item.is.removed", getDbName(), id);
            }
            if (Synt.declare(od.get("ctime"), 0L ) >= ctime) {
            //  throw new HongsException(400, "@matrix:matrix.wait.one.second", getDbName(), id);
                ctime = 0;
            }
        }

        // 仅对最新节点作更新
        if (ctime == 0) {
            Map nd = new HashMap();

            // 数据快照和日志标题
            nd.put("__data__", dd);
            nd.put("data", Dist.toString(dd,  true ));
            nd.put("name", this.getText (dd, "name"));

            // 操作备注和终端代码
            if (rd.containsKey("memo")) {
                nd.put("memo", getText(rd, "memo"));
            }
            if (rd.containsKey("meno")) {
                nd.put("meno", getText(rd, "meno"));
            }

            table.update(nd, where, param);
            return 1;
        }

        Map ud = new HashMap();
        ud.put("etime", ctime);

        Map nd = new HashMap();
        nd.put("ctime", ctime);
        nd.put("etime",   0  );
        nd.put("state",   t  );
        nd.put(     "id", id );
        nd.put("form_id", fid);
        nd.put("site_id", sid);
        nd.put("user_id", uid);

        // 数据快照和日志标题
        nd.put("__data__", dd);
        nd.put("data", Dist.toString(dd,  true ));
        nd.put("name", this.getText (dd, "name"));

        // 操作备注和终端代码
        if (rd.containsKey("memo")) {
            nd.put("memo", getText(rd, "memo"));
        }
        if (rd.containsKey("meno")) {
            nd.put("meno", getText(rd, "meno"));
        }

        table.update(ud, where, param);
        table.insert(nd);

        return 1;
    }

    /**
     * 删除记录
     *
     * 注意:
     * 有则删除无则跳过,
     * 成功会产生新节点.
     *
     * @param id
     * @param rd
     * @param ctime
     * @return 有更新为 1, 无更新为 0
     * @throws HongsException
     */
    @Override
    public int del(String id, Map rd, long ctime) throws HongsException {
        delDoc(id);

        Table table = getTable();
        if (table == null) {
            return 1;
        }

        String   uid   = getUserId();
        String   fid   = getFormId();
        String   sid   = getSiteId();
        Object[] param = new String[] {id, fid, sid, "0"};
        String   where = "`id`=? AND `form_id`=? AND `site_id`=? AND `etime`=?";

        Map od = table.fetchCase()
            .filter( where,param )
            .select("ctime,state,data,name")
            .getOne( );
        if (od.isEmpty()
        ||  Synt.declare(od.get("state"), 0  ) ==  0   ) {
            return 0; // 删除是幂等的可重复调用
        }
        if (Synt.declare(od.get("ctime"), 0L ) >= ctime) {
            throw new HongsException(400, "@matrix:matrix.wait.one.second", getDbName(), id);
        }

        Map ud = new HashMap();
        ud.put("etime", ctime);

        Map nd = new HashMap();
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
            nd.put("memo", getText(rd, "memo"));
        }
        if (rd.containsKey("meno")) {
            nd.put("meno", getText(rd, "meno"));
        }

        table.update(ud, where, param);
        table.insert(nd);

        return 1;
    }

    /**
     * 终止记录
     *
     * 注意:
     * 有则删除无则跳过,
     * ctime = 0 仅更新.
     *
     * @param id
     * @param rd
     * @param ctime
     * @return 有更新为 1, 无更新为 0
     * @throws HongsException
     */
    @Override
    public int end(String id, Map rd, long ctime) throws HongsException {
        delDoc(id);

        Table table = getTable();
        if (table == null) {
            return 1;
        }

        String   uid   = getUserId();
        String   fid   = getFormId();
        String   sid   = getSiteId();
        Object[] param = new String[] {id, fid, sid, "0"};
        String   where = "`id`=? AND `form_id`=? AND `site_id`=? AND `etime`=?";

        Map od = table.fetchCase()
            .filter( where,param )
            .select("ctime,state,data,name")
            .getOne( );
        if (od.isEmpty()
        ||  Synt.declare(od.get("state"), 0  ) ==  0   ) {
            return 0; // 删除是幂等的可重复调用
        }
        if (Synt.declare(od.get("ctime"), 0L ) >= ctime) {
        //  throw new HongsException(400, "@matrix:matrix.wait.one.second", getDbName(), id);
            ctime  = 0;
        }

        // 仅对最新节点作更新
        if (ctime == 0) {
            Map nd = new HashMap();
            nd.put("state",   0  );

            // 操作备注和终端代码
            if (rd.containsKey("memo")) {
                nd.put("memo", getText(rd, "memo"));
            }
            if (rd.containsKey("meno")) {
                nd.put("meno", getText(rd, "meno"));
            }

            table.update(nd, where, param);
            return 1;
        }

        Map ud = new HashMap();
        ud.put("etime", ctime);

        Map nd = new HashMap();
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
            nd.put("memo", getText(rd, "memo"));
        }
        if (rd.containsKey("meno")) {
            nd.put("meno", getText(rd, "meno"));
        }

        table.update(ud, where, param);
        table.insert(nd);

        return 1;
    }

    /**
     * 恢复记录
     * @param id
     * @param rd
     * @param ctime
     * @return 有更新为 1, 无更新为 0
     * @throws HongsException
     */
    @Override
    public int rev(String id, Map rd, long ctime) throws HongsException {
        Table table = getTable();
        if (table == null) {
            throw new HongsException(405, "@matrix:matrix.rev.unsupported", getDbName());
        }

        String   uid   = getUserId();
        String   fid   = getFormId();
        String   sid   = getSiteId();
        long     rtime = Synt.declare (rd.get("rtime"), 0L);
        Object[] param = new String[] {id,fid, sid,  "0"  };
        String   where = "`id`=? AND `form_id`=? AND `site_id`=? AND `etime`=?";
        Object[] para2 = new Object[] {id,fid, sid, rtime };
        String   wher2 = "`id`=? AND `form_id`=? AND `site_id`=? AND `ctime`=?";

        // 恢复最终数据
        if (rtime == 0L) {
            Map od = table.fetchCase()
                .filter( where, param)
            //  .assort("etime  DESC")
                .select("state, data")
                .getOne( );
            if (Synt.declare(od.get("state"), 0) > 0) {
                Map dd = (Map) Dist.toObject((String) od.get("data"));
                dd.put(Cnst.ID_KEY , id);
                padInf(dd, dd);
                Document dc = padDoc(dd);
                setDoc(id, dc);
            } else {
                delDoc(id);
            }
            return 0;
        }

        // 获取当前数据
        Map od = table.fetchCase()
            .filter( where, param)
        //  .assort("etime  DESC")
            .select("ctime")
            .getOne( );
        if (od.isEmpty()) {
        //  throw new HongsException(404, "@matrix:matrix.node.not.exists", getDbName(), id);
        } else
        if (Synt.declare(od.get("ctime"), 0L ) >= ctime) {
            throw new HongsException(400, "@matrix:matrix.wait.one.second", getDbName(), id);
        }

        // 获取快照数据
        Map sd = table.fetchCase()
            .filter( wher2, para2)
        //  .assort("ctime  DESC")
            .getOne( );
        if (sd.isEmpty()) {
            throw new HongsException(404, "@matrix:matrix.node.not.exists", getDbName(), id, ctime);
        }
        // 删除时保留的是删除前的快照, 即使为最终记录仍然可以恢复
        if (Synt.declare(sd.get("state"), 0  ) !=  0   ) {
        if (Synt.declare(sd.get("etime"), 0L ) ==  0L  ) {
            throw new HongsException(400, "@matrix:matrix.node.is.current", getDbName(), id, ctime);
        }}

        // 保存到文档库
        Map dd = (Map) Dist.toObject((String) sd.get("data"));
        dd.put(Cnst.ID_KEY , id);
        padInf(dd, dd);
        Document dc = padDoc(dd);
        setDoc(id, dc);

        Map ud = new HashMap();
        ud.put("etime", ctime);

        Map nd = new HashMap();
        nd.put("ctime", ctime);
        nd.put("rtime", rtime);
        nd.put("etime",   0  );
        nd.put("state",   3  );
        nd.put(     "id", id );
        nd.put("form_id", fid);
        nd.put("site_id", sid);
        nd.put("user_id", uid);

        // 数据快照和日志标题
        nd.put("__data__", dd);
        nd.put("data", Dist.toString(dd,  true ));
        nd.put("name", this.getText (dd, "name"));

        // 操作备注和终端代码
        if (rd.containsKey("memo")) {
            nd.put("memo", getText(rd, "memo"));
        }
        if (rd.containsKey("meno")) {
            nd.put("meno", getText(rd, "meno"));
        }

        table.update(ud, where, param);
        table.insert(nd);

        return 1;
    }

}
