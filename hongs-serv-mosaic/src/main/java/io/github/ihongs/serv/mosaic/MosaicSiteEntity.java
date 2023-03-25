package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;
import java.util.Map;

/**
 * 模型基类
 * 按照用户存放
 * @author Hongs
 */
public class MosaicSiteEntity extends MosaicData {

    protected MosaicSiteEntity(String conf, String form) {
        super(conf, form);
    }

    public static MosaicSiteEntity getInstance(String conf, String form) {
        String name = MosaicSiteEntity.class.getName() +":"+ conf +":"+ form;
        return Core.getInstance().got(name, ()->new MosaicSiteEntity(conf, form));
    }

    @Override
    public String getSiteId() {
        return "0";
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
