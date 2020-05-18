//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.apache.parquet.bytes;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BytesInput {
    private static final Logger LOG = LoggerFactory.getLogger(BytesInput.class);
    private static final boolean DEBUG = false;
    private static final BytesInput.EmptyBytesInput EMPTY_BYTES_INPUT = new BytesInput.EmptyBytesInput();

    private static class ByteBufferBytesInput extends BytesInput {
        private final ByteBuffer buffer;

        private ByteBufferBytesInput(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        public void writeAllTo(OutputStream out) throws IOException {
            Channels.newChannel(out).write(this.buffer.duplicate());
        }

        public ByteBufferInputStream toInputStream() {
            return ByteBufferInputStream.wrap(new ByteBuffer[]{this.buffer});
        }

        public long size() {
            return (long)this.buffer.remaining();
        }
    }

    public BytesInput() {
    }

    public ByteBuffer toByteBuffer() throws IOException {
        return ByteBuffer.wrap(this.toByteArray());
    }

    public ByteBufferInputStream toInputStream() throws IOException {
        return ByteBufferInputStream.wrap(new ByteBuffer[]{this.toByteBuffer()});
    }

    public static BytesInput concat(BytesInput... inputs) {
        return new BytesInput.SequenceBytesIn(Arrays.asList(inputs));
    }
    private static class BufferListBytesInput extends BytesInput {
        private final List<ByteBuffer> buffers;
        private final long length;

        public BufferListBytesInput(List<ByteBuffer> buffers) {
            this.buffers = buffers;
            long totalLen = 0L;

            ByteBuffer buffer;
            for(Iterator var4 = buffers.iterator(); var4.hasNext(); totalLen += (long)buffer.remaining()) {
                buffer = (ByteBuffer)var4.next();
            }

            this.length = totalLen;
        }

        public void writeAllTo(OutputStream out) throws IOException {
            WritableByteChannel channel = Channels.newChannel(out);
            Iterator var3 = this.buffers.iterator();

            while(var3.hasNext()) {
                ByteBuffer buffer = (ByteBuffer)var3.next();
                channel.write(buffer.duplicate());
            }

        }

        public ByteBufferInputStream toInputStream() {
            return ByteBufferInputStream.wrap(this.buffers);
        }

        public long size() {
            return this.length;
        }
    }

    public static BytesInput concat(List<BytesInput> inputs) {
        return new BytesInput.SequenceBytesIn(inputs);
    }

    public static BytesInput from(InputStream in, int bytes) {
        return new BytesInput.StreamBytesInput(in, bytes);
    }

    public static BytesInput from(List<ByteBuffer> buffers) {
        return (BytesInput)(buffers.size() == 1 ? new BytesInput.ByteBufferBytesInput((ByteBuffer)buffers.get(0)) : new BytesInput.BufferListBytesInput(buffers));
    }

    public static BytesInput from(byte[] in) {
        LOG.debug("BytesInput from array of {} bytes", in.length);
        return new BytesInput.ByteArrayBytesInput(in, 0, in.length);
    }

    public static BytesInput from(byte[] in, int offset, int length) {
        LOG.debug("BytesInput from array of {} bytes", length);
        return new BytesInput.ByteArrayBytesInput(in, offset, length);
    }

    public static BytesInput fromInt(int intValue) {
        return new BytesInput.IntBytesInput(intValue);
    }

    public static BytesInput fromUnsignedVarInt(int intValue) {
        return new BytesInput.UnsignedVarIntBytesInput(intValue);
    }

    public static BytesInput fromZigZagVarInt(int intValue) {
        int zigZag = intValue << 1 ^ intValue >> 31;
        return new BytesInput.UnsignedVarIntBytesInput(zigZag);
    }

    public static BytesInput from(CapacityByteArrayOutputStream arrayOut) {
        return new BytesInput.CapacityBAOSBytesInput(arrayOut);
    }

    public static BytesInput from(ByteArrayOutputStream baos) {
        return new BytesInput.BAOSBytesInput(baos);
    }

    public static BytesInput empty() {
        return EMPTY_BYTES_INPUT;
    }

    public static BytesInput copy(BytesInput bytesInput) throws IOException {
        return from(bytesInput.toByteArray());
    }

    public abstract void writeAllTo(OutputStream var1) throws IOException;

    public byte[] toByteArray() throws IOException {
        BytesInput.BAOS baos = new BytesInput.BAOS((int)this.size());
        this.writeAllTo(baos);
        LOG.debug("converted {} to byteArray of {} bytes", this.size(), baos.size());
        return baos.getBuf();
    }

    public abstract long size();

    private static class ByteArrayBytesInput extends BytesInput {
        private final byte[] in;
        private final int offset;
        private final int length;

        private ByteArrayBytesInput(byte[] in, int offset, int length) {
            this.in = in;
            this.offset = offset;
            this.length = length;
        }

        public void writeAllTo(OutputStream out) throws IOException {
            out.write(this.in, this.offset, this.length);
        }

        public long size() {
            return (long)this.length;
        }
    }

    private static class BAOSBytesInput extends BytesInput {
        private final ByteArrayOutputStream arrayOut;

        private BAOSBytesInput(ByteArrayOutputStream arrayOut) {
            this.arrayOut = arrayOut;
        }

        public void writeAllTo(OutputStream out) throws IOException {
            this.arrayOut.writeTo(out);
        }

        public long size() {
            return (long)this.arrayOut.size();
        }
    }

    private static class CapacityBAOSBytesInput extends BytesInput {
        private final CapacityByteArrayOutputStream arrayOut;

        private CapacityBAOSBytesInput(CapacityByteArrayOutputStream arrayOut) {
            this.arrayOut = arrayOut;
        }

        public void writeAllTo(OutputStream out) throws IOException {
            this.arrayOut.writeTo(out);
        }

        public long size() {
            return this.arrayOut.size();
        }
    }

    private static class EmptyBytesInput extends BytesInput {
        private EmptyBytesInput() {
        }

        public void writeAllTo(OutputStream out) throws IOException {
        }

        public long size() {
            return 0L;
        }
    }

    private static class UnsignedVarIntBytesInput extends BytesInput {
        private final int intValue;

        public UnsignedVarIntBytesInput(int intValue) {
            this.intValue = intValue;
        }

        public void writeAllTo(OutputStream out) throws IOException {
            BytesUtils.writeUnsignedVarInt(this.intValue, out);
        }

        public long size() {
            int s = 5 - (Integer.numberOfLeadingZeros(this.intValue) + 3) / 7;
            return s == 0 ? 1L : (long)s;
        }
    }

    private static class IntBytesInput extends BytesInput {
        private final int intValue;

        public IntBytesInput(int intValue) {
            this.intValue = intValue;
        }

        public void writeAllTo(OutputStream out) throws IOException {
            BytesUtils.writeIntLittleEndian(out, this.intValue);
        }

        public long size() {
            return 4L;
        }
    }

    private static class SequenceBytesIn extends BytesInput {
        private static final Logger LOG = LoggerFactory.getLogger(BytesInput.SequenceBytesIn.class);
        private final List<BytesInput> inputs;
        private final long size;

        private SequenceBytesIn(List<BytesInput> inputs) {
            this.inputs = inputs;
            long total = 0L;

            BytesInput input;
            for(Iterator i$ = inputs.iterator(); i$.hasNext(); total += input.size()) {
                input = (BytesInput)i$.next();
            }

            this.size = total;
        }

        public void writeAllTo(OutputStream out) throws IOException {
            Iterator i$ = this.inputs.iterator();

            while(i$.hasNext()) {
                BytesInput input = (BytesInput)i$.next();
                LOG.debug("write {} bytes to out", input.size());
                if (input instanceof BytesInput.SequenceBytesIn) {
                    LOG.debug("{");
                }

                input.writeAllTo(out);
                if (input instanceof BytesInput.SequenceBytesIn) {
                    LOG.debug("}");
                }
            }

        }

        public long size() {
            return this.size;
        }
    }

    private static class StreamBytesInput extends BytesInput {
        private static final Logger LOG = LoggerFactory.getLogger(BytesInput.StreamBytesInput.class);
        private final InputStream in;
        private final int byteCount;

        private StreamBytesInput(InputStream in, int byteCount) {
            this.in = in;
            this.byteCount = byteCount;
        }

        public void writeAllTo(OutputStream out) throws IOException {
            LOG.debug("write All {} bytes", this.byteCount);
            out.write(this.toByteArray());
        }

        public byte[] toByteArray() throws IOException {
            LOG.debug("read all {} bytes", this.byteCount);
            byte[] buf = new byte[this.byteCount];
            (new DataInputStream(this.in)).readFully(buf);
            return buf;
        }

        public long size() {
            return (long)this.byteCount;
        }
    }

    private static final class BAOS extends ByteArrayOutputStream {
        private BAOS(int size) {
            super(size);
        }

        public byte[] getBuf() {
            return this.buf;
        }
    }

}
