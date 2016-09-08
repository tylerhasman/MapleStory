package tools;

import java.math.BigInteger;
import java.util.Arrays;

import lombok.Getter;
import maplestory.server.MapleStory;
import maplestory.util.Randomizer;

/**
 * This class by definition must replicate the programming language C's implementation of a random number generator
 * @author <a href="http://forum.ragezone.com/members/858399.html">anhtanh95</a>
 *
 */
public class CRand32 {
	
	private static final long CONSTANT = 0x6B5FCA6B;//This is defined in the MS client
	
	@Getter
    private long seed1, seed2, seed3;

    public CRand32() {//constructor
    	newSeeds();
    }

    public long random() {
        long seed1 = this.seed1;
        long seed2 = this.seed2;
        long seed3 = this.seed3;

        long newSeed1 = (seed1 << 12) ^ (seed1 >> 19) ^ ((seed1 >> 6) ^ (seed1 << 12)) & 0x1FFF;
        long newSeed2 = 16 * seed2 ^ (seed2 >> 25) ^ ((16 * seed2) ^ (seed2 >> 23)) & 0x7F;
        long newSeed3 = (seed3 >> 11) ^ (seed3 << 17) ^ ((seed3 >> 8) ^ (seed3 << 17)) & 0x1FFFFF;

        this.seed1 = newSeed1;
        this.seed2 = newSeed2;
        this.seed3 = newSeed3;
        return (newSeed1 ^ newSeed2 ^ newSeed3) & 0xffffffffl;//& 0xffffffffl will help you convert long to unsigned int
    }
    
    public static void main(String[] args){
    	
    	CRand32 r = new CRand32();
    	
    	int numRand = 11;
    	
    	long[] rand = new long[numRand];
    	
    	Arrays.setAll(rand, i -> r.random());
    	
    	int index = 0;
    	
    	for(int i = 0;i < 10;i++){
    		index++;
    		long unused = rand[index++ % numRand];
    		
    		int minDamage = 35, maxDamage = 55;
    		
    		double damage = r.randomInRange(rand[index++ % numRand], minDamage, maxDamage);
    		
    		if(r.randomInRange(rand[index++ % numRand], 0, 100) < 0){
    			//Crit!
    		}
    		
    		System.out.println(damage);
    	}
    	
    }
    
    public double randomInRange(int min, int max){
    	return randomInRange(random(), min, max);
    }
    
    public double randomInRange(long random, int min, int max){
    	if(min > max){
    		MapleStory.getLogger().warn("Random in range, min was greater than max!");
    		return randomInRange(random, max, min);
    	}
    	BigInteger ECX = new BigInteger(String.valueOf(random));
    	
    	BigInteger EAX = new BigInteger("1801439851");
    	
    	BigInteger mult = ECX.multiply(EAX);
    	
    	long highBit = mult.shiftRight(32).longValue();
        long rightShift = highBit >> 22;
        
        double randomNumber = ECX.longValue() - (rightShift * 10000000.0);
        
        double value;
        
        if(min != max) {
        	value = (max - min) * randomNumber / 9999999.0 + min;
        }else{
        	value = max;
        }
        
        return value;
    }

    public void seed(long s1, long s2, long s3) {
        this.seed1 = s1 | 0x100000;

        this.seed2 = s2 | 0x1000;

        this.seed3 = s3 | 0x10;
    }

	public void newSeeds() {
		//seed(Randomizer.nextInt(), Randomizer.nextInt(), Randomizer.nextInt());
		seed(0, 0, 0);
	}
}  