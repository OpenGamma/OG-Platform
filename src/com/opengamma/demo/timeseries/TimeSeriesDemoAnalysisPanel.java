package com.opengamma.demo.timeseries;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import com.opengamma.plot.StandardAutocorrelogram;
import com.opengamma.plot.StandardTimeSeriesDistributionAnalysisChart;
import com.opengamma.timeseries.DoubleTimeSeries;

public class TimeSeriesDemoAnalysisPanel extends JPanel {
  private final JComboBox _box;
  private final int PLOT_WIDTH = 500;
  private final int PLOT_HEIGHT = 350;
  private final Dimension SIZE = new Dimension(800, 400);
  DoubleTimeSeries _ts;
  // TODO change to JPanel
  final Map<String, JFreeChart> DATA = new TreeMap<String, JFreeChart>();
  final JPanel _analysisPlotPanel = new JPanel();
  final SpringLayout _layout = new SpringLayout();
  private static final String EMPTY_STRING = "";
  private static final String HISTOGRAM_STRING = "Histogram";
  private static final String AUTOCORRELOGRAM_STRING = "Autocorrelogram";

  public TimeSeriesDemoAnalysisPanel() {
    init();
    setLayout(_layout);
    _box = getComboBox();
    add(_box);
    _layout.putConstraint(SpringLayout.WEST, _box, 5, SpringLayout.WEST, this);
    _layout.putConstraint(SpringLayout.NORTH, _box, 5, SpringLayout.NORTH, this);
  }

  private void init() {
    DATA.put(EMPTY_STRING, null);
    DATA.put(HISTOGRAM_STRING, null);
    DATA.put(AUTOCORRELOGRAM_STRING, null);
  }

  public void setDoubleTimeSeries(DoubleTimeSeries ts) {
    _ts = ts;
    DATA.put(HISTOGRAM_STRING, StandardTimeSeriesDistributionAnalysisChart.getChart(_ts));
    DATA.put(AUTOCORRELOGRAM_STRING, StandardAutocorrelogram.getAutocorrelogram(_ts, 20));
  }

  public JComboBox getComboBox() {
    final JComboBox box = new JComboBox(DATA.keySet().toArray());
    box.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String selection = (String) box.getSelectedItem();
        _analysisPlotPanel.removeAll();
        Component[] components = TimeSeriesDemoAnalysisPanel.this.getComponents();
        for (Component component : components) {
          if (component != box) {
            TimeSeriesDemoAnalysisPanel.this.remove(component);
          }
        }
        if (selection != EMPTY_STRING) {
          JFreeChart chart = DATA.get(selection);
          if (chart != null) {
            // TODO JFreeChartPanelProvider
            _analysisPlotPanel.add(new ChartPanel(chart, PLOT_WIDTH, PLOT_HEIGHT, PLOT_WIDTH, PLOT_HEIGHT, PLOT_WIDTH, PLOT_HEIGHT, true, true, false, false, false, false));
            TimeSeriesDemoAnalysisPanel.this.add(_analysisPlotPanel);
            _layout.putConstraint(SpringLayout.EAST, _analysisPlotPanel, -80, SpringLayout.EAST, TimeSeriesDemoAnalysisPanel.this);
            _layout.putConstraint(SpringLayout.NORTH, _analysisPlotPanel, 50, SpringLayout.NORTH, TimeSeriesDemoAnalysisPanel.this);
          }
        }
        repaint();
        revalidate();
      }
    });
    return box;
  }

  @Override
  public Dimension getPreferredSize() {
    return SIZE;
  }
}
