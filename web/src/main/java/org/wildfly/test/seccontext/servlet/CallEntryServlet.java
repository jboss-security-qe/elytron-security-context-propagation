package org.wildfly.test.seccontext.servlet;

import static java.util.Objects.nonNull;
import static org.wildfly.test.seccontext.shared.IdentityUtils.switchIdentity;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.annotation.security.DeclareRoles;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wildfly.test.seccontext.shared.Entry;
import org.wildfly.test.seccontext.shared.LookupUtil;
import org.wildfly.test.seccontext.shared.ReAuthnType;

@WebServlet(urlPatterns = "/CallEntryServlet")
@ServletSecurity(@HttpConstraint(rolesAllowed = { "servlet" }))
@DeclareRoles({ "entry", "whoami", "servlet" })
public class CallEntryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Writer writer = resp.getWriter();
        String method = req.getParameter("method");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String role = req.getParameter("role");
        String type = req.getParameter("type");
        String username2 = req.getParameter("username2");
        String password2 = req.getParameter("password2");
        String type2 = req.getParameter("type2");

        final ReAuthnType reAuthnType = type != null?ReAuthnType.valueOf(type):null;
        final ReAuthnType reAuthnType2 = type2 != null?ReAuthnType.valueOf(type2):null;

        Callable<Void> callable;
        if ("whoAmI".equals(method)) {
                callable = () -> {
                    writer.write(getBean(req).whoAmI());
                    return null;
                };
        } else if ("doubleWhoAmI".equals(method)) {
            callable = () -> {
                String[] response;
                if (username != null && password != null) {
                    response = getBean(req).doubleWhoAmI(username2, password2, reAuthnType2);
                } else {
                    response = getBean(req).doubleWhoAmI();
                }
                writer.write(Arrays.toString(response));
                return null;
            };
        } else if ("doIHaveRole".equals(method)) {
            callable = () -> {
                writer.write(String.valueOf(getBean(req).doIHaveRole(role)));
                return null;
            };
        } else if ("doubleDoIHaveRole".equals(method)) {
            callable = () -> {
                boolean[] response = null;
                if (username != null && password != null) {
                    response = getBean(req).doubleDoIHaveRole(role, username2, password2, reAuthnType2);
                } else {
                    response = getBean(req).doubleDoIHaveRole(role);
                }
                writer.write(Arrays.toString(response));
                return null;
            };
        } else {
            throw new IllegalArgumentException("Parameter 'method' either missing or invalid method='" + method + "'");
        }
        try {
            switchIdentity(username, password, callable, reAuthnType);
        } catch (Exception e) {
            throw new IOException("Unexpected failure", e);
        }
    }

    Entry getBean(HttpServletRequest req) throws NamingException {
        return LookupUtil.lookup("ejb:/seccontext-entry/EntryBean!org.wildfly.test.seccontext.shared.Entry", getOverrides(req));
    }

    private Properties getOverrides(HttpServletRequest req) {
        String url = req.getParameter("url");
        Properties overrides = null;
        if (nonNull(url) && url.length()>0) {
            overrides = new Properties();
            overrides.put(Context.PROVIDER_URL, url);
        }
        return overrides;
    }

}
