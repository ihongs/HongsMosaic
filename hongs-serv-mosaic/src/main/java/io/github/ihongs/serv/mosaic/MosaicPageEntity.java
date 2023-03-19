package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;

/**
 * 内容模型
 * @author Hongs
 */
public class MosaicPageEntity extends MosaicEntity {

    private MosaicPageEntity(String siteId) {
        super("mosaic", "page", siteId);
    }

    /**
     * 获取实例
     * 生命周期将交由 Core 维护
     * @param site
     * @return
     */
    public static MosaicPageEntity getInstance(String site) {
        Core   core = Core.getInstance();
        String name = MosaicPageEntity.class.getName() +":"+ site  ;
        MosaicPageEntity inst = (MosaicPageEntity) core.get( name );
        if (inst == null) {
            inst  = new MosaicPageEntity(site);
            core.set(name, inst);
        }
        return inst;
    }

}
