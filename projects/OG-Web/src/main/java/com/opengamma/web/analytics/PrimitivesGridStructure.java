/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 *
 */
public final class PrimitivesGridStructure extends MainGridStructure {

  /** Target type for anything that isn't part of a portoflio structure. */
  private static final ComputationTargetType NON_PRIMITIVE =
      ComputationTargetType.PORTFOLIO_NODE
          .or(ComputationTargetType.POSITION)
          .or(ComputationTargetType.TRADE)
          .or(ComputationTargetType.SECURITY);

  /** Creates names for the label column in the grid. */
  private static final ComputationTargetReferenceVisitor<String> s_nameVisitor = new ComputationTargetReferenceVisitor<String>() {

    @Override
    public String visitComputationTargetRequirement(ComputationTargetRequirement requirement) {
      return StringUtils.join(requirement.getIdentifiers().iterator(), ", ");
    }

    @Override
    public String visitComputationTargetSpecification(ComputationTargetSpecification specification) {
      if (specification.getUniqueId() != null) {
        return specification.getUniqueId().toString();
      } else {
        return "No target";
      }
    }
  };

  private PrimitivesGridStructure() {
  }

  private PrimitivesGridStructure(GridColumnGroup fixedColumns,
                                  GridColumnGroups nonFixedColumns,
                                  TargetLookup targetLookup,
                                  UnversionedValueMappings valueMappings) {
    super(fixedColumns, nonFixedColumns, targetLookup, valueMappings);
  }

  /* package */ static PrimitivesGridStructure create(CompiledViewDefinition compiledViewDef) {
    List<MainGridStructure.Row> rows = rows(compiledViewDef);
    GridColumn labelColumn = new GridColumn("Name", "", String.class, new PrimitivesLabelRenderer(rows));
    GridColumnGroup fixedColumns = new GridColumnGroup("fixed", ImmutableList.of(labelColumn), false);
    UnversionedValueMappings valueMappings = new UnversionedValueMappings(compiledViewDef);
    TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(compiledViewDef.getViewDefinition(), targetLookup);
    return new PrimitivesGridStructure(fixedColumns, new GridColumnGroups(analyticsColumns), targetLookup, valueMappings);
  }

  private static List<GridColumnGroup> buildAnalyticsColumns(ViewDefinition viewDef, TargetLookup targetLookup) {
    List<GridColumnGroup> columnGroups = Lists.newArrayList();
    Set<ColumnSpecification> columnSpecs = Sets.newHashSet();
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      List<GridColumn> columns = Lists.newArrayList();
      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        if (!specificRequirement.getTargetReference().getType().isTargetType(NON_PRIMITIVE)) {
          String valueName = specificRequirement.getValueName();
          Class<?> columnType = ValueTypes.getTypeForValueName(valueName);
          ValueProperties constraints = specificRequirement.getConstraints();
          ColumnSpecification columnSpec = new ColumnSpecification(calcConfig.getName(), valueName, constraints);
          // ensure columnSpec isn't a duplicate
          if (columnSpecs.add(columnSpec)) {
            columns.add(GridColumn.forSpec(columnSpec, columnType, targetLookup));
          }
        }
      }
      if (!columns.isEmpty()) {
        columnGroups.add(new GridColumnGroup(calcConfig.getName(), columns, true));
      }
    }
    return columnGroups;
  }

  private static List<MainGridStructure.Row> rows(CompiledViewDefinition compiledViewDef) {
    Set<ComputationTargetReference> targetRefs = Sets.newLinkedHashSet();
    for (ViewCalculationConfiguration calcConfig : compiledViewDef.getViewDefinition().getAllCalculationConfigurations()) {
      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        targetRefs.add(specificRequirement.getTargetReference());
      }
    }
    List<MainGridStructure.Row> rows = Lists.newArrayList();
    for (ComputationTargetReference targetRef : targetRefs) {
      rows.add(new Row(targetRef, targetRef.accept(s_nameVisitor)));
    }
    return rows;
  }

  /* package */ static PrimitivesGridStructure empty() {
    return new PrimitivesGridStructure();
  }
}
