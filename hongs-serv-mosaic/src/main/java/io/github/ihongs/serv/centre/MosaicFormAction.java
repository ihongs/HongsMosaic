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
import io.github.ihongs.dh.ModelGate;
import io.github.ihongs.serv.matrix.Data;
import io.github.ihongs.serv.mosaic.MosaicEntity;
import io.github.ihongs.serv.mosaic.MosaicFormEntity;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author Hongs
 */
@Action("centre/mosaic/_auto_")
public class MosaicFormAction extends ModelGate {

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
        String unitId = runner.getModule( );
        if (unitId.length() > 14) {
            unitId = unitId.substring( 15 ); // 格式: centre/mosaic/unitId
        if (unitId.length() < 1 ) {
            throw new HongsException(0x1100, "URI must be centre/mosaic/UNIT_ID");
        }} else {
            throw new HongsException(0x1100, "URI must be centre/mosaic/UNIT_ID");
        }
        MosaicEntity entity = MosaicFormEntity.getInstance(unitId);
//      entity.setUnitId(unitId);
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

    @Action("form/search")
    @Preset(conf="", form="")
    @Select(conf="", form="")
    @Override
    public void search(ActionHelper helper) throws HongsException {
        super.search(helper);
    }

    @Action("form/create")
    @Preset(conf="", form="", defs={":defence"})
    @Verify(conf="", form="")
    @Select(conf="", form="")
    @CommitSuccess
    @Override
    public void create(ActionHelper helper) throws HongsException {
        super.create(helper);
    }

    @Action("form/update")
    @Preset(conf="", form="", defs={":defence"})
    @Verify(conf="", form="")
    @CommitSuccess
    @Override
    public void update(ActionHelper helper) throws HongsException {
        super.update(helper);
    }

    @Action("form/delete")
    @Preset(conf="", form="", defs={":defence"})
    @CommitSuccess
    @Override
    public void delete(ActionHelper helper) throws HongsException {
        super.delete(helper);
    }

}
