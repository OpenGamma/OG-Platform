/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.ALL_PV01S;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Gets the PV01 of an instrument to a named curve using curves constructed with
 * the discounting method.
 */
public class DiscountingPV01Function extends DiscountingFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(DiscountingPV01Function.class);

  /**
   * Sets the value requirements to {@link ValueRequirementNames#PV01}
   */
  public DiscountingPV01Function() {
    super(PV01);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new DiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @SuppressWarnings("synthetic-access")
      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final String desiredCurveName = desiredValue.getConstraint(CURVE);
        final ValueProperties properties = desiredValue.getConstraints();
        final Map<Pair<String, Currency>, Double> pv01s = (Map<Pair<String, Currency>, Double>) inputs.getValue(ALL_PV01S);
        final Set<ComputedValue> results = new HashSet<>();
        for (final Map.Entry<Pair<String, Currency>, Double> entry : pv01s.entrySet()) {
          final String curveName = entry.getKey().getFirst();
          if (desiredCurveName.equals(curveName)) {
            final ValueProperties curveSpecificProperties = properties.copy()
                .withoutAny(CURVE)
                .with(CURVE, curveName)
                .get();
            final ValueSpecification spec = new ValueSpecification(PV01, target.toSpecification(), curveSpecificProperties);
            results.add(new ComputedValue(spec, entry.getValue()));
            return results;
          }
        }
        s_logger.info("Could not get sensitivities to " + desiredCurveName + " for " + target.getName());
        return Collections.emptySet();
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          return null;
        }
        final Set<String> curveExposureConfigs = constraints.getValues(CURVE_EXPOSURES);
        if (curveExposureConfigs == null) {
          return null;
        }
        final ValueProperties properties = ValueProperties
            .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
            .with(CURVE_EXPOSURES, curveExposureConfigs)
            .get();
        return Collections.singleton(new ValueRequirement(ALL_PV01S, target.toSpecification(), properties));
      }

      @Override
      protected ValueProperties.Builder getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final ValueProperties.Builder properties = super.getResultProperties(compilationContext, target);
        return properties.withAny(CURVE);
      }

    };
  }
}
