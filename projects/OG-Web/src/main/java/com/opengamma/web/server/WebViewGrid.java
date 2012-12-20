/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import org.cometd.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.ImmutableSortedMap;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.conversion.ConversionMode;
import com.opengamma.web.server.conversion.ResultConverter;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 * Stores state relating to an individual grid in a web client instance.
 */
public abstract class WebViewGrid {
  private static final Logger s_logger = LoggerFactory.getLogger(WebViewGrid.class);
  private static final String UPDATES_ROOT_CHANNEL = "/updates";
  private static final int HISTORY_SIZE = 20;
  
  private final String _name;
  
  private final String _updateChannel;
  
  private final ResultConverterCache _resultConverterCache;
  private final ViewClient _viewClient;
  private final Client _local;
  private final Client _remote;
  
  // Row-based state
  private final AtomicReference<SortedMap<Integer, Long>> _viewportMap = new AtomicReference<SortedMap<Integer, Long>>();
  
  // Cell-based state
  private final Set<WebGridCell> _fullConversionModeCells = new CopyOnWriteArraySet<WebGridCell>();  
  
  private final Map<WebGridCell, SortedMap<Long, Object>> _cellValueHistory = new HashMap<WebGridCell, SortedMap<Long, Object>>();
  
  protected WebViewGrid(String name, ViewClient viewClient, ResultConverterCache resultConverterCache, Client local, Client remote) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(viewClient, "viewClient");
    ArgumentChecker.notNull(resultConverterCache, "resultConverterCache");
    ArgumentChecker.notNull(local, "local");
    ArgumentChecker.notNull(remote, "remote");
    
    _name = name;
    _viewClient = viewClient;
    _updateChannel = UPDATES_ROOT_CHANNEL + "/" + name;
    
    _resultConverterCache = resultConverterCache;
    _local = local;
    _remote = remote;
    
    setViewport(ImmutableSortedMap.<Integer, Long>of());
  }
  
  //-------------------------------------------------------------------------
  
  public String getName() {
    return _name;
  }
  
  public SortedMap<Integer, Long> getViewport() {
    return _viewportMap.get();
  }
  
  public void setViewport(SortedMap<Integer, Long> viewportMap) {
    _viewportMap.set(viewportMap);
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
  
  public Map<String, Object> getInitialJsonGridStructure() {
    Map<String, Object> gridStructure = new HashMap<String, Object>();
    gridStructure.put("name", getName());
    gridStructure.put("rows", getInitialJsonRowStructures());
    return gridStructure;
  }

  protected abstract List<Object> getInitialJsonRowStructures();
  
  //-------------------------------------------------------------------------

  protected String getUpdateChannel() {
    return _updateChannel;
  }
  
  protected ResultConverterCache getConverterCache() {
    return _resultConverterCache;
  }
  
  protected Client getRemoteClient() {
    return _remote;
  }
  
  protected Client getLocalClient() {
    return _local;
  }
  
  protected ViewClient getViewClient() {
    return _viewClient;
  }
  
  //-------------------------------------------------------------------------
  
  protected abstract boolean isHistoryOutput(WebGridCell cell);
  
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

  protected Map<String, Object> processCellValue(WebGridCell cell, ValueSpecification specification, Object value, Long resultTimestamp, ResultConverter<Object> converter) {
    boolean rowInViewport = getViewport().containsKey(cell.getRowId());
    Long lastHistoryTimestamp = getViewport().get(cell.getRowId());
    ConversionMode mode = getConversionMode(cell);    
    boolean isHistoryOutput = isHistoryOutput(cell);

    if (isHistoryOutput) {
      Object historyValue;
      if (value != null) {
        historyValue = converter.convertForHistory(_resultConverterCache, specification, value);
      } else {
        historyValue = null;
      }
      addCellHistory(cell, resultTimestamp, historyValue);
    }
    
    if (!rowInViewport) {
      // No reason to send anything
      return null;
    }
    
    Map<String, Object> cellData = new HashMap<String, Object>();
    cellData.put("v", getDisplayValue(specification, mode, value, converter));
    if (isHistoryOutput) {
      // Represent as an object
      SortedMap<Long, Object> history = getCellHistory(cell, lastHistoryTimestamp);
      if (history != null) {
        cellData.put("h", history.values());
      }
    }
    return cellData;
  }

  private Object getDisplayValue(ValueSpecification valueSpecification, ConversionMode mode, Object originalValue, ResultConverter<Object> converter) {
    Object displayValue;
    if (originalValue != null) {
      try {
        displayValue = converter.convertForDisplay(_resultConverterCache, valueSpecification, originalValue, mode);
      } catch (Exception e) {
        s_logger.error("Exception when converting: ", e);
        displayValue = "Conversion Error";
      }
    } else {
      displayValue = null;
    }
    return displayValue;
  }
  
  //-------------------------------------------------------------------------
  
  /*package*/ String dumpContentsToCsv(ViewComputationResultModel result) {
    StringWriter stringWriter = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(stringWriter);
    String[][] columnHeaders = getCsvColumnHeaders();
    if (columnHeaders != null) {
      for (String[] header : columnHeaders) {
        csvWriter.writeNext(header);
      }
    }
    String[][] rows = getCsvRows(result);
    if (rows != null) {
      for (String[] row : rows) {
        csvWriter.writeNext(row);
      }
    }
    return stringWriter.toString();
  }
  
  protected abstract String[][] getCsvColumnHeaders();

  protected abstract String[][] getCsvRows(ViewComputationResultModel result);
  
}
