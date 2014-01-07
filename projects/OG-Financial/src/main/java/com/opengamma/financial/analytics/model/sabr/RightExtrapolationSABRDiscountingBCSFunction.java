/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabr;

import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.financial.analytics.model.sabr.SABRPropertyValues.PROPERTY_MU;
import static com.opengamma.financial.analytics.model.sabr.SABRPropertyValues.PROPERTY_STRIKE_CUTOFF;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Instant;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;

/**
 *
 */
public class RightExtrapolationSABRDiscountingBCSFunction extends RightExtrapolationSABRDiscountingFunction {

  /**
   * Sets the value requirements to {@link ValueRequirementNames#BLOCK_CURVE_SENSITIVITIES}
   */
  public RightExtrapolationSABRDiscountingBCSFunction() {
    super(BLOCK_CURVE_SENSITIVITIES);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new RightExtrapolationSABRDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final Set<ComputedValue> result = new HashSet<>();
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final DayCount dayCount = DayCounts.ACT_360; //TODO
        final SABRSwaptionProvider sabrData = getSABRSurfaces(executionContext, inputs, target, fxMatrix, dayCount);
        final double strikeCutoff = Double.parseDouble(desiredValue.getConstraint(PROPERTY_STRIKE_CUTOFF));
        final double mu = Double.parseDouble(desiredValue.getConstraint(PROPERTY_MU));
        final InstrumentDerivativeVisitor<SABRSwaptionProviderInterface, MultipleCurrencyMulticurveSensitivity> pvcdsc =
            new PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator(strikeCutoff, mu);
        final ParameterSensitivityParameterCalculator<SABRSwaptionProviderInterface> psc =
            new ParameterSensitivityParameterCalculator<>(pvcdsc);
        final MarketQuoteSensitivityBlockCalculator<SABRSwaptionProviderInterface> calculator =
            new MarketQuoteSensitivityBlockCalculator<>(psc);
        final CurveBuildingBlockBundle blocks = getMergedCurveBuildingBlocks(inputs);
        final MultipleCurrencyParameterSensitivity sensitivities = calculator.fromInstrument(derivative, sabrData, blocks);
        for (final ValueRequirement dv : desiredValues) {
          final ValueSpecification spec = new ValueSpecification(BLOCK_CURVE_SENSITIVITIES, target.toSpecification(), dv.getConstraints().copy().get());
          result.add(new ComputedValue(spec, sensitivities));
        }
        return result;
      }

    };
  }

}
