package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Cnst;
import io.github.ihongs.HongsException;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.db.DB;
import io.github.ihongs.db.Model;
import io.github.ihongs.db.Table;
import io.github.ihongs.serv.matrix.Data;
import io.github.ihongs.util.Syno;
import io.github.ihongs.util.Synt;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @Override
    public int add(String id, Map rd, long ctime) throws HongsException {
        int i = super.add(id, rd, ctime);
        call(id, "create", ctime);
        return i;
    }

    @Override
    public int set(String id, Map rd, long ctime) throws HongsException {
        int i = super.set(id, rd, ctime);
        call(id, "update", ctime);
        return i;
    }

    @Override
    public int put(String id, Map rd, long ctime) throws HongsException {
        int i = super.put(id, rd, ctime);
        call(id, "update", ctime);
        return i;
    }

    @Override
    public int del(String id, Map rd, long ctime) throws HongsException {
        int i = super.del(id, rd, ctime);
        call(id, "delete", ctime);
        return i;
    }

    @Override
    public int rev(String id, Map rd, long ctime) throws HongsException {
        int i = super. rev(id, rd, ctime);
        call(id, "revert", ctime);
        return i;
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
            "type"   , on ,
            "time"   , ct
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
