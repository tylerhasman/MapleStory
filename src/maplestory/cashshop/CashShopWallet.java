package maplestory.cashshop;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.MapleStory;
import database.MapleDatabase;
import database.QueryResult;

@ToString
public class CashShopWallet {
	
	private static final Map<Integer, CashShopWallet> walletCache = new HashMap<>();
	
	private final int accountId;
	
	@Getter
	private int nxCash, maplePoints, prepaidCash;
	
	CashShopWallet(int account, int cash, int mp, int pc) {
		accountId = account;
		nxCash = cash;
		maplePoints = mp;
		prepaidCash = pc;
	}
	
	public void commitChanges() throws SQLException{
		MapleDatabase.getInstance().execute("UPDATE `accounts` SET `nx_cash`=?,`maple_points`=?,`nx_prepaid`=? WHERE `id`=?", nxCash, maplePoints, prepaidCash, accountId);
	}
	
	public boolean spendCash(CashShopCurrency type, int amount){
		if(getCash(type) >= amount){
			setCash(type, getCash(type) - amount);
			
			return true;
		}else{
			return false;
		}
	}
	
	public boolean giveCash(CashShopCurrency type, int amount){
		if(getCash(type) >= amount || amount > 0){
			setCash(type, getCash(type) + amount);
			
			return true;
		}else{
			return false;
		}
	}
	
	private void setCash(CashShopCurrency type, int value){
		if(type == null){
			throw new IllegalArgumentException("type cannot be null");
		}
		if(type == CashShopCurrency.NX_CASH){
			nxCash = value;
		}else if(type == CashShopCurrency.MAPLE_POINTS){
			maplePoints = value;
		}else if(type == CashShopCurrency.PREPAID){
			prepaidCash = value;
		}else{
			throw new IllegalArgumentException("Unsupported currency "+type);
		}
	}
	
	public int getCash(CashShopCurrency type){
		if(type == null){
			throw new IllegalArgumentException("type cannot be null");
		}
		if(type == CashShopCurrency.NX_CASH){
			return nxCash;
		}else if(type == CashShopCurrency.MAPLE_POINTS){
			return maplePoints;
		}else if(type == CashShopCurrency.PREPAID){
			return prepaidCash;
		}else{
			throw new IllegalArgumentException("Unsupported currency "+type);
		}
	}
	
	@AllArgsConstructor
	public static enum CashShopCurrency {
		
		NX_CASH(1),
		MAPLE_POINTS(2),
		PREPAID(4);
		
		@Getter
		private final int networkCode;
		
		public static CashShopCurrency getById(int id){
			for(CashShopCurrency cur : values()){
				if(cur.networkCode == id){
					return cur;
				}
			}
			return null;
		}
		
	}
	
	public static CashShopWallet getWallet(int accountId){
		
		if(walletCache.containsKey(accountId)){
			return walletCache.get(accountId);
		}
		
		CashShopWallet wallet = null;
		
		try {
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `nx_cash`,`maple_points`,`nx_prepaid` FROM `accounts` WHERE `id`=?", accountId);
		
			if(results.size() > 0){
				QueryResult result = results.get(0);
				
				int nxCash = result.get("nx_cash");
				int maplePoints = result.get("maple_points");
				int prepaidCash = result.get("nx_prepaid");
				
				wallet = new CashShopWallet(accountId, nxCash, maplePoints, prepaidCash);
			
				if(MapleStory.getServerConfig().isCacheCashShopWalletsEnabled()){
					walletCache.put(accountId, wallet);
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return wallet;
	}
	
	public static CashShopWallet getWallet(MapleClient client){
		return getWallet(client.getId());
	}
	
	public static CashShopWallet getWallet(MapleCharacter chr){
		return getWallet(chr.getAccountId());
	}
	
}
