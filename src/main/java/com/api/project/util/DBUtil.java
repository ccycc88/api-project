package com.api.project.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

	/**
	 * 
	 * @param url
	 * @param user
	 * @param pwd
	 * @param driver
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection(String url,String user,String pwd,String driver) throws Exception{
		
		Class.forName(driver);
		return DriverManager.getConnection(url, user, pwd);
	}
	
	public static void closeConnection(Connection con){
		try {
			if (con != null && !con.isClosed()) {
				con.setAutoCommit(true);
				con.close();
			}
		} catch (SQLException e) {
		}
	}
	
	public static void close(Connection con, Statement stmt, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
		}
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException e) {
		}
		try {
			if (con != null && !con.isClosed()) {
				con.setAutoCommit(true);
				con.close();
			}
		} catch (SQLException e) {
		}
	}
	
	public static boolean executeSQL(String sql, Connection conn) throws SQLException {
		Statement stmt = null;
		boolean b = false;
		try {
			stmt = conn.createStatement();
			b = stmt.execute(sql);
		} catch (SQLException e) {
			throw e;
		} finally {
			close(null, stmt, null);
		}
		return b;
	}
	public static int executeUpdate(String sql, Connection conn) throws SQLException {
		Statement stmt = null;
		int b = 0;
		try {
			stmt = conn.createStatement();
			b = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			throw e;
		} finally {
			close(null, stmt, null);
		}
		return b;
	}
}
