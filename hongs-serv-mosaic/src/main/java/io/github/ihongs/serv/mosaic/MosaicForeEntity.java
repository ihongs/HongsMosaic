package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;
import io.github.ihongs.util.Syno;

/**
 * 记录模型
 * @author Hongs
 */
public class MosaicForeEntity extends MosaicEntity {

    protected MosaicForeEntity(String conf, String form) {
        super(conf, form);
    }

    /**
     * 获取实例
     * 生命周期将交由 Core 维护
     * @param unit
     * @param form
     * @return
     */
    public static MosaicForeEntity getInstance(String unit, String form) {
        MosaicForeEntity inst;
        Core   core = Core.getInstance();
        String name = MosaicForeEntity.class.getName() +":"+ unit +":"+ form ;
        if (core.containsKey(name)) {
            inst = (MosaicForeEntity) core.got( name );
        } else {
            unit = Syno.splitPath (unit);
            inst = new MosaicForeEntity("mosaic/" + unit + "/" + form , form);
            core.put ( name, inst );
        }
        return inst;
    }

}
