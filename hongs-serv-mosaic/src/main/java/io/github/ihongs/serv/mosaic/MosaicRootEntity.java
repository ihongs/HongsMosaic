package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;
import java.util.Map;

/**
 * 模型基类
 * 按照用户存放
 * @author Hongs
 */
public class MosaicRootEntity extends MosaicData {

    protected MosaicRootEntity(String conf, String form) {
        super(conf , form);
    }
    
    public static MosaicRootEntity getInstance(String conf, String form) {
        String name = MosaicRootEntity.class.getName() +":"+ conf +":"+ form;
        return Core.getInstance().got(name, ()->new MosaicRootEntity(conf, form));
    }

    @Override
    public String getSiteId() {
        return null;
    }

    @Override
    public String getSiteId(Map dd) {
        return (String) dd.get(SITE_ID_KEY);
    }
    
    @Override
    public String getFormId(Map dd) {
        return (String) dd.get(PART_ID_KEY);
    }
    
}
