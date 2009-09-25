/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.ChannelInput;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.lang.ProcessManager;
import org.jcsp.util.OverWriteOldestBuffer;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXTable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.analytics.AbstractAnalyticValue;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction;
import com.opengamma.engine.analytics.InMemoryAnalyticFunctionRepository;
import com.opengamma.engine.analytics.ResolveSecurityKeyToMarketDataHeaderDefinition;
import com.opengamma.engine.analytics.VolatilitySurfaceValueDefinition;
import com.opengamma.engine.analytics.yc.DiscountCurveAnalyticFunction;
import com.opengamma.engine.analytics.yc.DiscountCurveDefinition;
import com.opengamma.engine.analytics.yc.FixedIncomeStrip;
import com.opengamma.engine.livedata.FixedLiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.position.csv.CSVPositionMaster;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.EquitySecurity;
import com.opengamma.engine.security.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.engine.security.InMemorySecurityMaster;
import com.opengamma.engine.security.OptionType;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityIdentificationDomain;
import com.opengamma.engine.security.SecurityIdentifier;
import com.opengamma.engine.security.SecurityKey;
import com.opengamma.engine.security.SecurityKeyImpl;
import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.MapViewComputationCacheSource;
import com.opengamma.engine.view.ViewComputationCacheSource;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinitionImpl;
import com.opengamma.engine.view.ViewImpl;
import com.opengamma.engine.view.ViewProcessingContext;
import com.opengamma.engine.view.calcnode.LinkedBlockingCompletionQueue;
import com.opengamma.engine.view.calcnode.LinkedBlockingJobQueue;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.securities.Currency;
import com.opengamma.util.KeyValuePair;
import com.opengamma.util.Pair;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author jim
 */
public class ViewerLauncher extends SingleFrameApplication {
  private final Clock _clock = Clock.system(TimeZone.UTC);
  private static final double ONEYEAR = 365.25;
  private static final SecurityIdentificationDomain BLOOMBERG = new SecurityIdentificationDomain("BLOOMBERG");
  private ViewImpl _view;
  private SnapshotPopulatorJob _popJob;
  

  protected ViewImpl constructTrivialExampleView() throws Exception {
    ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("Kirk", "KirkPortfolio");
    //viewDefinition.addValueDefinition("EQUITY_OPTION", HardCodedUSDDiscountCurveAnalyticFunction.getDiscountCurveValueDefinition());
    viewDefinition.addValueDefinition("EQUITY_OPTION", new VolatilitySurfaceValueDefinition(null));
    final Portfolio portfolio = CSVPositionMaster.loadPortfolio("KirkPortfolio", getClass().getResourceAsStream("KirkPortfolio.txt"));
    PositionMaster positionMaster = new PositionMaster() {
      @Override
      public Portfolio getRootPortfolio(String portfolioName) {
        return portfolio;
      }

      @Override
      public Collection<String> getRootPortfolioNames() {
        return Collections.singleton(portfolio.getName());
      }
    };
    InMemorySecurityMaster secMaster = new InMemorySecurityMaster();
    List<Security> securities = new ArrayList<Security>();
    String[] tickers = new String[] {"APVJS.X", "APVJN.X", "AJLJV.X"};
    double[] strikes = new double[] {195.0, 170.0, 210.0 };
    Expiry expiry = new Expiry(_clock.zonedDateTime().withDate(2009, 10, 16).withTime(17, 00));
    Security aapl = new EquitySecurity("AAPL", "BLOOMBERG");
    secMaster.add(aapl);
    
    for (int i=0; i<tickers.length; i++) {
      DefaultSecurity security = new EuropeanVanillaEquityOptionSecurity(OptionType.CALL, strikes[i], expiry, aapl.getIndentityKey(), Currency.getInstance("USD"));
      security.setIdentifiers(Collections.singleton(new SecurityIdentifier(new SecurityIdentificationDomain("BLOOMBERG"), tickers[i])));
      securities.add(security);
      secMaster.add(security);
    }
    
    DiscountCurveDefinition curveDefinition = constructDiscountCurveDefinition("USD", "Stupidly Lame");
    DiscountCurveAnalyticFunction function = new DiscountCurveAnalyticFunction(curveDefinition);
    HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction function2 = new HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction();
    InMemoryAnalyticFunctionRepository functionRepo = new InMemoryAnalyticFunctionRepository();
    functionRepo.addFunction(function, function);
    functionRepo.addFunction(function2, function2);
    
    FixedLiveDataAvailabilityProvider ldap = new FixedLiveDataAvailabilityProvider();
    ldap.addDefinition(new ResolveSecurityKeyToMarketDataHeaderDefinition(aapl.getIndentityKey()));
    for(Security security : securities) {
      for(AnalyticValueDefinition<?> definition : function.getInputs(security)) {
        ldap.addDefinition(definition);
      }
      for(AnalyticValueDefinition<?> definition : function2.getInputs(security)) {
        if (!definition.getValue("TYPE").equals("DISCOUNT_CURVE")) { // skip derived data.
          ldap.addDefinition(definition);
        }
      }
    }
    
    ViewComputationCacheSource cacheFactory = new MapViewComputationCacheSource();
    
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    populateSnapshot(snapshotProvider, curveDefinition, false);
    
    LinkedBlockingJobQueue jobQueue = new LinkedBlockingJobQueue();
    LinkedBlockingCompletionQueue completionQueue = new LinkedBlockingCompletionQueue();
    
    ViewProcessingContext processingContext = new ViewProcessingContext(
        ldap, snapshotProvider, functionRepo, positionMaster, secMaster, cacheFactory,
        jobQueue, completionQueue
      );
    
    ViewImpl view = new ViewImpl(viewDefinition, processingContext);
    view.setComputationExecutorService(Executors.newSingleThreadExecutor());
    
    return view;
  }
  
  public static AnalyticValueDefinition<?> constructBloombergTickerDefinition(String bbTicker) {
    ResolveSecurityKeyToMarketDataHeaderDefinition definition =
      new ResolveSecurityKeyToMarketDataHeaderDefinition(
          new SecurityKeyImpl(new SecurityIdentifier(BLOOMBERG, bbTicker)));
    return definition;
  }
  
  public static DiscountCurveDefinition constructDiscountCurveDefinition(String isoCode, String name) {
    DiscountCurveDefinition defn = new DiscountCurveDefinition(Currency.getInstance(isoCode), name);
    defn.addStrip(new FixedIncomeStrip(1/ONEYEAR, constructBloombergTickerDefinition("US1D")));
    defn.addStrip(new FixedIncomeStrip(2/ONEYEAR, constructBloombergTickerDefinition("US2D")));
    defn.addStrip(new FixedIncomeStrip(7/ONEYEAR, constructBloombergTickerDefinition("US7D")));
    defn.addStrip(new FixedIncomeStrip(1/12.0, constructBloombergTickerDefinition("US1M")));
    defn.addStrip(new FixedIncomeStrip(0.25, constructBloombergTickerDefinition("US3M")));
    defn.addStrip(new FixedIncomeStrip(0.5, constructBloombergTickerDefinition("US6M")));

    defn.addStrip(new FixedIncomeStrip(1.0, constructBloombergTickerDefinition("USSW1")));
    defn.addStrip(new FixedIncomeStrip(2.0, constructBloombergTickerDefinition("USSW2")));
    defn.addStrip(new FixedIncomeStrip(3.0, constructBloombergTickerDefinition("USSW3")));
    defn.addStrip(new FixedIncomeStrip(4.0, constructBloombergTickerDefinition("USSW4")));
    defn.addStrip(new FixedIncomeStrip(5.0, constructBloombergTickerDefinition("USSW5")));
    defn.addStrip(new FixedIncomeStrip(6.0, constructBloombergTickerDefinition("USSW6")));
    defn.addStrip(new FixedIncomeStrip(7.0, constructBloombergTickerDefinition("USSW7")));
    defn.addStrip(new FixedIncomeStrip(8.0, constructBloombergTickerDefinition("USSW8")));
    defn.addStrip(new FixedIncomeStrip(9.0, constructBloombergTickerDefinition("USSW9")));
    defn.addStrip(new FixedIncomeStrip(10.0, constructBloombergTickerDefinition("USSW10")));
    return defn;
  }

  
  /**
   * @param function 
   * @param snapshotProvider 
   * 
   */
  @SuppressWarnings("unchecked")
  private void populateSnapshot(
      InMemoryLKVSnapshotProvider snapshotProvider,
      DiscountCurveDefinition curveDefinition,
      boolean addRandom) {
    // Inflection point is 10.
    double currValue = 0.005;
    for(FixedIncomeStrip strip : curveDefinition.getStrips()) {
      if(addRandom) {
        currValue += (Math.random() * 0.010);
      }
      final Map<String, Double> dataFields = new HashMap<String, Double>();
      dataFields.put(DiscountCurveAnalyticFunction.PRICE_FIELD_NAME, currValue);
      
      AnalyticValue value = new AbstractAnalyticValue(strip.getStripValueDefinition(), dataFields) {
        @Override
        public AnalyticValue<Map<String, Double>> scaleForPosition(BigDecimal quantity) {
          return this;
        }
      };
      snapshotProvider.addValue(value);
      if(strip.getNumYears() <= 5.0) {
        currValue += 0.005;
      } else {
        currValue -= 0.001;
      }
    }
    populateOptions(snapshotProvider, addRandom);
  }
  
  private SecurityKey makeSecurityKey(String ticker) {
    return new SecurityKeyImpl(new SecurityIdentifier(new SecurityIdentificationDomain("BLOOMBERG"), ticker));
  }
  
  private AnalyticValue<Map<String, Double>> makeHeaderValue(AnalyticValueDefinition<Map<String, Double>> def, String field, Double value) {
    Map<String, Double> map = new HashMap<String, Double>();
    map.put(field, value);
    return new AbstractAnalyticValue<Map<String, Double>>(def, map) {
      @Override
      public AnalyticValue<Map<String, Double>> scaleForPosition(
          BigDecimal quantity) {
        return this;
      }
    };
  }
  
  private void populateOptions(InMemoryLKVSnapshotProvider snapshotProvider, boolean addRandom) {
    final double OPTION_SCALE_FACTOR = 5.0;
    final double UNDERLYING_SCALE_FACTOR = 5.0;
    AnalyticValueDefinition<Map<String, Double>> apvjs_x_def = new ResolveSecurityKeyToMarketDataHeaderDefinition(makeSecurityKey("APVJS.X"));
    AnalyticValueDefinition<Map<String, Double>> apvjn_x_def = new ResolveSecurityKeyToMarketDataHeaderDefinition(makeSecurityKey("APVJN.X"));
    AnalyticValueDefinition<Map<String, Double>> ajljv_x_def = new ResolveSecurityKeyToMarketDataHeaderDefinition(makeSecurityKey("AJLJV.X"));
    AnalyticValueDefinition<Map<String, Double>> aapl_def = new ResolveSecurityKeyToMarketDataHeaderDefinition(makeSecurityKey("AAPL"));
    
    AnalyticValue<Map<String, Double>> apvjs_x_val = makeHeaderValue(apvjs_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 2.69);
    AnalyticValue<Map<String, Double>> apvjn_x_val = makeHeaderValue(apvjn_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 16.75);
    AnalyticValue<Map<String, Double>> ajljv_x_val = makeHeaderValue(ajljv_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 0.66);
    AnalyticValue<Map<String, Double>> aapl_val = makeHeaderValue(aapl_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * UNDERLYING_SCALE_FACTOR * 0.01)) * 185.5);
    snapshotProvider.addValue(apvjs_x_val);
    snapshotProvider.addValue(apvjn_x_val);
    snapshotProvider.addValue(ajljv_x_val);
    snapshotProvider.addValue(aapl_val);
  }
  
  private class SnapshotPopulatorJob extends TerminatableJob {
    private final InMemoryLKVSnapshotProvider _snapshotProvider;
    private final DiscountCurveDefinition _curveDefinition;
    
    public SnapshotPopulatorJob(InMemoryLKVSnapshotProvider snapshotProvider,
        DiscountCurveDefinition curveDefinition) {
      _snapshotProvider = snapshotProvider;
      _curveDefinition = curveDefinition;
    }
    
    @Override
    protected void runOneCycle() {
      populateSnapshot(_snapshotProvider, _curveDefinition, true);
      try {
        Thread.sleep(10l);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    
  }
  
  private class PortfolioTableModel extends AbstractTableModel implements ComputationResultListener {
    private List<Position> _positions = new ArrayList<Position>();
    private Map<Position, Map<AnalyticValueDefinition<?>, AnalyticValue<?>>> _resultsMap = new HashMap<Position, Map<AnalyticValueDefinition<?>, AnalyticValue<?>>>();
    private Set<AnalyticValueDefinition<?>> _valueDefinitionsSet = new HashSet<AnalyticValueDefinition<?>>();
    private AnalyticValueDefinition<?>[] _valueDefinitionsArray = new AnalyticValueDefinition<?>[] {};
    
    @Override
    public synchronized String getColumnName(int column) {
      if (column == 0) {
        return "Trade";
      } else {
        AnalyticValueDefinition<?> defn = _valueDefinitionsArray[column-1];
        return defn.getValue("TYPE").toString();
      }
    }
    @Override
    public synchronized int getColumnCount() {
      return _valueDefinitionsArray.length + 1;
    }

    @Override
    public synchronized int getRowCount() {
      return _positions.size();
    }

    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
      Position position = _positions.get(rowIndex);
      if (columnIndex == 0) {
        return position.getSecurityKey().getIdentifiers().iterator().next().getValue() + " @ "+position.getQuantity();
      } else {
        AnalyticValueDefinition<?> defn = _valueDefinitionsArray[columnIndex-1];
        
        Object o = _resultsMap.get(position).get(defn).getValue();
        if (o instanceof DiscountCurve) {
          DiscountCurve curve = (DiscountCurve)o;
          return curve.getData().toString();
        } else {
          return o.toString();
        }
      }
    }
    
    public synchronized Map.Entry<Position, Map<AnalyticValueDefinition<?>, AnalyticValue<?>>> getRow(int rowIndex) {
      Position position = _positions.get(rowIndex);
      Map<AnalyticValueDefinition<?>, AnalyticValue<?>> map = _resultsMap.get(position);
      return new KeyValuePair<Position, Map<AnalyticValueDefinition<?>, AnalyticValue<?>>>(position, map);
    }

    @Override
    public void computationResultAvailable(
        ViewComputationResultModel resultModel) {
      System.err.println("Tick!");
      boolean allDataChanged = false;
      synchronized (this) {
        _positions.clear();
        _positions.addAll(resultModel.getPositions());
        _resultsMap.clear();
        
        for (Position position : _positions) {
          Map<AnalyticValueDefinition<?>, AnalyticValue<?>> values = resultModel.getValues(position);
          _valueDefinitionsSet.addAll(values.keySet());
          _resultsMap.put(position, values);
        }
        int lengthB4 = _valueDefinitionsArray.length;
        _valueDefinitionsArray = _valueDefinitionsSet.toArray(new AnalyticValueDefinition<?>[] {});
        if (_valueDefinitionsArray.length != lengthB4) {
          allDataChanged = true;
        }
      }
      if (allDataChanged) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            fireTableDataChanged();
          }
        });
      } else {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            fireTableRowsUpdated(0, getRowCount());
          }
        });
      }
    }
  }
  
  private class ValueSelectionListenerPanel extends JPanel implements ListSelectionListener, TableModelListener {
    private final Logger s_logger = LoggerFactory.getLogger(ValueSelectionListenerPanel.class);
    private final int DATA_POINTS = 50;
    private JXTable _parentTable;
    @SuppressWarnings("unused")
    private Position _position;
    private Entry<AnalyticValueDefinition<?>, AnalyticValue<?>> _row;
    private One2OneChannel _channel;

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
  
  @SuppressWarnings("unchecked")
  private class PortfolioSelectionListenerAndTableModel extends AbstractTableModel implements ListSelectionListener, TableModelListener {
    private final String[] _columnNames = new String[] { "Definition", "Value" };
    private JXTable _parentTable;
    private Position _position = null;
    private Map.Entry<AnalyticValueDefinition<?>, AnalyticValue<?>>[] _rows = new Map.Entry[0];
    public PortfolioSelectionListenerAndTableModel(JXTable parentTable) {
      _parentTable = parentTable;
      _parentTable.getSelectionModel().addListSelectionListener(this);
      TableModel model = _parentTable.getModel();
      model.addTableModelListener(this);
    }
    
    private void readChanges(ListSelectionModel lsm) {
      boolean allDataChanged = false; // flag to refresh whole table or just all rows
      if (lsm.isSelectionEmpty()) {
        synchronized (this) {
          _rows = new Map.Entry[0];
          setPosition(null);
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
        PortfolioTableModel model = (PortfolioTableModel) _parentTable.getModel();
        Entry<Position, Map<AnalyticValueDefinition<?>, AnalyticValue<?>>> row = model.getRow(modelRow);
        synchronized (this) {
          int previousRows = _rows.length;
          _rows = row.getValue().entrySet().toArray(_rows);
          if (_rows.length != previousRows) {
            allDataChanged = true;
          }
          setPosition(row.getKey());
        }
        if (allDataChanged) {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              fireTableDataChanged();
            }
          });
        } else {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              fireTableRowsUpdated(0, getRowCount());
            }
          });
        }
      }
    }
    @Override
    public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting()) return;
      
      ListSelectionModel lsm = (ListSelectionModel)e.getSource();
      readChanges(lsm);
    }
    
    public synchronized Map.Entry<AnalyticValueDefinition<?>, AnalyticValue<?>> getRow(final int row) {
      if (row > 0) {
        return _rows[row-1];
      } else {
        return null;
      }
    }
    
    
    @Override
    public int getColumnCount() {
      return 2;
    }
    @Override
    public synchronized int getRowCount() {
      return _rows.length+1;
    }
    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex == 0) {
        if (columnIndex == 0) {
          return "Position"; 
        } else { 
          return getPosition(); 
        }
      }
      if (columnIndex == 0) {
        return _rows[rowIndex-1].getKey();
      } else {
        return _rows[rowIndex-1].getValue();
      }
    }
    
    public String getColumnName(int column) {
      return _columnNames[column];
    }

    @Override
    public void tableChanged(TableModelEvent e) {
      ListSelectionModel lsm = _parentTable.getSelectionModel();
      RowSorter<? extends TableModel> rowSorter = _parentTable.getRowSorter();
      int row;
      if (rowSorter != null) {
        row = rowSorter.convertRowIndexToModel(lsm.getMinSelectionIndex());
      } else {
        row = lsm.getMinSelectionIndex();
      }
      // this logic isn't actually necessary at the moment, but might be if we get more efficient table updating.
      if (e.getFirstRow() <= row && e.getLastRow() >= row) {
        // the selected row has changed under us, regrab it.
        readChanges(lsm);
      }
    }

    /**
     * @param position the position to set
     */
    private void setPosition(Position position) {
      _position = position;
    }

    /**
     * @return the position
     */
    public Position getPosition() {
      return _position;
    }
  }
  
  private Pair<JPanel, JXTable> buildLeftTable(JXTable parentTable) {
    PortfolioSelectionListenerAndTableModel listenerAndTableModel = new PortfolioSelectionListenerAndTableModel(parentTable);
    JXTable table = new JXTable(listenerAndTableModel);
    table.setName("positionTable");
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = new JScrollPane(table);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);
    return new Pair<JPanel, JXTable>(panel, table);
  }
  
  private JPanel buildRightTable(JXTable parentTable) {
    ValueSelectionListenerPanel valueSelectionListenerPanel = new ValueSelectionListenerPanel(parentTable);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(valueSelectionListenerPanel);
    return panel;
  }
  
  private TableModel buildTableModel(ViewImpl view) {
    PortfolioTableModel tableModel = new PortfolioTableModel();
    view.addResultListener(tableModel);
    return tableModel; 
  }
  
  private void setupView() {
    try {
      _view = constructTrivialExampleView();
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Error constructing view", e);
    }
    _view.init();
    InMemoryLKVSnapshotProvider snapshotProvider = (InMemoryLKVSnapshotProvider) _view.getProcessingContext().getLiveDataSnapshotProvider();
    DiscountCurveDefinition curveDefinition = constructDiscountCurveDefinition("USD", "Stupidly Lame");
    _popJob = new SnapshotPopulatorJob(snapshotProvider, curveDefinition);
    Thread popThread = new Thread(_popJob);
    popThread.start();
  }
  
  
  @Override
  protected void startup() {
    setupView();
    TableModel tableModel = buildTableModel(_view);
    _view.start();
    JXTable table = new JXTable(tableModel);
    table.setName("table");
    table.setShowGrid(true);
    table.setFillsViewportHeight(true);
    table.setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setSortable(true);
    JScrollPane scrollPane = new JScrollPane(table);
    JPanel panel = new JPanel(new BorderLayout());
   
    JSplitPane splitPane = new JSplitPane(SwingConstants.HORIZONTAL);
    splitPane.add(scrollPane);
    JSplitPane bottomPane = new JSplitPane(SwingConstants.VERTICAL);
    Pair<JPanel, JXTable> buildLeftTable = buildLeftTable(table);
    JPanel leftPanel = buildLeftTable.getFirst();
    JXTable leftTable = buildLeftTable.getSecond();
    bottomPane.add(leftPanel);
    bottomPane.add(buildRightTable(leftTable));
    splitPane.add(bottomPane);
    panel.add(splitPane, BorderLayout.CENTER);
    show(panel);
  }
  
  protected void shutdown() {
    _view.stop();
    _popJob.terminate();
  }
  
  public static void main(String[] args) {
    launch(ViewerLauncher.class, args);
  }

}
