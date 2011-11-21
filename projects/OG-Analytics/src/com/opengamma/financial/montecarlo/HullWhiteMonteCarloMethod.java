/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.montecarlo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.InstrumentDerivative;
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
  public HullWhiteMonteCarloMethod(RandomNumberGenerator numberGenerator, int nbPath) {
    super(numberGenerator, nbPath);
  }

  /**
   * Computes the present value in the Hull-White one factor model by Monte-Carlo.
   * Implementation note: The total number of paths is divided in blocks of maximum size BLOCK_SIZE=1000. The Monte Carlo is run on each block and the average of each
   * block price is the total price. 
   * @param instrument The swaption.
   * @param ccy The currency
   * @param dscName The discounting curve name.
   * @param hwData The Hull-White data (curves and Hull-White parameters).
   * @return The present value.
   */
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, Currency ccy, final String dscName, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    // TODO: remove currency and dsc curve name (should be available from the instrument)
    YieldAndDiscountCurve dsc = hwData.getCurve(dscName);
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
      for (int looppath = 0; looppath < nbPath2[loopblock]; looppath++) {
        for (int i = 0; i < nbJump; i++) {
          for (int j = 0; j < nbJump; j++) {
            y[i][looppath] += x[j][looppath] * covCD[i][j];
          }
        }
      }
      Double[][][] pD = pathGeneratorDiscount(pDI, y, h, h2, gamma);
      pv += MCC.visit(instrument, new MonteCarloDiscountFactorDataBundle(pD, impactAmount)) * nbPath2[loopblock];
    }
    pv *= pDN / getNbPath(); // Multiply by the numeraire.
    return CurrencyAmount.of(ccy, pv);
  }

  /**
   * Computes the present value curve sensitivity in the Hull-White one factor model by Monte-Carlo. The sensitivity is computed by Adjoint Algorithmic Differentiation. 
   * Implementation note: The total number of paths is divided in blocks of maximum size BLOCK_SIZE=1000. The Monte Carlo is run on each block and the average of each
   * block price is the total price. 
   * @param instrument The swaption.
   * @param dscName The discounting curve name.
   * @param hwData The Hull-White data (curves and Hull-White parameters).
   * @return The curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final InstrumentDerivative instrument, final String dscName, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    YieldAndDiscountCurve dsc = hwData.getCurve(dscName);
    // TODO: remove dsc curve name
    // Forward sweep
    DecisionScheduleDerivative decision = DDC.visit(instrument, hwData);
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
    double[] pvBlock = new double[nbBlock];
    // Backward sweep (init)
    double pvBar = 1.0;
    double[] pvBlockBar = new double[nbBlock];
    for (int loopblock = 0; loopblock < nbBlock; loopblock++) {
      pvBlockBar[loopblock] = pDN / getNbPath() * pvBar;
    }
    double[][] impactAmountBar = new double[nbJump][];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      impactAmountBar[loopjump] = new double[impactAmount[loopjump].length];
    }
    // Forward sweep (end) and backward sweep (main)
    double[][] pDIBar = new double[nbJump][];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      pDIBar[loopjump] = new double[impactAmount[loopjump].length];
    }
    for (int loopblock = 0; loopblock < nbBlock; loopblock++) {
      double[][] x = getNormalArray(nbJump, nbPath2[loopblock]);
      double[][] y = new double[nbJump][nbPath2[loopblock]]; // jump/path
      for (int looppath = 0; looppath < nbPath2[loopblock]; looppath++) {
        for (int i = 0; i < nbJump; i++) {
          for (int j = 0; j < nbJump; j++) {
            y[i][looppath] += x[j][looppath] * covCD[i][j];
          }
        }
      }
      Double[][][] pD = pathGeneratorDiscount(pDI, y, h, h2, gamma);
      MonteCarloDiscountFactorDerivativeDataBundle mcdDB = new MonteCarloDiscountFactorDerivativeDataBundle(pD, impactAmount);
      pvBlock[loopblock] = MCDC.visit(instrument, mcdDB) * nbPath2[loopblock];
      pv += pvBlock[loopblock];
      // Backward sweep (in block loop)
      for (int loopjump = 0; loopjump < nbJump; loopjump++) {
        for (int loopimp = 0; loopimp < impactAmount[loopjump].length; loopimp++) {
          impactAmountBar[loopjump][loopimp] += mcdDB.getImpactAmountDerivative()[loopjump][loopimp] * nbPath2[loopblock] * pvBlockBar[loopblock];
        }
      }
      Double[][][] pDBar = new Double[nbPath2[loopblock]][nbJump][];
      for (int looppath = 0; looppath < nbPath2[loopblock]; looppath++) {
        for (int loopjump = 0; loopjump < nbJump; loopjump++) {
          pDBar[looppath][loopjump] = new Double[impactAmount[loopjump].length];
          for (int loopimp = 0; loopimp < impactAmount[loopjump].length; loopimp++) {
            pDBar[looppath][loopjump][loopimp] = mcdDB.getPathDiscountingFactorDerivative()[looppath][loopjump][loopimp] * nbPath2[loopblock] * pvBlockBar[loopblock];
          }
        }
      }
      double[][] pDIBarTemp = pathGeneratorDiscountAdjointIDF(pDI, y, h, h2, gamma, pDBar);
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
        pDNBar += -dsc.getDiscountFactor(impactTime[loopjump][loopimp]) / (pDN * pDN) * pDIBar[loopjump][loopimp];
      }
    }
    final Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(numeraireTime, -numeraireTime * pDN * pDNBar));
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      for (int loopimp = 0; loopimp < impactTime[loopjump].length; loopimp++) {
        listDiscounting.add(new DoublesPair(impactTime[loopjump][loopimp], -impactTime[loopjump][loopimp] * pDI[loopjump][loopimp] * pDIBar[loopjump][loopimp]));
      }
    }
    resultMap.put(dscName, listDiscounting);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    // Adding sensitivity due to cash flow equivalent sensitivity to curves.
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      Map<Double, InterestRateCurveSensitivity> impactAmountDerivative = decision.getImpactAmountDerivative().get(loopjump);
      for (int loopimp = 0; loopimp < impactTime[loopjump].length; loopimp++) {
        InterestRateCurveSensitivity sensiCfe = impactAmountDerivative.get(impactTime[loopjump][loopimp]);
        if (!(sensiCfe == null)) { // There is some sensitivity to that cfe.
          result = result.add(sensiCfe.multiply(impactAmountBar[loopjump][loopimp]));
        }
      }
    }
    result = result.clean();
    return result;
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
   * @return The discount factor paths (path/jump/cf).
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
  private double[][] pathGeneratorDiscountAdjointIDF(double[][] initDiscountFactor, double[][] y, double[][] h, double[][] h2, double[] gamma, Double[][][] pDBar) {
    int nbJump = y.length;
    int nbPath = y[0].length;
    double[] h2gamma;
    double[][] initDiscountFactorBar = new double[nbJump][];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      int nbCF = h[loopjump].length;
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

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    // TODO: Remove currency and dsc curve name (should be available from the instrument)
    return null;
  }

}
