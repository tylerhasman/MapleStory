package maplestory.server.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteOrder;
import maplestory.util.Hex;

public class MaplePacket {

	private byte[] payload;
	private short type;
	
	public MaplePacket(short type, byte[] payload) {
		this.payload = payload;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return getType() + " [ " + Hex.toHex(payload)+ " ]";
	}
	
	public short getType() {
		return type;
	}
	
	public ByteBuf getBuffer(){
		return Unpooled.wrappedBuffer(payload).order(ByteOrder.LITTLE_ENDIAN);
	}
	
}
