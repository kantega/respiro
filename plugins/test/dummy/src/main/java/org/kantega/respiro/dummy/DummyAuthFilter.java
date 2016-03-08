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

        filterChain.doFilter(new DummyAuthWrapper((HttpServletRequest) servletRequest, openServices), servletResponse);
    }

    @Override
    public void destroy() {

    }
}
