package io.github.ihongs.serv.centra;

import io.github.ihongs.Cnst;
import io.github.ihongs.HongsException;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.action.anno.Action;
import io.github.ihongs.dh.IEntity;
import io.github.ihongs.serv.matrix.Data;

/**
 * 管理动作
 * @author Hongs
 */
@Action("centra/mosaic/root")
public class MosaicRootAction extends DataAction {

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
        Data   entity = Data.getInstance("mosaic", "root");
        String userId = (String) helper.getSessibute(Cnst.UID_SES);
//      if  (  userId == null  ) userId = Cnst.GUS_UID; // 禁止匿名
        entity.setUserId(userId);
        return entity;
    }

}
