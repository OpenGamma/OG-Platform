/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import static com.opengamma.engine.value.ValueRequirementNames.ALL_PV01S;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Calculates all relevant PV01s of instruments using curves constructed using
 * the discounting method.
 */
public class DiscountingAllPV01Function extends DiscountingFunction {
  /** The PV01 calculator */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, ReferenceAmount<Pair<String, Currency>>> CALCULATOR =
      new PV01CurveParametersCalculator<>(PresentValueCurveSensitivityDiscountingCalculator.getInstance());

  /**
   * Sets the value requirements to {@link ValueRequirementNames#ALL_PV01S}
   */
  public DiscountingAllPV01Function() {
    super(ALL_PV01S);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new DiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @SuppressWarnings("synthetic-access")
      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final MulticurveProviderInterface data = getMergedProviders(inputs, fxMatrix);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ReferenceAmount<Pair<String, Currency>> pv01s = derivative.accept(CALCULATOR, data);
        final ValueSpecification spec = new ValueSpecification(ALL_PV01S, target.toSpecification(), desiredValue.getConstraints().copy().get());
        return Collections.singleton(new ComputedValue(spec, pv01s.getMap()));
      }

    };
  }
}
