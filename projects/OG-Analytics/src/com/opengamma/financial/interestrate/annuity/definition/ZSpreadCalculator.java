/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;

/**
 * 
 */
public final class ZSpreadCalculator {

  private static PresentValueCalculator s_pvc = PresentValueCalculator.getInstance();
  private static BracketRoot s_bracketRoot = new BracketRoot();
  private static final RealSingleRootFinder s_root = new VanWijngaardenDekkerBrentSingleRootFinder();
  private static final ZSpreadCalculator s_instance = new ZSpreadCalculator();

  private ZSpreadCalculator() {
  }

  public static ZSpreadCalculator getInstance() {
    return s_instance;
  }

  public double calculateSpread(final GenericAnnuity<? extends FixedPayment> annuity, final YieldCurveBundle curves, final double price) {
    Validate.notNull(annuity, "annuity");
    Validate.notNull(curves, "curves");

    final Function1D<Double, Double> f = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double y) {
        return calculatePriceForSpread(annuity, curves, y) - price;
      }
    };

    final double[] range = s_bracketRoot.getBracketedPoints(f, 0.0, 0.2);
    return s_root.getRoot(f, range[0], range[1]);
  }

  public double calculatePriceForSpread(final GenericAnnuity<? extends FixedPayment> annuity, final YieldCurveBundle curves, final double spread) {
    Validate.notNull(annuity, "annuity");
    Validate.notNull(curves, "curves");

    double sum = 0;

    final int n = annuity.getNumberOfpayments();
    FixedPayment payment;
    for (int i = 0; i < n; i++) {
      payment = annuity.getNthPayment(i);
      double temp = s_pvc.getValue(payment, curves);
      sum += temp * Math.exp(-spread * payment.getPaymentTime());
    }
    return sum;
  }
}
