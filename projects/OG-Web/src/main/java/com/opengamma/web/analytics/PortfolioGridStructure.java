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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * The structure of the grid that displays portfolio data and analytics. Contains the column definitions and
 * the portfolio tree structure.
 */
public class PortfolioGridStructure extends MainGridStructure {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioGridStructure.class);

  /** The root node of the portfolio structure. */
  private final AnalyticsNode _rootNode;
  /** For mapping cells to values in the results. */
  private final ValueMappings _valueMappings;
  /** Definition of the view driving the grid. */
  private final ViewDefinition _viewDef;
  /** Number of exploded child columns for each column whose values can be exploded. */
  private final Map<ColumnSpecification, SortedSet<ColumnMeta>> _inlineColumnMeta;
  /** Rows in the grid. */
  private final List<PortfolioGridRow> _rows;

  /* package */ PortfolioGridStructure(List<PortfolioGridRow> rows,
                                       GridColumnGroup fixedColumns,
                                       GridColumnGroups nonFixedColumns,
                                       AnalyticsNode rootNode,
                                       TargetLookup targetLookup,
                                       ValueMappings valueMappings,
                                       ViewDefinition viewDef) {
    this(rows, fixedColumns, nonFixedColumns, rootNode, targetLookup, valueMappings, viewDef,
         Collections.<ColumnSpecification, SortedSet<ColumnMeta>>emptyMap());
  }

  /* package */ PortfolioGridStructure(List<PortfolioGridRow> rows,
                                       GridColumnGroup fixedColumns,
                                       GridColumnGroups nonFixedColumns,
                                       AnalyticsNode rootNode,
                                       TargetLookup targetLookup,
                                       ValueMappings valueMappings,
                                       ViewDefinition viewDef,
                                       Map<ColumnSpecification, SortedSet<ColumnMeta>> inlineColumnMeta) {
    super(fixedColumns, nonFixedColumns, targetLookup);
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(inlineColumnMeta, "inlineColumnCounts");
    _inlineColumnMeta = inlineColumnMeta;
    _rows = rows;
    _rootNode = rootNode;
    _valueMappings = valueMappings;
    _viewDef = viewDef;
  }

  /* package */ static PortfolioGridStructure create(Portfolio portfolio, ValueMappings valueMappings) {
    ArgumentChecker.notNull(valueMappings, "valueMappings");
    // TODO these can be empty, not used any more
    List<PortfolioGridRow> rows = buildRows(portfolio);
    TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    AnalyticsNode rootNode = AnalyticsNode.portoflioRoot(portfolio);
    return new PortfolioGridStructure(rows,
                                      GridColumnGroup.empty(),
                                      GridColumnGroups.empty(),
                                      rootNode,
                                      targetLookup,
                                      valueMappings,
                                      new ViewDefinition("empty", "dummy"));
  }

  /**
   * @return The root node of the portfolio structure.
   */
  public AnalyticsNode getRootNode() {
    return _rootNode;
  }

  /* package */ PortfolioGridStructure withUpdatedRows(Portfolio portfolio) {
    AnalyticsNode rootNode = AnalyticsNode.portoflioRoot(portfolio);
    List<PortfolioGridRow> rows = buildRows(portfolio);
    GridColumnGroup fixedColumns = buildFixedColumns(rows);
    TargetLookup targetLookup = new TargetLookup(_valueMappings, rows);
    List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(_viewDef, targetLookup);
    GridColumnGroups nonFixedColumns = new GridColumnGroups(analyticsColumns);
    return new PortfolioGridStructure(rows, fixedColumns, nonFixedColumns, rootNode, targetLookup, _valueMappings, _viewDef);
  }

  /* package */ PortfolioGridStructure withUpdatedStructure(CompiledViewDefinition compiledViewDef) {
    Portfolio portfolio = compiledViewDef.getPortfolio();
    AnalyticsNode rootNode = AnalyticsNode.portoflioRoot(portfolio);
    List<PortfolioGridRow> rows = buildRows(portfolio);
    GridColumnGroup fixedColumns = buildFixedColumns(rows);
    ValueMappings valueMappings = new ValueMappings(compiledViewDef);
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
      if (Inliner.isDisplayableInline(column.getType(), column.getSpecification())) {
        // order set of the union of the column metadata for the whole set. need this to figure out how many unique
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
    if (!inlineColumnMeta.equals(_inlineColumnMeta)) {
      List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(_viewDef, getTargetLookup(), inlineColumnMeta);
      return new PortfolioGridStructure(_rows,
                                        buildFixedColumns(_rows),
                                        new GridColumnGroups(analyticsColumns),
                                        _rootNode,
                                        getTargetLookup(),
                                        getValueMappings(),
                                        _viewDef,
                                        inlineColumnMeta);
    } else {
      return this;
    }
  }

  private Integer max(Integer count1, Integer count2) {
    if (count1 == null) {
      return count2;
    } else if (count2 == null) {
      return count1;
    } else {
      return Math.max(count1, count2);
    }
  }

  /* package */ static GridColumnGroup buildFixedColumns(List<PortfolioGridRow> rows) {
    GridColumn labelColumn = new GridColumn("Name", "", null, new PortfolioLabelRenderer(rows));
    // TODO make the quantity column optional until it's replaced with a function
    //GridColumn quantityColumn = new GridColumn("Quantity", "", BigDecimal.class, new QuantityRenderer(rows), null);
    //return new GridColumnGroup("fixed", ImmutableList.of(labelColumn, quantityColumn), false);
    return new GridColumnGroup("fixed", ImmutableList.of(labelColumn), false);
  }

  /* package */ static List<GridColumnGroup> buildAnalyticsColumns(ViewDefinition viewDef, TargetLookup targetLookup) {
    return buildAnalyticsColumns(viewDef, targetLookup, Collections.<ColumnSpecification, SortedSet<ColumnMeta>>emptyMap());
  }

  /**
   * @param viewDef The view definition
   * @return Columns for displaying calculated analytics data, one group per calculation configuration
   */
  /* package */
  static List<GridColumnGroup> buildAnalyticsColumns(ViewDefinition viewDef,
                                                     TargetLookup targetLookup,
                                                     Map<ColumnSpecification, SortedSet<ColumnMeta>> inlineColumnMeta) {
    List<GridColumnGroup> columnGroups = Lists.newArrayList();
    Set<ColumnSpecification> columnSpecs = Sets.newHashSet();
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      List<GridColumn> columns = Lists.newArrayList();
      for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
        String valueName = portfolioOutput.getFirst();
        Class<?> columnType = ValueTypes.getTypeForValueName(valueName);
        ValueProperties constraints = portfolioOutput.getSecond();
        ColumnSpecification columnSpec = new ColumnSpecification(calcConfig.getName(), valueName, constraints);
        // ensure columnSpec isn't a duplicate
        if (columnSpecs.add(columnSpec)) {
          SortedSet<ColumnMeta> meta = inlineColumnMeta.get(columnSpec);
          //if (true) { // column can't be inlined
          if (meta == null) { // column can't be inlined
            columns.add(GridColumn.forSpec(columnSpec, columnType, targetLookup));
          } else {
            int inlineIndex = 0;
            for (ColumnMeta columnMeta : meta) {
              String header;
              if (inlineIndex++ == 0) {
                header = columnSpec.getValueName() + " / " + columnMeta.getHeader();
              } else {
                header = columnMeta.getHeader();
              }
              columns.add(GridColumn.forSpec(header, columnSpec, columnType, targetLookup, columnMeta.getKey(), inlineIndex));
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

  /* package */ ValueMappings getValueMappings() {
    return _valueMappings;
  }
}
