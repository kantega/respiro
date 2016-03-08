package org.kantega.respiro.dummy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Base64;
import java.util.List;

/**
 */
public class DummyAuthWrapper extends HttpServletRequestWrapper {

    private final List<String> openServices;

    public DummyAuthWrapper(HttpServletRequest request, List<String> openServices) {
        super(request);
        this.openServices = openServices;
    }

    @Override
    public String getHeader(String name) {
        if ("Authorization".equals(name) && openServices.contains(((HttpServletRequest) getRequest()).getServletPath())) {

            return "Basic " + new String( Base64.getEncoder().encode("joe:joe".getBytes()));
        } else
            return super.getHeader(name);
    }
}
