package p1.client.server.utils;

import java.util.UUID;

public class GUID {
    public static long get() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }
}
