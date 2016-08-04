package maplestory.server.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import maplestory.server.security.MapleAESOFB;
import maplestory.server.security.MapleCustomEncryption;

public class MaplePacketDecoder extends ByteToMessageDecoder {

	private int length = -1;
	private MapleConnectionHandler handler;
	
	public MaplePacketDecoder(MapleConnectionHandler handler) {
		this.handler = handler;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {

		if(handler == null){
			return;
		}
		
		if(length <= 0){
			
			if(buf.readableBytes() < 4){
				return;
			}
			
			length = buf.readInt();
			
			length = MapleAESOFB.getPacketLength(length);
			
		}
		
		if(buf.readableBytes() < length){
			return;
		}
		
		byte[] decryptedPacket = new byte[length];
		
		buf.readBytes(decryptedPacket);
		
		handler.getClient().getCryptRecv().crypt(decryptedPacket);
		MapleCustomEncryption.decryptData(decryptedPacket);
		
		ByteBuffer packetBuffer = ByteBuffer.wrap(decryptedPacket).order(ByteOrder.LITTLE_ENDIAN);
		
		short type = packetBuffer.getShort();
		byte[] payload = new byte[length - 2];
		
		packetBuffer.get(payload);
		
		length = -1;
		
		out.add(new MaplePacket(type, payload));
		
	}

}
