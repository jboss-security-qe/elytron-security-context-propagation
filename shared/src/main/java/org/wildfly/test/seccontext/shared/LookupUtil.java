package org.wildfly.test.seccontext.shared;

import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class LookupUtil {

    /**
     * Lookup for remote EJBs.
     */
    public static <T> T lookupEjb(String appName, String beanSimpleName, String remoteInterfaceName,
            Map<?, ?> propertyOverrides) throws NamingException {
        return lookup(getRemoteEjbName(appName, beanSimpleName, remoteInterfaceName), propertyOverrides);
    }

    public static String getRemoteEjbName(String appName, String beanSimpleName, String remoteInterfaceName) {
        return "ejb:/" + appName + "/" + beanSimpleName + "!" + remoteInterfaceName;
    }

    /**
     * Do JNDI lookup.
     */
    @SuppressWarnings("unchecked")
    public static <T> T lookup(String name, Map<?, ?> propertyOverrides) throws NamingException {
        final Properties jndiProperties = new Properties();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProperties.put(Context.PROVIDER_URL,
                System.getProperty("test.seccontext.provider.url", "remote://127.0.0.1:16444"));
        if (propertyOverrides != null) {
            jndiProperties.putAll(propertyOverrides);
        }
        final Context context = new InitialContext(jndiProperties);
        return (T) context.lookup(name);
    }
}
