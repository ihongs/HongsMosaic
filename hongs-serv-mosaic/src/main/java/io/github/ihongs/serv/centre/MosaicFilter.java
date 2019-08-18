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

    private PasserHelper ignore = null;

    @Override
    public void init(FilterConfig cnf) throws ServletException {
        super.init(cnf);

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
            return;
        }

        // 跳过应用接口
        if (url.endsWith(Cnst.API_EXT)) {
            chain.doFilter( req , rsp );
            return ;
        }

        // 跳过内部目录
        if (url.startsWith(   "/_"   )) {
            chain.doFilter( req , rsp );
            return ;
        }

        if (url.endsWith(Cnst.ACT_EXT)) {
            // 定制脚本
            String ast ;
            int pos  = url.lastIndexOf("/");
            if (pos >= 1) {
                ast  = url.substring(0,pos);
                ast  = "/__main__"  +  ast + ".jsp";
            } else {
                throw  new ServletException( "Wrong url!" );
            }
            File src = new File(Core.BASE_PATH + pre + url);
            if ( src.exists()) {
                include( req , rsp , pre + url , pre + ast);
                return ;
            }

            // 模板动作
            String act ;
                pos  = url.indexOf  ("/",1);
            if (pos >= 1) {
                act  = url.substring(0+pos);
                act  = "/__unit__"  +  act ;
            } else {
                throw  new ServletException( "Wrong url!" );
            }
            /**/include( req , rsp , pre + url , pre + act);
        } else {
            // 用户资源
            File src = new File(Core.BASE_PATH + pre + url);
            if ( src.exists()) {
                chain.doFilter ( req, rsp );
                return ;
            }

            // 模板资源
            String act ;
            int pos  = url.indexOf  ("/",1);
            if (pos >= 1) {
                act  = url.substring(0+pos);
                act  = "/__unit__"  +  act ;
            } else {
                throw  new ServletException( "Wrong url." );
            }
            /**/forward( req , rsp , pre + url , pre + act);
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
