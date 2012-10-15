/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.web.server.RequirementBasedColumnKey;

/**
 *
 */
public class PrimitivesGridStructure extends MainGridStructure {

  /* package */ PrimitivesGridStructure(CompiledViewDefinition compiledViewDef) {
    super(compiledViewDef, rows(compiledViewDef));
  }

  private PrimitivesGridStructure() {
  }

  /* package */ static PrimitivesGridStructure empty() {
    return new PrimitivesGridStructure();
  }

  /* package */ List<RequirementBasedColumnKey> buildColumns(ViewCalculationConfiguration calcConfig) {
    List<RequirementBasedColumnKey> columnKeys = Lists.newArrayList();
    for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
      if (specificRequirement.getTargetReference().getType().isTargetType(ComputationTargetType.PRIMITIVE)) { // [PLAT-2286]: this check is probably wrong
        String valueName = specificRequirement.getValueName();
        ValueProperties constraints = specificRequirement.getConstraints();
        RequirementBasedColumnKey columnKey = new RequirementBasedColumnKey(calcConfig.getName(), valueName, constraints);
        columnKeys.add(columnKey);
      }
    }
    return columnKeys;
  }

  private static List<Row> rows(CompiledViewDefinition compiledViewDef) {
    Set<ComputationTargetSpecification> specs = new LinkedHashSet<ComputationTargetSpecification>();
    for (CompiledViewCalculationConfiguration compiledCalcConfig : compiledViewDef.getCompiledCalculationConfigurations()) {
      for (ValueSpecification valueSpec : compiledCalcConfig.getTerminalOutputSpecifications().keySet()) {
        ComputationTargetSpecification targetSpec = valueSpec.getTargetSpecification();
        if (targetSpec.getType().isTargetType(ComputationTargetType.PRIMITIVE)) { // [PLAT-2286]: this check is probably wrong
          specs.add(targetSpec);
        }
      }
    }
    // TODO is the row name right?
    List<Row> rows = Lists.newArrayList();
    for (ComputationTargetSpecification spec : specs) {
      rows.add(new Row(spec, spec.getUniqueId().toString()));
    }
    return rows;
  }
}
