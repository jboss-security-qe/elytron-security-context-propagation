package org.wildfly.test.seccontext.whoami;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.wildfly.test.seccontext.shared.WhoAmI;

@Stateless
@RolesAllowed("whoami")
@DeclareRoles({ "entry", "whoami", "servlet" })
public class WhoAmIBean implements WhoAmI {

    @Resource
    private SessionContext context;

    public Principal getCallerPrincipal() {
        return context.getCallerPrincipal();
    }

    public boolean doIHaveRole(String roleName) {
        return context.isCallerInRole(roleName);
    }
}
