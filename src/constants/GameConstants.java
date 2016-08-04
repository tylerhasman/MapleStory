package constants;

import constants.skills.Aran;

/**
 *
 * @author kevintjuh93
 */
public class GameConstants {
	
    public static int getHiddenSkill(final int skill) {
        switch (skill) {
            case Aran.HIDDEN_FULL_DOUBLE:
            case Aran.HIDDEN_FULL_TRIPLE:
                return Aran.FULL_SWING;
            case Aran.HIDDEN_OVER_DOUBLE:
            case Aran.HIDDEN_OVER_TRIPLE:
                return Aran.OVER_SWING;
        }
        return skill;
    }
        
    public static boolean isAran(final int job) {
        return job == 2000 || (job >= 2100 && job <= 2112);
    }

	/*public static boolean hasSPTable(MapleJob job) {
		switch (job) {
		case EVAN:
		case EVAN1:
		case EVAN2:
		case EVAN3:
		case EVAN4:
		case EVAN5:
		case EVAN6:
		case EVAN7:
		case EVAN8:
		case EVAN9:
		case EVAN10:
			return true;
		default:
			return false;
		}
	}*/
}
