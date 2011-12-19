/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.grid;

import com.google.common.collect.Sets;
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
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.RequirementBasedColumnKey;
import com.opengamma.web.server.RequirementBasedGridStructure;
import com.opengamma.web.server.WebGridCell;
import com.opengamma.web.server.WebViewGridColumn;
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
import java.util.Set;

/**
 * An abstract base class for dynamically-structured, requirement-based grids.
 * TODO temporary name just to distinguish it from the similarly named class in the parent package
*/
/* package */ abstract class PushRequirementBasedWebViewGrid extends PushWebViewGrid {

  private static final Logger s_logger = LoggerFactory.getLogger(PushRequirementBasedWebViewGrid.class);

  private final RequirementBasedGridStructure _gridStructure;
  private final String _nullCellValue;
  
  // Column-based state: few entries expected so using an array set 
  private final LongSet _historyOutputs = new LongArraySet();
  
  // Cell-based state
  private final Map<WebGridCell, PushWebViewDepGraphGrid> _depGraphGrids = new HashMap<WebGridCell, PushWebViewDepGraphGrid>();

  protected PushRequirementBasedWebViewGrid(String name,
                                            ViewClient viewClient,
                                            CompiledViewDefinition compiledViewDefinition,
                                            List<ComputationTargetSpecification> targets,
                                            EnumSet<ComputationTargetType> targetTypes,
                                            ResultConverterCache resultConverterCache,
                                            String nullCellValue) {
    super(name, viewClient, resultConverterCache);
    
    List<RequirementBasedColumnKey> requirements = getRequirements(compiledViewDefinition.getViewDefinition(), targetTypes);
    _gridStructure = new RequirementBasedGridStructure(compiledViewDefinition, targetTypes, requirements, targets);
    _nullCellValue = nullCellValue;
  }
  
  //-------------------------------------------------------------------------

  /**
   * @param target The target whose result is required
   * @param resultModel The model containing the results
   * @param resultTimestamp The timestamp of the results
   * @return {@code {"rowId": rowId, "0": col0Val, "1": col1Val, ...}}
   * cell values: {@code {"v": value, "h": [historyVal1, historyVal2, ...]}}
   */
  public Map<String, Object> getTargetResult(ComputationTargetSpecification target,
                                             ViewTargetResultModel resultModel,
                                             Long resultTimestamp) {
    Integer rowId = getGridStructure().getRowId(target);
    if (rowId == null) {
      // Result not in the grid
      return Collections.emptyMap();
    }

    Map<String, Object> valuesToSend = createTargetResult(rowId);
    // insert nulls into the results for cells which are unsatisfied in the dependency graph
    for (Integer unsatisfiedColId : getGridStructure().getUnsatisfiedCells(rowId)) {
      valuesToSend.put(Integer.toString(unsatisfiedColId), null);
    }

    // Whether or not the row is in the viewport, we may have to store history
    if (resultModel != null) {
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
            if (cellData != null) {
              valuesToSend.put(Integer.toString(colId), cellData);
            }
          }
        }
      }
    }
    return valuesToSend;
  }

  /**
   * Creates a blank set of results for a row.
   * @param rowId The zero-based index of the row
   * @return {@code {rowId: rowId}}
   */
  private Map<String, Object> createTargetResult(Integer rowId) {
    Map<String, Object> valuesToSend = new HashMap<String, Object>();
    valuesToSend.put("rowId", rowId);
    return valuesToSend;
  }

  /**
   * Returns all the dependency graphs for the grid.
   * @param resultTimestamp Timestamp of the set of results
   * @return <pre>[{"rowId": "1", "1": {"dg": depGraphForRow1Col1}},
   * {"rowId": "1", "2": {"dg": depGraphForRow1Col2}},
   * {"rowId": "2", "1": {"dg": depGraphForRow2Col1}}]</pre>
   * TODO the return value is ugly but is based on the current Cometd impl to reduce changes in the web client.
   * It's probably worth revisting it at some point
   */
  public List<Map<String, Object>> getDepGraphs(long resultTimestamp) {
    if (_depGraphGrids.isEmpty()) {
      return Collections.emptyList();
    }
    // TODO: this may not be the cycle corresponding to the result - some tracking of cycle IDs required
    EngineResourceReference<? extends ViewCycle> cycleReference = getViewClient().createLatestCycleReference();
    if (cycleReference == null) {
      // Unable to get a cycle reference - perhaps no cycle has completed since enabling introspection
      s_logger.warn("Unable to get a cycle reference");
      return Collections.emptyList();
    }
    List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

    try {
      for (PushWebViewDepGraphGrid depGraphGrid : _depGraphGrids.values()) {
        Object gridStructure = null;
        if (!depGraphGrid.isInit()) {
          String calcConfigName = depGraphGrid.getParentCalcConfigName();
          ValueSpecification valueSpecification = depGraphGrid.getParentValueSpecification();
          DependencyGraphExplorer explorer = cycleReference.get().getCompiledViewDefinition().getDependencyGraphExplorer(calcConfigName);
          DependencyGraph subgraph = explorer.getSubgraphProducing(valueSpecification);
          if (subgraph == null) {
            s_logger.warn("No subgraph producing value specification {}", valueSpecification);
            continue;
          }
          if (depGraphGrid.init(subgraph, calcConfigName, valueSpecification)) {
            gridStructure = depGraphGrid.getInitialJsonGridStructure();
          }
        }
        Map<String, Object> depGraph = depGraphGrid.processViewCycle(cycleReference.get(), resultTimestamp);
        Object depGraphMessage;
        if (gridStructure != null) {
          Map<String, Object> structureMessage = new HashMap<String, Object>();
          structureMessage.put("grid", gridStructure);
          structureMessage.put("update", depGraph);
          depGraphMessage = structureMessage;
        } else {
          depGraphMessage = depGraph;
        }
        Map<String, Object> valuesToSend = createTargetResult(depGraphGrid.getParentGridCell().getRowId());
        Map<String, Object> columnMessage = new HashMap<String, Object>();
        columnMessage.put("dg", depGraphMessage);
        valuesToSend.put(Integer.toString(depGraphGrid.getParentGridCell().getColumnId()), columnMessage);
        results.add(valuesToSend);
      }
    } finally {
      cycleReference.release();
    }
    return results;
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
    for (Map.Entry<ComputationTargetSpecification, Integer> targetEntry : getGridStructure().getTargets().entrySet()) {
      Map<String, Object> rowDetails = new HashMap<String, Object>();
      UniqueId target = targetEntry.getKey().getUniqueId();
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
  
  public RequirementBasedGridStructure getGridStructure() {
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

  /**
   * Returns the dependency graph grid for the specified cell or {@code null} if that cell doesn't have a
   * dependency graph grid.
   * @param row The cell's row index
   * @param col The cell's column index
   * @return The cell's depdency graph grid or {@code null} if it doesn't have one
   */
  /* package */ PushWebViewGrid getDepGraphGrid(int row, int col) {
    return _depGraphGrids.get(new WebGridCell(row, col));
  }

  /* package */ void updateDepGraphCells(Set<WebGridCell> newCells) {
    Set<WebGridCell> currentCells = _depGraphGrids.keySet();
    Set<WebGridCell> cellsToRemove = Sets.difference(currentCells, newCells);
    Set<WebGridCell> cellsToAdd = Sets.difference(newCells, currentCells);

    for (WebGridCell cell : cellsToRemove) {
      _depGraphGrids.remove(cell);
    }
    for (WebGridCell cell : cellsToAdd) {
      String gridName = getName() + ".depgraph-" + cell.getRowId() + "-" + cell.getColumnId();
      OperationTimer timer = new OperationTimer(s_logger, "depgraph");
      Pair<String, ValueSpecification> columnMappingPair =
          getGridStructure().findCellSpecification(cell, getViewClient().getLatestCompiledViewDefinition());
      s_logger.debug("includeDepGraph took {}", timer.finished());
      // TODO should this ever happen? it is currently
      if (columnMappingPair != null) {
        PushWebViewDepGraphGrid grid = new PushWebViewDepGraphGrid(gridName,
                                                                   getViewClient(),
                                                                   getConverterCache(),
                                                                   cell,
                                                                   columnMappingPair.getFirst(),
                                                                   columnMappingPair.getSecond());
        _depGraphGrids.put(cell, grid);
      }
    }
}
  
  //-------------------------------------------------------------------------
  
  @Override
  protected String[][] getRawDataColumnHeaders() {
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
  protected String[][] getRawDataRows(ViewComputationResultModel result) {
    String[][] rows = new String[getGridStructure().getTargets().size()][];
    int columnCount = getGridStructure().getColumns().size() + getAdditionalCsvColumnCount();
    int offset = getCsvDataColumnOffset();
    for (ComputationTargetSpecification target : result.getAllTargets()) {
      Integer rowId = getGridStructure().getRowId(target);
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
            ResultConverter<Object> converter = getConverter(column, value.getSpecification().getValueName(), originalValue.getClass());
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
