/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of a bond from the clean price and a curve bundle.
 */
public class InflationBondPresentValueFromCleanPriceFunction extends InflationBondFromCleanPriceAndCurvesFunction {
  /** The present value calculator */
  private static final BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer CALCULATOR = BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer.getInstance();
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#PRESENT_VALUE}.
   */
  public InflationBondPresentValueFromCleanPriceFunction() {
    super(PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final FunctionInputs inputs, final BondCapitalIndexedTransaction<?> bond, final InflationProviderInterface provider, final double cleanPrice,
      final ValueSpecification spec) {
    final String expectedCurrency = spec.getProperty(CURRENCY);
    final MultipleCurrencyAmount pvBond = CALCULATOR.presentValueFromCleanPriceReal(bond.getBondTransaction(), provider, cleanPrice);
    final MultipleCurrencyAmount pvSettlement = bond.getBondTransaction().getSettlement().accept(PVIC, provider.getInflationProvider()).multipliedBy(
        bond.getQuantity() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional());
    final MultipleCurrencyAmount pv = pvBond.plus(pvSettlement);
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
