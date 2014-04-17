/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;

/**
 * Returns the discount factors for each payment of a bond.
 */
public final class BondDiscountFactorsVisitor extends InstrumentDerivativeVisitorAdapter<IssuerProviderInterface, double[]> {
  /** A singleton instance */
  private static final InstrumentDerivativeVisitor<IssuerProviderInterface, double[]> INSTANCE = new BondDiscountFactorsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDerivativeVisitor<IssuerProviderInterface, double[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private BondDiscountFactorsVisitor() {
  }

  @Override
  public double[] visitBondFixedSecurity(final BondFixedSecurity bond, final IssuerProviderInterface data) {
    return visitGenericBond(bond, data);
  }

  @Override
  public double[] visitBondFixedTransaction(final BondFixedTransaction bond, final IssuerProviderInterface data) {
    return visitGenericBond(bond.getBondTransaction(), data);
  }

  @Override
  public double[] visitBondIborSecurity(final BondIborSecurity bond, final IssuerProviderInterface data) {
    return visitGenericBond(bond, data);
  }

  @Override
  public double[] visitBondIborTransaction(final BondIborTransaction bond, final IssuerProviderInterface data) {
    return visitGenericBond(bond.getBondTransaction(), data);
  }

  /**
   * Returns the discount factors for a generic bond.
   * @param bond The bond
   * @param curves The curves
   * @return The discount factors.
   */
  private static double[] visitGenericBond(final BondSecurity<? extends Payment, ? extends Coupon> bond, final IssuerProviderInterface curves) {
    final int n = bond.getCoupon().getNumberOfPayments();
    final double[] fractions = new double[n];
    for (int i = 0; i < n; i++) {
      fractions[i] = curves.getDiscountFactor(bond.getIssuerEntity(), bond.getCoupon().getNthPayment(i).getPaymentTime());
    }
    return fractions;
  }

}
