/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.montecarlo;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.math.linearalgebra.CholeskyDecompositionCommons;
import com.opengamma.math.linearalgebra.CholeskyDecompositionResult;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.random.RandomNumberGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Monte Carlo pricing method for swaptions in the hull-White one factor model.
 * The Monte Carlo is on the solution of the discount factor (not on the equation of the short rate).
 */
public class HullWhiteMonteCarloMethod extends MonteCarloMethod {

  /**
   * The decision schedule calculator (calculate the exercise dates, the cash flow dates and the reference amounts).
   */
  private static final DecisionScheduleCalculator DC = DecisionScheduleCalculator.getInstance();
  /**
   * The calculator from discount factors (calculate the price from simulated discount factors and the reference amounts).
   */
  private static final MonteCarloDiscountFactorCalculator MCC = MonteCarloDiscountFactorCalculator.getInstance();
  /**
   * The Hull-White one factor model.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  /**
   * The number of paths in one block.
   */
  private static final int BLOCK_SIZE = 1000;

  /**
   * @param numberGenerator The random number generator.
   * @param nbPath The number of paths.
   */
  public HullWhiteMonteCarloMethod(RandomNumberGenerator numberGenerator, int nbPath) {
    super(numberGenerator, nbPath);
  }

  /**
   * Computes the present value for European swaption in the Hull-White one factor model by Monte-Carlo.
   * Implementation note: The total number of paths is divided in blocks of maximum size BLOCK_SIZE=1000. The Monte Carlo is run on each block and the average of each
   * block price is the total price. 
   * @param instrument The swaption.
   * @param ccy TODO
   * @param dsc TODO
   * @param hwData The Hull-White data (curves and Hull-White parameters).
   * @return The present value.
   */
  public CurrencyAmount presentValue(final InterestRateDerivative instrument, Currency ccy, YieldAndDiscountCurve dsc, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    // TODO: remove currency and dsc curve
    DecisionSchedule decision = DC.visit(instrument, hwData);
    double[] decisionTime = decision.getDecisionTime();
    double[][] impactTime = decision.getImpactTime();
    int nbJump = decisionTime.length;
    double numeraireTime = decisionTime[nbJump - 1];
    double pDN = dsc.getDiscountFactor(numeraireTime);
    // Discount factor to numeraire date for rebasing.
    double[][] pDI = new double[nbJump][];
    // Initial discount factors to each impact date.
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      pDI[loopjump] = new double[impactTime[loopjump].length];
      for (int i = 0; i < impactTime[loopjump].length; i++) {
        pDI[loopjump][i] = dsc.getDiscountFactor(impactTime[loopjump][i]) / pDN;
      }
    }
    double[] gamma = new double[nbJump];
    double[][] cov = new double[nbJump][nbJump];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      gamma[loopjump] = MODEL.beta(0.0, decisionTime[loopjump], hwData.getHullWhiteParameter());
      gamma[loopjump] = gamma[loopjump] * gamma[loopjump];
      cov[loopjump][loopjump] = gamma[loopjump];
      for (int j = loopjump + 1; j < nbJump; j++) {
        cov[j][loopjump] = gamma[loopjump];
        cov[loopjump][j] = gamma[loopjump];
      }
    }
    double[][] h = MODEL.volatilityMaturityPart(hwData.getHullWhiteParameter(), numeraireTime, impactTime); // jump/cf
    double[][] h2 = new double[nbJump][];
    for (int i = 0; i < nbJump; i++) {
      h2[i] = new double[h[i].length];
      for (int j = 0; j < h[i].length; j++) {
        h2[i][j] = h[i][j] * h[i][j] / 2;
      }
    }
    // To remove the 0 (fixed coupons)
    int nbZero = 0;
    while (cov[nbZero][nbZero] < 1.0E-12) {
      nbZero++;
    }
    double[][] cov2 = new double[nbJump - nbZero][nbJump - nbZero];
    for (int loopjump = 0; loopjump < nbJump - nbZero; loopjump++) {
      for (int loopjump2 = 0; loopjump2 < nbJump - nbZero; loopjump2++) {
        cov2[loopjump][loopjump2] = cov[loopjump + nbZero][loopjump2 + nbZero];
      }
    }
    CholeskyDecompositionCommons cd = new CholeskyDecompositionCommons();
    CholeskyDecompositionResult cdr2 = cd.evaluate(new DoubleMatrix2D(cov2));
    double[][] covCD2 = cdr2.getL().toArray();
    double[][] covCD = new double[nbJump][nbJump];
    for (int loopjump = 0; loopjump < nbJump - nbZero; loopjump++) {
      for (int loopjump2 = 0; loopjump2 < nbJump - nbZero; loopjump2++) {
        covCD[loopjump + nbZero][loopjump2 + nbZero] = covCD2[loopjump][loopjump2];
      }
    }

    int nbBlock = (int) Math.round(Math.ceil(getNbPath() / ((double) BLOCK_SIZE)));
    int[] nbPath2 = new int[nbBlock];
    for (int i = 0; i < nbBlock - 1; i++) {
      nbPath2[i] = BLOCK_SIZE;
    }
    nbPath2[nbBlock - 1] = getNbPath() - (nbBlock - 1) * BLOCK_SIZE;
    double[][] impactAmount = decision.getImpactAmount();
    double pv = 0;
    for (int loopblock = 0; loopblock < nbBlock; loopblock++) {
      double[][] x = getNormalArray(nbJump, nbPath2[loopblock]);

      double[][] y = new double[nbJump][nbPath2[loopblock]]; // jump/path
      double s; // tmp sum
      for (int looppath = 0; looppath < nbPath2[loopblock]; looppath++) {
        for (int i = 0; i < nbJump; i++) {
          s = 0;
          for (int j = 0; j < nbJump; j++) {
            s += x[j][looppath] * covCD[i][j];
          }
          y[i][looppath] = s;
        }
      }
      Double[][][] pD = pathGeneratorDiscount(pDI, y, h, h2, gamma);
      pv += MCC.visit(instrument, new MonteCarloDiscountFactorDataBundle(pD, impactAmount)) * nbPath2[loopblock];
    }
    pv *= pDN / getNbPath(); // Multiply by the numeraire.
    return CurrencyAmount.of(ccy, pv);
  }

  /**
   * Gets a 2D-array of independent normally distributed variables.
   * @param nbJump The number of jumps.
   * @param nbPath The number of paths.
   * @return The array of variables.
   */
  private double[][] getNormalArray(int nbJump, int nbPath) {
    double[][] result = new double[nbJump][nbPath];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      result[loopjump] = getNumberGenerator().getVector(nbPath);
    }
    return result;
  }

  /**
   * Construct the discount factors on the simulated paths from the random variables and the model constants.
   * @param initDiscountFactor The initial discount factors.
   * @param y The correlated random variables.
   * @param h The H parameters.
   * @param h2 The H^2 parameters.
   * @param gamma The gamma parameters.
   * @return The discount factor paths.
   */
  private Double[][][] pathGeneratorDiscount(double[][] initDiscountFactor, double[][] y, double[][] h, double[][] h2, double[] gamma) {
    int nbJump = y.length;
    int nbPath = y[0].length;
    Double[][][] pD = new Double[nbPath][nbJump][];
    double[] h2gamma;
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      int nbCF = h[loopjump].length;
      h2gamma = new double[nbCF];
      for (int loopcf = 0; loopcf < nbCF; loopcf++) {
        h2gamma[loopcf] = h2[loopjump][loopcf] * gamma[loopjump];
      }
      for (int looppath = 0; looppath < nbPath; looppath++) {
        pD[looppath][loopjump] = new Double[nbCF];
        for (int loopcf = 0; loopcf < nbCF; loopcf++) {
          pD[looppath][loopjump][loopcf] = initDiscountFactor[loopjump][loopcf] * Math.exp(-h[loopjump][loopcf] * y[loopjump][looppath] - h2gamma[loopcf]);
        }
      }
    }
    return pD;
  }

  @Override
  public CurrencyAmount presentValue(InterestRateDerivative instrument, YieldCurveBundle curves) {
    return null;
    // TODO: generalize to other interest rate derivatives.
  }

}
