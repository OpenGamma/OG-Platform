/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

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
   * @param issuer The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BillSecurity bill, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(issuer, "Issuer and multi-curves provider");
    final double pvBill = bill.getNotional() * issuer.getDiscountFactor(bill.getIssuerEntity(), bill.getEndTime());
    return MultipleCurrencyAmount.of(bill.getCurrency(), pvBill);
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
  public double yieldFromCleanPrice(final BillSecurity bill, final double price) {
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
   * @param issuer The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromYield(final BillSecurity bill, final double yield, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(issuer, "Issuer and multi-curves provider");
    final double price = priceFromYield(bill, yield);
    return presentValueFromPrice(bill, price, issuer);
  }

  /**
   * Computes the present value of the bill security by discounting from its price.
   * @param bill The bill.
   * @param price The (dirty) price at settlement.
   * @param issuer The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromPrice(final BillSecurity bill, final double price, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(issuer, "Issuer and multi-curves provider");
    final double pvBill = bill.getNotional() * price * issuer.getMulticurveProvider().getDiscountFactor(bill.getCurrency(), bill.getSettlementTime());
    return MultipleCurrencyAmount.of(bill.getCurrency(), pvBill);
  }

  /**
   * Compute the bill price from the curves. The price is the relative price at settlement.
   * @param bill The bill.
   * @param issuer The issuer and multi-curves provider.
   * @return The price.
   */
  public double priceFromCurves(final BillSecurity bill, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(issuer, "Issuer and multi-curves provider");
    final double pvBill = bill.getNotional() * issuer.getDiscountFactor(bill.getIssuerEntity(), bill.getEndTime());
    final double price = pvBill / (bill.getNotional() * issuer.getMulticurveProvider().getDiscountFactor(bill.getCurrency(), bill.getSettlementTime()));
    return price;
  }

  /**
   * Computes the bill yield from the curves. The yield is in the bill yield convention.
   * @param bill The bill.
   * @param issuer The issuer and multi-curves provider.
   * @return The yield.
   */
  public double yieldFromCurves(final BillSecurity bill, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(issuer, "Issuer and multi-curves provider");
    final double pvBill = bill.getNotional() * issuer.getDiscountFactor(bill.getIssuerEntity(), bill.getEndTime());
    final double price = pvBill / (bill.getNotional() * issuer.getMulticurveProvider().getDiscountFactor(bill.getCurrency(), bill.getSettlementTime()));
    return yieldFromCleanPrice(bill, price);
  }

  /**
   * Computes the bill present value curve sensitivity.
   * @param bill The bill.
   * @param issuer The issuer and multi-curves provider.
   * @return The sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final BillSecurity bill, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(issuer, "Issuer and multi-curves provider");
    final double dfEnd = issuer.getDiscountFactor(bill.getIssuerEntity(), bill.getEndTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfEndBar = bill.getNotional() * pvBar;
    final Map<String, List<DoublesPair>> resultMapCredit = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(bill.getEndTime(), -bill.getEndTime() * dfEnd * dfEndBar));
    resultMapCredit.put(issuer.getName(bill.getIssuerEntity()), listDiscounting);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMapCredit);
    return MultipleCurrencyMulticurveSensitivity.of(bill.getCurrency(), result);
  }

}
