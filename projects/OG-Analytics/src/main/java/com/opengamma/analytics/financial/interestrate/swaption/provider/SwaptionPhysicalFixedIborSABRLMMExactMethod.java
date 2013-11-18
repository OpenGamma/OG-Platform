/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import java.util.ArrayList;
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
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderLMMDDCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderLMMDDCalibrationObjective;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to computes the present value and sensitivities of physical delivery European swaptions with a Libor Market Model calibrated exactly to SABR prices.
 * The LMM displacements and volatility weights are hard coded.
 */
public class SwaptionPhysicalFixedIborSABRLMMExactMethod {

  /**
   * The default mean reversion parameter for the model.
   */
  private static final double DEFAULT_MEAN_REVERSION = 0.01;
  /**
   * The default common displacement parameter for all rates in the model.
   */
  private static final double DEFAULT_DISPLACEMENT = 0.10;
  /**
   * The default parameter to create the weight on the different model volatilities.
   */
  private static final double DEFAULT_ANGLE = Math.PI / 2;
  // TODO: Create a way to pass the model parameters to the method.
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
   * The method calibrates a LMM on a set of vanilla swaption priced with SABR. The set of vanilla swaptions is given by the CalibrationType.
   * The original swaption is priced with the calibrated LMM.
   * This should not be used for vanilla swaptions (the price is equal to the SABR price with a longer computation type and some approximation).
   * This is useful for non-standard swaptions like amortized swaptions.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final Currency ccy = swaption.getCurrency();
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    //TODO: Create a way to chose the LMM base parameters (displacement, mean reversion, volatility).
    final LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionParameters.from(swaption, DEFAULT_DISPLACEMENT, DEFAULT_MEAN_REVERSION, new VolatilityLMMAngle(
        DEFAULT_ANGLE, DEFAULT_DISPLACEMENT));
    final SuccessiveRootFinderLMMDDCalibrationObjective objective = new SuccessiveRootFinderLMMDDCalibrationObjective(lmmParameters, ccy);
    final SuccessiveRootFinderLMMDDCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderLMMDDCalibrationEngine<>(objective);
    final SwaptionPhysicalFixedIbor[] swaptionCalibration = METHOD_BASKET.calibrationBasketFixedLegPeriod(swaption);
    calibrationEngine.addInstrument(swaptionCalibration, PVSSC);
    calibrationEngine.calibrate(sabrData);
    final LiborMarketModelDisplacedDiffusionProviderInterface lmm = new LiborMarketModelDisplacedDiffusionProvider(multicurves, lmmParameters, ccy);
    return MultipleCurrencyAmount.of(swaption.getCurrency(), METHOD_SWAPTION_LMM.presentValue(swaption, lmm).getAmount(ccy));
  }

  /**
   * The method calibrates a LMM on a set of vanilla swaption priced with SABR. The set of vanilla swaptions is given by the CalibrationType.
   * The SABR parameters sensitivities of the original swaption are calculated with LMM re-calibration.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The present value SABR parameters sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final Currency ccy = swaption.getCurrency();
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    //TODO: Create a way to chose the LMM base parameters (displacement, mean reversion, volatility).
    final LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionParameters.from(swaption, DEFAULT_DISPLACEMENT, DEFAULT_MEAN_REVERSION, new VolatilityLMMAngle(
        DEFAULT_ANGLE, DEFAULT_DISPLACEMENT));
    final SuccessiveRootFinderLMMDDCalibrationObjective objective = new SuccessiveRootFinderLMMDDCalibrationObjective(lmmParameters, ccy);
    final SuccessiveRootFinderLMMDDCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderLMMDDCalibrationEngine<>(objective);
    final SwaptionPhysicalFixedIbor[] swaptionCalibration = METHOD_BASKET.calibrationBasketFixedLegPeriod(swaption);
    calibrationEngine.addInstrument(swaptionCalibration, PVSSC);
    calibrationEngine.calibrate(sabrData);
    final LiborMarketModelDisplacedDiffusionProviderInterface lmm = new LiborMarketModelDisplacedDiffusionProvider(multicurves, lmmParameters, ccy);
    // Risks
    final int nbCal = swaptionCalibration.length;
    final int nbFact = lmmParameters.getNbFactor();
    final List<Integer> instrumentIndex = calibrationEngine.getInstrumentIndex();
    final double[] dPvAmdLambda = new double[nbCal];
    final double[][][] dPvCaldGamma = new double[nbCal][][];
    final double[][] dPvCaldLambda = new double[nbCal][nbCal];
    final PresentValueSABRSensitivityDataBundle[] dPvCaldSABR = new PresentValueSABRSensitivityDataBundle[nbCal];
    final double[][] dPvAmdGamma = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaption, lmm);
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      dPvCaldGamma[loopcal] = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaptionCalibration[loopcal], lmm);
    }
    // Multiplicative-factor sensitivity
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      for (int loopperiod = instrumentIndex.get(loopcal); loopperiod < instrumentIndex.get(loopcal + 1); loopperiod++) {
        for (int loopfact = 0; loopfact < nbFact; loopfact++) {
          dPvAmdLambda[loopcal] += dPvAmdGamma[loopperiod][loopfact] * lmmParameters.getVolatility()[loopperiod][loopfact];
        }
      }
    }
    for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
      for (int loopcal2 = 0; loopcal2 < nbCal; loopcal2++) {
        for (int loopperiod = instrumentIndex.get(loopcal2); loopperiod < instrumentIndex.get(loopcal2 + 1); loopperiod++) {
          for (int loopfact = 0; loopfact < nbFact; loopfact++) {
            dPvCaldLambda[loopcal1][loopcal2] += dPvCaldGamma[loopcal1][loopperiod][loopfact] * lmmParameters.getVolatility()[loopperiod][loopfact];
          }
        }
      }
    }
    final CommonsMatrixAlgebra matrix = new CommonsMatrixAlgebra();
    final DoubleMatrix2D dPvCaldLambdaMatrix = new DoubleMatrix2D(dPvCaldLambda);
    final DoubleMatrix2D dPvCaldLambdaMatrixInverse = matrix.getInverse(dPvCaldLambdaMatrix);
    // SABR sensitivity
    final double[][] dPvCaldAlpha = new double[nbCal][nbCal];
    final double[][] dPvCaldRho = new double[nbCal][nbCal];
    final double[][] dPvCaldNu = new double[nbCal][nbCal];
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      dPvCaldSABR[loopcal] = swaptionCalibration[loopcal].accept(PVSSSSC, sabrData);
      final Set<DoublesPair> keySet = dPvCaldSABR[loopcal].getAlpha().getMap().keySet();
      final DoublesPair[] keys = keySet.toArray(new DoublesPair[keySet.size()]);
      dPvCaldAlpha[loopcal][loopcal] = dPvCaldSABR[loopcal].getAlpha().getMap().get(keys[0]);
      dPvCaldRho[loopcal][loopcal] = dPvCaldSABR[loopcal].getRho().getMap().get(keys[0]);
      dPvCaldNu[loopcal][loopcal] = dPvCaldSABR[loopcal].getNu().getMap().get(keys[0]);
    }
    final DoubleMatrix1D dPvAmdLambdaMatrix = new DoubleMatrix1D(dPvAmdLambda);
    final DoubleMatrix2D dPvCaldAlphaMatrix = new DoubleMatrix2D(dPvCaldAlpha);
    final DoubleMatrix2D dLambdadAlphaMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixInverse, dPvCaldAlphaMatrix);
    final DoubleMatrix2D dPvAmdAlphaMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadAlphaMatrix), dPvAmdLambdaMatrix);
    final DoubleMatrix2D dPvCaldRhoMatrix = new DoubleMatrix2D(dPvCaldRho);
    final DoubleMatrix2D dLambdadRhoMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixInverse, dPvCaldRhoMatrix);
    final DoubleMatrix2D dPvAmdRhoMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadRhoMatrix), dPvAmdLambdaMatrix);
    final DoubleMatrix2D dPvCaldNuMatrix = new DoubleMatrix2D(dPvCaldNu);
    final DoubleMatrix2D dLambdadNuMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixInverse, dPvCaldNuMatrix);
    final DoubleMatrix2D dPvAmdNuMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadNuMatrix), dPvAmdLambdaMatrix);
    final double[] dPvAmdAlpha = matrix.getTranspose(dPvAmdAlphaMatrix).getData()[0];
    final double[] dPvAmdRho = matrix.getTranspose(dPvAmdRhoMatrix).getData()[0];
    final double[] dPvAmdNu = matrix.getTranspose(dPvAmdNuMatrix).getData()[0];
    // Storage in PresentValueSABRSensitivityDataBundle
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      final DoublesPair expiryMaturity = DoublesPair.of(swaptionCalibration[loopcal].getTimeToExpiry(), swaptionCalibration[loopcal].getMaturityTime());
      sensi.addAlpha(expiryMaturity, dPvAmdAlpha[loopcal]);
      sensi.addRho(expiryMaturity, dPvAmdRho[loopcal]);
      sensi.addNu(expiryMaturity, dPvAmdNu[loopcal]);
    }
    return sensi;
  }

  /**
   * The method calibrates a LMM on a set of vanilla swaption priced with SABR. The set of vanilla swaptions is given by the CalibrationType.
   * The curve sensitivities of the original swaption are calculated with LMM re-calibration.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The present value curve sensitivities.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final Currency ccy = swaption.getCurrency();
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    //TODO: Create a way to chose the LMM base parameters (displacement, mean reversion, volatility).
    final LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionParameters.from(swaption, DEFAULT_DISPLACEMENT, DEFAULT_MEAN_REVERSION, new VolatilityLMMAngle(
        DEFAULT_ANGLE, DEFAULT_DISPLACEMENT));
    final SuccessiveRootFinderLMMDDCalibrationObjective objective = new SuccessiveRootFinderLMMDDCalibrationObjective(lmmParameters, ccy);
    final SuccessiveRootFinderLMMDDCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderLMMDDCalibrationEngine<>(objective);
    final SwaptionPhysicalFixedIbor[] swaptionCalibration = METHOD_BASKET.calibrationBasketFixedLegPeriod(swaption);
    calibrationEngine.addInstrument(swaptionCalibration, PVSSC);
    calibrationEngine.calibrate(sabrData);
    final LiborMarketModelDisplacedDiffusionProviderInterface lmm = new LiborMarketModelDisplacedDiffusionProvider(multicurves, lmmParameters, ccy);
    // Risks
    final int nbCal = swaptionCalibration.length;
    final int nbFact = lmmParameters.getNbFactor();
    final List<Integer> instrumentIndex = calibrationEngine.getInstrumentIndex();
    final double[] dPvAmdLambda = new double[nbCal];
    final double[][][] dPvCaldGamma = new double[nbCal][][];
    final double[][] dPvCaldLambda = new double[nbCal][nbCal];
    MultipleCurrencyMulticurveSensitivity pvcsCal = METHOD_SWAPTION_LMM.presentValueCurveSensitivity(swaption, lmm);
    pvcsCal = pvcsCal.cleaned();
    final double[][] dPvAmdGamma = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaption, lmm);
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      dPvCaldGamma[loopcal] = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaptionCalibration[loopcal], lmm);
    }
    // Multiplicative-factor sensitivity
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      for (int loopperiod = instrumentIndex.get(loopcal); loopperiod < instrumentIndex.get(loopcal + 1); loopperiod++) {
        for (int loopfact = 0; loopfact < nbFact; loopfact++) {
          dPvAmdLambda[loopcal] += dPvAmdGamma[loopperiod][loopfact] * lmmParameters.getVolatility()[loopperiod][loopfact];
        }
      }
    }
    for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
      for (int loopcal2 = 0; loopcal2 < nbCal; loopcal2++) {
        for (int loopperiod = instrumentIndex.get(loopcal2); loopperiod < instrumentIndex.get(loopcal2 + 1); loopperiod++) {
          for (int loopfact = 0; loopfact < nbFact; loopfact++) {
            dPvCaldLambda[loopcal1][loopcal2] += dPvCaldGamma[loopcal1][loopperiod][loopfact] * lmmParameters.getVolatility()[loopperiod][loopfact];
          }
        }
      }
    }
    final MultipleCurrencyMulticurveSensitivity[] pvcsCalBase = new MultipleCurrencyMulticurveSensitivity[nbCal];
    final MultipleCurrencyMulticurveSensitivity[] pvcsCalCal = new MultipleCurrencyMulticurveSensitivity[nbCal];
    final MultipleCurrencyMulticurveSensitivity[] pvcsCalDiff = new MultipleCurrencyMulticurveSensitivity[nbCal];
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      pvcsCalBase[loopcal] = swaptionCalibration[loopcal].accept(PVCSSSC, sabrData);
      pvcsCalBase[loopcal] = pvcsCalBase[loopcal].cleaned();
      pvcsCalCal[loopcal] = METHOD_SWAPTION_LMM.presentValueCurveSensitivity(swaptionCalibration[loopcal], lmm);
      pvcsCalCal[loopcal] = pvcsCalCal[loopcal].cleaned();
      pvcsCalDiff[loopcal] = pvcsCalBase[loopcal].plus(pvcsCalCal[loopcal].multipliedBy(-1));
      pvcsCalDiff[loopcal] = pvcsCalDiff[loopcal].cleaned();
    }
    final CommonsMatrixAlgebra matrix = new CommonsMatrixAlgebra();
    final DoubleMatrix2D dPvCaldLambdaMatrix = new DoubleMatrix2D(dPvCaldLambda);
    final DoubleMatrix2D dPvCaldLambdaMatrixInverse = matrix.getInverse(dPvCaldLambdaMatrix);
    // Curve sensitivity
    final MultipleCurrencyMulticurveSensitivity[] dLambdadC = new MultipleCurrencyMulticurveSensitivity[nbCal];
    for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
      dLambdadC[loopcal1] = new MultipleCurrencyMulticurveSensitivity();
      for (int loopcal2 = 0; loopcal2 <= loopcal1; loopcal2++) {
        dLambdadC[loopcal1] = dLambdadC[loopcal1].plus(pvcsCalDiff[loopcal2].multipliedBy(dPvCaldLambdaMatrixInverse.getEntry(loopcal1, loopcal2)));
      }
    }
    MultipleCurrencyMulticurveSensitivity pvcsAdjust = new MultipleCurrencyMulticurveSensitivity();
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      pvcsAdjust = pvcsAdjust.plus(dLambdadC[loopcal].multipliedBy(dPvAmdLambda[loopcal]));
    }
    pvcsAdjust = pvcsAdjust.cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsTot = pvcsCal.plus(pvcsAdjust);
    pvcsTot = pvcsTot.cleaned();
    return pvcsTot;
  }

  private static final class VolatilityLMMAngle extends Function1D<Double, Double[]> {
    /**
     * The angle between the factors: factor 1 weight is cos(angle*t/20) and factor 2 weight is sin(angle*t/20).
     * For the angle = 0, there is only one factor. For angle = pi/2, the 0Y rate is independent of the 20Y rate.
     */
    private final double _angle;
    private final double _displacement;

    public VolatilityLMMAngle(final double angle, final double displacement) {
      _angle = angle;
      _displacement = displacement;
    }

    @Override
    public Double[] evaluate(final Double x) {
      final Double[] result = new Double[2];
      result[0] = 0.01 / (_displacement + 0.05) * Math.cos(x / 20.0 * _angle);
      result[1] = 0.01 / (_displacement + 0.05) * Math.sin(x / 20.0 * _angle);
      // Implementation note: the initial value are chosen to have a 20% Black vol at 5% rate level.
      return result;
    }
  }

  /**
   * The method calibrates a LMM on a set of vanilla swaption priced with SABR. The set of vanilla swaptions is given by the CalibrationType.
   * The curve and SABR sensitivities of the original swaption are calculated with LMM re-calibration.
   * Used mainly for performance test purposes as the output is hybrid list.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The results (returned as a list of objects) [0] the present value, [1] the present curve sensitivity, [2] the present value SABR sensitivity.
   */
  public List<Object> presentValueCurveSABRSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final Currency ccy = swaption.getCurrency();
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    //TODO: Create a way to chose the LMM base parameters (displacement, mean reversion, volatility).
    final LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionParameters.from(swaption, DEFAULT_DISPLACEMENT, DEFAULT_MEAN_REVERSION, new VolatilityLMMAngle(
        DEFAULT_ANGLE, DEFAULT_DISPLACEMENT));
    final SuccessiveRootFinderLMMDDCalibrationObjective objective = new SuccessiveRootFinderLMMDDCalibrationObjective(lmmParameters, ccy);
    final SuccessiveRootFinderLMMDDCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderLMMDDCalibrationEngine<>(objective);
    final SwaptionPhysicalFixedIbor[] swaptionCalibration = METHOD_BASKET.calibrationBasketFixedLegPeriod(swaption);
    calibrationEngine.addInstrument(swaptionCalibration, PVSSC);
    calibrationEngine.calibrate(sabrData);
    final LiborMarketModelDisplacedDiffusionProviderInterface lmm = new LiborMarketModelDisplacedDiffusionProvider(multicurves, lmmParameters, ccy);
    // Risks
    final int nbCal = swaptionCalibration.length;
    final int nbFact = lmmParameters.getNbFactor();
    final List<Integer> instrumentIndex = calibrationEngine.getInstrumentIndex();
    final double[] dPvAmdLambda = new double[nbCal];
    final double[][][] dPvCaldGamma = new double[nbCal][][];
    final double[][] dPvCaldLambda = new double[nbCal][nbCal];
    final PresentValueSABRSensitivityDataBundle[] dPvCaldSABR = new PresentValueSABRSensitivityDataBundle[nbCal];
    MultipleCurrencyMulticurveSensitivity pvcsCal = METHOD_SWAPTION_LMM.presentValueCurveSensitivity(swaption, lmm);
    pvcsCal = pvcsCal.cleaned();
    final double[][] dPvAmdGamma = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaption, lmm);
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      dPvCaldGamma[loopcal] = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaptionCalibration[loopcal], lmm);
    }
    // Multiplicative-factor sensitivity
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      for (int loopperiod = instrumentIndex.get(loopcal); loopperiod < instrumentIndex.get(loopcal + 1); loopperiod++) {
        for (int loopfact = 0; loopfact < nbFact; loopfact++) {
          dPvAmdLambda[loopcal] += dPvAmdGamma[loopperiod][loopfact] * lmmParameters.getVolatility()[loopperiod][loopfact];
        }
      }
    }
    for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
      for (int loopcal2 = 0; loopcal2 < nbCal; loopcal2++) {
        for (int loopperiod = instrumentIndex.get(loopcal2); loopperiod < instrumentIndex.get(loopcal2 + 1); loopperiod++) {
          for (int loopfact = 0; loopfact < nbFact; loopfact++) {
            dPvCaldLambda[loopcal1][loopcal2] += dPvCaldGamma[loopcal1][loopperiod][loopfact] * lmmParameters.getVolatility()[loopperiod][loopfact];
          }
        }
      }
    }
    final MultipleCurrencyMulticurveSensitivity[] pvcsCalBase = new MultipleCurrencyMulticurveSensitivity[nbCal];
    final MultipleCurrencyMulticurveSensitivity[] pvcsCalCal = new MultipleCurrencyMulticurveSensitivity[nbCal];
    final MultipleCurrencyMulticurveSensitivity[] pvcsCalDiff = new MultipleCurrencyMulticurveSensitivity[nbCal];
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      pvcsCalBase[loopcal] = swaptionCalibration[loopcal].accept(PVCSSSC, sabrData);
      pvcsCalBase[loopcal] = pvcsCalBase[loopcal].cleaned();
      pvcsCalCal[loopcal] = METHOD_SWAPTION_LMM.presentValueCurveSensitivity(swaptionCalibration[loopcal], lmm);
      pvcsCalCal[loopcal] = pvcsCalCal[loopcal].cleaned();
      pvcsCalDiff[loopcal] = pvcsCalBase[loopcal].plus(pvcsCalCal[loopcal].multipliedBy(-1));
      pvcsCalDiff[loopcal] = pvcsCalDiff[loopcal].cleaned();
    }
    final CommonsMatrixAlgebra matrix = new CommonsMatrixAlgebra();
    final DoubleMatrix2D dPvCaldLambdaMatrix = new DoubleMatrix2D(dPvCaldLambda);
    final DoubleMatrix2D dPvCaldLambdaMatrixInverse = matrix.getInverse(dPvCaldLambdaMatrix);
    // SABR sensitivity
    final double[][] dPvCaldAlpha = new double[nbCal][nbCal];
    final double[][] dPvCaldRho = new double[nbCal][nbCal];
    final double[][] dPvCaldNu = new double[nbCal][nbCal];
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      dPvCaldSABR[loopcal] = swaptionCalibration[loopcal].accept(PVSSSSC, sabrData);
      final Set<DoublesPair> keySet = dPvCaldSABR[loopcal].getAlpha().getMap().keySet();
      final DoublesPair[] keys = keySet.toArray(new DoublesPair[keySet.size()]);
      dPvCaldAlpha[loopcal][loopcal] = dPvCaldSABR[loopcal].getAlpha().getMap().get(keys[0]);
      dPvCaldRho[loopcal][loopcal] = dPvCaldSABR[loopcal].getRho().getMap().get(keys[0]);
      dPvCaldNu[loopcal][loopcal] = dPvCaldSABR[loopcal].getNu().getMap().get(keys[0]);
    }
    final DoubleMatrix1D dPvAmdLambdaMatrix = new DoubleMatrix1D(dPvAmdLambda);
    final DoubleMatrix2D dPvCaldAlphaMatrix = new DoubleMatrix2D(dPvCaldAlpha);
    final DoubleMatrix2D dLambdadAlphaMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixInverse, dPvCaldAlphaMatrix);
    final DoubleMatrix2D dPvAmdAlphaMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadAlphaMatrix), dPvAmdLambdaMatrix);
    final DoubleMatrix2D dPvCaldRhoMatrix = new DoubleMatrix2D(dPvCaldRho);
    final DoubleMatrix2D dLambdadRhoMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixInverse, dPvCaldRhoMatrix);
    final DoubleMatrix2D dPvAmdRhoMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadRhoMatrix), dPvAmdLambdaMatrix);
    final DoubleMatrix2D dPvCaldNuMatrix = new DoubleMatrix2D(dPvCaldNu);
    final DoubleMatrix2D dLambdadNuMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixInverse, dPvCaldNuMatrix);
    final DoubleMatrix2D dPvAmdNuMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadNuMatrix), dPvAmdLambdaMatrix);
    final double[] dPvAmdAlpha = matrix.getTranspose(dPvAmdAlphaMatrix).getData()[0];
    final double[] dPvAmdRho = matrix.getTranspose(dPvAmdRhoMatrix).getData()[0];
    final double[] dPvAmdNu = matrix.getTranspose(dPvAmdNuMatrix).getData()[0];
    // Storage in PresentValueSABRSensitivityDataBundle
    final PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      final DoublesPair expiryMaturity = DoublesPair.of(swaptionCalibration[loopcal].getTimeToExpiry(), swaptionCalibration[loopcal].getMaturityTime());
      pvss.addAlpha(expiryMaturity, dPvAmdAlpha[loopcal]);
      pvss.addRho(expiryMaturity, dPvAmdRho[loopcal]);
      pvss.addNu(expiryMaturity, dPvAmdNu[loopcal]);
    }
    // Curve sensitivity
    final MultipleCurrencyMulticurveSensitivity[] dLambdadC = new MultipleCurrencyMulticurveSensitivity[nbCal];
    for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
      dLambdadC[loopcal1] = new MultipleCurrencyMulticurveSensitivity();
      for (int loopcal2 = 0; loopcal2 <= loopcal1; loopcal2++) {
        dLambdadC[loopcal1] = dLambdadC[loopcal1].plus(pvcsCalDiff[loopcal2].multipliedBy(dPvCaldLambdaMatrixInverse.getEntry(loopcal1, loopcal2)));
      }
    }
    MultipleCurrencyMulticurveSensitivity pvcs = new MultipleCurrencyMulticurveSensitivity();
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      pvcs = pvcs.plus(dLambdadC[loopcal].multipliedBy(dPvAmdLambda[loopcal]));
    }
    pvcs = pvcs.plus(pvcsCal);
    pvcs = pvcs.cleaned();
    final List<Object> results = new ArrayList<>();
    results.add(CurrencyAmount.of(swaption.getCurrency(), METHOD_SWAPTION_LMM.presentValue(swaption, lmm).getAmount(ccy)));
    results.add(pvcs);
    results.add(pvss);
    return results;
  }

}
