/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PortfolioMapper;
import com.opengamma.core.position.impl.PortfolioMapperFunction;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.RequirementBasedColumnKey;

/**
 * TODO different subclasses for portfolio and primitives? portfolio has a tree, primitives is flat
 */
public class MainGridStructure extends AnalyticsGridStructure {

  private static final Set<ValueRequirement> NO_MAPPINGS = ImmutableSet.of();

  private final List<Row> _rows;
  private final Map<RequirementBasedColumnKey, Integer> _indexByRequirement;
  /** Mappings of specification to requirements, keyed by calculation config name. */
  private final Map<String, Map<ValueSpecification, Set<ValueRequirement>>> _specsToReqs;

  MainGridStructure(AnalyticsNode root,
                    List<AnalyticsColumnGroup> columnGroups,
                    List<Row> rows,
                    Map<RequirementBasedColumnKey, Integer> indexByRequirement,
                    Map<String, Map<ValueSpecification, Set<ValueRequirement>>> specsToReqs) {
    super(root, columnGroups);
    _rows = rows;
    _indexByRequirement = indexByRequirement;
    _specsToReqs = specsToReqs;
  }

  public static MainGridStructure portoflio(CompiledViewDefinition compiledViewDef) {
    List<Row> rows = portfolioRows(compiledViewDef);
    AnalyticsNode root = AnalyticsNode.portoflioRoot(compiledViewDef);
    return create(compiledViewDef, new PortfolioColumnBuilder(), root, rows);
  }

  public static MainGridStructure primitives(CompiledViewDefinition compiledViewDef) {
    List<Row> rows = primitivesRows(compiledViewDef);
    AnalyticsNode root = AnalyticsNode.primitivesRoot(rows.size());
    return create(compiledViewDef, new PrimitivesColumnBuilder(), root, rows);
  }

  public static MainGridStructure empty() {
    return new MainGridStructure(AnalyticsNode.emptyRoot(),
                                 Collections.<AnalyticsColumnGroup>emptyList(),
                                 Collections.<Row>emptyList(),
                                 Collections.<RequirementBasedColumnKey, Integer>emptyMap(),
                                 Collections.<String, Map<ValueSpecification, Set<ValueRequirement>>>emptyMap());
  }

  private static MainGridStructure create(CompiledViewDefinition compiledViewDef,
                                          ColumnBuilder columnBuilder,
                                          AnalyticsNode root,
                                          List<Row> rows) {
    ViewDefinition viewDef = compiledViewDef.getViewDefinition();
    // map of column index keyed by column key
    Map<RequirementBasedColumnKey, Integer> indexMap = Maps.newHashMap();
    // column group for the label column
    AnalyticsColumnGroup labelGroup = new AnalyticsColumnGroup("", ImmutableList.of(new AnalyticsColumn("Label", "")));
    List<AnalyticsColumnGroup> columnGroups = Lists.newArrayList(labelGroup);
    Map<String, Map<ValueSpecification, Set<ValueRequirement>>> specsToReqs = Maps.newHashMap();
    int colIndex = 1; // col 0 is the node name
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      String configName = calcConfig.getName();
      CompiledViewCalculationConfiguration compiledConfig = compiledViewDef.getCompiledCalculationConfiguration(configName);
      // store the mappings from outputs to requirements for each calc config
      specsToReqs.put(configName, compiledConfig.getTerminalOutputSpecifications());
      List<AnalyticsColumn> configColumns = new ArrayList<AnalyticsColumn>();

      List<RequirementBasedColumnKey> columnKeys = columnBuilder.buildColumns(calcConfig);
      for (RequirementBasedColumnKey columnKey : columnKeys) {
        if (!indexMap.containsKey(columnKey)) {
          indexMap.put(columnKey, colIndex);
          colIndex++;
          configColumns.add(AnalyticsColumn.forKey(columnKey));
        }
      }
      columnGroups.add(new AnalyticsColumnGroup(configName, configColumns));
    }
    return new MainGridStructure(root, columnGroups, rows, indexMap, specsToReqs);
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

  public Row getRowAtIndex(int rowIndex) {
    return _rows.get(rowIndex);
  }

  /* package */ int getColumnIndexForRequirement(String calcConfigName, ValueRequirement requirement) {
    RequirementBasedColumnKey key =
        new RequirementBasedColumnKey(calcConfigName, requirement.getValueName(), requirement.getConstraints());
    return _indexByRequirement.get(key);
  }

  /* package */ Set<ValueRequirement> getRequirementsForSpecification(String calcConfigName, ValueSpecification spec) {
    Map<ValueSpecification, Set<ValueRequirement>> specToReqs = _specsToReqs.get(calcConfigName);
    if (specToReqs == null) {
      return null;
    }
    Set<ValueRequirement> reqs = specToReqs.get(spec);
    if (reqs == null) {
      return NO_MAPPINGS;
    }
    return reqs;
  }

  /**
   *
   * @param row
   * @param col
   * @return Pair of value spec and calculation config name.
   * TODO need to reverse the map of valueSpec->set(valueReq)
   * TODO need to specify this using a stable target ID to cope with dynamic reaggregation
   */
  /* package */ Pair<ValueSpecification, String> getTargetForCell(int row, int col) {
    throw new UnsupportedOperationException();
  }

  public static class Row {

    private final ComputationTargetSpecification _target;
    private final String _name;

    /* package */ Row(ComputationTargetSpecification target, String name) {
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

  private interface ColumnBuilder {

    List<RequirementBasedColumnKey> buildColumns(ViewCalculationConfiguration calcConfig);
  }

  private static class PortfolioColumnBuilder implements ColumnBuilder {

    @Override
    public List<RequirementBasedColumnKey> buildColumns(ViewCalculationConfiguration calcConfig) {
      List<RequirementBasedColumnKey> columnKeys = Lists.newArrayList();
      for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
        String valueName = portfolioOutput.getFirst();
        ValueProperties constraints = portfolioOutput.getSecond();
        RequirementBasedColumnKey columnKey = new RequirementBasedColumnKey(calcConfig.getName(), valueName, constraints);
        columnKeys.add(columnKey);
      }
      return columnKeys;
    }
  }

  private static class PrimitivesColumnBuilder implements ColumnBuilder {

    @Override
    public List<RequirementBasedColumnKey> buildColumns(ViewCalculationConfiguration calcConfig) {
      List<RequirementBasedColumnKey> columnKeys = Lists.newArrayList();
      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        if (specificRequirement.getTargetSpecification().getType() == ComputationTargetType.PRIMITIVE) {
          String valueName = specificRequirement.getValueName();
          ValueProperties constraints = specificRequirement.getConstraints();
          RequirementBasedColumnKey columnKey = new RequirementBasedColumnKey(calcConfig.getName(), valueName, constraints);
          columnKeys.add(columnKey);
        }
      }
      return columnKeys;
    }
  }
}
