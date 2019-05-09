package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;

/**
 *
 * @author hong
 */
public class MosaicFormEntity extends MosaicEntity {

    protected MosaicFormEntity(String conf, String form) {
        super(conf, form);
    }

    public MosaicFormEntity() {
        super("mosaic", "form");
    }

}
