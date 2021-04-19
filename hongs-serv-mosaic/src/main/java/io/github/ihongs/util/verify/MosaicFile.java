package io.github.ihongs.util.verify;

import io.github.ihongs.Cnst;
import io.github.ihongs.HongsExemption;
import io.github.ihongs.action.ActionHelper;

/**
 *
 * @author hongs
 */
public class MosaicFile extends IsFile {

    @Override
    public Object getParam(String key) {
        Object prm = super.getParam(key);
        if ("path".equals(key)
        ||  "href".equals(key)) {
            if (null != prm) {
                String  uri = (String) prm;
                String  uid = (String) ActionHelper.getInstance().getSessibute(Cnst.UID_SES);
                if (uid == null) {
                    throw new HongsExemption(401);
                }
                return uri.replace("${uid}", uid);
            }
        }
        return prm;
    }

}
