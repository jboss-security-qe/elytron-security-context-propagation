package org.wildfly.test.seccontext.shared;

import java.security.Principal;

import javax.ejb.Remote;

@Remote
public interface WhoAmI {

    /**
     * @return the caller principal obtained from the EJBContext.
     */
    Principal getCallerPrincipal();

    /**
     * @param roleName - The role to check.
     * @return The result of calling EJBContext.isCallerInRole() with the supplied role name.
     */
    boolean doIHaveRole(String roleName);

}