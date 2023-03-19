package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;

/**
 * 面板模型
 * @author Hongs
 */
public class MosaicPaneEntity extends MosaicEntity {
    
    private MosaicPaneEntity(String siteId) {
        super("mosaic", "pane", siteId);
    }

    /**
     * 获取实例
     * 生命周期将交由 Core 维护
     * @param site
     * @return
     */
    public static MosaicPaneEntity getInstance(String site) {
        Core   core = Core.getInstance();
        String name = MosaicPaneEntity.class.getName() +":"+ site  ;
        MosaicPaneEntity inst = (MosaicPaneEntity) core.get( name );
        if (inst == null) {
            inst  = new MosaicPaneEntity(site);
            core.set(name, inst);
        }
        return inst;
    }

}
