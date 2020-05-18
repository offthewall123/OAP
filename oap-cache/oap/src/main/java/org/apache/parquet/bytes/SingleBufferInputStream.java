//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.apache.parquet.bytes;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

class SingleBufferInputStream extends ByteBufferInputStream {
    private final ByteBuffer buffer;
    private final long startPosition;
    private int mark = -1;

    SingleBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer.duplicate();
        this.startPosition = (long)buffer.position();
    }

    public long position() {
        return (long)this.buffer.position() - this.startPosition;
    }

    public int read() throws IOException {
        if (!this.buffer.hasRemaining()) {
            throw new EOFException();
        } else {
            return this.buffer.get() & 255;
        }
    }

    public int read(byte[] bytes, int offset, int length) throws IOException {
        if (length == 0) {
            return 0;
        } else {
            int remaining = this.buffer.remaining();
            if (remaining <= 0) {
                return -1;
            } else {
                int bytesToRead = Math.min(this.buffer.remaining(), length);
                this.buffer.get(bytes, offset, bytesToRead);
                return bytesToRead;
            }
        }
    }

    public long skip(long n) {
        if (n == 0L) {
            return 0L;
        } else if (this.buffer.remaining() <= 0) {
            return -1L;
        } else {
            int bytesToSkip = (int)Math.min((long)this.buffer.remaining(), n);
            this.buffer.position(this.buffer.position() + bytesToSkip);
            return (long)bytesToSkip;
        }
    }

    public int read(ByteBuffer out) {
        int bytesToCopy;
        ByteBuffer copyBuffer;
        if (this.buffer.remaining() <= out.remaining()) {
            bytesToCopy = this.buffer.remaining();
            copyBuffer = this.buffer;
        } else {
            bytesToCopy = out.remaining();
            copyBuffer = this.buffer.duplicate();
            copyBuffer.limit(this.buffer.position() + bytesToCopy);
            this.buffer.position(this.buffer.position() + bytesToCopy);
        }

        out.put(copyBuffer);
        out.flip();
        return bytesToCopy;
    }

    public ByteBuffer slice(int length) throws EOFException {
        if (this.buffer.remaining() < length) {
            throw new EOFException();
        } else {
            ByteBuffer copy = this.buffer.duplicate();
            copy.limit(copy.position() + length);
            this.buffer.position(this.buffer.position() + length);
            return copy;
        }
    }

    public List<ByteBuffer> sliceBuffers(long length) throws EOFException {
        if (length == 0L) {
            return Collections.emptyList();
        } else if (length > (long)this.buffer.remaining()) {
            throw new EOFException();
        } else {
            return Collections.singletonList(this.slice((int)length));
        }
    }

    public List<ByteBuffer> remainingBuffers() {
        if (this.buffer.remaining() <= 0) {
            return Collections.emptyList();
        } else {
            ByteBuffer remaining = this.buffer.duplicate();
            this.buffer.position(this.buffer.limit());
            return Collections.singletonList(remaining);
        }
    }

    public void mark(int readlimit) {
        this.mark = this.buffer.position();
    }

    public void reset() throws IOException {
        if (this.mark >= 0) {
            this.buffer.position(this.mark);
            this.mark = -1;
        } else {
            throw new IOException("No mark defined");
        }
    }

    public boolean markSupported() {
        return true;
    }

    public int available() {
        return this.buffer.remaining();
    }
}
