package org.wildfly.test.seccontext.client;

import java.util.Arrays;
import java.util.concurrent.Callable;

import org.wildfly.security.auth.client.AuthenticationConfiguration;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.MatchRule;
import org.wildfly.security.sasl.SaslMechanismSelector;
import org.wildfly.test.seccontext.shared.Constants;
import org.wildfly.test.seccontext.shared.Entry;
import org.wildfly.test.seccontext.shared.LookupUtil;
import org.wildfly.test.seccontext.shared.ReAuthnType;

public class Client {

    public static final String BEAN_REMOTE_NAME = System.getProperty(Constants.PROP_BEAN_LOOKUP_ENTRY,
            LookupUtil.getRemoteEjbName("seccontext-entry", "EntryBean", Entry.class.getName()));

    public static void main(String[] args) throws Exception {
        Callable<Void> callable = () -> {
            final Entry bean = LookupUtil.lookup(BEAN_REMOTE_NAME, null);
            System.out.println("WhoAmI: " + bean.whoAmI());
            System.out.println("DoubleWhoAmI: " + Arrays.toString(bean.doubleWhoAmI("whoami", "whoami",  ReAuthnType.SECURITY_DOMAIN_AUTHENTICATE_FORWARDED)));
            return null;
        };

        AuthenticationContext authnCtx = AuthenticationContext.empty().with(MatchRule.ALL, AuthenticationConfiguration.empty().useName("admin")
                .usePassword("admin").setSaslMechanismSelector(SaslMechanismSelector.ALL));
        authnCtx.runCallable(callable);
    }

}