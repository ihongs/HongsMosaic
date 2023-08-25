package io.github.ihongs.serv.mosaic;

import io.github.ihongs.Core;
import io.github.ihongs.CoreConfig;
import io.github.ihongs.CoreLogger;
import io.github.ihongs.HongsException;
import io.github.ihongs.util.Remote;
import io.github.ihongs.util.Syno;
import io.github.ihongs.util.daemon.Async;
import java.util.function.Supplier;

/**
 * 数据更新通知队列
 * @author Hongs
 */
public class MosaicCaller extends Async<String> implements Core.Singleton {

    private static final String SPACE = "hongs.log.mosaic.data.call" ;
    private static final String ENVIR =    "INSIDE mosaic.data.call ";

    protected MosaicCaller(int maxTasks, int maxServs) throws HongsException {
        super(MosaicCaller.class.getName( ), maxTasks, maxServs);
    }

    public static MosaicCaller getInstance() {
        return Core.GLOBAL_CORE.got (
            MosaicCaller.class.getName (),
            new Supplier<MosaicCaller> () {
                @Override
                public MosaicCaller get() {
                    CoreConfig conf = CoreConfig.getInstance();
                    try {
                        return new MosaicCaller(
                            conf.getProperty("core.mosaic.data.caller.max.tasks", Integer.MAX_VALUE),
                            conf.getProperty("core.mosaic.data.caller.max.servs", 1)
                        );
                    } catch (HongsException x) {
                        throw x.toExemption( );
                    }
                }
            }
        );
    }

    @Override
    public void run(String url) {
        try {
            String rsp = Remote.get(url);
            String log = "GET: "+ url +" RSP: "+ Syno.indent(rsp.trim( ));
            CoreLogger.getLogger (SPACE).info(ENVIR + log);
        } catch ( Exception | Error ex ) {
            String log = ex.getMessage();
            CoreLogger.getLogger (SPACE).warn(ENVIR + log);
        }
    }

}
