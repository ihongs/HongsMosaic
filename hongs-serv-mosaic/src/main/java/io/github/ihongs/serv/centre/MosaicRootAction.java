package io.github.ihongs.serv.centre;

import io.github.ihongs.Cnst;
import io.github.ihongs.HongsException;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.action.anno.Action;
import io.github.ihongs.dh.IEntity;
import io.github.ihongs.serv.mosaic.MosaicEntity;
import io.github.ihongs.serv.mosaic.MosaicRootEntity;

/**
 *
 * @author Hongs
 */
@Action("centre/site/root")
public class MosaicRootAction extends MosaicAction {

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
        String siteId = (String) helper.getAttribute(SITE_ID_ATTR);
        MosaicEntity entity = MosaicRootEntity.getInstance(siteId);
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
