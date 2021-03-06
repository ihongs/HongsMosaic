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
     * @param site
     * @return
     */
    public static MosaicPageEntity getInstance(String site) {
        Core   core = Core.getInstance( );
        String name = MosaicPageEntity.class.getName() +":"+ site  ;
        MosaicPageEntity inst = (MosaicPageEntity) core.get( name );
        if (inst == null) {
            site = Syno.splitPath( site );
            inst = new MosaicPageEntity();
            inst.setSiteId(site);
            core.set(name, inst);
        }
        return inst;
    }

}
