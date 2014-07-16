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
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborON;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
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
  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY =
      new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  public static MulticurveDiscountBuildingRepository curveBuildingRepository() {
    return CURVE_BUILDING_REPOSITORY;
  }

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_LL = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR,
      Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR); // Log-linear on the discount factor = step on the instantaneous rates

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final GeneratorYDCurve GENERATOR_YD_MAT_LIN = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);

  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();

  /** EUR **/
  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final Currency EUR = Currency.EUR;
  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  private static final IndexON EONIA = GENERATOR_OIS_EUR.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_EUR = new GeneratorDepositON("EUR Deposit ON", EUR, TARGET, EONIA.getDayCount());
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final IborIndex EURIBOR6M = EUR1YEURIBOR6M.getIborIndex();
  private static final GeneratorFRA GENERATOR_FRA_6M = new GeneratorFRA("GENERATOR_FRA_6M", EURIBOR6M, TARGET);
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
      generator[nbIbor + loopfra] = GENERATOR_FRA_6M;
    }
    for (int loopirs = 0; loopirs < nbIrs; loopirs++) {
      generator[nbIbor + nbFra + loopirs] = EUR1YEURIBOR6M;
    }
    return generator;
  }

  /** USD **/
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final Currency USD = Currency.USD;
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON INDEX_FEDFUND_USD = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", USD, NYC, INDEX_FEDFUND_USD.getDayCount());
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USDLIBOR3M, NYC);
  private static final GeneratorSwapIborON GENERATOR_USDLIBOR3M_FF = new GeneratorSwapIborON("USD Fed Fund Swap", USDLIBOR3M,
      INDEX_FEDFUND_USD, USDLIBOR3M.getBusinessDayConvention(), USDLIBOR3M.isEndOfMonth(), 2, 0, NYC, NYC);

  @SuppressWarnings("unchecked")
  public static GeneratorInstrument<? extends GeneratorAttribute>[] generatorUsdOnOis(int nbDepositON, int nbOis, int nbFF) {
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
  public static GeneratorInstrument<? extends GeneratorAttribute>[] generatorUsdIbor3Irs3(int nbIbor, int nbIrs) {
    GeneratorInstrument<? extends GeneratorAttribute>[] generator = new GeneratorInstrument[nbIbor + nbIrs];
    for (int loopibor = 0; loopibor < nbIbor; loopibor++) {
      generator[loopibor] = GENERATOR_USDLIBOR3M;
    }
    for (int loopirs = 0; loopirs < nbIrs; loopirs++) {
      generator[nbIbor + loopirs] = USD6MLIBOR3M;
    }
    return generator;
  }

  public static GeneratorYDCurve generatorYDMatLin() {
    return GENERATOR_YD_MAT_LIN;
  }

}
