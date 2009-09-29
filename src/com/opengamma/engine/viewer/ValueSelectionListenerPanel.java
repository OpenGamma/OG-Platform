/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.ChannelInput;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.lang.ProcessManager;
import org.jcsp.util.OverWriteOldestBuffer;
import org.jdesktop.swingx.JXTable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.VolatilitySurfaceValueDefinition;
import com.opengamma.engine.position.Position;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;

public class ValueSelectionListenerPanel extends JPanel implements ListSelectionListener, TableModelListener {
  /**
   * 
   */
  private final Logger s_logger = LoggerFactory.getLogger(ValueSelectionListenerPanel.class);
  private final int DATA_POINTS = 50;
  private JXTable _parentTable;
  @SuppressWarnings("unused")
  private Position _position;
  private Entry<AnalyticValueDefinition<?>, AnalyticValue<?>> _row;
  private One2OneChannel _channel;
  
  static private Set<AnalyticValueDefinition<?>> s_allowsUpdate = new HashSet<AnalyticValueDefinition<?>>();
  static {
    s_allowsUpdate.add(new VolatilitySurfaceValueDefinition());
  }

  public ValueSelectionListenerPanel(JXTable parentTable) {
    _parentTable = parentTable;
    _parentTable.getSelectionModel().addListSelectionListener(this);
    TableModel model = _parentTable.getModel();
    model.addTableModelListener(this);
    setLayout(new BorderLayout());
    _channel = Channel.one2one(new OverWriteOldestBuffer(1));
    ProcessManager manager = new ProcessManager(new UpdateProcess(_channel.in()));
    manager.start();
  }
  
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    ListSelectionModel lsm = (ListSelectionModel) e.getSource();
    readChanges(lsm);
    updateComponent();
  }

  @Override
  public void tableChanged(TableModelEvent e) {
    ListSelectionModel lsm = _parentTable.getSelectionModel();
    readChanges(lsm);
    updateComponent();
  }
  
  private class UpdateProcess implements CSProcess {
    private ChannelInput _in;
    public UpdateProcess(ChannelInput in) {
      _in = in;
    }
    @Override
    public void run() {
      while (true) {
        DiscountCurve curve = (DiscountCurve) _in.read();
        if (curve != null) {
          double smallestValue = curve.getData().firstKey();
          double numYears = curve.getData().lastKey();
          double delta = numYears/DATA_POINTS;
          XYSeries xySeries = new XYSeries("Discount Curve");
          for (double t = smallestValue; t<=numYears; t+=delta) {
              xySeries.add(t, curve.getInterestRate(t));
          }
          XYSeriesCollection dataSet = new XYSeriesCollection(xySeries);
          final JFreeChart chart = ChartFactory.createXYLineChart("Discount Curve", "Time (Years)", "Rate", dataSet, PlotOrientation.VERTICAL, true, false, false);
          try {
            SwingUtilities.invokeAndWait(new Runnable() {
              public void run() {
                ValueSelectionListenerPanel.this.removeAll();
                ValueSelectionListenerPanel.this.add(new ChartPanel(chart), BorderLayout.CENTER);
                ValueSelectionListenerPanel.this.validate();              
              }
            });
          } catch (Exception e) {
            throw new RuntimeException(e); 
          }
        } else {
          try {
            SwingUtilities.invokeAndWait(new Runnable() {
              public void run() {
                ValueSelectionListenerPanel.this.removeAll();
                ValueSelectionListenerPanel.this.add(new JLabel("Nothing renderable selected"), BorderLayout.CENTER);
                ValueSelectionListenerPanel.this.validate();              
              }
            });
          } catch (Exception e) {
            throw new RuntimeException(e); 
          }
        }
      }
    }
  }
  
  public void updateComponent() {
    if (_row != null) {
      Object type = _row.getKey().getValue("TYPE");
      if (type != null && type.equals("DISCOUNT_CURVE")) {
        s_logger.info("Updating discount curve component");
        DiscountCurve curve = (DiscountCurve) _row.getValue().getValue();
        _channel.out().write(curve);
        return;
      }
    }
    _channel.out().write(null);
  }
  
  private void readChanges(ListSelectionModel lsm) {
    if (lsm.isSelectionEmpty()) {
      synchronized (this) {
        
      }
    } else {
      int selectedRow = lsm.getMinSelectionIndex();
      RowSorter<? extends TableModel> rowSorter = _parentTable.getRowSorter();
      int modelRow;
      if (rowSorter != null) {
        modelRow = rowSorter.convertRowIndexToModel(selectedRow);
      }  else {
        modelRow = selectedRow;
      }
      PortfolioSelectionListenerAndTableModel model = (PortfolioSelectionListenerAndTableModel) _parentTable.getModel();
      _row = model.getRow(modelRow);
      _position = model.getPosition();
    }
  }
}