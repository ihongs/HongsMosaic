package io.github.ihongs.serv.centre;

import io.github.ihongs.Cnst;
import io.github.ihongs.HongsException;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.action.ActionRunner;
import io.github.ihongs.action.anno.Action;
import io.github.ihongs.action.anno.Select;
import io.github.ihongs.action.anno.Verify;
import io.github.ihongs.dh.IEntity;
import io.github.ihongs.dh.search.SearchAction;
import io.github.ihongs.serv.matrix.Data;
import io.github.ihongs.serv.mosaic.MosaicRootEntity;
import io.github.ihongs.util.Dict;
import io.github.ihongs.util.Synt;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Hongs
 */
@Action("centre/site/root")
public class MosaicRootAction extends SearchAction {

    public static final String SITE_ID_ATTR = MosaicFilter.SITE_ID_ATTR;

    @Override
    public void acting(ActionHelper helper, ActionRunner runner)
    throws HongsException {
        super.acting(helper, runner);

        String uid = (String) helper.getSessibute(Cnst.UID_SES);
        String sid = (String) helper.getAttribute(SITE_ID_ATTR);
        if (sid == null || sid.isEmpty()) {
            throw new HongsException(400, "Site not exists");
        }

        // 查验管理员权限
        Map row = Data.getInstance ("mosaic", "site")
                      .getOne(Synt.mapOf(
            Cnst.RB_KEY, Synt.setOf("state", "owner"),
            Cnst.ID_KEY, sid
        ));
        if (row == null || row.isEmpty()) {
            throw new HongsException(404, "Site not exists");
        }
        int state  = Synt.declare(row.get("state"), 0);
        if (state != 1) {
            throw new HongsException(404, "Site is not ready or locked");
        }
        Set owner  = Synt. asSet (row.get("owner"));
        switch ( runner.getHandle() ) {
            case "search":
                // 限定查阅公开的内容
                if (null == uid
                ||  null == owner || ! owner.contains(uid)) {
                    Dict.put(helper.getRequestData(), 1, Cnst.AR_KEY, null, "state");
                }
                // 限定查阅本站的内容
                /**/Dict.put(helper.getRequestData(), sid, Cnst.AR_KEY, null, "sd" );
                break;
        }

        // 绑定特制的表单
        String mod = runner.getModule();
        String ent = runner.getEntity();
        Method met = runner.getMethod();
        if (met.isAnnotationPresent(Select.class)
        ||  met.isAnnotationPresent(Verify.class)) {
            Data dat = (Data) getEntity(helper);
            Map  fcs =  dat.getFields();
            helper.setAttribute("form:"+ mod +":"+ ent, fcs);
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
        MosaicRootEntity entity = MosaicRootEntity.getInstance("mosaic", "root");
        entity.setUserId(userId);
        return entity;
    }

    @Override
    public void create(ActionHelper helper) throws HongsException {
        throw new HongsException(405);
    }

    @Override
    public void update(ActionHelper helper) throws HongsException {
        throw new HongsException(405);
    }

    @Override
    public void delete(ActionHelper helper) throws HongsException {
        throw new HongsException(405);
    }

}
