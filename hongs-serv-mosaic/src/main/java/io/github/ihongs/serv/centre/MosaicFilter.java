package io.github.ihongs.serv.centre;

import io.github.ihongs.Cnst;
import io.github.ihongs.Core;
import io.github.ihongs.action.ActionDriver;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.action.ActionRunner;
import java.io.File;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Hongs
 */
public class MosaicFilter extends ActionDriver {

    public static final String SITE_ID_ATTR = MosaicFilter.class.getName() + ":SITE";

    private String action;
    private String layout;
    private String script;
    private String prefix;
    private String acting;
    private PathPattern patter = null;

    @Override
    public void init(FilterConfig cnf) throws ServletException {
        super.init(cnf);

        action = cnf.getInitParameter("action-path");
        script = cnf.getInitParameter("script-path");
        layout = cnf.getInitParameter("layout-path");
        prefix = cnf.getInitParameter("prefix-path");
        if (action == null) {
            action ="/centre/site";
        }
        if (script == null) {
            script =  action + "/__main__";
        }
        if (layout == null) {
            layout =  action + "/__base__";
        }
        if (prefix == null) {
            prefix =  action ;
        }
        acting = action.substring(1);

        // 获取不包含的URL
        patter = new PathPattern(
            cnf.getInitParameter("url-include"),
            cnf.getInitParameter("url-exclude")
        );
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void doFilter(Core core, ActionHelper hlpr, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse rsp = hlpr.getResponse();
        HttpServletRequest  req = hlpr.getRequest( );
        String url = ActionDriver.getRecentPath(req);

        // 跳过指定路径
        if (patter != null && ! patter.matches(url)) {
            chain.doFilter(req, rsp);
            return ;
        }

        String ref = url.substring(prefix.length( ));

        // 站点编号
        String sid = (String) hlpr.getAttribute(SITE_ID_ATTR);
        if (sid == null) {
            int end = ref. indexOf ("/", 1);
            if (end > 0) {
                sid = ref.substring(1, end);
                hlpr.setAttribute(SITE_ID_ATTR, sid);
            }
        }

        if (url.endsWith(Cnst.API_EXT)) {
            // 应用接口, 直接跳过
        } else
        if (url.endsWith(Cnst.ACT_EXT)) {
            // 内置动作
            String ast ;
            ast = Cnst.ACT_EXT ;
            ast = ref.substring( 0 , ref.length( ) - ast.length( ) );
            if (ActionRunner.getActions().containsKey(acting + ast)) {
                chain.doFilter (req, rsp);
                return ;
            }

            // 定制脚本
            int poz  = url.lastIndexOf("/");
            if (poz >= 1) {
                ast  = url.substring(0,poz);
                ast  = script + ast +".jsp";
            } else {
                throw  new ServletException("Wrong url!");
            }
            File src = new File(Core.BASE_PATH + ast);
            if ( src.exists()) {
                include(req, rsp, url, ast);
                return ;
            }

            // 模板动作
            String act ;
            int pos  = ref.indexOf  ("/" , 1);
            if (pos >= 1) {
                act  = ref.substring(pos + 0);

                // 去除 fore 的 FORM_ID
                if (act.startsWith("/fore/")) {
                        pos  = ref.indexOf  ("/" , 1);
                    if (pos >= 1) {
                        act  = ref.substring(pos + 0);
                        act  = "/fore"+act ;
                    }
                }

                ref = prefix + ref;
                act = action + act;
                include(req, rsp, ref, act);
                return;
            }
        } else {
            // 用户资源
            File src = new File(Core.BASE_PATH + url);
            if ( src.exists()) {
                chain.doFilter (req, rsp);
                return ;
            }

            // 模板资源
            String act ;
            int pos  = ref.indexOf  ("/" , 1);
            if (pos >= 1) {
                act  = ref.substring(pos + 0);

                // 去除 fore 的 FORM_ID
                if (act.startsWith("/fore/")) {
                        pos  = ref.indexOf  ("/" , 1);
                    if (pos >= 1) {
                        act  = ref.substring(pos + 0);
                        act  = "/fore"+act ;
                    }
                }

                ref = prefix + ref;
                act = layout + act;
                forward(req, rsp, ref, act);
                return;
            }
        }

        chain.doFilter (req, rsp);
    }

    private void include(ServletRequest req, ServletResponse rsp, String url, String uri)
            throws ServletException, IOException {
        // 虚拟路径
        req.setAttribute(Cnst.ACTION_ATTR, url.substring(1));
        // 转发请求
        req.getRequestDispatcher( uri ).include( req , rsp );
    }

    private void forward(ServletRequest req, ServletResponse rsp, String url, String uri)
            throws ServletException, IOException {
        // 虚拟路径
        req.setAttribute(Cnst.ACTION_ATTR, url.substring(1));
        // 转发请求
        req.getRequestDispatcher( uri ).forward( req , rsp );
    }

}
