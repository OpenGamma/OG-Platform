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

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class with methods related to bill transaction valued by discounting.
 * <P> Reference: Bill pricing, version 1.0. OpenGamma documentation, January 2012.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.bond.provider.BillTransactionDiscountingMethod}
 */
@Deprecated
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
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(curves, "Curves");
    final CurrencyAmount pvBill = METHOD_SECURITY.presentValue(bill.getBillPurchased(), curves);
    final double pvSettle = bill.getSettlementAmount() * curves.getCurve(bill.getBillPurchased().getDiscountingCurveName()).getDiscountFactor(bill.getBillPurchased().getSettlementTime());
    return pvBill.multipliedBy(bill.getQuantity()).plus(pvSettle);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof BillTransaction, "Bill Transaction");
    return presentValue((BillTransaction) instrument, curves);
  }

  /**
   * Computes the bill present value curve sensitivity.
   * @param bill The bill.
   * @param curves The curves.
   * @return The sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final BillTransaction bill, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(curves, "Curves");
    final double dfCreditEnd = curves.getCurve(bill.getBillPurchased().getCreditCurveName()).getDiscountFactor(bill.getBillPurchased().getEndTime());
    final double dfDscSettle = curves.getCurve(bill.getBillPurchased().getDiscountingCurveName()).getDiscountFactor(bill.getBillPurchased().getSettlementTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfCreditEndBar = bill.getQuantity() * bill.getBillPurchased().getNotional() * pvBar;
    final double dfDscSettleBar = bill.getSettlementAmount() * pvBar;
    final Map<String, List<DoublesPair>> resultMapCredit = new HashMap<>();
    final List<DoublesPair> listCredit = new ArrayList<>();
    listCredit.add(DoublesPair.of(bill.getBillPurchased().getEndTime(), -bill.getBillPurchased().getEndTime() * dfCreditEnd * dfCreditEndBar));
    resultMapCredit.put(bill.getBillPurchased().getCreditCurveName(), listCredit);
    final InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMapCredit);
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    final List<DoublesPair> listDsc = new ArrayList<>();
    listDsc.add(DoublesPair.of(bill.getBillPurchased().getSettlementTime(), -bill.getBillPurchased().getSettlementTime() * dfDscSettle * dfDscSettleBar));
    resultMapDsc.put(bill.getBillPurchased().getDiscountingCurveName(), listDsc);
    return result.plus(new InterestRateCurveSensitivity(resultMapDsc));
  }

  /**
   * The par spread for which the present value of the bill transaction is 0. If that spread was added to the transaction yield, the new transaction would have a present value of 0.
   * @param bill The bill transaction.
   * @param curves The yield curve bundle.
   * @return The spread.
   */
  public double parSpread(final BillTransaction bill, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(curves, "Curves");
    final double dfCreditEnd = curves.getCurve(bill.getBillPurchased().getCreditCurveName()).getDiscountFactor(bill.getBillPurchased().getEndTime());
    final double dfDscSettle = curves.getCurve(bill.getBillPurchased().getDiscountingCurveName()).getDiscountFactor(bill.getBillPurchased().getSettlementTime());
    final double pricePar = dfCreditEnd / dfDscSettle;
    return METHOD_SECURITY.yieldFromPrice(bill.getBillPurchased(), pricePar)
        - METHOD_SECURITY.yieldFromPrice(bill.getBillPurchased(), -bill.getSettlementAmount() / (bill.getQuantity() * bill.getBillPurchased().getNotional()));
  }

  public InterestRateCurveSensitivity parSpreadCurveSensitivity(final BillTransaction bill, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(curves, "Curves");
    final double dfCreditEnd = curves.getCurve(bill.getBillPurchased().getCreditCurveName()).getDiscountFactor(bill.getBillPurchased().getEndTime());
    final double dfDscSettle = curves.getCurve(bill.getBillPurchased().getDiscountingCurveName()).getDiscountFactor(bill.getBillPurchased().getSettlementTime());
    final double pricePar = dfCreditEnd / dfDscSettle;
    //    double spread = METHOD_SECURITY.yieldFromPrice(bill.getBillPurchased(), pricePar)
    //        - METHOD_SECURITY.yieldFromPrice(bill.getBillPurchased(), -bill.getSettlementAmount() / (bill.getQuantity() * bill.getBillPurchased().getNotional()));
    // Backward sweep
    final double spreadBar = 1.0;
    final double priceParBar = METHOD_SECURITY.yieldFromPriceDerivative(bill.getBillPurchased(), pricePar) * spreadBar;
    final double dfDscSettleBar = -dfCreditEnd / (dfDscSettle * dfDscSettle) * priceParBar;
    final double dfCreditEndBar = priceParBar / dfDscSettle;
    final Map<String, List<DoublesPair>> resultMapCredit = new HashMap<>();
    final List<DoublesPair> listCredit = new ArrayList<>();
    listCredit.add(DoublesPair.of(bill.getBillPurchased().getEndTime(), -bill.getBillPurchased().getEndTime() * dfCreditEnd * dfCreditEndBar));
    resultMapCredit.put(bill.getBillPurchased().getCreditCurveName(), listCredit);
    final InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMapCredit);
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    final List<DoublesPair> listDsc = new ArrayList<>();
    listDsc.add(DoublesPair.of(bill.getBillPurchased().getSettlementTime(), -bill.getBillPurchased().getSettlementTime() * dfDscSettle * dfDscSettleBar));
    resultMapDsc.put(bill.getBillPurchased().getDiscountingCurveName(), listDsc);
    return result.plus(new InterestRateCurveSensitivity(resultMapDsc));
  }

}
