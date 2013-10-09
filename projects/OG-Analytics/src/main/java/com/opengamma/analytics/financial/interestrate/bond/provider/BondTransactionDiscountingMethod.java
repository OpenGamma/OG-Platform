/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscountingDecoratedIssuer;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Class with methods related to bond transaction valued by discounting.
 */
public final class BondTransactionDiscountingMethod {

  private static final Logger LOGGER = LoggerFactory.getLogger(BondTransactionDiscountingMethod.class);
  /**
   * The unique instance of the class.
   */
  private static final BondTransactionDiscountingMethod INSTANCE = new BondTransactionDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BondTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor
   */
  private BondTransactionDiscountingMethod() {
  }

  /**
   * The present value calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  /**
   * The present value calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueCurveSensitivityDiscountingCalculator PVSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

  /**
   * Compute the present value of a fixed coupon bond transaction.
   * @param bond The bond transaction.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondFixedTransaction bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    final Currency ccy = bond.getBondTransaction().getCurrency();
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, ccy, bond.getBondTransaction().getIssuer());
    final MultipleCurrencyAmount pvNominal = bond.getBondTransaction().getNominal().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = bond.getBondTransaction().getCoupon().accept(PVDC, multicurvesDecorated);
    final double settlementAmount = -(bond.getTransactionPrice() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional() + bond.getBondTransaction().getAccruedInterest())
        * bond.getQuantity();
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), settlementAmount);
    final MultipleCurrencyAmount pvSettlement = settlement.accept(PVDC, issuerMulticurves.getMulticurveProvider());
    return pvNominal.plus(pvCoupon).multipliedBy(bond.getQuantity()).plus(pvSettlement);
  }

  /**
   * Compute the present value of a Ibor coupon bond (FRN) transaction.
   * @param bond The bond transaction.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondIborTransaction bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    final Currency ccy = bond.getBondTransaction().getCurrency();
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, ccy, bond.getBondTransaction().getIssuer());
    final MultipleCurrencyAmount pvNominal = bond.getBondTransaction().getNominal().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = bond.getBondTransaction().getCoupon().accept(PVDC, multicurvesDecorated);
    final double settlementAmount = bond.getTransactionPrice() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional(); //FIXME: add accrued.
    LOGGER.error("The FRN settlement amount does not include the accrued interests.");
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), settlementAmount);
    final MultipleCurrencyAmount pvSettlement = settlement.accept(PVDC, issuerMulticurves.getMulticurveProvider());
    return pvNominal.plus(pvCoupon).multipliedBy(bond.getQuantity()).plus(pvSettlement);
  }

  /**
   * Compute the present value of a bond transaction from its clean price.
   * @param bond The bond transaction.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param cleanPrice The bond clean price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromCleanPrice(final BondTransaction<? extends BondSecurity<? extends Payment, ? extends Coupon>> bond, final IssuerProviderInterface issuerMulticurves,
      final double cleanPrice) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.isTrue(bond instanceof BondFixedTransaction, "Present value from clean price only for fixed coupon bond");
    final Currency ccy = bond.getBondTransaction().getCurrency();
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, ccy, bond.getBondTransaction().getIssuer());
    final BondFixedTransaction bondFixed = (BondFixedTransaction) bond;
    final double dfSettle = issuerMulticurves.getMulticurveProvider().getDiscountFactor(ccy, bondFixed.getBondTransaction().getSettlementTime());
    final MultipleCurrencyAmount pvPriceStandard = MultipleCurrencyAmount.of(ccy, (cleanPrice * bondFixed.getNotionalStandard() + bondFixed.getBondStandard().getAccruedInterest()) * dfSettle);
    final MultipleCurrencyAmount pvNominalStandard = bond.getBondStandard().getNominal().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCouponStandard = bond.getBondStandard().getCoupon().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvDiscountingStandard = pvNominalStandard.plus(pvCouponStandard);
    final MultipleCurrencyAmount pvNominalTransaction = bond.getBondTransaction().getNominal().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCouponTransaction = bond.getBondTransaction().getCoupon().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvDiscountingTransaction = pvNominalTransaction.plus(pvCouponTransaction);
    return pvDiscountingTransaction.plus(pvDiscountingStandard.multipliedBy(-1d)).multipliedBy(bond.getQuantity()).plus(pvPriceStandard);
  }

  /**
   * Compute the present value sensitivity of a bond transaction.
   * @param bond The bond transaction.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueSensitivity(final BondFixedTransaction bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    final Currency ccy = bond.getBondTransaction().getCurrency();
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, ccy, bond.getBondTransaction().getIssuer());
    final MultipleCurrencyMulticurveSensitivity pvcsNominal = bond.getBondTransaction().getNominal().accept(PVSDC, multicurvesDecorated);
    final MultipleCurrencyMulticurveSensitivity pvcsCoupon = bond.getBondTransaction().getCoupon().accept(PVSDC, multicurvesDecorated);
    final double settlementAmount = -(bond.getTransactionPrice() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional() + bond.getBondTransaction().getAccruedInterest())
        * bond.getQuantity();
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), settlementAmount);
    final MultipleCurrencyMulticurveSensitivity pvcsSettlement = settlement.accept(PVSDC, issuerMulticurves.getMulticurveProvider());
    return pvcsNominal.plus(pvcsCoupon).multipliedBy(bond.getQuantity()).plus(pvcsSettlement);
  }

  public MultipleCurrencyMulticurveSensitivity presentValueSensitivity(final BondIborTransaction bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    final Currency ccy = bond.getBondTransaction().getCurrency();
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, ccy, bond.getBondTransaction().getIssuer());
    final MultipleCurrencyMulticurveSensitivity pvcsNominal = bond.getBondTransaction().getNominal().accept(PVSDC, multicurvesDecorated);
    final MultipleCurrencyMulticurveSensitivity pvcsCoupon = bond.getBondTransaction().getCoupon().accept(PVSDC, multicurvesDecorated);
    final double settlementAmount = bond.getTransactionPrice() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional(); //FIXME: add accrued.
    LOGGER.error("The FRN settlement amount does not include the accrued interests.");
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), settlementAmount);
    final MultipleCurrencyMulticurveSensitivity pvcsSettlement = settlement.accept(PVSDC, issuerMulticurves.getMulticurveProvider());
    return pvcsNominal.plus(pvcsCoupon).multipliedBy(bond.getQuantity()).plus(pvcsSettlement);
  }

  /**
   * The par spread for which the present value of the bond transaction is 0. If that spread was added to the transaction yield, the new transaction would have a present value of 0.
   * @param bond The bond transaction.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The spread.
   */
  public double parSpread(final BondFixedTransaction bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final Currency ccy = bond.getBondTransaction().getCurrency();
    final PaymentFixed nominalAtSettlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), bond.getBondTransaction().getCoupon()
        .getNthPayment(0).getNotional() *
        bond.getQuantity());
    final double pvNominalAtSettlement = nominalAtSettlement.accept(PVDC, issuerMulticurves.getMulticurveProvider()).getAmount(ccy);
    return -presentValue(bond, issuerMulticurves).getAmount(ccy) / pvNominalAtSettlement;
  }

  public double parSpread(final BondIborTransaction bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final Currency ccy = bond.getBondTransaction().getCurrency();
    final PaymentFixed nominalAtSettlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), bond.getBondTransaction().getCoupon()
        .getNthPayment(0).getNotional() *
        bond.getQuantity());
    final double pvNominalAtSettlement = nominalAtSettlement.accept(PVDC, issuerMulticurves.getMulticurveProvider()).getAmount(ccy);
    return -presentValue(bond, issuerMulticurves).getAmount(ccy) / pvNominalAtSettlement;
  }

  /**
   * The par spread curve sensitivity.
   * @param bond The bond transaction.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The curve sensitivity.
   */
  public MulticurveSensitivity parSpreadCurveSensitivity(final BondFixedTransaction bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final Currency ccy = bond.getBondTransaction().getCurrency();
    final PaymentFixed nominalAtSettlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), bond.getBondTransaction().getCoupon()
        .getNthPayment(0).getNotional() *
        bond.getQuantity());
    final double pvNominalAtSettlement = nominalAtSettlement.accept(PVDC, issuerMulticurves.getMulticurveProvider()).getAmount(ccy);
    final MulticurveSensitivity pvcsNominalAtSettlement = nominalAtSettlement.accept(PVSDC, issuerMulticurves.getMulticurveProvider()).getSensitivity(ccy);
    final MulticurveSensitivity pvcsBond = presentValueSensitivity(bond, issuerMulticurves).getSensitivity(ccy);
    final double pvBond = presentValue(bond, issuerMulticurves).getAmount(ccy);
    return pvcsBond.multipliedBy(-1 / pvNominalAtSettlement).plus(pvcsNominalAtSettlement.multipliedBy(pvBond / (pvNominalAtSettlement * pvNominalAtSettlement)));
  }

  public MulticurveSensitivity parSpreadCurveSensitivity(final BondIborTransaction bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final Currency ccy = bond.getBondTransaction().getCurrency();
    final PaymentFixed nominalAtSettlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), bond.getBondTransaction().getCoupon()
        .getNthPayment(0).getNotional() *
        bond.getQuantity());
    final double pvNominalAtSettlement = nominalAtSettlement.accept(PVDC, issuerMulticurves.getMulticurveProvider()).getAmount(ccy);
    final MulticurveSensitivity pvcsNominalAtSettlement = nominalAtSettlement.accept(PVSDC, issuerMulticurves.getMulticurveProvider()).getSensitivity(ccy);
    final MulticurveSensitivity pvcsBond = presentValueSensitivity(bond, issuerMulticurves).getSensitivity(ccy);
    final double pvBond = presentValue(bond, issuerMulticurves).getAmount(ccy);
    return pvcsBond.multipliedBy(-1 / pvNominalAtSettlement).plus(pvcsNominalAtSettlement.multipliedBy(pvBond / (pvNominalAtSettlement * pvNominalAtSettlement)));
  }

}
