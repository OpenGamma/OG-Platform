/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.montecarlo;

import java.util.Arrays;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.random.RandomNumberGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Monte Carlo pricing method in the Libor Market Model with Displaced Diffusion.
 */
public class LiborMarketModelMonteCarloMethod extends MonteCarloMethod {

  /**
   * The maximum length of a jump in the path generation.
   */
  private final double _maxJump;

  /**
   * The decision schedule calculator (calculate the exercise dates, the cash flow dates and the reference amounts).
   */
  private static final DecisionScheduleCalculator DC = DecisionScheduleCalculator.getInstance();
  /**
   * The calculator from discount factors (calculate the price from simulated discount factors and the reference amounts).
   */
  private static final MonteCarloIborRateCalculator MCC = MonteCarloIborRateCalculator.getInstance();
  /**
   * The number of paths in one block.
   */
  private static final int BLOCK_SIZE = 1000;
  /**
   * The default maximum length of a jump in the path generation.
   */
  private static final double MAX_JUMP_DEFAULT = 1.0;

  /**
   * Constructor.
   * @param numberGenerator The random number generator. Generate Normally distributed numbers.
   * @param nbPath The number of paths.
   */
  public LiborMarketModelMonteCarloMethod(RandomNumberGenerator numberGenerator, int nbPath) {
    super(numberGenerator, nbPath);
    _maxJump = MAX_JUMP_DEFAULT;
  }

  /**
   * Constructor.
   * @param numberGenerator The random number generator. Generate Normally distributed numbers.
   * @param nbPath The number of paths.
   * @param maxJump The maximum length of a jump in the path generation.
   */
  public LiborMarketModelMonteCarloMethod(RandomNumberGenerator numberGenerator, int nbPath, double maxJump) {
    super(numberGenerator, nbPath);
    _maxJump = maxJump;
  }

  public CurrencyAmount presentValue(final InstrumentDerivative instrument, Currency ccy, YieldAndDiscountCurve dsc, final LiborMarketModelDisplacedDiffusionDataBundle lmmData) {
    // The numeraire is the last time in the LMM description.
    DecisionSchedule decision = DC.visit(instrument, lmmData);
    int[][] impactIndex = index(decision.getImpactTime(), lmmData.getLmmParameter());

    int nbPeriodLMM = lmmData.getLmmParameter().getNbPeriod();
    double[] initL = new double[nbPeriodLMM];
    double[] deltaLMM = lmmData.getLmmParameter().getAccrualFactor();
    double[] dfL = new double[nbPeriodLMM + 1];
    for (int loopper = 0; loopper < nbPeriodLMM + 1; loopper++) {
      dfL[loopper] = dsc.getDiscountFactor(lmmData.getLmmParameter().getIborTime()[loopper]);
    }
    for (int loopper = 0; loopper < nbPeriodLMM; loopper++) {
      initL[loopper] = (dfL[loopper] / dfL[loopper + 1] - 1.0) / deltaLMM[loopper];
    }

    int nbBlock = (int) Math.round(Math.ceil(getNbPath() / ((double) BLOCK_SIZE)));
    int[] nbPath2 = new int[nbBlock];
    for (int i = 0; i < nbBlock - 1; i++) {
      nbPath2[i] = BLOCK_SIZE;
    }
    nbPath2[nbBlock - 1] = getNbPath() - (nbBlock - 1) * BLOCK_SIZE;

    double price = 0.0;
    for (int loopblock = 0; loopblock < nbBlock; loopblock++) {
      double[][] initLPath = new double[nbPeriodLMM][nbPath2[loopblock]];
      for (int loopper = 0; loopper < nbPeriodLMM; loopper++) {
        for (int looppath = 0; looppath < nbPath2[loopblock]; looppath++) {
          initLPath[loopper][looppath] = initL[loopper];
        }
      }
      double[][][] pathIbor = pathgeneratorlibor(decision.getDecisionTime(), initLPath, lmmData.getLmmParameter());
      price += MCC.visit(instrument, new MonteCarloIborRateDataBundle(pathIbor, deltaLMM, decision.getImpactAmount(), impactIndex));
    }
    price *= dsc.getDiscountFactor(lmmData.getLmmParameter().getIborTime()[lmmData.getLmmParameter().getIborTime().length - 1]) / getNbPath();
    return CurrencyAmount.of(ccy, price);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    return null;
  }

  private int[][] index(final double[][] time, final LiborMarketModelDisplacedDiffusionParameters lmm) {
    int[][] index = new int[time.length][];
    for (int loop1 = 0; loop1 < time.length; loop1++) {
      index[loop1] = new int[time[loop1].length];
      for (int loop2 = 0; loop2 < time[loop1].length; loop2++) {
        index[loop1][loop2] = lmm.getTimeIndex(time[loop1][loop2]);
      }
    }
    return index;
  }

  /**
   * Create one step in the LMM diffusion. The step is done through several jump times. The diffusion is approximated with a predictor-corrector approach.
   * @param jumpTime The jump times.
   * @param initIbor Rate at the start of the period. Size: nbPeriodLMM x nbPath.
   * @return The Ibor rates at the end of the jump period. Size: nbPeriodLMM x nbPath.
   */
  private double[][] stepPC(double[] jumpTime, double[][] initIbor, final LiborMarketModelDisplacedDiffusionParameters lmm) {
    double amr = lmm.getMeanReversion();
    double[] iborTime = lmm.getIborTime();
    double[] almm = lmm.getDisplacement();
    double[] deltalmm = lmm.getAccrualFactor();
    DoubleMatrix2D gammaLMM = new DoubleMatrix2D(lmm.getVolatility());
    MatrixAlgebra algebra = new CommonsMatrixAlgebra();
    DoubleMatrix2D s = (DoubleMatrix2D) algebra.multiply(gammaLMM, algebra.getTranspose(gammaLMM));
    int nbJump = jumpTime.length - 1;
    int nbPath = initIbor[0].length;
    int nbPeriodLMM = lmm.getNbPeriod();
    int nbFactorLMM = lmm.getNbFactor();
    double[] dt = new double[nbJump];
    double[] alpha = new double[nbJump];
    double[] alpha2 = new double[nbJump];
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      dt[loopjump] = jumpTime[loopjump + 1] - jumpTime[loopjump];
      alpha[loopjump] = Math.exp(amr * jumpTime[loopjump + 1]);
      alpha2[loopjump] = alpha[loopjump] * alpha[loopjump];
    }

    double[][] f = initIbor;
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      double sqrtDt = Math.sqrt(dt[loopjump]);
      int index = Arrays.binarySearch(iborTime, jumpTime[loopjump + 1] - lmm.getTimeTolerance());
      index = -index - 1; // The index from which the rate should be evolved.
      int nI = nbPeriodLMM - index;
      double[] dI = new double[nI];
      for (int loopn = 0; loopn < nI; loopn++) {
        dI[loopn] = 1.0 / deltalmm[index + loopn];
      }
      double[][] salpha2Array = new double[nI][nI];
      for (int loopn1 = 0; loopn1 < nI; loopn1++) {
        for (int loopn2 = 0; loopn2 < nI; loopn2++) {
          salpha2Array[loopn1][loopn2] = s.getEntry(index + loopn1, index + loopn2) * alpha2[loopjump];
        }
      }
      DoubleMatrix2D salpha2 = new DoubleMatrix2D(salpha2Array);
      // Random seed
      double[][] dw = getNormalArray(nbFactorLMM, nbPath);
      // Common figures
      double[] dr1 = new double[nI];
      for (int loopn = 0; loopn < nI; loopn++) {
        dr1[loopn] = -salpha2.getEntry(loopn, loopn) * dt[loopjump] / 2.0;
      }
      double[][] cc = new double[nI][nbPath];
      for (int loopn = 0; loopn < nI; loopn++) {
        for (int looppath = 0; looppath < nbPath; looppath++) {
          for (int loopfact = 0; loopfact < nbFactorLMM; loopfact++) {
            cc[loopn][looppath] += gammaLMM.getEntry(index + loopn, loopfact) * dw[loopfact][looppath] * sqrtDt * alpha[loopjump];
          }
          cc[loopn][looppath] += dr1[loopn];
        }
      }
      // Unique step: predictor and corrector
      double[][] mP = new double[nI][nbPath];
      double[][] mC = new double[nI][nbPath];
      double[][] coefP = new double[nbPath][nI - 1];
      double[][] coefC = new double[nI][nbPath];
      for (int looppath = 0; looppath < nbPath; looppath++) {
        for (int loopn = 0; loopn < nI - 1; loopn++) {
          coefP[looppath][loopn] = (f[index + loopn + 1][looppath] + almm[index + loopn + 1]) / (f[index + loopn + 1][looppath] + dI[loopn + 1]);
        }
      }
      for (int loopdrift = nI - 1; loopdrift >= 0; loopdrift--) {
        if (loopdrift < nI - 1) {
          for (int looppath = 0; looppath < nbPath; looppath++) {
            coefC[loopdrift + 1][looppath] = (f[index + loopdrift + 1][looppath] + almm[index + loopdrift + 1]) / (f[index + loopdrift + 1][looppath] + dI[loopdrift + 1]);
            for (int loop = loopdrift + 1; loop < nI; loop++) {
              mP[loopdrift][looppath] += salpha2.getEntry(loop, loopdrift) * coefP[looppath][loop - 1];
              mC[loopdrift][looppath] += salpha2.getEntry(loop, loopdrift) * coefC[loop][looppath];
            }
          }
          for (int looppath = 0; looppath < nbPath; looppath++) {
            f[loopdrift + index][looppath] = (f[loopdrift + index][looppath] + almm[index + loopdrift])
                * Math.exp(-(mP[loopdrift][looppath] + mC[loopdrift][looppath]) * dt[loopjump] / 2.0 + cc[loopdrift][looppath]) - almm[index + loopdrift];
          }
        } else {
          for (int looppath = 0; looppath < nbPath; looppath++) {
            f[loopdrift + index][looppath] = (f[loopdrift + index][looppath] + almm[index + loopdrift]) * Math.exp(cc[loopdrift][looppath]) - almm[index + loopdrift];
          }
        }
      }
    }
    return f;
  }

  /**
   * 
   * @param jumpTime The time of the mandatory jumps.
   * @param initIbor The Ibor rates at the start. nbPeriodLMM x nbPath
   * @param lmm The LMM parameters.
   * @return The paths. Size: nbJump x nbPeriodLMM x nbPath
   */
  private double[][][] pathgeneratorlibor(double[] jumpTime, final double[][] initIbor, final LiborMarketModelDisplacedDiffusionParameters lmm) {
    int nbPeriod = initIbor.length;
    int nbPath = initIbor[0].length;
    int nbJump = jumpTime.length;
    double[][] initTmp = new double[nbPeriod][nbPath];
    for (int loop1 = 0; loop1 < nbPeriod; loop1++) {
      System.arraycopy(initIbor[loop1], 0, initTmp[loop1], 0, nbPath);
    }
    double[] jumpTimeA = new double[nbJump + 1];
    jumpTimeA[0] = 0;
    System.arraycopy(jumpTime, 0, jumpTimeA, 1, nbJump);
    double[][][] result = new double[nbJump][nbPeriod][nbPath];
    // TODO: add intermediary jump dates if necessary
    for (int loopjump = 0; loopjump < nbJump; loopjump++) {
      // Intermediary jumps
      double[] jumpIn;
      if (jumpTimeA[loopjump + 1] - jumpTimeA[loopjump] < _maxJump) {
        jumpIn = new double[] {jumpTimeA[loopjump], jumpTimeA[loopjump + 1]};
      } else {
        double jump = jumpTimeA[loopjump + 1] - jumpTimeA[loopjump];
        int nbJumpIn = (int) Math.ceil(jump / _maxJump);
        jumpIn = new double[nbJumpIn + 1];
        jumpIn[0] = jumpTimeA[loopjump];
        for (int loopJumpIn = 1; loopJumpIn <= nbJumpIn; loopJumpIn++) {
          jumpIn[loopJumpIn] = jumpTimeA[loopjump] + loopJumpIn * jump / nbJumpIn;
        }
      }
      initTmp = stepPC(jumpIn, initTmp, lmm);
      for (int loop1 = 0; loop1 < nbPeriod; loop1++) {
        System.arraycopy(initTmp[loop1], 0, result[loopjump][loop1], 0, nbPath);
      }
    }
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

}
