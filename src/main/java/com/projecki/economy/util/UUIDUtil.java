package com.projecki.economy.util;

import java.nio.ByteBuffer;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

public final class UUIDUtil {

    /**
     * Create a new {@code byte} array from the given {@link UUID}.
     * This will return a byte array made up of exactly {@code 16}
     * bytes (two longs) from the {@link UUID#getMostSignificantBits() most}
     * significant bits and the {@link UUID#getLeastSignificantBits() least}
     * significant bits from the UUID.
     *
     * @param uuid The UUID to turn into a byte array.
     * @return The newly create byte array made from the UUID.
     */
    public static byte[] toBytes(UUID uuid) {
        ByteBuffer buf = ByteBuffer.wrap(new byte[16]);
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
        return buf.array();
    }

    /**
     * Create a new {@link UUID} from the given {@code byte} array.
     * The byte array must be exactly {@code 16} bytes in length.
     * <p>
     * It is ideal to use this method reconstruct a UUID from a
     * byte array created using the {@link #toBytes(UUID)} method.
     *
     * @param bytes The byte array to get the UUID from.
     * @return The newly created UUID from the contents of the byte array.
     * @throws IllegalArgumentException If the byte array is not 16
     *                                  bytes in length.
     */
    public static UUID toUuid(byte[] bytes) throws IllegalArgumentException {
        checkArgument(bytes.length == 16, "must have 16 bytes");
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return new UUID(buf.getLong(), buf.getLong());
    }
}
