package io.github.ihongs.serv.mosaic;

/**
 * 内容模型
 * @author Hongs
 */
public class MosaicPageEntity extends MosaicEntity {
    
    public MosaicPageEntity(String conf, String form) {
        super(conf, form);
    }

    public MosaicPageEntity() {
        super("mosaic", "page");
    }

}
