/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.financial.security.lookup.SecurityAttribute;
import com.opengamma.financial.security.lookup.SecurityAttributeMapper;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class BlotterGridStructure extends PortfolioGridStructure {

  /** Maps the shared blotter columns to properties in the different security types. */
  private final SecurityAttributeMapper _columnMapper;
  /** The view definition that's driving the grid. */
  private final ViewDefinition _viewDef;

  /* package */ BlotterGridStructure(List<PortfolioGridRow> rows,
                                     GridColumnGroup fixedColumns,
                                     GridColumnGroup blotterColumns,
                                     List<GridColumnGroup> analyticsColumns,
                                     AnalyticsNode rootNode,
                                     TargetLookup targetLookup,
                                     SecurityAttributeMapper columnMapper,
                                     UnversionedValueMappings valueMappings,
                                     ViewDefinition viewDef) {
    super(rows, fixedColumns, createGroups(blotterColumns, analyticsColumns), rootNode, targetLookup, valueMappings, viewDef);
    ArgumentChecker.notNull(columnMapper, "columnMapper");
    ArgumentChecker.notNull(viewDef, "viewDef");
    _viewDef = viewDef;
    _columnMapper = columnMapper;
  }

  private static GridColumnGroups createGroups(GridColumnGroup blotterColumns, List<GridColumnGroup> analyticsColumns) {
    List<GridColumnGroup> groups = Lists.newArrayList(blotterColumns);
    groups.addAll(analyticsColumns);
    return new GridColumnGroups(groups);
  }

  /* package */ static BlotterGridStructure create(Portfolio portfolio, SecurityAttributeMapper columnMapper) {
    List<PortfolioGridRow> rows = buildRows(portfolio);
    UnversionedValueMappings valueMappings = new UnversionedValueMappings();
    TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    return new BlotterGridStructure(rows,
                                    GridColumnGroup.empty(),
                                    GridColumnGroup.empty(),
                                    Collections.<GridColumnGroup>emptyList(),
                                    AnalyticsNode.portfolioRoot(portfolio),
                                    targetLookup,
                                    columnMapper,
                                    valueMappings,
                                    new ViewDefinition("empty", "dummy"));
  }


  private static GridColumnGroup buildBlotterColumns(SecurityAttributeMapper columnMapper, List<PortfolioGridRow> rows) {
    GridColumn quantityColumn = new GridColumn(SecurityAttribute.QUANTITY.getName(), "", Double.class,
                                               new BlotterColumnRenderer(SecurityAttribute.QUANTITY, columnMapper, rows));
    List<GridColumn> columns = Lists.newArrayList(
        blotterColumn(SecurityAttribute.TYPE, columnMapper, rows),
        blotterColumn(SecurityAttribute.PRODUCT, columnMapper, rows),
        quantityColumn,
        blotterColumn(SecurityAttribute.DIRECTION, columnMapper, rows),
        blotterColumn(SecurityAttribute.START, columnMapper, rows),
        blotterColumn(SecurityAttribute.MATURITY, columnMapper, rows),
        blotterColumn(SecurityAttribute.RATE, columnMapper, rows),
        blotterColumn(SecurityAttribute.INDEX, columnMapper, rows),
        blotterColumn(SecurityAttribute.FREQUENCY, columnMapper, rows),
        blotterColumn(SecurityAttribute.FLOAT_FREQUENCY, columnMapper, rows));
    return new GridColumnGroup("Blotter", columns, false);
  }

  private static GridColumn blotterColumn(SecurityAttribute column,
                                          SecurityAttributeMapper columnMappings,
                                          List<PortfolioGridRow> rows) {
    return new GridColumn(column.getName(), "", String.class, new BlotterColumnRenderer(column, columnMappings, rows));
  }

  // TODO combine with the method below
  @Override
  /* package */ BlotterGridStructure withUpdatedRows(Portfolio portfolio) {
    AnalyticsNode rootNode = AnalyticsNode.portfolioRoot(portfolio);
    List<PortfolioGridRow> rows = buildRows(portfolio);
    TargetLookup targetLookup = new TargetLookup(getValueMappings(), rows);
    GridColumnGroup fixedColumns = buildFixedColumns(rows);
    GridColumnGroup blotterColumns = buildBlotterColumns(_columnMapper, rows);
    List<GridColumnGroup> analyticsColumns = Collections.emptyList();
    //List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(_viewDef, targetLookup);
    return new BlotterGridStructure(rows, fixedColumns, blotterColumns, analyticsColumns, rootNode, targetLookup,
                                    _columnMapper, getValueMappings(), _viewDef);
  }

  @Override
  /* package */ BlotterGridStructure withUpdatedStructure(CompiledViewDefinition compiledViewDef, Portfolio portfolio) {
    AnalyticsNode rootNode = AnalyticsNode.portfolioRoot(portfolio);
    List<PortfolioGridRow> rows = buildRows(portfolio);
    UnversionedValueMappings valueMappings = new UnversionedValueMappings(compiledViewDef);
    TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    ViewDefinition viewDef = compiledViewDef.getViewDefinition();
    List<GridColumnGroup> analyticsColumns = Collections.emptyList();
    //List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(viewDef, targetLookup);
    GridColumnGroup fixedColumns = buildFixedColumns(rows);
    GridColumnGroup blotterColumns = buildBlotterColumns(_columnMapper, rows);
    return new BlotterGridStructure(rows, fixedColumns, blotterColumns, analyticsColumns, rootNode, targetLookup,
                                    _columnMapper, valueMappings, viewDef);
  }

  // TODO handle inlining of values into columns
  @Override
  PortfolioGridStructure withUpdatedStructure(ResultsCache cache) {
    return this;
  }
}
