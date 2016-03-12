package tools.data.output;

import java.awt.Point;
import java.nio.charset.Charset;

public class GenericLittleEndianWriter implements LittleEndianWriter {

    private static Charset BIG5 = Charset.forName("Big5");
    private ByteOutputStream bos;

    /**
     * Class constructor - Protected to prevent instantiation with no arguments.
     */
    protected GenericLittleEndianWriter() {
        // Blah!
    }

    /**
     * Sets the byte-output stream for this instance of the object.
     *
     * @param bos The new output stream to set.
     */
    void setByteOutputStream(ByteOutputStream bos) {
        this.bos = bos;
    }

    /**
     * Write an array of bytes to the stream.
     *
     * @param b The bytes to write.
     */
    @Override
    public void write(byte[] b) {
        for (int x = 0; x < b.length; x++) {
            bos.writeByte(b[x]);
        }
    }

    /**
     * Write a byte to the stream.
     *
     * @param b The byte to write.
     */
    @Override
    public void write(byte b) {
        bos.writeByte(b);
    }

    /**
     * Write a byte in integer form to the stream.
     *
     * @param b The byte as an <code>Integer</code> to write.
     */
    @Override
    public void write(int b) {
        bos.writeByte((byte) b);
    }

    @Override
    public void skip(int b) {
        write(new byte[b]);
    }

    /**
     * Write a short integer to the stream.
     *
     * @param i The short integer to write.
     */
    @Override
    public void writeShort(int i) {
        bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) ((i >>> 8) & 0xFF));
    }

    /**
     * Writes an integer to the stream.
     *
     * @param i The integer to write.
     */
    @Override
    public void writeInt(int i) {
        bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) ((i >>> 8) & 0xFF));
        bos.writeByte((byte) ((i >>> 16) & 0xFF));
        bos.writeByte((byte) ((i >>> 24) & 0xFF));
    }

    /**
     * Writes an ASCII string the the stream.
     *
     * @param s The ASCII string to write.
     */
    @Override
    public void writeAsciiString(String s) {
        write(s.getBytes(BIG5));
    }

    @Override
    public void writeAsciiString(String s, int len) {
        byte[] data = s.getBytes(BIG5);
        int min = len > data.length ? data.length : len;
        for (int i = 0; i < min; i++) {
            write(data[i]);
        }
        for (int i = min; i < len; i++) {
            write(0);
        }
    }

    /**
     * Writes a maple-convention ASCII string to the stream.
     *
     * @param s The ASCII string to use maple-convention to write.
     */
    @Override
    public void writeMapleAsciiString(String s) {
        writeShort((short) s.getBytes(BIG5).length);
        writeAsciiString(s);
    }

    /**
     * Writes a null-terminated ASCII string to the stream.
     *
     * @param s The ASCII string to write.
     */
    @Override
    public void writeNullTerminatedAsciiString(String s) {
        writeAsciiString(s);
        write(0);
    }

    /**
     * Write a long integer to the stream.
     *
     * @param l The long integer to write.
     */
    @Override
    public void writeLong(long l) {
        bos.writeByte((byte) (l & 0xFF));
        bos.writeByte((byte) ((l >>> 8) & 0xFF));
        bos.writeByte((byte) ((l >>> 16) & 0xFF));
        bos.writeByte((byte) ((l >>> 24) & 0xFF));
        bos.writeByte((byte) ((l >>> 32) & 0xFF));
        bos.writeByte((byte) ((l >>> 40) & 0xFF));
        bos.writeByte((byte) ((l >>> 48) & 0xFF));
        bos.writeByte((byte) ((l >>> 56) & 0xFF));
    }

    /**
     * Writes a 2D 4 byte position information
     *
     * @param s The Point position to write.
     */
    @Override
    public void writePos(Point s) {
        writeShort(s.x);
        writeShort(s.y);
    }

    /**
     * Writes a boolean true ? 1 : 0
     *
     * @param b The boolean to write.
     */
    @Override
    public void writeBool(final boolean b) {
        write(b ? 1 : 0);
    }

    /**
     * Writes a number of byte
     *
     * @param count zero bytes count
     */
    @Override
    public void writeZeroBytes(int count) {
        for (int i = 0; i < count; i++) {
            write(0);
        }
    }
}
