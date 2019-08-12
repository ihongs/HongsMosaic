package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;
import io.github.ihongs.util.Syno;

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

    /**
     * 获取实例
     * 生命周期将交由 Core 维护
     * @param unit
     * @return
     */
    public static MosaicPageEntity getInstance(String unit) {
        MosaicPageEntity inst;
        Core   core = Core.getInstance( );
        String name = MosaicPageEntity.class.getName() +":"+ unit;
        if (core.containsKey(name)) {
            inst = (MosaicPageEntity) core.got( name );
        } else {
            unit = Syno.splitPath( unit );
            inst = new MosaicPageEntity();
            inst.setUnitId ( unit );
            core.put ( name, inst );
        }
        return inst;
    }

}
