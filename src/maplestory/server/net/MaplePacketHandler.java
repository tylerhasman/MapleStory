package maplestory.server.net;

import java.awt.Point;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;

public abstract class MaplePacketHandler {

	public abstract void handle(ByteBuf buf, MapleClient client) throws Exception;
	
	protected static String readMapleAsciiString(ByteBuf buf){
		int length = buf.readUnsignedShort();
		
		byte[] buffer = new byte[length];
		
		buf.readBytes(buffer);
		
		return new String(buffer);
	}
	
	protected static Point readPosition(ByteBuf buf){
		int x = buf.readShort();
		int y = buf.readShort();
		
		return new Point(x, y);
	}
}
