package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;
import java.util.Map;

/**
 * 模型基类
 * 按照用户存放
 * @author Hongs
 */
public class MosaicForeEntity extends MosaicData {

    /**
     * 分站ID
     */
    final private String site;

    protected MosaicForeEntity(String conf, String form, String site) {
        super(conf, form);
        this.site = site ;
    }

    public static MosaicForeEntity getInstance(String conf, String form, String site) {
        String name = MosaicForeEntity.class.getName() +":"+ conf +":"+ form +":"+ site;
        return Core.getInstance().got(name, ()->new MosaicForeEntity(conf, form, site));
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

}
