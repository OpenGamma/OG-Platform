/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the present value of Bermuda swaptions with the Hull-White one factor model by numerical integration.
 * Reference: Henrard, M. Bermudan Swaptions in Gaussian HJM One-Factor Model: Analytical and Numerical Approaches. SSRN, October 2008. Available at SSRN: http://ssrn.com/abstract=1287982
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod}
 */
@Deprecated
public final class SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod INSTANCE = new SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod() {
  }

  // TODO: The number of integration points should be a setting.
  /**
   * The number of points used in the numerical integration process.
   */
  private static final int NB_POINT = 50;
  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /**
   * The model used in computations.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  /**
   * The normal distribution implementation.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Computes the present value of the Physical delivery swaption.
   * @param swaption The swaption.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionBermudaFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(hwData, "Hull-White data");
    final int nbExpiry = swaption.getExpiryTime().length;
    ArgumentChecker.isTrue(nbExpiry > 1, "At least two expiry dates required for this method");

    double tmpdb;
    final YieldAndDiscountCurve discountingCurve = hwData.getCurve(swaption.getUnderlyingSwap()[0].getFirstLeg().getDiscountCurve());

    final double[] theta = new double[nbExpiry + 1]; // Extended expiry time (with 0).
    theta[0] = 0.0;
    System.arraycopy(swaption.getExpiryTime(), 0, theta, 1, nbExpiry);
    final AnnuityPaymentFixed[] cashflow = new AnnuityPaymentFixed[nbExpiry];
    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      cashflow[loopexp] = swaption.getUnderlyingSwap()[loopexp].accept(CFEC, hwData);
    }
    final int[] n = new int[nbExpiry];
    final double[][][] alpha = new double[nbExpiry][][];
    final double[][][] alpha2 = new double[nbExpiry][][]; // alpha^2

    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      n[loopexp] = cashflow[loopexp].getNumberOfPayments();
      alpha[loopexp] = new double[loopexp + 1][];
      alpha2[loopexp] = new double[loopexp + 1][];
      for (int k = 0; k <= loopexp; k++) {
        alpha[loopexp][k] = new double[n[loopexp]];
        alpha2[loopexp][k] = new double[n[loopexp]];
        for (int l = 0; l < alpha[loopexp][k].length; l++) {
          alpha[loopexp][k][l] = MODEL.alpha(hwData.getHullWhiteParameter(), theta[k], theta[k + 1], theta[k + 1], cashflow[loopexp].getNthPayment(l).getPaymentTime());
          alpha2[loopexp][k][l] = alpha[loopexp][k][l] * alpha[loopexp][k][l];
        }
      }
    }

    final int nbPoint2 = 2 * NB_POINT + 1;
    final int[] startInt = new int[nbExpiry - 1];
    final int[] endInt = new int[nbExpiry - 1];
    for (int i = 1; i < nbExpiry - 1; i++) {
      startInt[i] = 0;
      endInt[i] = nbPoint2 - 1;
    }
    startInt[0] = NB_POINT;
    endInt[0] = NB_POINT;

    final double[][] t = new double[nbExpiry][]; // payment time
    final double[][] dfS = new double[nbExpiry][]; // discount factor
    final double[] beta = new double[nbExpiry];
    final double[][] h = new double[nbExpiry][];
    final double[][] sa2 = new double[nbExpiry][];

    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      beta[loopexp] = MODEL.beta(hwData.getHullWhiteParameter(), theta[loopexp], theta[loopexp + 1]);
      t[loopexp] = new double[n[loopexp]];
      dfS[loopexp] = new double[n[loopexp]];
      h[loopexp] = new double[n[loopexp]];
      sa2[loopexp] = new double[n[loopexp]];
      for (int loopcf = 0; loopcf < n[loopexp]; loopcf++) {
        t[loopexp][loopcf] = cashflow[loopexp].getNthPayment(loopcf).getPaymentTime();
        dfS[loopexp][loopcf] = discountingCurve.getDiscountFactor(t[loopexp][loopcf]);
        h[loopexp][loopcf] = (1 - Math.exp(-hwData.getHullWhiteParameter().getMeanReversion() * t[loopexp][loopcf])) / hwData.getHullWhiteParameter().getMeanReversion();
        tmpdb = 0.0;
        for (int k = 0; k <= loopexp; k++) {
          tmpdb += alpha2[loopexp][k][loopcf];
        }
        sa2[loopexp][loopcf] = tmpdb;
      }
    }
    final double[] discountedCashFlowN = new double[n[nbExpiry - 1]];
    for (int loopcf = 0; loopcf < n[nbExpiry - 1]; loopcf++) {
      discountedCashFlowN[loopcf] = dfS[nbExpiry - 1][loopcf] * cashflow[nbExpiry - 1].getNthPayment(loopcf).getAmount();
    }
    final double lambda = MODEL.lambda(discountedCashFlowN, sa2[nbExpiry - 1], h[nbExpiry - 1]);
    final double[] betaSort = new double[nbExpiry];
    System.arraycopy(beta, 0, betaSort, 0, nbExpiry);
    Arrays.sort(betaSort);
    final double minbeta = betaSort[0];
    final double maxbeta = betaSort[nbExpiry - 1];

    final double b = Math.min(10 * minbeta, maxbeta);
    final double epsilon = -2.0 / NB_POINT * NORMAL.getInverseCDF(1.0 / (200.0 * NB_POINT)) * b; // <-
    final double[] bX = new double[nbPoint2];
    for (int looppt = 0; looppt < nbPoint2; looppt++) {
      bX[looppt] = -NB_POINT * epsilon + looppt * epsilon;
    }
    final double[] bX2 = new double[4 * NB_POINT + 1];
    for (int looppt = 0; looppt < 4 * NB_POINT + 1; looppt++) {
      bX2[looppt] = -2 * NB_POINT * epsilon + looppt * epsilon;
    }
    final double[] htheta = new double[nbExpiry];
    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      htheta[loopexp] = (1 - Math.exp(-hwData.getHullWhiteParameter().getMeanReversion() * theta[loopexp + 1])) / hwData.getHullWhiteParameter().getMeanReversion();
    }

    final double[][] vZ = new double[nbExpiry - 1][nbPoint2];
    for (int i = nbExpiry - 2; i >= 0; i--) {
      for (int looppt = 0; looppt < nbPoint2; looppt++) {
        vZ[i][looppt] = Math.exp(bX[looppt] * htheta[i]);
      }
    }

    final double[][] vW = new double[nbExpiry][]; // Swaption
    final double[][] vT = new double[nbExpiry - 1][]; // Swap

    final double omega = -Math.signum(cashflow[nbExpiry - 1].getNthPayment(0).getAmount());
    final double[] kappaL = new double[nbPoint2];
    for (int looppt = 0; looppt < nbPoint2; looppt++) {
      kappaL[looppt] = (lambda - bX[looppt]) / beta[nbExpiry - 1];
    }

    final double[] sa2N1 = new double[n[nbExpiry - 1]];
    for (int i = 0; i < n[nbExpiry - 1]; i++) {
      tmpdb = 0;
      for (int k = 0; k <= nbExpiry - 2; k++) {
        tmpdb += alpha2[nbExpiry - 1][k][i];
      }
      sa2N1[i] = tmpdb;
    }

    vW[nbExpiry - 1] = new double[4 * NB_POINT + 1];
    for (int j = 0; j < n[nbExpiry - 1]; j++) {
      for (int looppt = 0; looppt < nbPoint2; looppt++) {
        vW[nbExpiry - 1][NB_POINT + looppt] += discountedCashFlowN[j] * Math.exp(-sa2N1[j] / 2.0 - h[nbExpiry - 1][j] * bX[looppt])
            * NORMAL.getCDF(omega * (kappaL[looppt] + alpha[nbExpiry - 1][nbExpiry - 1][j]));
      }
    }
    for (int looppt = 0; looppt < NB_POINT; looppt++) {
      vW[nbExpiry - 1][looppt] = vW[nbExpiry - 1][NB_POINT];
    }
    for (int looppt = 0; looppt < NB_POINT; looppt++) {
      vW[nbExpiry - 1][3 * NB_POINT + 1 + looppt] = vW[nbExpiry - 1][3 * NB_POINT];
    }

    final double c1sqrt2pi = 1.0 / Math.sqrt(2 * Math.PI);
    final double[][] pvcfT = new double[nbExpiry - 1][];
    double[] vL; // Left side of intersection
    double[] vR; // Right side of intersection
    double[][] labc;
    double[][] rabc;
    final double[][] labcM = new double[3][4 * NB_POINT + 1];
    final double[][] rabcM = new double[3][4 * NB_POINT + 1];

    final double[] dabc = new double[3];
    final int[] indSwap = new int[nbExpiry - 1]; // index of the intersection
    double xroot;
    final double[][] xN = new double[nbExpiry - 1][nbPoint2];
    double ci;
    double coi;
    int is;
    final double[] ncdf0 = new double[nbPoint2];
    final double[] ncdf1 = new double[nbPoint2];
    final double[] ncdf2 = new double[nbPoint2];
    final double[] ncdf0X = new double[nbPoint2 + 1];
    final double[] ncdf1X = new double[nbPoint2 + 1];
    final double[] ncdf2X = new double[nbPoint2 + 1];
    double ncdf0x;
    double ncdf1x;
    double ncdf2x;
    double ncdfinit;

    // Main loop for the different expiry dates (except the last one)
    for (int i = nbExpiry - 2; i >= 0; i--) {
      vW[i] = new double[4 * NB_POINT + 1];
      vT[i] = new double[4 * NB_POINT + 1];
      // T: swap
      pvcfT[i] = new double[n[i]];
      for (int j = 0; j < n[i]; j++) {
        pvcfT[i][j] = cashflow[i].getNthPayment(j).getAmount() * dfS[i][j];
        for (int looppt = 0; looppt < 4 * NB_POINT + 1; looppt++) {
          vT[i][looppt] += pvcfT[i][j] * Math.exp(-sa2[i][j] / 2.0 - h[i][j] * bX2[looppt]);
        }
      }
      // Preparation
      for (int looppt = 0; looppt < nbPoint2; looppt++) {
        xN[i][looppt] = bX[looppt] / beta[i];
      }
      ci = htheta[i] * beta[i];
      coi = Math.exp(ci * ci / 2);

      // Left/Right
      if (omega < 0) {
        vL = vW[i + 1];
        vR = vT[i];
      } else {
        vR = vW[i + 1];
        vL = vT[i];
      }
      indSwap[i] = 0;
      while (vL[indSwap[i] + 1] >= vR[indSwap[i] + 1]) {
        indSwap[i]++;
      }
      // Parabola fit
      labc = parafit(epsilon / beta[i], vL);
      rabc = parafit(epsilon / beta[i], vR);
      for (int k = 0; k < 3; k++) {
        dabc[k] = labc[k][indSwap[i]] - rabc[k][indSwap[i]];
        System.arraycopy(labc[k], 0, labcM[k], 0, indSwap[i] + 1);
        System.arraycopy(labc[k], indSwap[i], labcM[k], indSwap[i] + 1, labc[k].length - indSwap[i]);
        System.arraycopy(rabc[k], 0, rabcM[k], 0, indSwap[i] + 1);
        System.arraycopy(rabc[k], indSwap[i], rabcM[k], indSwap[i] + 1, rabc[k].length - indSwap[i]);
      }

      for (int looppt = 0; looppt < 4 * NB_POINT + 1; looppt++) {
        labcM[1][looppt] = labcM[1][looppt] + labcM[0][looppt] * 2 * ci;
        labcM[2][looppt] = labcM[2][looppt] + labcM[1][looppt] * ci - labcM[0][looppt] * ci * ci;
        rabcM[1][looppt] = rabcM[1][looppt] + rabcM[0][looppt] * 2 * ci;
        rabcM[2][looppt] = rabcM[2][looppt] + rabcM[1][looppt] * ci - rabcM[0][looppt] * ci * ci;
      }
      xroot = (-dabc[1] - Math.sqrt(dabc[1] * dabc[1] - 4 * dabc[0] * dabc[2])) / (2 * dabc[0]);

      ncdfinit = NORMAL.getCDF(xN[i][0]);

      for (int looppt = 0; looppt < nbPoint2; looppt++) {
        ncdf0[looppt] = NORMAL.getCDF(xN[i][looppt] - ci);
        ncdf1[looppt] = -c1sqrt2pi * Math.exp(-(xN[i][looppt] - ci) * (xN[i][looppt] - ci) / 2.0);
        ncdf2[looppt] = ncdf1[looppt] * (xN[i][looppt] - ci) + ncdf0[looppt];
      }

      for (int j = startInt[i]; j <= endInt[i]; j++) {
        is = indSwap[i] - j + 1;
        // % all L
        if (j + 2 * NB_POINT <= indSwap[i]) {
          final double[][] xabc = new double[3][2 * NB_POINT];
          for (int k = 0; k < 3; k++) {
            System.arraycopy(labcM[k], j, xabc[k], 0, 2 * NB_POINT);
          }
          for (int looppt = 0; looppt < 2 * NB_POINT; looppt++) {
            xabc[1][looppt] = xabc[1][looppt] + xabc[0][looppt] * 2 * xN[i][j];
            xabc[2][looppt] = xabc[2][looppt] + xabc[1][looppt] * xN[i][j] - xabc[0][looppt] * xN[i][j] * xN[i][j];
          }
          vW[i][j + NB_POINT] = 0;
          vW[i][j + NB_POINT] = vW[i][j + NB_POINT] + coi * ni2ncdf(ncdf2, ncdf1, ncdf0, xabc);
        } else if (j < indSwap[i]) {
          final double[][] xabc = new double[3][2 * NB_POINT + 1];
          tmpdb = xroot - xN[i][j] - ci;
          ncdf0x = NORMAL.getCDF(tmpdb);
          ncdf1x = -Math.exp(-(tmpdb * tmpdb) / 2) * c1sqrt2pi;
          ncdf2x = ncdf1x * tmpdb + ncdf0x;
          for (int k = 0; k < 3; k++) {
            //            System.arraycopy(rabcM[k], j, xabc[k], 0, 2 * _nbPoint + 1); // Swap
            System.arraycopy(labcM[k], j, xabc[k], 0, 2 * NB_POINT + 1);
            System.arraycopy(rabcM[k], indSwap[i] + 1, xabc[k], indSwap[i] + 1 - j, j + 2 * NB_POINT - indSwap[i]);
          }
          for (int looppt = 0; looppt < 2 * NB_POINT; looppt++) {
            xabc[1][looppt] = xabc[1][looppt] + xabc[0][looppt] * 2 * xN[i][j];
            xabc[2][looppt] = xabc[2][looppt] + xabc[1][looppt] * xN[i][j] - xabc[0][looppt] * xN[i][j] * xN[i][j];
          }
          System.arraycopy(ncdf0, 0, ncdf0X, 0, is);
          ncdf0X[is] = ncdf0x;
          System.arraycopy(ncdf0, is, ncdf0X, is + 1, ncdf0.length - is);
          System.arraycopy(ncdf1, 0, ncdf1X, 0, is);
          ncdf1X[is] = ncdf1x;
          System.arraycopy(ncdf1, is, ncdf1X, is + 1, ncdf1.length - is);
          System.arraycopy(ncdf2, 0, ncdf2X, 0, is);
          ncdf2X[is] = ncdf2x;
          System.arraycopy(ncdf2, is, ncdf2X, is + 1, ncdf2.length - is);
          vW[i][j + NB_POINT] = vL[j] * vZ[i][0] * ncdfinit;
          vW[i][j + NB_POINT] += coi * ni2ncdf(ncdf2X, ncdf1X, ncdf0X, xabc);
          vW[i][j + NB_POINT] += vR[j + 2 * NB_POINT] * vZ[i][vZ[i].length - 1] * ncdfinit;
        } else {
          final double[][] xabc = new double[3][2 * NB_POINT];
          for (int k = 0; k < 3; k++) {
            System.arraycopy(rabcM[k], j + 1, xabc[k], 0, 2 * NB_POINT);
            //            System.arraycopy(labcM[k], j + 1, xabc[k], 0, 2 * _nbPoint); // Swaption
          }
          for (int looppt = 0; looppt < 2 * NB_POINT; looppt++) {
            xabc[1][looppt] = xabc[1][looppt] + xabc[0][looppt] * 2 * xN[i][j];
            xabc[2][looppt] = xabc[2][looppt] + xabc[1][looppt] * xN[i][j] - xabc[0][looppt] * xN[i][j] * xN[i][j];
          }
          vW[i][j + NB_POINT] = vR[j] * vZ[i][0] * ncdfinit;
          vW[i][j + NB_POINT] += coi * ni2ncdf(ncdf2, ncdf1, ncdf0, xabc);
          vW[i][j + NB_POINT] += vR[j + 2 * NB_POINT] * vZ[i][vZ[i].length - 1] * ncdfinit;
        }
      }
      for (int j = 0; j < NB_POINT; j++) { // Flat extrapolation
        vW[i][j] = vW[i][NB_POINT];
        vW[i][3 * NB_POINT + 1 + j] = vW[i][3 * NB_POINT];
      }
    } // End main loop

    return CurrencyAmount.of(swaption.getUnderlyingSwap()[0].getFixedLeg().getCurrency(), vW[0][2 * NB_POINT] * (swaption.isLong() ? 1.0 : -1.0));
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof SwaptionBermudaFixedIbor, "Physical delivery swaption");
    ArgumentChecker.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue((SwaptionBermudaFixedIbor) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  /**
   * Fit the parabolas.
   * @param dx Distance between the x values.
   * @param y The y values.
   * @return The parabolas coefficients.
   */
  private static double[][] parafit(final double dx, final double[] y) {
    final int nbPts = y.length;
    final int nbStep = (nbPts - 1) / 2;
    final double dx2 = dx * dx;
    final double[] x1 = new double[nbStep];
    final double[] x = new double[nbStep];
    for (int i = 0; i < nbStep; i++) {
      x1[i] = -nbStep + 2.0 * i;
      x[i] = x1[i] * dx;
    }
    final double[][] abc = new double[3][2 * nbStep];
    double tmp;
    for (int i = 0; i < nbStep; i++) {
      tmp = (y[2 + 2 * i] - 2 * y[1 + 2 * i] + y[2 * i]) / (2 * dx2);
      abc[0][2 * i] = tmp;
      abc[0][2 * i + 1] = tmp;
      tmp = (y[1 + 2 * i] - y[2 * i]) / dx - (2 * x[i] + dx) * abc[0][2 * i];
      abc[1][2 * i] = tmp;
      abc[1][2 * i + 1] = tmp;
      tmp = y[2 * i] - x[i] * x[i] * abc[0][2 * i] - x[i] * abc[1][2 * i];
      abc[2][2 * i] = tmp;
      abc[2][2 * i + 1] = tmp;
    }
    return abc;
  }

  /**
   * Numerical integration of the parabolas against the normal distribution.
   * @param n2 Second order integrals.
   * @param n1 First order integrals.
   * @param n0 Order 0 integrals.
   * @param abc The parabolas coefficients.
   * @return The integral.
   */
  private static double ni2ncdf(final double[] n2, final double[] n1, final double[] n0, final double[][] abc) {
    double s = 0;
    for (int i = 0; i < abc[0].length; i++) {
      s += abc[0][i] * (n2[i + 1] - n2[i]);
      s += abc[1][i] * (n1[i + 1] - n1[i]);
      s += abc[2][i] * (n0[i + 1] - n0[i]);
    }
    return s;
  }
}
