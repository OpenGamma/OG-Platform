/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueInflationIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.BondAndBondFutureFunctionUtils;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class InflationBondPresentValueFromCurvesFunction extends InflationBondFromCurvesFunction<InflationIssuerProviderInterface, MultipleCurrencyAmount> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#PRESENT_VALUE} and
   * the calculator to {@link PresentValueFromCurvesCalculator}.
   * PresentValueFromCurvesCalculator.getInstance()
   */
  public InflationBondPresentValueFromCurvesFunction() {
    super(PRESENT_VALUE, null);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints();
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final InstrumentDerivative derivative = BondAndBondFutureFunctionUtils.getBondOrBondFutureDerivative(executionContext, target, now, inputs);
    final InflationIssuerProviderInterface issuerCurves = (InflationIssuerProviderInterface) inputs.getValue(CURVE_BUNDLE);
    final ValueSpecification spec = new ValueSpecification(PRESENT_VALUE, target.toSpecification(), properties);
    final MultipleCurrencyAmount pv = derivative.accept(PresentValueInflationIssuerDiscountingCalculator.getInstance(), issuerCurves);
    final String expectedCurrency = spec.getProperty(CURRENCY);
    if (pv.size() != 1 || !(expectedCurrency.equals(pv.getCurrencyAmounts()[0].getCurrency().getCode()))) {
      throw new OpenGammaRuntimeException("Expecting a single result in " + expectedCurrency);
    }
    return Collections.singleton(new ComputedValue(spec, pv.getCurrencyAmounts()[0].getAmount()));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    return super.getResultProperties(target)
        .with(CURRENCY, currency);
  }

}
