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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
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

import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTable;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.analytics.AbstractAnalyticValue;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.HardCodedUSDDiscountCurveAnalyticFunction;
import com.opengamma.engine.analytics.InMemoryAnalyticFunctionRepository;
import com.opengamma.engine.livedata.FixedLiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.position.csv.CSVPositionMaster;
import com.opengamma.engine.security.InMemorySecurityMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityIdentificationDomain;
import com.opengamma.engine.security.SecurityIdentifier;
import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.MapViewComputationCache;
import com.opengamma.engine.view.ViewComputationCache;
import com.opengamma.engine.view.ViewComputationCacheFactory;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinitionImpl;
import com.opengamma.engine.view.ViewImpl;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.math.interpolation.InterpolationException;
import com.opengamma.util.KeyValuePair;
import com.opengamma.util.TerminatableJob;

/**
 * 
 *
 * @author jim
 */
public class ViewerLauncher extends SingleFrameApplication {
  private ViewImpl _view;
  private SnapshotPopulatorJob _popJob;

  protected ViewImpl constructTrivialExampleView() throws Exception {
    ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("Kirk", "KirkPortfolio");
    viewDefinition.addValueDefinition("KIRK", HardCodedUSDDiscountCurveAnalyticFunction.getDiscountCurveValueDefinition());
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
    int id=1;
    List<Security> securities = new ArrayList<Security>();
    for (final String name : new String[] {"KIRK", "JIM", "ELAINE", "YOMI", "NACHO", "ANDREW"}) {
      final int internalId = id++;
      Security security = new Security() {
        @Override
        public Collection<SecurityIdentifier> getIdentifiers() {
          return Collections.singleton(new SecurityIdentifier(new SecurityIdentificationDomain(name), "ID"+internalId));
        }
        @Override
        public String getSecurityType() {
          return "KIRK";
        }
      };
      securities.add(security);
      secMaster.add(security);
    }
    
    
    HardCodedUSDDiscountCurveAnalyticFunction function = new HardCodedUSDDiscountCurveAnalyticFunction();
    InMemoryAnalyticFunctionRepository functionRepo = new InMemoryAnalyticFunctionRepository(Collections.singleton(function));
    
    FixedLiveDataAvailabilityProvider ldap = new FixedLiveDataAvailabilityProvider();
    for(Security security : securities) {
      for(AnalyticValueDefinition<?> definition : function.getInputs(security)) {
        ldap.addDefinition(definition);
      }
    }
    
    ViewComputationCacheFactory cacheFactory = new ViewComputationCacheFactory() {
      @Override
      public ViewComputationCache generateCache() {
        return new MapViewComputationCache();
      }
    };
    
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    populateSnapshot(snapshotProvider, false);
    
    ViewImpl view = new ViewImpl(viewDefinition);
    view.setPositionMaster(positionMaster);
    view.setAnalyticFunctionRepository(functionRepo);
    view.setLiveDataAvailabilityProvider(ldap);
    view.setSecurityMaster(secMaster);
    view.setComputationCacheFactory(cacheFactory);
    view.setLiveDataSnapshotProvider(snapshotProvider);
    
    return view;
  }
  
  /**
   * @param function 
   * @param snapshotProvider 
   * 
   */
  @SuppressWarnings("unchecked")
  private void populateSnapshot(
      InMemoryLKVSnapshotProvider snapshotProvider,boolean addRandom) {
    // Inflection point is 10.
    double currValue = 0.005;
    for(int i = 0; i < HardCodedUSDDiscountCurveAnalyticFunction.STRIPS.length; i++) {
      if(addRandom) {
        currValue += (Math.random() * 0.010);
      }
      String strip = HardCodedUSDDiscountCurveAnalyticFunction.STRIPS[i];
      final Map<String, Double> dataFields = new HashMap<String, Double>();
      dataFields.put(HardCodedUSDDiscountCurveAnalyticFunction.PRICE_FIELD_NAME, currValue);
      final AnalyticValueDefinition<Object> definition = (AnalyticValueDefinition<Object>) HardCodedUSDDiscountCurveAnalyticFunction.constructDefinition(strip);
      AnalyticValue<Object> value = new AbstractAnalyticValue<Object>(definition, dataFields) {
        @Override
        public AnalyticValue<Object> scaleForPosition(BigDecimal quantity) {
          return this;
        }
      };
      snapshotProvider.addValue(value);
      
      if(i < 10) {
        currValue += 0.005;
      } else {
        currValue -= 0.001;
      }
    }
  }
  
  private class SnapshotPopulatorJob extends TerminatableJob {
    private final InMemoryLKVSnapshotProvider _snapshotProvider;
    
    public SnapshotPopulatorJob(InMemoryLKVSnapshotProvider snapshotProvider) {
      _snapshotProvider = snapshotProvider;
    }
    
    @Override
    protected void runOneCycle() {
      populateSnapshot(_snapshotProvider, true);
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
  
  private class ValueSelectionListener extends JPanel implements ListSelectionListener, TableModelListener {
    private final int DATA_POINTS = 200;
    private JXTable _parentTable;
    private Position _position;
    private Entry<AnalyticValueDefinition<?>, AnalyticValue<?>> _row;

    public ValueSelectionListener(JXTable parentTable) {
      _parentTable = parentTable;
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
    
    public void updateComponent() {
      if (_row.getKey().getValue("TYPE").equals("DISCOUNT_CURVE")) {
        DiscountCurve curve = (DiscountCurve) _row.getValue().getValue();
        double numYears = curve.getData().lastKey();
        double delta = numYears/DATA_POINTS;
        XYSeries xySeries = new XYSeries("Discount Curve");
        for (double t = 0.0d; t<=numYears; t+=delta) {
            xySeries.add(t, curve.getInterestRate(t));
        }
      } else {
        removeAll();
      }
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
  
  private JPanel buildLeftTable(JXTable parentTable) {
    PortfolioSelectionListenerAndTableModel listenerAndTableModel = new PortfolioSelectionListenerAndTableModel(parentTable);
    JXTable table = new JXTable(listenerAndTableModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = new JScrollPane(table);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);
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
      throw new OpenGammaRuntimeException("Error constructing view");
    }
    _view.init();
    InMemoryLKVSnapshotProvider snapshotProvider = (InMemoryLKVSnapshotProvider) _view.getLiveDataSnapshotProvider();
    _popJob = new SnapshotPopulatorJob(snapshotProvider);
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
    bottomPane.add(buildLeftTable(table));
    bottomPane.add(new JLabel("Right"));
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
