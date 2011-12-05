/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor.SwaptionPhysicalFixedIborCalibrationType;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to computes the present value and sensitivities of physical delivery European swaptions with a Libor Market Model calibrated to SABR prices.
 * The LMM displacements and volatility weights are hard coded.
 */
public class SwaptionPhysicalFixedIborSABRLMMMethod implements PricingMethod {

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
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SWAPTION_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  /**
   * The LMM method used for European swaptions with physical delivery.
   */
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_SWAPTION_LMM = new SwaptionPhysicalFixedIborLMMDDMethod();

  /**
   * The method calibrates a LMM on a set of vanilla swaption priced with SABR. The set of vanilla swaptions is given by the CalibrationType.
   * The original swaption is priced with the calibrated LMM. 
   * This should not be used for vanilla swaptions (the price is equal to the SABR price with a longer computation type and some approximation).
   * This is useful for non-standard swaptions like amortized swaptions.
   * @param swaption The swaption.
   * @param curves The curves and SABR data.
   * @return The present value. 
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final SABRInterestRateDataBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    //TODO: Create a way to chose the LMM base parameters (displacement, mean reversion, volatility).
    LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionParameters.from(swaption, DEFAULT_DISPLACEMENT, DEFAULT_MEAN_REVERSION, new VolatilityLMMAngle(
        DEFAULT_ANGLE, DEFAULT_DISPLACEMENT));
    SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
    SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
    //TODO: Create a way to chose the calibration type.
    InstrumentDerivative[] swaptionCalibration = swaption.calibrationBasket(SwaptionPhysicalFixedIborCalibrationType.FIXEDLEG_STRIKE);
    calibrationEngine.addInstrument(swaptionCalibration, METHOD_SWAPTION_SABR);
    calibrationEngine.calibrate(curves);
    LiborMarketModelDisplacedDiffusionDataBundle lmmBundle = new LiborMarketModelDisplacedDiffusionDataBundle(lmmParameters, curves);
    return CurrencyAmount.of(swaption.getCurrency(), METHOD_SWAPTION_LMM.presentValue(swaption, lmmBundle).getAmount());
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Physical delivery swaption");
    Validate.isTrue(curves instanceof LiborMarketModelDisplacedDiffusionDataBundle, "Bundle should contain LMM data");
    return presentValue(instrument, curves);
  }

  /**
   * The method calibrates a LMM on a set of vanilla swaption priced with SABR. The set of vanilla swaptions is given by the CalibrationType.
   * The SABR parameters sensitivities of the original swaption are calculated with LMM re-calibration.
   * @param swaption The swaption.
   * @param curves The curves and SABR data.
   * @return The present value SABR parameters sensitivity. 
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRInterestRateDataBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    //TODO: Create a way to chose the LMM base parameters (displacement, mean reversion, volatility).
    LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionParameters.from(swaption, DEFAULT_DISPLACEMENT, DEFAULT_MEAN_REVERSION, new VolatilityLMMAngle(
        DEFAULT_ANGLE, DEFAULT_DISPLACEMENT));
    SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
    SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
    //TODO: Create a way to chose the calibration type.
    SwaptionPhysicalFixedIbor[] swaptionCalibration = swaption.calibrationBasket(SwaptionPhysicalFixedIborCalibrationType.FIXEDLEG_STRIKE);
    calibrationEngine.addInstrument(swaptionCalibration, METHOD_SWAPTION_SABR);
    calibrationEngine.calibrate(curves);
    LiborMarketModelDisplacedDiffusionDataBundle lmmBundle = new LiborMarketModelDisplacedDiffusionDataBundle(lmmParameters, curves);
    // Risks
    int nbCal = swaptionCalibration.length;
    int nbFact = lmmParameters.getNbFactor();
    List<Integer> instrumentIndex = calibrationEngine.getInstrumentIndex();
    double[] dPvAmdLambda = new double[nbCal];
    double[][][] dPvCaldGamma = new double[nbCal][][];
    double[][] dPvCaldLambda = new double[nbCal][nbCal];
    PresentValueSABRSensitivityDataBundle[] dPvCaldSABR = new PresentValueSABRSensitivityDataBundle[nbCal];
    double[][] dPvAmdGamma = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaption, lmmBundle);
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      dPvCaldGamma[loopcal] = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaptionCalibration[loopcal], lmmBundle);
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
    CommonsMatrixAlgebra matrix = new CommonsMatrixAlgebra();
    DoubleMatrix2D dPvCaldLambdaMatrix = new DoubleMatrix2D(dPvCaldLambda);
    DoubleMatrix2D dPvCaldLambdaMatrixInverse = matrix.getInverse(dPvCaldLambdaMatrix);
    // SABR sensitivity
    double[][] dPvCaldAlpha = new double[nbCal][nbCal];
    double[][] dPvCaldRho = new double[nbCal][nbCal];
    double[][] dPvCaldNu = new double[nbCal][nbCal];
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      dPvCaldSABR[loopcal] = METHOD_SWAPTION_SABR.presentValueSABRSensitivity(swaptionCalibration[loopcal], curves);
      DoublesPair[] keySet = dPvCaldSABR[loopcal].getAlpha().getMap().keySet().toArray(new DoublesPair[0]);
      dPvCaldAlpha[loopcal][loopcal] = dPvCaldSABR[loopcal].getAlpha().getMap().get(keySet[0]);
      dPvCaldRho[loopcal][loopcal] = dPvCaldSABR[loopcal].getRho().getMap().get(keySet[0]);
      dPvCaldNu[loopcal][loopcal] = dPvCaldSABR[loopcal].getNu().getMap().get(keySet[0]);
    }
    DoubleMatrix1D dPvAmdLambdaMatrix = new DoubleMatrix1D(dPvAmdLambda);
    DoubleMatrix2D dPvCaldAlphaMatrix = new DoubleMatrix2D(dPvCaldAlpha);
    DoubleMatrix2D dLambdadAlphaMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixInverse, dPvCaldAlphaMatrix);
    DoubleMatrix2D dPvAmdAlphaMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadAlphaMatrix), dPvAmdLambdaMatrix);
    DoubleMatrix2D dPvCaldRhoMatrix = new DoubleMatrix2D(dPvCaldRho);
    DoubleMatrix2D dLambdadRhoMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixInverse, dPvCaldRhoMatrix);
    DoubleMatrix2D dPvAmdRhoMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadRhoMatrix), dPvAmdLambdaMatrix);
    DoubleMatrix2D dPvCaldNuMatrix = new DoubleMatrix2D(dPvCaldNu);
    DoubleMatrix2D dLambdadNuMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixInverse, dPvCaldNuMatrix);
    DoubleMatrix2D dPvAmdNuMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadNuMatrix), dPvAmdLambdaMatrix);
    double[] dPvAmdAlpha = matrix.getTranspose(dPvAmdAlphaMatrix).getData()[0];
    double[] dPvAmdRho = matrix.getTranspose(dPvAmdRhoMatrix).getData()[0];
    double[] dPvAmdNu = matrix.getTranspose(dPvAmdNuMatrix).getData()[0];
    // Storage in PresentValueSABRSensitivityDataBundle
    PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      DoublesPair expiryMaturity = new DoublesPair(swaptionCalibration[loopcal].getTimeToExpiry(), swaptionCalibration[loopcal].getMaturityTime());
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
   * @param curves The curves and SABR data.
   * @return The present value curve sensitivities. 
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRInterestRateDataBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    //TODO: Create a way to chose the LMM base parameters (displacement, mean reversion, volatility).
    LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionParameters.from(swaption, DEFAULT_DISPLACEMENT, DEFAULT_MEAN_REVERSION, new VolatilityLMMAngle(
        DEFAULT_ANGLE, DEFAULT_DISPLACEMENT));
    SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
    SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
    //TODO: Create a way to chose the calibration type.
    SwaptionPhysicalFixedIbor[] swaptionCalibration = swaption.calibrationBasket(SwaptionPhysicalFixedIborCalibrationType.FIXEDLEG_STRIKE);
    calibrationEngine.addInstrument(swaptionCalibration, METHOD_SWAPTION_SABR);
    calibrationEngine.calibrate(curves);
    LiborMarketModelDisplacedDiffusionDataBundle lmmBundle = new LiborMarketModelDisplacedDiffusionDataBundle(lmmParameters, curves);
    // Risks
    int nbCal = swaptionCalibration.length;
    int nbFact = lmmParameters.getNbFactor();
    List<Integer> instrumentIndex = calibrationEngine.getInstrumentIndex();
    double[] dPvAmdLambda = new double[nbCal];
    double[][][] dPvCaldGamma = new double[nbCal][][];
    double[][] dPvCaldLambda = new double[nbCal][nbCal];
    InterestRateCurveSensitivity pvcsCal = METHOD_SWAPTION_LMM.presentValueCurveSensitivity(swaption, lmmBundle);
    pvcsCal = pvcsCal.clean();
    double[][] dPvAmdGamma = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaption, lmmBundle);
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      dPvCaldGamma[loopcal] = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaptionCalibration[loopcal], lmmBundle);
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
    InterestRateCurveSensitivity[] pvcsCalBase = new InterestRateCurveSensitivity[nbCal];
    InterestRateCurveSensitivity[] pvcsCalCal = new InterestRateCurveSensitivity[nbCal];
    InterestRateCurveSensitivity[] pvcsCalDiff = new InterestRateCurveSensitivity[nbCal];
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      pvcsCalBase[loopcal] = METHOD_SWAPTION_SABR.presentValueSensitivity(swaptionCalibration[loopcal], curves);
      pvcsCalBase[loopcal] = pvcsCalBase[loopcal].clean();
      pvcsCalCal[loopcal] = METHOD_SWAPTION_LMM.presentValueCurveSensitivity(swaptionCalibration[loopcal], lmmBundle);
      pvcsCalCal[loopcal] = pvcsCalCal[loopcal].clean();
      pvcsCalDiff[loopcal] = pvcsCalBase[loopcal].add(pvcsCalCal[loopcal].multiply(-1));
      pvcsCalDiff[loopcal] = pvcsCalDiff[loopcal].clean();
    }
    CommonsMatrixAlgebra matrix = new CommonsMatrixAlgebra();
    DoubleMatrix2D dPvCaldLambdaMatrix = new DoubleMatrix2D(dPvCaldLambda);
    DoubleMatrix2D dPvCaldLambdaMatrixInverse = matrix.getInverse(dPvCaldLambdaMatrix);
    // Curve sensitivity
    InterestRateCurveSensitivity[] dLambdadC = new InterestRateCurveSensitivity[nbCal];
    for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
      dLambdadC[loopcal1] = new InterestRateCurveSensitivity();
      for (int loopcal2 = 0; loopcal2 <= loopcal1; loopcal2++) {
        dLambdadC[loopcal1] = dLambdadC[loopcal1].add(pvcsCalDiff[loopcal2].multiply(dPvCaldLambdaMatrixInverse.getEntry(loopcal1, loopcal2)));
      }
    }
    InterestRateCurveSensitivity pvcsAdjust = new InterestRateCurveSensitivity();
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      pvcsAdjust = pvcsAdjust.add(dLambdadC[loopcal].multiply(dPvAmdLambda[loopcal]));
    }
    pvcsAdjust = pvcsAdjust.clean();
    InterestRateCurveSensitivity pvcsTot = pvcsCal.add(pvcsAdjust);
    pvcsTot = pvcsTot.clean();
    return pvcsTot;
  }

  class VolatilityLMMAngle extends Function1D<Double, Double[]> {
    /**
     * The angle between the factors: factor 1 weight is cos(angle*t/20) and factor 2 weight is sin(angle*t/20).
     * For the angle = 0, there is only one factor. For angle = pi/2, the 0Y rate is independent of the 20Y rate.
     */
    private final double _angle;
    private final double _displacement;

    public VolatilityLMMAngle(double angle, double displacement) {
      _angle = angle;
      _displacement = displacement;
    }

    @Override
    public Double[] evaluate(Double x) {
      Double[] result = new Double[2];
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
   * @param curves The curves and SABR data.
   * @return The results (returned as a list of objects) [0] the present value, [1] the present curve sensitivity, [2] the present value SABR sensitivity.
   */
  public List<Object> presentValueCurveSABRSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRInterestRateDataBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    //TODO: Create a way to chose the LMM base parameters (displacement, mean reversion, volatility).
    LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionParameters.from(swaption, DEFAULT_DISPLACEMENT, DEFAULT_MEAN_REVERSION, new VolatilityLMMAngle(
        DEFAULT_ANGLE, DEFAULT_DISPLACEMENT));
    SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
    SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
    //TODO: Create a way to chose the calibration type.
    SwaptionPhysicalFixedIbor[] swaptionCalibration = swaption.calibrationBasket(SwaptionPhysicalFixedIborCalibrationType.FIXEDLEG_STRIKE);
    calibrationEngine.addInstrument(swaptionCalibration, METHOD_SWAPTION_SABR);
    calibrationEngine.calibrate(curves);
    LiborMarketModelDisplacedDiffusionDataBundle lmmBundle = new LiborMarketModelDisplacedDiffusionDataBundle(lmmParameters, curves);
    // Risks
    int nbCal = swaptionCalibration.length;
    int nbFact = lmmParameters.getNbFactor();
    List<Integer> instrumentIndex = calibrationEngine.getInstrumentIndex();
    double[] dPvAmdLambda = new double[nbCal];
    double[][][] dPvCaldGamma = new double[nbCal][][];
    double[][] dPvCaldLambda = new double[nbCal][nbCal];
    PresentValueSABRSensitivityDataBundle[] dPvCaldSABR = new PresentValueSABRSensitivityDataBundle[nbCal];
    InterestRateCurveSensitivity pvcsCal = METHOD_SWAPTION_LMM.presentValueCurveSensitivity(swaption, lmmBundle);
    pvcsCal = pvcsCal.clean();
    double[][] dPvAmdGamma = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaption, lmmBundle);
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      dPvCaldGamma[loopcal] = METHOD_SWAPTION_LMM.presentValueLMMSensitivity(swaptionCalibration[loopcal], lmmBundle);
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
    InterestRateCurveSensitivity[] pvcsCalBase = new InterestRateCurveSensitivity[nbCal];
    InterestRateCurveSensitivity[] pvcsCalCal = new InterestRateCurveSensitivity[nbCal];
    InterestRateCurveSensitivity[] pvcsCalDiff = new InterestRateCurveSensitivity[nbCal];
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      pvcsCalBase[loopcal] = METHOD_SWAPTION_SABR.presentValueSensitivity(swaptionCalibration[loopcal], curves);
      pvcsCalBase[loopcal] = pvcsCalBase[loopcal].clean();
      pvcsCalCal[loopcal] = METHOD_SWAPTION_LMM.presentValueCurveSensitivity(swaptionCalibration[loopcal], lmmBundle);
      pvcsCalCal[loopcal] = pvcsCalCal[loopcal].clean();
      pvcsCalDiff[loopcal] = pvcsCalBase[loopcal].add(pvcsCalCal[loopcal].multiply(-1));
      pvcsCalDiff[loopcal] = pvcsCalDiff[loopcal].clean();
    }
    CommonsMatrixAlgebra matrix = new CommonsMatrixAlgebra();
    DoubleMatrix2D dPvCaldLambdaMatrix = new DoubleMatrix2D(dPvCaldLambda);
    DoubleMatrix2D dPvCaldLambdaMatrixInverse = matrix.getInverse(dPvCaldLambdaMatrix);
    // SABR sensitivity
    double[][] dPvCaldAlpha = new double[nbCal][nbCal];
    double[][] dPvCaldRho = new double[nbCal][nbCal];
    double[][] dPvCaldNu = new double[nbCal][nbCal];
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      dPvCaldSABR[loopcal] = METHOD_SWAPTION_SABR.presentValueSABRSensitivity(swaptionCalibration[loopcal], curves);
      DoublesPair[] keySet = dPvCaldSABR[loopcal].getAlpha().getMap().keySet().toArray(new DoublesPair[0]);
      dPvCaldAlpha[loopcal][loopcal] = dPvCaldSABR[loopcal].getAlpha().getMap().get(keySet[0]);
      dPvCaldRho[loopcal][loopcal] = dPvCaldSABR[loopcal].getRho().getMap().get(keySet[0]);
      dPvCaldNu[loopcal][loopcal] = dPvCaldSABR[loopcal].getNu().getMap().get(keySet[0]);
    }
    DoubleMatrix1D dPvAmdLambdaMatrix = new DoubleMatrix1D(dPvAmdLambda);
    DoubleMatrix2D dPvCaldAlphaMatrix = new DoubleMatrix2D(dPvCaldAlpha);
    DoubleMatrix2D dLambdadAlphaMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixInverse, dPvCaldAlphaMatrix);
    DoubleMatrix2D dPvAmdAlphaMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadAlphaMatrix), dPvAmdLambdaMatrix);
    DoubleMatrix2D dPvCaldRhoMatrix = new DoubleMatrix2D(dPvCaldRho);
    DoubleMatrix2D dLambdadRhoMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixInverse, dPvCaldRhoMatrix);
    DoubleMatrix2D dPvAmdRhoMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadRhoMatrix), dPvAmdLambdaMatrix);
    DoubleMatrix2D dPvCaldNuMatrix = new DoubleMatrix2D(dPvCaldNu);
    DoubleMatrix2D dLambdadNuMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixInverse, dPvCaldNuMatrix);
    DoubleMatrix2D dPvAmdNuMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadNuMatrix), dPvAmdLambdaMatrix);
    double[] dPvAmdAlpha = matrix.getTranspose(dPvAmdAlphaMatrix).getData()[0];
    double[] dPvAmdRho = matrix.getTranspose(dPvAmdRhoMatrix).getData()[0];
    double[] dPvAmdNu = matrix.getTranspose(dPvAmdNuMatrix).getData()[0];
    // Storage in PresentValueSABRSensitivityDataBundle
    PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      DoublesPair expiryMaturity = new DoublesPair(swaptionCalibration[loopcal].getTimeToExpiry(), swaptionCalibration[loopcal].getMaturityTime());
      pvss.addAlpha(expiryMaturity, dPvAmdAlpha[loopcal]);
      pvss.addRho(expiryMaturity, dPvAmdRho[loopcal]);
      pvss.addNu(expiryMaturity, dPvAmdNu[loopcal]);
    }
    // Curve sensitivity
    InterestRateCurveSensitivity[] dLambdadC = new InterestRateCurveSensitivity[nbCal];
    for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
      dLambdadC[loopcal1] = new InterestRateCurveSensitivity();
      for (int loopcal2 = 0; loopcal2 <= loopcal1; loopcal2++) {
        dLambdadC[loopcal1] = dLambdadC[loopcal1].add(pvcsCalDiff[loopcal2].multiply(dPvCaldLambdaMatrixInverse.getEntry(loopcal1, loopcal2)));
      }
    }
    InterestRateCurveSensitivity pvcs = new InterestRateCurveSensitivity();
    for (int loopcal = 0; loopcal < nbCal; loopcal++) {
      pvcs = pvcs.add(dLambdadC[loopcal].multiply(dPvAmdLambda[loopcal]));
    }
    pvcs = pvcs.add(pvcsCal);
    pvcs = pvcs.clean();
    List<Object> results = new ArrayList<Object>();
    results.add(CurrencyAmount.of(swaption.getCurrency(), METHOD_SWAPTION_LMM.presentValue(swaption, lmmBundle).getAmount()));
    results.add(pvcs);
    results.add(pvss);
    return results;
  }

}
