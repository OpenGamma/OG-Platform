/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.blotter.BlotterColumn;
import com.opengamma.web.analytics.blotter.BlotterColumnMapper;

/**
 *
 */
public class BlotterGridStructure extends PortfolioGridStructure {

  private final BlotterColumnMapper _columnMapper;

  /* package */ BlotterGridStructure(GridColumnGroups columnGroups,
                                     AnalyticsNode rootNode,
                                     List<PortfolioGridRow> rows,
                                     TargetLookup targetLookup,
                                     BlotterColumnMapper columnMapper,
                                     ValueMappings valueMappings) {
    super(columnGroups, rootNode, rows, targetLookup, valueMappings);
    ArgumentChecker.notNull(columnMapper, "columnMapper");
    _columnMapper = columnMapper;
  }

  /* package */ static BlotterGridStructure create(Portfolio portfolio, BlotterColumnMapper columnMapper) {
    List<PortfolioGridRow> rows = buildRows(portfolio);
    ValueMappings valueMappings = new ValueMappings();
    TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    return new BlotterGridStructure(GridColumnGroups.empty(),
                                    AnalyticsNode.portoflioRoot(portfolio),
                                    rows,
                                    targetLookup,
                                    columnMapper,
                                    valueMappings);
  }


  private GridColumnGroup buildBlotterColumns() {
    List<GridColumn> columns = Lists.newArrayList(
        blotterColumn(BlotterColumn.TYPE, _columnMapper, getRows()),
        blotterColumn(BlotterColumn.PRODUCT, _columnMapper, getRows()),
        blotterColumn(BlotterColumn.QUANTITY, _columnMapper, getRows()),
        blotterColumn(BlotterColumn.DIRECTION, _columnMapper, getRows()),
        blotterColumn(BlotterColumn.START, _columnMapper, getRows()),
        blotterColumn(BlotterColumn.MATURITY, _columnMapper, getRows()),
        blotterColumn(BlotterColumn.RATE, _columnMapper, getRows()),
        blotterColumn(BlotterColumn.INDEX, _columnMapper, getRows()),
        blotterColumn(BlotterColumn.FREQUENCY, _columnMapper, getRows()),
        blotterColumn(BlotterColumn.FLOAT_FREQUENCY, _columnMapper, getRows()));
    return new GridColumnGroup("Blotter", columns, false);
  }

  private static GridColumn blotterColumn(BlotterColumn column,
                                          BlotterColumnMapper columnMappings,
                                          List<PortfolioGridRow> rows) {
    return new GridColumn(column.getName(), "", String.class, new BlotterColumnRenderer(column, columnMappings, rows));
  }

  @Override
  BlotterGridStructure withUpdatedRows(Portfolio portfolio) {
    AnalyticsNode rootNode = AnalyticsNode.portoflioRoot(portfolio);
    List<PortfolioGridRow> rows = buildRows(portfolio);
    TargetLookup targetLookup = new TargetLookup(getValueMappings(), rows);
    return new BlotterGridStructure(getColumnStructure(), rootNode, rows, targetLookup, _columnMapper, getValueMappings());
  }

  @Override
  BlotterGridStructure withUpdatedColumns(CompiledViewDefinition compiledViewDef) {
    GridColumnGroup fixedColumns = buildFixedColumns(getRows());
    GridColumnGroup blotterColumns = buildBlotterColumns();
    ValueMappings valueMappings = new ValueMappings(compiledViewDef);
    TargetLookup targetLookup = new TargetLookup(valueMappings, getRows());
    List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(compiledViewDef.getViewDefinition(), targetLookup);
    List<GridColumnGroup> groups = Lists.newArrayList(fixedColumns);
    groups.add(blotterColumns);
    groups.addAll(analyticsColumns);
    GridColumnGroups columnGroups = new GridColumnGroups(groups);
    return new BlotterGridStructure(columnGroups, getRootNode(), getRows(), targetLookup, _columnMapper, valueMappings);
  }
}
