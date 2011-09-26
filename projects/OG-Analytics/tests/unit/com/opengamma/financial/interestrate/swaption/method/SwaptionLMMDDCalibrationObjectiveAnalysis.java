/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.LiborMarketModelDisplacedDiffusionTestsDataSet;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Analysis related to the calibration engine for LMM calibration to European swaptions.
 */
public class SwaptionLMMDDCalibrationObjectiveAnalysis {
  // Swaption description
  private static final boolean IS_LONG = true;
  private static final int SETTLEMENT_DAYS = 2;
  // Swap 5Y description
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(12);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final boolean FIXED_IS_PAYER = true;
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(6);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  //  private static final int[] SWAP_TENOR_YEAR = {1, 2, 3, 4, 5};
  private static final int[] SWAP_TENOR_YEAR = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final int EXPIRY_TENOR = 5;
  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofYears(EXPIRY_TENOR));
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, BUSINESS_DAY, CALENDAR, SETTLEMENT_DAYS);
  private static final CMSIndex[] CMS_INDEX = new CMSIndex[SWAP_TENOR_YEAR.length];
  static {
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      CMS_INDEX[loopexp] = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, Period.ofYears(SWAP_TENOR_YEAR[loopexp]));
    }
  }
  // to derivatives
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSets.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES);
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_LMM = new SwaptionPhysicalFixedIborLMMDDMethod();

  @Test(enabled = true)
  /**
   * Tests the price sensitivity with calibration for cash-settled swaptions in LMM. In normal testing, "enabled = false".
   */
  public void amortizedCalibrationLMMParameters() {
    final int nbTest = 250;
    long startTime, endTime;
    CommonsMatrixAlgebra matrix = new CommonsMatrixAlgebra();
    // Amortized swaption
    int nbCal = SWAP_TENOR_YEAR.length;
    int nbPeriodYear = 2;
    //    int nbPeriod = nbCal * nbPeriodYear;
    int nbFact = 2;
    double rateStart = 0.0325;
    //    double[] amotization = new double[] {1.00, 0.80, 0.60, 0.40, 0.20}; // For 5Y amortization
    double[] amotization = new double[] {1.00, 0.90, 0.80, 0.70, 0.60, 0.50, 0.40, 0.30, 0.20, 0.10}; // For 10Y amortization
    SwapFixedIborDefinition[][] swapCalibrationDefinition = new SwapFixedIborDefinition[nbTest][SWAP_TENOR_YEAR.length];
    SwaptionPhysicalFixedIborDefinition[][] swaptionCalibrationDefinition = new SwaptionPhysicalFixedIborDefinition[nbTest][SWAP_TENOR_YEAR.length];
    SwaptionPhysicalFixedIbor[][] swaptionCalibration = new SwaptionPhysicalFixedIbor[nbTest][SWAP_TENOR_YEAR.length];
    FixedFloatSwap[] swapAmortized = new FixedFloatSwap[nbTest];
    SwaptionPhysicalFixedIbor[] swaptionAmortized = new SwaptionPhysicalFixedIbor[nbTest];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        swapCalibrationDefinition[looptest][loopexp] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX[loopexp], NOTIONAL, rateStart + looptest * 0.01 / nbTest, FIXED_IS_PAYER);
        swaptionCalibrationDefinition[looptest][loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapCalibrationDefinition[looptest][loopexp], IS_LONG);
        swaptionCalibration[looptest][loopexp] = swaptionCalibrationDefinition[looptest][loopexp].toDerivative(REFERENCE_DATE, CURVES_NAME);
      }
      CouponFixed[] cpnFixed = new CouponFixed[SWAP_TENOR_YEAR.length];
      AnnuityCouponFixed legFixed = swaptionCalibration[looptest][SWAP_TENOR_YEAR.length - 1].getUnderlyingSwap().getFixedLeg();
      CouponIbor[] cpnIbor = new CouponIbor[2 * SWAP_TENOR_YEAR.length];
      @SuppressWarnings("unchecked")
      GenericAnnuity<Payment> legIbor = (GenericAnnuity<Payment>) swaptionCalibration[looptest][SWAP_TENOR_YEAR.length - 1].getUnderlyingSwap().getSecondLeg();
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        cpnFixed[loopexp] = legFixed.getNthPayment(loopexp).withNotional(legFixed.getNthPayment(loopexp).getNotional() * amotization[loopexp]);
        cpnIbor[2 * loopexp] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp)).getNotional() * amotization[loopexp]);
        cpnIbor[2 * loopexp + 1] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).getNotional() * amotization[loopexp]);
      }
      swapAmortized[looptest] = new FixedFloatSwap(new AnnuityCouponFixed(cpnFixed), new AnnuityCouponIbor(cpnIbor));
      swaptionAmortized[looptest] = SwaptionPhysicalFixedIbor.from(swaptionCalibration[looptest][0].getTimeToExpiry(), swapAmortized[looptest], swaptionCalibration[looptest][0].getSettlementTime(),
          IS_LONG);
    }
    // Calibration and price
    startTime = System.currentTimeMillis();
    CurrencyAmount[] pvAmortized = new CurrencyAmount[nbTest];
    LiborMarketModelDisplacedDiffusionDataBundle[] lmmBundle = new LiborMarketModelDisplacedDiffusionDataBundle[nbTest];
    double[][][] volInit = new double[nbTest][][];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE,
          swapCalibrationDefinition[looptest][SWAP_TENOR_YEAR.length - 1]);
      SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
      volInit[looptest] = objective.getVolatilityInit();
      SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        calibrationEngine.addInstrument(swaptionCalibration[looptest][loopexp], METHOD_SABR);
      }
      calibrationEngine.calibrate(SABR_BUNDLE);
      lmmBundle[looptest] = new LiborMarketModelDisplacedDiffusionDataBundle(lmmParameters, CURVES);
      pvAmortized[looptest] = METHOD_LMM.presentValue(swaptionAmortized[looptest], lmmBundle[looptest]);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " LMM calibration and amortized swaption price: " + (endTime - startTime) + " ms (price=" + pvAmortized.toString() + ")");
    // Performance note: calibration: 1-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2200 ms for 1000 price with calibration.
    // Risks
    double[] alphaBar = new double[nbCal];
    double[] rhoBar = new double[nbCal];
    double[] nuBar = new double[nbCal];
    double[][][] dPvAmdGamma = new double[nbTest][][];
    double[][] dPvAmdLambda = new double[nbTest][nbCal];
    double[][][][] dPvCaldGamma = new double[nbTest][nbCal][][];
    double[][][] dPvCaldLambda = new double[nbTest][nbCal][nbCal];
    double[][][] dPvAmdAlpha = new double[nbTest][][];
    double[][][] dPvAmdRho = new double[nbTest][][];
    double[][][] dPvAmdNu = new double[nbTest][][];
    PresentValueSABRSensitivityDataBundle[][] dPvCaldSABR = new PresentValueSABRSensitivityDataBundle[nbTest][nbCal];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      dPvAmdGamma[looptest] = METHOD_LMM.presentValueLMMSensitivity(swaptionAmortized[looptest], lmmBundle[looptest]);
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        dPvCaldGamma[looptest][loopcal] = METHOD_LMM.presentValueLMMSensitivity(swaptionCalibration[looptest][loopcal], lmmBundle[looptest]);
      }
      // Multiplicative-factor sensitivity
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        for (int loopperiod = 0; loopperiod < nbPeriodYear; loopperiod++) {
          for (int loopfact = 0; loopfact < nbFact; loopfact++) {
            dPvAmdLambda[looptest][loopcal] += dPvAmdGamma[looptest][nbPeriodYear * loopcal + loopperiod][loopfact] * volInit[looptest][nbPeriodYear * loopcal + loopperiod][loopfact];
          }
        }
      }
      for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
        for (int loopcal2 = 0; loopcal2 < nbCal; loopcal2++) {
          for (int loopperiod = 0; loopperiod < nbPeriodYear; loopperiod++) {
            for (int loopfact = 0; loopfact < nbFact; loopfact++) {
              dPvCaldLambda[looptest][loopcal1][loopcal2] += dPvCaldGamma[looptest][loopcal1][nbPeriodYear * loopcal2 + loopperiod][loopfact]
                  * volInit[looptest][nbPeriodYear * loopcal2 + loopperiod][loopfact];
            }
          }
        }
      }
      DoubleMatrix2D dPvCaldLambdaMatrix = new DoubleMatrix2D(dPvCaldLambda[looptest]);
      DoubleMatrix2D dPvCaldLambdaMatrixMinus1 = matrix.getInverse(dPvCaldLambdaMatrix);
      // SABR sensitivity
      double[][] dPvCaldAlpha = new double[nbCal][nbCal];
      double[][] dPvCaldRho = new double[nbCal][nbCal];
      double[][] dPvCaldNu = new double[nbCal][nbCal];
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        dPvCaldSABR[looptest][loopcal] = METHOD_SABR.presentValueSABRSensitivity(swaptionCalibration[looptest][loopcal], SABR_BUNDLE);
        DoublesPair[] keySet = dPvCaldSABR[looptest][loopcal].getAlpha().keySet().toArray(new DoublesPair[0]);
        dPvCaldAlpha[loopcal][loopcal] = dPvCaldSABR[looptest][loopcal].getAlpha().get(keySet[0]);
        dPvCaldRho[loopcal][loopcal] = dPvCaldSABR[looptest][loopcal].getRho().get(keySet[0]);
        dPvCaldNu[loopcal][loopcal] = dPvCaldSABR[looptest][loopcal].getNu().get(keySet[0]);
      }
      DoubleMatrix1D dPvAmdLambdaMatrix = new DoubleMatrix1D(dPvAmdLambda[looptest]);
      DoubleMatrix2D dPvCaldAlphaMatrix = new DoubleMatrix2D(dPvCaldAlpha);
      DoubleMatrix2D dLambdadAlphaMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixMinus1, dPvCaldAlphaMatrix);
      dLambdadAlphaMatrix = (DoubleMatrix2D) matrix.scale(dLambdadAlphaMatrix, -1.0);
      DoubleMatrix2D dPvAmdAlphaMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadAlphaMatrix), dPvAmdLambdaMatrix);
      DoubleMatrix2D dPvCaldRhoMatrix = new DoubleMatrix2D(dPvCaldRho);
      DoubleMatrix2D dLambdadRhoMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixMinus1, dPvCaldRhoMatrix);
      dLambdadRhoMatrix = (DoubleMatrix2D) matrix.scale(dLambdadRhoMatrix, -1.0);
      DoubleMatrix2D dPvAmdRhoMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadAlphaMatrix), dPvAmdLambdaMatrix);
      DoubleMatrix2D dPvCaldNuMatrix = new DoubleMatrix2D(dPvCaldNu);
      DoubleMatrix2D dLambdadNuMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixMinus1, dPvCaldNuMatrix);
      dLambdadNuMatrix = (DoubleMatrix2D) matrix.scale(dLambdadNuMatrix, -1.0);
      DoubleMatrix2D dPvAmdNuMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadAlphaMatrix), dPvAmdLambdaMatrix);
      dPvAmdAlpha[looptest] = dPvAmdAlphaMatrix.getData();
      dPvAmdRho[looptest] = dPvAmdRhoMatrix.getData();
      dPvAmdNu[looptest] = dPvAmdNuMatrix.getData();
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " LMM amortized SABR risks: " + (endTime - startTime) + " ms (risk=" + alphaBar + " ," + rhoBar + " ," + nuBar + ")");
    // Performance note: calibration: 1-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 875 ms for 1000 SABR risk.
  }

  @Test(enabled = true)
  /**
   * Tests the price sensitivity with calibration for cash-settled swaptions in LMM. In normal testing, "enabled = false".
   */
  public void amortizedCalibrationCurve() {
    final int nbTest = 250;
    long startTime, endTime;
    CommonsMatrixAlgebra matrix = new CommonsMatrixAlgebra();
    // Amortized swaption
    int nbCal = SWAP_TENOR_YEAR.length;
    int nbPeriodYear = 2;
    int nbFact = 2;
    double rateStart = 0.0325;
    //    double[] amotization = new double[] {1.00, 0.80, 0.60, 0.40, 0.20}; // For 5Y amortization
    double[] amotization = new double[] {1.00, 0.90, 0.80, 0.70, 0.60, 0.50, 0.40, 0.30, 0.20, 0.10}; // For 10Y amortization
    SwapFixedIborDefinition[][] swapCalibrationDefinition = new SwapFixedIborDefinition[nbTest][SWAP_TENOR_YEAR.length];
    SwaptionPhysicalFixedIborDefinition[][] swaptionCalibrationDefinition = new SwaptionPhysicalFixedIborDefinition[nbTest][SWAP_TENOR_YEAR.length];
    SwaptionPhysicalFixedIbor[][] swaptionCalibration = new SwaptionPhysicalFixedIbor[nbTest][SWAP_TENOR_YEAR.length];
    FixedFloatSwap[] swapAmortized = new FixedFloatSwap[nbTest];
    SwaptionPhysicalFixedIbor[] swaptionAmortized = new SwaptionPhysicalFixedIbor[nbTest];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        swapCalibrationDefinition[looptest][loopexp] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX[loopexp], NOTIONAL, rateStart + looptest * 0.01 / nbTest, FIXED_IS_PAYER);
        swaptionCalibrationDefinition[looptest][loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapCalibrationDefinition[looptest][loopexp], IS_LONG);
        swaptionCalibration[looptest][loopexp] = swaptionCalibrationDefinition[looptest][loopexp].toDerivative(REFERENCE_DATE, CURVES_NAME);
      }
      CouponFixed[] cpnFixed = new CouponFixed[SWAP_TENOR_YEAR.length];
      AnnuityCouponFixed legFixed = swaptionCalibration[looptest][SWAP_TENOR_YEAR.length - 1].getUnderlyingSwap().getFixedLeg();
      CouponIbor[] cpnIbor = new CouponIbor[2 * SWAP_TENOR_YEAR.length];
      @SuppressWarnings("unchecked")
      GenericAnnuity<Payment> legIbor = (GenericAnnuity<Payment>) swaptionCalibration[looptest][SWAP_TENOR_YEAR.length - 1].getUnderlyingSwap().getSecondLeg();
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        cpnFixed[loopexp] = legFixed.getNthPayment(loopexp).withNotional(legFixed.getNthPayment(loopexp).getNotional() * amotization[loopexp]);
        cpnIbor[2 * loopexp] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp)).getNotional() * amotization[loopexp]);
        cpnIbor[2 * loopexp + 1] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).getNotional() * amotization[loopexp]);
      }
      swapAmortized[looptest] = new FixedFloatSwap(new AnnuityCouponFixed(cpnFixed), new AnnuityCouponIbor(cpnIbor));
      swaptionAmortized[looptest] = SwaptionPhysicalFixedIbor.from(swaptionCalibration[looptest][0].getTimeToExpiry(), swapAmortized[looptest], swaptionCalibration[looptest][0].getSettlementTime(),
          IS_LONG);
    }
    // Calibration and price
    startTime = System.currentTimeMillis();
    CurrencyAmount[] pvAmortized = new CurrencyAmount[nbTest];
    LiborMarketModelDisplacedDiffusionDataBundle[] lmmBundle = new LiborMarketModelDisplacedDiffusionDataBundle[nbTest];
    double[][][] volInit = new double[nbTest][][];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE,
          swapCalibrationDefinition[looptest][SWAP_TENOR_YEAR.length - 1]);
      SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
      volInit[looptest] = objective.getVolatilityInit();
      SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        calibrationEngine.addInstrument(swaptionCalibration[looptest][loopexp], METHOD_SABR);
      }
      calibrationEngine.calibrate(SABR_BUNDLE);
      lmmBundle[looptest] = new LiborMarketModelDisplacedDiffusionDataBundle(lmmParameters, CURVES);
      pvAmortized[looptest] = METHOD_LMM.presentValue(swaptionAmortized[looptest], lmmBundle[looptest]);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " LMM calibration and amortized swaption price: " + (endTime - startTime) + " ms (price=" + pvAmortized.toString() + ")");
    // Performance note: calibration: 23-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2200 ms for 1000 price with calibration.
    // Risks
    PresentValueSensitivity[] pvcsAmCal = new PresentValueSensitivity[nbTest];
    PresentValueSensitivity[] pvcsAmTot = new PresentValueSensitivity[nbTest];
    double[][][] dPvAmdGamma = new double[nbTest][][];
    double[][] dPvAmdLambda = new double[nbTest][nbCal];
    double[][][][] dPvCaldGamma = new double[nbTest][nbCal][][];
    double[][][] dPvCaldLambda = new double[nbTest][nbCal][nbCal];
    PresentValueSensitivity[][] pvcsCalBase = new PresentValueSensitivity[nbTest][nbCal];
    PresentValueSensitivity[][] pvcsCalCal = new PresentValueSensitivity[nbTest][nbCal];
    PresentValueSensitivity[][] pvcsCalDiff = new PresentValueSensitivity[nbTest][nbCal];
    PresentValueSensitivity[] pvcsAdjust = new PresentValueSensitivity[nbTest];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcsAmCal[looptest] = METHOD_LMM.presentValueCurveSensitivity(swaptionAmortized[looptest], lmmBundle[looptest]);
      pvcsAmCal[looptest] = pvcsAmCal[looptest].clean();
      dPvAmdGamma[looptest] = METHOD_LMM.presentValueLMMSensitivity(swaptionAmortized[looptest], lmmBundle[looptest]);
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        dPvCaldGamma[looptest][loopcal] = METHOD_LMM.presentValueLMMSensitivity(swaptionCalibration[looptest][loopcal], lmmBundle[looptest]);
      }
      // Multiplicative-factor sensitivity
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        for (int loopperiod = 0; loopperiod < nbPeriodYear; loopperiod++) {
          for (int loopfact = 0; loopfact < nbFact; loopfact++) {
            dPvAmdLambda[looptest][loopcal] += dPvAmdGamma[looptest][nbPeriodYear * loopcal + loopperiod][loopfact] * volInit[looptest][nbPeriodYear * loopcal + loopperiod][loopfact];
          }
        }
      }
      for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
        for (int loopcal2 = 0; loopcal2 < nbCal; loopcal2++) {
          for (int loopperiod = 0; loopperiod < nbPeriodYear; loopperiod++) {
            for (int loopfact = 0; loopfact < nbFact; loopfact++) {
              dPvCaldLambda[looptest][loopcal1][loopcal2] += dPvCaldGamma[looptest][loopcal1][nbPeriodYear * loopcal2 + loopperiod][loopfact]
                  * volInit[looptest][nbPeriodYear * loopcal2 + loopperiod][loopfact];
            }
          }
        }
      }
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        pvcsCalBase[looptest][loopcal] = METHOD_SABR.presentValueSensitivity(swaptionCalibration[looptest][loopcal], SABR_BUNDLE);
        pvcsCalBase[looptest][loopcal] = pvcsCalBase[looptest][loopcal].clean();
        pvcsCalCal[looptest][loopcal] = METHOD_LMM.presentValueCurveSensitivity(swaptionCalibration[looptest][loopcal], lmmBundle[looptest]);
        pvcsCalCal[looptest][loopcal] = pvcsCalCal[looptest][loopcal].clean();
        pvcsCalDiff[looptest][loopcal] = pvcsCalBase[looptest][loopcal].add(pvcsCalCal[looptest][loopcal].multiply(-1));
        pvcsCalDiff[looptest][loopcal] = pvcsCalDiff[looptest][loopcal].clean();
      }
      DoubleMatrix2D dPvCaldLambdaMatrix = new DoubleMatrix2D(dPvCaldLambda[looptest]);
      DoubleMatrix2D dPvCaldLambdaMatrixMinus1 = matrix.getInverse(dPvCaldLambdaMatrix);
      // Curve sensitivity
      PresentValueSensitivity[] dLambdadC = new PresentValueSensitivity[nbCal];
      for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
        dLambdadC[loopcal1] = new PresentValueSensitivity();
        for (int loopcal2 = 0; loopcal2 <= loopcal1; loopcal2++) {
          dLambdadC[loopcal1] = dLambdadC[loopcal1].add(pvcsCalDiff[looptest][loopcal2].multiply(dPvCaldLambdaMatrixMinus1.getEntry(loopcal1, loopcal2)));
        }
      }
      pvcsAdjust[looptest] = new PresentValueSensitivity();
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        pvcsAdjust[looptest] = pvcsAdjust[looptest].add(dLambdadC[loopcal].multiply(dPvAmdLambda[looptest][loopcal]));
      }
      pvcsAdjust[looptest] = pvcsAdjust[looptest].clean();
      pvcsAmTot[looptest] = pvcsAmCal[looptest].add(pvcsAdjust[looptest]);
      pvcsAmTot[looptest] = pvcsAmTot[looptest].clean();
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " LMM amortized curve risks: " + (endTime - startTime) + " ms");
    // Performance note: calibration: 20-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2650 ms for 1000 SABR risk.
  }

  @Test(enabled = true)
  /**
   * Tests the price sensitivity with calibration for cash-settled swaptions in LMM. In normal testing, "enabled = false".
   */
  public void amortizedCalibrationLMMParameters2() {
    final int nbTest = 250;
    long startTime, endTime;
    CommonsMatrixAlgebra matrix = new CommonsMatrixAlgebra();
    // Amortized swaption
    int nbCal = SWAP_TENOR_YEAR.length;
    int nbPeriodYear = 2;
    //    int nbPeriod = nbCal * nbPeriodYear;
    int nbFact = 2;
    double rateStart = 0.0325;
    //    double[] amotization = new double[] {1.00, 0.80, 0.60, 0.40, 0.20}; // For 5Y amortization
    double[] amotization = new double[] {1.00, 0.90, 0.80, 0.70, 0.60, 0.50, 0.40, 0.30, 0.20, 0.10}; // For 10Y amortization
    SwapFixedIborDefinition[][] swapCalibrationDefinition = new SwapFixedIborDefinition[nbTest][SWAP_TENOR_YEAR.length];
    SwaptionPhysicalFixedIborDefinition[][] swaptionCalibrationDefinition = new SwaptionPhysicalFixedIborDefinition[nbTest][SWAP_TENOR_YEAR.length];
    SwaptionPhysicalFixedIbor[][] swaptionCalibration = new SwaptionPhysicalFixedIbor[nbTest][SWAP_TENOR_YEAR.length];
    FixedFloatSwap[] swapAmortized = new FixedFloatSwap[nbTest];
    SwaptionPhysicalFixedIbor[] swaptionAmortized = new SwaptionPhysicalFixedIbor[nbTest];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        swapCalibrationDefinition[looptest][loopexp] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX[loopexp], NOTIONAL, rateStart + looptest * 0.01 / nbTest, FIXED_IS_PAYER);
        swaptionCalibrationDefinition[looptest][loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapCalibrationDefinition[looptest][loopexp], IS_LONG);
        swaptionCalibration[looptest][loopexp] = swaptionCalibrationDefinition[looptest][loopexp].toDerivative(REFERENCE_DATE, CURVES_NAME);
      }
      CouponFixed[] cpnFixed = new CouponFixed[SWAP_TENOR_YEAR.length];
      AnnuityCouponFixed legFixed = swaptionCalibration[looptest][SWAP_TENOR_YEAR.length - 1].getUnderlyingSwap().getFixedLeg();
      CouponIbor[] cpnIbor = new CouponIbor[2 * SWAP_TENOR_YEAR.length];
      @SuppressWarnings("unchecked")
      GenericAnnuity<Payment> legIbor = (GenericAnnuity<Payment>) swaptionCalibration[looptest][SWAP_TENOR_YEAR.length - 1].getUnderlyingSwap().getSecondLeg();
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        cpnFixed[loopexp] = legFixed.getNthPayment(loopexp).withNotional(legFixed.getNthPayment(loopexp).getNotional() * amotization[loopexp]);
        cpnIbor[2 * loopexp] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp)).getNotional() * amotization[loopexp]);
        cpnIbor[2 * loopexp + 1] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).getNotional() * amotization[loopexp]);
      }
      swapAmortized[looptest] = new FixedFloatSwap(new AnnuityCouponFixed(cpnFixed), new AnnuityCouponIbor(cpnIbor));
      swaptionAmortized[looptest] = SwaptionPhysicalFixedIbor.from(swaptionCalibration[looptest][0].getTimeToExpiry(), swapAmortized[looptest], swaptionCalibration[looptest][0].getSettlementTime(),
          IS_LONG);
    }
    // Calibration and price
    startTime = System.currentTimeMillis();
    CurrencyAmount[] pvAmortized = new CurrencyAmount[nbTest];
    LiborMarketModelDisplacedDiffusionDataBundle[] lmmBundle = new LiborMarketModelDisplacedDiffusionDataBundle[nbTest];
    double[][][] volInit = new double[nbTest][][];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE,
          swapCalibrationDefinition[looptest][SWAP_TENOR_YEAR.length - 1]);
      SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
      volInit[looptest] = objective.getVolatilityInit();
      SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        calibrationEngine.addInstrument(swaptionCalibration[looptest][loopexp], METHOD_SABR);
      }
      calibrationEngine.calibrate(SABR_BUNDLE);
      lmmBundle[looptest] = new LiborMarketModelDisplacedDiffusionDataBundle(lmmParameters, CURVES);
      pvAmortized[looptest] = METHOD_LMM.presentValue(swaptionAmortized[looptest], lmmBundle[looptest]);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " LMM calibration and amortized swaption price: " + (endTime - startTime) + " ms (price=" + pvAmortized.toString() + ")");
    // Performance note: calibration: 1-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2200 ms for 1000 price with calibration.
    // Risks
    double[] alphaBar = new double[nbCal];
    double[] rhoBar = new double[nbCal];
    double[] nuBar = new double[nbCal];
    double[][][] dPvAmdGamma = new double[nbTest][][];
    double[][] dPvAmdLambda = new double[nbTest][nbCal];
    double[][][][] dPvCaldGamma = new double[nbTest][nbCal][][];
    double[][][] dPvCaldLambda = new double[nbTest][nbCal][nbCal];
    double[][][] dPvAmdAlpha = new double[nbTest][][];
    double[][][] dPvAmdRho = new double[nbTest][][];
    double[][][] dPvAmdNu = new double[nbTest][][];
    PresentValueSABRSensitivityDataBundle[][] dPvCaldSABR = new PresentValueSABRSensitivityDataBundle[nbTest][nbCal];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      dPvAmdGamma[looptest] = METHOD_LMM.presentValueLMMSensitivity(swaptionAmortized[looptest], lmmBundle[looptest]);
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        dPvCaldGamma[looptest][loopcal] = METHOD_LMM.presentValueLMMSensitivity(swaptionCalibration[looptest][loopcal], lmmBundle[looptest]);
      }
      // Multiplicative-factor sensitivity
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        for (int loopperiod = 0; loopperiod < nbPeriodYear; loopperiod++) {
          for (int loopfact = 0; loopfact < nbFact; loopfact++) {
            dPvAmdLambda[looptest][loopcal] += dPvAmdGamma[looptest][nbPeriodYear * loopcal + loopperiod][loopfact] * volInit[looptest][nbPeriodYear * loopcal + loopperiod][loopfact];
          }
        }
      }
      for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
        for (int loopcal2 = 0; loopcal2 < nbCal; loopcal2++) {
          for (int loopperiod = 0; loopperiod < nbPeriodYear; loopperiod++) {
            for (int loopfact = 0; loopfact < nbFact; loopfact++) {
              dPvCaldLambda[looptest][loopcal1][loopcal2] += dPvCaldGamma[looptest][loopcal1][nbPeriodYear * loopcal2 + loopperiod][loopfact]
                  * volInit[looptest][nbPeriodYear * loopcal2 + loopperiod][loopfact];
            }
          }
        }
      }
      DoubleMatrix2D dPvCaldLambdaMatrix = new DoubleMatrix2D(dPvCaldLambda[looptest]);
      DoubleMatrix2D dPvCaldLambdaMatrixMinus1 = matrix.getInverse(dPvCaldLambdaMatrix);
      // SABR sensitivity
      double[][] dPvCaldAlpha = new double[nbCal][nbCal];
      double[][] dPvCaldRho = new double[nbCal][nbCal];
      double[][] dPvCaldNu = new double[nbCal][nbCal];
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        dPvCaldSABR[looptest][loopcal] = METHOD_SABR.presentValueSABRSensitivity(swaptionCalibration[looptest][loopcal], SABR_BUNDLE);
        DoublesPair[] keySet = dPvCaldSABR[looptest][loopcal].getAlpha().keySet().toArray(new DoublesPair[0]);
        dPvCaldAlpha[loopcal][loopcal] = dPvCaldSABR[looptest][loopcal].getAlpha().get(keySet[0]);
        dPvCaldRho[loopcal][loopcal] = dPvCaldSABR[looptest][loopcal].getRho().get(keySet[0]);
        dPvCaldNu[loopcal][loopcal] = dPvCaldSABR[looptest][loopcal].getNu().get(keySet[0]);
      }
      DoubleMatrix1D dPvAmdLambdaMatrix = new DoubleMatrix1D(dPvAmdLambda[looptest]);
      DoubleMatrix2D dPvCaldAlphaMatrix = new DoubleMatrix2D(dPvCaldAlpha);
      DoubleMatrix2D dLambdadAlphaMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixMinus1, dPvCaldAlphaMatrix);
      dLambdadAlphaMatrix = (DoubleMatrix2D) matrix.scale(dLambdadAlphaMatrix, -1.0);
      DoubleMatrix2D dPvAmdAlphaMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadAlphaMatrix), dPvAmdLambdaMatrix);
      DoubleMatrix2D dPvCaldRhoMatrix = new DoubleMatrix2D(dPvCaldRho);
      DoubleMatrix2D dLambdadRhoMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixMinus1, dPvCaldRhoMatrix);
      dLambdadRhoMatrix = (DoubleMatrix2D) matrix.scale(dLambdadRhoMatrix, -1.0);
      DoubleMatrix2D dPvAmdRhoMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadAlphaMatrix), dPvAmdLambdaMatrix);
      DoubleMatrix2D dPvCaldNuMatrix = new DoubleMatrix2D(dPvCaldNu);
      DoubleMatrix2D dLambdadNuMatrix = (DoubleMatrix2D) matrix.multiply(dPvCaldLambdaMatrixMinus1, dPvCaldNuMatrix);
      dLambdadNuMatrix = (DoubleMatrix2D) matrix.scale(dLambdadNuMatrix, -1.0);
      DoubleMatrix2D dPvAmdNuMatrix = (DoubleMatrix2D) matrix.multiply(matrix.getTranspose(dLambdadAlphaMatrix), dPvAmdLambdaMatrix);
      dPvAmdAlpha[looptest] = dPvAmdAlphaMatrix.getData();
      dPvAmdRho[looptest] = dPvAmdRhoMatrix.getData();
      dPvAmdNu[looptest] = dPvAmdNuMatrix.getData();
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " LMM amortized SABR risks: " + (endTime - startTime) + " ms (risk=" + alphaBar + " ," + rhoBar + " ," + nuBar + ")");
    // Performance note: calibration: 1-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 875 ms for 1000 SABR risk.
  }

  @Test(enabled = true)
  /**
   * Tests the price sensitivity with calibration for cash-settled swaptions in LMM. In normal testing, "enabled = false".
   */
  public void amortizedCalibrationCurve2() {
    final int nbTest = 250;
    long startTime, endTime;
    CommonsMatrixAlgebra matrix = new CommonsMatrixAlgebra();
    // Amortized swaption
    int nbCal = SWAP_TENOR_YEAR.length;
    int nbPeriodYear = 2;
    int nbFact = 2;
    double rateStart = 0.0325;
    //    double[] amotization = new double[] {1.00, 0.80, 0.60, 0.40, 0.20}; // For 5Y amortization
    double[] amotization = new double[] {1.00, 0.90, 0.80, 0.70, 0.60, 0.50, 0.40, 0.30, 0.20, 0.10}; // For 10Y amortization
    SwapFixedIborDefinition[][] swapCalibrationDefinition = new SwapFixedIborDefinition[nbTest][SWAP_TENOR_YEAR.length];
    SwaptionPhysicalFixedIborDefinition[][] swaptionCalibrationDefinition = new SwaptionPhysicalFixedIborDefinition[nbTest][SWAP_TENOR_YEAR.length];
    SwaptionPhysicalFixedIbor[][] swaptionCalibration = new SwaptionPhysicalFixedIbor[nbTest][SWAP_TENOR_YEAR.length];
    FixedFloatSwap[] swapAmortized = new FixedFloatSwap[nbTest];
    SwaptionPhysicalFixedIbor[] swaptionAmortized = new SwaptionPhysicalFixedIbor[nbTest];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        swapCalibrationDefinition[looptest][loopexp] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX[loopexp], NOTIONAL, rateStart + looptest * 0.01 / nbTest, FIXED_IS_PAYER);
        swaptionCalibrationDefinition[looptest][loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapCalibrationDefinition[looptest][loopexp], IS_LONG);
        swaptionCalibration[looptest][loopexp] = swaptionCalibrationDefinition[looptest][loopexp].toDerivative(REFERENCE_DATE, CURVES_NAME);
      }
      CouponFixed[] cpnFixed = new CouponFixed[SWAP_TENOR_YEAR.length];
      AnnuityCouponFixed legFixed = swaptionCalibration[looptest][SWAP_TENOR_YEAR.length - 1].getUnderlyingSwap().getFixedLeg();
      CouponIbor[] cpnIbor = new CouponIbor[2 * SWAP_TENOR_YEAR.length];
      @SuppressWarnings("unchecked")
      GenericAnnuity<Payment> legIbor = (GenericAnnuity<Payment>) swaptionCalibration[looptest][SWAP_TENOR_YEAR.length - 1].getUnderlyingSwap().getSecondLeg();
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        cpnFixed[loopexp] = legFixed.getNthPayment(loopexp).withNotional(legFixed.getNthPayment(loopexp).getNotional() * amotization[loopexp]);
        cpnIbor[2 * loopexp] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp)).getNotional() * amotization[loopexp]);
        cpnIbor[2 * loopexp + 1] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).getNotional() * amotization[loopexp]);
      }
      swapAmortized[looptest] = new FixedFloatSwap(new AnnuityCouponFixed(cpnFixed), new AnnuityCouponIbor(cpnIbor));
      swaptionAmortized[looptest] = SwaptionPhysicalFixedIbor.from(swaptionCalibration[looptest][0].getTimeToExpiry(), swapAmortized[looptest], swaptionCalibration[looptest][0].getSettlementTime(),
          IS_LONG);
    }
    // Calibration and price
    startTime = System.currentTimeMillis();
    CurrencyAmount[] pvAmortized = new CurrencyAmount[nbTest];
    LiborMarketModelDisplacedDiffusionDataBundle[] lmmBundle = new LiborMarketModelDisplacedDiffusionDataBundle[nbTest];
    double[][][] volInit = new double[nbTest][][];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE,
          swapCalibrationDefinition[looptest][SWAP_TENOR_YEAR.length - 1]);
      SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
      volInit[looptest] = objective.getVolatilityInit();
      SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        calibrationEngine.addInstrument(swaptionCalibration[looptest][loopexp], METHOD_SABR);
      }
      calibrationEngine.calibrate(SABR_BUNDLE);
      lmmBundle[looptest] = new LiborMarketModelDisplacedDiffusionDataBundle(lmmParameters, CURVES);
      pvAmortized[looptest] = METHOD_LMM.presentValue(swaptionAmortized[looptest], lmmBundle[looptest]);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " LMM calibration and amortized swaption price: " + (endTime - startTime) + " ms (price=" + pvAmortized.toString() + ")");
    // Performance note: calibration: 23-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2200 ms for 1000 price with calibration.
    // Risks
    PresentValueSensitivity[] pvcsAmCal = new PresentValueSensitivity[nbTest];
    PresentValueSensitivity[] pvcsAmTot = new PresentValueSensitivity[nbTest];
    double[][][] dPvAmdGamma = new double[nbTest][][];
    double[][] dPvAmdLambda = new double[nbTest][nbCal];
    double[][][][] dPvCaldGamma = new double[nbTest][nbCal][][];
    double[][][] dPvCaldLambda = new double[nbTest][nbCal][nbCal];
    PresentValueSensitivity[][] pvcsCalBase = new PresentValueSensitivity[nbTest][nbCal];
    PresentValueSensitivity[][] pvcsCalCal = new PresentValueSensitivity[nbTest][nbCal];
    PresentValueSensitivity[][] pvcsCalDiff = new PresentValueSensitivity[nbTest][nbCal];
    PresentValueSensitivity[] pvcsAdjust = new PresentValueSensitivity[nbTest];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcsAmCal[looptest] = METHOD_LMM.presentValueCurveSensitivity(swaptionAmortized[looptest], lmmBundle[looptest]);
      pvcsAmCal[looptest] = pvcsAmCal[looptest].clean();
      dPvAmdGamma[looptest] = METHOD_LMM.presentValueLMMSensitivity(swaptionAmortized[looptest], lmmBundle[looptest]);
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        dPvCaldGamma[looptest][loopcal] = METHOD_LMM.presentValueLMMSensitivity(swaptionCalibration[looptest][loopcal], lmmBundle[looptest]);
      }
      // Multiplicative-factor sensitivity
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        for (int loopperiod = 0; loopperiod < nbPeriodYear; loopperiod++) {
          for (int loopfact = 0; loopfact < nbFact; loopfact++) {
            dPvAmdLambda[looptest][loopcal] += dPvAmdGamma[looptest][nbPeriodYear * loopcal + loopperiod][loopfact] * volInit[looptest][nbPeriodYear * loopcal + loopperiod][loopfact];
          }
        }
      }
      for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
        for (int loopcal2 = 0; loopcal2 < nbCal; loopcal2++) {
          for (int loopperiod = 0; loopperiod < nbPeriodYear; loopperiod++) {
            for (int loopfact = 0; loopfact < nbFact; loopfact++) {
              dPvCaldLambda[looptest][loopcal1][loopcal2] += dPvCaldGamma[looptest][loopcal1][nbPeriodYear * loopcal2 + loopperiod][loopfact]
                  * volInit[looptest][nbPeriodYear * loopcal2 + loopperiod][loopfact];
            }
          }
        }
      }
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        pvcsCalBase[looptest][loopcal] = METHOD_SABR.presentValueSensitivity(swaptionCalibration[looptest][loopcal], SABR_BUNDLE);
        pvcsCalBase[looptest][loopcal] = pvcsCalBase[looptest][loopcal].clean();
        pvcsCalCal[looptest][loopcal] = METHOD_LMM.presentValueCurveSensitivity(swaptionCalibration[looptest][loopcal], lmmBundle[looptest]);
        pvcsCalCal[looptest][loopcal] = pvcsCalCal[looptest][loopcal].clean();
        pvcsCalDiff[looptest][loopcal] = pvcsCalBase[looptest][loopcal].add(pvcsCalCal[looptest][loopcal].multiply(-1));
        pvcsCalDiff[looptest][loopcal] = pvcsCalDiff[looptest][loopcal].clean();
      }
      DoubleMatrix2D dPvCaldLambdaMatrix = new DoubleMatrix2D(dPvCaldLambda[looptest]);
      DoubleMatrix2D dPvCaldLambdaMatrixMinus1 = matrix.getInverse(dPvCaldLambdaMatrix);
      // Curve sensitivity
      PresentValueSensitivity[] dLambdadC = new PresentValueSensitivity[nbCal];
      for (int loopcal1 = 0; loopcal1 < nbCal; loopcal1++) {
        dLambdadC[loopcal1] = new PresentValueSensitivity();
        for (int loopcal2 = 0; loopcal2 <= loopcal1; loopcal2++) {
          dLambdadC[loopcal1] = dLambdadC[loopcal1].add(pvcsCalDiff[looptest][loopcal2].multiply(dPvCaldLambdaMatrixMinus1.getEntry(loopcal1, loopcal2)));
        }
      }
      pvcsAdjust[looptest] = new PresentValueSensitivity();
      for (int loopcal = 0; loopcal < nbCal; loopcal++) {
        pvcsAdjust[looptest] = pvcsAdjust[looptest].add(dLambdadC[loopcal].multiply(dPvAmdLambda[looptest][loopcal]));
      }
      pvcsAdjust[looptest] = pvcsAdjust[looptest].clean();
      pvcsAmTot[looptest] = pvcsAmCal[looptest].add(pvcsAdjust[looptest]);
      pvcsAmTot[looptest] = pvcsAmTot[looptest].clean();
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " LMM amortized curve risks: " + (endTime - startTime) + " ms");
    // Performance note: calibration: 20-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2650 ms for 1000 SABR risk.
  }

}
