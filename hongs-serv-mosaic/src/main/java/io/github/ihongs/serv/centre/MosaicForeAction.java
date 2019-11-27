package io.github.ihongs.serv.centre;

import io.github.ihongs.Cnst;
import io.github.ihongs.HongsException;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.action.ActionRunner;
import io.github.ihongs.action.anno.Action;
import io.github.ihongs.action.anno.CommitSuccess;
import io.github.ihongs.action.anno.Preset;
import io.github.ihongs.action.anno.Select;
import io.github.ihongs.action.anno.Verify;
import io.github.ihongs.dh.IEntity;
import io.github.ihongs.dh.JAction;
import io.github.ihongs.serv.matrix.Data;
import io.github.ihongs.serv.mosaic.MosaicEntity;
import io.github.ihongs.serv.mosaic.MosaicForeEntity;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author Hongs
 */
@Action("centre/mosaic/__site__")
public class MosaicForeAction extends JAction {

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
        ActionRunner runner = (ActionRunner) helper.getAttribute(ActionRunner.class.getName());
        String userId = (String) helper.getSessibute(Cnst.UID_SES);
        String siteId = runner.getModule( );
        String formId = runner.getEntity( );
               siteId = siteId.substring(0 , -5 + siteId.length()); // 去掉 /fore
        if (siteId.length() > 14) {
            siteId = siteId.substring( 15 ); // 格式: centre/mosaic/siteId
        if (siteId.length() < 1 ) {
            throw new HongsException(0x1100, "URI must be centre/mosaic/SITE/fore/FORM/ACTION.act");
        }} else {
            throw new HongsException(0x1100, "URI must be centre/mosaic/SITE/fore/FORM/ACTION.act");
        }
        MosaicEntity entity = MosaicForeEntity.getInstance(siteId, formId);
//      entity.setSiteId(siteId);
        entity.setUserId(userId);
        return entity;
    }

    @Override
    public void acting(ActionHelper helper, ActionRunner runner)
    throws HongsException {
        super.acting(helper, runner);

        String ent = runner.getEntity();
        String mod = runner.getModule();
        Method met = runner.getMethod();

        // 绑定特制的表单
        if (met.isAnnotationPresent(Select.class)
        ||  met.isAnnotationPresent(Verify.class)) {
            Data dat = (Data) getEntity(helper);
            Map  fcs =  dat.getFields();
            helper.setAttribute("form:"+ mod +"."+ ent, fcs);
        }
    }

    @Action("fore/select")
    @Preset(conf="", form="")
    @Select(conf="", form="")
    @Override
    public void select(ActionHelper helper) throws HongsException {
        super.select(helper);
    }

    @Action("fore/search")
    @Preset(conf="", form="")
    @Select(conf="", form="")
    @Override
    public void search(ActionHelper helper) throws HongsException {
        super.search(helper);
    }

    @Action("fore/create")
    @Preset(conf="", form="", defs={":defence"})
    @Verify(conf="", form="")
    @Select(conf="", form="")
    @CommitSuccess
    @Override
    public void create(ActionHelper helper) throws HongsException {
        super.create(helper);
    }

    @Action("fore/update")
    @Preset(conf="", form="", defs={":defence"})
    @Verify(conf="", form="")
    @CommitSuccess
    @Override
    public void update(ActionHelper helper) throws HongsException {
        super.update(helper);
    }

    @Action("fore/delete")
    @Preset(conf="", form="", defs={":defence"})
    @CommitSuccess
    @Override
    public void delete(ActionHelper helper) throws HongsException {
        super.delete(helper);
    }

}
