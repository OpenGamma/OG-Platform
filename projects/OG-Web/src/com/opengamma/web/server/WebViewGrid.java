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
import java.util.LinkedHashMap;
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
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewTargetResultModel;
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
  
  private final Map<UniqueIdentifier, Long> _targetIdMap;
  private final Map<String, WebViewGridColumn> _columnDetailsMap;
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
  
  protected WebViewGrid(String name, List<UniqueIdentifier> targets, ViewDefinition viewDefinition,
      EnumSet<ComputationTargetType> targetTypes, ResultConverterCache resultConverterCache, Client local, Client remote, String nullCellValue) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(targets, "targets");
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    ArgumentChecker.notNull(targetTypes, "targetTypes");
    ArgumentChecker.notNull(resultConverterCache, "resultConverterCache");
    ArgumentChecker.notNull(local, "local");
    ArgumentChecker.notNull(remote, "remote");
    
    _name = name;
    _updateChannel = UPDATES_ROOT_CHANNEL + "/" + name;
    _columnStructureChannel = GRID_STRUCTURE_ROOT_CHANNEL + "/" + name + "/columns";
    
    // Order of targets could be important, so use a linked map
    _targetIdMap = new LinkedHashMap<UniqueIdentifier, Long>();
    long nextId = 0;
    for (UniqueIdentifier target : targets) {
      _targetIdMap.put(target, nextId++);
    }
    
    _columnDetailsMap = generateColumns(viewDefinition, targetTypes);
    
    _resultConverterCache = resultConverterCache;
    _local = local;
    _remote = remote;
    _nullCellValue = nullCellValue;
  }
  
  public String getName() {
    return _name;
  }
  
  public boolean isEmpty() {
    return _columnDetailsMap.isEmpty() || _targetIdMap.isEmpty();
  }
  
  //-------------------------------------------------------------------------

  public void processTargetResult(ComputationTargetSpecification target, ViewTargetResultModel resultModel, Long resultTimestamp) {
    Long rowId = getRowId(target.getUniqueId());
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
      for (Map.Entry<String, ComputedValue> value : resultModel.getValues(configName).entrySet()) {
        String valueRequirementName = value.getKey();
        String columnName = getColumnKey(configName, valueRequirementName);
        WebViewGridColumn columnDetails = _columnDetailsMap.get(columnName);
        long colId = columnDetails.getId();
        
        // s_logger.debug("{} {} = {} {}", new Object[] {target.getUniqueId(), columnName, value.getValue().getValue(), value.getValue().getSpecification().getProperties()});

        WebGridCell cell = WebGridCell.of(rowId, colId);
        
        ConversionMode mode = getConversionMode(cell);
        Object latestValue;
        try {
          latestValue = convertResult(columnDetails, value.getValue().getValue(), mode);
        } catch (Exception e) {
          s_logger.error("Exception when converting: ", e);
          latestValue = "Conversion Error";
        }
        
        boolean isHistoryOutput = isHistoryOutput(colId);
        if (isHistoryOutput) {
          addCellHistory(cell, resultTimestamp, latestValue);
        }
        
        if (rowInViewport) {
          // Client requires this row
          Object cellValue = null;
          if (isHistoryOutput) {
            SortedMap<Long, Object> history = getCellHistory(cell, lastHistoryTime);
            if (history != null) {
              cellValue = history.values();
            }
          } else {
            cellValue = latestValue;
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
  
  private <T> Object convertResult(WebViewGridColumn column, T value, ConversionMode mode) {
    if (value == null) {
      return null;
    }
    
    if (!column.isTypeKnown()) {
      sendColumnDetails(Collections.singleton(column));
    }
    
    return _resultConverterCache.convert(column.getValueRequirementName(), value, mode);
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
  
  public Object getGridStructure() {
    Map<String, Object> gridStructure = new HashMap<String, Object>();
    gridStructure.put("name", getName());
    gridStructure.put("rows", getRowStructures());
    gridStructure.put("columns", getColumnStructures(_columnDetailsMap.values()));
    return gridStructure;
  }

  private void sendColumnDetails(Collection<WebViewGridColumn> columnDetails) {
    _remote.deliver(_local, _columnStructureChannel, getColumnStructures(columnDetails), null);
  }
  
  private Map<String, Object> getColumnStructures(Collection<WebViewGridColumn> columns) {
    Map<String, Object> columnStructures = new HashMap<String, Object>();
    for (WebViewGridColumn columnDetails : columns) {
      columnStructures.put(Long.toString(columnDetails.getId()), getColumnStructure(columnDetails));
    }
    return columnStructures;
  }
  
  private Map<String, Object> getColumnStructure(WebViewGridColumn column) {
    Map<String, Object> detailsToSend = new HashMap<String, Object>();
    long colId = column.getId();
    detailsToSend.put("key", column.getKey());
    detailsToSend.put("colId", colId);
    detailsToSend.put("nullValue", _nullCellValue);
    
    String resultType = _resultConverterCache.getKnownResultTypeName(column.getValueRequirementName());
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

  private List<Object> getRowStructures() {
    List<Object> rowStructures = new ArrayList<Object>();
    for (Map.Entry<UniqueIdentifier, Long> targetEntry : _targetIdMap.entrySet()) {
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
  // Utilities
  
  private Map<String, WebViewGridColumn> generateColumns(ViewDefinition viewDefinition, EnumSet<ComputationTargetType> targetTypes) {
    Map<String, WebViewGridColumn> columns = new LinkedHashMap<String, WebViewGridColumn>();
    long colId = 0;
    for (ViewCalculationConfiguration calcConfig : viewDefinition.getAllCalculationConfigurations()) {
      String configName = calcConfig.getName();
      
      if (targetTypes.contains(ComputationTargetType.POSITION) || targetTypes.contains(ComputationTargetType.PORTFOLIO_NODE)) {
        for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
          String columnKey = getColumnKey(configName, portfolioOutput.getFirst());
          columns.put(columnKey, new WebViewGridColumn(colId++, columnKey, portfolioOutput.getFirst()));
        }
      }
      
      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        if (!targetTypes.contains(specificRequirement.getTargetSpecification().getType())) {
          continue;
        }
        String valueName = specificRequirement.getValueName();
        String columnKey = getColumnKey(configName, valueName);
        columns.put(columnKey, new WebViewGridColumn(colId++, columnKey, valueName));
      }
    }
    return columns;
  }
  
  private static String getColumnKey(String configName, String outputName) {
    return configName + "/" + outputName;
  }
  
  protected Long getRowId(UniqueIdentifier target) {
    return _targetIdMap.get(target);
  }
  
}
