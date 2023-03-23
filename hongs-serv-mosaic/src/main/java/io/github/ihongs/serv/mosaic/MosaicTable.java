package io.github.ihongs.serv.mosaic;

import io.github.ihongs.HongsException;
import io.github.ihongs.db.DB;
import io.github.ihongs.db.Table;
import io.github.ihongs.util.Synt;
import java.util.Map;

/**
 * 补全
 * @author Hongs
 */
public class MosaicTable extends Table {

    public MosaicTable (DB db, Map conf) throws HongsException {
        super(db, conf);
    }

    @Override
    public int insert(Map<String, Object> values) throws HongsException {
        if (values.containsKey("__data__")) {
            Map data = Synt.asMap(values.get("__data__"));
            if (data.containsKey("pd")) {
                values.put("form_id", data.get("pd"));
            }
            if (data.containsKey("sd")) {
                values.put("site_id", data.get("sd"));
            }
        } else
        if (values.containsKey(  "data"  )) {
            Map data = Synt.toMap(values.get(  "data"  ));
            if (data.containsKey("pd")) {
                values.put("form_id", data.get("pd"));
            }
            if (data.containsKey("sd")) {
                values.put("site_id", data.get("sd"));
            }
        }
        return super.insert(values);
    }


}
