package com.api.project.worker.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.api.project.util.helper.SysHelper;
import com.api.project.util.thread.ControllThread;

public class ExecuteSqlToTable extends ControllThread{

	private SqlCacheFactory cacheFactory = SqlCacheFactory.getInstance();
	
	private static final Log LOG = LogFactory.getFactory().getLog(ExecuteSqlToTable.class);
	
	Connection conn = null;
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
		while(this.isStart()){
			
			
			Statement st = null;
			LinkedHashMap<String, String> batchs = null;
			try {
				
				conn = null;//SqlUtil.getInstance().getCon(Constant.CORE_DB_TYPE);
				if(conn == null){
					
					SysHelper.waitIt(this, 5000);
					continue;
				}
				
				batchs = cacheFactory.taskBatch();
				if(batchs == null){
					
					SysHelper.waitIt(this, 1000);
					continue;
				}
				st = conn.createStatement();
				st.setQueryTimeout(3*60);
				Iterator<String> it = batchs.keySet().iterator();
				while(it.hasNext()){
					
					st.addBatch(it.next());
				}
				try {
					st.executeBatch();
				} catch (SQLException e) {
					LOG.error("批量入库失败" + e.getMessage());
					st.clearBatch();
					
					for (String sql : batchs.keySet()) {
						try {
							st.execute(sql);
						} catch (SQLException ex) {
							LOG.error("执行sql[" + sql + "]失败"
									+ ex.getMessage());
							
							if(ex instanceof SQLIntegrityConstraintViolationException){
								
								String sqlU = batchs.get(sql);
								try {
									if(sqlU != null){
										st.execute(sqlU);
									}
								} catch (Exception eb) {
									// TODO Auto-generated catch block
									eb.printStackTrace();
								}
							}
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}finally{
			
				//SqlUtil.getInstance().close(conn, st, null);
				if(batchs != null){
				
					batchs.clear();
				}
				
			}
		}
	}

}
