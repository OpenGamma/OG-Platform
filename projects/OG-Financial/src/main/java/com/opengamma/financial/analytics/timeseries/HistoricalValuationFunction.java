/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.financial.view.ViewEvaluationTarget;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Iterates a view client over historical data to produce a historical valuation of a target.
 */
public class HistoricalValuationFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Property naming the value produced on the target to generate the time series. For example {@code Historical Series[Value=FairValue]} will produce a time series based on evaluating
   * {@code FairValue[]}.
   */
  public static final String VALUE = "Value";

  /**
   * Prefix on properties corresponding the the underlying production on the target. For example {@code Historical Series[Value=FairValue, Value_Foo=Bar]} will produce a time series based on
   * evaluating {@code FairValue[Foo=Bar]}.
   */
  public static final String PASSTHROUGH_PREFIX = VALUE + "_";

  /**
   * Property naming how the target is specified, for example by unique identifier, object identifier or external identifier.
   */
  public static final String TARGET_SPECIFICATION = "Target";

  /**
   * Value of the {@link #TARGET_SPECIFICATION} property indicating the target is specified by its unique identifier and is independent of the resolver version/correction time on the spawned view
   * cycles.
   */
  public static final String TARGET_SPECIFICATION_UNIQUE = "Unique";

  /**
   * Value of the {@link #TARGET_SPECIFICATION} property indicating the target is specified by its object identifier and is dependent of the resolver version/correction time on the spawned view
   * cycles.
   */
  public static final String TARGET_SPECIFICATION_OBJECT = "Object";

  /**
   * Value of the {@link #TARGET_SPECIFICATION} property indicating the target is specified by its external identifier bundle and is dependent of the resolver version/correction time on the spawned
   * view cycles.
   */
  public static final String TARGET_SPECIFICATION_EXTERNAL = "External";

  // CompiledFunctionDefinition

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.ANYTHING;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ViewEvaluationTarget tempTarget = new ViewEvaluationTarget(context.getViewCalculationConfiguration().getViewDefinition().getMarketDataUser());
    String valueName = ValueRequirementNames.VALUE;
    ComputationTargetReference requirementTarget = null;
    final ValueProperties.Builder requirementConstraints = ValueProperties.builder();
    final ValueProperties desiredConstraints = desiredValue.getConstraints();
    for (final String constraintName : desiredConstraints.getProperties()) {
      final Set<String> constraintValues = desiredConstraints.getValues(constraintName);
      if (VALUE.equals(constraintName)) {
        if (constraintValues.isEmpty()) {
          valueName = ValueRequirementNames.VALUE;
        } else if (constraintValues.size() > 1) {
          return null;
        } else {
          valueName = constraintValues.iterator().next();
        }
      } else if (TARGET_SPECIFICATION.equals(constraintName)) {
        if (constraintValues.isEmpty() || constraintValues.contains(TARGET_SPECIFICATION_UNIQUE)) {
          requirementTarget = target.toSpecification();
        } else if (constraintValues.contains(TARGET_SPECIFICATION_OBJECT)) {
          requirementTarget = target.toSpecification().replaceIdentifier(target.getUniqueId().toLatest());
        } else if (constraintValues.contains(TARGET_SPECIFICATION_EXTERNAL)) {
          final ExternalIdBundle identifiers;
          if (target.getValue() instanceof ExternalIdentifiable) {
            final ExternalId identifier = ((ExternalIdentifiable) target.getValue()).getExternalId();
            if (identifier == null) {
              identifiers = ExternalIdBundle.EMPTY;
            } else {
              identifiers = identifier.toBundle();
            }
          } else if (target.getValue() instanceof ExternalBundleIdentifiable) {
            identifiers = ((ExternalBundleIdentifiable) target.getValue()).getExternalIdBundle();
          } else {
            return null;
          }
          if (target.getContextIdentifiers() == null) {
            requirementTarget = new ComputationTargetRequirement(target.getType(), identifiers);
          } else {
            requirementTarget = target.getContextSpecification().containing(target.getLeafSpecification().getType(), identifiers);
          }
        } else {
          return null;
        }
      } else if (constraintName.startsWith(PASSTHROUGH_PREFIX)) {
        final String name = constraintName.substring(PASSTHROUGH_PREFIX.length());
        if (constraintValues.isEmpty()) {
          requirementConstraints.withAny(name);
        } else {
          requirementConstraints.with(name, constraintValues);
        }
        if (desiredConstraints.isOptional(constraintName)) {
          requirementConstraints.withOptional(name);
        }
      } else if (HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY.equals(constraintName)) {
        if (constraintValues.isEmpty()) {
          tempTarget.setFirstValuationDate("");
        } else {
          tempTarget.setFirstValuationDate(constraintValues.iterator().next());
        }
      } else if (HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY.equals(constraintName)) {
        if (constraintValues.isEmpty() || constraintValues.contains(HistoricalTimeSeriesFunctionUtils.YES_VALUE)) {
          tempTarget.setIncludeFirstValuationDate(true);
        } else if (constraintValues.contains(HistoricalTimeSeriesFunctionUtils.NO_VALUE)) {
          tempTarget.setIncludeFirstValuationDate(false);
        } else {
          return null;
        }
      } else if (HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY.equals(constraintName)) {
        if (constraintValues.isEmpty()) {
          tempTarget.setLastValuationDate("");
        } else {
          tempTarget.setLastValuationDate(constraintValues.iterator().next());
        }
      } else if (HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY.equals(constraintName)) {
        if (constraintValues.isEmpty() || constraintValues.contains(HistoricalTimeSeriesFunctionUtils.YES_VALUE)) {
          tempTarget.setIncludeLastValuationDate(true);
        } else if (constraintValues.contains(HistoricalTimeSeriesFunctionUtils.NO_VALUE)) {
          tempTarget.setIncludeLastValuationDate(false);
        } else {
          return null;
        }
      } else if (!desiredConstraints.isOptional(constraintName)) {
        return null;
      }
    }
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(tempTarget.getViewDefinition(), context.getViewCalculationConfiguration().getName());
    calcConfig.addSpecificRequirement(new ValueRequirement(valueName, requirementTarget, requirementConstraints.get()));
    tempTarget.getViewDefinition().addViewCalculationConfiguration(calcConfig);
    final TempTargetRepository targets = OpenGammaCompilationContext.getTempTargets(context);
    final UniqueId requirement = targets.locateOrStore(tempTarget);
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.VALUE, new ComputationTargetSpecification(ViewEvaluationTarget.TYPE, requirement), ValueProperties.none()));
  }

  // FunctionInvoker

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
      throws AsynchronousExecution {
    // TODO: fetch the valuation results from the cache and construct a time series of the desired value
    throw new UnsupportedOperationException();
  }

}
