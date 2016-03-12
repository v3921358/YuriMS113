package tools.data.output;

import java.io.ByteArrayOutputStream;

class BAOSByteOutputStream implements ByteOutputStream {

    private ByteArrayOutputStream baos;

    /**
     * Class constructor - Wraps the stream around a Java BAOS.
     *
     * @param baos <code>The ByteArrayOutputStream</code> to wrap this around.
     */
    BAOSByteOutputStream(ByteArrayOutputStream baos) {
        super();
        this.baos = baos;
    }

    /**
     * Writes a byte to the stream.
     *
     * @param b The byte to write to the stream.
     * @see tools.data.output.ByteOutputStream#writeByte(byte)
     */
    @Override
    public void writeByte(byte b) {
        baos.write(b);
    }
}
