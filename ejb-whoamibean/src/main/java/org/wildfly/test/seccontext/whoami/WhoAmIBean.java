package org.wildfly.test.seccontext.whoami;

import java.security.Principal;

import javax.annotation.Resource;
import javax.ejb.SessionContext;

import org.wildfly.test.seccontext.shared.WhoAmI;

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
