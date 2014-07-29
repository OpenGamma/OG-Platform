/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.index;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.DoublesScheduleGenerator.combineSets;

import java.util.Arrays;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class PortfolioSwapAdjustment {
  private static final NewtonRaphsonSingleRootFinder ROOTFINDER = new NewtonRaphsonSingleRootFinder();

  private final CDSIndexCalculator _pricer;

  /**
   * Default constructor
   */
  public PortfolioSwapAdjustment() {
    _pricer = new CDSIndexCalculator();
  }

  /**
   * Adjust the hazard rates of the credit curves of the individual single names in a index so that the index is priced exactly.  The hazard rates are adjusted on
   * a percentage rather than a absolute bases (e.g. all hazard rates are increased by 1%) 
   * @param indexPUF The clean price of the index for unit current notional (i.e. divide the actual clean price by the current notional) 
   * @param indexCDS analytic description of a CDS traded at a certain time
   * @param indexCoupon The coupon of the index (as a fraction)
   * @param yieldCurve The yield curve
   * @param intrinsicData The credit curves of the individual single names making up the index 
   * @return credit curve adjusted so they will exactly reprice the index.
   */
  public IntrinsicIndexDataBundle adjustCurves(final double indexPUF, final CDSAnalytic indexCDS, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve,
      final IntrinsicIndexDataBundle intrinsicData) {
    ArgumentChecker.isTrue(indexPUF <= 1.0, "indexPUF must be given as a fraction. Value of {} is too high.", indexPUF);
    ArgumentChecker.notNull(indexCDS, "indexCDS");
    ArgumentChecker.isTrue(indexCoupon >= 0, "indexCoupon cannot be negative");
    ArgumentChecker.isTrue(indexCoupon < 10, "indexCoupon should be a fraction. The value of {} would be a coupon of {}", indexCoupon, indexCoupon * 1e4);
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(intrinsicData, "intrinsicData");

    final Function1D<Double, Double> func = getHazardRateAdjFunction(indexPUF, indexCDS, indexCoupon, yieldCurve, intrinsicData);
    final double x = ROOTFINDER.getRoot(func, 1.0);
    final ISDACompliantCreditCurve[] adjCC = adjustCurves(intrinsicData.getCreditCurves(), x);
    return intrinsicData.withCreditCurves(adjCC);
  }

  /**
   * Adjust the hazard rates of the credit curves of the individual single names in a index so that the index is priced exactly at multiple terms. The
   * hazard rates are multiplied by a piecewise constant adjuster (e.g. all hazard rates between two index terms are increased by the same percentage). 
   * When required extra knots are added to the credit curves, so the adjusted curves returned may contain more knots than the original curves.
   * @param indexPUF The clean prices of the index for unit current notional.
   * @param indexCDS analytic descriptions of the index for different terms 
   * @param indexCoupon The coupon of the index (as a fraction)
   * @param yieldCurve The yield curve
   * @param intrinsicData The credit curves of the individual single names making up the index 
   * @return credit curve adjusted so they will exactly reprice the index at the different terms.
   */
  public IntrinsicIndexDataBundle adjustCurves(final double[] indexPUF, final CDSAnalytic[] indexCDS, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve,
      final IntrinsicIndexDataBundle intrinsicData) {

    ArgumentChecker.notEmpty(indexPUF, "indexPUF");
    ArgumentChecker.noNulls(indexCDS, "indexCDS");
    final int nIndexTerms = indexCDS.length;
    ArgumentChecker.isTrue(nIndexTerms == indexPUF.length, "number of indexCDS ({}) does not match number of indexPUF ({})", nIndexTerms, indexPUF.length);
    if (nIndexTerms == 1) {
      return adjustCurves(indexPUF[0], indexCDS[0], indexCoupon, yieldCurve, intrinsicData);
    }
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(intrinsicData, "intrinsicData");
    ArgumentChecker.isTrue(indexCoupon >= 0, "indexCoupon cannot be negative");
    ArgumentChecker.isTrue(indexCoupon < 10, "indexCoupon should be a fraction. The value of {} would be a coupon of {}", indexCoupon, indexCoupon * 1e4);

    final double[] indexKnots = new double[nIndexTerms];
    for (int i = 0; i < nIndexTerms; i++) {
      ArgumentChecker.isTrue(indexPUF[i] <= 1.0, "indexPUF must be given as a fraction. Value of {} is too high.", indexPUF[i]);
      indexKnots[i] = indexCDS[i].getProtectionEnd();
      if (i > 0) {
        ArgumentChecker.isTrue(indexKnots[i] > indexKnots[i - 1], "indexCDS must be in assending order of maturity");
      }
    }

    final ISDACompliantCreditCurve[] creditCurves = intrinsicData.getCreditCurves();
    final int nCurves = creditCurves.length;
    //we cannot assume that all the credit curves have knots at the same times or that the terms of the indices fall on these knots.
    ISDACompliantCreditCurve[] modCreditCurves = new ISDACompliantCreditCurve[nCurves];
    final int[][] indexMap = new int[nCurves][nIndexTerms];
    for (int i = 0; i < nCurves; i++) {
      if (creditCurves[i] == null) {
        modCreditCurves[i] = null; //null credit curves correspond to defaulted names, so are ignored 
      } else {
        final double[] ccKnots = creditCurves[i].getKnotTimes();
        final double[] comKnots = combineSets(ccKnots, indexKnots);
        final int nKnots = comKnots.length;
        if (nKnots == ccKnots.length) {
          modCreditCurves[i] = creditCurves[i];
        } else {
          final double[] rt = new double[nKnots];
          for (int j = 0; j < nKnots; j++) {
            rt[j] = creditCurves[i].getRT(comKnots[j]);
          }
          modCreditCurves[i] = ISDACompliantCreditCurve.makeFromRT(comKnots, rt);
        }

        for (int j = 0; j < nIndexTerms; j++) {
          final int index = Arrays.binarySearch(modCreditCurves[i].getKnotTimes(), indexKnots[j]);
          if (index < 0) {
            throw new MathException("This should not happen. There is a bug in the logic");
          }
          indexMap[i][j] = index;
        }
      }
    }

    int[] startKnots = new int[nCurves];
    final int[] endKnots = new int[nCurves];
    double alpha = 1.0;
    for (int i = 0; i < nIndexTerms; i++) {
      if (i == (nIndexTerms - 1)) {
        for (int jj = 0; jj < nCurves; jj++) {
          if (modCreditCurves[jj] != null) {
            endKnots[jj] = modCreditCurves[jj].getNumberOfKnots();
          }
        }
      } else {
        for (int jj = 0; jj < nCurves; jj++) {
          if (modCreditCurves[jj] != null) {
            endKnots[jj] = indexMap[jj][i] + 1;
          }
        }
      }

      final IntrinsicIndexDataBundle modIntrinsicData = intrinsicData.withCreditCurves(modCreditCurves);
      final Function1D<Double, Double> func = getHazardRateAdjFunction(indexPUF[i], indexCDS[i], indexCoupon, yieldCurve, modIntrinsicData, startKnots, endKnots);
      alpha = ROOTFINDER.getRoot(func, alpha);
      modCreditCurves = adjustCurves(modCreditCurves, alpha, startKnots, endKnots);
      startKnots = endKnots.clone();
    }

    return intrinsicData.withCreditCurves(modCreditCurves);
  }

  private Function1D<Double, Double> getHazardRateAdjFunction(final double indexPUF, final CDSAnalytic indexCDS, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve,
      final IntrinsicIndexDataBundle intrinsicData) {

    final ISDACompliantCreditCurve[] creditCurves = intrinsicData.getCreditCurves();
    final double clean = intrinsicData.getIndexFactor() * indexPUF;
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        final ISDACompliantCreditCurve[] adjCurves = adjustCurves(creditCurves, x);
        return _pricer.indexPV(indexCDS, indexCoupon, yieldCurve, intrinsicData.withCreditCurves(adjCurves)) - clean;
      }
    };
  }

  private Function1D<Double, Double> getHazardRateAdjFunction(final double indexPUF, final CDSAnalytic indexCDS, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve,
      final IntrinsicIndexDataBundle intrinsicData, final int[] firstKnots, final int[] lastKnots) {

    final ISDACompliantCreditCurve[] creditCurves = intrinsicData.getCreditCurves();
    final double clean = intrinsicData.getIndexFactor() * indexPUF;
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        final ISDACompliantCreditCurve[] adjCurves = adjustCurves(creditCurves, x, firstKnots, lastKnots);
        return _pricer.indexPV(indexCDS, indexCoupon, yieldCurve, intrinsicData.withCreditCurves(adjCurves)) - clean;
      }
    };
  }

  private ISDACompliantCreditCurve[] adjustCurves(final ISDACompliantCreditCurve[] creditCurve, final double amount) {
    final int nCurves = creditCurve.length;
    final ISDACompliantCreditCurve[] adjCurves = new ISDACompliantCreditCurve[nCurves];
    for (int jj = 0; jj < nCurves; jj++) {
      adjCurves[jj] = adjustCreditCurve(creditCurve[jj], amount);
    }
    return adjCurves;
  }

  private ISDACompliantCreditCurve adjustCreditCurve(final ISDACompliantCreditCurve creditCurve, final double amount) {
    if (creditCurve == null) {
      return creditCurve;
    }
    final int nKnots = creditCurve.getNumberOfKnots();
    final double[] rt = creditCurve.getRt();
    final double[] rtAdj = new double[nKnots];
    for (int i = 0; i < nKnots; i++) {
      rtAdj[i] = rt[i] * amount;
    }
    return ISDACompliantCreditCurve.makeFromRT(creditCurve.getKnotTimes(), rtAdj);
  }

  private ISDACompliantCreditCurve[] adjustCurves(final ISDACompliantCreditCurve[] creditCurve, final double amount, final int[] firstKnots, final int[] lastknots) {
    final int nCurves = creditCurve.length;
    final ISDACompliantCreditCurve[] adjCurves = new ISDACompliantCreditCurve[nCurves];
    for (int jj = 0; jj < nCurves; jj++) {
      if (creditCurve[jj] == null) {
        adjCurves[jj] = null;
      } else {
        adjCurves[jj] = adjustCreditCurve(creditCurve[jj], amount, firstKnots[jj], lastknots[jj]);
      }
    }
    return adjCurves;
  }

  private ISDACompliantCreditCurve adjustCreditCurve(final ISDACompliantCreditCurve creditCurve, final double amount, final int firstKnot, final int lastKnot) {
    final double[] rt = creditCurve.getRt();
    final double[] rtAdj = rt.clone();
    for (int i = firstKnot; i < lastKnot; i++) {
      rtAdj[i] = rt[i] * amount;
    }
    return ISDACompliantCreditCurve.makeFromRT(creditCurve.getKnotTimes(), rtAdj);
  }

}
