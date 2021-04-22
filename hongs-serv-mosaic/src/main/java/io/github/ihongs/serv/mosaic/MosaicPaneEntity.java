package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;
import io.github.ihongs.util.Syno;

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
     * @param site
     * @return
     */
    public static MosaicPaneEntity getInstance(String site) {
        Core   core = Core.getInstance( );
        String name = MosaicPaneEntity.class.getName() +":"+ site  ;
        MosaicPaneEntity inst = (MosaicPaneEntity) core.get( name );
        if (inst == null) {
            site = Syno.splitPath( site );
            inst = new MosaicPaneEntity();
            inst.setSiteId(site);
            core.set(name, inst);
        }
        return inst;
    }

}
