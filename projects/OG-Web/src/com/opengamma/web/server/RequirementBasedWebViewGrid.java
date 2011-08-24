/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewTargetResultModel;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.conversion.ResultConverter;
import com.opengamma.web.server.conversion.ResultConverterCache;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An abstract base class for dynamically-structured, requirement-based grids.
 */
public abstract class RequirementBasedWebViewGrid extends WebViewGrid {

  private static final Logger s_logger = LoggerFactory.getLogger(RequirementBasedWebViewGrid.class);

  private final RequirementBasedGridStructure _gridStructure;
  private final String _nullCellValue;
  
  // Column-based state: few entries expected so using an array set 
  private final LongSet _historyOutputs = new LongArraySet();
  
  // Cell-based state
  private final ConcurrentMap<WebGridCell, WebViewDepGraphGrid> _depGraphGrids = new ConcurrentHashMap<WebGridCell, WebViewDepGraphGrid>();

  protected RequirementBasedWebViewGrid(String name,
                                        ViewClient viewClient,
                                        CompiledViewDefinition compiledViewDefinition,
                                        List<UniqueId> targets,
                                        EnumSet<ComputationTargetType> targetTypes,
                                        ResultConverterCache resultConverterCache,
                                        String nullCellValue) {
    super(name, viewClient, resultConverterCache);
    
    List<RequirementBasedColumnKey> requirements = getRequirements(compiledViewDefinition.getViewDefinition(), targetTypes);
    _gridStructure = new RequirementBasedGridStructure(compiledViewDefinition, targetTypes, requirements, targets);
    _nullCellValue = nullCellValue;
  }
  
  //-------------------------------------------------------------------------

  // publishes results to the client TODO would it be better if it returned the value?

  /**
   * @return {@code {"rowId": rowId, "0": col0Val, "1": col1Val, ...}}
   * cell values: {"v": value, "h": [historyVal1, historyVal2, ...], "dg", depGraph}
   */
  public Map<String, Object> getTargetResult(ComputationTargetSpecification target,
                                             ViewTargetResultModel resultModel,
                                             Long resultTimestamp) {
    Integer rowId = getGridStructure().getRowId(target.getUniqueId());
    if (rowId == null) {
      // Result not in the grid
      return null; // TODO empty map?
    }

    Map<String, Object> valuesToSend = createDefaultTargetResult(rowId);
    
    // Whether or not the row is in the viewport, we may have to store history
    for (String calcConfigName : resultModel.getCalculationConfigurationNames()) {
      
      for (ComputedValue value : resultModel.getAllValues(calcConfigName)) {
        ValueSpecification specification = value.getSpecification();
        Collection<WebViewGridColumn> columns = getGridStructure().getColumns(calcConfigName, specification);
        if (columns == null) {
          // Expect a column for every value
          s_logger.warn("Could not find column for calculation configuration {} with value specification {}", calcConfigName, specification);
          continue;
        }
        
        Object originalValue = value.getValue();        
        for (WebViewGridColumn column : columns) {
          int colId = column.getId();
          WebGridCell cell = WebGridCell.of(rowId, colId);
          ResultConverter<Object> converter;
          if (originalValue == null) {
            converter = null;
          } else {
            converter = getConverter(column, value.getSpecification().getValueName(), originalValue.getClass());
          }
          Map<String, Object> cellData = getCellValue(cell, specification, originalValue, resultTimestamp, converter);
          Object depGraph = getDepGraphIfRequested(cell, calcConfigName, specification, resultTimestamp);
          if (depGraph != null) {
            if (cellData == null) {
              cellData = new HashMap<String, Object>();
            }
            cellData.put("dg", depGraph);
          }
          if (cellData != null) {
            valuesToSend.put(Integer.toString(colId), cellData);
          }
        }
      }
    }
    return valuesToSend; // TODO empty map if null?
  }
  
  private Map<String, Object> createDefaultTargetResult(Integer rowId) {
    Map<String, Object> valuesToSend;
    valuesToSend = new HashMap<String, Object>();
    valuesToSend.put("rowId", rowId);
    for (Integer unsatisfiedColId : getGridStructure().getUnsatisfiedCells(rowId)) {
      valuesToSend.put(Integer.toString(unsatisfiedColId), null);
    }
    return valuesToSend;
  }

  // TODO this publishes to the client. not nice for a method named get*
  @SuppressWarnings("unchecked")
  private ResultConverter<Object> getConverter(WebViewGridColumn column, String valueName, Class<?> valueType) {
    // Ensure the converter is cached against the value name before sending the column details 
    ResultConverter<Object> converter = (ResultConverter<Object>) getConverterCache().getAndCacheConverter(valueName, valueType);
    if (!column.isTypeKnown()) {
      // TODO what's this all about?
      //getRemoteClient().deliver(getLocalClient(), _columnStructureChannel, getJsonColumnStructures(Collections.singleton(column)), null);
    }
    return converter;
  }

  @Override
  public Map<String, Object> getInitialJsonGridStructure() {
    Map<String, Object> gridStructure = super.getInitialJsonGridStructure();
    gridStructure.put("columns", getJsonColumnStructures(getGridStructure().getColumns()));
    return gridStructure;
  }
  
  @Override
  protected List<Object> getInitialJsonRowStructures() {
    List<Object> rowStructures = new ArrayList<Object>();
    for (Map.Entry<UniqueId, Integer> targetEntry : getGridStructure().getTargets().entrySet()) {
      Map<String, Object> rowDetails = new HashMap<String, Object>();
      UniqueId target = targetEntry.getKey();
      int rowId = targetEntry.getValue();
      rowDetails.put("rowId", rowId);
      addRowDetails(target, rowId, rowDetails);
      rowStructures.add(rowDetails);
    }
    return rowStructures;
  }
  
  private Map<String, Object> getJsonColumnStructures(Collection<WebViewGridColumn> columns) {
    Map<String, Object> columnStructures = new HashMap<String, Object>(columns.size());
    for (WebViewGridColumn columnDetails : columns) {
      columnStructures.put(Integer.toString(columnDetails.getId()), getJsonColumnStructure(columnDetails));
    }
    return columnStructures;
  }
  
  private Map<String, Object> getJsonColumnStructure(WebViewGridColumn column) {
    Map<String, Object> detailsToSend = new HashMap<String, Object>();
    long colId = column.getId();
    detailsToSend.put("colId", colId);
    detailsToSend.put("header", column.getHeader());
    detailsToSend.put("description", column.getValueName() + ":\n" + column.getDescription());
    detailsToSend.put("nullValue", _nullCellValue);
    
    String resultType = getConverterCache().getKnownResultTypeName(column.getValueName());
    if (resultType != null) {
      column.setTypeKnown(true);
      detailsToSend.put("dataType", resultType);
      
      // Hack - the client should decide which columns it requires history for, taking into account the capabilities of
      // the renderer.
      if (resultType.equals("DOUBLE")) {
        addHistoryOutput(column.getId());
      }
    }
    return detailsToSend;
  }
  
  protected abstract void addRowDetails(UniqueId target, int rowId, Map<String, Object> details);
  
  //-------------------------------------------------------------------------
  
  protected RequirementBasedGridStructure getGridStructure() {
    return _gridStructure;
  }
  
  //-------------------------------------------------------------------------
  
  private void addHistoryOutput(long colId) {
    _historyOutputs.add(colId);
  }

  @Override
  protected boolean isHistoryOutput(WebGridCell cell) {
    return _historyOutputs.contains(cell.getColumnId());
  }
  
  //-------------------------------------------------------------------------

  // TODO does this belong in the portfolio-specific subclass? or can / will you be able to get dep graphs for primitives?
  public WebViewGrid getDepGraphGrid(String name) {
    // TODO implement RequirementBasedWebViewGrid.getDepGraphGrid()
    throw new UnsupportedOperationException("getDepGraphGrid not implemented");
  }

  /* package */ void updateDepGraphCells(List<WebGridCell> dependencyGraphCells) {
    // TODO implement
  }

  // TODO move this logic to updateDepGraphCells
  public WebViewGrid setIncludeDepGraph(WebGridCell cell, boolean includeDepGraph) {
    if (includeDepGraph) {
      String gridName = getName() + ".depgraph-" + cell.getRowId() + "-" + cell.getColumnId();      
      WebViewDepGraphGrid grid = new WebViewDepGraphGrid(gridName, getViewClient(), getConverterCache());
      _depGraphGrids.putIfAbsent(cell, grid);
      return grid;
    } else {
      return _depGraphGrids.remove(cell);
    }
  }
  
  private Object getDepGraphIfRequested(WebGridCell cell, String calcConfigName, ValueSpecification valueSpecification, Long resultTimestamp) {
    WebViewDepGraphGrid depGraphGrid = _depGraphGrids.get(cell);
    if (depGraphGrid == null) {
      return null;
    }
    
    // TODO: this may not be the cycle corresponding to the result - some tracking of cycle IDs required
    EngineResourceReference<? extends ViewCycle> cycleReference = getViewClient().createLatestCycleReference();
    if (cycleReference == null) {
      // Unable to get a cycle reference - perhaps no cycle has completed since enabling introspection
      return null;
    }
    
    try {
      Object gridStructure = null;
      if (!depGraphGrid.isInit()) {
        DependencyGraphExplorer explorer = cycleReference.get().getCompiledViewDefinition().getDependencyGraphExplorer(calcConfigName);
        DependencyGraph subgraph = explorer.getSubgraphProducing(valueSpecification);
        if (subgraph == null) {
          s_logger.warn("No subgraph producing value specification {}", valueSpecification);
          return null;
        }
        if (depGraphGrid.init(subgraph, calcConfigName, valueSpecification)) {
          gridStructure = depGraphGrid.getInitialJsonGridStructure();
        }
      }
      Map<String, Object> depGraph = depGraphGrid.processViewCycle(cycleReference.get(), resultTimestamp);
      if (gridStructure != null) {
        Map<String, Object> structureMessage = new HashMap<String, Object>();
        structureMessage.put("grid", gridStructure);
        structureMessage.put("update", depGraph);
        return structureMessage;
      } else {
        return depGraph;
      }
    } finally {
      cycleReference.release();
    }
  }
  
  //-------------------------------------------------------------------------
  
  @Override
  protected String[][] getCsvColumnHeaders() {
    Collection<WebViewGridColumn> columns = getGridStructure().getColumns();
    int additionalColumns = getAdditionalCsvColumnCount();
    int columnCount = columns.size() + additionalColumns;
    String[] header1 = new String[columnCount];
    String[] header2 = new String[columnCount];
    supplementCsvColumnHeaders(header1);
    int offset = getCsvDataColumnOffset();
    for (WebViewGridColumn column : columns) {
      header1[offset + column.getId()] = column.getHeader();
      header2[offset + column.getId()] = column.getDescription();
    }
    return new String[][] {header1, header2};
  }

  @Override
  protected String[][] getCsvRows(ViewComputationResultModel result) {
    String[][] rows = new String[getGridStructure().getTargets().size()][];
    int columnCount = getGridStructure().getColumns().size() + getAdditionalCsvColumnCount();
    int offset = getCsvDataColumnOffset();
    for (ComputationTargetSpecification target : result.getAllTargets()) {
      Integer rowId = getGridStructure().getRowId(target.getUniqueId());
      if (rowId == null) {
        continue;
      }
      ViewTargetResultModel resultModel = result.getTargetResult(target);
      String[] values = new String[columnCount];
      supplementCsvRowData(rowId, target, values);
      rows[rowId] = values;
      for (String calcConfigName : resultModel.getCalculationConfigurationNames()) {
        for (ComputedValue value : resultModel.getAllValues(calcConfigName)) {
          Object originalValue = value.getValue();
          if (originalValue == null) {
            continue;
          }
          ValueSpecification specification = value.getSpecification();
          Collection<WebViewGridColumn> columns = getGridStructure().getColumns(calcConfigName, specification);
          if (columns == null) {
            // Expect a column for every value
            s_logger.warn("Could not find column for calculation configuration {} with value specification {}", calcConfigName, specification);
            continue;
          }
          for (WebViewGridColumn column : columns) {
            int colId = column.getId();
            ResultConverter<Object> converter = originalValue != null ? getConverter(column, value.getSpecification().getValueName(), originalValue.getClass()) : null;
            values[offset + colId] = converter.convertToText(getConverterCache(), value.getSpecification(), originalValue);
          }
        }
      }
    }
    return rows;
  }

  protected int getAdditionalCsvColumnCount() {
    return 0;
  }
  
  protected int getCsvDataColumnOffset() {
    return 0;
  }
  
  protected void supplementCsvColumnHeaders(String[] headers) {
  }
  
  protected void supplementCsvRowData(int rowId, ComputationTargetSpecification target, String[] row) {
  }
  
  //-------------------------------------------------------------------------

  private static List<RequirementBasedColumnKey> getRequirements(ViewDefinition viewDefinition, EnumSet<ComputationTargetType> targetTypes) {
    List<RequirementBasedColumnKey> result = new ArrayList<RequirementBasedColumnKey>();
    for (ViewCalculationConfiguration calcConfig : viewDefinition.getAllCalculationConfigurations()) {
      String calcConfigName = calcConfig.getName();
      if (targetTypes.contains(ComputationTargetType.POSITION) || targetTypes.contains(ComputationTargetType.PORTFOLIO_NODE)) {
        for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
          String valueName = portfolioOutput.getFirst();
          ValueProperties constraints = portfolioOutput.getSecond();
          RequirementBasedColumnKey columnKey = new RequirementBasedColumnKey(calcConfigName, valueName, constraints);
          result.add(columnKey);
        }
      }
      
      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        if (!targetTypes.contains(specificRequirement.getTargetSpecification().getType())) {
          continue;
        }
        String valueName = specificRequirement.getValueName();
        ValueProperties constraints = specificRequirement.getConstraints();
        RequirementBasedColumnKey columnKey = new RequirementBasedColumnKey(calcConfigName, valueName, constraints);
        result.add(columnKey);
      }
    }
    return result;
  }
}
