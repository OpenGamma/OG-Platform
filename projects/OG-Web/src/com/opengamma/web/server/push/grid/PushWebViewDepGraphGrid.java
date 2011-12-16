/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.grid;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calc.ComputationCacheQuery;
import com.opengamma.engine.view.calc.ComputationCacheResponse;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.WebGridCell;
import com.opengamma.web.server.conversion.ResultConverter;
import com.opengamma.web.server.conversion.ResultConverterCache;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a dependency graph grid. This is slightly special since, unlike the other grids, the columns are known
 * statically and each value may differ in type.
 * TODO temporary name just to distinguish it from the similarly named class in the parent package
*/
/* package */ class PushWebViewDepGraphGrid extends PushWebViewGrid {

  private static final Logger s_logger = LoggerFactory.getLogger(PushWebViewDepGraphGrid.class);

  private final AtomicBoolean _init = new AtomicBoolean();
  private final IntSet _historyOutputs = new IntOpenHashSet();
  private final WebGridCell _parentGridCell;
  private final String _parentCalcConfigName;
  private final ValueSpecification _parentValueSpecification;
  private final Set<ValueSpecification> _typedRows = new HashSet<ValueSpecification>();

  private Map<ValueSpecification, IntSet> _rowIdMap;
  private List<Object> _rowStructure;
  private ComputationCacheQuery _cacheQuery;

  protected PushWebViewDepGraphGrid(String name,
                                    ViewClient viewClient,
                                    ResultConverterCache resultConverterCache,
                                    WebGridCell parentGridCell,
                                    String parentCalcConfigName,
                                    ValueSpecification parentValueSpecification) {
    super(name, viewClient, resultConverterCache);
    _parentGridCell = parentGridCell;
    _parentCalcConfigName = parentCalcConfigName;
    _parentValueSpecification = parentValueSpecification;
  }
  
  //-------------------------------------------------------------------------
  
  /*package*/ boolean isInit() {
    return _init.get();
  }
  
  /*package*/ boolean init(DependencyGraph depGraph, String calcConfigName, ValueSpecification valueSpecification) {
    if (!_init.compareAndSet(false, true)) {
      return false;
    }
    
    HashMap<ValueSpecification, IntSet> rowIdMap = new HashMap<ValueSpecification, IntSet>();
    _rowStructure = generateRowStructure(depGraph, valueSpecification, rowIdMap);
    _rowIdMap = rowIdMap;
    
    _cacheQuery = new ComputationCacheQuery();
    _cacheQuery.setCalculationConfigurationName(calcConfigName);
    _cacheQuery.setValueSpecifications(new HashSet<ValueSpecification>(_rowIdMap.keySet()));
    
    // Not doing viewport for now, so tell it that everything is in the viewport
    SortedMap<Integer, Long> viewportMap = new TreeMap<Integer, Long>();
    for (IntSet rowIds : rowIdMap.values()) {
      for (int rowId : rowIds) {
        viewportMap.put(rowId, null);
      }
    }
    setViewport(viewportMap);
    return true;
  }
  
  /*package*/ WebGridCell getParentGridCell() {
    return _parentGridCell;
  }

  /*package*/ String getParentCalcConfigName() {
    return _parentCalcConfigName;
  }

  /*package*/ ValueSpecification getParentValueSpecification() {
    return _parentValueSpecification;
  }

  private List<Object> generateRowStructure(DependencyGraph depGraph, ValueSpecification output, Map<ValueSpecification, IntSet> rowIdMap) {
    List<Object> rowStructure = new ArrayList<Object>();
    addRowIdAssociation(0, output, rowIdMap);
    rowStructure.add(getJsonRowStructure(depGraph.getNodeProducing(output), output, -1, 0, 0));
    addInputRowStructures(depGraph, depGraph.getNodeProducing(output), rowIdMap, rowStructure, 1, 0, 1);
    return rowStructure;
  }
  
  private int addInputRowStructures(DependencyGraph graph, DependencyNode node, Map<ValueSpecification, IntSet> rowIdMap, List<Object> rowStructure, int indent, int parentRowId, int nextRowId) {
    for (ValueSpecification inputValue : node.getInputValues()) {
      DependencyNode inputNode = graph.getNodeProducing(inputValue);
      int rowId = nextRowId++;
      addRowIdAssociation(rowId, inputValue, rowIdMap);
      rowStructure.add(getJsonRowStructure(inputNode, inputValue, parentRowId, rowId, indent));
      nextRowId = addInputRowStructures(graph, inputNode, rowIdMap, rowStructure, indent + 1, rowId, nextRowId);
    }
    return nextRowId;
  }
  
  private void addRowIdAssociation(int rowId, ValueSpecification specification, Map<ValueSpecification, IntSet> rowIdMap) {
    IntSet rowIdSet = rowIdMap.get(specification);
    if (rowIdSet == null) {
      rowIdSet = new IntArraySet();
      rowIdMap.put(specification, rowIdSet);
    }
    rowIdSet.add(rowId);
  }
  
  private Object getJsonRowStructure(DependencyNode node, ValueSpecification valueSpecification, long parentRowId, long rowId, int indent) {
    Map<String, Object> row = new HashMap<String, Object>();
    String targetName;
    if (node.getComputationTarget().getType() == ComputationTargetType.TRADE) {
      targetName = node.getComputationTarget().getUniqueId().toString();
    } else {
      targetName = node.getComputationTarget().getName();
      if (targetName == null) {
        targetName = node.getComputationTarget().getUniqueId().toString();
      }
    }
    String targetType = getTargetTypeName(node.getComputationTarget().getType());
    String functionName = node.getFunction().getFunction().getFunctionDefinition().getShortName();   
    String displayProperties = getValuePropertiesForDisplay(valueSpecification.getProperties());
    
    row.put("rowId", rowId);
    if (parentRowId > -1) {
      row.put("parentRowId", parentRowId);
    }
    row.put("indent", indent);
    row.put("target", targetName);
    
    // These are static cell values which are not updated on each tick
    addCellValue(row, "targetType", targetType);
    addCellValue(row, "function", functionName);
    addCellValue(row, "valueName", valueSpecification.getValueName());
    if (displayProperties != null) {
      addCellValue(row, "properties", displayProperties);
    }
    return row;
  }
  
  private void addCellValue(Map<String, Object> row, String fieldName, Object fieldValue) {
    Map<String, Object> valueMap = new HashMap<String, Object>();
    valueMap.put("v", fieldValue);
    row.put(fieldName, valueMap);
  }
  
  //-------------------------------------------------------------------------

  public Map<String, Object> processViewCycle(ViewCycle viewCycle, Long resultTimestamp) {
    ComputationCacheResponse valueResponse = viewCycle.queryComputationCaches(_cacheQuery);
    Map<String, Object> rows = new HashMap<String, Object>();
    for (Pair<ValueSpecification, Object> valuePair : valueResponse.getResults()) {
      ValueSpecification specification = valuePair.getFirst();
      Object value = valuePair.getSecond();
      
      IntSet rowIds = _rowIdMap.get(specification);
      if (rowIds == null) {
        s_logger.warn("Cache query returned unexpected item with value specification {}", specification);
        continue;
      }

      ResultConverter<Object> converter = getConverter(value);
      if (converter != null && !_typedRows.contains(specification)) {
        _typedRows.add(specification);
        // TODO: same hack as other grids
        if (converter.getFormatterName().equals("DOUBLE")) {
          _historyOutputs.addAll(rowIds);
        }
      }
      for (int rowId : rowIds) {
        WebGridCell cell = WebGridCell.of(rowId, 0);
        Map<String, Object> cellValue = getCellValue(cell, specification, value, resultTimestamp, converter);
        if (cellValue != null) {
          if (converter != null) {
            cellValue.put("t", converter.getFormatterName());
          }
          Map<String, Object> cellData = new HashMap<String, Object>();
          cellData.put("rowId", rowId);
          cellData.put("0", cellValue);
          rows.put(Long.toString(rowId), cellData);
        }
      }
    }
    return rows;
  }
  
  private String getValuePropertiesForDisplay(ValueProperties properties) {
    StringBuilder sb = new StringBuilder();
    boolean isFirst = true;
    for (String property : properties.getProperties()) {
      if (ValuePropertyNames.FUNCTION.equals(property)) {
        continue;
      }
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append("; ");
      }
      sb.append(property).append("=");
      Set<String> propertyValues = properties.getValues(property);
      if (propertyValues.isEmpty()) {
        sb.append("*");
      } else {
        boolean isFirstValue = true;
        for (String value : propertyValues) {
          if (isFirstValue) {
            isFirstValue = false;
          } else {
            sb.append(", ");
          }
          sb.append(value);
        }
      }
    }
    return sb.length() == 0 ? null : sb.toString();
  }
  
  private String getTargetTypeName(ComputationTargetType targetType) {
    switch (targetType) {
      case PORTFOLIO_NODE:
        return "Agg";
      case POSITION:
        return "Pos";
      case SECURITY:
        return "Sec";
      case PRIMITIVE:
        return "Prim";
      default:
        return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  private ResultConverter<Object> getConverter(Object value) {
    if (value == null) {
      return null;
    }
    return (ResultConverter<Object>) getConverterCache().getConverterForType(value.getClass());
  }

  @Override
  protected boolean isHistoryOutput(WebGridCell cell) {
    return _historyOutputs.contains(cell.getRowId());
  }

  @Override
  protected List<Object> getInitialJsonRowStructures() {
    return _rowStructure;
  }

  @Override
  protected String[][] getRawDataColumnHeaders() {
    // TODO implement this if primitive dependency graphs are required
    return null;
  }

  @Override
  protected String[][] getRawDataRows(ViewComputationResultModel result) {
    // TODO implement this if primitive dependency graphs are required
    return null;
  }

}
