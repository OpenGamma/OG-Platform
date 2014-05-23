/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBasketMethod;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.method.SuccessiveLeastSquareLMMDDCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveLeastSquareLMMDDCalibrationObjective;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Triple;

/**
 * Method to computes the present value and sensitivities of physical delivery European swaptions with a Libor Market Model calibrated exactly to SABR prices.
 * The LMM displacements and volatility weights are hard coded.
 * <p> Reference: M. Henrard, Algorithmic differentiation and calibration: optimization, September 2012.
 */
public class SwaptionPhysicalFixedIborSABRLMMLeastSquareMethod {

  /**
   * The SABR method used for European swaptions with physical delivery.
   */
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRSwaptionCalculator PVCSSSC = PresentValueCurveSensitivitySABRSwaptionCalculator.getInstance();
  private static final PresentValueSABRSensitivitySABRSwaptionCalculator PVSSSSC = PresentValueSABRSensitivitySABRSwaptionCalculator.getInstance();
  /**
   * The LMM method used for European swaptions with physical delivery.
   */
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_SWAPTION_LMM = SwaptionPhysicalFixedIborLMMDDMethod.getInstance();
  /**
   * The method used to create the calibration basket.
   */
  private static final SwaptionPhysicalFixedIborBasketMethod METHOD_BASKET = SwaptionPhysicalFixedIborBasketMethod.getInstance();
  /**
   * The matrix algebra used.
   */
  private static final CommonsMatrixAlgebra ALGEBRA = new CommonsMatrixAlgebra();
  /**
   * The moneyness of strikes used in the calibration basket. Difference between the swaption rate and the basket rates.
   */
  private final double[] _strikeMoneyness;
  /**
   * The initial value of the LMM parameters for calibration. The initial parameters are not modified by the calibration but a new copy is created for each calibration.
   */
  private final LiborMarketModelDisplacedDiffusionParameters _parametersInit;

  /**
   * Constructor.
   * @param strikeMoneyness The moneyness of strikes used in the calibration basket. Difference between the swaption rate and the basket rates.
   * @param parametersInit The initial value of the LMM parameters for calibration. The initial parameters are not modified by the calibration but a new copy is created for each calibration.
   */
  public SwaptionPhysicalFixedIborSABRLMMLeastSquareMethod(final double[] strikeMoneyness, final LiborMarketModelDisplacedDiffusionParameters parametersInit) {
    _strikeMoneyness = strikeMoneyness;
    _parametersInit = parametersInit;
  }

  /**
   * The method calibrates a LMM on a set of vanilla swaption priced with SABR. The set of vanilla swaptions is given by the CalibrationType.
   * The original swaption is priced with the calibrated LMM.
   * This should not be used for vanilla swaptions (the price is equal to the SABR price with a longer computation type and some approximation).
   * This is useful for non-standard swaptions like amortized swaptions.
   * @param swaption The swaption.
   * @param sabrData The SABR and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final Currency ccy = swaption.getCurrency();
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    final int nbStrikes = _strikeMoneyness.length;
    final LiborMarketModelDisplacedDiffusionParameters lmmParameters = _parametersInit.copy();
    final SuccessiveLeastSquareLMMDDCalibrationObjective objective = new SuccessiveLeastSquareLMMDDCalibrationObjective(lmmParameters, ccy);
    final SuccessiveLeastSquareLMMDDCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveLeastSquareLMMDDCalibrationEngine<>(objective, nbStrikes);
    final SwaptionPhysicalFixedIbor[] swaptionCalibration = METHOD_BASKET.calibrationBasketFixedLegPeriod(swaption, _strikeMoneyness);
    calibrationEngine.addInstrument(swaptionCalibration, PVSSC);
    calibrationEngine.calibrate(sabrData);
    final LiborMarketModelDisplacedDiffusionProvider lmm = new LiborMarketModelDisplacedDiffusionProvider(multicurves, lmmParameters, ccy);
    final MultipleCurrencyAmount pv = METHOD_SWAPTION_LMM.presentValue(swaption, lmm);
    return pv;
  }

  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final Currency ccy = swaption.getCurrency();
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    final int nbStrikes = _strikeMoneyness.length;
    final LiborMarketModelDisplacedDiffusionParameters lmmParameters = _parametersInit.copy();
    final SuccessiveLeastSquareLMMDDCalibrationObjective objective = new SuccessiveLeastSquareLMMDDCalibrationObjective(lmmParameters, ccy);
    final SuccessiveLeastSquareLMMDDCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveLeastSquareLMMDDCalibrationEngine<>(objective, nbStrikes);
    final SwaptionPhysicalFixedIbor[] swaptionCalibration = METHOD_BASKET.calibrationBasketFixedLegPeriod(swaption, _strikeMoneyness);
    calibrationEngine.addInstrument(swaptionCalibration, PVSSC);
    calibrationEngine.calibrate(sabrData);
    final LiborMarketModelDisplacedDiffusionProvider lmm = new LiborMarketModelDisplacedDiffusionProvider(multicurves, lmmParameters, ccy);

    final int nbCalibrations = swaptionCalibration.length;
    final int nbPeriods = nbCalibrations / nbStrikes;
    final int nbFact = lmmParameters.getNbFactor();
    final List<Integer> instrumentIndex = calibrationEngine.getInstrumentIndex();
    final double[] dPvdPhi = new double[2 * nbPeriods];
    // Implementation note: Derivative of the priced swaptions wrt the calibration parameters (multiplicative factor and additive term)
    // Implementation note: Phi is a vector with the multiplicative factors on the volatility and then the additive terms on the displacements.
    final double[][] dPvdGamma = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaption, lmm);
    final double[] dPvdDis = METHOD_SWAPTION_LMM.presentValueDDSensitivity(swaption, lmm);
    for (int loopperiod = 0; loopperiod < nbPeriods; loopperiod++) {
      for (int loopsub = instrumentIndex.get(loopperiod * nbStrikes); loopsub < instrumentIndex.get((loopperiod + 1) * nbStrikes); loopsub++) {
        for (int loopfact = 0; loopfact < nbFact; loopfact++) {
          dPvdPhi[loopperiod] += dPvdGamma[loopsub][loopfact] * lmmParameters.getVolatility()[loopsub][loopfact];
          dPvdPhi[nbPeriods + loopperiod] += dPvdDis[loopsub];
        }
      }
    }

    final double[][] dPvCaldPhi = new double[nbCalibrations][2 * nbPeriods];
    // Implementation note: Derivative of the calibration swaptions wrt the calibration parameters (multiplicative factor and additive term)
    final double[][][] dPvCaldGamma = new double[nbCalibrations][][];
    for (int loopcal = 0; loopcal < nbCalibrations; loopcal++) {
      dPvCaldGamma[loopcal] = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaptionCalibration[loopcal], lmm);
    }
    final double[][] dPvCaldDis = new double[nbCalibrations][];
    for (int loopcal = 0; loopcal < nbCalibrations; loopcal++) {
      dPvCaldDis[loopcal] = METHOD_SWAPTION_LMM.presentValueDDSensitivity(swaptionCalibration[loopcal], lmm);
    }
    for (int loopcal = 0; loopcal < nbCalibrations; loopcal++) {
      for (int loopperiod = 0; loopperiod < nbPeriods; loopperiod++) {
        for (int loopsub = instrumentIndex.get(loopperiod * nbStrikes); loopsub < instrumentIndex.get((loopperiod + 1) * nbStrikes); loopsub++) {
          for (int loopfact = 0; loopfact < nbFact; loopfact++) {
            dPvCaldPhi[loopcal][loopperiod] += dPvCaldGamma[loopcal][loopsub][loopfact] * lmmParameters.getVolatility()[loopsub][loopfact];
            dPvCaldPhi[loopcal][nbPeriods + loopperiod] += dPvCaldDis[loopcal][loopsub];
          }
        }
      }
    }

    final double[][] dPvCaldTheta = new double[nbCalibrations][3 * nbPeriods];
    // Implementation note: Derivative of the calibration swaptions wrt the SABR parameters as a unique array.
    // Implementation note: Theta is vector with first the Alpha, the the Rho and finally the Nu.
    for (int loopperiod = 0; loopperiod < nbPeriods; loopperiod++) {
      for (int loopstrike = 0; loopstrike < nbStrikes; loopstrike++) {
        final PresentValueSABRSensitivityDataBundle dPvCaldSABR = swaptionCalibration[loopperiod * nbStrikes + loopstrike].accept(PVSSSSC, sabrData);
        final Set<DoublesPair> keySet = dPvCaldSABR.getAlpha().getMap().keySet();
        final DoublesPair[] keys = keySet.toArray(new DoublesPair[keySet.size()]);
        dPvCaldTheta[loopperiod * nbStrikes + loopstrike][loopperiod] += dPvCaldSABR.getAlpha().getMap().get(keys[0]);
        dPvCaldTheta[loopperiod * nbStrikes + loopstrike][nbPeriods + loopperiod] = dPvCaldSABR.getRho().getMap().get(keys[0]);
        dPvCaldTheta[loopperiod * nbStrikes + loopstrike][2 * nbPeriods + loopperiod] = dPvCaldSABR.getNu().getMap().get(keys[0]);
      }
    }

    final double[][] dfdTheta = new double[2 * nbPeriods][3 * nbPeriods];
    // Implementation note: Derivative of f wrt the SABR parameters.
    for (int loopp = 0; loopp < 2 * nbPeriods; loopp++) {
      for (int loops = 0; loops < 3 * nbPeriods; loops++) {
        for (int loopcal = 0; loopcal < nbCalibrations; loopcal++) {
          dfdTheta[loopp][loops] += -2 * dPvCaldPhi[loopcal][loopp] * dPvCaldTheta[loopcal][loops];
        }
      }
    }
    final double[][] dfdPhi = new double[2 * nbPeriods][2 * nbPeriods];
    // Implementation note: Derivative of f wrt the calibration parameters. This is an approximation: the second order derivative part are ignored.
    for (int loopp1 = 0; loopp1 < 2 * nbPeriods; loopp1++) {
      for (int loopp2 = 0; loopp2 < 2 * nbPeriods; loopp2++) {
        for (int loopcal = 0; loopcal < nbCalibrations; loopcal++) {
          dfdPhi[loopp1][loopp2] += 2 * dPvCaldPhi[loopcal][loopp1] * dPvCaldPhi[loopcal][loopp2];
        }
      }
    }

    final DoubleMatrix2D dfdThetaMat = new DoubleMatrix2D(dfdTheta);
    final DoubleMatrix2D dfdPhiMat = new DoubleMatrix2D(dfdPhi);
    final DoubleMatrix2D dPhidThetaMat = (DoubleMatrix2D) ALGEBRA.scale(ALGEBRA.multiply(ALGEBRA.getInverse(dfdPhiMat), dfdThetaMat), -1.0);
    final DoubleMatrix1D dPvdPhiMat = new DoubleMatrix1D(dPvdPhi);
    final DoubleMatrix2D dPvdThetaMat = ALGEBRA.getTranspose(ALGEBRA.multiply(ALGEBRA.getTranspose(dPhidThetaMat), dPvdPhiMat));
    final double[] dPvdTheta = dPvdThetaMat.getData()[0];

    // Storage in PresentValueSABRSensitivityDataBundle
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    for (int loopp = 0; loopp < nbPeriods; loopp++) {
      final DoublesPair expiryMaturity = DoublesPair.of(swaptionCalibration[loopp * nbStrikes].getTimeToExpiry(), swaptionCalibration[loopp * nbStrikes].getMaturityTime());
      sensi.addAlpha(expiryMaturity, dPvdTheta[loopp]);
      sensi.addRho(expiryMaturity, dPvdTheta[nbPeriods + loopp]);
      sensi.addNu(expiryMaturity, dPvdTheta[2 * nbPeriods + loopp]);
    }
    return sensi;
  }

  public Triple<MultipleCurrencyAmount, PresentValueSABRSensitivityDataBundle, MultipleCurrencyMulticurveSensitivity> presentValueAndSensitivity(final SwaptionPhysicalFixedIbor swaption,
      final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final Currency ccy = swaption.getCurrency();
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    final int nbStrikes = _strikeMoneyness.length;
    final LiborMarketModelDisplacedDiffusionParameters lmmParameters = _parametersInit.copy();
    final SuccessiveLeastSquareLMMDDCalibrationObjective objective = new SuccessiveLeastSquareLMMDDCalibrationObjective(lmmParameters, ccy);
    final SuccessiveLeastSquareLMMDDCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveLeastSquareLMMDDCalibrationEngine<>(objective, nbStrikes);
    final SwaptionPhysicalFixedIbor[] swaptionCalibration = METHOD_BASKET.calibrationBasketFixedLegPeriod(swaption, _strikeMoneyness);
    calibrationEngine.addInstrument(swaptionCalibration, PVSSC);
    calibrationEngine.calibrate(sabrData);
    final LiborMarketModelDisplacedDiffusionProvider lmm = new LiborMarketModelDisplacedDiffusionProvider(multicurves, lmmParameters, ccy);

    // 1. PV

    final MultipleCurrencyAmount pv = METHOD_SWAPTION_LMM.presentValue(swaption, lmm);

    final int nbCalibrations = swaptionCalibration.length;
    final int nbPeriods = nbCalibrations / nbStrikes;
    final int nbFact = lmmParameters.getNbFactor();
    final List<Integer> instrumentIndex = calibrationEngine.getInstrumentIndex();

    // 2. SABR sensitivities

    final double[] dPvdPhi = new double[2 * nbPeriods];
    // Implementation note: Derivative of the priced swaptions wrt the calibration parameters (multiplicative factor and additive term)
    final double[][] dPvdGamma = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaption, lmm);
    final double[] dPvdDis = METHOD_SWAPTION_LMM.presentValueDDSensitivity(swaption, lmm);
    for (int loopperiod = 0; loopperiod < nbPeriods; loopperiod++) {
      for (int loopsub = instrumentIndex.get(loopperiod * nbStrikes); loopsub < instrumentIndex.get((loopperiod + 1) * nbStrikes); loopsub++) {
        for (int loopfact = 0; loopfact < nbFact; loopfact++) {
          dPvdPhi[loopperiod] += dPvdGamma[loopsub][loopfact] * lmmParameters.getVolatility()[loopsub][loopfact];
          dPvdPhi[nbPeriods + loopperiod] += dPvdDis[loopsub];
        }
      }
    }

    final double[][] dPvCaldPhi = new double[nbCalibrations][2 * nbPeriods];
    // Implementation note: Derivative of the calibration swaptions wrt the calibration parameters (multiplicative factor and additive term)
    final double[][][] dPvCaldGamma = new double[nbCalibrations][][];
    for (int loopcal = 0; loopcal < nbCalibrations; loopcal++) {
      dPvCaldGamma[loopcal] = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaptionCalibration[loopcal], lmm);
    }
    final double[][] dPvCaldDis = new double[nbCalibrations][];
    for (int loopcal = 0; loopcal < nbCalibrations; loopcal++) {
      dPvCaldDis[loopcal] = METHOD_SWAPTION_LMM.presentValueDDSensitivity(swaptionCalibration[loopcal], lmm);
    }
    for (int loopcal = 0; loopcal < nbCalibrations; loopcal++) {
      for (int loopperiod = 0; loopperiod < nbPeriods; loopperiod++) {
        for (int loopsub = instrumentIndex.get(loopperiod * nbStrikes); loopsub < instrumentIndex.get((loopperiod + 1) * nbStrikes); loopsub++) {
          for (int loopfact = 0; loopfact < nbFact; loopfact++) {
            dPvCaldPhi[loopcal][loopperiod] += dPvCaldGamma[loopcal][loopsub][loopfact] * lmmParameters.getVolatility()[loopsub][loopfact];
            dPvCaldPhi[loopcal][nbPeriods + loopperiod] += dPvCaldDis[loopcal][loopsub];
          }
        }
      }
    }

    final double[][] dPvCaldTheta = new double[nbCalibrations][3 * nbPeriods];
    // Implementation note: Derivative of the calibration swaptions wrt the SABR parameters as a unique array.
    for (int loopperiod = 0; loopperiod < nbPeriods; loopperiod++) {
      for (int loopstrike = 0; loopstrike < nbStrikes; loopstrike++) {
        final PresentValueSABRSensitivityDataBundle dPvCaldSABR = swaptionCalibration[loopperiod * nbStrikes + loopstrike].accept(PVSSSSC, sabrData);
        final Set<DoublesPair> keySet = dPvCaldSABR.getAlpha().getMap().keySet();
        final DoublesPair[] keys = keySet.toArray(new DoublesPair[keySet.size()]);
        dPvCaldTheta[loopperiod * nbStrikes + loopstrike][loopperiod] += dPvCaldSABR.getAlpha().getMap().get(keys[0]);
        dPvCaldTheta[loopperiod * nbStrikes + loopstrike][nbPeriods + loopperiod] = dPvCaldSABR.getRho().getMap().get(keys[0]);
        dPvCaldTheta[loopperiod * nbStrikes + loopstrike][2 * nbPeriods + loopperiod] = dPvCaldSABR.getNu().getMap().get(keys[0]);
      }
    }

    final double[][] dfdTheta = new double[2 * nbPeriods][3 * nbPeriods];
    // Implementation note: Derivative of f wrt the SABR parameters.
    for (int loopp = 0; loopp < 2 * nbPeriods; loopp++) {
      for (int loops = 0; loops < 3 * nbPeriods; loops++) {
        for (int loopcal = 0; loopcal < nbCalibrations; loopcal++) {
          dfdTheta[loopp][loops] += -2 * dPvCaldPhi[loopcal][loopp] * dPvCaldTheta[loopcal][loops];
        }
      }
    }
    final double[][] dfdPhi = new double[2 * nbPeriods][2 * nbPeriods];
    // Implementation note: Derivative of f wrt the calibration parameters. This is an approximation: the second order derivative part are ignored.
    for (int loopp1 = 0; loopp1 < 2 * nbPeriods; loopp1++) {
      for (int loopp2 = 0; loopp2 < 2 * nbPeriods; loopp2++) {
        for (int loopcal = 0; loopcal < nbCalibrations; loopcal++) {
          dfdPhi[loopp1][loopp2] += 2 * dPvCaldPhi[loopcal][loopp1] * dPvCaldPhi[loopcal][loopp2];
        }
      }
    }

    final DoubleMatrix2D dfdThetaMat = new DoubleMatrix2D(dfdTheta);
    final DoubleMatrix2D dfdPhiMat = new DoubleMatrix2D(dfdPhi);
    final DoubleMatrix2D dfdPhiInvMat = ALGEBRA.getInverse(dfdPhiMat);
    final DoubleMatrix2D dPhidThetaMat = (DoubleMatrix2D) ALGEBRA.scale(ALGEBRA.multiply(dfdPhiInvMat, dfdThetaMat), -1.0);
    final DoubleMatrix1D dPvdPhiMat = new DoubleMatrix1D(dPvdPhi);
    final DoubleMatrix2D dPvdThetaMat = ALGEBRA.getTranspose(ALGEBRA.multiply(ALGEBRA.getTranspose(dPhidThetaMat), dPvdPhiMat));
    final double[] dPvdTheta = dPvdThetaMat.getData()[0];

    // Storage in PresentValueSABRSensitivityDataBundle
    final PresentValueSABRSensitivityDataBundle sensiSABR = new PresentValueSABRSensitivityDataBundle();
    for (int loopp = 0; loopp < nbPeriods; loopp++) {
      final DoublesPair expiryMaturity = DoublesPair.of(swaptionCalibration[loopp * nbStrikes].getTimeToExpiry(), swaptionCalibration[loopp * nbStrikes].getMaturityTime());
      sensiSABR.addAlpha(expiryMaturity, dPvdTheta[loopp]);
      sensiSABR.addRho(expiryMaturity, dPvdTheta[nbPeriods + loopp]);
      sensiSABR.addNu(expiryMaturity, dPvdTheta[2 * nbPeriods + loopp]);
    }

    // 3. Curve sensitivities

    final MultipleCurrencyMulticurveSensitivity[] dPvCalBasedC = new MultipleCurrencyMulticurveSensitivity[nbCalibrations];
    final MultipleCurrencyMulticurveSensitivity[] dPvCalLmmdC = new MultipleCurrencyMulticurveSensitivity[nbCalibrations];
    final MultipleCurrencyMulticurveSensitivity[] dPvCalDiffdC = new MultipleCurrencyMulticurveSensitivity[nbCalibrations];
    for (int loopcal = 0; loopcal < nbCalibrations; loopcal++) {
      dPvCalBasedC[loopcal] = swaptionCalibration[loopcal].accept(PVCSSSC, sabrData);
      dPvCalLmmdC[loopcal] = METHOD_SWAPTION_LMM.presentValueCurveSensitivity(swaptionCalibration[loopcal], lmm);
      dPvCalDiffdC[loopcal] = dPvCalBasedC[loopcal].plus(dPvCalLmmdC[loopcal].multipliedBy(-1.0)).cleaned();
    }
    final MultipleCurrencyMulticurveSensitivity[] dfdC = new MultipleCurrencyMulticurveSensitivity[2 * nbPeriods];
    // Implementation note: Derivative of f wrt the curves. This is an approximation: the second order derivative part are ignored.
    for (int loopp = 0; loopp < 2 * nbPeriods; loopp++) {
      dfdC[loopp] = new MultipleCurrencyMulticurveSensitivity();
      for (int loopcal = 0; loopcal < nbCalibrations; loopcal++) {
        dfdC[loopp] = dfdC[loopp].plus(dPvCalDiffdC[loopcal].multipliedBy(-2 * dPvCaldPhi[loopcal][loopp])).cleaned();
      }
    }
    final MultipleCurrencyMulticurveSensitivity[] dPhidC = new MultipleCurrencyMulticurveSensitivity[2 * nbPeriods];
    for (int loopp1 = 0; loopp1 < 2 * nbPeriods; loopp1++) {
      dPhidC[loopp1] = new MultipleCurrencyMulticurveSensitivity();
      for (int loopp2 = 0; loopp2 < 2 * nbPeriods; loopp2++) {
        dPhidC[loopp1] = dPhidC[loopp1].plus(dfdC[loopp2].multipliedBy(-dfdPhiInvMat.getEntry(loopp1, loopp2))).cleaned();
      }
    }
    MultipleCurrencyMulticurveSensitivity dPvdC = METHOD_SWAPTION_LMM.presentValueCurveSensitivity(swaption, lmm);
    for (int loopp = 0; loopp < 2 * nbPeriods; loopp++) {
      dPvdC = dPvdC.plus(dPhidC[loopp].multipliedBy(dPvdPhi[loopp])).cleaned();
    }
    return new Triple<>(pv, sensiSABR, dPvdC);
  }

}
