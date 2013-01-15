/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
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

  private PrimitivesGridStructure() {
  }

  private PrimitivesGridStructure(List<AnalyticsColumnGroup> staticColumns,
                                  Map<String, List<ColumnKey>> analyticsColumns,
                                  CompiledViewDefinition compiledViewDef,
                                  ValueMappings valueMappings,
                                  List<Row> rows) {
    super(staticColumns, analyticsColumns, compiledViewDef, valueMappings, rows);
  }

  /* package */ static PrimitivesGridStructure create(CompiledViewDefinition compiledViewDef, ValueMappings valueMappings) {
    List<MainGridStructure.Row> rows = rows(compiledViewDef);
    AnalyticsColumn labelColumn = new AnalyticsColumn("Label", "", String.class, new LabelRenderer(0, rows));
    AnalyticsColumnGroup columnGroup = new AnalyticsColumnGroup("fixed", ImmutableList.of(labelColumn));
    Map<String, List<ColumnKey>> columns = buildColumns(compiledViewDef.getViewDefinition());
    return new PrimitivesGridStructure(ImmutableList.of(columnGroup), columns, compiledViewDef, valueMappings, rows);
  }

  private static Map<String, List<ColumnKey>> buildColumns(ViewDefinition viewDef) {
    Map<String, List<ColumnKey>> columnsByCalcConfig = Maps.newHashMap();
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      List<ColumnKey> columnKeys = Lists.newArrayList();
      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        if (specificRequirement.getTargetSpecification().getType() == ComputationTargetType.PRIMITIVE) {
          String valueName = specificRequirement.getValueName();
          ValueProperties constraints = specificRequirement.getConstraints();
          ColumnKey columnKey = new ColumnKey(calcConfig.getName(), valueName, constraints);
          columnKeys.add(columnKey);
        }
      }
      columnsByCalcConfig.put(calcConfig.getName(), columnKeys);
    }
    return columnsByCalcConfig;
  }

  private static List<MainGridStructure.Row> rows(CompiledViewDefinition compiledViewDef) {
    Set<ComputationTargetSpecification> specs = Sets.newLinkedHashSet();
    for (CompiledViewCalculationConfiguration compiledCalcConfig : compiledViewDef.getCompiledCalculationConfigurations()) {
      for (ValueSpecification valueSpec : compiledCalcConfig.getTerminalOutputSpecifications().keySet()) {
        ComputationTargetSpecification targetSpec = valueSpec.getTargetSpecification();
        if (targetSpec.getType() == ComputationTargetType.PRIMITIVE) {
          specs.add(targetSpec);
        }
      }
    }
    List<MainGridStructure.Row> rows = Lists.newArrayList();
    for (ComputationTargetSpecification spec : specs) {
      rows.add(new MainGridStructure.Row(spec, spec.getIdentifier().toString()));
    }
    return rows;
  }

  public static PrimitivesGridStructure empty() {
    return new PrimitivesGridStructure();
  }
}
