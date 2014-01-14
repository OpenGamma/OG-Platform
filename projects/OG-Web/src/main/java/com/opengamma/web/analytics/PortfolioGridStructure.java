/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PortfolioMapper;
import com.opengamma.core.position.impl.PortfolioMapperFunction;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationConfiguration.MergedOutput;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * The structure of the grid that displays portfolio data and analytics. Contains the column definitions and
 * the portfolio tree structure.
 */
public class PortfolioGridStructure extends MainGridStructure {

  /** Definition of the view driving the grid. */
  private final ViewDefinition _viewDef;
  /** Meta data for exploded child columns, keyed by the specification of the parent column. */
  private final Map<ColumnSpecification, SortedSet<ColumnMeta>> _inlineColumnMeta;
  /** Rows in the grid. */
  private final List<PortfolioGridRow> _rows;

  /* package */ PortfolioGridStructure(List<PortfolioGridRow> rows,
                                       GridColumnGroup fixedColumns,
                                       GridColumnGroups nonFixedColumns,
                                       AnalyticsNode rootNode,
                                       TargetLookup targetLookup,
                                       UnversionedValueMappings valueMappings,
                                       ViewDefinition viewDef) {
    this(rows, fixedColumns, nonFixedColumns, rootNode, targetLookup, valueMappings, viewDef,
         Collections.<ColumnSpecification, SortedSet<ColumnMeta>>emptyMap());
  }

  /* package */ PortfolioGridStructure(List<PortfolioGridRow> rows,
                                       GridColumnGroup fixedColumns,
                                       GridColumnGroups nonFixedColumns,
                                       AnalyticsNode rootNode,
                                       TargetLookup targetLookup,
                                       UnversionedValueMappings valueMappings,
                                       ViewDefinition viewDef,
                                       Map<ColumnSpecification, SortedSet<ColumnMeta>> inlineColumnMeta) {
    super(fixedColumns, nonFixedColumns, targetLookup, rootNode, valueMappings);
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(inlineColumnMeta, "inlineColumnCounts");
    _inlineColumnMeta = inlineColumnMeta;
    _rows = rows;
    _viewDef = viewDef;
  }

  /* package */ static PortfolioGridStructure create(Portfolio portfolio, UnversionedValueMappings valueMappings) {
    ArgumentChecker.notNull(valueMappings, "valueMappings");
    // TODO these can be empty, not used any more
    List<PortfolioGridRow> rows = buildRows(portfolio);
    TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    AnalyticsNode rootNode = AnalyticsNode.portfolioRoot(portfolio);
    return new PortfolioGridStructure(rows,
                                      GridColumnGroup.empty(),
                                      GridColumnGroups.empty(),
                                      rootNode,
                                      targetLookup,
                                      valueMappings,
                                      new ViewDefinition("empty", "dummy"));
  }

  /* package */ PortfolioGridStructure withUpdatedRows(Portfolio portfolio) {
    AnalyticsNode rootNode = AnalyticsNode.portfolioRoot(portfolio);
    List<PortfolioGridRow> rows = buildRows(portfolio);
    GridColumnGroup fixedColumns = buildFixedColumns(rows);
    TargetLookup targetLookup = new TargetLookup(super.getValueMappings(), rows);
    List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(_viewDef, targetLookup);
    GridColumnGroups nonFixedColumns = new GridColumnGroups(analyticsColumns);
    return new PortfolioGridStructure(rows, fixedColumns, nonFixedColumns, rootNode, targetLookup,
                                      super.getValueMappings(), _viewDef);
  }

  /* package */ PortfolioGridStructure withUpdatedStructure(CompiledViewDefinition compiledViewDef, Portfolio portfolio) {
    AnalyticsNode rootNode = AnalyticsNode.portfolioRoot(portfolio);
    List<PortfolioGridRow> rows = buildRows(portfolio);
    GridColumnGroup fixedColumns = buildFixedColumns(rows);
    UnversionedValueMappings valueMappings = new UnversionedValueMappings(compiledViewDef);
    TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    ViewDefinition viewDef = compiledViewDef.getViewDefinition();
    List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(viewDef, targetLookup);
    GridColumnGroups nonFixedColumns = new GridColumnGroups(analyticsColumns);
    return new PortfolioGridStructure(rows, fixedColumns, nonFixedColumns, rootNode, targetLookup, valueMappings, viewDef);
  }

  /* package */ PortfolioGridStructure withUpdatedStructure(ResultsCache cache) {
    Map<ColumnSpecification, SortedSet<ColumnMeta>> inlineColumnMeta = Maps.newHashMap();
    for (GridColumn column : getColumnStructure().getColumns()) {
      ColumnSpecification colSpec = column.getSpecification();
      if (Inliner.isDisplayableInline(column.getUnderlyingType(), column.getSpecification())) {
        // ordered set of the union of the column metadata for the whole set. need this to figure out how many unique
        // columns are required
        SortedSet<ColumnMeta> allColumnMeta = Sets.newTreeSet();
        // traverse every result in the column and get the column metadata
        for (Iterator<Pair<String, ValueSpecification>> it = getTargetLookup().getTargetsForColumn(colSpec); it.hasNext(); ) {
          Pair<String, ValueSpecification> target = it.next();
          if (target != null) {
            ResultsCache.Result result = cache.getResult(target.getFirst(), target.getSecond(), column.getType());
            Object value = result.getValue();
            allColumnMeta.addAll(Inliner.columnMeta(value));
          }
        }
        if (!allColumnMeta.isEmpty()) {
          inlineColumnMeta.put(colSpec, allColumnMeta);
        }
      }
    }
    // TODO implement equals() and always return a new instance? conceptually a bit neater but less efficient
    if (!inlineColumnMeta.equals(_inlineColumnMeta)) {
      List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(_viewDef, getTargetLookup(), inlineColumnMeta);
      return new PortfolioGridStructure(_rows,
                                        buildFixedColumns(_rows),
                                        new GridColumnGroups(analyticsColumns),
                                        getRootNode(),
                                        getTargetLookup(),
                                        getValueMappings(),
                                        _viewDef,
                                        inlineColumnMeta);
    } else {
      return this;
    }
  }

  /* package */ PortfolioGridStructure withNode(AnalyticsNode node) {
    return new PortfolioGridStructure(_rows, getFixedColumns(), getNonFixedColumns(), node, getTargetLookup(),
                                      super.getValueMappings(), _viewDef);
  }

  /* package */ static GridColumnGroup buildFixedColumns(List<PortfolioGridRow> rows) {
    GridColumn labelColumn = new GridColumn("Name", "", null, new PortfolioLabelRenderer(rows));
    return new GridColumnGroup("fixed", ImmutableList.of(labelColumn), false);
  }

  /* package */ static List<GridColumnGroup> buildAnalyticsColumns(ViewDefinition viewDef, TargetLookup targetLookup) {
    return buildAnalyticsColumns(viewDef, targetLookup, Collections.<ColumnSpecification, SortedSet<ColumnMeta>>emptyMap());
  }

  /**
   * @param viewDef The view definition
   * @return Columns for displaying calculated analytics data, one group per calculation configuration
   */
  /* package */ static List<GridColumnGroup> buildAnalyticsColumns(ViewDefinition viewDef,
                                                                   TargetLookup targetLookup,
                                                                   Map<ColumnSpecification, SortedSet<ColumnMeta>> inlineColumnMeta) {
    List<GridColumnGroup> columnGroups = Lists.newArrayList();
    Set<Triple<String, String, ValueProperties>> columnSpecs = Sets.newHashSet();
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      List<ColumnSpecification> allSpecs = Lists.newArrayList();
      for (ViewCalculationConfiguration.Column column : calcConfig.getColumns()) {
        allSpecs.add(new ColumnSpecification(calcConfig.getName(),
                                             column.getValueName(),
                                             column.getProperties(),
                                             column.getHeader()));
      }
      for (Pair<String, ValueProperties> output : calcConfig.getAllPortfolioRequirements()) {
        allSpecs.add(new ColumnSpecification(calcConfig.getName(), output.getFirst(), output.getSecond()));
      }
      for (MergedOutput output : calcConfig.getMergedOutputs()) {
        ValueProperties constraints = ValueProperties.with(ValuePropertyNames.NAME, output.getMergedOutputName()).get();
        allSpecs.add(new ColumnSpecification(calcConfig.getName(), ValueRequirementNames.MERGED_OUTPUT, constraints, output.getMergedOutputName()));
      }
      List<GridColumn> columns = Lists.newArrayList();
      for (ColumnSpecification columnSpec : allSpecs) {
        Class<?> columnType = ValueTypes.getTypeForValueName(columnSpec.getValueName());
        // ensure column isn't a duplicate. can't use a set of col specs because we need to treat columns as duplicates
        // even if they have different headers
        if (columnSpecs.add(Triple.of(columnSpec.getCalcConfigName(), columnSpec.getValueName(), columnSpec.getValueProperties()))) {
          SortedSet<ColumnMeta> meta = inlineColumnMeta.get(columnSpec);
          if (meta == null) { // column can't be inlined
            columns.add(GridColumn.forSpec(columnSpec, columnType, targetLookup));
          } else {
            int inlineIndex = 0;
            for (ColumnMeta columnMeta : meta) {
              String header;
              if (inlineIndex++ == 0) {
                header = columnSpec.getHeader() + " / " + columnMeta.getHeader();
              } else {
                header = columnMeta.getHeader();
              }
              columns.add(GridColumn.forSpec(header,
                                             columnSpec,
                                             columnMeta.getType(),
                                             columnMeta.getUnderlyingType(),
                                             targetLookup,
                                             columnMeta.getKey(),
                                             inlineIndex));
            }
          }
        }
      }
      if (!columns.isEmpty()) {
        columnGroups.add(new GridColumnGroup(calcConfig.getName(), columns, true));
      }
    }
    return columnGroups;
  }

  /* package */ static List<PortfolioGridRow> buildRows(final Portfolio portfolio) {
    if (portfolio == null) {
      return Collections.emptyList();
    }
    PortfolioMapperFunction<List<PortfolioGridRow>> targetFn = new PortfolioMapperFunction<List<PortfolioGridRow>>() {

      @Override
      public List<PortfolioGridRow> apply(PortfolioNode node) {
        ComputationTargetSpecification target =
            new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, node.getUniqueId());
        String nodeName;
        // if the parent ID is null it's the root node
        if (node.getParentNodeId() == null) {
          // the root node is called "Root" which isn't any use for displaying in the UI, use the portfolio name instead
          nodeName = portfolio.getName();
        } else {
          nodeName = node.getName();
        }
        return Lists.newArrayList(new PortfolioGridRow(target, nodeName, node.getUniqueId()));
      }

      @Override
      public List<PortfolioGridRow> apply(PortfolioNode parentNode, Position position) {
        ComputationTargetSpecification nodeSpec = ComputationTargetSpecification.of(parentNode);
        // TODO I don't think toLatest() will do long term. resolution time available on the result model
        UniqueId positionId = position.getUniqueId();
        ComputationTargetSpecification target = nodeSpec.containing(ComputationTargetType.POSITION,
                                                                    positionId.toLatest());
        Security security = position.getSecurity();
        List<PortfolioGridRow> rows = Lists.newArrayList();
        UniqueId nodeId = parentNode.getUniqueId();
        if (isFungible(position.getSecurity())) {
          rows.add(new PortfolioGridRow(target, security.getName(), security.getUniqueId(), nodeId, positionId));
          for (Trade trade : position.getTrades()) {
            String tradeDate = trade.getTradeDate().toString();
            rows.add(new PortfolioGridRow(ComputationTargetSpecification.of(trade),
                                          tradeDate,
                                          security.getUniqueId(),
                                          nodeId,
                                          positionId,
                                          trade.getUniqueId()));
          }
        } else {
          Collection<Trade> trades = position.getTrades();
          if (trades.isEmpty()) {
            rows.add(new PortfolioGridRow(target, security.getName(), security.getUniqueId(), nodeId, positionId));
          } else {
            // there is never more than one trade on a position in an OTC security
            UniqueId tradeId = trades.iterator().next().getUniqueId();
            rows.add(new PortfolioGridRow(target, security.getName(), security.getUniqueId(), nodeId, positionId, tradeId));
          }
        }
        return rows;
      }
    };
    List<List<PortfolioGridRow>> rows = PortfolioMapper.map(portfolio.getRootNode(), targetFn);
    Iterable<PortfolioGridRow> flattenedRows = Iterables.concat(rows);
    return Lists.newArrayList(flattenedRows);
  }

  /**
   * @param security A security
   * @return true if the security is fungible, false if OTC
   */
  private static boolean isFungible(Security security) {
    if (security instanceof FinancialSecurity) {
      return !((FinancialSecurity) security).accept(new OtcSecurityVisitor());
    } else {
      return false;
    }
  }
}
