package com.intel.oap.common.sparkutils;

import static com.intel.oap.common.sparkutils.unsafe.Platform.*;

public class UTF8String {

    private Object base;
    private long offset;
    private int numBytes;

    protected UTF8String(Object base, long offset, int numBytes) {
        this.base = base;
        this.offset = offset;
        this.numBytes = numBytes;
    }

    public UTF8String clone() {
        return fromBytes(getBytes());
    }

    /**
     * Returns the number of bytes
     */
    public int numBytes() {
        return numBytes;
    }

    public Object getBaseObject() { return base; }

    public long getBaseOffset() { return offset; }

    /**
     * Returns the underline bytes, will be a copy of it if it's part of another array.
     */
    public byte[] getBytes() {
        // avoid copy if `base` is `byte[]`
        if (offset == BYTE_ARRAY_OFFSET && base instanceof byte[]
                && ((byte[]) base).length == numBytes) {
            return (byte[]) base;
        } else {
            byte[] bytes = new byte[numBytes];
            copyMemory(base, offset, bytes, BYTE_ARRAY_OFFSET, numBytes);
            return bytes;
        }
    }

    /**
     * Creates an UTF8String from given address (base and offset) and length.
     */
    public static UTF8String fromAddress(Object base, long offset, int numBytes) {
        return new UTF8String(base, offset, numBytes);
    }

    public UTF8String copy() {
        byte[] bytes = new byte[numBytes];
        copyMemory(base, offset, bytes, BYTE_ARRAY_OFFSET, numBytes);
        return fromBytes(bytes);
    }

    /**
     * Creates an UTF8String from byte array, which should be encoded in UTF-8.
     *
     * Note: `bytes` will be hold by returned UTF8String.
     */
    public static UTF8String fromBytes(byte[] bytes) {
        if (bytes != null) {
            return new UTF8String(bytes, BYTE_ARRAY_OFFSET, bytes.length);
        } else {
            return null;
        }
    }
}
