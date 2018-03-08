/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.graphs;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.IGraphColorConstants;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.ScrollAdapter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.structures.DataPoint;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.structures.NumberType;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.widgets.GraphComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;



/**
 * A generic scatter plot implementation.
 * @author Ryan Morse
 * @author Henry Hughes
 */
public class ScatterGraph extends AGraph implements IScrollGraph {
	/**
	 * Default constructor for ScatterGraph. Simply invokes the constructor from <code>Graph</code>
	 * and fires the Update Event when its done, causing the graph to draw itself.
	 */
	public ScatterGraph(GraphComposite parent, int style, String title, ScrollAdapter adapter) {
		super(parent, style, title, adapter);
		this.adapter = adapter;
		handleUpdateEvent();
	}

	@Override
	public void paintElementList(GC gc) {
		DataPoint[] points = new DataPoint[0];

		Color temp = gc.getForeground();
		Color temp1 = gc.getBackground();
		Color c;

		double xSize = super.getSize().x - (super.getXPadding()<<1);
		xSize /= (super.getLocalWidth());
		double ySize = super.getSize().y - (super.getYPadding()<<1);
		ySize /= (super.getLocalHeight());

		double px, py;

		for(int j=0; j<elementList.length; j++) {
			points = elementList[j].toArray(points);

			c = new Color(getDisplay(), IGraphColorConstants.COLORS[j]);
			gc.setForeground(c);
			gc.setBackground(c);

			for(DataPoint point:points) {
				px = (point.x-super.getLocalXMin());
				px *= xSize;
				px += super.getXPadding() - (DIAMETER>>1);

				py = super.getLocalYMax() - point.y;
				py *= ySize;
				py += super.getYPadding() - (DIAMETER>>1);

				gc.fillOval((int)px, (int)py, DIAMETER, DIAMETER);
			}
		}

		gc.setForeground(temp);
		gc.setBackground(temp1);
	}

	@Override
	public boolean isMultiGraph() {
		return adapter.getSeriesCount() > 0;
	}

	/**
	 * Updates the graph when the <code>IDataSet</code> has more data, adding the new samples to the graph.
	 */
	@Override
	public void handleUpdateEvent() {
		if(null == adapter) return;

		this.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				Object[][] data = adapter.getData(removedItems, adapter.getRecordCount());

				if(normalize) {
					double max;
					for(int j,i=0; i<adapter.getSeriesCount(); i++) {
						elementList[i].clear();	//TODO: Only temparary
						max = adapter.getYSeriesMax(i, removedItems, adapter.getRecordCount()).doubleValue() / 100;
						for(j=0; j<data.length; j++) {
							elementList[i].add(new DataPoint(NumberType.obj2num(data[j][0]).doubleValue(),
				  					  					NumberType.obj2num(data[j][i+1]).doubleValue() / max));
						}
					}
				} else {
					for(int j,i=0; i<adapter.getSeriesCount(); i++) {
						elementList[i].clear();	//TODO: Only temparary
						for(j=0; j<data.length; j++) {
							elementList[i].add(new DataPoint(NumberType.obj2num(data[j][0]).doubleValue(),
				  					  					NumberType.obj2num(data[j][i+1]).doubleValue()));
						}
					}
				}
			}
		});
		this.repaint();
	}

	private ScrollAdapter adapter;
	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.graphs.scattergraph"; //$NON-NLS-1$
	private static final int DIAMETER = 5;
}
