/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo.provider;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.G2ppPiecewiseConstantModel;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.montecarlo.DecisionSchedule;
import com.opengamma.analytics.financial.montecarlo.MonteCarloDiscountFactorCalculator;
import com.opengamma.analytics.financial.montecarlo.MonteCarloDiscountFactorDataBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.linearalgebra.CholeskyDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.CholeskyDecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.random.RandomNumberGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Monte Carlo pricing method in the G2++ two factors model.
 * The Monte Carlo is on the solution of the discount factor (not on the equation of the short rate).
 */
public class G2ppMonteCarloMethod extends MonteCarloMethod {

  /**
   * The decision schedule calculator (calculate the exercise dates, the cash flow dates and the reference amounts).
   */
  private static final DecisionScheduleCalculator DC = DecisionScheduleCalculator.getInstance();
  //  /**
  //   * The decision schedule derivative calculator (calculate the exercise dates, the cash flow dates, the reference amounts and the sensitivity of the reference amount to the curves).
  //   */
  //  private static final DecisionScheduleDerivativeCalculator DDC = DecisionScheduleDerivativeCalculator.getInstance();
  /**
   * The calculator from discount factors (calculate the price from simulated discount factors and the reference amounts).
   */
  private static final MonteCarloDiscountFactorCalculator MCC = MonteCarloDiscountFactorCalculator.getInstance();
  //  /**
  //   * The calculator of price and derivatives from discount factors and reference amounts.
  //   */
  //  private static final MonteCarloDiscountFactorDerivativeCalculator MCDC = MonteCarloDiscountFactorDerivativeCalculator.getInstance();
  /**
   * The Hull-White one factor model.
   */
  private static final G2ppPiecewiseConstantModel MODEL = new G2ppPiecewiseConstantModel();
  /**
   * The number of paths in one block.
   */
  private static final int BLOCK_SIZE = 1000;

  /**
   * @param numberGenerator The random number generator.
   * @param nbPath The number of paths.
   */
  public G2ppMonteCarloMethod(final RandomNumberGenerator numberGenerator, final int nbPath) {
    super(numberGenerator, nbPath);
  }

  /**
   * Computes the present value in the G2++ two factors model by Monte-Carlo.
   * Implementation note: The total number of paths is divided in blocks of maximum size BLOCK_SIZE=1000. The Monte Carlo is run on each block and the average of each
   * block price is the total price. 
   * @param instrument The swaption.
   * @param ccy The currency
   * @param g2Data The G2++ data (curves and G2++ parameters).
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final Currency ccy, final G2ppProviderInterface g2Data) {
    MulticurveProviderInterface multicurves = g2Data.getMulticurveProvider();
    G2ppPiecewiseConstantParameters parameters = g2Data.getG2ppParameters();
    final DecisionSchedule decision = instrument.accept(DC, multicurves);
    final double[] decisionTime = decision.getDecisionTime();
    final double[][] impactTime = decision.getImpactTime();
    final int nbJump = decisionTime.length;
    final double numeraireTime = decisionTime[nbJump - 1];
    final double pDN = multicurves.getDiscountFactor(ccy, numeraireTime);
    // Discount factor to numeraire date for rebasing.
    final double[][] pDI = new double[nbJump][];
    // Initial discount factors to each impact date.
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      pDI[loopjump] = new double[impactTime[loopjump].length];
      for (int i = 0; i < impactTime[loopjump].length; i++) {
        pDI[loopjump][i] = multicurves.getDiscountFactor(ccy, impactTime[loopjump][i]) / pDN;
      }
    }
    final double rhog2pp = parameters.getCorrelation();
    final double[][][] h = MODEL.volatilityMaturityPart(parameters, numeraireTime, impactTime); // factor/jump/cf
    final double[][][] gamma = new double[nbJump][2][2]; // jump/factor/factor
    final double[][] cov = new double[2 * nbJump][2 * nbJump]; // factor 0 - factor 1
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      gamma[loopjump] = MODEL.gamma(parameters, 0.0, decisionTime[loopjump]);
      for (int j = loopjump; j < nbJump; j++) {
        cov[j][loopjump] = gamma[loopjump][0][0];
        cov[loopjump][j] = gamma[loopjump][0][0];
        cov[nbJump + j][nbJump + loopjump] = gamma[loopjump][1][1];
        cov[nbJump + loopjump][nbJump + j] = gamma[loopjump][1][1];
        cov[j][nbJump + loopjump] = rhog2pp * gamma[loopjump][0][1];
        cov[loopjump][nbJump + j] = rhog2pp * gamma[loopjump][0][1];
        cov[nbJump + j][loopjump] = rhog2pp * gamma[loopjump][0][1];
        cov[nbJump + loopjump][j] = rhog2pp * gamma[loopjump][0][1];
      }
    }
    final double[][][] alpha = new double[2][nbJump][]; // factor/jump/cf
    final double[][] tau2 = new double[nbJump][];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      tau2[loopjump] = new double[impactTime[loopjump].length];
      alpha[0][loopjump] = new double[impactTime[loopjump].length];
      alpha[1][loopjump] = new double[impactTime[loopjump].length];
      for (int loopcf = 0; loopcf < impactTime[loopjump].length; loopcf++) {
        alpha[0][loopjump][loopcf] = Math.sqrt(gamma[loopjump][0][0]) * h[0][loopjump][loopcf];
        alpha[1][loopjump][loopcf] = Math.sqrt(gamma[loopjump][1][1]) * h[1][loopjump][loopcf];
        tau2[loopjump][loopcf] = alpha[0][loopjump][loopcf] * alpha[0][loopjump][loopcf] + alpha[1][loopjump][loopcf] * alpha[1][loopjump][loopcf] + 2 * rhog2pp * gamma[loopjump][0][1]
            * h[0][loopjump][loopcf] * h[1][loopjump][loopcf];
      }
    }
    final CholeskyDecompositionCommons cd = new CholeskyDecompositionCommons();
    final CholeskyDecompositionResult cdr = cd.evaluate(new DoubleMatrix2D(cov));
    final double[][] covCD = cdr.getL().getData();
    final int nbBlock = (int) Math.round(Math.ceil(getNbPath() / ((double) BLOCK_SIZE)));
    final int[] nbPath2 = new int[nbBlock];
    for (int i = 0; i < nbBlock - 1; i++) {
      nbPath2[i] = BLOCK_SIZE;
    }
    nbPath2[nbBlock - 1] = getNbPath() - (nbBlock - 1) * BLOCK_SIZE;
    final double[][] impactAmount = decision.getImpactAmount();
    double pv = 0;
    for (int loopblock = 0; loopblock < nbBlock; loopblock++) {
      final double[][] x = getNormalArray(2 * nbJump, nbPath2[loopblock]);
      final double[][] y = new double[2 * nbJump][nbPath2[loopblock]]; // jump/path
      for (int looppath = 0; looppath < nbPath2[loopblock]; looppath++) {
        for (int i = 0; i < 2 * nbJump; i++) {
          for (int j = 0; j < 2 * nbJump; j++) {
            y[i][looppath] += x[j][looppath] * covCD[i][j];
          }
        }
      }
      final Double[][][] pD = pathGeneratorDiscount(pDI, y, h, tau2);
      pv += instrument.accept(MCC, new MonteCarloDiscountFactorDataBundle(pD, impactAmount)) * nbPath2[loopblock];
    }
    pv *= pDN / getNbPath(); // Multiply by the numeraire.
    return MultipleCurrencyAmount.of(ccy, pv);
  }

  /**
   * Gets a 2D-array of independent normally distributed variables.
   * @param nbJump The number of jumps.
   * @param nbPath The number of paths.
   * @return The array of variables.
   */
  private double[][] getNormalArray(final int nbJump, final int nbPath) {
    final double[][] result = new double[nbJump][nbPath];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      result[loopjump] = getNumberGenerator().getVector(nbPath);
    }
    return result;
  }

  /**
   * Construct the discount factors on the simulated paths from the random variables and the model constants.
   * @param initDiscountFactor The initial discount factors. jump/cf
   * @param y The correlated random variables. jump0+jump1/path.
   * @param h The H parameters. factor/jump/cf
   * @param tau2 The square of total volatilities. jump/cf 
   * @return The discount factor paths (path/jump/cf).
   */
  private Double[][][] pathGeneratorDiscount(final double[][] initDiscountFactor, final double[][] y, final double[][][] h, final double[][] tau2) {
    final int nbJump = y.length / 2;
    final int nbPath = y[0].length;
    final Double[][][] pD = new Double[nbPath][nbJump][];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      final int nbCF = h[0][loopjump].length;
      for (int looppath = 0; looppath < nbPath; looppath++) {
        pD[looppath][loopjump] = new Double[nbCF];
        for (int loopcf = 0; loopcf < nbCF; loopcf++) {
          pD[looppath][loopjump][loopcf] = initDiscountFactor[loopjump][loopcf]
              * Math.exp(-h[0][loopjump][loopcf] * y[loopjump][looppath] - h[1][loopjump][loopcf] * y[nbJump + loopjump][looppath] - 0.5 * tau2[loopjump][loopcf]);
        }
      }
    }
    return pD;
  }

}
