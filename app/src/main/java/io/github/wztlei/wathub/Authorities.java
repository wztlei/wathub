package io.github.wztlei.wathub;

import io.github.wztlei.wathub.utils.Joiner;

public final class Authorities {

    private static final String BASE_AUTHORITY = Constants.APPLICATION_ID;

    private Authorities() {
        throw new AssertionError();
    }

    private static String createAuthority(final String... parts) {
        return Joiner.on('.').join(parts);
    }

    public static String prefsFile(final String name) {
        return createAuthority(BASE_AUTHORITY, "file", name);
    }

    public static String key(final String key) {
        return createAuthority(BASE_AUTHORITY, "key", key);
    }

    public static int requestCode(final String name) {
        return createAuthority(BASE_AUTHORITY, name).hashCode() & 0xFFFF;
    }

}
