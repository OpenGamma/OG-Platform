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


  private static GridColumnGroup buildBlotterColumns(BlotterColumnMapper columnMapper, List<PortfolioGridRow> rows) {
    List<GridColumn> columns = Lists.newArrayList(
        blotterColumn(BlotterColumn.TYPE, columnMapper, rows),
        blotterColumn(BlotterColumn.PRODUCT, columnMapper, rows),
        blotterColumn(BlotterColumn.QUANTITY, columnMapper, rows),
        blotterColumn(BlotterColumn.DIRECTION, columnMapper, rows),
        blotterColumn(BlotterColumn.START, columnMapper, rows),
        blotterColumn(BlotterColumn.MATURITY, columnMapper, rows),
        blotterColumn(BlotterColumn.RATE, columnMapper, rows),
        blotterColumn(BlotterColumn.INDEX, columnMapper, rows),
        blotterColumn(BlotterColumn.FREQUENCY, columnMapper, rows),
        blotterColumn(BlotterColumn.FLOAT_FREQUENCY, columnMapper, rows));
    return new GridColumnGroup("Blotter", columns, false);
  }

  private static GridColumn blotterColumn(BlotterColumn column,
                                          BlotterColumnMapper columnMappings,
                                          List<PortfolioGridRow> rows) {
    return new GridColumn(column.getName(), "", String.class, new BlotterColumnRenderer(column, columnMappings, rows));
  }

  // TODO get rid of this, not sure it works
  @Override
  /* package */ BlotterGridStructure withUpdatedRows(Portfolio portfolio) {
    AnalyticsNode rootNode = AnalyticsNode.portoflioRoot(portfolio);
    List<PortfolioGridRow> rows = buildRows(portfolio);
    TargetLookup targetLookup = new TargetLookup(getValueMappings(), rows);
    return new BlotterGridStructure(getColumnStructure(), rootNode, rows, targetLookup, _columnMapper, getValueMappings());
  }

  @Override
  /* package */ BlotterGridStructure withUpdatedStructure(CompiledViewDefinition compiledViewDef) {
    Portfolio portfolio = compiledViewDef.getPortfolio();
    AnalyticsNode rootNode = AnalyticsNode.portoflioRoot(portfolio);
    List<PortfolioGridRow> rows = buildRows(portfolio);
    ValueMappings valueMappings = new ValueMappings(compiledViewDef);
    TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(compiledViewDef.getViewDefinition(), targetLookup);
    GridColumnGroup fixedColumns = buildFixedColumns(rows);
    GridColumnGroup blotterColumns = buildBlotterColumns(_columnMapper, rows);
    List<GridColumnGroup> groups = Lists.newArrayList(fixedColumns);
    groups.add(blotterColumns);
    groups.addAll(analyticsColumns);
    GridColumnGroups columnGroups = new GridColumnGroups(groups);
    return new BlotterGridStructure(columnGroups, rootNode, rows, targetLookup, _columnMapper, valueMappings);
  }

  /* package */ /*BlotterGridStructure withUpdatedStructure(Portfolio portfolio) {

  }*/
}
