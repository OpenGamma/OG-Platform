/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.trs;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.equity.EquityTotalReturnSwap;
import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
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
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Calculates the PV01 of an equity total return swap security.
 */
public class EquityTotalReturnSwapPV01Function extends EquityTotalReturnSwapFunction {
  /** The calculator */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, ReferenceAmount<Pair<String, Currency>>> CALCULATOR =
      new PV01CurveParametersCalculator<>(PresentValueCurveSensitivityDiscountingCalculator.getInstance());

  /**
   * Sets the value requirement to {@link ValueRequirementNames#PV01}.
   */
  public EquityTotalReturnSwapPV01Function() {
    super(PV01);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new EquityTotalReturnSwapCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @SuppressWarnings("synthetic-access")
      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy().get();
        final EquityTrsDataBundle data = getDataBundle(inputs, fxMatrix);
        final String desiredCurveName = properties.getStrictValue(CURVE);
        final ReferenceAmount<Pair<String, Currency>> pv01 = ((EquityTotalReturnSwap) derivative).getFundingLeg().accept(CALCULATOR, data.getCurves());
        final Set<ComputedValue> results = new HashSet<>();
        boolean curveNameFound = false;
        for (final Map.Entry<Pair<String, Currency>, Double> entry : pv01.getMap().entrySet()) {
          final String curveName = entry.getKey().getFirst();
          if (desiredCurveName.equals(curveName)) {
            curveNameFound = true;
          }
          final ValueProperties curveSpecificProperties = properties.copy()
              .withoutAny(CURVE)
              .with(CURVE, curveName)
              .get();
          final ValueSpecification spec = new ValueSpecification(PV01, target.toSpecification(), curveSpecificProperties);
          results.add(new ComputedValue(spec, entry.getValue()));
        }
        if (!curveNameFound) {
          final ValueSpecification spec = new ValueSpecification(PV01, target.toSpecification(), properties.copy().with(CURVE, desiredCurveName).get());
          return Collections.singleton(new ComputedValue(spec, 0));
        }
        return results;
      }
    };
  }

}
