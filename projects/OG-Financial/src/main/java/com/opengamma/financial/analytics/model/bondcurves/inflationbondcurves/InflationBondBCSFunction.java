/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.inflation.MarketQuoteInflationSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueCurveSensitivityInflationIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.BondAndBondFutureFunctionUtils;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Calculates the sensitivities to the market quotes of a bond or bond future for all curves
 * to which the instruments are sensitive.
 */
public class InflationBondBCSFunction extends InflationBondFromCurvesFunction<InflationIssuerProviderInterface, MultipleCurrencyInflationSensitivity> {
  /** The curve sensitivity calculator */
  private static final InstrumentDerivativeVisitor<ParameterInflationIssuerProviderInterface, MultipleCurrencyInflationSensitivity> PVCSIIC =
      PresentValueCurveSensitivityInflationIssuerCalculator.getInstance();
  /** The parameter sensitivity calculator */
  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationIssuerProviderInterface> PSC =
      new ParameterSensitivityInflationParameterCalculator<>(PVCSIIC);
  /** The market quote sensitivity calculator */
  private static final MarketQuoteInflationSensitivityBlockCalculator<ParameterInflationIssuerProviderInterface> CALCULATOR =
      new MarketQuoteInflationSensitivityBlockCalculator<>(PSC);

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#BLOCK_CURVE_SENSITIVITIES} and
   * sets the calculator to null.
   */
  public InflationBondBCSFunction() {
    super(BLOCK_CURVE_SENSITIVITIES, null);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext context, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(context.getValuationClock());
    final InstrumentDerivative derivative = BondAndBondFutureFunctionUtils.getBondOrBondFutureDerivative(context, target, now, inputs);
    final InflationIssuerProviderInterface issuerCurves = (InflationIssuerProviderInterface) inputs.getValue(CURVE_BUNDLE);
    final CurveBuildingBlockBundle blocks = (CurveBuildingBlockBundle) inputs.getValue(JACOBIAN_BUNDLE);
    final Set<ComputedValue> result = new HashSet<>();
    final MultipleCurrencyParameterSensitivity sensitivities = CALCULATOR.fromInstrument(derivative, issuerCurves, blocks);
    for (final ValueRequirement desiredValue : desiredValues) {
      final ValueSpecification spec = new ValueSpecification(BLOCK_CURVE_SENSITIVITIES, target.toSpecification(), desiredValue.getConstraints().copy().get());
      result.add(new ComputedValue(spec, sensitivities));
    }
    return result;
  }

}
