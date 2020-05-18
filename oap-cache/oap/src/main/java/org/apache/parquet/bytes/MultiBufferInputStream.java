//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.apache.parquet.bytes;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

class MultiBufferInputStream extends ByteBufferInputStream {
    private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);
    private final List<ByteBuffer> buffers;
    private final long length;
    private Iterator<ByteBuffer> iterator;
    private ByteBuffer current;
    private long position;
    private long mark;
    private long markLimit;
    private List<ByteBuffer> markBuffers;

    MultiBufferInputStream(List<ByteBuffer> buffers) {
        this.current = EMPTY;
        this.position = 0L;
        this.mark = -1L;
        this.markLimit = 0L;
        this.markBuffers = new ArrayList();
        this.buffers = buffers;
        long totalLen = 0L;

        ByteBuffer buffer;
        for(Iterator var4 = buffers.iterator(); var4.hasNext(); totalLen += (long)buffer.remaining()) {
            buffer = (ByteBuffer)var4.next();
        }

        this.length = totalLen;
        this.iterator = buffers.iterator();
        this.nextBuffer();
    }

    public long position() {
        return this.position;
    }

    public long skip(long n) throws IOException {
        if (n <= 0L) {
            return 0L;
        } else if (this.current == null) {
            return -1L;
        } else {
            long bytesSkipped = 0L;

            while(bytesSkipped < n) {
                if (this.current.remaining() > 0) {
                    long bytesToSkip = Math.min(n - bytesSkipped, (long)this.current.remaining());
                    this.current.position(this.current.position() + (int)bytesToSkip);
                    bytesSkipped += bytesToSkip;
                    this.position += bytesToSkip;
                } else if (!this.nextBuffer()) {
                    return bytesSkipped > 0L ? bytesSkipped : -1L;
                }
            }

            return bytesSkipped;
        }
    }

    public int read(ByteBuffer out) {
        int len = out.remaining();
        if (len <= 0) {
            return 0;
        } else if (this.current == null) {
            return -1;
        } else {
            int bytesCopied = 0;

            while(bytesCopied < len) {
                if (this.current.remaining() > 0) {
                    int bytesToCopy;
                    ByteBuffer copyBuffer;
                    if (this.current.remaining() <= out.remaining()) {
                        bytesToCopy = this.current.remaining();
                        copyBuffer = this.current;
                    } else {
                        bytesToCopy = out.remaining();
                        copyBuffer = this.current.duplicate();
                        copyBuffer.limit(copyBuffer.position() + bytesToCopy);
                        this.current.position(copyBuffer.position() + bytesToCopy);
                    }

                    out.put(copyBuffer);
                    bytesCopied += bytesToCopy;
                    this.position += (long)bytesToCopy;
                } else if (!this.nextBuffer()) {
                    return bytesCopied > 0 ? bytesCopied : -1;
                }
            }

            return bytesCopied;
        }
    }

    public ByteBuffer slice(int length) throws EOFException {
        if (length <= 0) {
            return EMPTY;
        } else if (this.current == null) {
            throw new EOFException();
        } else {
            ByteBuffer slice;
            if (length > this.current.remaining()) {
                slice = ByteBuffer.allocate(length);
                int bytesCopied = this.read(slice);
                slice.flip();
                if (bytesCopied < length) {
                    throw new EOFException();
                }
            } else {
                slice = this.current.duplicate();
                slice.limit(slice.position() + length);
                this.current.position(slice.position() + length);
                this.position += (long)length;
            }

            return slice;
        }
    }

    public List<ByteBuffer> sliceBuffers(long len) throws EOFException {
        if (len <= 0L) {
            return Collections.emptyList();
        } else if (this.current == null) {
            throw new EOFException();
        } else {
            List<ByteBuffer> buffers = new ArrayList();
            long bytesAccumulated = 0L;

            while(bytesAccumulated < len) {
                if (this.current.remaining() > 0) {
                    int bufLen = (int)Math.min(len - bytesAccumulated, (long)this.current.remaining());
                    ByteBuffer slice = this.current.duplicate();
                    slice.limit(slice.position() + bufLen);
                    buffers.add(slice);
                    bytesAccumulated += (long)bufLen;
                    this.current.position(this.current.position() + bufLen);
                    this.position += (long)bufLen;
                } else if (!this.nextBuffer()) {
                    throw new EOFException();
                }
            }

            return buffers;
        }
    }

    public List<ByteBuffer> remainingBuffers() {
        if (this.position >= this.length) {
            return Collections.emptyList();
        } else {
            try {
                return this.sliceBuffers(this.length - this.position);
            } catch (EOFException var2) {
                throw new RuntimeException("[Parquet bug] Stream is bad: incorrect bytes remaining " + (this.length - this.position));
            }
        }
    }

    public int read(byte[] bytes, int off, int len) {
        if (len <= 0) {
            if (len < 0) {
                throw new IndexOutOfBoundsException("Read length must be greater than 0: " + len);
            } else {
                return 0;
            }
        } else if (this.current == null) {
            return -1;
        } else {
            int bytesRead = 0;

            while(bytesRead < len) {
                if (this.current.remaining() > 0) {
                    int bytesToRead = Math.min(len - bytesRead, this.current.remaining());
                    this.current.get(bytes, off + bytesRead, bytesToRead);
                    bytesRead += bytesToRead;
                    this.position += (long)bytesToRead;
                } else if (!this.nextBuffer()) {
                    return bytesRead > 0 ? bytesRead : -1;
                }
            }

            return bytesRead;
        }
    }

    public int read(byte[] bytes) {
        return this.read(bytes, 0, bytes.length);
    }

    public int read() throws IOException {
        if (this.current == null) {
            throw new EOFException();
        } else {
            while(this.current.remaining() <= 0) {
                if (!this.nextBuffer()) {
                    throw new EOFException();
                }
            }

            ++this.position;
            return this.current.get() & 255;
        }
    }

    public int available() {
        long remaining = this.length - this.position;
        return remaining > 2147483647L ? 2147483647 : (int)remaining;
    }

    public void mark(int readlimit) {
        if (this.mark >= 0L) {
            this.discardMark();
        }

        this.mark = this.position;
        this.markLimit = this.mark + (long)readlimit + 1L;
        if (this.current != null) {
            this.markBuffers.add(this.current.duplicate());
        }

    }

    public void reset() throws IOException {
        if (this.mark >= 0L && this.position < this.markLimit) {
            this.position = this.mark;
            this.iterator = concat(this.markBuffers.iterator(), this.iterator);
            this.discardMark();
            this.nextBuffer();
        } else {
            throw new IOException("No mark defined or has read past the previous mark limit");
        }
    }

    private void discardMark() {
        this.mark = -1L;
        this.markLimit = 0L;
        this.markBuffers = new ArrayList();
    }

    public boolean markSupported() {
        return true;
    }

    private boolean nextBuffer() {
        if (!this.iterator.hasNext()) {
            this.current = null;
            return false;
        } else {
            this.current = ((ByteBuffer)this.iterator.next()).duplicate();
            if (this.mark >= 0L) {
                if (this.position < this.markLimit) {
                    this.markBuffers.add(this.current.duplicate());
                } else {
                    this.discardMark();
                }
            }

            return true;
        }
    }

    private static <E> Iterator<E> concat(Iterator<E> first, Iterator<E> second) {
        return new MultiBufferInputStream.ConcatIterator(first, second);
    }

    private static class ConcatIterator<E> implements Iterator<E> {
        private final Iterator<E> first;
        private final Iterator<E> second;
        boolean useFirst = true;

        public ConcatIterator(Iterator<E> first, Iterator<E> second) {
            this.first = first;
            this.second = second;
        }

        public boolean hasNext() {
            if (this.useFirst) {
                if (this.first.hasNext()) {
                    return true;
                } else {
                    this.useFirst = false;
                    return this.second.hasNext();
                }
            } else {
                return this.second.hasNext();
            }
        }

        public E next() {
            if (this.useFirst && !this.first.hasNext()) {
                this.useFirst = false;
            }

            if (!this.useFirst && !this.second.hasNext()) {
                throw new NoSuchElementException();
            } else {
                return this.useFirst ? this.first.next() : this.second.next();
            }
        }

        public void remove() {
            if (this.useFirst) {
                this.first.remove();
            }

            this.second.remove();
        }
    }
}
