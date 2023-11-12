package io.github.ihongs.serv.centra;

import io.github.ihongs.Cnst;
import io.github.ihongs.CruxException;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.action.anno.Action;
import io.github.ihongs.action.anno.CommitSuccess;
import io.github.ihongs.action.anno.Preset;
import io.github.ihongs.action.anno.Verify;
import io.github.ihongs.dh.IEntity;
import io.github.ihongs.serv.matrix.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 站点管理
 * @author Hongs
 */
@Action("centra/mosaic/site")
public class MosaicSiteAction extends DataAction {

    /**
     * 获取模型对象
     * 注意:
     *  对象 Action 注解的命名必须为 "模型路径/实体名称"
     *  方法 Action 注解的命名只能是 "动作名称", 不得含子级实体名称
     * @param helper
     * @return
     * @throws CruxException
     */
    @Override
    public IEntity getEntity(ActionHelper helper)
    throws CruxException {
        String userId = (String) helper.getSessibute(Cnst.UID_SES);
        Data   entity = Data.getInstance("mosaic", "site");
        entity.setUserId(userId);
        return entity;
    }

    @Action("update")
    @Preset(conf="", form="", defs={".defence"})
    @Verify(conf="", form="")
    @CommitSuccess
    @Override
    public void update(ActionHelper helper) throws CruxException {
        Map req = helper.getRequestData();
        Map rep = new HashMap();
        if (req.containsKey("state")) {
            rep.put("state", req.get("state"));
        }
        if (req.containsKey("cause")) {
            rep.put("cause", req.get("cause"));
        }
        if (rep.isEmpty()) {
            helper.fault("参数错误, 仅能审核");
            return;
        }
        rep.put(Cnst.ID_KEY, req.get(Cnst.ID_KEY));
        helper.setRequestData(rep);
        super.update(helper);
    }

    @Override
    public void create(ActionHelper helper) throws CruxException {
        throw new CruxException(405);
    }

}
