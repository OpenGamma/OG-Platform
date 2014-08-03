/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorFRA;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborON;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.hullwhite.HullWhiteProviderDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;

/**
 * Generators and indexes used in curve calibration tests.
 */
public class CurveCalibrationConventionDataSets {

  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;
  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY_MC =
      new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);
  private static final HullWhiteProviderDiscountBuildingRepository CURVE_BUILDING_REPOSITORY_HW =
      new HullWhiteProviderDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  public static MulticurveDiscountBuildingRepository curveBuildingRepositoryMulticurve() {
    return CURVE_BUILDING_REPOSITORY_MC;
  }

  public static HullWhiteProviderDiscountBuildingRepository curveBuildingRepositoryHullWhite() {
    return CURVE_BUILDING_REPOSITORY_HW;
  }

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_DQ = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_NCS = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_CCS = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.CLAMPED_CUBIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  //  private static final Interpolator1D INTERPOLATOR_LL = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR,
  //      Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR); // Log-linear on the discount factor = step on the instantaneous rates

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final GeneratorYDCurve GENERATOR_YD_MAT_LIN = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
  private static final GeneratorYDCurve GENERATOR_YD_MAT_DQ = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_DQ);
  private static final GeneratorYDCurve GENERATOR_YD_MAT_NCS = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_NCS);
  private static final GeneratorYDCurve GENERATOR_YD_MAT_CCS = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_CCS);

  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapIborIborMaster GENERATOR_BS_MASTER = GeneratorSwapIborIborMaster.getInstance();

  /** EUR **/
  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final Currency EUR = Currency.EUR;
  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  private static final IndexON EONIA = GENERATOR_OIS_EUR.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_EUR = new GeneratorDepositON("EUR Deposit ON", EUR, TARGET, EONIA.getDayCount());
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_IRS_MASTER.getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_IRS_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final IborIndex EURIBOR3M = EUR1YEURIBOR3M.getIborIndex();
  private static final IborIndex EURIBOR6M = EUR1YEURIBOR6M.getIborIndex();
  private static final GeneratorFRA GENERATOR_FRA_3M_EUR = new GeneratorFRA("GENERATOR_FRA_3M", EURIBOR3M, TARGET);
  private static final GeneratorFRA GENERATOR_FRA_6M_EUR = new GeneratorFRA("GENERATOR_FRA_6M", EURIBOR6M, TARGET);
  private static final GeneratorDepositIbor GENERATOR_EURIBOR3M = new GeneratorDepositIbor("GENERATOR_EURIBOR3M", EURIBOR3M, TARGET);
  private static final GeneratorDepositIbor GENERATOR_EURIBOR6M = new GeneratorDepositIbor("GENERATOR_EURIBOR6M", EURIBOR6M, TARGET);

  @SuppressWarnings("unchecked")
  public static GeneratorInstrument<? extends GeneratorAttribute>[] generatorEurOnOis(int nbDepositON, int nbOis) {
    GeneratorInstrument<? extends GeneratorAttribute>[] generator = new GeneratorInstrument[nbDepositON + nbOis];
    for (int loopdepo = 0; loopdepo < nbDepositON; loopdepo++) {
      generator[loopdepo] = GENERATOR_DEPOSIT_ON_EUR;
    }
    for (int loopois = 0; loopois < nbOis; loopois++) {
      generator[nbDepositON + loopois] = GENERATOR_OIS_EUR;
    }
    return generator;
  }

  @SuppressWarnings("unchecked")
  public static GeneratorInstrument<? extends GeneratorAttribute>[] generatorEurIbor6Fra6Irs6(int nbIbor, int nbFra, int nbIrs) {
    GeneratorInstrument<? extends GeneratorAttribute>[] generator = new GeneratorInstrument[nbIbor + nbFra + nbIrs];
    for (int loopibor = 0; loopibor < nbIbor; loopibor++) {
      generator[loopibor] = GENERATOR_EURIBOR6M;
    }
    for (int loopfra = 0; loopfra < nbFra; loopfra++) {
      generator[nbIbor + loopfra] = GENERATOR_FRA_6M_EUR;
    }
    for (int loopirs = 0; loopirs < nbIrs; loopirs++) {
      generator[nbIbor + nbFra + loopirs] = EUR1YEURIBOR6M;
    }
    return generator;
  }

  @SuppressWarnings("unchecked")
  public static GeneratorInstrument<? extends GeneratorAttribute>[] generatorEurIbor3Fra3Irs3(int nbIbor, int nbFra, int nbIrs) {
    GeneratorInstrument<? extends GeneratorAttribute>[] generator = new GeneratorInstrument[nbIbor + nbFra + nbIrs];
    for (int loopibor = 0; loopibor < nbIbor; loopibor++) {
      generator[loopibor] = GENERATOR_EURIBOR3M;
    }
    for (int loopfra = 0; loopfra < nbFra; loopfra++) {
      generator[nbIbor + loopfra] = GENERATOR_FRA_3M_EUR;
    }
    for (int loopirs = 0; loopirs < nbIrs; loopirs++) {
      generator[nbIbor + nbFra + loopirs] = EUR1YEURIBOR3M;
    }
    return generator;
  }
  
  /** JPY **/
  private static final Calendar TYO = new MondayToFridayCalendar("TYO");
  private static final Currency JPY = Currency.JPY;
  private static final GeneratorSwapFixedON GENERATOR_OIS_JPY = GeneratorSwapFixedONMaster.getInstance().getGenerator("JPY1YTONAR", TYO);
  private static final IndexON TONAR = GENERATOR_OIS_JPY.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_JPY = new GeneratorDepositON("JPY Deposit ON", JPY, TYO, TONAR.getDayCount());
  private static final GeneratorSwapFixedIbor JPY6MLIBOR6M = GENERATOR_IRS_MASTER.getGenerator("JPY6MLIBOR6M", TYO);
  private static final GeneratorSwapIborIbor JPYLIBOR3MLIBOR6M = GENERATOR_BS_MASTER.getGenerator("JPYLIBOR3MLIBOR6M", TYO);
  private static final IborIndex JPYLIBOR3M = JPYLIBOR3MLIBOR6M.getIborIndex1();
  private static final IborIndex JPYLIBOR6M = JPY6MLIBOR6M.getIborIndex();
  private static final GeneratorFRA GENERATOR_FRA_3M_JPY = new GeneratorFRA("GENERATOR_FRA_3M", JPYLIBOR3M, TYO);
  private static final GeneratorFRA GENERATOR_FRA_6M_JPY = new GeneratorFRA("GENERATOR_FRA_6M", JPYLIBOR6M, TYO);
  private static final GeneratorDepositIbor GENERATOR_JPYLIBOR3M = new GeneratorDepositIbor("GENERATOR_JPYLIBOR3M", JPYLIBOR3M, TYO);
  private static final GeneratorDepositIbor GENERATOR_JPYLIBOR6M = new GeneratorDepositIbor("GENERATOR_JPYLIBOR6M", JPYLIBOR6M, TYO);

  @SuppressWarnings("unchecked")
  public static GeneratorInstrument<? extends GeneratorAttribute>[] generatorJpyOnOis(int nbDepositON, int nbOis) {
    GeneratorInstrument<? extends GeneratorAttribute>[] generator = new GeneratorInstrument[nbDepositON + nbOis];
    for (int loopdepo = 0; loopdepo < nbDepositON; loopdepo++) {
      generator[loopdepo] = GENERATOR_DEPOSIT_ON_JPY;
    }
    for (int loopois = 0; loopois < nbOis; loopois++) {
      generator[nbDepositON + loopois] = GENERATOR_OIS_JPY;
    }
    return generator;
  }

  @SuppressWarnings("unchecked")
  public static GeneratorInstrument<? extends GeneratorAttribute>[] generatorJpyIbor6Fra6Irs6(int nbIbor, int nbFra, int nbIrs) {
    GeneratorInstrument<? extends GeneratorAttribute>[] generator = new GeneratorInstrument[nbIbor + nbFra + nbIrs];
    for (int loopibor = 0; loopibor < nbIbor; loopibor++) {
      generator[loopibor] = GENERATOR_JPYLIBOR6M;
    }
    for (int loopfra = 0; loopfra < nbFra; loopfra++) {
      generator[nbIbor + loopfra] = GENERATOR_FRA_6M_JPY;
    }
    for (int loopirs = 0; loopirs < nbIrs; loopirs++) {
      generator[nbIbor + nbFra + loopirs] = JPY6MLIBOR6M;
    }
    return generator;
  }

  @SuppressWarnings("unchecked")
  public static GeneratorInstrument<? extends GeneratorAttribute>[] generatorJpyIbor3Fra3Bs3(int nbIbor, int nbFra, int nbBs) {
    GeneratorInstrument<? extends GeneratorAttribute>[] generator = new GeneratorInstrument[nbIbor + nbFra + nbBs];
    for (int loopibor = 0; loopibor < nbIbor; loopibor++) {
      generator[loopibor] = GENERATOR_JPYLIBOR3M;
    }
    for (int loopfra = 0; loopfra < nbFra; loopfra++) {
      generator[nbIbor + loopfra] = GENERATOR_FRA_3M_JPY;
    }
    for (int loopirs = 0; loopirs < nbBs; loopirs++) {
      generator[nbIbor + nbFra + loopirs] = JPYLIBOR3MLIBOR6M;
    }
    return generator;
  }

  /** USD **/
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final Currency USD = Currency.USD;
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON INDEX_FEDFUND_USD = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", USD, NYC, INDEX_FEDFUND_USD.getDayCount());
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USDLIBOR3M, NYC);
  private static final GeneratorFRA GENERATOR_FRA_3M_USD = new GeneratorFRA("GENERATOR USD FRA 3M", USDLIBOR3M, NYC);
  private static final GeneratorSwapIborON GENERATOR_USDLIBOR3M_FF = new GeneratorSwapIborON("USD Fed Fund Swap", USDLIBOR3M,
      INDEX_FEDFUND_USD, USDLIBOR3M.getBusinessDayConvention(), USDLIBOR3M.isEndOfMonth(), 2, 0, NYC, NYC);

  @SuppressWarnings("unchecked")
  public static GeneratorInstrument<? extends GeneratorAttribute>[] generatorUsdOnOisFfs(int nbDepositON, int nbOis, int nbFF) {
    GeneratorInstrument<? extends GeneratorAttribute>[] generator = new GeneratorInstrument[nbDepositON + nbOis + nbFF];
    for (int loopdepo = 0; loopdepo < nbDepositON; loopdepo++) {
      generator[loopdepo] = GENERATOR_DEPOSIT_ON_USD;
    }
    for (int loopois = 0; loopois < nbOis; loopois++) {
      generator[nbDepositON + loopois] = GENERATOR_OIS_USD;
    }
    for (int loopff = 0; loopff < nbFF; loopff++) {
      generator[nbDepositON + nbOis + loopff] = GENERATOR_USDLIBOR3M_FF;
    }
    return generator;
  }

  @SuppressWarnings("unchecked")
  public static GeneratorInstrument<? extends GeneratorAttribute>[] generatorUsdIbor3Fra3Irs3(int nbIbor, int nbFra, int nbIrs) {
    GeneratorInstrument<? extends GeneratorAttribute>[] generator = new GeneratorInstrument[nbIbor + nbFra + nbIrs];
    for (int loopibor = 0; loopibor < nbIbor; loopibor++) {
      generator[loopibor] = GENERATOR_USDLIBOR3M;
    }
    for (int loopfra = 0; loopfra < nbFra; loopfra++) {
      generator[nbIbor + loopfra] = GENERATOR_FRA_3M_USD;
    }
    for (int loopirs = 0; loopirs < nbIrs; loopirs++) {
      generator[nbIbor + nbFra + loopirs] = USD6MLIBOR3M;
    }
    return generator;
  }

  /**
   * Returns a Yield and discount curve generator based on node computed from the maturity calculator and linear interpolation.
   * The extrapolation is flat.
   * @return The generator.
   */
  public static GeneratorYDCurve generatorYDMatLin() {
    return GENERATOR_YD_MAT_LIN;
  }

  /**
   * Returns a Yield and discount curve generator based on node computed from the maturity calculator and double quadratic interpolation.
   * The extrapolation is flat.
   * @return The generator.
   */
  public static GeneratorYDCurve generatorYDMatDq() {
    return GENERATOR_YD_MAT_DQ;
  }

  /**
   * Returns a Yield and discount curve generator based on node computed from the maturity calculator and natural cubic spline interpolation.
   * Natural cubic spline has 0 second derivative at each extreme of the interpolation range. The extrapolation is flat.
   * @return The generator.
   */
  public static GeneratorYDCurve generatorYDMatNcs() {
    return GENERATOR_YD_MAT_NCS;
  }

  /**
   * Returns a Yield and discount curve generator based on node computed from the maturity calculator and linear interpolation.
   * Clamp cubic spline has 0 first derivative at each extreme of the interpolation range. The extrapolation is flat.
   * @return The generator.
   */
  public static GeneratorYDCurve generatorYDMatCcs() {
    return GENERATOR_YD_MAT_CCS;
  }

}
