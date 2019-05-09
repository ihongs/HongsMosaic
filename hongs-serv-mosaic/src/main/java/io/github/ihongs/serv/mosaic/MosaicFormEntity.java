package io.github.ihongs.serv.mosaic;

/**
 * 表单模型
 * @author Hongs
 */
public class MosaicFormEntity extends MosaicEntity {

    protected MosaicFormEntity(String conf, String form) {
        super(conf, form);
    }

    public MosaicFormEntity() {
        super("mosaic", "form");
    }

}
