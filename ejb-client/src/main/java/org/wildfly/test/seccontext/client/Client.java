package org.wildfly.test.seccontext.client;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.naming.Context;

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
            for (ReAuthnType type : ReAuthnType.values()) {
                System.out.println("DoubleWhoAmI (noCreds) " + type + ": " + Arrays.toString(bean.doubleWhoAmI(null, null, type)));
                System.out.println("DoubleWhoAmI (creds) " + type + ": " + Arrays.toString(bean.doubleWhoAmI("whoami", "whoami", type)));
            }
            return null;
        };

        AuthenticationContext.empty().with(MatchRule.ALL, AuthenticationConfiguration.empty().useName("entry")
                .usePassword("entry").setSaslMechanismSelector(SaslMechanismSelector.ALL)).runCallable(callable);
    }

    public static Properties getCtxProperties() {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, Constants.INITIAL_CTX_FACTORY);
        props.put(Context.PROVIDER_URL, Constants.INITIAL_CTX_FACTORY);
        return props;
    }

}