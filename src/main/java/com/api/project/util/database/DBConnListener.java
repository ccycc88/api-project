package com.api.project.util.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.logicalcobwebs.proxool.ConnectionListenerIF;

public class DBConnListener implements ConnectionListenerIF{

	public static final String DB_NAMEPREFIX_INFORMIX = "INFORMIX"; 
	public static final String DB_NAMEPREFIX_SYBASE = "SYBASE";
	
	/* (non-Javadoc)
	 * @see org.logicalcobwebs.proxool.ConnectionListenerIF#onBirth(java.sql.Connection)
	 */
	public void onBirth(Connection arg0) throws SQLException {
		String databaseTypeName=arg0.getMetaData().getDatabaseProductName();
//		Statement stmt = arg0.createStatement();
		if (databaseTypeName.toUpperCase().startsWith(DB_NAMEPREFIX_INFORMIX)||
				databaseTypeName.toUpperCase().startsWith(DB_NAMEPREFIX_SYBASE)) {
//			boolean b=stmt.execute("set isolation to dirty read;");
//			b=stmt.execute("set lock mode to wait 10;");	
//			System.out.println(databaseTypeName);
			if (arg0.getTransactionIsolation() != Connection.TRANSACTION_READ_UNCOMMITTED) {
				arg0.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			} 
			ProxoolPool.log.info("Set INFORMIX|SYBASE dirty read :TRANSACTION_READ_UNCOMMITTED,sucess!");
		}

	}
	
//	  public void onBirth(Connection arg0)
//	    throws SQLException
//	  {
//	    String databaseTypeName = arg0.getMetaData().getDatabaseProductName();
//	    Statement stmt = arg0.createStatement();
//	    if (databaseTypeName.toUpperCase().startsWith("INFORMIX")) {
//	      boolean b = stmt.execute("set isolation to dirty read;");
//	      System.out.println(b);
//	      b = stmt.execute("set lock mode to wait 10;");
//	      System.out.println(b);
//	      System.out.println(databaseTypeName);
//	      ProxoolPool.log.info("设置INFORMIX数据库参数set isolation to dirty read;set lock mode to wait 10;成功");
//	    }
//	  }

	/* (non-Javadoc)
	 * @see org.logicalcobwebs.proxool.ConnectionListenerIF#onDeath(java.sql.Connection)
	 */
	public void onDeath(Connection arg0) throws SQLException {
//		System.out.println(arg0.isClosed());

	}

	/* (non-Javadoc)
	 * @see org.logicalcobwebs.proxool.ConnectionListenerIF#onExecute(java.lang.String, long)
	 */
	public void onExecute(String arg0, long arg1) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.logicalcobwebs.proxool.ConnectionListenerIF#onFail(java.lang.String, java.lang.Exception)
	 */
	public void onFail(String arg0, Exception arg1) {
		// TODO Auto-generated method stub
		//System.out.println(arg1.getMessage());
	}
}
