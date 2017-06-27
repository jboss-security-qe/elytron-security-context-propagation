package org.wildfly.test.seccontext.entry;

import static org.wildfly.test.seccontext.shared.IdentityUtils.switchIdentity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.NamingException;

import org.wildfly.test.seccontext.shared.Constants;
import org.wildfly.test.seccontext.shared.Entry;
import org.wildfly.test.seccontext.shared.LookupUtil;
import org.wildfly.test.seccontext.shared.ReAuthnType;
import org.wildfly.test.seccontext.shared.WhoAmI;

@Stateless
@RolesAllowed("entry")
@DeclareRoles({ "entry", "whoami", "servlet" })
public class EntryBean implements Entry {

    public static final String BEAN_REMOTE_NAME = System.getProperty(Constants.PROP_BEAN_LOOKUP_WHOAMI,
            LookupUtil.getRemoteEjbName("seccontext-whoami", "WhoAmIBean", WhoAmI.class.getName()));

    @Resource
    private SessionContext context;

    public String whoAmI() throws Exception {
        return context.getCallerPrincipal().getName();
    }

    public String[] doubleWhoAmI() {
        return doubleWhoAmI(null, null, ReAuthnType.NO_REAUTHN);
    }

    public String[] doubleWhoAmI(String username, String password, ReAuthnType type) {
        String[] result = new String[2];
        result[0] = context.getCallerPrincipal().getName();

        final Callable<String> callable = () -> {
            return getWhoAmIBean().getCallerPrincipal().getName();
        };
        try {
            result[1] = switchIdentity(username, password, callable, type);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            result[1] = sw.toString();
        } finally {
            String secondLocalWho = context.getCallerPrincipal().getName();
            if (! secondLocalWho.equals(result[0])) {
                throw new IllegalStateException(
                        "Local getCallerPrincipal changed from '" + result[0] + "' to '" + secondLocalWho);
            }
        }
        return result;
    }

    public boolean doIHaveRole(String roleName) throws Exception {
        return context.isCallerInRole(roleName);
    }

    public boolean[] doubleDoIHaveRole(String roleName) throws Exception {
        return doubleDoIHaveRole(roleName, null, null, ReAuthnType.NO_REAUTHN);
    }

    public boolean[] doubleDoIHaveRole(String roleName, String username, String password, ReAuthnType type) throws Exception {
        boolean localDoI = context.isCallerInRole(roleName);
        final Callable<boolean[]> callable = () -> {
            boolean remoteDoI = getWhoAmIBean().doIHaveRole(roleName);
            return new boolean[] { localDoI, remoteDoI };
        };
        try {
            return switchIdentity(username, password, callable, type);
        } finally {
            boolean secondLocalDoI = context.isCallerInRole(roleName);
            if (secondLocalDoI != localDoI) {
                throw new IllegalStateException("Local call to isCallerInRole for '" + roleName + "' changed from " + localDoI
                        + " to " + secondLocalDoI);
            }
        }
    }

    private WhoAmI getWhoAmIBean() throws NamingException {
        return LookupUtil.lookup(BEAN_REMOTE_NAME, null);
    }

}
