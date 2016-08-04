package maplestory.server.net;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import maplestory.client.MapleClient;
import maplestory.server.security.MapleAESOFB;
import maplestory.server.security.MapleCustomEncryption;

public class MaplePacketEncoder extends MessageToByteEncoder<byte[]> {

	private MapleConnectionHandler handler;
	
	public MaplePacketEncoder(MapleConnectionHandler handler) {
		this.handler = handler;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, byte[] message, ByteBuf out) throws Exception {
		MapleClient client = handler.getClient();
		
		if (client != null) {
			MapleAESOFB send_crypto = client.getCryptSend();
			byte[] input = new byte[message.length];
			System.arraycopy(message, 0, input, 0, message.length);
			byte[] header = send_crypto.getPacketHeader(input.length);
			
			out.writeBytes(header);
			
			input = MapleCustomEncryption.encryptData(input);
			
			send_crypto.crypt(input);
			
			out.writeBytes(input);
			
		} else {
			out.writeBytes(message);
		}

	}
	
}
