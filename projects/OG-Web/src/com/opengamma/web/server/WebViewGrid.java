/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import org.cometd.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewTargetResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.conversion.ConversionMode;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 * Stores state relating to an individual grid in a web client instance.
 */
public abstract class WebViewGrid {
  private static final Logger s_logger = LoggerFactory.getLogger(WebViewGrid.class);
  private static final String GRID_STRUCTURE_ROOT_CHANNEL = "/gridStructure";
  private static final String UPDATES_ROOT_CHANNEL = "/updates";
  private static final int HISTORY_SIZE = 20;
  
  private final String _name;
  
  private final String _updateChannel;
  private final String _columnStructureChannel;
  
  private final WebViewGridStructure _gridStructure;
  private final ResultConverterCache _resultConverterCache;
  private final Client _local;
  private final Client _remote;
  private final String _nullCellValue;
  
  // Row-based state
  private final AtomicReference<SortedMap<Long, Long>> _viewportMap = new AtomicReference<SortedMap<Long, Long>>();
  
  // Column-based state: few entries expected so using an array set 
  private final LongSet _historyOutputs = new LongArraySet();
  
  // Cell-based state
  private final Set<WebGridCell> _fullConversionModeCells = new HashSet<WebGridCell>();
  private final Map<WebGridCell, SortedMap<Long, Object>> _cellValueHistory = new HashMap<WebGridCell, SortedMap<Long, Object>>();
  
  protected WebViewGrid(String name, CompiledViewDefinition compiledViewDefinition, List<UniqueIdentifier> targets,
      EnumSet<ComputationTargetType> targetTypes, ResultConverterCache resultConverterCache, Client local,
      Client remote, String nullCellValue) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(compiledViewDefinition, "compiledViewDefinition");
    ArgumentChecker.notNull(targetTypes, "targetTypes");
    ArgumentChecker.notNull(resultConverterCache, "resultConverterCache");
    ArgumentChecker.notNull(local, "local");
    ArgumentChecker.notNull(remote, "remote");
    
    _name = name;
    _updateChannel = UPDATES_ROOT_CHANNEL + "/" + name;
    _columnStructureChannel = GRID_STRUCTURE_ROOT_CHANNEL + "/" + name + "/columns";
    
    List<WebViewGridColumnKey> requirements = getRequirements(compiledViewDefinition.getViewDefinition(), targetTypes);    
    _gridStructure = new WebViewGridStructure(compiledViewDefinition, targetTypes, requirements, targets);
    
    _resultConverterCache = resultConverterCache;
    _local = local;
    _remote = remote;
    _nullCellValue = nullCellValue;
  }
  
  public String getName() {
    return _name;
  }
  
  //-------------------------------------------------------------------------

  public void processTargetResult(ComputationTargetSpecification target, ViewTargetResultModel resultModel, Long resultTimestamp) {
    Long rowId = getGridStructure().getRowId(target.getUniqueId());
    if (rowId == null) {
      // Result not in the grid
      return;
    }
    boolean rowInViewport = getViewport().containsKey(rowId);
    Long lastHistoryTime = getViewport().get(rowId);
    Map<String, Object> valuesToSend = null;
    if (rowInViewport) {
      valuesToSend = new HashMap<String, Object>();
      valuesToSend.put("rowId", rowId);
    }
    
    for (String configName : resultModel.getCalculationConfigurationNames()) {
      for (ComputedValue value : resultModel.getAllValues(configName)) {
        ValueSpecification specification = value.getSpecification();
        WebViewGridColumn column = getGridStructure().getColumn(configName, specification);
        if (column == null) {
          s_logger.warn("Could not find column for calculation configuration {} with value specification {}", configName, specification);
          continue;
        }
        
        long colId = column.getId();
        
        // s_logger.debug("{} {} = {} {}", new Object[] {target.getUniqueId(), columnName, value.getValue().getValue(), value.getValue().getSpecification().getProperties()});

        WebGridCell cell = WebGridCell.of(rowId, colId);
        
        ConversionMode mode = getConversionMode(cell);
        Object originalValue = value.getValue();
        Object displayValue;
        try {
          displayValue = convertResult(column, value.getSpecification(), originalValue, mode);
        } catch (Exception e) {
          s_logger.error("Exception when converting: ", e);
          displayValue = "Conversion Error";
        }
        
        boolean isHistoryOutput = isHistoryOutput(colId);
        if (isHistoryOutput) {
          addCellHistory(cell, resultTimestamp, originalValue);
        }
        
        Object cellValue;
        if (rowInViewport) {
          // Client requires this row
          if (isHistoryOutput) {
            Map<String, Object> cellData = new HashMap<String, Object>();
            cellData.put("display", displayValue);
            SortedMap<Long, Object> history = getCellHistory(cell, lastHistoryTime);
            if (history != null) {
              cellData.put("history", history.values());
            }
            cellValue = cellData;
          } else {
            cellValue = displayValue;
          }

          if (cellValue != null) {
            valuesToSend.put(Long.toString(colId), cellValue);
          }
        }
      }
    }
    if (rowInViewport) {
      _remote.deliver(_local, _updateChannel, valuesToSend, null);
    }
  }
  
  private <T> Object convertResult(WebViewGridColumn column, ValueSpecification valueSpec, T value, ConversionMode mode) {
    if (value == null) {
      return null;
    }
    
    if (!column.isTypeKnown()) {
      sendColumnDetails(Collections.singleton(column));
    }
    
    return _resultConverterCache.convert(valueSpec, value, mode);
  }
  
  public ConversionMode getConversionMode(WebGridCell cell) {
    return _fullConversionModeCells.contains(cell)
        ? ConversionMode.FULL
        : ConversionMode.SUMMARY;
  }
  
  public void setConversionMode(WebGridCell cell, ConversionMode mode) {
    if (mode == ConversionMode.SUMMARY) {
      _fullConversionModeCells.remove(cell);
    } else {
      _fullConversionModeCells.add(cell);
    }
  }
  
  //-------------------------------------------------------------------------
  
  public Object getJsonGridStructure() {
    Map<String, Object> gridStructure = new HashMap<String, Object>();
    gridStructure.put("name", getName());
    gridStructure.put("rows", getJsonRowStructures());
    gridStructure.put("columns", getJsonColumnStructures(getGridStructure().getColumns()));
    return gridStructure;
  }

  private void sendColumnDetails(Collection<WebViewGridColumn> columnDetails) {
    _remote.deliver(_local, _columnStructureChannel, getJsonColumnStructures(columnDetails), null);
  }
  
  private Map<String, Object> getJsonColumnStructures(Collection<WebViewGridColumn> columns) {
    Map<String, Object> columnStructures = new HashMap<String, Object>();
    for (WebViewGridColumn columnDetails : columns) {
      columnStructures.put(Long.toString(columnDetails.getId()), getJsonColumnStructure(columnDetails));
    }
    return columnStructures;
  }
  
  private Map<String, Object> getJsonColumnStructure(WebViewGridColumn column) {
    Map<String, Object> detailsToSend = new HashMap<String, Object>();
    long colId = column.getId();
    detailsToSend.put("colId", colId);
    detailsToSend.put("header", column.getHeader());
    detailsToSend.put("description", column.getDescription());
    detailsToSend.put("nullValue", _nullCellValue);
    
    String resultType = _resultConverterCache.getKnownResultTypeName(column.getValueName());
    if (resultType != null) {
      column.setTypeKnown(true);
      detailsToSend.put("dataType", resultType);
      
      // Hack - the client should decide which columns it requires history for, taking into account the capabilities of
      // the renderer.
      if (resultType.equals("PRIMITIVE")) {
        addHistoryOutput(column.getId());
      }
    }
    return detailsToSend;
  }

  private List<Object> getJsonRowStructures() {
    List<Object> rowStructures = new ArrayList<Object>();
    for (Map.Entry<UniqueIdentifier, Long> targetEntry : getGridStructure().getTargets().entrySet()) {
      Map<String, Object> rowDetails = new HashMap<String, Object>();
      UniqueIdentifier target = targetEntry.getKey();
      long rowId = targetEntry.getValue();
      rowDetails.put("rowId", rowId);
      addRowDetails(target, rowId, rowDetails);
      rowStructures.add(rowDetails);
    }
    return rowStructures;
  }
  
  protected abstract void addRowDetails(UniqueIdentifier target, long rowId, Map<String, Object> details);
  
  //-------------------------------------------------------------------------
  
  public SortedMap<Long, Long> getViewport() {
    return _viewportMap.get();
  }
  
  public void setViewport(SortedMap<Long, Long> viewportMap) {
    _viewportMap.set(viewportMap);
  }
  
  protected WebViewGridStructure getGridStructure() {
    return _gridStructure;
  }
  
  //-------------------------------------------------------------------------
  
  private void addHistoryOutput(long colId) {
    _historyOutputs.add(colId);
  }
  
  private boolean isHistoryOutput(long colId) {
    return _historyOutputs.contains(colId);
  }
  
  private void addCellHistory(WebGridCell cell, Long timestamp, Object value) {
    SortedMap<Long, Object> history = _cellValueHistory.get(cell);
    if (history == null) {
      history = new TreeMap<Long, Object>();
      _cellValueHistory.put(cell, history);
    }
    if (history.size() > HISTORY_SIZE) {
      history.remove(history.entrySet().iterator().next().getKey());
    }
    history.put(timestamp, value);
  }
  
  private SortedMap<Long, Object> getCellHistory(WebGridCell cell, Long lastTimestamp) {
    SortedMap<Long, Object> history = _cellValueHistory.get(cell);
    if (history == null) {
      return null;
    }
    if (lastTimestamp == null) {
      return history;
    }
    return history.tailMap(lastTimestamp + 1);
  }

  //-------------------------------------------------------------------------

  private static List<WebViewGridColumnKey> getRequirements(ViewDefinition viewDefinition, EnumSet<ComputationTargetType> targetTypes) {
    List<WebViewGridColumnKey> result = new ArrayList<WebViewGridColumnKey>();
    for (ViewCalculationConfiguration calcConfig : viewDefinition.getAllCalculationConfigurations()) {
      String calcConfigName = calcConfig.getName();
      if (targetTypes.contains(ComputationTargetType.POSITION) || targetTypes.contains(ComputationTargetType.PORTFOLIO_NODE)) {
        for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
          String valueName = portfolioOutput.getFirst();
          ValueProperties constraints = portfolioOutput.getSecond();
          WebViewGridColumnKey columnKey = new WebViewGridColumnKey(calcConfigName, valueName, constraints);
          result.add(columnKey);
        }
      }
      
      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        if (!targetTypes.contains(specificRequirement.getTargetSpecification().getType())) {
          continue;
        }
        String valueName = specificRequirement.getValueName();
        ValueProperties constraints = specificRequirement.getConstraints();
        WebViewGridColumnKey columnKey = new WebViewGridColumnKey(calcConfigName, valueName, constraints);
        result.add(columnKey);
      }
    }
    return result;
  }
    
}
