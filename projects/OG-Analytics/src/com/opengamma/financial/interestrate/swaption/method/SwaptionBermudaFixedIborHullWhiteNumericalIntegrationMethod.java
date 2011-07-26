/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the present value of Bermuda swaptions with the Hull-White one factor model by numerical integration.
 * Reference: Henrard, M. Bermudan Swaptions in Gaussian HJM One-Factor Model: Analytical and Numerical Approaches SSRN, 2008.
 */
public class SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod implements PricingMethod {

  /**
   * The number of points used in the numerical integration process.
   */
  private static int _nbPoint;
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
    Validate.notNull(swaption);
    Validate.notNull(hwData);

    double tmpdb;
    YieldAndDiscountCurve discountingCurve = hwData.getCurve(swaption.getUnderlyingSwap()[0].getFirstLeg().getDiscountCurve());

    int N = swaption.getExpiryTime().length;
    double[] theta = new double[N + 1];
    theta[0] = 0.0;
    System.arraycopy(swaption.getExpiryTime(), 0, theta, 1, N);
    AnnuityPaymentFixed[] cashflow = new AnnuityPaymentFixed[N];
    for (int loopexp = 0; loopexp < N; loopexp++) {
      cashflow[loopexp] = CFEC.visit(swaption.getUnderlyingSwap()[loopexp], hwData);
    }
    int[] n = new int[N];
    double[][][] alpha = new double[N][][];
    double[][][] alpha2 = new double[N][][]; // alpha^2

    for (int loopexp = 0; loopexp < N; loopexp++) {
      n[loopexp] = cashflow[loopexp].getNumberOfPayments();
      alpha[loopexp] = new double[loopexp + 1][];
      alpha2[loopexp] = new double[loopexp + 1][];
      for (int k = 0; k <= loopexp; k++) {
        alpha[loopexp][k] = new double[n[loopexp]];
        alpha2[loopexp][k] = new double[n[loopexp]];
        for (int l = 0; l < alpha[loopexp][k].length; l++) {
          alpha[loopexp][k][l] = MODEL.alpha(theta[k], theta[k + 1], theta[k + 1], cashflow[loopexp].getNthPayment(l).getPaymentTime(), hwData.getHullWhiteParameter());
          alpha2[loopexp][k][l] = alpha[loopexp][k][l] * alpha[loopexp][k][l];
        }
      }
    }

    int nbPoint2 = 2 * _nbPoint + 1;
    int[] startInt = new int[N - 1];
    int[] endInt = new int[N - 1];
    for (int i = 1; i < N - 1; i++) {
      startInt[i] = 0;
      endInt[i] = nbPoint2 - 1;
    }
    startInt[0] = _nbPoint;
    endInt[0] = _nbPoint;

    double[][] t = new double[N][]; // payment time
    double[][] dfS = new double[N][]; // discount factor
    double[] beta = new double[N];
    double[][] H = new double[N][];
    double[][] Sa2 = new double[N][];

    for (int loopexp = 0; loopexp < N; loopexp++) {
      beta[loopexp] = MODEL.beta(theta[loopexp], theta[loopexp + 1], hwData.getHullWhiteParameter());
      t[loopexp] = new double[n[loopexp]];
      dfS[loopexp] = new double[n[loopexp]];
      H[loopexp] = new double[n[loopexp]];
      Sa2[loopexp] = new double[n[loopexp]];
      for (int loopcf = 0; loopcf < n[loopexp]; loopcf++) {
        t[loopexp][loopcf] = cashflow[loopexp].getNthPayment(loopcf).getPaymentTime();
        dfS[loopexp][loopcf] = discountingCurve.getDiscountFactor(t[loopexp][loopcf]);
        H[loopexp][loopcf] = (Math.exp(hwData.getHullWhiteParameter().getMeanReversion() * t[loopexp][loopcf]) - 1) / hwData.getHullWhiteParameter().getMeanReversion();
        tmpdb = 0.0;
        for (int k = 0; k <= loopexp; k++) {
          tmpdb += alpha2[loopexp][k][loopcf];
        }
        Sa2[loopexp][loopcf] = tmpdb;
      }
    }
    double[] discountedCashFlowN = new double[n[N - 1]];
    for (int loopcf = 0; loopcf < n[N - 1]; loopcf++) {
      discountedCashFlowN[loopcf] = dfS[N - 1][loopcf] * cashflow[N - 1].getNthPayment(loopcf).getAmount();
    }
    double lambda = MODEL.lambda(discountedCashFlowN, Sa2[N - 1], H[N - 1]);
    double[] betaSort = new double[N];
    System.arraycopy(beta, 0, betaSort, 0, N);
    Arrays.sort(betaSort);
    double minbeta = betaSort[0];
    double maxbeta = betaSort[N - 1];

    double b = Math.min(10 * minbeta, maxbeta);
    double epsilon = -2.0 / _nbPoint * NORMAL.getInverseCDF(1.0 / (50.0 * _nbPoint)) * b;
    double[] bX = new double[nbPoint2];
    for (int looppt = 0; looppt < nbPoint2; looppt++) {
      bX[looppt] = -_nbPoint * epsilon + looppt * epsilon;
    }
    double[] bX2 = new double[4 * _nbPoint + 1];
    for (int looppt = 0; looppt < 4 * _nbPoint + 1; looppt++) {
      bX2[looppt] = -2 * _nbPoint * epsilon + looppt * epsilon;
    }
    double[] Htheta = new double[N];
    for (int loopexp = 0; loopexp < N; loopexp++) {
      Htheta[loopexp] = (1 - Math.exp(-theta[loopexp] * hwData.getHullWhiteParameter().getMeanReversion())) / hwData.getHullWhiteParameter().getMeanReversion();
    }

    double[][] Z = new double[N - 1][nbPoint2];
    for (int i = N - 2; i >= 0; i--) {
      for (int looppt = 0; looppt < nbPoint2; looppt++) {
        Z[i][looppt] = Math.exp(bX[looppt] * Htheta[i]);
      }
    }
    double[] Sa2N_1 = new double[n[N - 1]];
    for (int i = 0; i < n[N - 1]; i++) {
      tmpdb = 0;
      for (int k = 0; k <= N - 2; k++) {
        tmpdb += alpha2[N - 1][k][i];
      }
      Sa2N_1[i] = tmpdb;
    }

    double[][] W = new double[N][];
    double[][] T = new double[N - 1][];

    double omega = -Math.signum(cashflow[N - 1].getNthPayment(0).getAmount());
    double[] kappaL = new double[nbPoint2];
    for (int looppt = 0; looppt < nbPoint2; looppt++) {
      kappaL[looppt] = (lambda - bX[looppt]) / beta[N - 1];
    }

    W[N - 1] = new double[4 * _nbPoint + 1];
    for (int j = 0; j < n[N - 1]; j++) {
      for (int looppt = 0; looppt < nbPoint2; looppt++) {
        W[N - 1][_nbPoint + looppt] += discountedCashFlowN[j] * Math.exp(-Sa2N_1[j] - H[N - 1][j] * bX[looppt]) * NORMAL.getCDF(omega * (kappaL[looppt] + alpha[N - 1][N - 1][j]));
      }
    }
    for (int looppt = 0; looppt < _nbPoint; looppt++) {
      W[N - 1][looppt] = W[N - 1][_nbPoint];
    }
    for (int looppt = 0; looppt < _nbPoint; looppt++) {
      W[N - 1][3 * _nbPoint + 1 + looppt] = W[N - 1][3 * _nbPoint];
    }

    double c1sqrt2pi = 1.0 / Math.sqrt(2 * Math.PI);
    double[][] pvcfT = new double[N - 1][];
    double[] L; // Left side of intersection
    double[] R; // Right side of intersection
    double[][] Labc;
    double[][] Rabc;
    double[][] LabcM = new double[3][4 * _nbPoint + 1];
    double[][] RabcM = new double[3][4 * _nbPoint + 1];
    double[][] Xabc = new double[3][2 * _nbPoint];

    double[] Dabc = new double[3];
    int[] indSwap = new int[N - 1]; // index of the intersection
    double xroot;
    double[][] xN = new double[N - 1][nbPoint2];
    double ci;
    double coi;
    int is;
    double[] ncdf0 = new double[nbPoint2];
    double[] ncdf1 = new double[nbPoint2];
    double[] ncdf2 = new double[nbPoint2];
    double[] ncdf0X = new double[nbPoint2 + 1];
    double[] ncdf1X;
    double[] ncdf2X;
    double ncdf0_x;
    double ncdf1_x;
    double ncdf2_x;
    double ncdf_init;

    // Main loop for the different expiry dates (except the last one)
    for (int i = N - 2; i >= 0; i--) {
      W[i] = new double[4 * _nbPoint + 1];
      T[i] = new double[4 * _nbPoint + 1];
      // T: swap
      pvcfT[i] = new double[n[i]];
      for (int j = 0; j < n[i]; j++) {
        pvcfT[i][j] = cashflow[i].getNthPayment(j).getAmount() * dfS[i][j];
        for (int looppt = 0; looppt < 4 * _nbPoint + 1; looppt++) {
          T[i][looppt] += pvcfT[i][j] * Math.exp(-Sa2[i][j] / 2.0 - H[i][j] * bX2[looppt]);

        }
      }
      // Preparation
      for (int looppt = 0; looppt < nbPoint2; looppt++) {
        xN[i][looppt] = bX[looppt] / beta[i];
      }
      ci = Htheta[i] * beta[i];
      coi = Math.exp(ci * ci / 2);

      // Left/Right
      if (omega < 0) {
        L = W[i + 1];
        R = T[i];
      } else {
        R = W[i + 1];
        L = T[i];
      }
      indSwap[i] = 0;
      while (L[indSwap[i] + 1] >= R[indSwap[i] + 1]) {
        indSwap[i]++;
      }
      // Parabola fit
      Labc = parafit(epsilon / beta[i], L);
      Rabc = parafit(epsilon / beta[i], R);
      for (int k = 0; k < 3; k++) {
        Dabc[k] = Labc[k][indSwap[i]] - Rabc[k][indSwap[i]];
        System.arraycopy(Labc[k], 0, LabcM[k], 0, indSwap[i] + 1);
        System.arraycopy(Labc[k], indSwap[i], LabcM[k], indSwap[i] + 1, Labc[k].length - indSwap[i]);
        System.arraycopy(Rabc[k], 0, RabcM[k], 0, indSwap[i] + 1);
        System.arraycopy(Rabc[k], indSwap[i], RabcM[k], indSwap[i] + 1, Rabc[k].length - indSwap[i]);
      }

      for (int looppt = 0; looppt < 4 * _nbPoint + 1; looppt++) {
        LabcM[1][looppt] = LabcM[1][looppt] + LabcM[0][looppt] * 2 * ci;
        LabcM[2][looppt] = LabcM[2][looppt] + LabcM[1][looppt] * ci - LabcM[0][looppt] * ci * ci;
        RabcM[1][looppt] = RabcM[1][looppt] + RabcM[0][looppt] * 2 * ci;
        RabcM[2][looppt] = RabcM[2][looppt] + RabcM[1][looppt] * ci - RabcM[0][looppt] * ci * ci;
      }
      xroot = (-Dabc[1] - Math.sqrt(Dabc[1] * Dabc[1] - 4 * Dabc[0] * Dabc[2])) / (2 * Dabc[0]);

      ncdf_init = NORMAL.getCDF(xN[i][0]);

      for (int looppt = 0; looppt < nbPoint2; looppt++) {
        ncdf0[looppt] = NORMAL.getCDF(xN[i][looppt] - ci);
        ncdf1[looppt] = -c1sqrt2pi * Math.exp(-(xN[i][looppt] - ci) * (xN[i][looppt] - ci) / 2.0);
        ncdf2[looppt] = ncdf1[looppt] * (xN[i][looppt] - ci) + ncdf0[looppt];
      }

      for (int j = startInt[i]; j <= endInt[i]; j++) {
        is = indSwap[i] - j + 1;
        // % all L
        if (j + 2 * _nbPoint <= indSwap[i]) {
          for (int k = 0; k < 3; k++) {
            System.arraycopy(LabcM[k], j, Xabc[k], 0, 2 * _nbPoint);
          }
          for (int looppt = 0; looppt < 2 * _nbPoint; looppt++) {
            Xabc[1][looppt] = Xabc[1][looppt] + Xabc[0][looppt] * 2 * xN[i][j];
            Xabc[2][looppt] = Xabc[2][looppt] + Xabc[1][looppt] * xN[i][j] - Xabc[0][looppt] * xN[i][j] * xN[i][j];
          }
          W[i][j + _nbPoint] = 0;
          W[i][j + _nbPoint] = W[i][j + _nbPoint] + coi * ni2ncdf(ncdf2, ncdf1, ncdf0, Xabc);
        } else if (j < indSwap[i]) {
          tmpdb = xroot - xN[i][j] - ci;
          ncdf0_x = NORMAL.getCDF(tmpdb);
          ncdf1_x = -Math.exp(-(tmpdb * tmpdb) / 2) * c1sqrt2pi;
          ncdf2_x = ncdf1_x * tmpdb + ncdf0_x;
          //          for (int k = 0; k < 3; k++) {
          //            Xabc[k] = LabcM[k].getSubVector(j, 2 * nbPoint_ + 1);
          //            Xabc[k].setSubVector(indSwap[i] + 1 - j, RabcM[k].getSubVector(indSwap[i] + 1, j + 2 * nbPoint_ - indSwap[i]));
          //          }
          //          Xabc[1] = Xabc[1].add(Xabc[0].mapMultiply(2 * xN[i].getEntry(j)));
          //          Xabc[2] = Xabc[2].add(Xabc[1].mapMultiply(xN[i].getEntry(j))).subtract(Xabc[0].mapMultiply(xN[i].getEntry(j) * xN[i].getEntry(j)));
          //          ncdf0X.setSubVector(0, ncdf0.getSubVector(0, is));
          //          ncdf0X.setEntry(is, ncdf0_x);
          //          ncdf0X.setSubVector(is + 1, ncdf0.getSubVector(is, ncdf0.getDimension() - is));
          //          ncdf1X = ncdf1.getSubVector(0, is).append(ncdf1_x).append(ncdf1.getSubVector(is, ncdf1.getDimension() - is));
          //          ncdf2X = ncdf2.getSubVector(0, is).append(ncdf2_x).append(ncdf2.getSubVector(is, ncdf2.getDimension() - is));
          //          W[i][j + _nbPoint] = L[j] * Z[i][0] * ncdf_init;
          //          W[i][j + _nbPoint] += +coi * ni2ncdf(ncdf2X, ncdf1X, ncdf0X, Xabc);
          //          W[i][j + _nbPoint] += R[j + 2 * _nbPoint] * Z[i][Z[i].length - 1] * ncdf_init;
          //        }else {
          //          for (int k = 0; k < 3; k++) {
          //            Xabc[k] = RabcM[k].getSubVector(j + 1, 2 * nbPoint_);
          //          }
          //          Xabc[1] = Xabc[1].add(Xabc[0].mapMultiply(2 * xN[i]
          //              .getEntry(j)));
          //          Xabc[2] = Xabc[2].add(
          //              Xabc[1].mapMultiply(xN[i].getEntry(j))).subtract(
          //              Xabc[0].mapMultiply(xN[i].getEntry(j)
          //                  * xN[i].getEntry(j)));
          W[i][j + _nbPoint] = R[j] * Z[i][0] * ncdf_init;
          W[i][j + _nbPoint] += coi * ni2ncdf(ncdf2, ncdf1, ncdf0, Xabc);
          W[i][j + _nbPoint] += R[j + 2 * _nbPoint] * Z[i][Z[i].length - 1] * ncdf_init;
        }
      }
    }

    return CurrencyAmount.of(swaption.getUnderlyingSwap()[0].getFixedLeg().getCurrency(), 0.0);
  }

  @Override
  public CurrencyAmount presentValue(InterestRateDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof SwaptionBermudaFixedIbor, "Physical delivery swaption");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue(instrument, curves);
  }

  private static double[][] parafit(double dx, double[] y) {
    int nbPts = y.length;
    int nbStep = (nbPts - 1) / 2;
    double dx2 = dx * dx;
    double[] x1 = new double[nbStep];
    double[] x = new double[nbStep];
    for (int i = 0; i < nbStep; i++) {
      x1[i] = -nbStep + 2.0 * i;
      x[i] = x1[i] * dx;
    }
    double[][] abc = new double[3][2 * nbStep];
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

  private static double ni2ncdf(double[] n2, double[] n1, double[] n0, double[][] abc) {
    double s = 0;
    for (int i = 0; i < abc[0].length; i++) {
      s += abc[0][i] * (n2[i + 1] - n2[i]);
      s += abc[1][i] * (n1[i + 1] - n1[i]);
      s += abc[2][i] * (n0[i + 1] - n0[i]);
    }
    return s;
  }
}
