package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import maplestory.life.movement.AbsoluteLifeMovement;
import maplestory.life.movement.ChairMovement;
import maplestory.life.movement.ChangeEquip;
import maplestory.life.movement.JumpDownMovement;
import maplestory.life.movement.LifeMovement;
import maplestory.life.movement.LifeMovementFragment;
import maplestory.life.movement.RelativeLifeMovement;
import maplestory.map.AnimatedMapleMapObject;
import maplestory.server.net.MaplePacketHandler;

public abstract class MovementPacketHandler extends MaplePacketHandler {

	protected List<LifeMovementFragment> parseMovement(ByteBuf lea) {
        List<LifeMovementFragment> res = new ArrayList<>();
        byte numCommands = lea.readByte();
        for (byte i = 0; i < numCommands; i++) {
            byte command = lea.readByte();
            switch (command) {
                case 0: // normal move
                case 5:
                case 17: { // Float
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short xwobble = lea.readShort();
                    short ywobble = lea.readShort();
                    short unk = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    alm.setUnk(unk);
                    alm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(alm);
                    break;
                }
                case 1:
                case 2:
                case 6: // fj
                case 12:
                case 13: // Shot-jump-back thing
                case 16: { // Float
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    res.add(rlm);
                    break;
                }
                case 3:
                case 4: // tele... -.-
                case 7: // assaulter
                case 8: // assassinate
                case 9: // rush
                case 14: { // Before Jump Down - fixes item/mobs dissappears
                    lea.skipBytes(9);
                    break;
                    /*case 14: {
                     short xpos = lea.readShort();
                     short ypos = lea.readShort();
                     short xwobble = lea.readShort();
                     short ywobble = lea.readShort();
                     byte newstate = lea.readByte();
                     TeleportMovement tm = new TeleportMovement(command, new Point(xpos, ypos), newstate);
                     tm.setPixelsPerSecond(new Point(xwobble, ywobble));
                     res.add(tm);
                     break;
                     } */
                }
                case 10: // Change Equip
                    res.add(new ChangeEquip(lea.readByte()));
                    break;
                case 11: { // Chair
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short unk = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    ChairMovement cm = new ChairMovement(command, new Point(xpos, ypos), duration, newstate);
                    cm.setUnk(unk);
                    res.add(cm);
                    break;
                }
                case 15: {
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short xwobble = lea.readShort();
                    short ywobble = lea.readShort();
                    short unk = lea.readShort();
                    short fh = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    JumpDownMovement jdm = new JumpDownMovement(command, new Point(xpos, ypos), duration, newstate);
                    jdm.setUnk(unk);
                    jdm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    jdm.setFH(fh);
                    res.add(jdm);
                    break;
                }
                case 21: {//Causes aran to do weird stuff when attacking o.o
                    /*byte newstate = lea.readByte();
                     short unk = lea.readShort();
                     AranMovement am = new AranMovement(command, null, unk, newstate);
                     res.add(am);*/
                    lea.skipBytes(3);
                    break;
                }
                default:
                    return null;
            }
        }
        return res;
    }

    protected void updatePosition(List<LifeMovementFragment> movement, AnimatedMapleMapObject target, int yoffset) {
        for (LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    Point position = ((LifeMovement) move).getPosition();
                    position.y += yoffset;
                    target.setPosition(position);
                }
                target.setStance(((LifeMovement) move).getNewstate());
            }
        }
        //movement.clear();
    }
	
}
