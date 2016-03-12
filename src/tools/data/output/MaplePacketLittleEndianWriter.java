package tools.data.output;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.SendOpcode;
import tools.HexTool;

public class MaplePacketLittleEndianWriter extends GenericLittleEndianWriter {

    private ByteArrayOutputStream baos;

    /**
     * Constructor - initializes this stream with a default size.
     */
    public MaplePacketLittleEndianWriter() {
        this(32);
    }

    /**
     * Constructor - initializes this stream with size <code>size</code>.
     *
     * @param size The size of the underlying stream.
     */
    public MaplePacketLittleEndianWriter(int size) {
        this.baos = new ByteArrayOutputStream(size);
        setByteOutputStream(new BAOSByteOutputStream(baos));
    }

    /**
     * Gets a <code>MaplePacket</code> instance representing this sequence of
     * bytes.
     *
     * @return A <code>MaplePacket</code> with the bytes in this stream.
     */
    public byte[] getPacket() {

        byte[] data = baos.toByteArray();
        /* byte[] h = {data[0], data[1]};
        short hv = 0;
        hv = ByteBuffer.wrap(h).order(ByteOrder.LITTLE_ENDIAN).getShort();

        for (SendOpcode header : SendOpcode.values()) {
            if (header.getValue() == hv && header.getDebugMode()) {
                System.out.println("Send :" + header.toString());
                System.out.println("Packet to be sent:\n" + HexTool.toString(baos.toByteArray()));

            }
        }*/
        return data;
    }

    /**
     * Changes this packet into a human-readable hexadecimal stream of bytes.
     *
     * @return This packet as hex digits.
     */
    @Override
    public String toString() {
        return HexTool.toString(baos.toByteArray());
    }

}
