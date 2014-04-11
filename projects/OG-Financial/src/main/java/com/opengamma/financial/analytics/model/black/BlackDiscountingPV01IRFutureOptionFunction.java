/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
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
 * Calculates the PV01 of interest rate future options using a Black surface and curves constructed using the discounting method.
 */
public class BlackDiscountingPV01IRFutureOptionFunction extends BlackDiscountingIRFutureOptionFunction {
  /** The PV01 calculator */
  private static final InstrumentDerivativeVisitor<BlackSTIRFuturesProviderInterface, ReferenceAmount<Pair<String, Currency>>> CALCULATOR = new PV01CurveParametersCalculator<>(
      PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator.getInstance());

  /**
   * Sets the value requirements to {@link ValueRequirementNames#PV01}
   */
  public BlackDiscountingPV01IRFutureOptionFunction() {
    super(PV01);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final BlackSTIRFuturesProviderInterface blackData = getBlackSurface(executionContext, inputs, target, fxMatrix);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final String desiredCurveName = desiredValue.getConstraint(CURVE);
        final ValueProperties properties = desiredValue.getConstraints();
        final ReferenceAmount<Pair<String, Currency>> pv01 = derivative.accept(CALCULATOR, blackData);
        final Set<ComputedValue> results = new HashSet<>();
        boolean curveNameFound = false;
        for (final Map.Entry<Pair<String, Currency>, Double> entry : pv01.getMap().entrySet()) {
          final String curveName = entry.getKey().getFirst();
          if (desiredCurveName.equals(curveName)) {
            curveNameFound = true;
          }
          final ValueProperties curveSpecificProperties = properties.copy().withoutAny(CURVE).with(CURVE, curveName).get();
          final ValueSpecification spec = new ValueSpecification(PV01, target.toSpecification(), curveSpecificProperties);
          results.add(new ComputedValue(spec, entry.getValue()));
        }
        if (!curveNameFound) {
          throw new OpenGammaRuntimeException("Could not get sensitivities to " + desiredCurveName + " for " + target.getName());
        }
        return results;
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
      protected boolean requirementsSet(final ValueProperties constraints) {
        if (super.requirementsSet(constraints)) {
          final Set<String> curves = constraints.getValues(CURVE);
          if (curves == null) {
            return false;
          }
          return true;
        }
        return false;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
        Set<String> curveNames = null;
        for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
          final ValueSpecification key = entry.getKey();
          if (key.getValueName().equals(CURVE_BUNDLE)) {
            curveNames = key.getProperties().getValues(CURVE);
            break;
          }
        }
        if (curveNames == null) {
          return null;
        }
        final Collection<ValueProperties.Builder> commonPropertiesSet = super.getResultProperties(compilationContext, target);
        final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(commonPropertiesSet.size() * curveNames.size());
        for (final String curveName : curveNames) {
          for (ValueProperties.Builder commonProperties : commonPropertiesSet) {
            final ValueProperties properties = commonProperties.withoutAny(CURVE).with(CURVE, curveName).get();
            results.add(new ValueSpecification(PV01, target.toSpecification(), properties));
          }
        }
        return results;
      }
    };
  }
}
