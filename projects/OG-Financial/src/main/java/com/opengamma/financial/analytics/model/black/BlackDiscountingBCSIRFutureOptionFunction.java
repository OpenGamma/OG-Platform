/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesSmileProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
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
 * Calculates the sensitivities of an interest rate future option to the bundle of curves used
 * in pricing. The Black method is used.
 */
public class BlackDiscountingBCSIRFutureOptionFunction extends BlackDiscountingIRFutureOptionFunction {
  /** The curve sensitivity calculator */
  private static final InstrumentDerivativeVisitor<BlackSTIRFuturesSmileProviderInterface, MultipleCurrencyMulticurveSensitivity> PVCSDC =
      PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator.getInstance();
  /** The parameter sensitivity calculator */
  private static final ParameterSensitivityParameterCalculator<BlackSTIRFuturesSmileProviderInterface> PSC =
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  /** The market quote sensitivity calculator */
  private static final MarketQuoteSensitivityBlockCalculator<BlackSTIRFuturesSmileProviderInterface> CALCULATOR =
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  /**
   * Sets the value requirements to {@link ValueRequirementNames#BLOCK_CURVE_SENSITIVITIES}
   */
  public BlackDiscountingBCSIRFutureOptionFunction() {
    super(BLOCK_CURVE_SENSITIVITIES);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        ValueProperties.Builder properties = null;
        if (desiredValues.size() == 1) {
          properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy();
        } else {
          final Iterator<ValueRequirement> iterator = desiredValues.iterator();
          final Set<String> curveNames = new HashSet<>();
          while (iterator.hasNext()) {
            if (properties == null) {
              properties = iterator.next().getConstraints().copy();
              curveNames.addAll(properties.get().getValues(CURVE));
            } else {
              curveNames.addAll(iterator.next().getConstraints().getValues(CURVE));
            }
          }
          if (properties == null) {
            throw new OpenGammaRuntimeException("No entries in desiredValues");
          }
          properties.withoutAny(CURVE).with(CURVE, curveNames);
        }
        final BlackSTIRFuturesSmileProviderInterface blackData = getBlackSurface(executionContext, inputs, target, fxMatrix);
        final CurveBuildingBlockBundle blocks = getMergedCurveBuildingBlocks(inputs);
        final MultipleCurrencyParameterSensitivity sensitivities = CALCULATOR.fromInstrument(derivative, blackData, blocks);
        final ValueSpecification spec = new ValueSpecification(BLOCK_CURVE_SENSITIVITIES, target.toSpecification(), properties.get());
        return Collections.singleton(new ComputedValue(spec, sensitivities));
      }

    };
  }

}
