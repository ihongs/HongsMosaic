package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;

/**
 *
 * @author Hongs
 */
public class MosaicRootEntity extends MosaicEntity {

    private MosaicRootEntity(String siteId) {
        super("mosaic", "data", siteId);
    }

    /**
     * 获取实例
     * 生命周期将交由 Core 维护
     * @param site
     * @return
     */
    public static MosaicRootEntity getInstance(String site) {
        Core   core = Core.getInstance();
        String name = MosaicFormEntity.class.getName() +":"+ site  ;
        MosaicRootEntity inst = (MosaicRootEntity) core.get( name );
        if (inst == null) {
            inst  = new MosaicRootEntity(site);
            core.set(name, inst);
        }
        return inst;
    }

}
