package com.opengamma.demo.timeseries;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.opengamma.util.KeyValuePair;

public class TimeSeriesParameterDialogFactory {
  final static int LABEL_SOUTH_OFFSET = -5;
  final static int SPINNER_HEIGHT = 23;
  final static int SPINNER_WIDTH = 70;
  final static int PADDING = 5;
  private TimeSeriesGeneratorDialog _normalDialog;
  private TimeSeriesGeneratorDialog _cauchyDialog;
  private TimeSeriesGeneratorDialog _weibullDialog;
  private TimeSeriesGeneratorDialog _arDialog;

  public void initialiseGenerators() {
    if (_normalDialog != null) _normalDialog.setDefaultParameters();
    if (_cauchyDialog != null) _cauchyDialog.setDefaultParameters();
    if (_weibullDialog != null) _weibullDialog.setDefaultParameters();
    if (_arDialog != null) _arDialog.setDefaultParameters();
  }

  public TimeSeriesGeneratorDialog getDialog(TimeSeriesType type, JFrame parentFrame, int n) {
    if (_normalDialog == null) _normalDialog = new NormalDistributionDialog(parentFrame, n);
    if (_cauchyDialog == null) _cauchyDialog = new CauchyDistributionDialog(parentFrame, n);
    if (_weibullDialog == null) _weibullDialog = new WeibullDistributionDialog(parentFrame, n);
    if (_arDialog == null) _arDialog = new AutoregressiveDistributionDialog(parentFrame, n);
    if (type.getLabel().equals(TimeSeriesDemoSelectionPanel.NORMAL_DISTRIBUTION_STRING)) return _normalDialog;
    if (type.getLabel().equals(TimeSeriesDemoSelectionPanel.CAUCHY_DISTRIBUTION_STRING)) return _cauchyDialog;
    if (type.getLabel().equals(TimeSeriesDemoSelectionPanel.WEIBULL_DISTRIBUTION_STRING)) return _weibullDialog;
    if (type.getLabel().equals(TimeSeriesDemoSelectionPanel.AR_STRING)) return _arDialog;
    return null;
  }

  abstract class TimeSeriesGeneratorDialog extends JDialog {
    final int _n;

    public TimeSeriesGeneratorDialog(JFrame parentFrame, int n) {
      super(parentFrame, "Model parameters", true);
      _n = n;
    }

    protected void createDialog() {
      setLayout(new BorderLayout());
      JPanel parameterPanel = getParameterPanel();
      JPanel buttonPanel = getButtonPanel();
      add(parameterPanel, BorderLayout.CENTER);
      add(buttonPanel, BorderLayout.PAGE_END);
      pack();
    }

    protected void repaintDialog() {
      pack();
      repaint();
    }

    private JPanel getButtonPanel() {
      JPanel panel = new JPanel();
      SpringLayout layout = new SpringLayout();
      panel.setLayout(layout);
      JButton finished = new JButton("Finished");
      finished.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          setGeneratorParameters();
          TimeSeriesGeneratorDialog.this.setVisible(false);
        }
      });
      JButton reset = new JButton("Reset");
      reset.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          setDefaultParameters();
        }
      });
      panel.add(finished);
      panel.add(reset);
      layout.putConstraint(SpringLayout.WEST, reset, PADDING, SpringLayout.WEST, panel);
      layout.putConstraint(SpringLayout.NORTH, reset, PADDING, SpringLayout.NORTH, panel);
      layout.putConstraint(SpringLayout.WEST, finished, PADDING, SpringLayout.EAST, reset);
      layout.putConstraint(SpringLayout.NORTH, finished, PADDING, SpringLayout.NORTH, panel);
      layout.putConstraint(SpringLayout.EAST, panel, PADDING, SpringLayout.EAST, finished);
      layout.putConstraint(SpringLayout.SOUTH, panel, PADDING, SpringLayout.SOUTH, finished);
      return panel;
    }

    public abstract TimeSeriesDemoData getDataGenerator();

    protected abstract JPanel getParameterPanel();

    protected abstract void setGeneratorParameters();

    protected abstract void setDefaultParameters();
  }

  class CauchyDistributionDialog extends TimeSeriesGeneratorDialog {
    private final JLabel _label = new JLabel("Cut-off");
    private final JSpinner _spinner = new JSpinner(new SpinnerNumberModel(7, 0.5, 20, 0.5));
    private final SpringLayout _layout = new SpringLayout();
    private final JPanel _panel;
    private final int LABEL_WIDTH = 50;
    private final Dimension _dimension = new Dimension(LABEL_WIDTH + SPINNER_WIDTH + 4 * PADDING, 2 * SPINNER_HEIGHT + 3 * PADDING);
    private TimeSeriesDemoData _generator;

    public CauchyDistributionDialog(JFrame parentFrame, int n) {
      super(parentFrame, n);
      _panel = new JPanel();
      _panel.setLayout(_layout);
      _panel.add(_label);
      _panel.add(_spinner);
      _panel.setPreferredSize(_dimension);
      setLocation(parentFrame.getX() + (int) ((parentFrame.getWidth() - _dimension.getWidth()) / 2), parentFrame.getY() + 100);
      _spinner.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
      _layout.putConstraint(SpringLayout.EAST, _spinner, -PADDING, SpringLayout.EAST, _panel);
      _layout.putConstraint(SpringLayout.NORTH, _spinner, PADDING, SpringLayout.NORTH, _panel);
      _layout.putConstraint(SpringLayout.EAST, _label, -PADDING, SpringLayout.WEST, _spinner);
      _layout.putConstraint(SpringLayout.SOUTH, _label, LABEL_SOUTH_OFFSET, SpringLayout.SOUTH, _spinner);
      createDialog();
    }

    @Override
    public TimeSeriesDemoData getDataGenerator() {
      return _generator;
    }

    private double getCutoff() {
      return (Double) _spinner.getValue();
    }

    @Override
    protected JPanel getParameterPanel() {
      return _panel;
    }

    @Override
    protected void setDefaultParameters() {
      _generator = new CauchyDistributionTimeSeriesData(7.);
      _spinner.setValue(7.);
      repaint();
    }

    @Override
    protected void setGeneratorParameters() {
      _generator = new CauchyDistributionTimeSeriesData(getCutoff());
    }
  }

  class NormalDistributionDialog extends TimeSeriesGeneratorDialog {
    private final JLabel _meanLabel = new JLabel("Mean:");
    private final JSpinner _meanSpinner = new JSpinner(new SpinnerNumberModel(0., -100., 100., 0.1));
    private final JLabel _standardDeviationLabel = new JLabel("Standard deviation:");
    private final JSpinner _standardDeviationSpinner = new JSpinner(new SpinnerNumberModel(1., 0.01, 10, 0.01));
    private final JPanel _panel;
    private final SpringLayout _layout = new SpringLayout();
    private final Dimension _dimension = new Dimension(SPINNER_WIDTH + 4 * PADDING + 150, 2 * SPINNER_HEIGHT + 3 * PADDING);
    private TimeSeriesDemoData _generator;

    public NormalDistributionDialog(JFrame parentFrame, int n) {
      super(parentFrame, n);
      _panel = new JPanel();
      _panel.setLayout(_layout);
      _meanSpinner.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
      _standardDeviationSpinner.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
      _panel.add(_meanLabel);
      _panel.add(_meanSpinner);
      _panel.add(_standardDeviationLabel);
      _panel.add(_standardDeviationSpinner);
      _panel.setPreferredSize(_dimension);
      setLocation(parentFrame.getX() + (int) ((parentFrame.getWidth() - _dimension.getWidth()) / 2), parentFrame.getY() + 100);
      _layout.putConstraint(SpringLayout.EAST, _meanSpinner, -PADDING, SpringLayout.EAST, _panel);
      _layout.putConstraint(SpringLayout.NORTH, _meanSpinner, PADDING, SpringLayout.NORTH, _panel);
      _layout.putConstraint(SpringLayout.EAST, _meanLabel, -PADDING, SpringLayout.WEST, _meanSpinner);
      _layout.putConstraint(SpringLayout.SOUTH, _meanLabel, LABEL_SOUTH_OFFSET, SpringLayout.SOUTH, _meanSpinner);
      _layout.putConstraint(SpringLayout.WEST, _standardDeviationSpinner, 0, SpringLayout.WEST, _meanSpinner);
      _layout.putConstraint(SpringLayout.NORTH, _standardDeviationSpinner, PADDING, SpringLayout.SOUTH, _meanSpinner);
      _layout.putConstraint(SpringLayout.EAST, _standardDeviationLabel, -PADDING, SpringLayout.WEST, _standardDeviationSpinner);
      _layout.putConstraint(SpringLayout.SOUTH, _standardDeviationLabel, LABEL_SOUTH_OFFSET, SpringLayout.SOUTH, _standardDeviationSpinner);
      createDialog();
    }

    @Override
    public TimeSeriesDemoData getDataGenerator() {
      return _generator;
    }

    private double getMean() {
      return (Double) _meanSpinner.getValue();
    }

    private double getStandardDeviation() {
      return (Double) _standardDeviationSpinner.getValue();
    }

    @Override
    protected JPanel getParameterPanel() {
      return _panel;
    }

    @Override
    protected void setDefaultParameters() {
      _generator = new NormalDistributionTimeSeriesData(0., 1.);
      _meanSpinner.setValue(0.);
      _standardDeviationSpinner.setValue(1.);
      repaint();
    }

    @Override
    protected void setGeneratorParameters() {
      _generator = new NormalDistributionTimeSeriesData(getMean(), getStandardDeviation());
    }
  }

  class WeibullDistributionDialog extends TimeSeriesGeneratorDialog {
    private final JLabel _alphaLabel = new JLabel("Alpha:");
    private final JSpinner _alphaSpinner = new JSpinner(new SpinnerNumberModel(5., 0., 30., 1));
    private final JLabel _betaLabel = new JLabel("Beta:");
    private final JSpinner _betaSpinner = new JSpinner(new SpinnerNumberModel(10., 0., 40., 1));
    private final JPanel _panel;
    private final SpringLayout _layout = new SpringLayout();
    private final Dimension _dimension = new Dimension(SPINNER_WIDTH + 100 + 4 * PADDING, SPINNER_HEIGHT * 2 + 3 * PADDING);
    private TimeSeriesDemoData _generator;

    public WeibullDistributionDialog(JFrame parentFrame, int n) {
      super(parentFrame, n);
      _panel = new JPanel();
      _alphaSpinner.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
      _betaSpinner.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
      _panel.setLayout(_layout);
      _panel.add(_alphaLabel);
      _panel.add(_alphaSpinner);
      _panel.add(_betaLabel);
      _panel.add(_betaSpinner);
      _panel.setPreferredSize(_dimension);
      setLocation(parentFrame.getX() + (int) ((parentFrame.getWidth() - _dimension.getWidth()) / 2), parentFrame.getY() + 100);
      _layout.putConstraint(SpringLayout.EAST, _alphaSpinner, -PADDING, SpringLayout.EAST, _panel);
      _layout.putConstraint(SpringLayout.NORTH, _alphaSpinner, PADDING, SpringLayout.NORTH, _panel);
      _layout.putConstraint(SpringLayout.EAST, _alphaLabel, -PADDING, SpringLayout.WEST, _alphaSpinner);
      _layout.putConstraint(SpringLayout.SOUTH, _alphaLabel, LABEL_SOUTH_OFFSET, SpringLayout.SOUTH, _alphaSpinner);
      _layout.putConstraint(SpringLayout.WEST, _betaSpinner, 0, SpringLayout.WEST, _alphaSpinner);
      _layout.putConstraint(SpringLayout.NORTH, _betaSpinner, PADDING, SpringLayout.SOUTH, _alphaSpinner);
      _layout.putConstraint(SpringLayout.EAST, _betaLabel, -PADDING, SpringLayout.WEST, _betaSpinner);
      _layout.putConstraint(SpringLayout.SOUTH, _betaLabel, LABEL_SOUTH_OFFSET, SpringLayout.SOUTH, _betaSpinner);
      createDialog();
    }

    @Override
    public TimeSeriesDemoData getDataGenerator() {
      return _generator;
    }

    private double getAlpha() {
      return (Double) _alphaSpinner.getValue();
    }

    private double getBeta() {
      return (Double) _betaSpinner.getValue();
    }

    @Override
    protected JPanel getParameterPanel() {
      return _panel;
    }

    @Override
    protected void setDefaultParameters() {
      _generator = new WeibullDistributionTimeSeriesData(5., 10.);
      _alphaSpinner.setValue(5.);
      _betaSpinner.setValue(10.);
      repaint();
    }

    @Override
    protected void setGeneratorParameters() {
      _generator = new WeibullDistributionTimeSeriesData(getAlpha(), getBeta());
    }
  }

  class AutoregressiveDistributionDialog extends TimeSeriesGeneratorDialog {
    private final JLabel _label = new JLabel("Order");
    private final SpringLayout _spinnerLayout = new SpringLayout();
    private Dimension _dimension = new Dimension(200, 90);// TODO
    private TimeSeriesDemoData _generator;
    private List<KeyValuePair<JLabel, JTextField>> _pairs;
    private final Dimension _fieldSize = new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT);
    final JSpinner _spinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    final JPanel _panel;
    final JPanel _spinnerPanel = new JPanel();
    JPanel _coefficientPanel;
    SpringLayout _layout = new SpringLayout();

    public AutoregressiveDistributionDialog(JFrame parentFrame, int n) {
      super(parentFrame, n);
      _panel = new JPanel();
      _panel.setLayout(_layout);
      _coefficientPanel = getCoefficientPanel((Integer) _spinner.getValue());
      _spinner.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
      _spinner.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          _panel.removeAll();
          _layout = new SpringLayout();
          _panel.setLayout(_layout);
          setPanelLayout();
          _panel.setPreferredSize(new Dimension(200, (Integer) _spinner.getValue() * (SPINNER_HEIGHT + PADDING) + 2 * PADDING + 50));
          _panel.repaint();
          _panel.revalidate();
          repaintDialog();
        }
      });
      _spinnerPanel.setLayout(_spinnerLayout);
      _spinnerPanel.add(_label);
      _spinnerPanel.add(_spinner);
      _spinnerLayout.putConstraint(SpringLayout.EAST, _spinner, -PADDING, SpringLayout.EAST, _spinnerPanel);
      _spinnerLayout.putConstraint(SpringLayout.NORTH, _spinner, PADDING, SpringLayout.NORTH, _spinnerPanel);
      _spinnerLayout.putConstraint(SpringLayout.EAST, _label, -PADDING, SpringLayout.WEST, _spinner);
      _spinnerLayout.putConstraint(SpringLayout.SOUTH, _label, LABEL_SOUTH_OFFSET, SpringLayout.SOUTH, _spinner);
      _spinnerPanel.setPreferredSize(new Dimension(200, 50));
      setPanelLayout();
      _panel.setPreferredSize(_dimension);
      setLocation(parentFrame.getX() + (int) ((parentFrame.getWidth() - _dimension.getWidth()) / 2), parentFrame.getY() + 100);
      createDialog();
    }

    void setPanelLayout() {
      _panel.add(_spinnerPanel);
      _layout.putConstraint(SpringLayout.EAST, _spinnerPanel, -PADDING, SpringLayout.EAST, _panel);
      _layout.putConstraint(SpringLayout.NORTH, _spinnerPanel, PADDING, SpringLayout.NORTH, _panel);
      _layout.putConstraint(SpringLayout.EAST, _coefficientPanel, -PADDING, SpringLayout.EAST, _panel);
      _layout.putConstraint(SpringLayout.NORTH, _coefficientPanel, PADDING, SpringLayout.SOUTH, _spinnerPanel);
      _coefficientPanel = getCoefficientPanel((Integer) _spinner.getValue());
      _panel.add(_coefficientPanel);
      _layout.putConstraint(SpringLayout.EAST, _coefficientPanel, -PADDING, SpringLayout.EAST, _panel);
      _layout.putConstraint(SpringLayout.NORTH, _coefficientPanel, PADDING, SpringLayout.SOUTH, _spinnerPanel);
    }

    JPanel getCoefficientPanel(int order) {
      _pairs = new ArrayList<KeyValuePair<JLabel, JTextField>>();
      JPanel panel = new JPanel();
      SpringLayout layout = new SpringLayout();
      panel.setLayout(layout);
      FocusListener listener = new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
          return;
        }

        @Override
        public void focusLost(FocusEvent e) {
          JTextField field = (JTextField) e.getSource();
          try {
            Double.parseDouble(field.getText());
          } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(AutoregressiveDistributionDialog.this, "Coefficient must be a number");
            field.setSelectionStart(0);
            field.setSelectionEnd(field.getText().length());
          }
        }
      };
      for (int i = 0; i < order; i++) {
        JLabel label = new JLabel("Phi" + i);// TODO subscript
        JTextField field = new JTextField("0.5", 5);
        field.setPreferredSize(_fieldSize);
        field.addFocusListener(listener);
        _pairs.add(new KeyValuePair<JLabel, JTextField>(label, field));
        panel.add(label);
        panel.add(field);
      }
      KeyValuePair<JLabel, JTextField> firstPair = _pairs.get(0);
      JTextField firstField = firstPair.getValue();
      JLabel firstLabel = firstPair.getKey();
      layout.putConstraint(SpringLayout.EAST, firstField, -PADDING, SpringLayout.EAST, panel);
      layout.putConstraint(SpringLayout.NORTH, firstField, PADDING, SpringLayout.NORTH, panel);
      layout.putConstraint(SpringLayout.EAST, firstLabel, -PADDING, SpringLayout.WEST, firstField);
      layout.putConstraint(SpringLayout.SOUTH, firstLabel, LABEL_SOUTH_OFFSET, SpringLayout.SOUTH, firstField);
      for (int i = 1; i < order; i++) {
        KeyValuePair<JLabel, JTextField> pair = _pairs.get(i);
        JLabel label = pair.getKey();
        JTextField field = pair.getValue();
        JTextField previousField = _pairs.get(i - 1).getValue();
        layout.putConstraint(SpringLayout.EAST, field, -PADDING, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, field, PADDING, SpringLayout.SOUTH, previousField);
        layout.putConstraint(SpringLayout.EAST, label, -PADDING, SpringLayout.WEST, field);
        layout.putConstraint(SpringLayout.SOUTH, label, LABEL_SOUTH_OFFSET, SpringLayout.SOUTH, field);
      }
      panel.setPreferredSize(new Dimension(200, 40 * order));
      return panel;
    }

    @Override
    public TimeSeriesDemoData getDataGenerator() {
      return _generator;
    }

    private int getOrder() {
      return (Integer) _spinner.getValue();
    }

    @Override
    protected JPanel getParameterPanel() {
      return _panel;
    }

    @Override
    protected void setDefaultParameters() {
      _spinner.setValue(1);
      List<Double> defaultCoefficients = new ArrayList<Double>();
      defaultCoefficients.add(0.5);
      int order = getOrder();
      for (int i = 1; i < order; i++) {
        defaultCoefficients.add(0.);
      }
      _generator = new AutoregressiveTimeSeriesData(order, _n, defaultCoefficients);
      repaint();
    }

    @Override
    protected void setGeneratorParameters() {
      List<Double> coefficients = new ArrayList<Double>();
      int order = getOrder();
      double sum = 0;
      for (int i = 0; i < order; i++) {
        double coeff = Double.parseDouble(_pairs.get(i).getValue().getText());
        sum += coeff;
        coefficients.add(coeff);
      }
      if (sum >= 1) {
        Object[] options = { "Yes", "Retry" };
        int option = JOptionPane.showOptionDialog(this, "Sum of coefficients is greater than 1: \nthis will give a non-stationary series. \nContinue anyway?", "",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
        if (option == JOptionPane.YES_OPTION) {
          AutoregressiveDistributionDialog.this.setVisible(false);
        } else {
          // TODO this doesn't work
          createDialog();
        }
      }
      _generator = new AutoregressiveTimeSeriesData(getOrder(), _n, coefficients);
    }
  }
}
