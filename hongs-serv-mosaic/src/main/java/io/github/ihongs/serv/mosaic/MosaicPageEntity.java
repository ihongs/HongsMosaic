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
        MosaicPageEntity inst;
        Core   core = Core.getInstance( );
        String name = MosaicPageEntity.class.getName() +":"+ site;
        if (core.containsKey(name)) {
            inst = (MosaicPageEntity) core.get( name );
        } else {
            site = Syno.splitPath(site );
            inst = new MosaicPageEntity();
            inst.setSiteId (site );
            core.put ( name, inst );
        }
        return inst;
    }

}
