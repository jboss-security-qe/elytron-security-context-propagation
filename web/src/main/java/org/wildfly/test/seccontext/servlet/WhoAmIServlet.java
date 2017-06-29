package org.wildfly.test.seccontext.servlet;

import static org.wildfly.test.seccontext.shared.IdentityUtils.switchIdentity;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Callable;

import javax.annotation.security.DeclareRoles;
import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wildfly.test.seccontext.shared.Entry;
import org.wildfly.test.seccontext.shared.ReAuthnType;

@WebServlet(urlPatterns = "/*")
@ServletSecurity(@HttpConstraint(rolesAllowed = { "servlet" }))
@DeclareRoles({ "entry", "whoami", "servlet" })
public class WhoAmIServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB(lookup = "ejb:/seccontext-entry/EntryBean!org.wildfly.test.seccontext.shared.Entry")
    private Entry bean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Writer writer = resp.getWriter();
        String method = req.getParameter("method");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String role = req.getParameter("role");
        String type = req.getParameter("type");
        ReAuthnType reAuthnType = null;
        if (type != null) {
            reAuthnType = ReAuthnType.valueOf(type);
        }

        if ("whoAmI".equals(method)) {
            try {
                Callable<Void> callable = () -> {
                    writer.write(bean.whoAmI());
                    return null;
                };
                switchIdentity(username, password, callable, reAuthnType);
            } catch (Exception e) {
                throw new IOException("Unexpected failure", e);
            }
        } else if ("doubleWhoAmI".equals(method)) {
            String[] response;
            try {
                if (username != null && password != null) {
                    response = bean.doubleWhoAmI(username, password, reAuthnType);
                } else {
                    response = bean.doubleWhoAmI();
                }
            } catch (EJBAccessException e) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.toString());
                return;
            } catch (Exception e) {
                throw new ServletException("Unexpected failure", e);
            }
            writer.write(response[0] + "," + response[1]);
        } else if ("doIHaveRole".equals(method)) {
            try {
                Callable<Void> callable = () -> {
                    writer.write(String.valueOf(bean.doIHaveRole(role)));
                    return null;
                };
                switchIdentity(username, password, callable, reAuthnType);
            } catch (Exception e) {
                throw new IOException("Unexpected failure", e);
            }
        } else if ("doubleDoIHaveRole".equals(method)) {
            try {
                boolean[] response = null;
                if (username != null && password != null) {
                    response = bean.doubleDoIHaveRole(role, username, password, reAuthnType);
                } else {
                    response = bean.doubleDoIHaveRole(role);
                }
                writer.write(String.valueOf(response[0]) + "," + String.valueOf(response[1]));
            } catch (Exception e) {
                throw new ServletException("Unexpected Failure", e);
            }
        } else {
            throw new IllegalArgumentException("Parameter 'method' either missing or invalid method='" + method + "'");
        }

    }
}
