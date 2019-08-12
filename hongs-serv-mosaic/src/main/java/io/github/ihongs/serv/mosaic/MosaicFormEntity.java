package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;
import io.github.ihongs.util.Syno;

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

    /**
     * 获取实例
     * 生命周期将交由 Core 维护
     * @param unit
     * @return
     */
    public static MosaicFormEntity getInstance(String unit) {
        MosaicFormEntity inst;
        Core   core = Core.getInstance( );
        String name = MosaicFormEntity.class.getName() +":"+ unit;
        if (core.containsKey(name)) {
            inst = (MosaicFormEntity) core.got( name );
        } else {
            unit = Syno.splitPath( unit );
            inst = new MosaicFormEntity();
            inst.setUnitId ( unit );
            core.put ( name, inst );
        }
        return inst;
    }

}
