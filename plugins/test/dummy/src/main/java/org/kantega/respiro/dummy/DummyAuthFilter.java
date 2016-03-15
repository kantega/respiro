package org.kantega.respiro.dummy;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 */
public class DummyAuthFilter implements Filter {
    private final List<String> openServices;

    public DummyAuthFilter(List<String> openServices) {
        this.openServices = openServices;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        if(openServices.contains(((HttpServletRequest)servletRequest).getServletPath()))
            servletRequest.setAttribute("skipBasicAuth", Boolean.TRUE);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
