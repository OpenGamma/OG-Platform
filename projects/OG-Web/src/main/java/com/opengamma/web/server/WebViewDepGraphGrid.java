/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.cycle.ComputationCacheResponse;
import com.opengamma.engine.view.cycle.ComputationCycleQuery;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.conversion.ResultConverter;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 * Represents a dependency graph grid. This is slightly special since, unlike the other grids, the columns are known statically and each value may differ in type.
 */
public class WebViewDepGraphGrid extends WebViewGrid {

  private static final Logger s_logger = LoggerFactory.getLogger(WebViewDepGraphGrid.class);

  private static final ComputationTargetTypeMap<String> TARGET_TYPE_NAMES = createTargetTypeNames();

  private final AtomicBoolean _init = new AtomicBoolean();
  private final IntSet _historyOutputs = new IntOpenHashSet();
  private final WebGridCell _parentGridCell;
  private final String _parentCalcConfigName;
  private final ValueSpecification _parentValueSpecification;
  private final ComputationTargetResolver _computationTargetResolver;
  private final Set<ValueSpecification> _typedRows = new HashSet<ValueSpecification>();
  private Map<ValueSpecification, IntSet> _rowIdMap;
  private List<Object> _rowStructure;
  private ComputationCycleQuery _cacheQuery;

  private static ComputationTargetTypeMap<String> createTargetTypeNames() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.PORTFOLIO_NODE, "Agg");
    map.put(ComputationTargetType.POSITION, "Pos");
    map.put(ComputationTargetType.SECURITY, "Sec");
    map.put(ComputationTargetType.ANYTHING, "Prim");
    map.put(ComputationTargetType.NULL, "Prim");
    map.put(ComputationTargetType.TRADE, "Trade");
    return map;
  }

  protected WebViewDepGraphGrid(final String name, final ViewClient viewClient, final ResultConverterCache resultConverterCache,
      final LocalSession local, final ServerSession remote, final WebGridCell parentGridCell, final String parentCalcConfigName,
      final ValueSpecification parentValueSpecification, final ComputationTargetResolver computationTargetResolver) {
    super(name, viewClient, resultConverterCache, local, remote);
    _parentGridCell = parentGridCell;
    _parentCalcConfigName = parentCalcConfigName;
    _parentValueSpecification = parentValueSpecification;
    _computationTargetResolver = computationTargetResolver;
  }

  //-------------------------------------------------------------------------
  /*package*/boolean isInit() {
    return _init.get();
  }

  /*package*/boolean init(final DependencyGraph depGraph, final String calcConfigName, final ValueSpecification valueSpecification) {
    if (!_init.compareAndSet(false, true)) {
      return false;
    }

    try {
      final HashMap<ValueSpecification, IntSet> rowIdMap = new HashMap<ValueSpecification, IntSet>();
      _rowStructure = generateRowStructure(depGraph, valueSpecification, rowIdMap);
      _rowIdMap = rowIdMap;

      _cacheQuery = new ComputationCycleQuery();
      _cacheQuery.setCalculationConfigurationName(calcConfigName);
      _cacheQuery.setValueSpecifications(new HashSet<ValueSpecification>(_rowIdMap.keySet()));
    } catch (final Exception e) {
      s_logger.error("Exception initialising dependency graph grid", e);
      _init.set(false);
      return false;
    }

    return true;
  }

  /*package*/WebGridCell getParentGridCell() {
    return _parentGridCell;
  }

  /*package*/String getParentCalcConfigName() {
    return _parentCalcConfigName;
  }

  /*package*/ValueSpecification getParentValueSpecification() {
    return _parentValueSpecification;
  }

  private ComputationTargetResolver getComputationTargetResolver() {
    return _computationTargetResolver;
  }

  private List<Object> generateRowStructure(final DependencyGraph depGraph, final ValueSpecification output, final Map<ValueSpecification, IntSet> rowIdMap) {
    final List<Object> rowStructure = new ArrayList<Object>();
    addRowIdAssociation(0, output, rowIdMap);
    rowStructure.add(getJsonRowStructure(depGraph.getNodeProducing(output), output, -1, 0, 0));
    addInputRowStructures(depGraph, depGraph.getNodeProducing(output), rowIdMap, rowStructure, 1, 0, 1);
    return rowStructure;
  }

  private int addInputRowStructures(final DependencyGraph graph, final DependencyNode node, final Map<ValueSpecification, IntSet> rowIdMap, final List<Object> rowStructure, final int indent,
      final int parentRowId, int nextRowId) {
    for (final ValueSpecification inputValue : node.getInputValues()) {
      final DependencyNode inputNode = graph.getNodeProducing(inputValue);
      final int rowId = nextRowId++;
      addRowIdAssociation(rowId, inputValue, rowIdMap);
      rowStructure.add(getJsonRowStructure(inputNode, inputValue, parentRowId, rowId, indent));
      nextRowId = addInputRowStructures(graph, inputNode, rowIdMap, rowStructure, indent + 1, rowId, nextRowId);
    }
    return nextRowId;
  }

  private void addRowIdAssociation(final int rowId, final ValueSpecification specification, final Map<ValueSpecification, IntSet> rowIdMap) {
    IntSet rowIdSet = rowIdMap.get(specification);
    if (rowIdSet == null) {
      rowIdSet = new IntArraySet();
      rowIdMap.put(specification, rowIdSet);
    }
    rowIdSet.add(rowId);
  }

  private String getTargetName(final ComputationTargetSpecification targetSpec) {
    final ComputationTarget target = getComputationTargetResolver().resolve(targetSpec, VersionCorrection.LATEST);
    if (target != null) {
      return target.getName();
    } else {
      final UniqueId uid = targetSpec.getUniqueId();
      if (uid != null) {
        return uid.toString();
      } else {
        return targetSpec.getType().toString();
      }
    }
  }

  private Object getJsonRowStructure(final DependencyNode node, final ValueSpecification valueSpecification, final long parentRowId, final long rowId, final int indent) {
    ArgumentChecker.notNull(node, "node");
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");

    final Map<String, Object> row = new HashMap<String, Object>();
    final String targetName = getTargetName(node.getComputationTarget());
    final String targetType = TARGET_TYPE_NAMES.get(node.getComputationTarget().getType());
    final String functionName = node.getFunction().getFunction().getFunctionDefinition().getShortName();
    final String displayProperties = getValuePropertiesForDisplay(valueSpecification.getProperties());

    row.put("rowId", rowId);
    if (parentRowId > -1) {
      row.put("parentRowId", parentRowId);
    }
    row.put("indent", indent);
    row.put("target", targetName);

    // These are static cell values which are not updated on each tick
    addCellValue(row, "targetType", targetType);
    addCellValue(row, "function", functionName);
    addCellValue(row, "valueName", valueSpecification.getValueName().toString());
    if (displayProperties != null) {
      addCellValue(row, "properties", displayProperties);
    }
    return row;
  }

  private void addCellValue(final Map<String, Object> row, final String fieldName, final Object fieldValue) {
    final Map<String, Object> valueMap = new HashMap<String, Object>();
    valueMap.put("v", fieldValue);
    row.put(fieldName, valueMap);
  }

  //-------------------------------------------------------------------------

  public Map<String, Object> processViewCycle(final ViewCycle viewCycle, final Long resultTimestamp) {
    final ComputationCacheResponse valueResponse = viewCycle.queryComputationCaches(_cacheQuery);
    final Map<String, Object> rows = new HashMap<String, Object>();
    for (final Pair<ValueSpecification, Object> valuePair : valueResponse.getResults()) {
      final ValueSpecification specification = valuePair.getFirst();
      final Object value = valuePair.getSecond();

      final IntSet rowIds = _rowIdMap.get(specification);
      if (rowIds == null) {
        s_logger.warn("Cache query returned unexpected item with value specification {}", specification);
        continue;
      }

      final ResultConverter<Object> converter = getConverter(value);
      if (converter != null && !_typedRows.contains(specification)) {
        _typedRows.add(specification);
        // TODO: same hack as other grids
        if (converter.getFormatterName().equals("DOUBLE")) {
          _historyOutputs.addAll(rowIds);
        }
      }
      for (final int rowId : rowIds) {
        final WebGridCell cell = WebGridCell.of(rowId, 0);
        final Map<String, Object> cellValue = processCellValue(cell, specification, value, resultTimestamp, converter);
        if (cellValue != null) {
          if (converter != null) {
            cellValue.put("t", converter.getFormatterName());
          }
          final Map<String, Object> cellData = new HashMap<String, Object>();
          cellData.put("rowId", rowId);
          cellData.put("0", cellValue);
          rows.put(Long.toString(rowId), cellData);
        }
      }
    }
    return rows;
  }

  private String getValuePropertiesForDisplay(final ValueProperties properties) {
    final StringBuilder sb = new StringBuilder();
    boolean isFirst = true;
    for (final String property : properties.getProperties()) {
      if (ValuePropertyNames.FUNCTION.equals(property)) {
        continue;
      }
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append("; ");
      }
      sb.append(property).append("=");
      final Set<String> propertyValues = properties.getValues(property);
      if (propertyValues.isEmpty()) {
        sb.append("*");
      } else {
        boolean isFirstValue = true;
        for (final String value : propertyValues) {
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

  @SuppressWarnings("unchecked")
  private ResultConverter<Object> getConverter(final Object value) {
    if (value == null) {
      return null;
    }
    return (ResultConverter<Object>) getConverterCache().getConverterForType(value.getClass());
  }

  @Override
  protected boolean isHistoryOutput(final WebGridCell cell) {
    return _historyOutputs.contains(cell.getRowId());
  }

  @Override
  protected List<Object> getInitialJsonRowStructures() {
    return _rowStructure;
  }

  @Override
  protected String[][] getCsvColumnHeaders() {
    return null;
  }

  @Override
  protected String[][] getCsvRows(final ViewComputationResultModel result) {
    return null;
  }

}
