package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;
import io.github.ihongs.util.Tool;

/**
 * 记录模型
 * @author Hongs
 */
public class MosaicFormRecord extends MosaicEntity {

    protected MosaicFormRecord(String conf, String form) {
        super(conf, form);
    }

    /**
     * 获取实例
     * 生命周期将交由 Core 维护
     * @param unit
     * @param form
     * @return
     */
    public static MosaicFormRecord getInstance(String unit, String form) {
        MosaicFormRecord inst;
        Core   core = Core.getInstance();
        String name = MosaicFormRecord.class.getName() +":"+ unit +":"+ form ;
        if (core.containsKey(name)) {
            inst = (MosaicFormRecord) core.got( name );
        } else {
            unit = Tool.splitPath (unit);
            inst = new MosaicFormRecord("mosaic/" + unit + "/" + form , form);
            core.put ( name, inst );
        }
        return inst;
    }

}
