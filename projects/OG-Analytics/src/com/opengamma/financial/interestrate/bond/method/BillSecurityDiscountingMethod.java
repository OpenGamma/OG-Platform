/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Class with methods related to bill security valued by discounting.
 * <P> Reference: Bill pricing, version 1.0. OpenGamma documentation, January 2012.
 */
public final class BillSecurityDiscountingMethod {

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
    double pvBill = bill.getNotional() * curves.getCurve(bill.getCreditCurveName()).getDiscountFactor(bill.getEndTime());
    return CurrencyAmount.of(bill.getCurrency(), pvBill);
  }

  /**
   * Compute the bill price from the yield. The price is the relative price at settlement. The yield is in the bill yield convention.
   * @param bill The bill.
   * @param yield The yield in the bill yield convention.
   * @return The price.
   */
  public double priceFromYield(final BillSecurity bill, final double yield) {
    if (bill.getYieldConvention() == SimpleYieldConvention.DISCOUNT) {
      return 1.0 - bill.getAccralFactor() * yield;
    }
    if (bill.getYieldConvention() == SimpleYieldConvention.INTERESTATMTY) {
      return 1.0 / (1 + bill.getAccralFactor() * yield);
    }
    throw new UnsupportedOperationException("The convention " + bill.getYieldConvention().getConventionName() + " is not supported.");
  }

  /**
   * Computes the bill yield from the price. The yield is in the bill yield convention.
   * @param bill The bill.
   * @param price The price. The price is the relative price at settlement.
   * @return The yield.
   */
  public double yieldFromPrice(final BillSecurity bill, final double price) {
    if (bill.getYieldConvention() == SimpleYieldConvention.DISCOUNT) {
      return (1.0 - price) / bill.getAccralFactor();
    }
    if (bill.getYieldConvention() == SimpleYieldConvention.INTERESTATMTY) {
      return (1.0 / price - 1) / bill.getAccralFactor();
    }
    throw new UnsupportedOperationException("The convention " + bill.getYieldConvention().getConventionName() + " is not supported.");
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
    double price = priceFromYield(bill, yield);
    double pvBill = price * curves.getCurve(bill.getCreditCurveName()).getDiscountFactor(bill.getSettlementTime());
    return CurrencyAmount.of(bill.getCurrency(), pvBill);
  }

}
