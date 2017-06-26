package org.wildfly.test.seccontext.client;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.wildfly.test.seccontext.shared.Constants;
import org.wildfly.test.seccontext.shared.Entry;
import org.wildfly.test.seccontext.shared.LookupUtil;

public class Client {

    public static final String BEAN_REMOTE_NAME = System.getProperty(Constants.PROP_BEAN_LOOKUP_ENTRY,
            LookupUtil.getRemoteEjbName("seccontext-entry", "EntryBean", Entry.class.getName()));

    public static void main(String[] args) throws Exception {
        InitialContext ctx = new InitialContext(getCtxProperties());
        try {
            Entry bean = (Entry) ctx.lookup(BEAN_REMOTE_NAME);
            System.out.println(bean.whoAmI());
        } finally {
            ctx.close();
        }
    }

    public static Properties getCtxProperties() {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, Constants.INITIAL_CTX_FACTORY);
        return props;
    }

}