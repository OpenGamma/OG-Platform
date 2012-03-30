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
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class with methods related to bill transaction valued by discounting.
 * <P> Reference: Bill pricing, version 1.0. OpenGamma documentation, January 2012.
 */
public final class BillTransactionDiscountingMethod implements PricingMethod {

  /**
   * The unique instance of the class.
   */
  private static final BillTransactionDiscountingMethod INSTANCE = new BillTransactionDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BillTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor
   */
  private BillTransactionDiscountingMethod() {
  }

  /**
   * Methods.
   */
  private static final BillSecurityDiscountingMethod METHOD_SECURITY = BillSecurityDiscountingMethod.getInstance();

  /**
   * Computes the bill transaction present value.
   * @param bill The bill.
   * @param curves The curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final BillTransaction bill, final YieldCurveBundle curves) {
    Validate.notNull(bill, "Bill");
    Validate.notNull(curves, "Curves");
    CurrencyAmount pvBill = METHOD_SECURITY.presentValue(bill.getBillPurchased(), curves);
    double pvSettle = bill.getSettlementAmount() * curves.getCurve(bill.getBillPurchased().getDiscountingCurveName()).getDiscountFactor(bill.getBillPurchased().getSettlementTime());
    return pvBill.multipliedBy(bill.getQuantity()).plus(pvSettle);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof BillTransaction, "Bill Transaction");
    return presentValue((BillTransaction) instrument, curves);
  }

  /**
   * Computes the bill present value curve sensitivity.
   * @param bill The bill.
   * @param curves The curves.
   * @return The sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final BillTransaction bill, final YieldCurveBundle curves) {
    Validate.notNull(bill, "Bill");
    Validate.notNull(curves, "Curves");
    double dfCreditEnd = curves.getCurve(bill.getBillPurchased().getCreditCurveName()).getDiscountFactor(bill.getBillPurchased().getEndTime());
    double dfDscSettle = curves.getCurve(bill.getBillPurchased().getDiscountingCurveName()).getDiscountFactor(bill.getBillPurchased().getSettlementTime());
    // Backward sweep
    double pvBar = 1.0;
    double dfCreditEndBar = bill.getQuantity() * bill.getBillPurchased().getNotional() * pvBar;
    double dfDscSettleBar = bill.getSettlementAmount() * pvBar;
    final Map<String, List<DoublesPair>> resultMapCredit = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listCredit = new ArrayList<DoublesPair>();
    listCredit.add(new DoublesPair(bill.getBillPurchased().getEndTime(), -bill.getBillPurchased().getEndTime() * dfCreditEnd * dfCreditEndBar));
    resultMapCredit.put(bill.getBillPurchased().getCreditCurveName(), listCredit);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMapCredit);
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDsc = new ArrayList<DoublesPair>();
    listDsc.add(new DoublesPair(bill.getBillPurchased().getSettlementTime(), -bill.getBillPurchased().getSettlementTime() * dfDscSettle * dfDscSettleBar));
    resultMapDsc.put(bill.getBillPurchased().getDiscountingCurveName(), listDsc);
    return result.plus(new InterestRateCurveSensitivity(resultMapDsc));
  }

}
