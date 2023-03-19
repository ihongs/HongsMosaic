package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;
import io.github.ihongs.util.Syno;

/**
 * 表单模型
 * @author Hongs
 */
public class MosaicFormEntity extends MosaicEntity {

    private MosaicFormEntity(String siteId) {
        super("mosaic", "form", siteId);
    }

    /**
     * 获取实例
     * 生命周期将交由 Core 维护
     * @param site
     * @return
     */
    public static MosaicFormEntity getInstance(String site) {
        Core   core = Core.getInstance();
        String name = MosaicFormEntity.class.getName() +":"+ site  ;
        MosaicFormEntity inst = (MosaicFormEntity) core.get( name );
        if (inst == null) {
            inst  = new MosaicFormEntity(site);
            core.set(name, inst);
        }
        return inst;
    }

}
