package edu.byu.minecraft.invbackup.data;

import java.nio.ByteBuffer;
import java.util.UUID;

public class SaveDataUtils {
    public static UUID uuidFromIntArray(int[] intArray) {
        if (intArray.length != 4) {
            throw new IllegalArgumentException("The integer array must have a length of 4.");
        }

        ByteBuffer buffer = ByteBuffer.allocate(16);
        for (int i : intArray) {
            buffer.putInt(i);
        }

        buffer.rewind();
        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();

        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
