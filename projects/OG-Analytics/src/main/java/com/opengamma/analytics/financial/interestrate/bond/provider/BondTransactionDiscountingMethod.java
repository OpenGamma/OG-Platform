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
import com.opengamma.analytics.financial.provider.description.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountingDecoratedIssuer;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
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
    Currency ccy = bond.getBondTransaction().getCurrency();
    MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, ccy, bond.getBondTransaction().getIssuer());
    final MultipleCurrencyAmount pvNominal = PVDC.visit(bond.getBondTransaction().getNominal(), multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = PVDC.visit(bond.getBondTransaction().getCoupon(), multicurvesDecorated);
    final double settlementAmount = -(bond.getTransactionPrice() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional() + bond.getBondTransaction().getAccruedInterest())
        * bond.getQuantity();
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), settlementAmount, bond.getBondTransaction()
        .getRepoCurveName());
    final MultipleCurrencyAmount pvSettlement = PVDC.visit(settlement, issuerMulticurves.getMulticurveProvider());
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
    Currency ccy = bond.getBondTransaction().getCurrency();
    MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, ccy, bond.getBondTransaction().getIssuer());
    final MultipleCurrencyAmount pvNominal = PVDC.visit(bond.getBondTransaction().getNominal(), multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = PVDC.visit(bond.getBondTransaction().getCoupon(), multicurvesDecorated);
    final double settlementAmount = bond.getTransactionPrice() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional(); //FIXME: add accrued.
    LOGGER.error("The FRN settlement amount does not include the accrued interests.");
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), settlementAmount, bond.getBondTransaction()
        .getRepoCurveName());
    final MultipleCurrencyAmount pvSettlement = PVDC.visit(settlement, issuerMulticurves.getMulticurveProvider());
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
    Currency ccy = bond.getBondTransaction().getCurrency();
    MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, ccy, bond.getBondTransaction().getIssuer());
    final BondFixedTransaction bondFixed = (BondFixedTransaction) bond;
    final double dfSettle = issuerMulticurves.getMulticurveProvider().getDiscountFactor(ccy, bondFixed.getBondTransaction().getSettlementTime());
    final MultipleCurrencyAmount pvPriceStandard = MultipleCurrencyAmount.of(ccy, (cleanPrice * bondFixed.getNotionalStandard() + bondFixed.getBondStandard().getAccruedInterest()) * dfSettle);
    final MultipleCurrencyAmount pvNominalStandard = PVDC.visit(bond.getBondStandard().getNominal(), multicurvesDecorated);
    final MultipleCurrencyAmount pvCouponStandard = PVDC.visit(bond.getBondStandard().getCoupon(), multicurvesDecorated);
    final MultipleCurrencyAmount pvDiscountingStandard = pvNominalStandard.plus(pvCouponStandard);
    final MultipleCurrencyAmount pvNominalTransaction = PVDC.visit(bond.getBondTransaction().getNominal(), multicurvesDecorated);
    final MultipleCurrencyAmount pvCouponTransaction = PVDC.visit(bond.getBondTransaction().getCoupon(), multicurvesDecorated);
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
    Currency ccy = bond.getBondTransaction().getCurrency();
    MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, ccy, bond.getBondTransaction().getIssuer());
    final MultipleCurrencyMulticurveSensitivity pvcsNominal = PVSDC.visit(bond.getBondTransaction().getNominal(), multicurvesDecorated);
    final MultipleCurrencyMulticurveSensitivity pvcsCoupon = PVSDC.visit(bond.getBondTransaction().getCoupon(), multicurvesDecorated);
    final double settlementAmount = -(bond.getTransactionPrice() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional() + bond.getBondTransaction().getAccruedInterest())
        * bond.getQuantity();
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), settlementAmount, bond.getBondTransaction()
        .getRepoCurveName());
    final MultipleCurrencyMulticurveSensitivity pvcsSettlement = PVSDC.visit(settlement, issuerMulticurves.getMulticurveProvider());
    return pvcsNominal.plus(pvcsCoupon).multipliedBy(bond.getQuantity()).plus(pvcsSettlement);
  }

  public MultipleCurrencyMulticurveSensitivity presentValueSensitivity(final BondIborTransaction bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    Currency ccy = bond.getBondTransaction().getCurrency();
    MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, ccy, bond.getBondTransaction().getIssuer());
    final MultipleCurrencyMulticurveSensitivity pvcsNominal = PVSDC.visit(bond.getBondTransaction().getNominal(), multicurvesDecorated);
    final MultipleCurrencyMulticurveSensitivity pvcsCoupon = PVSDC.visit(bond.getBondTransaction().getCoupon(), multicurvesDecorated);
    final double settlementAmount = bond.getTransactionPrice() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional(); //FIXME: add accrued.
    LOGGER.error("The FRN settlement amount does not include the accrued interests.");
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), settlementAmount, bond.getBondTransaction()
        .getRepoCurveName());
    final MultipleCurrencyMulticurveSensitivity pvcsSettlement = PVSDC.visit(settlement, issuerMulticurves.getMulticurveProvider());
    return pvcsNominal.plus(pvcsCoupon).multipliedBy(bond.getQuantity()).plus(pvcsSettlement);
  }
}
