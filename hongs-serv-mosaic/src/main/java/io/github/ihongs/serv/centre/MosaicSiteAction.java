package io.github.ihongs.serv.centre;

import io.github.ihongs.Cnst;
import io.github.ihongs.HongsException;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.action.ActionRunner;
import io.github.ihongs.action.anno.Action;
import io.github.ihongs.dh.IEntity;
import io.github.ihongs.serv.matrix.Data;
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
    throws HongsException {
        super.acting(helper, runner);
        
        // 限制操作范围
        Map    req = helper.getRequestData();
        String act = runner.getHandle();
        String uid = (String) helper.getSessibute(Cnst.UID_SES);
        if (null == uid) {
            throw new HongsException(401, "Login required");
        }
        switch(act) {
            case "search":
            case "acount":
            case "amount":
            case "update":
            case "delete":
                Dict.del(req, "owner");
                Dict.put(req, uid, Cnst.AR_KEY, "x", "owner");
                break;
            case "create":
                Dict.put(req, Synt.setOf(uid), "owner");
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
     * @throws HongsException
     */
    @Override
    public IEntity getEntity(ActionHelper helper)
    throws HongsException {
        String userId = (String) helper.getSessibute(Cnst.UID_SES);
        Data entity = Data.getInstance("mosaic", "site");
        entity.setUserId(userId);
        return entity;
    }

}
