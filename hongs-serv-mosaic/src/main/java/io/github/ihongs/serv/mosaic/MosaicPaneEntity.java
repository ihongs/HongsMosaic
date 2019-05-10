package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;
import io.github.ihongs.util.Tool;

/**
 * 面板模型
 * @author Hongs
 */
public class MosaicPaneEntity extends MosaicEntity {
    
    public MosaicPaneEntity(String conf, String form) {
        super(conf, form);
    }

    public MosaicPaneEntity() {
        super("mosaic", "pane");
    }

    /**
     * 获取实例
     * 生命周期将交由 Core 维护
     * @param unit
     * @return
     */
    public static MosaicPaneEntity getInstance(String unit) {
        MosaicPaneEntity inst;
        Core   core = Core.getInstance( );
        String name = MosaicPaneEntity.class.getName() +":"+ unit;
        if (core.containsKey(name)) {
            inst = (MosaicPaneEntity) core.got( name );
        } else {
            unit = Tool.splitPath( unit );
            inst = new MosaicPaneEntity();
            inst.setUnitId ( unit );
            core.put ( name, inst );
        }
        return inst;
    }

}
