package io.github.ihongs.serv.centre;

import io.github.ihongs.Cnst;
import io.github.ihongs.CruxException;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.action.ActionRunner;
import io.github.ihongs.action.anno.Select;
import io.github.ihongs.action.anno.Verify;
import io.github.ihongs.dh.search.SearchAction;
import io.github.ihongs.serv.matrix.Data;
import io.github.ihongs.util.Dict;
import io.github.ihongs.util.Synt;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Hongs
 */
public abstract class MosaicAction extends SearchAction {
    
    public static final String SITE_ID_ATTR = MosaicFilter.SITE_ID_ATTR;

    @Override
    public void acting(ActionHelper helper, ActionRunner runner)
    throws CruxException {
        super.acting(helper, runner);

        String uid = (String) helper.getSessibute(Cnst.UID_SES);
        String sid = (String) helper.getAttribute(SITE_ID_ATTR);
        if (sid == null || sid.isEmpty()) {
            throw new CruxException(400, "Site not exists");
        }

        // 查验管理员权限
        Map row = Data.getInstance ("mosaic", "site")
                      .getOne(Synt.mapOf(
            Cnst.RB_KEY, Synt.setOf("state", "owner"),
            Cnst.ID_KEY, sid
        ));
        if (row == null || row.isEmpty()) {
            throw new CruxException(404, "Site not exists");
        }
        int state  = Synt.declare(row.get("state"), 0);
        if (state != 1) {
            throw new CruxException(404, "Site is not ready or locked");
        }
        Set owner  = Synt. asSet (row.get("owner"));
        switch ( runner.getHandle() ) {
            case "create":
            case "update":
            case "delete":
                // 只能管理自己的内容
                if (null == uid) {
                    throw new CruxException(401, "Login required");
                }
                if (null == owner || ! owner.contains(uid)) {
                    throw new CruxException(403, "Power required");
                }
                break;
            case "search":
                // 限定查阅公开的内容
                if (null == uid
                ||  null == owner || ! owner.contains(uid)) {
                    Dict.put(helper.getRequestData(), 1, Cnst.AR_KEY, null, "state");
                }
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

}
