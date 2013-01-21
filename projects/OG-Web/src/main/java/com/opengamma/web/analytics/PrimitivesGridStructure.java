/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 *
 */
public class PrimitivesGridStructure extends MainGridStructure {

  private static final ComputationTargetType NON_PRIMITIVE = ComputationTargetType.PORTFOLIO_NODE.or(ComputationTargetType.POSITION).or(ComputationTargetType.TRADE).or(ComputationTargetType.SECURITY);

  private PrimitivesGridStructure() {
  }

  private PrimitivesGridStructure(final GridColumnGroups columnGroups, final TargetLookup targetLookup) {
    super(columnGroups, targetLookup);
  }

  /* package */static PrimitivesGridStructure create(final CompiledViewDefinition compiledViewDef, final ValueMappings valueMappings) {
    final List<MainGridStructure.Row> rows = rows(compiledViewDef);
    final GridColumn labelColumn = new GridColumn("Label", "", String.class, new PrimitivesLabelRenderer(rows));
    final GridColumnGroup fixedColumns = new GridColumnGroup("fixed", ImmutableList.of(labelColumn));
    final TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    final List<GridColumnGroup> analyticsColumns = buildColumns(compiledViewDef.getViewDefinition(), targetLookup);
    final List<GridColumnGroup> groups = Lists.newArrayList(fixedColumns);
    groups.addAll(analyticsColumns);
    return new PrimitivesGridStructure(new GridColumnGroups(groups), targetLookup);
  }

  private static List<GridColumnGroup> buildColumns(final ViewDefinition viewDef, final TargetLookup targetLookup) {
    final List<GridColumnGroup> columnGroups = Lists.newArrayList();
    for (final ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      final List<GridColumn> columns = Lists.newArrayList();
      for (final ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        if (!specificRequirement.getTargetReference().getType().isTargetType(NON_PRIMITIVE)) {
          final String valueName = specificRequirement.getValueName();
          final Class<?> columnType = ValueTypes.getTypeForValueName(valueName);
          final ValueProperties constraints = specificRequirement.getConstraints();
          final ColumnSpecification columnSpec = new ColumnSpecification(calcConfig.getName(), valueName, constraints);
          columns.add(GridColumn.forKey(columnSpec, columnType, targetLookup));
        }
      }
      columnGroups.add(new GridColumnGroup(calcConfig.getName(), columns));
    }
    return columnGroups;
  }

  private static List<MainGridStructure.Row> rows(final CompiledViewDefinition compiledViewDef) {
    final Set<ComputationTargetSpecification> specs = Sets.newLinkedHashSet();
    for (final CompiledViewCalculationConfiguration compiledCalcConfig : compiledViewDef.getCompiledCalculationConfigurations()) {
      for (final ValueSpecification valueSpec : compiledCalcConfig.getTerminalOutputSpecifications().keySet()) {
        final ComputationTargetSpecification targetSpec = valueSpec.getTargetSpecification();
        if (!targetSpec.getType().isTargetType(NON_PRIMITIVE)) {
          specs.add(targetSpec);
        }
      }
    }
    final List<MainGridStructure.Row> rows = Lists.newArrayList();
    for (final ComputationTargetSpecification spec : specs) {
      rows.add(new Row(spec, spec.getUniqueId().toString()));
    }
    return rows;
  }

  public static PrimitivesGridStructure empty() {
    return new PrimitivesGridStructure();
  }
}
