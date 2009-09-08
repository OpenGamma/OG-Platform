package com.opengamma.demo.timeseries;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import com.opengamma.financial.timeseries.DoubleTimeSeries;
import com.opengamma.plot.StandardJFreeChartTimeSeriesChart;

// TODO find some way of removing all references to JFreeChart
public class TimeSeriesDemoSelectionPanel extends JPanel {
  final static int PADDING = 10;
  private static final int TOTAL_WIDTH = 800;
  private static final int PANEL_WIDTH = TOTAL_WIDTH - 2 * PADDING;
  private static final int PLOT_WIDTH = 725;
  private static final int PLOT_HEIGHT = 250;
  private static final int SELECTION_PANEL_HEIGHT = 100;
  private static final int TS_PANEL_HEIGHT = 300;
  private static final int BUTTON_PANEL_HEIGHT = 30;
  private static final Dimension SIZE = new Dimension(TOTAL_WIDTH, SELECTION_PANEL_HEIGHT + TS_PANEL_HEIGHT + BUTTON_PANEL_HEIGHT + 4 * PADDING);
  final static int LABEL_SOUTH_OFFSET = -5;
  final static int SPINNER_HEIGHT = 23;
  final static int SPINNER_WIDTH = 70;
  final JPanel _tsSelectionPanel;
  final JPanel _tsPlotPanel;
  final JPanel _buttonPanel;
  final JFrame _parentFrame;
  final TimeSeriesDemoAnalysisPanel _tsAnalysisPanel;
  final Set<TimeSeriesType> DATA = new TreeSet<TimeSeriesType>();
  final StandardJFreeChartTimeSeriesChart _plotProvider = new StandardJFreeChartTimeSeriesChart();
  final SpringLayout _layout = new SpringLayout();
  final TimeSeriesParameterDialogFactory _dialogFactory;
  final static String EMPTY_STRING = "";
  final static String NORMAL_DISTRIBUTION_STRING = "Normal distribution";
  final static String CAUCHY_DISTRIBUTION_STRING = "Cauchy distribution";
  final static String WEIBULL_DISTRIBUTION_STRING = "Weibull distribution";
  final static String AR_STRING = "Stationary AR(N)";
  final TimeSeriesType EMPTY = new TimeSeriesType(EMPTY_STRING, TimeSeriesType.Type.DATA);
  final TimeSeriesType CAUCHY_DISTRIBUTION = new TimeSeriesType(CAUCHY_DISTRIBUTION_STRING, TimeSeriesType.Type.STATISTICAL);
  final TimeSeriesType NORMAL_DISTRIBUTION = new TimeSeriesType(NORMAL_DISTRIBUTION_STRING, TimeSeriesType.Type.STATISTICAL);
  final TimeSeriesType WEIBULL_DISTRIBUTION = new TimeSeriesType(WEIBULL_DISTRIBUTION_STRING, TimeSeriesType.Type.STATISTICAL);
  final TimeSeriesType AUTOREGRESSIVE = new TimeSeriesType(AR_STRING, TimeSeriesType.Type.GENERATED);
  DoubleTimeSeries _ts;

  public TimeSeriesDemoSelectionPanel(JFrame parentFrame, TimeSeriesDemoAnalysisPanel tsAnalysisPanel) {
    init();
    _parentFrame = parentFrame;
    _tsAnalysisPanel = tsAnalysisPanel;
    _tsSelectionPanel = getTimeSeriesSelectionPanel();
    _dialogFactory = new TimeSeriesParameterDialogFactory();
    _tsPlotPanel = new JPanel();
    _buttonPanel = getButtonPanel();
    _tsSelectionPanel.setPreferredSize(new Dimension(PANEL_WIDTH, SELECTION_PANEL_HEIGHT));
    _tsPlotPanel.setPreferredSize(new Dimension(PANEL_WIDTH, TS_PANEL_HEIGHT));
    _tsPlotPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
    _buttonPanel.setPreferredSize(new Dimension(PANEL_WIDTH, BUTTON_PANEL_HEIGHT));
    add(_tsSelectionPanel);
    add(_tsPlotPanel);
    add(_buttonPanel);
    setLayout(_layout);
    setPanelLayout();
  }

  private void setPanelLayout() {
    _layout.putConstraint(SpringLayout.WEST, _tsSelectionPanel, PADDING, SpringLayout.WEST, this);
    _layout.putConstraint(SpringLayout.NORTH, _tsSelectionPanel, PADDING, SpringLayout.NORTH, this);
    _layout.putConstraint(SpringLayout.WEST, _tsPlotPanel, PADDING, SpringLayout.WEST, this);
    _layout.putConstraint(SpringLayout.NORTH, _tsPlotPanel, PADDING, SpringLayout.SOUTH, _tsSelectionPanel);
    _layout.putConstraint(SpringLayout.WEST, _buttonPanel, PADDING, SpringLayout.WEST, this);
    _layout.putConstraint(SpringLayout.NORTH, _buttonPanel, PADDING, SpringLayout.SOUTH, _tsPlotPanel);
    _layout.putConstraint(SpringLayout.EAST, this, PADDING, SpringLayout.EAST, _buttonPanel);
    _layout.putConstraint(SpringLayout.SOUTH, this, PADDING, SpringLayout.SOUTH, _buttonPanel);
  }

  private void init() {
    DATA.add(CAUCHY_DISTRIBUTION);
    DATA.add(NORMAL_DISTRIBUTION);
    DATA.add(WEIBULL_DISTRIBUTION);
    DATA.add(AUTOREGRESSIVE);
    DATA.add(EMPTY);
  }

  @Override
  public Dimension getPreferredSize() {
    return SIZE;
  }

  public DoubleTimeSeries getDoubleTimeSeries() {
    return _ts;
  }

  private JPanel getTimeSeriesSelectionPanel() {
    JPanel panel = new JPanel();
    SpringLayout myLayout = new SpringLayout();
    panel.setLayout(myLayout);
    JLabel comboBoxLabel = new JLabel("Time series type:");
    JComboBox tsTypeComboBox = new JComboBox(DATA.toArray());
    JLabel numberLabel = new JLabel("Number of data points:");
    SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1000, 100, 10000, 100);
    JSpinner numberSpinner = new JSpinner(spinnerModel);
    JButton button = new JButton("Generate Time Series");
    numberSpinner.setEnabled(false);
    button.setEnabled(false);
    numberLabel.setForeground(Color.lightGray);
    button.addActionListener(new TSSelectionPanelButtonActionListener(tsTypeComboBox, numberSpinner));
    tsTypeComboBox.addActionListener(new ComboBoxListener(numberLabel, numberSpinner, button));
    panel.add(comboBoxLabel, 0);
    panel.add(tsTypeComboBox, 1);
    panel.add(numberLabel, 2);
    panel.add(numberSpinner, 3);
    panel.add(button, 4);
    numberSpinner.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
    myLayout.putConstraint(SpringLayout.WEST, tsTypeComboBox, 180, SpringLayout.WEST, panel);
    myLayout.putConstraint(SpringLayout.NORTH, tsTypeComboBox, PADDING, SpringLayout.NORTH, panel);
    myLayout.putConstraint(SpringLayout.EAST, comboBoxLabel, -PADDING, SpringLayout.WEST, tsTypeComboBox);
    myLayout.putConstraint(SpringLayout.SOUTH, comboBoxLabel, LABEL_SOUTH_OFFSET, SpringLayout.SOUTH, tsTypeComboBox);
    myLayout.putConstraint(SpringLayout.WEST, numberSpinner, 180, SpringLayout.WEST, panel);
    myLayout.putConstraint(SpringLayout.NORTH, numberSpinner, PADDING, SpringLayout.SOUTH, tsTypeComboBox);
    myLayout.putConstraint(SpringLayout.EAST, numberLabel, -PADDING, SpringLayout.WEST, numberSpinner);
    myLayout.putConstraint(SpringLayout.SOUTH, numberLabel, LABEL_SOUTH_OFFSET, SpringLayout.SOUTH, numberSpinner);
    myLayout.putConstraint(SpringLayout.WEST, button, 180, SpringLayout.WEST, panel);
    myLayout.putConstraint(SpringLayout.NORTH, button, PADDING, SpringLayout.SOUTH, numberSpinner);
    return panel;
  }

  private JPanel getButtonPanel() {
    JPanel panel = new JPanel();
    SpringLayout layout = new SpringLayout();
    panel.setLayout(layout);
    Dimension size = new Dimension(100, 25);
    JButton resetButton = new JButton("Reset");
    JButton closeButton = new JButton("Close");
    resetButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        _tsPlotPanel.removeAll();
        Component[] components = _tsSelectionPanel.getComponents();
        for (Component component : components) {
          if (component instanceof JComboBox) {
            JComboBox box = (JComboBox) component;
            box.setSelectedItem(EMPTY);
            _dialogFactory.initialiseGenerators();
          }
        }
        revalidate();
        repaint();
      }
    });
    closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    resetButton.setPreferredSize(size);
    closeButton.setPreferredSize(size);
    layout.putConstraint(SpringLayout.EAST, closeButton, -PADDING, SpringLayout.EAST, panel);
    layout.putConstraint(SpringLayout.NORTH, closeButton, PADDING, SpringLayout.NORTH, panel);
    layout.putConstraint(SpringLayout.EAST, resetButton, -PADDING, SpringLayout.WEST, closeButton);
    layout.putConstraint(SpringLayout.NORTH, resetButton, PADDING, SpringLayout.NORTH, panel);
    panel.add(resetButton);
    panel.add(closeButton);
    return panel;
  }

  private class ComboBoxListener implements ActionListener {
    private JLabel _label;
    private JSpinner _spinner;
    private JButton _button;
    private int PANEL_INDEX = 5;

    public ComboBoxListener(JLabel label, JSpinner spinner, JButton button) {
      _label = label;
      _spinner = spinner;
      _button = button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JComboBox box = (JComboBox) e.getSource();
      TimeSeriesType type = (TimeSeriesType) box.getSelectedItem();
      if (_tsSelectionPanel.getComponentCount() > 5) {
        _tsSelectionPanel.remove(PANEL_INDEX);
      }
      if (type.equals(EMPTY)) {
        _label.repaint();
        _spinner.setEnabled(false);
        _button.setEnabled(false);
        _label.setForeground(Color.lightGray);
        _tsPlotPanel.removeAll();
      } else {
        _label.setEnabled(true);
        _spinner.setEnabled(true);
        _button.setEnabled(true);
        _label.setForeground(Color.black);
        JDialog dialog = _dialogFactory.getDialog(type, _parentFrame, (Integer) _spinner.getValue());
        dialog.setVisible(true);
      }
      revalidate();
      repaint();
    }
  }

  private class TSSelectionPanelButtonActionListener implements ActionListener {
    private JComboBox _box;
    private JSpinner _spinner;

    public TSSelectionPanelButtonActionListener(JComboBox box, JSpinner spinner) {
      _box = box;
      _spinner = spinner;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      TimeSeriesType selection = (TimeSeriesType) _box.getSelectedItem();
      int n = (Integer) _spinner.getValue();
      TimeSeriesDemoData data = _dialogFactory.getDialog(selection, _parentFrame, n).getDataGenerator();
      _tsPlotPanel.removeAll();
      if (!selection.equals(EMPTY)) {
        _ts = data.getTimeSeries(n);
        _tsAnalysisPanel.setDoubleTimeSeries(_ts);
        JFreeChart newChart = _plotProvider.getPlot(_ts, "", "Date", "Return", false, false, false);
        if (_tsPlotPanel.getComponentCount() == 0) {
          _tsPlotPanel.add(new ChartPanel(newChart, PLOT_WIDTH, PLOT_HEIGHT, PLOT_WIDTH, PLOT_HEIGHT, PLOT_WIDTH, PLOT_HEIGHT, true, true, false, false, false, false), 0);
        } else {
          ChartPanel chart = ((ChartPanel) (_tsPlotPanel.getComponents()[0]));
          chart.setChart(newChart);
        }
      }
      revalidate();
      repaint();
    }
  }

}
