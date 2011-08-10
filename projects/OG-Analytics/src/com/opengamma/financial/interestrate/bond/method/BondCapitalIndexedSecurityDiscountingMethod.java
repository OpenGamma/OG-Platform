/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueInflationCalculator;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.financial.interestrate.market.MarketBundle;
import com.opengamma.financial.interestrate.market.MarketDiscountingDecorated;
import com.opengamma.financial.interestrate.method.PricingMarketMethod;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Pricing method for inflation bond. The price is computed by index estimation and discounting.
 */
public final class BondCapitalIndexedSecurityDiscountingMethod implements PricingMarketMethod {

  /**
   * The present value inflation calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueInflationCalculator PVIC = PresentValueInflationCalculator.getInstance();

  /**
   * Computes the present value of a capital indexed bound by index estimation and discounting.
   * @param bond The bond.
   * @param market The market.
   * @return The present value.
   */
  public CurrencyAmount presentValue(BondCapitalIndexedSecurity<?> bond, MarketBundle market) {
    Validate.notNull(bond, "Coupon");
    Validate.notNull(market, "Market");
    MarketBundle creditDiscounting = new MarketDiscountingDecorated(market, bond.getCurrency(), market.getCurve(bond.getIssuer()));
    final CurrencyAmount pvNominal = PVIC.visit(bond.getNominal(), creditDiscounting);
    final CurrencyAmount pvCoupon = PVIC.visit(bond.getCoupon(), creditDiscounting);
    return pvNominal.plus(pvCoupon);
  }

  @Override
  public CurrencyAmount presentValue(InterestRateDerivative instrument, MarketBundle market) {
    Validate.isTrue(instrument instanceof BondCapitalIndexedSecurity<?>, "Capital inflation indexed bond.");
    return presentValue((BondCapitalIndexedSecurity<?>) instrument, market);
  }

  // TODO: curve sensitivity
  // TODO: price index sensitivity

}
