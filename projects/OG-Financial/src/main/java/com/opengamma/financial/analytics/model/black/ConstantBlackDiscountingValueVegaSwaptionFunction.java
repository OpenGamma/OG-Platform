/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValueRequirementNames.VALUE_VEGA;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackSwaptionSensitivity;
import com.opengamma.analytics.financial.provider.calculator.blackswaption.PresentValueBlackSensitivityBlackSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProvider;
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
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculates the value vega of a swaption using the Black method with no volatility modelling assumptions.
 * The implied volatility is read directly from the market data system.
 */
public class ConstantBlackDiscountingValueVegaSwaptionFunction extends ConstantBlackDiscountingSwaptionFunction {
  /** The calculator */
  private static final PresentValueBlackSensitivityBlackSwaptionCalculator CALCULATOR =
      PresentValueBlackSensitivityBlackSwaptionCalculator.getInstance();

  /**
   * Sets {@link ValueRequirementNames#VALUE_VEGA} as the result.
   */
  public ConstantBlackDiscountingValueVegaSwaptionFunction() {
    super(VALUE_VEGA);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final BlackSwaptionFlatProvider blackData = getSwaptionBlackSurface(executionContext, inputs, target, fxMatrix);
        final PresentValueBlackSwaptionSensitivity sensitivities = derivative.accept(CALCULATOR, blackData);
        final HashMap<DoublesPair, Double> result = sensitivities.getSensitivity().getMap();
        if (result.size() != 1) {
          throw new OpenGammaRuntimeException("Expecting only one result for Black value vega");
        }
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final ValueSpecification spec = new ValueSpecification(VALUE_VEGA, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, Iterables.getOnlyElement(result.values())));
      }

    };
  }

}
