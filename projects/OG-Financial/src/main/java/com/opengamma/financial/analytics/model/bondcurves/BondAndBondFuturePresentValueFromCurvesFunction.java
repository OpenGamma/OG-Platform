/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
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
 * Calculates the modified duration of a bond from yield curves.
 */
public class BondAndBondFuturePresentValueFromCurvesFunction extends BondAndBondFutureFromCurvesFunction<IssuerProviderInterface, MultipleCurrencyAmount> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#PRESENT_VALUE}.
   */
  public BondAndBondFuturePresentValueFromCurvesFunction() {
    super(PRESENT_VALUE, null);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints();
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final InstrumentDerivative derivative = BondAndBondFutureFunctionUtils.getBondOrBondFutureDerivative(executionContext, target, now, inputs);
    final IssuerProvider issuerCurves = (IssuerProvider) inputs.getValue(CURVE_BUNDLE);
    final ValueSpecification spec = new ValueSpecification(PRESENT_VALUE, target.toSpecification(), properties);
    final MultipleCurrencyAmount pv = derivative.accept(PresentValueIssuerCalculator.getInstance(), issuerCurves);
    final String expectedCurrency = spec.getProperty(CURRENCY);
    if (pv.size() != 1 || !(expectedCurrency.equals(pv.getCurrencyAmounts()[0].getCurrency().getCode()))) {
      throw new OpenGammaRuntimeException("Expecting a single result in " + expectedCurrency);
    }
    return Collections.singleton(new ComputedValue(spec, pv.getCurrencyAmounts()[0].getAmount()));
  }

  @Override
  protected Collection<ValueProperties.Builder> getResultProperties(final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    final Collection<ValueProperties.Builder> properties = super.getResultProperties(target);
    final Collection<ValueProperties.Builder> result = new HashSet<>();
    for (final ValueProperties.Builder builder : properties) {
      result.add(builder
          .with(CURRENCY, currency));
    }
    return result;
  }
}
