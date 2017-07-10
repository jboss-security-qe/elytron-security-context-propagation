package org.wildfly.test.seccontext.shared;

import java.util.concurrent.Callable;

import org.wildfly.security.auth.client.AuthenticationConfiguration;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.MatchRule;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.evidence.PasswordGuessEvidence;
import org.wildfly.security.sasl.SaslMechanismSelector;

public class IdentityUtils {

    public static <T> T switchIdentity(final String username, final String password, final Callable<T> callable,
            ReAuthnType type) throws Exception {
        if (type == null) {
            type = ReAuthnType.AUTHENTICATION_CONTEXT;
        }
        switch (type) {
            case FORWARDED_IDENTITY:
                return AuthenticationContext.empty().with(MatchRule.ALL, AuthenticationConfiguration.empty()
                        .useForwardedIdentity(SecurityDomain.getCurrent()).setSaslMechanismSelector(SaslMechanismSelector.ALL))
                        .runCallable(callable);
            case AUTHENTICATION_CONTEXT:
                AuthenticationConfiguration authCfg = AuthenticationConfiguration.empty()
                        .setSaslMechanismSelector(SaslMechanismSelector.ALL);
                if (username != null) {
                    authCfg = authCfg.useName(username);
                }
                if (password != null) {
                    authCfg = authCfg.usePassword(password);
                }
                return AuthenticationContext.empty().with(MatchRule.ALL, authCfg).runCallable(callable);
            case SECURITY_DOMAIN_AUTHENTICATE:
                return password == null ? null
                        : SecurityDomain.getCurrent().authenticate(username, new PasswordGuessEvidence(password.toCharArray()))
                                .runAs(callable);
            case SECURITY_DOMAIN_AUTHENTICATE_FORWARDED:
                final Callable<T> forwardIdentityCallable = () -> {
                    return AuthenticationContext.empty()
                            .with(MatchRule.ALL,
                                    AuthenticationConfiguration.empty().useForwardedIdentity(SecurityDomain.getCurrent())
                                            .setSaslMechanismSelector(SaslMechanismSelector.ALL))
                            .runCallable(callable);
                };
                return password == null ? null
                        : SecurityDomain.getCurrent().authenticate(username, new PasswordGuessEvidence(password.toCharArray()))
                                .runAs(forwardIdentityCallable);
            case NO_REAUTHN:
            default:
                return callable.call();
        }
    }

}
