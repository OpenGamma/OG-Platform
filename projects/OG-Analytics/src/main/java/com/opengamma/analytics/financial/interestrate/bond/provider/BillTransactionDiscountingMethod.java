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

import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class with methods related to bill transaction valued by discounting.
 * <P> Reference: Bill pricing, version 1.0. OpenGamma documentation, January 2012.
 */
public final class BillTransactionDiscountingMethod {

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
   * @param issuer The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BillTransaction bill, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(issuer, "Issuer and multi-curves provider");
    final Currency ccy = bill.getCurrency();
    final MultipleCurrencyAmount pvBill = METHOD_SECURITY.presentValue(bill.getBillPurchased(), issuer);
    final double pvSettle = bill.getSettlementAmount() * issuer.getMulticurveProvider().getDiscountFactor(ccy, bill.getBillPurchased().getSettlementTime());
    return pvBill.multipliedBy(bill.getQuantity()).plus(MultipleCurrencyAmount.of(ccy, pvSettle));
  }

  /**
   * Computes the bill transaction present value from the quoted yield to maturity.
   * @param bill The bill.
   * @param issuer The issuer and multi-curves provider.
   * @param yield The yield.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromYield(final BillTransaction bill, final double yield, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(issuer, "Issuer and multi-curves provider");
    final Currency ccy = bill.getCurrency();
    final MultipleCurrencyAmount pvSecurity = METHOD_SECURITY.presentValueFromYield(bill.getBillStandard(), yield, issuer);
    final double pvSettle = bill.getSettlementAmount() * issuer.getMulticurveProvider().getDiscountFactor(ccy, bill.getBillPurchased().getSettlementTime());
    return pvSecurity.plus(MultipleCurrencyAmount.of(bill.getCurrency(), pvSettle));
  }

  /**
   * Computes the bill present value curve sensitivity.
   * @param bill The bill.
   * @param issuer The issuer and multi-curves provider.
   * @return The sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final BillTransaction bill, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(issuer, "Issuer and multi-curves provider");
    final Currency ccy = bill.getCurrency();
    final double dfCreditEnd = issuer.getDiscountFactor(bill.getBillPurchased().getIssuerEntity(), bill.getBillPurchased().getEndTime());
    final double dfDscSettle = issuer.getMulticurveProvider().getDiscountFactor(ccy, bill.getBillPurchased().getSettlementTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfCreditEndBar = bill.getQuantity() * bill.getBillPurchased().getNotional() * pvBar;
    final double dfDscSettleBar = bill.getSettlementAmount() * pvBar;
    final Map<String, List<DoublesPair>> resultMapCredit = new HashMap<>();
    final List<DoublesPair> listCredit = new ArrayList<>();
    listCredit.add(DoublesPair.of(bill.getBillPurchased().getEndTime(), -bill.getBillPurchased().getEndTime() * dfCreditEnd * dfCreditEndBar));
    resultMapCredit.put(issuer.getName(bill.getBillPurchased().getIssuerEntity()), listCredit);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMapCredit);
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    final List<DoublesPair> listDsc = new ArrayList<>();
    listDsc.add(DoublesPair.of(bill.getBillPurchased().getSettlementTime(), -bill.getBillPurchased().getSettlementTime() * dfDscSettle * dfDscSettleBar));
    resultMapDsc.put(issuer.getMulticurveProvider().getName(ccy), listDsc);
    return MultipleCurrencyMulticurveSensitivity.of(ccy, result.plus(MulticurveSensitivity.ofYieldDiscounting(resultMapDsc)));
  }

  /**
   * The par spread for which the present value of the bill transaction is 0. If that spread was added to the transaction yield, the new transaction would have a present value of 0.
   * @param bill The bill transaction.
   * @param issuer The issuer and multi-curves provider.
   * @return The spread.
   */
  public double parSpread(final BillTransaction bill, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(issuer, "Issuer and multi-curves provider");
    final Currency ccy = bill.getCurrency();
    final double dfCreditEnd = issuer.getDiscountFactor(bill.getBillPurchased().getIssuerEntity(), bill.getBillPurchased().getEndTime());
    final double dfDscSettle = issuer.getMulticurveProvider().getDiscountFactor(ccy, bill.getBillPurchased().getSettlementTime());
    final double pricePar = dfCreditEnd / dfDscSettle;
    return METHOD_SECURITY.yieldFromCleanPrice(bill.getBillPurchased(), pricePar)
        - METHOD_SECURITY.yieldFromCleanPrice(bill.getBillPurchased(), -bill.getSettlementAmount() / (bill.getQuantity() * bill.getBillPurchased().getNotional()));
  }

  /**
   * The par spread curve sensitivity.
   * @param bill The bill transaction.
   * @param issuer The issuer and multi-curves provider.
   * @return The curve sensitivity.
   */
  public MulticurveSensitivity parSpreadCurveSensitivity(final BillTransaction bill, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bill, "Bill");
    ArgumentChecker.notNull(issuer, "Issuer and multi-curves provider");
    final Currency ccy = bill.getCurrency();
    final double dfCreditEnd = issuer.getDiscountFactor(bill.getBillPurchased().getIssuerEntity(), bill.getBillPurchased().getEndTime());
    final double dfDscSettle = issuer.getMulticurveProvider().getDiscountFactor(ccy, bill.getBillPurchased().getSettlementTime());
    final double pricePar = dfCreditEnd / dfDscSettle;
    // Backward sweep
    final double spreadBar = 1.0;
    final double priceParBar = METHOD_SECURITY.yieldFromPriceDerivative(bill.getBillPurchased(), pricePar) * spreadBar;
    final double dfDscSettleBar = -dfCreditEnd / (dfDscSettle * dfDscSettle) * priceParBar;
    final double dfCreditEndBar = priceParBar / dfDscSettle;
    final Map<String, List<DoublesPair>> resultMapCredit = new HashMap<>();
    final List<DoublesPair> listCredit = new ArrayList<>();
    listCredit.add(DoublesPair.of(bill.getBillPurchased().getEndTime(), -bill.getBillPurchased().getEndTime() * dfCreditEnd * dfCreditEndBar));
    resultMapCredit.put(issuer.getName(bill.getBillPurchased().getIssuerEntity()), listCredit);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMapCredit);
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    final List<DoublesPair> listDsc = new ArrayList<>();
    listDsc.add(DoublesPair.of(bill.getBillPurchased().getSettlementTime(), -bill.getBillPurchased().getSettlementTime() * dfDscSettle * dfDscSettleBar));
    resultMapDsc.put(issuer.getMulticurveProvider().getName(ccy), listDsc);
    return result.plus(MulticurveSensitivity.ofYieldDiscounting(resultMapDsc));
  }

}
