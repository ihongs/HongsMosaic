package io.github.ihongs.serv.centre;

import io.github.ihongs.Cnst;
import io.github.ihongs.CruxException;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.action.ActionRunner;
import io.github.ihongs.action.anno.Action;
import io.github.ihongs.dh.IEntity;
import io.github.ihongs.serv.mosaic.MosaicSiteEntity;
import io.github.ihongs.util.Dict;
import io.github.ihongs.util.Synt;
import java.util.Map;

/**
 *
 * @author Hongs
 */
@Action("centre/mosaic/site")
public class MosaicSiteAction extends DataAction {

    @Override
    public void acting(ActionHelper helper, ActionRunner runner)
    throws CruxException {
        super.acting(helper, runner);

        // 限制操作范围
        Map    req = helper.getRequestData();
        String act = runner.getHandle();
        String uid = (String) helper.getSessibute(Cnst.UID_SES);
        if (null == uid) {
            throw new CruxException(401, "Login required");
        }
        switch(act) {
            case "search":
            case "acount":
            case "amount":
            case "update":
            case "delete":
                req .remove("owner");
                Dict.put(req, uid, Cnst.AR_KEY, null, "owner");
                break;
            case "create":
                req .put( "owner", Synt.setOf(uid) );
                break;
        }
    }

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
        MosaicSiteEntity entity = MosaicSiteEntity.getInstance("mosaic", "site");
        entity.setUserId(userId);
        return entity;
    }

}
