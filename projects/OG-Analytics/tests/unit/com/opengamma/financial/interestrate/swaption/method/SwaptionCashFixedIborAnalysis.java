/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;

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
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.DataSet2010Sep30;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.LiborMarketModelDisplacedDiffusionTestsDataSet;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.montecarlo.LiborMarketModelMonteCarloMethod;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.random.NormalRandomNumberGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Analysis of cash-settled swaptions.
 */
public class SwaptionCashFixedIborAnalysis {
  private static final ZonedDateTime ANALYSIS_DATE = DateUtils.getUTCDate(2010, 9, 30); // DateUtils.getUTCDate(2014, 3, 18);

  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;

  private static final Period IBOR_TENOR = Period.ofMonths(6);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);

  private static final Period FIXED_TENOR = Period.ofMonths(12);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");

  private static final Period EXPIRY_TENOR = Period.ofYears(5);
  private static final int SWAP_TENOR = 10;
  private static final Period SWAP_TENOR_PERIOD = Period.ofYears(SWAP_TENOR);

  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(ANALYSIS_DATE, BUSINESS_DAY, CALENDAR, IS_EOM, EXPIRY_TENOR);
  private static final ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, CALENDAR, SETTLEMENT_DAYS);

  private static final double NOTIONAL = 10000;

  private static final CMSIndex[] CMS_INDEX = new CMSIndex[SWAP_TENOR];
  static {
    for (int loopmat = 0; loopmat < SWAP_TENOR; loopmat++) {
      CMS_INDEX[loopmat] = new CMSIndex(FIXED_TENOR, FIXED_DAY_COUNT, IBOR_INDEX, Period.ofYears(loopmat + 1));
    }
  }
  private static final SwapFixedIborDefinition SWAP_0_DEFINITION = SwapFixedIborDefinition.from(SETTLE_DATE, CMS_INDEX[SWAP_TENOR - 1], NOTIONAL, 0.0, true);

  private static final YieldCurveBundle CURVES = DataSet2010Sep30.createCurves();
  private static final String[] CURVES_NAMES = CURVES.getAllNames().toArray(new String[0]);
  private static final String[] CURVES_NAMES_6 = new String[] {CURVES_NAMES[0], CURVES_NAMES[2]};
  private static final SABRInterestRateParameters SABR_PARAMETERS = DataSet2010Sep30.createSABR();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETERS, CURVES);

  private static final FixedCouponSwap<Coupon> SWAP_0 = SWAP_0_DEFINITION.toDerivative(ANALYSIS_DATE, CURVES_NAMES_6);

  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SABR_PHYS = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final SwaptionCashFixedIborSABRMethod METHOD_SABR_CASH = SwaptionCashFixedIborSABRMethod.getInstance();
  private static final SwaptionCashFixedIborHullWhiteApproximationMethod METHOD_HW_CASH = new SwaptionCashFixedIborHullWhiteApproximationMethod();
  private static final SwaptionCashFixedIborG2ppNumericalIntegrationMethod METHOD_G2_CASH = new SwaptionCashFixedIborG2ppNumericalIntegrationMethod();
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_LMM_PHYS = new SwaptionPhysicalFixedIborLMMDDMethod();
  private static final SwaptionCashFixedIborLinearTSRMethod METHOD_TSR_CASH = new SwaptionCashFixedIborLinearTSRMethod();

  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  private static final double FORWARD = PRC.visit(SWAP_0, CURVES);
  private static final int NB_STRIKE = 6;
  private static final double[] STRIKE = new double[NB_STRIKE];
  static {
    for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
      STRIKE[loopstrike] = FORWARD + (loopstrike - 2) * 0.015;
    }
  }

  private static final SwapFixedIborDefinition[][] SWAP_PAYER_DEFINITION = new SwapFixedIborDefinition[NB_STRIKE][SWAP_TENOR];
  private static final SwapFixedIborDefinition[][] SWAP_RECEI_DEFINITION = new SwapFixedIborDefinition[NB_STRIKE][SWAP_TENOR];
  private static final SwaptionPhysicalFixedIborDefinition[][] SWAPTION_PHYS_PAYER_DEFINITION = new SwaptionPhysicalFixedIborDefinition[NB_STRIKE][SWAP_TENOR];
  private static final SwaptionPhysicalFixedIborDefinition[][] SWAPTION_PHYS_RECEI_DEFINITION = new SwaptionPhysicalFixedIborDefinition[NB_STRIKE][SWAP_TENOR];
  private static final SwaptionCashFixedIborDefinition[] SWAPTION_CASH_PAYER_DEFINITION = new SwaptionCashFixedIborDefinition[NB_STRIKE];
  private static final SwaptionCashFixedIborDefinition[] SWAPTION_CASH_RECEI_DEFINITION = new SwaptionCashFixedIborDefinition[NB_STRIKE];
  private static final SwaptionPhysicalFixedIbor[][] SWAPTION_PHYS_PAYER = new SwaptionPhysicalFixedIbor[NB_STRIKE][SWAP_TENOR];
  private static final SwaptionPhysicalFixedIbor[][] SWAPTION_PHYS_RECEI = new SwaptionPhysicalFixedIbor[NB_STRIKE][SWAP_TENOR];
  private static final SwaptionCashFixedIbor[] SWAPTION_CASH_PAYER = new SwaptionCashFixedIbor[NB_STRIKE];
  private static final SwaptionCashFixedIbor[] SWAPTION_CASH_RECEI = new SwaptionCashFixedIbor[NB_STRIKE];

  private static final double[][] PV_PHYS_PAYER = new double[NB_STRIKE][SWAP_TENOR];
  private static final double[][] PV_PHYS_RECEI = new double[NB_STRIKE][SWAP_TENOR];
  private static final double[] PV_CASH_PAYER = new double[NB_STRIKE];
  private static final double[] PV_CASH_RECEI = new double[NB_STRIKE];
  private static final double[] DIFF_PHYS_PAYER = new double[NB_STRIKE];
  private static final double[] DIFF_PHYS_RECEI = new double[NB_STRIKE];
  private static final double[] VEGA = new double[NB_STRIKE];

  static {
    for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
      for (int loopmat = 0; loopmat < SWAP_TENOR; loopmat++) {
        SWAP_PAYER_DEFINITION[loopstrike][loopmat] = SwapFixedIborDefinition.from(SETTLE_DATE, CMS_INDEX[loopmat], NOTIONAL, STRIKE[loopstrike], true);
        SWAP_RECEI_DEFINITION[loopstrike][loopmat] = SwapFixedIborDefinition.from(SETTLE_DATE, CMS_INDEX[loopmat], NOTIONAL, STRIKE[loopstrike], false);
        SWAPTION_PHYS_PAYER_DEFINITION[loopstrike][loopmat] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION[loopstrike][loopmat], true);
        SWAPTION_PHYS_RECEI_DEFINITION[loopstrike][loopmat] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEI_DEFINITION[loopstrike][loopmat], true);
        SWAPTION_PHYS_PAYER[loopstrike][loopmat] = SWAPTION_PHYS_PAYER_DEFINITION[loopstrike][loopmat].toDerivative(ANALYSIS_DATE, CURVES_NAMES_6);
        SWAPTION_PHYS_RECEI[loopstrike][loopmat] = SWAPTION_PHYS_RECEI_DEFINITION[loopstrike][loopmat].toDerivative(ANALYSIS_DATE, CURVES_NAMES_6);
        PV_PHYS_PAYER[loopstrike][loopmat] = METHOD_SABR_PHYS.presentValue(SWAPTION_PHYS_PAYER[loopstrike][loopmat], SABR_BUNDLE).getAmount();
        PV_PHYS_RECEI[loopstrike][loopmat] = METHOD_SABR_PHYS.presentValue(SWAPTION_PHYS_RECEI[loopstrike][loopmat], SABR_BUNDLE).getAmount();
      }
      SWAPTION_CASH_PAYER_DEFINITION[loopstrike] = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION[loopstrike][SWAP_TENOR - 1], true);
      SWAPTION_CASH_RECEI_DEFINITION[loopstrike] = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEI_DEFINITION[loopstrike][SWAP_TENOR - 1], true);
      SWAPTION_CASH_PAYER[loopstrike] = SWAPTION_CASH_PAYER_DEFINITION[loopstrike].toDerivative(ANALYSIS_DATE, CURVES_NAMES_6);
      SWAPTION_CASH_RECEI[loopstrike] = SWAPTION_CASH_RECEI_DEFINITION[loopstrike].toDerivative(ANALYSIS_DATE, CURVES_NAMES_6);
      PV_CASH_PAYER[loopstrike] = METHOD_SABR_CASH.presentValue(SWAPTION_CASH_PAYER[loopstrike], SABR_BUNDLE).getAmount();
      PV_CASH_RECEI[loopstrike] = METHOD_SABR_CASH.presentValue(SWAPTION_CASH_RECEI[loopstrike], SABR_BUNDLE).getAmount();
      DIFF_PHYS_PAYER[loopstrike] = PV_CASH_PAYER[loopstrike] - PV_PHYS_PAYER[loopstrike][SWAP_TENOR - 1];
      DIFF_PHYS_RECEI[loopstrike] = PV_CASH_RECEI[loopstrike] - PV_PHYS_RECEI[loopstrike][SWAP_TENOR - 1];
      PresentValueSABRSensitivityDataBundle pvss = METHOD_SABR_PHYS.presentValueSABRSensitivity(SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1], SABR_BUNDLE);
      final double maturity = SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1].getUnderlyingSwap().getFixedLeg()
          .getNthPayment(SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1].getUnderlyingSwap().getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
          - SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1].getSettlementTime();
      final DoublesPair expiryMaturity = new DoublesPair(SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1].getTimeToExpiry(), maturity);
      VEGA[loopstrike] = pvss.getAlpha().get(expiryMaturity) * 0.0002; // ~0.10% Black 
    }
  }

  @Test(enabled = true)
  /**
   * Computes the price of cash-settled swaptions in the Hull-White one-factor model with different mean reversions.
   */
  public void hullWhite() {
    double[] meanReversion = new double[] {0.001, 0.01, 0.02, 0.05, 0.10};
    int nbMR = meanReversion.length;

    double[][] pvCashPayerHW = new double[NB_STRIKE][nbMR];
    double[][] pvCashReceiHW = new double[NB_STRIKE][nbMR];
    double[][] diffCashPayerHW = new double[NB_STRIKE][nbMR];
    double[][] diffCashReceiHW = new double[NB_STRIKE][nbMR];

    for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
      for (int loopmr = 0; loopmr < nbMR; loopmr++) {
        // Calibration
        HullWhiteOneFactorPiecewiseConstantParameters hwParameters = new HullWhiteOneFactorPiecewiseConstantParameters(meanReversion[loopmr], new double[] {0.01}, new double[0]);
        SwaptionPhysicalHullWhiteCalibrationObjective objective = new SwaptionPhysicalHullWhiteCalibrationObjective(hwParameters);
        SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalHullWhiteSuccessiveRootFinderCalibrationEngine(objective);
        calibrationEngine.addInstrument(SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1], METHOD_SABR_PHYS);
        calibrationEngine.calibrate(SABR_BUNDLE);
        // Cash pricing
        pvCashPayerHW[loopstrike][loopmr] = METHOD_HW_CASH.presentValue(SWAPTION_CASH_PAYER[loopstrike], objective.getHwBundle()).getAmount();
        pvCashReceiHW[loopstrike][loopmr] = METHOD_HW_CASH.presentValue(SWAPTION_CASH_RECEI[loopstrike], objective.getHwBundle()).getAmount();
        diffCashPayerHW[loopstrike][loopmr] = PV_CASH_PAYER[loopstrike] - pvCashPayerHW[loopstrike][loopmr];
        diffCashReceiHW[loopstrike][loopmr] = PV_CASH_RECEI[loopstrike] - pvCashReceiHW[loopstrike][loopmr];
      }
    }
    double test = 0.0;
    test++;
  }

  @Test(enabled = true)
  /**
   * Computes the price of cash-settled swaptions in the linear Terminal Swap Rate model.
   */
  public void terminalSwapRate() {

    double[] pvCashPayerTSR = new double[NB_STRIKE];
    double[] pvCashReceiTSR = new double[NB_STRIKE];
    double[] diffCashPayerTSR = new double[NB_STRIKE];
    double[] diffCashReceiTSR = new double[NB_STRIKE];

    for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
      pvCashPayerTSR[loopstrike] = METHOD_TSR_CASH.presentValue(SWAPTION_CASH_PAYER[loopstrike], SABR_BUNDLE).getAmount();
      pvCashReceiTSR[loopstrike] = METHOD_TSR_CASH.presentValue(SWAPTION_CASH_RECEI[loopstrike], SABR_BUNDLE).getAmount();
      diffCashPayerTSR[loopstrike] = PV_CASH_PAYER[loopstrike] - pvCashPayerTSR[loopstrike];
      diffCashReceiTSR[loopstrike] = PV_CASH_RECEI[loopstrike] - pvCashReceiTSR[loopstrike];
    }

    double test = 0.0;
    test++;
  }

  @Test(enabled = true)
  /**
   * Computes the price of cash-settled swaptions in the G2++ model with different correlations.
   */
  public void g2pp() {

    double[] meanReversion = new double[] {0.01, 0.30};
    double ratio = 4.0;
    double[] correlation = new double[] {-0.90, -0.45, 0, 0.45, 0.90};
    int nbCorrelation = correlation.length;

    double[][] pvCashPayerG2 = new double[NB_STRIKE][nbCorrelation];
    double[][] pvCashReceiG2 = new double[NB_STRIKE][nbCorrelation];
    double[][] diffCashPayerG2 = new double[NB_STRIKE][nbCorrelation];
    double[][] diffCashReceiG2 = new double[NB_STRIKE][nbCorrelation];

    for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
      for (int loopcor = 0; loopcor < nbCorrelation; loopcor++) {
        // Calibration
        G2ppPiecewiseConstantParameters g2Parameters = new G2ppPiecewiseConstantParameters(meanReversion, new double[][] { {0.01}, {0.01 / ratio}}, new double[0], correlation[loopcor]);
        SwaptionPhysicalG2ppCalibrationObjective objective = new SwaptionPhysicalG2ppCalibrationObjective(g2Parameters, ratio);
        SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalG2ppSuccessiveRootFinderCalibrationEngine(objective);
        calibrationEngine.addInstrument(SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1], METHOD_SABR_PHYS);
        calibrationEngine.calibrate(SABR_BUNDLE);
        // Cash pricing
        pvCashPayerG2[loopstrike][loopcor] = METHOD_G2_CASH.presentValue(SWAPTION_CASH_PAYER[loopstrike], objective.getG2Bundle()).getAmount();
        pvCashReceiG2[loopstrike][loopcor] = METHOD_G2_CASH.presentValue(SWAPTION_CASH_RECEI[loopstrike], objective.getG2Bundle()).getAmount();
        diffCashPayerG2[loopstrike][loopcor] = PV_CASH_PAYER[loopstrike] - pvCashPayerG2[loopstrike][loopcor];
        diffCashReceiG2[loopstrike][loopcor] = PV_CASH_RECEI[loopstrike] - pvCashReceiG2[loopstrike][loopcor];
      }
    }

    double test = 0.0;
    test++;
  }

  @Test(enabled = false)
  /**
   * Computes the price of cash-settled swaptions in the LMM.
   */
  public void lmmLastMaturity() {
    YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAMES[0]);
    int nbPath = 1250000;
    LiborMarketModelMonteCarloMethod methodLmmMC;

    double[] angle = new double[] {0, Math.PI / 4, Math.PI / 2, 3 * Math.PI / 4, 4 * Math.PI / 4};
    int nbAngle = angle.length;
    double[][] pvCashPayerLMM = new double[NB_STRIKE][nbAngle];
    double[][] pvCashReceiLMM = new double[NB_STRIKE][nbAngle];
    double[][] diffCashPayerLMM = new double[NB_STRIKE][nbAngle];
    double[][] diffCashReceiLMM = new double[NB_STRIKE][nbAngle];

    for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
      for (int loopangle = 0; loopangle < nbAngle; loopangle++) {
        // Calibration
        LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParametersAngle(ANALYSIS_DATE,
            SWAP_PAYER_DEFINITION[loopstrike][SWAP_TENOR - 1], angle[loopangle]);
        SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
        SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
        calibrationEngine.addInstrument(SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1], METHOD_SABR_PHYS);
        calibrationEngine.calibrate(SABR_BUNDLE);
        assertEquals("Swaption cash - LMM calibration", PV_PHYS_PAYER[loopstrike][SWAP_TENOR - 1],
            METHOD_LMM_PHYS.presentValue(SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1], objective.getLmmBundle()).getAmount(), 1E-2);
        // Cash pricing
        methodLmmMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), nbPath);
        pvCashPayerLMM[loopstrike][loopangle] = methodLmmMC.presentValue(SWAPTION_CASH_PAYER[loopstrike], CUR, dsc, objective.getLmmBundle()).getAmount();
        methodLmmMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), nbPath);
        pvCashReceiLMM[loopstrike][loopangle] = methodLmmMC.presentValue(SWAPTION_CASH_RECEI[loopstrike], CUR, dsc, objective.getLmmBundle()).getAmount();
        diffCashPayerLMM[loopstrike][loopangle] = PV_CASH_PAYER[loopstrike] - pvCashPayerLMM[loopstrike][loopangle];
        diffCashReceiLMM[loopstrike][loopangle] = PV_CASH_RECEI[loopstrike] - pvCashReceiLMM[loopstrike][loopangle];
      }
    }

    double test = 0.0;
    test++;

  }

  @Test(enabled = false)
  /**
   * Computes the price of cash-settled swaptions in the LMM.
   */
  public void lmmDisplacement() {
    YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAMES[0]);
    int nbPath = 1250000;
    LiborMarketModelMonteCarloMethod methodLmmMC;

    double angle = Math.PI / 2;
    double[] displacement = new double[] {0.05, 0.10, 1.00};
    int nbDis = displacement.length;
    double[][] pvCashPayerLMM = new double[NB_STRIKE][nbDis];
    double[][] pvCashReceiLMM = new double[NB_STRIKE][nbDis];
    double[][] diffCashPayerLMM = new double[NB_STRIKE][nbDis];
    double[][] diffCashReceiLMM = new double[NB_STRIKE][nbDis];

    for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
      for (int loopdis = 0; loopdis < nbDis; loopdis++) {
        // Calibration
        LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParametersDisplacementAngle(ANALYSIS_DATE,
            SWAP_PAYER_DEFINITION[loopstrike][SWAP_TENOR - 1], displacement[loopdis], angle);
        SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
        SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
        calibrationEngine.addInstrument(SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1], METHOD_SABR_PHYS);
        calibrationEngine.calibrate(SABR_BUNDLE);
        assertEquals("Swaption cash - LMM calibration", PV_PHYS_PAYER[loopstrike][SWAP_TENOR - 1],
            METHOD_LMM_PHYS.presentValue(SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1], objective.getLmmBundle()).getAmount(), 1E-2);
        // Cash pricing
        methodLmmMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), nbPath);
        pvCashPayerLMM[loopstrike][loopdis] = methodLmmMC.presentValue(SWAPTION_CASH_PAYER[loopstrike], CUR, dsc, objective.getLmmBundle()).getAmount();
        methodLmmMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), nbPath);
        pvCashReceiLMM[loopstrike][loopdis] = methodLmmMC.presentValue(SWAPTION_CASH_RECEI[loopstrike], CUR, dsc, objective.getLmmBundle()).getAmount();
        diffCashPayerLMM[loopstrike][loopdis] = PV_CASH_PAYER[loopstrike] - pvCashPayerLMM[loopstrike][loopdis];
        diffCashReceiLMM[loopstrike][loopdis] = PV_CASH_RECEI[loopstrike] - pvCashReceiLMM[loopstrike][loopdis];
      }
    }

    double test = 0.0;
    test++;

  }

  @Test(enabled = false)
  /**
   * Computes the price of cash-settled swaptions in the LMM.
   */
  public void lmmTwoMaturities() {
    int calibrationSwaption = 4;
    YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAMES[0]);
    int nbPath = 1250000;
    LiborMarketModelMonteCarloMethod methodLmmMC;

    double[] angle = new double[] {0, Math.PI / 4, Math.PI / 2, 3 * Math.PI / 4, 4 * Math.PI / 4};
    int nbAngle = angle.length;
    double[][] pvCashPayerLMM = new double[NB_STRIKE][nbAngle];
    double[][] pvCashReceiLMM = new double[NB_STRIKE][nbAngle];
    double[][] diffCashPayerLMM = new double[NB_STRIKE][nbAngle];
    double[][] diffCashReceiLMM = new double[NB_STRIKE][nbAngle];

    for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
      for (int loopangle = 0; loopangle < nbAngle; loopangle++) {
        // Calibration
        LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParametersAngle(ANALYSIS_DATE,
            SWAP_PAYER_DEFINITION[loopstrike][SWAP_TENOR - 1], angle[loopangle]);
        SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
        SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
        calibrationEngine.addInstrument(SWAPTION_PHYS_PAYER[loopstrike][calibrationSwaption], METHOD_SABR_PHYS);
        calibrationEngine.addInstrument(SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1], METHOD_SABR_PHYS);
        calibrationEngine.calibrate(SABR_BUNDLE);
        assertEquals("Swaption cash - LMM calibration", PV_PHYS_PAYER[loopstrike][calibrationSwaption],
            METHOD_LMM_PHYS.presentValue(SWAPTION_PHYS_PAYER[loopstrike][calibrationSwaption], objective.getLmmBundle()).getAmount(), 1E-2);
        assertEquals("Swaption cash - LMM calibration", PV_PHYS_PAYER[loopstrike][SWAP_TENOR - 1],
            METHOD_LMM_PHYS.presentValue(SWAPTION_PHYS_PAYER[loopstrike][SWAP_TENOR - 1], objective.getLmmBundle()).getAmount(), 1E-2);
        // Cash pricing
        methodLmmMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), nbPath);
        pvCashPayerLMM[loopstrike][loopangle] = methodLmmMC.presentValue(SWAPTION_CASH_PAYER[loopstrike], CUR, dsc, objective.getLmmBundle()).getAmount();
        methodLmmMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), nbPath);
        pvCashReceiLMM[loopstrike][loopangle] = methodLmmMC.presentValue(SWAPTION_CASH_RECEI[loopstrike], CUR, dsc, objective.getLmmBundle()).getAmount();
        diffCashPayerLMM[loopstrike][loopangle] = PV_CASH_PAYER[loopstrike] - pvCashPayerLMM[loopstrike][loopangle];
        diffCashReceiLMM[loopstrike][loopangle] = PV_CASH_RECEI[loopstrike] - pvCashReceiLMM[loopstrike][loopangle];
      }
    }

    double test = 0.0;
    test++;

  }

  @Test(enabled = false)
  /**
   * Computes the price of cash-settled swaptions in the LMM.
   */
  public void lmmAllMaturities() {
    YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAMES[0]);
    int nbPath = 1250;
    LiborMarketModelMonteCarloMethod methodLmmMC;

    double[] angle = new double[] {0, Math.PI / 4, Math.PI / 2, 3 * Math.PI / 4, 4 * Math.PI / 4};
    int nbAngle = angle.length;
    double[][] pvCashPayerLMM = new double[NB_STRIKE][nbAngle];
    double[][] pvCashReceiLMM = new double[NB_STRIKE][nbAngle];
    double[][] diffCashPayerLMM = new double[NB_STRIKE][nbAngle];
    double[][] diffCashReceiLMM = new double[NB_STRIKE][nbAngle];

    for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
      for (int loopangle = 0; loopangle < nbAngle; loopangle++) {
        // Calibration
        LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParametersAngle(ANALYSIS_DATE,
            SWAP_PAYER_DEFINITION[loopstrike][SWAP_TENOR - 1], angle[loopangle]);
        SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
        SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
        for (int loopmat = 0; loopmat < SWAP_TENOR; loopmat++) {
          calibrationEngine.addInstrument(SWAPTION_PHYS_PAYER[loopstrike][loopmat], METHOD_SABR_PHYS);
        }
        calibrationEngine.calibrate(SABR_BUNDLE);
        for (int loopmat = 0; loopmat < SWAP_TENOR; loopmat++) {
          assertEquals("Swaption cash - LMM calibration", PV_PHYS_PAYER[loopstrike][loopmat], METHOD_LMM_PHYS.presentValue(SWAPTION_PHYS_PAYER[loopstrike][loopmat], objective.getLmmBundle())
              .getAmount(), 1E-2);
        }
        // Cash pricing
        methodLmmMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), nbPath);
        pvCashPayerLMM[loopstrike][loopangle] = methodLmmMC.presentValue(SWAPTION_CASH_PAYER[loopstrike], CUR, dsc, objective.getLmmBundle()).getAmount();
        methodLmmMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), nbPath);
        pvCashReceiLMM[loopstrike][loopangle] = methodLmmMC.presentValue(SWAPTION_CASH_RECEI[loopstrike], CUR, dsc, objective.getLmmBundle()).getAmount();
        diffCashPayerLMM[loopstrike][loopangle] = PV_CASH_PAYER[loopstrike] - pvCashPayerLMM[loopstrike][loopangle];
        diffCashReceiLMM[loopstrike][loopangle] = PV_CASH_RECEI[loopstrike] - pvCashReceiLMM[loopstrike][loopangle];
      }
    }

    double test = 0.0;
    test++;

  }

  @Test(enabled = false)
  public void expiry() {
    Period[] period = new Period[] {Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofMonths(12), Period.ofMonths(18), Period.ofYears(2),
        Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10), Period.ofYears(12),
        Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30)};
    ZonedDateTime date2010Sep30 = DateUtils.getUTCDate(2010, 9, 30);
    ZonedDateTime[] expiry = new ZonedDateTime[period.length];
    double[] expiryTime = new double[period.length];
    for (int loopexp = 0; loopexp < period.length; loopexp++) {
      expiry[loopexp] = ScheduleCalculator.getAdjustedDate(date2010Sep30, BUSINESS_DAY, CALENDAR, IS_EOM, period[loopexp]);
      expiryTime[loopexp] = TimeCalculator.getTimeBetween(date2010Sep30, expiry[loopexp]);
    }

    double test = 0.0;
    test++;
  }

}
