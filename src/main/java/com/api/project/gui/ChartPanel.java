package com.api.project.gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.Zoomable;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.api.project.util.chart.ChartUtil;

public class ChartPanel {

	private Point point = null;
	public ChartPanel() {
	}

	/**
	 * 创建数据集合
	 * 
	 * @return
	 */
	public TimeSeriesCollection createDataset(TimeSeries... series) {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		for(int i=0; i<series.length; i++){
		
			dataset.addSeries(series[i]);
		}
		return dataset;
	}

	public org.jfree.chart.ChartPanel createChart(
			TimeSeriesCollection dataset, double length, 
			String title, final double lower, final double upper, final double unit) {
		
		// 2：创建Chart[创建不同图形]
		JFreeChart chart = ChartFactory.createTimeSeriesChart(title, "", "", dataset);
		// 3:设置抗锯齿，防止字体显示不清楚
		ChartUtil.setAntiAlias(chart);// 抗锯齿
		// 4:对柱子进行渲染[创建不同图形]
		ChartUtil.setTimeSeriesRender(chart.getPlot(), false, false);
		// 5:对其他部分进行渲染
		XYPlot xyplot = (XYPlot) chart.getPlot();
		ChartUtil.setXY_XAixs(xyplot);
		ChartUtil.setXY_YAixs(xyplot);
		// 日期X坐标轴
		DateAxis domainAxis = (DateAxis) xyplot.getDomainAxis();
		//domainAxis.setAutoTickUnitSelection(false);
		domainAxis.setAutoRange(true);
		domainAxis.setFixedAutoRange(length);
		domainAxis.setLowerMargin(0.1);// 左边距 边框距离  
		domainAxis.setUpperMargin(0.1);// 右边距 边框距离,防止最后边的一个数据靠近了坐标轴。  
		//domainAxis.setTickMarkPaint(Color.BLACK);
		
		ValueAxis rangeaxis = xyplot.getRangeAxis();
		rangeaxis.setRange(lower, upper);
		//rangeaxis.setAutoRangeMinimumSize(200); 
		//rangeaxis.setLowerBound(10);
		
		NumberAxis numberAxis = (NumberAxis) rangeaxis;
		numberAxis .setAutoTickUnitSelection(true);
    	NumberTickUnit ntu= new NumberTickUnit(unit);
    	//numberAxis.setTickUnit(ntu);
    	numberAxis.setTickUnit(ntu);
    	//numberAxis.setAutoRangeMinimumSize(10);
    	//numberAxis.setTickMarkStroke(new BasicStroke(1.6f));
		
		
		ChartUtil.setLegendEmptyBorder(chart);
		// 设置图例位置
		// 6:使用chartPanel接收
		final org.jfree.chart.ChartPanel chartPanel = new org.jfree.chart.ChartPanel(chart);
		
		chartPanel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				point = null;
				chartPanel.setMouseZoomable(true);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				point = e.getPoint();
				chartPanel.setMouseZoomable(false);
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if(e.isMetaDown()){
					
					JPopupMenu menu = new JPopupMenu();
					JMenuItem reset = new JMenuItem("自动调整"); 
					reset.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stu
							Plot plot = chartPanel.getChart().getPlot();
							XYLineAndShapeRenderer xyRenderer = (XYLineAndShapeRenderer) ((XYPlot) plot).getRenderer();
						    xyRenderer.setBaseShapesVisible(false);
						    chartPanel.restoreAutoBounds();
						    ValueAxis rangeaxis = ((XYPlot) plot).getRangeAxis();
						    rangeaxis.setRange(lower, upper);
						    NumberAxis numberAxis = (NumberAxis) rangeaxis;
							numberAxis .setAutoTickUnitSelection(true);
					    	NumberTickUnit ntu= new NumberTickUnit(unit);
					    	numberAxis.setTickUnit(ntu);
						    
						}
					});
					menu.add(reset);
					menu.show(chartPanel, e.getX(), e.getY());
				}
			}
		});
		//chartPanel.set
		chartPanel.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// TODO Auto-generated method stub
				 JFreeChart chart = chartPanel.getChart();
				    if (chart == null) {
				      return;
				    }
				    Plot plot = chart.getPlot();
				    if ((plot instanceof Zoomable)) {
				    	
				      XYLineAndShapeRenderer xyRenderer = (XYLineAndShapeRenderer) ((XYPlot) plot).getRenderer();
				      xyRenderer.setBaseShapesVisible(true);
				      Zoomable zoomable = (Zoomable)plot;
				      handleZoomable(zoomable, e, chartPanel);
				    }
			}
		});
		chartPanel.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				handleZoomMove(e, chartPanel);
			}
		});
		return chartPanel;
	}
	private void handleZoomMove(MouseEvent e, org.jfree.chart.ChartPanel chartPanel){
		
		Rectangle2D screenDataArea = chartPanel.getScreenDataArea();
		screenDataArea.setRect(screenDataArea.getX() + (point.x - e.getX()), 
				screenDataArea.getY() + (point.y - e.getY()), 
				screenDataArea.getWidth(), screenDataArea.getHeight());
		point = e.getPoint();
		chartPanel.zoom(screenDataArea);
	    chartPanel.repaint();
	}
	private void handleZoomable(Zoomable zoomable, 
			MouseWheelEvent e, org.jfree.chart.ChartPanel chartPanel){
	
		XYPlot xyplot = (XYPlot) chartPanel.getChart().getPlot();
		
		ValueAxis rangeaxis = xyplot.getRangeAxis();
		NumberAxis numberAxis = (NumberAxis) rangeaxis;
		numberAxis .setAutoTickUnitSelection(true);
		NumberTickUnit ntu= new NumberTickUnit(5d);
    	//numberAxis.setTickUnit(ntu);
    	numberAxis.setTickUnit(ntu, false,false);
		// 日期X坐标轴
		ValueAxis rangeAxis = (ValueAxis) xyplot.getRangeAxis();
	    
	    ChartRenderingInfo info = chartPanel.getChartRenderingInfo();
	    PlotRenderingInfo pinfo = info.getPlotInfo();
	    Point2D p = chartPanel.translateScreenToJava2D(e.getPoint());
	    if (!pinfo.getDataArea().contains(p)) {
	      return;
	    }
	    Plot plot = (Plot)zoomable;
	    boolean notifyState = plot.isNotify();
	    
	    int clicks = e.getWheelRotation();
        if(rangeAxis.getLowerBound() < 0){
	    	
	    	if(clicks > 0){
	    		return;
	    	}
	    }
        plot.setNotify(false);
	    double zf = 1.1D;
	    if (clicks < 0) {
	      zf = 1.0D / zf;
	    }
	    if (chartPanel.isDomainZoomable()) {
	      zoomable.zoomDomainAxes(zf, pinfo, p, true);
	    }
	    if (chartPanel.isRangeZoomable()) {
	    	
	  	     zoomable.zoomRangeAxes(zf, pinfo, p, true);
	  	}
//	    if(!(rangeAxis.getLowerBound() < 0)){
//	    
//	    	if (chartPanel.isRangeZoomable()) {
//			    	
//	  	  	     zoomable.zoomRangeAxes(zf, pinfo, p, true);
//	  	  	}
//	    }else{
//	    	
//	    	if(clicks < 0){
//	    		if (chartPanel.isRangeZoomable()) {
//			    	
//		  	  	     zoomable.zoomRangeAxes(zf, pinfo, p, true);
//		  	  	}
//	    	}
//	    }
	    plot.setNotify(notifyState);
	  }
}
