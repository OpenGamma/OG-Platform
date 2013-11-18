/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class with methods related to bill security valued by discounting.
 * <P> Reference: Bill pricing, version 1.0. OpenGamma documentation, January 2012.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.bond.provider.BillSecurityDiscountingMethod}
 */
@Deprecated
public final class BillSecurityDiscountingMethod implements PricingMethod {

  /**
   * The unique instance of the class.
   */
  private static final BillSecurityDiscountingMethod INSTANCE = new BillSecurityDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BillSecurityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor
   */
  private BillSecurityDiscountingMethod() {
  }

  /**
   * Computes the present value of the bill security by discounting.
   * @param bill The bill.
   * @param curves The curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final BillSecurity bill, final YieldCurveBundle curves) {
    Validate.notNull(bill, "Bill");
    Validate.notNull(curves, "Curves");
    final double pvBill = bill.getNotional() * curves.getCurve(bill.getCreditCurveName()).getDiscountFactor(bill.getEndTime());
    return CurrencyAmount.of(bill.getCurrency(), pvBill);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof BillSecurity, "Bill Security");
    return presentValue((BillSecurity) instrument, curves);
  }

  /**
   * Compute the bill price from the yield. The price is the relative price at settlement. The yield is in the bill yield convention.
   * @param bill The bill.
   * @param yield The yield in the bill yield convention.
   * @return The price.
   */
  public double priceFromYield(final BillSecurity bill, final double yield) {
    return priceFromYield(bill.getYieldConvention(), yield, bill.getAccrualFactor());
  }

  /**
   * Compute the bill price from the yield. The price is the relative price at settlement.
   * @param convention The yield convention.
   * @param yield The yield in the bill yield convention.
   * @param accrualFactor The accrual factor between settlement and maturity.
   * @return The price.
   */
  public double priceFromYield(final YieldConvention convention, final double yield, final double accrualFactor) {
    if (convention == SimpleYieldConvention.DISCOUNT) {
      return 1.0 - accrualFactor * yield;
    }
    if (convention == SimpleYieldConvention.INTERESTATMTY) {
      return 1.0 / (1 + accrualFactor * yield);
    }
    throw new UnsupportedOperationException("The convention " + convention.getName() + " is not supported.");
  }

  /**
   * Computes the bill yield from the price. The yield is in the bill yield convention.
   * @param bill The bill.
   * @param price The price. The price is the relative price at settlement.
   * @return The yield.
   */
  public double yieldFromPrice(final BillSecurity bill, final double price) {
    if (bill.getYieldConvention() == SimpleYieldConvention.DISCOUNT) {
      return (1.0 - price) / bill.getAccrualFactor();
    }
    if (bill.getYieldConvention() == SimpleYieldConvention.INTERESTATMTY) {
      return (1.0 / price - 1) / bill.getAccrualFactor();
    }
    throw new UnsupportedOperationException("The convention " + bill.getYieldConvention().getName() + " is not supported.");
  }

  /**
   * Computes the derivative of the bill yield with respect to the price. The yield is in the bill yield convention.
   * @param bill The bill.
   * @param price The price. The price is the relative price at settlement.
   * @return The yield derivative.
   */
  public double yieldFromPriceDerivative(final BillSecurity bill, final double price) {
    if (bill.getYieldConvention() == SimpleYieldConvention.DISCOUNT) {
      return -1.0 / bill.getAccrualFactor();
    }
    if (bill.getYieldConvention() == SimpleYieldConvention.INTERESTATMTY) {
      return -1.0 / (price * price * bill.getAccrualFactor());
    }
    throw new UnsupportedOperationException("The convention " + bill.getYieldConvention().getName() + " is not supported.");
  }

  /**
   * Computes the present value of the bill security by discounting from its yield.
   * @param bill The bill.
   * @param yield The bill yield.
   * @param curves The curves.
   * @return The present value.
   */
  public CurrencyAmount presentValueFromYield(final BillSecurity bill, final double yield, final YieldCurveBundle curves) {
    Validate.notNull(bill, "Bill");
    Validate.notNull(curves, "Curves");
    final double price = priceFromYield(bill, yield);
    return presentValueFromPrice(bill, price, curves);
  }

  /**
   * Computes the present value of the bill security by discounting from its price.
   * @param bill The bill.
   * @param price The (dirty) price at settlement.
   * @param curves The curves (for discounting from settlement to today) .
   * @return The present value.
   */
  public CurrencyAmount presentValueFromPrice(final BillSecurity bill, final double price, final YieldCurveBundle curves) {
    Validate.notNull(bill, "Bill");
    Validate.notNull(curves, "Curves");
    final double pvBill = bill.getNotional() * price * curves.getCurve(bill.getDiscountingCurveName()).getDiscountFactor(bill.getSettlementTime());
    return CurrencyAmount.of(bill.getCurrency(), pvBill);
  }

  /**
   * Compute the bill price from the curves. The price is the relative price at settlement.
   * @param bill The bill.
   * @param curves The curves.
   * @return The price.
   */
  public double priceFromCurves(final BillSecurity bill, final YieldCurveBundle curves) {
    Validate.notNull(bill, "Bill");
    Validate.notNull(curves, "Curves");
    final double pvBill = bill.getNotional() * curves.getCurve(bill.getCreditCurveName()).getDiscountFactor(bill.getEndTime());
    final double price = pvBill / (bill.getNotional() * curves.getCurve(bill.getDiscountingCurveName()).getDiscountFactor(bill.getSettlementTime()));
    return price;
  }

  /**
   * Computes the bill yield from the curves. The yield is in the bill yield convention.
   * @param bill The bill.
   * @param curves The curves.
   * @return The yield.
   */
  public double yieldFromCurves(final BillSecurity bill, final YieldCurveBundle curves) {
    Validate.notNull(bill, "Bill");
    Validate.notNull(curves, "Curves");
    final double pvBill = bill.getNotional() * curves.getCurve(bill.getCreditCurveName()).getDiscountFactor(bill.getEndTime());
    final double price = pvBill / (bill.getNotional() * curves.getCurve(bill.getDiscountingCurveName()).getDiscountFactor(bill.getSettlementTime()));
    return yieldFromPrice(bill, price);
  }

  /**
   * Computes the bill present value curve sensitivity.
   * @param bill The bill.
   * @param curves The curves.
   * @return The sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final BillSecurity bill, final YieldCurveBundle curves) {
    Validate.notNull(bill, "Bill");
    Validate.notNull(curves, "Curves");
    final double dfEnd = curves.getCurve(bill.getCreditCurveName()).getDiscountFactor(bill.getEndTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfEndBar = bill.getNotional() * pvBar;
    final Map<String, List<DoublesPair>> resultMapCredit = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(bill.getEndTime(), -bill.getEndTime() * dfEnd * dfEndBar));
    resultMapCredit.put(bill.getCreditCurveName(), listDiscounting);
    return new InterestRateCurveSensitivity(resultMapCredit);
  }

}
