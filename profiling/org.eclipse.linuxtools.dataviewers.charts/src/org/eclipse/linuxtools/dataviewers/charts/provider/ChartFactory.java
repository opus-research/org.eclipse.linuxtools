/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.charts.provider;

import java.util.List;

import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField;
import org.eclipse.linuxtools.dataviewers.charts.view.ChartView;
import org.eclipse.linuxtools.dataviewers.piechart.PieChart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;

/**
 * A utility class that handles the charts creation (pie chart & bar chart)
 * 
 * @author Marzia Maugeri <marzia.maugeri@st.com>
 *
 */
public class ChartFactory {
	
	/** 
	 * Produces a pie chart from the input objects.
	 * 
	 * @param objects the input data
	 * @param nameField the field used to get the labels of the objects (colored parts in the pie).
	 * @param valField the field providing the values for the pie parts.
	 * @return a new pie chart
	 */
	public static final Chart producePieChart(Object[] objects, ISTDataViewersField nameField, List<IChartField> valFields, String title)
	{

		ChartView view;
		try {

			final Color WHITE = new Color(Display.getDefault(), 255, 255, 255);
			final Color BLACK = new Color(Display.getDefault(), 0, 0, 0);
			final Color GRAD = new Color(Display.getDefault(), 225, 225, 225);

			view = (ChartView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ChartView.VIEW_ID, ""+(ChartView.getSecId()), IWorkbenchPage.VIEW_ACTIVATE);
			PieChart chart = new PieChart(view.getParent(), SWT.NONE);

			chart.setBackground(WHITE);
			chart.setBackgroundInPlotArea(GRAD);

			chart.getTitle().setText(title);
			chart.getTitle().setForeground(BLACK);

			chart.getLegend().setPosition(SWT.RIGHT);

			String [] valueLabels = new String [objects.length];
			for (int i = 0; i < objects.length; i++) {
				valueLabels[i] = nameField.getValue(objects[i]);
			}

			/*String [] pieChartNames = new String [valFields.size()];
			for (int i = 0; i < valFields.size(); i++) {
				pieChartNames[i] = valFields.get(i).getColumnHeaderText();
			}*/

			// pie chart data is grouped by columns
			// row size is the number of pie charts
			// column size is the number of data per pie chart
			double [][] doubleValues = new double [objects.length][valFields.size()];

			// data
			for (int i = 0; i < valFields.size(); i++) {
				for (int j = 0; j < objects.length; j++) {
					Number num = valFields.get(i).getNumber(objects[j]);
					double longVal = num.doubleValue();
					doubleValues[j][i] = longVal + 1;
				}
			}

			chart.addPieChartSeries(valueLabels, doubleValues);
			chart.getAxisSet().adjustRange();

			return chart;
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}
		
	/** 
	 * Produces a 2D bar chart from the input objects.
	 * 
	 * @param objects the input data
	 * @param nameField the field used to get the labels of the objects (the labels of the series groups).
	 * @param valFields the fields providing the values for the different bars in a series group.
	 * @param horizontal if true the bars are displayed horizontally, else vertically.
	 * @return a new 2D bar chart
	 */
	
	public static final Chart produceBarChart( Object[] objects, final ISTDataViewersField nameField, List<IChartField> valFields, String title,boolean horizontal)
	{
		ChartView view;
		try {
			
			final Color WHITE = new Color(Display.getDefault(), 255, 255, 255);
			final Color BLACK = new Color(Display.getDefault(), 0, 0, 0);
			final Color GRAD = new Color(Display.getDefault(), 225, 225, 225);
			
			view = (ChartView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ChartView.VIEW_ID, ""+(ChartView.getSecId()), IWorkbenchPage.VIEW_ACTIVATE);
			Chart chart = new Chart(view.getParent(), SWT.NONE);

			chart.setBackground(WHITE);
			chart.setBackgroundInPlotArea(GRAD);

			chart.getTitle().setText(title);
			chart.getTitle().setForeground(BLACK);

			// this is correct (refers to orientation of x-axis, not bars)
			if (horizontal){
				chart.setOrientation(SWT.VERTICAL);
			}else{
				chart.setOrientation(SWT.HORIZONTAL);
			}

			chart.getLegend().setPosition(SWT.RIGHT);
			
			String [] textLabels = new String [objects.length];
			for (int i = 0; i < objects.length; i++) {
				textLabels[i] = nameField.getValue(objects[i]);
			}
			
			// x-axis
			IAxis xAxis = chart.getAxisSet().getXAxis(0);
			xAxis.getGrid().setStyle(LineStyle.NONE);
			xAxis.getTick().setForeground(BLACK);
			ITitle xTitle = xAxis.getTitle();
			xTitle.setForeground(BLACK);
			xTitle.setText(nameField.getColumnHeaderText());
			xAxis.setCategorySeries(textLabels);
			xAxis.enableCategory(true);

			// y-axis
			IAxis yAxis = chart.getAxisSet().getYAxis(0);
			yAxis.getGrid().setStyle(LineStyle.NONE);
			yAxis.getTick().setForeground(BLACK);
			yAxis.getTitle().setVisible(false);

			// data
			for (IChartField field : valFields) {
				final IBarSeries bs = (IBarSeries) chart.getSeriesSet().
						createSeries(SeriesType.BAR, field.getColumnHeaderText());
				bs.setBarColor(new Color(Display.getDefault(), getRC(), getRC(), getRC()));
				double [] doubleValues = new double [objects.length];

				for (int i = 0; i < objects.length; i++) {
					Number num = field.getNumber(objects[i]);
					double longVal = num.doubleValue();
					doubleValues[i] = longVal;
				}

				bs.setYSeries(doubleValues);
			}

			chart.getAxisSet().adjustRange();

			return chart;
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static int getRC () {
		return (int) (Math.random() * 255);
	}
	
	/**
	 * @param viewer
	 * @return the field used to provide the labels to the series
	 */
	public ISTDataViewersField getLabelField(AbstractSTViewer viewer) {
		return viewer.getAllFields()[0];
	}
}
