//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.apache.parquet.bytes;

import java.nio.ByteBuffer;

public interface ByteBufferAllocator {
    ByteBuffer allocate(int var1);

    void release(ByteBuffer var1);

    boolean isDirect();
}
