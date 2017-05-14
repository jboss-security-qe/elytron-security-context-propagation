package org.wildfly.test.seccontext.entry;

import java.util.concurrent.Callable;

import javax.annotation.Resource;
import javax.ejb.SessionContext;

import org.wildfly.security.auth.client.AuthenticationConfiguration;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.MatchRule;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.evidence.PasswordGuessEvidence;
import org.wildfly.test.seccontext.shared.Entry;
import org.wildfly.test.seccontext.shared.LookupUtil;
import org.wildfly.test.seccontext.shared.ReAuthnType;
import org.wildfly.test.seccontext.shared.WhoAmI;

public class EntryBean implements Entry {

    public static final String BEAN_REMOTE_NAME = System.getProperty("seccontext.whoami.name",
            LookupUtil.getRemoteEjbName("seccontext-whoami", "WhoAmIBean", WhoAmI.class.getName()));
    // @EJB()
    // private WhoAmI whoAmIBean;

    @Resource
    private SessionContext context;

    public String whoAmI() {
        return context.getCallerPrincipal().getName();
    }

    public String[] doubleWhoAmI() {
        return doubleWhoAmI(null, null, ReAuthnType.NO_REAUTHN);
    }

    public String[] doubleWhoAmI(String username, String password, ReAuthnType type) throws Exception {
        String localWho = context.getCallerPrincipal().getName();

        final Callable<String[]> callable = () -> {
            String remoteWho = getWhoAmIBean().getCallerPrincipal().getName();
            return new String[] { localWho, remoteWho };
        };
        try {
            return switchIdentity(username, password, callable, type);
        } finally {
            String secondLocalWho = context.getCallerPrincipal().getName();
            if (secondLocalWho.equals(localWho) == false) {
                throw new IllegalStateException(
                        "Local getCallerPrincipal changed from '" + localWho + "' to '" + secondLocalWho);
            }
        }
    }

    public boolean doIHaveRole(String roleName) {
        return context.isCallerInRole(roleName);
    }

    public boolean[] doubleDoIHaveRole(String roleName) {
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

    private WhoAmI getWhoAmIBean() {
        return LookupUtil.lookup(BEAN_REMOTE_NAME, null);
    }
    
    private static <T> T switchIdentity(final String username, final String password, final Callable<T> callable,
            ReAuthnType type) throws RealmUnavailableException, Exception {
        switch (type) {
            case AUTHENTICATION_CONTEXT:
                return AuthenticationContext.empty()
                        .with(MatchRule.ALL, AuthenticationConfiguration.EMPTY.useName(username).usePassword(password))
                        .runCallable(callable);
            // TODO authContext = AuthenticationContext.empty().with(MatchRule.ALL,
            // AuthenticationConfiguration.EMPTY.useForwardedIdentity(domain));
            case SECURITY_DOMAIN_AUTHENTICATE:
                return SecurityDomain.getCurrent().authenticate(username, new PasswordGuessEvidence(password.toCharArray()))
                        .runAs(callable);
            case NO_REAUTHN:
            default:
                return callable.call();
        }
    }

}
