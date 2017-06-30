package org.wildfly.test.seccontext.servlet;

import static org.wildfly.test.seccontext.shared.IdentityUtils.switchIdentity;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Callable;

import javax.annotation.security.DeclareRoles;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wildfly.test.seccontext.shared.ReAuthnType;
import org.wildfly.test.seccontext.shared.WhoAmI;

@WebServlet(urlPatterns = "/InjectedWhoAmIServlet")
@ServletSecurity(@HttpConstraint(rolesAllowed = { "servlet" }))
@DeclareRoles({ "entry", "whoami", "servlet" })
public class InjectedWhoAmIServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB(lookup = "ejb:/seccontext-whoami/WhoAmIBean!org.wildfly.test.seccontext.shared.WhoAmI")
    private WhoAmI bean;

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

        if ("getCallerPrincipal".equals(method)) {
            try {
                Callable<Void> callable = () -> {
                    writer.write(bean.getCallerPrincipal().getName());
                    return null;
                };
                switchIdentity(username, password, callable, reAuthnType);
            } catch (Exception e) {
                throw new IOException("Unexpected failure", e);
            }
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
        } else {
            throw new IllegalArgumentException("Parameter 'method' either missing or invalid method='" + method + "'");
        }

    }
}
