package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.life.movement.LifeMovementFactory;
import maplestory.life.movement.MovementPath;
import maplestory.server.net.MaplePacketHandler;

public abstract class MovementPacketHandler extends MaplePacketHandler {

	protected MovementPath parseMovement(ByteBuf lea) {
		return LifeMovementFactory.decodeMovements(lea);
    }
	
}
