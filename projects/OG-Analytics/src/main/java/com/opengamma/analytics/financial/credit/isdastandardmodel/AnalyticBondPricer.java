/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.math.utilities.Epsilon.epsilon;

import java.util.Arrays;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AnalyticBondPricer {

  private static final NewtonRaphsonSingleRootFinder ROOTFINDER = new NewtonRaphsonSingleRootFinder();
  private final AnalyticCDSPricer _pricer = new AnalyticCDSPricer();

  /**
   * Compute the equivalent CDS spread for a bond. This works by first finding a constant hazard rate that reprices the bond (given the supplied yield curve), the using this hazard
   * rate to calculate the par spread of a CDS. 
   * @param bond Simple analytic representation of a fixed coupon bond
   * @param yieldCurve The yield curve 
   * @param bondPrice The bond price (for unit notional). Can be given clean or dirty (see below). The dirty price cannot be low that the bond's recovery rate or greater
   * than its risk free price.
   * @param cleanOrDirty Clean or dirty price for the bond 
   * @param cds analytic description of a CDS traded at a certain time. The spread is calculated for this CDS.
   * @see {@link getHazardRate}
   * @return equivalent CDS spread
   */
  public double getEquivalentCDSSpread(final BondAnalytic bond, final ISDACompliantYieldCurve yieldCurve, final double bondPrice, final PriceType cleanOrDirty, final CDSAnalytic cds) {

    final double lambda = getHazardRate(bond, yieldCurve, bondPrice, cleanOrDirty);
    final ISDACompliantCreditCurve cc = new ISDACompliantCreditCurve(cds.getProtectionEnd(), lambda);
    return _pricer.parSpread(cds, yieldCurve, cc);
  }

  /**
   *Get the constant hazard rate implied from a bond price 
   * @param bond Simple analytic representation of a fixed coupon bond
   * @param yieldCurve  The yield curve 
   * @param bondPrice The bond price (for unit notional). Can be given clean or dirty (see below). The dirty price cannot be low that the bond's recovery rate or greater
   * than its risk free price.
   * @param cleanOrDirty Clean or dirty price for the bond 
   * @return The implied hazard rate
   */
  public double getHazardRate(final BondAnalytic bond, final ISDACompliantYieldCurve yieldCurve, final double bondPrice, final PriceType cleanOrDirty) {
    ArgumentChecker.isTrue(bondPrice > 0.0, "Bond price must be positive");

    final Function1D<Double, Double> priceFunc = getBondPriceForHazardRateFunction(bond, yieldCurve, cleanOrDirty);

    final double zeroRiskPrice = priceFunc.evaluate(0.);
    if (bondPrice == zeroRiskPrice) {
      return 0.0;
    }
    if (bondPrice > zeroRiskPrice) {
      throw new IllegalArgumentException("Bond price of " + bondPrice + ", is greater that zero-risk price of " + zeroRiskPrice +
          ". It is not possible to imply a hazard rate for this bond. Please check inputs");
    }
    final double dp = cleanOrDirty == PriceType.DIRTY ? bondPrice : bondPrice + bond.getAccruedInterest();
    if (dp <= bond.getRecoveryRate()) {
      throw new IllegalArgumentException("The dirty price of " + dp + " give, is less than the bond's recovery rate of " + bond.getRecoveryRate() + ". Please check inputs");
    }

    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double lambda) {
        return priceFunc.evaluate(lambda) - bondPrice;
      }
    };

    final double guess = 0.01;
    return ROOTFINDER.getRoot(func, guess);
  }

  /**
   * Compute the bond price for a given level of a constant hazard rate 
   * @param bond Simple analytic representation of a fixed coupon bond
   * @param yieldCurve The yield curve 
   * @param hazardRate The hazard rate. Can be zero. 
   * @param cleanOrDirty  Clean or dirty price for the bond 
   * @see {@link getBondPriceForHazardRateFunction}
   * @return The bond price
   */
  public double bondPriceForHazardRate(final BondAnalytic bond, final ISDACompliantYieldCurve yieldCurve, final double hazardRate, final PriceType cleanOrDirty) {
    return getBondPriceForHazardRateFunction(bond, yieldCurve, cleanOrDirty).evaluate(hazardRate);
  }

  /**
   * This gives a function (Function1D<Double, Double>) that allows you to price a bond for any level of a constant hazard rate 
   * @param bond Simple analytic representation of a fixed coupon bond
   * @param yieldCurve The yield curve 
   * @param cleanOrDirty Clean or dirty price for the bond 
   * @return a function of hazard rate -> bond price 
   */
  public Function1D<Double, Double> getBondPriceForHazardRateFunction(final BondAnalytic bond, final ISDACompliantYieldCurve yieldCurve, final PriceType cleanOrDirty) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(cleanOrDirty, "cleanOrDirty");

    final int nPayments = bond.getnPayments();
    final double[] discPayments = new double[nPayments];

    for (int i = 0; i < nPayments; i++) {
      discPayments[i] = bond.getPaymentAmount(i) * yieldCurve.getDiscountFactor(bond.getPaymentTime(i));
    }

    final double exp = bond.getPaymentTime(nPayments - 1);
    int index = Arrays.binarySearch(yieldCurve.getKnotTimes(), exp);
    double[] temp;
    if (index >= 0) {
      temp = new double[index + 1];
      System.arraycopy(yieldCurve.getKnotTimes(), 0, temp, 0, index + 1);
    } else {
      index = -(index + 1);
      temp = new double[index + 1];
      System.arraycopy(yieldCurve.getKnotTimes(), 0, temp, 0, index);
      temp[index] = exp;
    }

    final double[] intNodes = temp;
    final int nNodes = intNodes.length;
    final double[] rt = new double[nNodes];
    for (int i = 0; i < nNodes; i++) {
      rt[i] = yieldCurve.getRT(intNodes[i]);
    }

    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double lambda) {

        double riskyDisPayments = cleanOrDirty == PriceType.CLEAN ? -bond.getAccruedInterest() : 0.0;
        for (int i = 0; i < nPayments; i++) {
          final double q = Math.exp(-lambda * bond.getPaymentTime(i));
          riskyDisPayments += discPayments[i] * q;
        }
        if (bond.getRecoveryRate() == 0.0) {
          return riskyDisPayments;
        }

        final double[] ht = new double[nNodes];
        final double[] b = new double[nNodes];
        for (int i = 0; i < nNodes; ++i) {
          ht[i] = lambda * intNodes[i];
          b[i] = Math.exp(-rt[i] - ht[i]);
        }

        double defaultPV = 0.0;
        {
          final double dht = ht[0];
          final double drt = rt[0];
          final double dhrt = dht + drt;
          double dPV;
          if (Math.abs(dhrt) < 1e-5) {
            dPV = dht * epsilon(-dhrt);
          } else {
            dPV = (1 - b[0]) * dht / dhrt;
          }
          defaultPV += dPV;
        }
        for (int i = 1; i < nNodes; ++i) {

          final double dht = ht[i] - ht[i - 1];
          final double drt = rt[i] - rt[i - 1];
          final double dhrt = dht + drt;
          double dPV;
          if (Math.abs(dhrt) < 1e-5) {
            dPV = dht * b[i - 1] * epsilon(-dhrt);
          } else {
            dPV = (b[i - 1] - b[i]) * dht / dhrt;
          }
          defaultPV += dPV;
        }

        return riskyDisPayments + bond.getRecoveryRate() * defaultPV;
      }
    };

  }

}
