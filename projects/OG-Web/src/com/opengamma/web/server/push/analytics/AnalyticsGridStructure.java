/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PortfolioMapper;
import com.opengamma.core.position.impl.PortfolioMapperFunction;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class AnalyticsGridStructure {

  private final int _columnCount;

  private final List<AnalyticsColumnGroup> _columnGroups;
  private final AnalyticsNode _root;
  // TODO this is only for the main grids
  private final List<Row> _rows;
  // TODO persistent row IDs that can be tracked when the structure changes (for dynamic reaggregation)

  private AnalyticsGridStructure(AnalyticsNode root, List<AnalyticsColumnGroup> columnGroups, List<Row> rows) {
    ArgumentChecker.notNull(root, "root");
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(columnGroups, "columnGroups");
    _root = root;
    _rows = rows;
    int columnCount = 0;
    for (AnalyticsColumnGroup group : columnGroups) {
      columnCount += group.getColumnCount();
    }
    _columnCount = columnCount;
    _columnGroups = ImmutableList.copyOf(columnGroups);
  }

  public static AnalyticsGridStructure empty() {
    return new AnalyticsGridStructure(AnalyticsNode.emptyRoot(),
                                      Collections.<AnalyticsColumnGroup>emptyList(),
                                      Collections.<Row>emptyList());
  }

  public static AnalyticsGridStructure portoflio(CompiledViewDefinition compiledViewDef, List<AnalyticsColumnGroup> columnGroups) {
    return new AnalyticsGridStructure(AnalyticsNode.portoflioRoot(compiledViewDef), columnGroups, portfolioRows(compiledViewDef));
  }

  public static AnalyticsGridStructure primitives(CompiledViewDefinition compiledViewDef, List<AnalyticsColumnGroup> columnGroups) {
    List<Row> rows = primitivesRows(compiledViewDef);
    return new AnalyticsGridStructure(AnalyticsNode.primitivesRoot(rows.size()), columnGroups, rows);
  }

  public static AnalyticsGridStructure depdencyGraph(CompiledViewDefinition compiledViewDef) {
    throw new UnsupportedOperationException("not implemented");
  }

  private static List<Row> portfolioRows(CompiledViewDefinition viewDef) {
    PortfolioMapperFunction<Row> targetFn = new PortfolioMapperFunction<Row>() {
      @Override
      public Row apply(PortfolioNode node) {
        ComputationTargetSpecification target =
            new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, node.getUniqueId());
        return new Row(target, node.getName());
      }

      @Override
      public Row apply(Position position) {
        ComputationTargetSpecification target =
            new ComputationTargetSpecification(ComputationTargetType.POSITION, position.getUniqueId());
        // TODO will the security be resolved?
        return new Row(target, position.getSecurity().getName());
      }
    };
    return PortfolioMapper.map(viewDef.getPortfolio().getRootNode(), targetFn);
  }

  private static List<Row> primitivesRows(CompiledViewDefinition compiledViewDef) {
    Set<ComputationTargetSpecification> specs = new LinkedHashSet<ComputationTargetSpecification>();
    for (CompiledViewCalculationConfiguration compiledCalcConfig : compiledViewDef.getCompiledCalculationConfigurations()) {
      for (ValueSpecification valueSpec : compiledCalcConfig.getTerminalOutputSpecifications().keySet()) {
        ComputationTargetSpecification targetSpec = valueSpec.getTargetSpecification();
        if (targetSpec.getType() == ComputationTargetType.PRIMITIVE) {
          specs.add(targetSpec);
        }
      }
    }
    // TODO is the row name right?
    List<Row> rows = Lists.newArrayList();
    for (ComputationTargetSpecification spec : specs) {
      rows.add(new Row(spec, spec.getIdentifier().toString()));
    }
    return rows;
  }

  public AnalyticsNode getRoot() {
    return _root;
  }

  public Row getRowAtIndex(int rowIndex) {
    return _rows.get(rowIndex);
  }

  public int getColumnCount() {
    return _columnCount;
  }

  public List<AnalyticsColumnGroup> getColumnGroups() {
    return _columnGroups;
  }

  @Override
  public String toString() {
    return "AnalyticsGridStructure [" +
        "_columnCount=" + _columnCount +
        ", _columnGroups=" + _columnGroups +
        ", _root=" + _root +
        ", _rows=" + _rows +
        "]";
  }

  public static class Row {

    private final ComputationTargetSpecification _target;
    private final String _name;

    private Row(ComputationTargetSpecification target, String name) {
      ArgumentChecker.notNull(target, "target");
      ArgumentChecker.notNull(name, "name");
      _target = target;
      _name = name;
    }

    public ComputationTargetSpecification getTarget() {
      return _target;
    }

    public String getName() {
      return _name;
    }

    @Override
    public String toString() {
      return "Row [_target=" + _target + ", _name='" + _name + '\'' + "]";
    }
  }
}
