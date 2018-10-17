package org.logicalcobwebs.proxool;

public class ConnectionPoolFacade {

	public static ConnectionPool getConnectionPool(String alias) throws ProxoolException {
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
        return cp;
    }
	
	public static Prototyper getConnectionPoolPrototyper(String alias) throws ProxoolException {
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
        Prototyper prototyper=cp.getPrototyper();
        return prototyper;
	}
	
	public static int getMaximumConnectionCount(String alias) throws ProxoolException {
		int count=getConnectionPool(alias).getDefinition().getMaximumConnectionCount();
		return count;
	}
	
	public static int getAvailableConnectionCount(String alias) throws ProxoolException {
		int count=getConnectionPool(alias).getAvailableConnectionCount();
		return count;
	}
	
	public static String[] getConnectionPoolNames(){
		return ConnectionPoolManager.getInstance().getConnectionPoolNames();
	}
}
