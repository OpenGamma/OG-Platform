/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTotalReturnSwapDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value the asset leg of a bond total return swap. 
 */
public final class BondBillTrsAssetLegPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<IssuerProviderInterface, MultipleCurrencyAmount> {
  /** A singleton instance */
  private static final BondBillTrsAssetLegPresentValueCalculator INSTANCE = new BondBillTrsAssetLegPresentValueCalculator();

  /**
   * Gets the singleton instance.
   * @return The singleton instance
   */
  public static BondBillTrsAssetLegPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private BondBillTrsAssetLegPresentValueCalculator() {
  }

  @Override
  public MultipleCurrencyAmount visitBondTotalReturnSwap(final BondTotalReturnSwap bondTrs, final IssuerProviderInterface data) {
    ArgumentChecker.notNull(bondTrs, "bondTrs");
    ArgumentChecker.notNull(data, "data");
    return BondTotalReturnSwapDiscountingMethod.getInstance().presentValueAssetLeg(bondTrs, data);
  }

  @Override
  public MultipleCurrencyAmount visitBillTotalReturnSwap(final BillTotalReturnSwap billTrs, final IssuerProviderInterface data) {
    ArgumentChecker.notNull(billTrs, "billTrs");
    ArgumentChecker.notNull(data, "data");
    return BondTotalReturnSwapDiscountingMethod.getInstance().presentValueAssetLeg(billTrs, data);
  }

}
