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
     * @param site
     * @param form
     * @return
     */
    public static MosaicForeEntity getInstance(String site, String form) {
        Core   core = Core.getInstance();
        String name = MosaicForeEntity.class.getName() +":"+ site +":"+ form ;
        MosaicForeEntity inst = (MosaicForeEntity) core.get( name );
        if (inst == null) {
            site = Syno.splitPath (site);
            inst = new MosaicForeEntity("mosaic/"+ site +"/fore/"+ form,form);
            core.set ( name, inst );
        }
        return inst;
    }

}
