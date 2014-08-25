/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.datasets;

import java.util.LinkedHashMap;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadRateCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Curves calibration in USD: 
 * ONDSC-OIS/LIBOR3M-FRAIRS/LIBOR1M-BS/LIBOR6M-BS
 * Recent market data. Standard instruments with futures on LIBOR3M.
 */
public class UsdDatasetAug21 {

  public static final Interpolator1D INTERPOLATOR_LINEAR = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  public static final Interpolator1D INTERPOLATOR_LOG_LINEAR = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, 
          Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR,  
          Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final Currency USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);

  private static final double NOTIONAL = 1.0;

  private static final IndexIborMaster IBOR_MASTER = IndexIborMaster.getInstance();
  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();

  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GENERATOR_OIS_MASTER.getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON USDFEDFUND = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final IborIndex USDLIBOR1M = IBOR_MASTER.getIndex("USDLIBOR1M");
  private static final IborIndex USDLIBOR6M = IBOR_MASTER.getIndex("USDLIBOR6M");

  private static final String CURVE_NAME_DSC_USD = "USD-DSCON-OIS";
  private static final String CURVE_NAME_FWD3_USD = "USD-LIBOR3M-FRAIRS";

  /** Units of curves */
  private static final int NB_UNITS = 2;
  private static final int NB_BLOCKS = 2;
  private static final GeneratorYDCurve[][] ZERO_RATE_LINEAR_INTERPOLATION_GENERATORS = 
      new GeneratorYDCurve[NB_BLOCKS][NB_UNITS];
  private static final GeneratorYDCurve[][] DISCOUNT_FACTOR_LOG_LINEAR_INTERPOLATION_GENERATORS = 
      new GeneratorYDCurve[NB_BLOCKS][NB_UNITS];
  private static final String[][] NAMES_UNITS = new String[NB_BLOCKS][NB_UNITS];
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    ZERO_RATE_LINEAR_INTERPOLATION_GENERATORS[0][0] = genIntLin;
    ZERO_RATE_LINEAR_INTERPOLATION_GENERATORS[0][1] = genIntLin;
    ZERO_RATE_LINEAR_INTERPOLATION_GENERATORS[1][0] = genIntLin;
    ZERO_RATE_LINEAR_INTERPOLATION_GENERATORS[1][1] = genIntLin;

    final GeneratorYDCurve logLinInterpolationGenerator = 
        new GeneratorCurveDiscountFactorInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LOG_LINEAR);
    DISCOUNT_FACTOR_LOG_LINEAR_INTERPOLATION_GENERATORS[0][0] = logLinInterpolationGenerator;
    DISCOUNT_FACTOR_LOG_LINEAR_INTERPOLATION_GENERATORS[0][1] = logLinInterpolationGenerator;
    DISCOUNT_FACTOR_LOG_LINEAR_INTERPOLATION_GENERATORS[1][0] = logLinInterpolationGenerator;
    DISCOUNT_FACTOR_LOG_LINEAR_INTERPOLATION_GENERATORS[1][1] = logLinInterpolationGenerator;
 
    NAMES_UNITS[0][0] = CURVE_NAME_DSC_USD;
    NAMES_UNITS[0][1] = CURVE_NAME_FWD3_USD;
    NAMES_UNITS[1][0] = CURVE_NAME_FWD3_USD;
    NAMES_UNITS[1][1] = CURVE_NAME_DSC_USD;
    DSC_MAP.put(CURVE_NAME_DSC_USD, USD);
    FWD_ON_MAP.put(CURVE_NAME_DSC_USD, new IndexON[] {USDFEDFUND });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_USD, new IborIndex[] {USDLIBOR3M });
    FWD_IBOR_MAP.put(CURVE_NAME_DSC_USD, new IborIndex[] {USDLIBOR6M });
  }
  
  public static ZonedDateTime[] s_startDates = new ZonedDateTime[] {
      DateUtils.getUTCDate(2014, 9, 25),
      DateUtils.getUTCDate(2014, 10, 27),
      DateUtils.getUTCDate(2014, 11, 25),
      DateUtils.getUTCDate(2014, 12, 29),
      DateUtils.getUTCDate(2015, 1, 26),
      DateUtils.getUTCDate(2015, 2, 25),
      DateUtils.getUTCDate(2015, 8, 25),
      DateUtils.getUTCDate(2016, 2, 25)
  };
  public static ZonedDateTime[] s_endDates = new ZonedDateTime[] {
      DateUtils.getUTCDate(2015, 3, 25),
      DateUtils.getUTCDate(2015, 4, 27),
      DateUtils.getUTCDate(2015, 5, 25),
      DateUtils.getUTCDate(2015, 6, 25),
      DateUtils.getUTCDate(2015, 7, 27),
      DateUtils.getUTCDate(2015, 8, 25),
      DateUtils.getUTCDate(2016, 2, 25),
      DateUtils.getUTCDate(2016, 8, 24)
  };
  
  static double[] s_fraQuotes = new double[] {
      0.024899, 
      0.023569,
      0.022321,
      0.021698,
      0.021365,
      0.021050,
      0.021380,
      0.022698 
  };

  public static InstrumentDefinition<?>[] getDefinitions() {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[s_fraQuotes.length];
    for (int i = 0; i < s_fraQuotes.length; ++i) {
      definitions[i] = ForwardRateAgreementDefinition.from(s_startDates[i], s_endDates[i], NOTIONAL, USDLIBOR6M, 
          s_fraQuotes[i], NYC);
    }
    return definitions;
  }

  /** Calculators */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> PSMQDC =
      ParSpreadMarketQuoteDiscountingCalculator.getInstance(); // Market quotes 
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> PSRDC =
      ParSpreadRateDiscountingCalculator.getInstance(); // Rate version of market quotes, in particular future price replaced by future rate sensitivity.
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> PSMQCSC =
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance(); // Market quotes 
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> PSRCSC =
      ParSpreadRateCurveSensitivityDiscountingCalculator.getInstance(); // Rate version of market quotes, in particular future price replaced by future rate sensitivity.

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY =
      CurveCalibrationConventionDataSets.curveBuildingRepositoryMulticurve();

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getFraCurve(
      final ZonedDateTime calibrationDate, boolean marketQuoteRisk, final Interpolator1D interpolator) {
    InstrumentDefinition<?>[][][] definitionsUnits = new InstrumentDefinition<?>[1][][];
    InstrumentDefinition<?>[] definitions = getDefinitions();
    
    definitionsUnits[0] = new InstrumentDefinition<?>[][] {definitions};
    
    InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> target;
    InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> targetSensitivity;
    
    if (marketQuoteRisk) {
      target = PSMQDC;
      targetSensitivity = PSMQCSC;
    } else {
      target = PSRDC;
      targetSensitivity = PSRCSC;
    }
  
    GeneratorYDCurve[][] generators = null;
    if(interpolator == INTERPOLATOR_LINEAR) {
      generators = ZERO_RATE_LINEAR_INTERPOLATION_GENERATORS;
    } else if (interpolator == INTERPOLATOR_LOG_LINEAR) {
      generators = DISCOUNT_FACTOR_LOG_LINEAR_INTERPOLATION_GENERATORS;
    }

    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(calibrationDate, definitionsUnits,
        generators, NAMES_UNITS, KNOWN_DATA, target, targetSensitivity, false, DSC_MAP, 
        FWD_ON_MAP, FWD_IBOR_MAP, CURVE_BUILDING_REPOSITORY, TS_FIXED_OIS_USD_WITH_TODAY, 
        TS_FIXED_OIS_USD_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITH_LAST, TS_FIXED_IBOR_USD3M_WITHOUT_LAST);
  }
  
  /**
   * Returns the array of Ibor index used in the curve data set. 
   * @return The array: USDLIBOR1M, USDLIBOR3M, USDLIBOR6M
   */
  public static IborIndex[] indexIborArrayUSDOisL1L3L6() {
    return new IborIndex[] {USDLIBOR1M, USDLIBOR3M, USDLIBOR6M };
  }

  /**
   * Returns the array of overnight index used in the curve data set. 
   * @return The array: USDFEDFUND 
   */
  public static IndexON[] indexONArray() {
    return new IndexON[] {USDFEDFUND };
  }

  /**
   * Returns the array of calendars used in the curve data set. 
   * @return The array: NYC 
   */
  public static Calendar[] calendarArray() {
    return new Calendar[] {NYC };
  }

  /**
   * Returns an array with one time series corresponding to the USD LIBOR3M fixing up to and including the last date.
   * @return
   */
  public static ZonedDateTimeDoubleTimeSeries fixingUsdLibor3MWithLast() {
    return TS_IBOR_USD3M_WITH_LAST;
  }

  /**
   * Returns an array with one time series corresponding to the USD LIBOR3M fixing up to and including the last date.
   * @return
   */
  public static ZonedDateTimeDoubleTimeSeries fixingUsdLibor3MWithoutLast() {
    return TS_IBOR_USD3M_WITHOUT_LAST;
  }

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27), DateUtils.getUTCDate(2011, 9, 28) },
      new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY };

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_LAST = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25),
        DateUtils.getUTCDate(2014, 7, 28) },
      new double[] {0.002318, 0.002346, 0.002321, 0.002331,
        0.002341, 0.002336, 0.002341, 0.002336, 0.002336,
        0.002326, 0.002331, 0.002336, 0.002336, 0.002316,
        0.002331, 0.002326, 0.002341, 0.002351, 0.002341,
        0.002341 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_LAST = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25) },
      new double[] {0.002318, 0.002346, 0.002321, 0.002331,
        0.002341, 0.002336, 0.002341, 0.002336, 0.002336,
        0.002326, 0.002331, 0.002336, 0.002336, 0.002316,
        0.002331, 0.002326, 0.002341, 0.002351, 0.002341 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITH_LAST = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITH_LAST };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITHOUT_LAST = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITHOUT_LAST };

}
