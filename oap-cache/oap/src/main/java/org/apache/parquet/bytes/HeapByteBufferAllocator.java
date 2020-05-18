//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.apache.parquet.bytes;

import java.nio.ByteBuffer;

public class HeapByteBufferAllocator implements ByteBufferAllocator {
    public static final HeapByteBufferAllocator getInstance() {
        return new HeapByteBufferAllocator();
    }

    public HeapByteBufferAllocator() {
    }

    public ByteBuffer allocate(int size) {
        return ByteBuffer.allocate(size);
    }

    public void release(ByteBuffer b) {
    }

    public boolean isDirect() {
        return false;
    }
}
