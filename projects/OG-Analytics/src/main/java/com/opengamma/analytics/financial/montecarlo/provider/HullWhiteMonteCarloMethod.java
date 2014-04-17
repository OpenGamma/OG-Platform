/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.montecarlo.DecisionSchedule;
import com.opengamma.analytics.financial.montecarlo.MonteCarloDiscountFactorCalculator;
import com.opengamma.analytics.financial.montecarlo.MonteCarloDiscountFactorDataBundle;
import com.opengamma.analytics.financial.montecarlo.MonteCarloDiscountFactorDerivativeCalculator;
import com.opengamma.analytics.financial.montecarlo.MonteCarloDiscountFactorDerivativeDataBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.linearalgebra.CholeskyDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.CholeskyDecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.random.RandomNumberGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Monte Carlo pricing method in the hull-White one factor model.
 * The Monte Carlo is on the solution of the discount factor (not on the equation of the short rate).
 */
public class HullWhiteMonteCarloMethod extends MonteCarloMethod {

  /**
   * The decision schedule calculator (calculate the exercise dates, the cash flow dates and the reference amounts).
   */
  private static final DecisionScheduleCalculator DC = DecisionScheduleCalculator.getInstance();
  /**
   * The decision schedule derivative calculator (calculate the exercise dates, the cash flow dates, the reference amounts and the sensitivity of the reference amount to the curves).
   */
  private static final DecisionScheduleDerivativeCalculator DDC = DecisionScheduleDerivativeCalculator.getInstance();
  /**
   * The calculator from discount factors (calculate the price from simulated discount factors and the reference amounts).
   */
  private static final MonteCarloDiscountFactorCalculator MCC = MonteCarloDiscountFactorCalculator.getInstance();
  /**
   * The calculator of price and derivatives from discount factors and reference amounts.
   */
  private static final MonteCarloDiscountFactorDerivativeCalculator MCDC = MonteCarloDiscountFactorDerivativeCalculator.getInstance();
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
  public HullWhiteMonteCarloMethod(final RandomNumberGenerator numberGenerator, final int nbPath) {
    super(numberGenerator, nbPath);
  }

  /**
   * Computes the present value in the Hull-White one factor model by Monte-Carlo.
   * Implementation note: The total number of paths is divided in blocks of maximum size BLOCK_SIZE=1000. The Monte Carlo is run on each block and the average of each
   * block price is the total price.
   * @param instrument The swaption.
   * @param ccy The currency.
   * @param hwData The Hull-White data (curves and Hull-White parameters).
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final Currency ccy, final HullWhiteOneFactorProviderInterface hwData) {
    // TODO: remove currency and dsc curve name (should be available from the instrument)
    final MulticurveProviderInterface multicurves = hwData.getMulticurveProvider();
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = hwData.getHullWhiteParameters();
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
    final double[] gamma = new double[nbJump];
    final double[][] cov = new double[nbJump][nbJump];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      gamma[loopjump] = MODEL.beta(parameters, 0.0, decisionTime[loopjump]);
      gamma[loopjump] = gamma[loopjump] * gamma[loopjump];
      cov[loopjump][loopjump] = gamma[loopjump];
      for (int j = loopjump + 1; j < nbJump; j++) {
        cov[j][loopjump] = gamma[loopjump];
        cov[loopjump][j] = gamma[loopjump];
      }
    }
    final double[][] h = MODEL.volatilityMaturityPart(parameters, numeraireTime, impactTime); // jump/cf
    final double[][] h2 = new double[nbJump][];
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
    final double[][] cov2 = new double[nbJump - nbZero][nbJump - nbZero];
    for (int loopjump = 0; loopjump < nbJump - nbZero; loopjump++) {
      for (int loopjump2 = 0; loopjump2 < nbJump - nbZero; loopjump2++) {
        cov2[loopjump][loopjump2] = cov[loopjump + nbZero][loopjump2 + nbZero];
      }
    }
    final CholeskyDecompositionCommons cd = new CholeskyDecompositionCommons();
    final CholeskyDecompositionResult cdr2 = cd.evaluate(new DoubleMatrix2D(cov2));
    final double[][] covCD2 = cdr2.getL().toArray();
    final double[][] covCD = new double[nbJump][nbJump];
    for (int loopjump = 0; loopjump < nbJump - nbZero; loopjump++) {
      for (int loopjump2 = 0; loopjump2 < nbJump - nbZero; loopjump2++) {
        covCD[loopjump + nbZero][loopjump2 + nbZero] = covCD2[loopjump][loopjump2];
      }
    }
    final int nbBlock = (int) Math.round(Math.ceil(getNbPath() / ((double) BLOCK_SIZE)));
    final int[] nbPath2 = new int[nbBlock];
    for (int i = 0; i < nbBlock - 1; i++) {
      nbPath2[i] = BLOCK_SIZE;
    }
    nbPath2[nbBlock - 1] = getNbPath() - (nbBlock - 1) * BLOCK_SIZE;
    final double[][] impactAmount = decision.getImpactAmount();
    double pv = 0;
    for (int loopblock = 0; loopblock < nbBlock; loopblock++) {
      final double[][] x = getNormalArray(nbJump, nbPath2[loopblock]);
      final double[][] y = new double[nbJump][nbPath2[loopblock]]; // jump/path
      for (int looppath = 0; looppath < nbPath2[loopblock]; looppath++) {
        for (int i = 0; i < nbJump; i++) {
          for (int j = 0; j < nbJump; j++) {
            y[i][looppath] += x[j][looppath] * covCD[i][j];
          }
        }
      }
      final Double[][][] pD = pathGeneratorDiscount(pDI, y, h, h2, gamma);
      pv += instrument.accept(MCC, new MonteCarloDiscountFactorDataBundle(pD, impactAmount)) * nbPath2[loopblock];
    }
    pv *= pDN / getNbPath(); // Multiply by the numeraire.
    return MultipleCurrencyAmount.of(ccy, pv);
  }

  /**
   * Computes the present value curve sensitivity in the Hull-White one factor model by Monte-Carlo. The sensitivity is computed by Adjoint Algorithmic Differentiation.
   * Implementation note: The total number of paths is divided in blocks of maximum size BLOCK_SIZE=1000. The Monte Carlo is run on each block and the average of each
   * block price is the total price.
   * @param instrument The swaption.
   * @param ccy The currency.
   * @param hwData The Hull-White data (curves and Hull-White parameters).
   * @return The curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final InstrumentDerivative instrument, final Currency ccy, final HullWhiteOneFactorProviderInterface hwData) {
    final MulticurveProviderInterface multicurves = hwData.getMulticurveProvider();
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = hwData.getHullWhiteParameters();
    // Forward sweep
    final DecisionScheduleDerivative decision = instrument.accept(DDC, multicurves);
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
    final double[] gamma = new double[nbJump];
    final double[][] cov = new double[nbJump][nbJump];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      gamma[loopjump] = MODEL.beta(parameters, 0.0, decisionTime[loopjump]);
      gamma[loopjump] = gamma[loopjump] * gamma[loopjump];
      cov[loopjump][loopjump] = gamma[loopjump];
      for (int j = loopjump + 1; j < nbJump; j++) {
        cov[j][loopjump] = gamma[loopjump];
        cov[loopjump][j] = gamma[loopjump];
      }
    }
    final double[][] h = MODEL.volatilityMaturityPart(parameters, numeraireTime, impactTime); // jump/cf
    final double[][] h2 = new double[nbJump][];
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
    final double[][] cov2 = new double[nbJump - nbZero][nbJump - nbZero];
    for (int loopjump = 0; loopjump < nbJump - nbZero; loopjump++) {
      for (int loopjump2 = 0; loopjump2 < nbJump - nbZero; loopjump2++) {
        cov2[loopjump][loopjump2] = cov[loopjump + nbZero][loopjump2 + nbZero];
      }
    }
    final CholeskyDecompositionCommons cd = new CholeskyDecompositionCommons();
    final CholeskyDecompositionResult cdr2 = cd.evaluate(new DoubleMatrix2D(cov2));
    final double[][] covCD2 = cdr2.getL().toArray();
    final double[][] covCD = new double[nbJump][nbJump];
    for (int loopjump = 0; loopjump < nbJump - nbZero; loopjump++) {
      for (int loopjump2 = 0; loopjump2 < nbJump - nbZero; loopjump2++) {
        covCD[loopjump + nbZero][loopjump2 + nbZero] = covCD2[loopjump][loopjump2];
      }
    }
    final int nbBlock = (int) Math.round(Math.ceil(getNbPath() / ((double) BLOCK_SIZE)));
    final int[] nbPath2 = new int[nbBlock];
    for (int i = 0; i < nbBlock - 1; i++) {
      nbPath2[i] = BLOCK_SIZE;
    }
    nbPath2[nbBlock - 1] = getNbPath() - (nbBlock - 1) * BLOCK_SIZE;
    final double[][] impactAmount = decision.getImpactAmount();
    double pv = 0;
    final double[] pvBlock = new double[nbBlock];
    // Backward sweep (init)
    final double pvBar = 1.0;
    final double[] pvBlockBar = new double[nbBlock];
    for (int loopblock = 0; loopblock < nbBlock; loopblock++) {
      pvBlockBar[loopblock] = pDN / getNbPath() * pvBar;
    }
    final double[][] impactAmountBar = new double[nbJump][];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      impactAmountBar[loopjump] = new double[impactAmount[loopjump].length];
    }
    // Forward sweep (end) and backward sweep (main)
    final double[][] pDIBar = new double[nbJump][];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      pDIBar[loopjump] = new double[impactAmount[loopjump].length];
    }
    for (int loopblock = 0; loopblock < nbBlock; loopblock++) {
      final double[][] x = getNormalArray(nbJump, nbPath2[loopblock]);
      final double[][] y = new double[nbJump][nbPath2[loopblock]]; // jump/path
      for (int looppath = 0; looppath < nbPath2[loopblock]; looppath++) {
        for (int i = 0; i < nbJump; i++) {
          for (int j = 0; j < nbJump; j++) {
            y[i][looppath] += x[j][looppath] * covCD[i][j];
          }
        }
      }
      final Double[][][] pD = pathGeneratorDiscount(pDI, y, h, h2, gamma);
      final MonteCarloDiscountFactorDerivativeDataBundle mcdDB = new MonteCarloDiscountFactorDerivativeDataBundle(pD, impactAmount);
      pvBlock[loopblock] = instrument.accept(MCDC, mcdDB) * nbPath2[loopblock];
      pv += pvBlock[loopblock];
      // Backward sweep (in block loop)
      for (int loopjump = 0; loopjump < nbJump; loopjump++) {
        for (int loopimp = 0; loopimp < impactAmount[loopjump].length; loopimp++) {
          impactAmountBar[loopjump][loopimp] += mcdDB.getImpactAmountDerivative()[loopjump][loopimp] * nbPath2[loopblock] * pvBlockBar[loopblock];
        }
      }
      final Double[][][] pDBar = new Double[nbPath2[loopblock]][nbJump][];
      for (int looppath = 0; looppath < nbPath2[loopblock]; looppath++) {
        for (int loopjump = 0; loopjump < nbJump; loopjump++) {
          pDBar[looppath][loopjump] = new Double[impactAmount[loopjump].length];
          for (int loopimp = 0; loopimp < impactAmount[loopjump].length; loopimp++) {
            pDBar[looppath][loopjump][loopimp] = mcdDB.getPathDiscountingFactorDerivative()[looppath][loopjump][loopimp] * nbPath2[loopblock] * pvBlockBar[loopblock];
          }
        }
      }
      final double[][] pDIBarTemp = pathGeneratorDiscountAdjointIDF(pDI, y, h, h2, gamma, pDBar);
      for (int loopjump = 0; loopjump < nbJump; loopjump++) {
        for (int loopimp = 0; loopimp < impactAmount[loopjump].length; loopimp++) {
          pDIBar[loopjump][loopimp] += pDIBarTemp[loopjump][loopimp];
        }
      }
    }
    pv *= pDN / getNbPath(); // Multiply by the numeraire.
    // Backward sweep (end)
    double pDNBar = pv / pDN * pvBar;
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      for (int loopimp = 0; loopimp < impactTime[loopjump].length; loopimp++) {
        pDNBar += -multicurves.getDiscountFactor(ccy, impactTime[loopjump][loopimp]) / (pDN * pDN) * pDIBar[loopjump][loopimp];
      }
    }
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(numeraireTime, -numeraireTime * pDN * pDNBar));
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      for (int loopimp = 0; loopimp < impactTime[loopjump].length; loopimp++) {
        listDiscounting.add(DoublesPair.of(impactTime[loopjump][loopimp], -impactTime[loopjump][loopimp] * pDI[loopjump][loopimp] * pDIBar[loopjump][loopimp]));
      }
    }
    resultMap.put(multicurves.getName(ccy), listDiscounting);
    MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    // Adding sensitivity due to cash flow equivalent sensitivity to curves.
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      final Map<Double, MulticurveSensitivity> impactAmountDerivative = decision.getImpactAmountDerivative().get(loopjump);
      for (int loopimp = 0; loopimp < impactTime[loopjump].length; loopimp++) {
        final MulticurveSensitivity sensiCfe = impactAmountDerivative.get(impactTime[loopjump][loopimp]);
        if (!(sensiCfe == null)) { // There is some sensitivity to that cfe.
          result = result.plus(sensiCfe.multipliedBy(impactAmountBar[loopjump][loopimp]));
        }
      }
    }
    result = result.cleaned();
    return MultipleCurrencyMulticurveSensitivity.of(ccy, result);
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
   * @param initDiscountFactor The initial discount factors.
   * @param y The correlated random variables. jump/path
   * @param h The H parameters. jump/cf
   * @param h2 The H^2 parameters.
   * @param gamma The gamma parameters.
   * @return The discount factor paths (path/jump/cf).
   */
  private Double[][][] pathGeneratorDiscount(final double[][] initDiscountFactor, final double[][] y, final double[][] h, final double[][] h2, final double[] gamma) {
    final int nbJump = y.length;
    final int nbPath = y[0].length;
    final Double[][][] pD = new Double[nbPath][nbJump][];
    double[] h2gamma;
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      final int nbCF = h[loopjump].length;
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

  /**
   * Computes the initial discount factors adjoint values with respect to the initDiscountFactor.
   * @param initDiscountFactor The initial discount factors.
   * @param y The correlated random variables.
   * @param h The H parameters.
   * @param h2 The H^2 parameters.
   * @param gamma The gamma parameters.
   * @param pDBar The simulated discount factor adjoints (path/jump/cf).
   * @return The initial discount factor adjoints (jump/cf).
   */
  private double[][] pathGeneratorDiscountAdjointIDF(final double[][] initDiscountFactor, final double[][] y, final double[][] h, final double[][] h2, final double[] gamma, final Double[][][] pDBar) {
    final int nbJump = y.length;
    final int nbPath = y[0].length;
    double[] h2gamma;
    final double[][] initDiscountFactorBar = new double[nbJump][];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      final int nbCF = h[loopjump].length;
      h2gamma = new double[nbCF];
      for (int loopcf = 0; loopcf < nbCF; loopcf++) {
        h2gamma[loopcf] = h2[loopjump][loopcf] * gamma[loopjump];
      }
      // Backward sweep
      initDiscountFactorBar[loopjump] = new double[nbCF];
      for (int looppath = 0; looppath < nbPath; looppath++) {
        for (int loopcf = 0; loopcf < nbCF; loopcf++) {
          initDiscountFactorBar[loopjump][loopcf] += Math.exp(-h[loopjump][loopcf] * y[loopjump][looppath] - h2gamma[loopcf]) * pDBar[looppath][loopjump][loopcf];
        }
      }
    }
    return initDiscountFactorBar;
  }

  //  /**
  //   * Computes the initial discount factors adjoint values with respect to y.
  //   * @param initDiscountFactor The initial discount factors.
  //   * @param y The correlated random variables.
  //   * @param h The H parameters.
  //   * @param h2 The H^2 parameters.
  //   * @param gamma The gamma parameters.
  //   * @param pDBar The simulated discount factor adjoints (path/jump/cf).
  //   * @return The y adjoints (jump/path).
  //   */
  //  private double[][] pathGeneratorDiscountAdjointY(double[][] initDiscountFactor, double[][] y, double[][] h, double[][] h2, double[] gamma, Double[][][] pDBar) {
  //    int nbJump = y.length;
  //    int nbPath = y[0].length;
  //    double[] h2gamma;
  //    double[][] yBar = new double[nbJump][nbPath];
  //    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
  //      int nbCF = h[loopjump].length;
  //      h2gamma = new double[nbCF];
  //      for (int loopcf = 0; loopcf < nbCF; loopcf++) {
  //        h2gamma[loopcf] = h2[loopjump][loopcf] * gamma[loopjump];
  //      }
  //      // Backward sweep
  //      for (int looppath = 0; looppath < nbPath; looppath++) {
  //        for (int loopcf = 0; loopcf < nbCF; loopcf++) {
  //          y[loopjump][looppath] += initDiscountFactor[loopjump][loopcf] * Math.exp(-h[loopjump][loopcf] * y[loopjump][looppath] - h2gamma[loopcf]) * -h[loopjump][loopcf]
  //              * pDBar[looppath][loopjump][loopcf];
  //        }
  //      }
  //    }
  //    return yBar;
  //  }
  //
  //  /**
  //   * Computes the initial discount factors adjoint values with respect to gamma.
  //   * @param initDiscountFactor The initial discount factors.
  //   * @param y The correlated random variables.
  //   * @param h The H parameters.
  //   * @param h2 The H^2 parameters.
  //   * @param gamma The gamma parameters.
  //   * @param pDBar The simulated discount factor adjoints (path/jump/cf).
  //   * @return The y adjoints (jump/path).
  //   */
  //  private double[] pathGeneratorDiscountAdjointGamma(double[][] initDiscountFactor, double[][] y, double[][] h, double[][] h2, double[] gamma, Double[][][] pDBar) {
  //    int nbJump = y.length;
  //    int nbPath = y[0].length;
  //    double[] h2gamma;
  //    double[] h2gammaBar;
  //    double[] gammaBar = new double[nbJump];
  //    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
  //      int nbCF = h[loopjump].length;
  //      h2gamma = new double[nbCF];
  //      for (int loopcf = 0; loopcf < nbCF; loopcf++) {
  //        h2gamma[loopcf] = h2[loopjump][loopcf] * gamma[loopjump];
  //      }
  //      // Backward sweep
  //      h2gammaBar = new double[nbCF];
  //      for (int looppath = 0; looppath < nbPath; looppath++) {
  //        for (int loopcf = 0; loopcf < nbCF; loopcf++) {
  //          h2gammaBar[loopcf] += -initDiscountFactor[loopjump][loopcf] * Math.exp(-h[loopjump][loopcf] * y[loopjump][looppath] - h2gamma[loopcf]) * pDBar[looppath][loopjump][loopcf];
  //        }
  //      }
  //      for (int loopcf = 0; loopcf < nbCF; loopcf++) {
  //        gammaBar[loopjump] += h2[loopjump][loopcf] * h2gammaBar[loopcf];
  //      }
  //    }
  //    return gammaBar;
  //  }

}
