package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.MoveRecord;
import packets.data.WorldPosData;

/**
 * Sent to acknowledge a `NewTickPacket`, and to notify the
 * server of the client's current position and time.
 */
public class MovePacket extends Packet {
    /**
     * The tick id of the `NewTickPacket` which this is acknowledging.
     */
    public int tickId;
    /**
     * The current client time.
     */
    public int time;
    /**
     * The current server time in ms.
     */
    public int serverRealTimeMS;
    /**
     * The current client position.
     */
    public WorldPosData newPosition;
    /**
     * The move records of the client.
     * <p>
     * This property can be an empty array.
     */
    public MoveRecord[] records;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        tickId = buffer.readInt();
        time = buffer.readInt();
        newPosition = new WorldPosData().deserialize(buffer);
        records = new MoveRecord[buffer.readShort()];
        for (int i = 0; i < records.length; i++) {
            records[i] = new MoveRecord().deserialize(buffer);
        }
    }
}