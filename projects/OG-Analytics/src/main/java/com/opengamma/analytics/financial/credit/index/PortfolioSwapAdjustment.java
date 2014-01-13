/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.index;

import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class PortfolioSwapAdjustment {
  private static final NewtonRaphsonSingleRootFinder ROOTFINDER = new NewtonRaphsonSingleRootFinder();

  private final AnalyticCDSPricer _pricer;

  public PortfolioSwapAdjustment() {
    _pricer = new AnalyticCDSPricer();
  }

  /**
   * Price an index from the credit curves of the individual single names. Only undefaulted names should be passed in. This is based on a notional of 1. The get the
   * index value, this must be multiplied by the current index notional (i.e. the original index notional multiplied by the index factor)
   * @param indexCDS analytic description of a CDS traded at a certain time
   * @param indexCoupon The coupon of the index (as a fraction)
   * @param indexSize The (original) number of names in the index 
   * @param yieldCurve The yield curve
   * @param creditCurves The credit curves of the (undefaulted) individual single names making up the index 
   * @param recoveryRates The recovery rates for in individual single names. 
   * @param priceType Clean or dirty price
   * @return The index value for a unit current notional. 
   */
  public double indexPV(final CDSAnalytic indexCDS, final double indexCoupon, final int indexSize, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve[] creditCurves,
      final double[] recoveryRates, final PriceType priceType) {
    ArgumentChecker.notNull(indexCDS, "indexCDS");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.noNulls(creditCurves, "creditCurves");
    ArgumentChecker.notEmpty(recoveryRates, "recoveryRates");
    final int n = creditCurves.length;
    ArgumentChecker.isTrue(n == recoveryRates.length, "number of credit curves does not match number of recovery rates");
    ArgumentChecker.isTrue(indexSize >= n, "index size less than number of curve passed");

    final CDSAnalytic cds = indexCDS.withRecoveryRate(0.0);
    double protLeg = 0;
    double rpv01 = 0;
    for (int i = 0; i < n; i++) {
      protLeg += (1 - recoveryRates[i]) * _pricer.protectionLeg(cds, yieldCurve, creditCurves[i], 0);
      rpv01 += _pricer.annuity(cds, yieldCurve, creditCurves[i], PriceType.DIRTY, 0);
    }
    double pv = (protLeg - indexCoupon * rpv01) / indexSize;

    //TODO cds.getAccruedPremium returns a positive number, so we ADD the accrued for the clean price. The market convention, for the buyer of protection, is that the
    //accrued is negative, so one subtracts it  
    if (priceType == PriceType.CLEAN) {
      pv += cds.getAccruedPremium(indexCoupon);
    }
    return pv;
  }

  /**
   * Adjust the hazard rates of the credit curves of the individual single names in a index so that the index is priced exactly.  The hazard rates are adjusted on
   * a percentage rather than a absolute bases (e.g. all hazard rates are increased by 1%) 
   * @param indexCDS analytic description of a CDS traded at a certain time
   * @param indexCoupon The coupon of the index (as a fraction)
  * @param indexSize The (original) number of names in the index 
   * @param yieldCurve The yield curve
   * @param creditCurves The credit curves of the (undefaulted) individual single names making up the index 
   * @param recoveryRates The recovery rates for in individual single names. 
   * @param indexPUF The clean price of the index for unit current notional (i.e. divide the actual clean price by the current notional) 
   * @return credit curve adjusted so they will exactly reprice the index.
   */
  public ISDACompliantCreditCurve[] adjustCurves(final CDSAnalytic indexCDS, final double indexCoupon, final int indexSize, final ISDACompliantYieldCurve yieldCurve,
      final ISDACompliantCreditCurve[] creditCurves, final double[] recoveryRates, final double indexPUF) {
    final Function1D<Double, Double> func = getHazardRateAdjFunction(indexCDS, indexCoupon, indexSize, yieldCurve, creditCurves, recoveryRates, indexPUF);
    final double x = ROOTFINDER.getRoot(func, 1.0);
    return adjustCurves(creditCurves, x);
  }

  public Function1D<Double, Double> getHazardRateAdjFunction(final CDSAnalytic indexCDS, final double indexCoupon, final int indexSize, final ISDACompliantYieldCurve yieldCurve,
      final ISDACompliantCreditCurve[] creditCurve, final double[] recoveryRates, final double indexPUF) {

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        final ISDACompliantCreditCurve[] adjCurves = adjustCurves(creditCurve, x);
        return indexPV(indexCDS, indexCoupon, indexSize, yieldCurve, adjCurves, recoveryRates, PriceType.CLEAN) - indexPUF;
      }
    };
  }

  private ISDACompliantCreditCurve[] adjustCurves(final ISDACompliantCreditCurve[] creditCurve, final double amount) {
    final int nCurves = creditCurve.length;
    final ISDACompliantCreditCurve[] adjCurves = new ISDACompliantCreditCurve[nCurves];
    for (int jj = 0; jj < nCurves; jj++) {
      adjCurves[jj] = adjustCreditCruve(creditCurve[jj], amount);
    }
    return adjCurves;
  }

  private ISDACompliantCreditCurve adjustCreditCruve(final ISDACompliantCreditCurve creditCurve, final double amount) {
    final int nKnots = creditCurve.getNumberOfKnots();
    final double[] rt = creditCurve.getRt();
    final double[] rtAdj = new double[nKnots];
    for (int i = 0; i < nKnots; i++) {
      rtAdj[i] = rt[i] * amount;
    }
    return ISDACompliantCreditCurve.makeFromRT(creditCurve.getKnotTimes(), rtAdj);
  }
}
