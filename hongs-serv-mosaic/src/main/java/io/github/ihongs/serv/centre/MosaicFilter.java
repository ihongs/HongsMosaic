package io.github.ihongs.serv.centre;

import io.github.ihongs.Cnst;
import io.github.ihongs.Core;
import io.github.ihongs.action.ActionDriver;
import io.github.ihongs.action.ActionHelper;
import io.github.ihongs.action.PasserHelper;
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

    private String action;
    private String acting;
    private String layout;
    private PasserHelper ignore = null;

    @Override
    public void init(FilterConfig cnf) throws ServletException {
        super.init(cnf);

        action = cnf.getInitParameter("action-path");
        acting = cnf.getInitParameter("acting-path");
        layout = cnf.getInitParameter("layout-path");
        if (action == null) {
            action ="/centre/site";
        }
        if (acting == null) {
            acting =  action + "/__mian__";
        }
        if (layout == null) {
            layout =  action + "/__base__";
        }

        // 获取不包含的URL
        this.ignore = new PasserHelper(
            cnf.getInitParameter("ignore-urls"),
            cnf.getInitParameter("attend-urls")
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
        String pre = req.getServletPath();
        String url = req.getPathInfo(   );

        // 跳过指定路径
        if (ignore != null && ignore.ignore(pre+url)) {
            chain.doFilter( req , rsp );
            return ;
        }

        // 规避脚本注入
        if (url.endsWith(   ".jsp"   )) {
            throw new ServletException("Wrong uri?");
        }

        // 跳过应用接口
        if (url.endsWith(Cnst.API_EXT)) {
            chain.doFilter( req , rsp );
            return ;
        }

        if (url.endsWith(Cnst.ACT_EXT)) {
            // 定制脚本
            String ast ;
            int pos  = url.lastIndexOf("/");
            if (pos >= 1) {
                ast  = url.substring(0,pos);
                ast  = acting + ast +".jsp";
            } else {
                throw  new ServletException( "Wrong url!" );
            }
            File src = new File(Core.BASE_PATH + ast);
            if ( src.exists()) {
                include( req , rsp , pre + url , ast);
                return ;
            }

            // 模板动作
            String act ;
                pos  = url.indexOf  ("/" , 1);
            if (pos >= 1) {
                act  = url.substring(pos + 0);

                // 去除 fore 的 FORM_ID
                if (act.startsWith("/fore/")) {
                        pos  = url.indexOf  ("/" , 1);
                    if (pos >= 1) {
                        act  = url.substring(pos + 0);
                        act  = "/fore" + act ;
                    }
                }

                act = action + act ;
                include( req , rsp , pre + url , act);
            } else {
                throw  new ServletException( "Wrong url!" );
            }
        } else {
            // 用户资源
            File src = new File(Core.BASE_PATH + pre + url);
            if ( src.exists()) {
                chain.doFilter (req, rsp);
                return ;
            }

            // 模板资源
            String act ;
            int pos  = url.indexOf  ("/" , 1);
            if (pos >= 1) {
                act  = url.substring(pos + 0);

                // 去除 fore 的 FORM_ID
                if (act.startsWith("/fore/")) {
                        pos  = url.indexOf  ("/",1);
                    if (pos >= 1) {
                        act  = url.substring(pos+0);
                        act  = "/fore"+act ;
                    }
                }

                act = layout + act ;
                forward( req , rsp , pre + url , act);
            } else {
                throw  new ServletException( "Wrong url." );
            }
        }
    }

    private void include(ServletRequest req, ServletResponse rsp, String url, String uri)
            throws ServletException, IOException {
        // 虚拟路径
        req.setAttribute(Cnst.ORIGIN_ATTR, Core.ACTION_NAME.get());
        req.setAttribute(Cnst.ACTION_ATTR, url.substring(1));
        // 转发请求
        req.getRequestDispatcher( uri ).include( req , rsp );
    }

    private void forward(ServletRequest req, ServletResponse rsp, String url, String uri)
            throws ServletException, IOException {
        // 虚拟路径
        req.setAttribute(Cnst.ORIGIN_ATTR, Core.ACTION_NAME.get());
        req.setAttribute(Cnst.ACTION_ATTR, url.substring(1));
        // 转发请求
        req.getRequestDispatcher( uri ).forward( req , rsp );
    }

}
