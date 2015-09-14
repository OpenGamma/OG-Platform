/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Pricing method for inflation bond transaction. The price is computed by index estimation and discounting.
 */
public final class BondCapitalIndexedTransactionDiscountingMethod {

  /**
   * The unique instance of the class.
   */
  private static final BondCapitalIndexedTransactionDiscountingMethod INSTANCE = new BondCapitalIndexedTransactionDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BondCapitalIndexedTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The present value inflation calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSIC = PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  /**
   * The method used for security computation.
   */
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_SECURITY = 
      new BondCapitalIndexedSecurityDiscountingMethod();

  /**
   * Computes the present value of a capital indexed bound transaction by index estimation and discounting.
   * @param bond The bond transaction.
   * @param provider The provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondCapitalIndexedTransaction<?> bond, 
      final ParameterInflationIssuerProviderInterface provider) {
    double notional = bond.getNotionalStandard();
    final MultipleCurrencyAmount pvBond = METHOD_SECURITY.presentValue(bond.getBondTransaction(), provider);
    final MultipleCurrencyAmount pvSettlement = bond.getBondTransaction().getSettlement().
        accept(PVIC, provider.getInflationProvider()).multipliedBy(-bond.getQuantity() * (bond.getTransactionPrice() 
            + bond.getBondStandard().getAccruedInterest() / notional));
    return pvBond.multipliedBy(bond.getQuantity()).plus(pvSettlement);
  }

  /**
   * Computes the security present value from a quoted clean real price.
   * @param bond The bond transaction.
   * @param provider The provider.
   * @param cleanPriceReal The clean price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromCleanPriceReal(final BondCapitalIndexedTransaction<Coupon> bond, 
      final InflationIssuerProviderInterface provider, final double cleanPriceReal) {
    Validate.notNull(bond, "Coupon");
    Validate.notNull(provider, "Provider");
    final MultipleCurrencyAmount pvBond = METHOD_SECURITY.presentValueFromCleanRealPrice(bond.getBondTransaction(), provider, cleanPriceReal);
    final MultipleCurrencyAmount pvSettlement = bond.getBondTransaction().getSettlement().accept(PVIC, provider.getInflationProvider()).multipliedBy(
        bond.getQuantity() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional());
    return pvBond.plus(pvSettlement);
  }

  /**
   * Computes the present value of a capital indexed bound transaction by index estimation and discounting.
   * @param bond The bond transaction.
   * @param provider The provider.
   * @return The present value.
   */
  public MultipleCurrencyInflationSensitivity presentValueCurveSensitivity(final BondCapitalIndexedTransaction<?> bond, 
      final ParameterInflationIssuerProviderInterface provider) {
    double notional = bond.getNotionalStandard();
    final MultipleCurrencyInflationSensitivity sensitivityBond = 
        METHOD_SECURITY.presentValueCurveSensitivity(bond.getBondTransaction(), provider);
    final MultipleCurrencyInflationSensitivity sensitivitySettlement = bond.getBondTransaction().getSettlement().
        accept(PVCSIC, provider.getInflationProvider()).multipliedBy(-bond.getQuantity() * (bond.getTransactionPrice() 
            + bond.getBondStandard().getAccruedInterest() / notional));
    return sensitivityBond.multipliedBy(bond.getQuantity()).plus(sensitivitySettlement);
  }
  
  /**
   * Returns the par spread price of the bond, i.e. the same transaction with a price increased by the spread will have
   * a total present value of 0.
   * @param bond The bond transaction.
   * @param provider The inflation, issuer and multi-curve provider.
   * @return The spread.
   */
  public double parSpread(final BondCapitalIndexedTransaction<?> bond, 
      final ParameterInflationIssuerProviderInterface provider) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(provider, "inflation-issuer provider");
    Currency ccy = bond.getBondTransaction().getCurrency();
    MultipleCurrencyAmount pvSettle = bond.getBondTransaction().getSettlement().accept(PVIC, provider.getInflationProvider());
    ArgumentChecker.isTrue(pvSettle.getAmount(ccy) != 0.0, 
        "Present value of settlement is 0; probably the settlement took place in the past. ParSpread cannot be computed.");
    MultipleCurrencyAmount pvTransaction = presentValue(bond, provider);
    return (pvTransaction.getAmount(ccy) / bond.getQuantity()) / pvSettle.getAmount(ccy);
  }

  
  /**
   * Returns the par spread price curve sensitivity.
   * the par spread price is the number such that the same transaction with a price increased by the spread will have
   * a total present value of 0.
   * @param bond The bond transaction.
   * @param provider The inflation, issuer and multi-curve provider.
   * @return The spread.
   */
  public InflationSensitivity parSpreadCurveSensitivity(final BondCapitalIndexedTransaction<?> bond, 
      final ParameterInflationIssuerProviderInterface provider) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(provider, "inflation-issuer provider");
    Currency ccy = bond.getBondTransaction().getCurrency();
    double pvSettle = bond.getBondTransaction().getSettlement().accept(PVIC, provider.getInflationProvider()).getAmount(ccy);
    ArgumentChecker.isTrue(pvSettle != 0.0, 
        "Present value of settlement is 0; probably the settlement took place in the past. ParSpread cannot be computed.");
    double pvTransaction = presentValue(bond, provider).getAmount(ccy);
    double pv = pvTransaction / (bond.getQuantity() * pvSettle);
    InflationSensitivity pvTransactionDr = presentValueCurveSensitivity(bond, provider).getSensitivity(ccy);
    InflationSensitivity pvSettleDr = bond.getBondTransaction().getSettlement().
        accept(PVCSIC, provider.getInflationProvider()).getSensitivity(ccy);
    return pvTransactionDr.multipliedBy(1.0d / (bond.getQuantity() * pvSettle)).
        plus(pvSettleDr.multipliedBy(-pv / pvSettle));
  }

}
