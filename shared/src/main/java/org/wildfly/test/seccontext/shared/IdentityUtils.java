package org.wildfly.test.seccontext.shared;

import java.util.concurrent.Callable;

import org.wildfly.security.auth.client.AuthenticationConfiguration;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.MatchRule;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.evidence.PasswordGuessEvidence;

public class IdentityUtils {

    public static <T> T switchIdentity(final String username, final String password, final Callable<T> callable,
            ReAuthnType type) throws Exception {
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
