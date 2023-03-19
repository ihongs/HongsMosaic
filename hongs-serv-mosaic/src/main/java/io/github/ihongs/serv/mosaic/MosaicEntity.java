package io.github.ihongs.serv.mosaic;

import io.github.ihongs.HongsException;
import io.github.ihongs.HongsExemption;
import io.github.ihongs.db.DB;
import io.github.ihongs.db.Model;
import io.github.ihongs.db.Table;
import io.github.ihongs.serv.matrix.Data;
import io.github.ihongs.util.Synt;
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
public abstract class MosaicEntity extends Data {

    /**
     * 分站字段名
     */
    static public final String SITE_ID_KEY = "sd";

    /**
     * 分站ID
     */
    private final String siteId;

    protected MosaicEntity(String conf, String form, String siteId) {
        super( conf , form );
        this.siteId = siteId;
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

    public String getSiteId() {
        return siteId;
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
        String sd = getPartId();
        if (null != sd && ! sd.isEmpty( )) {
            doc.add(new StringField("@"+SITE_ID_KEY, sd, Field.Store.NO));
            doc.add(new StoredField(/**/SITE_ID_KEY, sd));
        } else {
            throw new HongsExemption( "siteId required" );
        }

        super.padDoc(doc, map, rep);
    }

}
