package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;
import io.github.ihongs.CruxException;
import io.github.ihongs.CruxExemption;
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
public class MosaicDataEntity extends MosaicData {

    /**
     * 分站ID
     */
    final private String site;

    protected MosaicDataEntity(String conf, String form, String site) {
        super(conf, form);
        this.site = site ;
    }

    public static MosaicDataEntity getInstance(String conf, String form, String site) {
        String name = MosaicDataEntity.class.getName() +":"+ conf +":"+ form +":"+ site;
        return Core.getInstance().got(name, ()->new MosaicDataEntity(conf, form, site));
    }

    @Deprecated
    public static MosaicDataEntity getInstance(String conf, String form) {
        throw new UnsupportedOperationException("Usage: getInstance(conf, form, site)");
    }

    @Override
    public String getSiteId() {
        return site;
    }

    @Override
    public String getSiteId(Map dd) {
        return getSiteId();
    }

    @Override
    public String getFormId(Map dd) {
        return getFormId();
    }

    @Override
    protected void padQry(BooleanQuery.Builder qr, Map rd) throws CruxException {
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
            throw new CruxExemption( "siteId required" );
        }

        super.padDoc(doc, map, rep);
    }

}
