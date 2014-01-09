/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.hullwhitediscounting;

import static com.opengamma.engine.value.ValueRequirementNames.PAR_RATE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParRateHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
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

/**
 * Calculates the par rate of instruments using curves constructed using
 * the Hull-White one factor discounting method.
 */
public class HullWhiteDiscountingParRateFunction extends HullWhiteDiscountingFunction {
  /** The par rate calculator */
  private static final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> CALCULATOR = ParRateHullWhiteCalculator.getInstance();

  /**
   * Sets the value requirements to {@link ValueRequirementNames#PAR_RATE}
   */
  public HullWhiteDiscountingParRateFunction() {
    super(PAR_RATE);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new HullWhiteCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final HullWhiteOneFactorProviderInterface data = getMergedProviders(inputs, fxMatrix);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final double parRate = derivative.accept(CALCULATOR, data);
        final ValueSpecification spec = new ValueSpecification(PAR_RATE, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, parRate));
      }
    };
  }
}
