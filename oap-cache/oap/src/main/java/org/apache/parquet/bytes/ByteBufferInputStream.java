//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.apache.parquet.bytes;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public abstract class ByteBufferInputStream extends InputStream {
    public ByteBufferInputStream() {
    }

    public static ByteBufferInputStream wrap(ByteBuffer... buffers) {
        return (ByteBufferInputStream)(buffers.length == 1 ? new SingleBufferInputStream(buffers[0]) : new MultiBufferInputStream(Arrays.asList(buffers)));
    }

    public static ByteBufferInputStream wrap(List<ByteBuffer> buffers) {
        return (ByteBufferInputStream)(buffers.size() == 1 ? new SingleBufferInputStream((ByteBuffer)buffers.get(0)) : new MultiBufferInputStream(buffers));
    }

    public abstract long position();

    public void skipFully(long n) throws IOException {
        long skipped = this.skip(n);
        if (skipped < n) {
            throw new EOFException("Not enough bytes to skip: " + skipped + " < " + n);
        }
    }

    public abstract int read(ByteBuffer var1);

    public abstract ByteBuffer slice(int var1) throws EOFException;

    public abstract List<ByteBuffer> sliceBuffers(long var1) throws EOFException;

    public ByteBufferInputStream sliceStream(long length) throws EOFException {
        return wrap(this.sliceBuffers(length));
    }

    public abstract List<ByteBuffer> remainingBuffers();

    public ByteBufferInputStream remainingStream() {
        return wrap(this.remainingBuffers());
    }
}
