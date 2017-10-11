package maplestory.server.net;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
	
	public static void main(String[] args) {
		byte key[] = {0x13, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, (byte) 0xB4, 0x00, 0x00, 0x00, 0x1B, 0x00, 0x00, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x33, 0x00, 0x00, 0x00, 0x52, 0x00, 0x00, 0x00};
	    byte iv[] = {1, 2, 3, 4};
		MapleAESOFB send_crypto = new MapleAESOFB(key, iv, (short) (0xFFFF - 83));
		byte[] input = new byte[] {50, 50, 50};
		byte[] header = send_crypto.getPacketHeader(input.length);
		
		ByteBuf out = Unpooled.buffer();
		
		out.writeBytes(header);
		
		input = MapleCustomEncryption.encryptData(input);
		
		send_crypto.crypt(input);
		
		out.writeBytes(input);
		
		out = out.capacity(out.writerIndex());
		
		System.out.println(Arrays.toString(out.array()));
	}
	
}
