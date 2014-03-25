/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.BLACK;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.model.multicurve.MultiCurveUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Calculates the yield curve node sensitivities of interest rate future options using curves constructed using the discounting method and a Black surface.
 */
public class BlackDiscountingYCNSCapFloorFunction extends BlackDiscountingIRFutureOptionFunction {

  /**
   * Sets the value requirements to {@link ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES}
   */
  public BlackDiscountingYCNSCapFloorFunction() {
    super(YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final MultipleCurrencyParameterSensitivity sensitivities = (MultipleCurrencyParameterSensitivity) inputs.getValue(BLOCK_CURVE_SENSITIVITIES);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final String curveName = desiredValue.getConstraint(CURVE);
        final Map<Pair<String, Currency>, DoubleMatrix1D> entries = sensitivities.getSensitivities();
        for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : entries.entrySet()) {
          if (curveName.equals(entry.getKey().getFirst())) {
            final ValueProperties properties = desiredValue.getConstraints().copy().with(CURVE, curveName).get();
            final CurveDefinition curveDefinition = (CurveDefinition) inputs.getValue(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, ValueProperties.builder()
                .with(CURVE, curveName).get()));
            final ValueSpecification spec = new ValueSpecification(YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
            final DoubleLabelledMatrix1D ycns = MultiCurveUtils.getLabelledMatrix(entry.getValue(), curveDefinition);
            return Collections.singleton(new ComputedValue(spec, ycns));
          }
        }
        throw new OpenGammaRuntimeException("Could not get sensitivities to " + curveName + " for " + target.getName());
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
        final Set<String> surfaces = constraints.getValues(SURFACE);
        if (surfaces == null) {
          return null;
        }
        final ValueProperties properties = ValueProperties.with(PROPERTY_CURVE_TYPE, DISCOUNTING).with(CURVE_EXPOSURES, curveExposureConfigs).with(SURFACE, surfaces)
            .with(PROPERTY_VOLATILITY_MODEL, BLACK).get();
        final ValueProperties curveProperties = ValueProperties.with(CURVE, curveNames).get();
        final Set<ValueRequirement> requirements = new HashSet<>();
        final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
        final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
        requirements.add(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, curveProperties));
        requirements.add(new ValueRequirement(BLOCK_CURVE_SENSITIVITIES, target.toSpecification(), properties));
        requirements.addAll(getFXRequirements(security, securitySource));
        final Set<ValueRequirement> tsRequirements = getTimeSeriesRequirements(context, target);
        if (tsRequirements == null) {
          return null;
        }
        requirements.addAll(tsRequirements);
        return requirements;
      }

      @Override
      protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final Collection<ValueProperties.Builder> properties = super.getResultProperties(compilationContext, target);
        for (ValueProperties.Builder builder : properties) {
          builder.withAny(CURVE);
        }
        return properties;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
        String curveName = null;
        for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
          final ValueRequirement requirement = entry.getValue();
          if (requirement.getValueName().equals(CURVE_DEFINITION)) {
            curveName = requirement.getConstraint(CURVE);
            break;
          }
        }
        if (curveName == null) {
          return null;
        }
        final Collection<ValueProperties.Builder> propertiesSet = getResultProperties(compilationContext, target);
        final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(propertiesSet.size());
        for (ValueProperties.Builder properties : propertiesSet) {
          properties.withoutAny(CURVE).with(CURVE, curveName);
          results.add(new ValueSpecification(YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties.get()));
        }
        return results;
      }
    };
  }
}
