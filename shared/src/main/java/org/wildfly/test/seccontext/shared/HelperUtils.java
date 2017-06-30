package org.wildfly.test.seccontext.shared;

public class HelperUtils {

    public static <T> T defaultIfNull(final T object, final T defaultValue) {
        return object != null ? object : defaultValue;
    }

}
