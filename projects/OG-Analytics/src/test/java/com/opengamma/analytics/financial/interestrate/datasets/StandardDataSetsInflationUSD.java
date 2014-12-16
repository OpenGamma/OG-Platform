/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurve;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurveInterpolatedAnchor;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurveMultiplyFixedCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.index.IndexPriceMaster;
import com.opengamma.analytics.financial.model.interestrate.curve.SeasonalCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastFixingEndTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.inflation.InflationDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.MultiplyCurveSpreadFunction;
import com.opengamma.analytics.math.curve.SpreadDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Curves calibration in USD: Dsc/ON and inflation curves.
 * 1) DSCON-OIS_HICP-ZC. Both curves calibrated in a single process using two units.
 * 2) DSCON-OIS_HICP-ZC. Inflation calibrated with the Multicurve of OIS externally provided (two step process).
 * 3) DSCON-OIS_HICP-ZC. Both curves calibrated in a single process using a single unit.
 * 4) DSCON-OIS_HICP-ZC. The inflation curve includes seasonality (monthly adjustment according to some 
 *    externally provided monthly multiplicative adjustments).
 * 5) DSCON-OIS_HICP-ZC. The inflation curve includes the already fixed price. As the standard in inflation is a three 
 *    months fixing offset, the current and past index levels may be used for some coupons and fixing the already known 
 *    part of the curve will impact the interpolation.
 * 6) DSCON-OIS_HICP-ZC. The seasonality adjustment and the known index parts of the previous two curves sets are
 *    combined in one curve.
 */
public class StandardDataSetsInflationUSD {

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final Currency USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);
  private static final MulticurveProviderDiscount KNOWN_DATA_MULTICURVE = new MulticurveProviderDiscount(FX_MATRIX);
  
  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();

  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GENERATOR_OIS_MASTER.getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON USDFEDFUND = GENERATOR_OIS_USD.getIndex();
  
  private static final String CURVE_NAME_USD_OIS = "USD-OIS";
  private static final String CURVE_NAME_CPI_USD = "USD-ZCHICP";
  
  private static final IndexPrice USCPI = IndexPriceMaster.getInstance().getIndex("USCPI");
  private static final GeneratorPriceIndexCurve GENERATOR_PI_FIX_LIN = 
      CurveCalibrationConventionDataSets.generatorPiFixLin();
  private static final GeneratorYDCurve GENERATOR_YD_MAT_LIN = 
      CurveCalibrationConventionDataSets.generatorYDMatLin();
  private static final Interpolator1D INTERPOLATOR_STEP_FLAT =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.STEP,
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_LINEAR = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final LastFixingEndTimeCalculator LAST_FIXING_END_CALCULATOR = LastFixingEndTimeCalculator.getInstance();
  public static final double[] SEASONAL_FACTORS = 
	    {1.00, 1.00, 1.0, 1, 1, 1, 1, 1, 1, 1, 1 };
//    {1.005, 1.001, 1.01, .999, .998, .9997, 1.004, 1.006, .994, .993, .9991 };
  
  /** Market values for the dsc USD curve */
  private static final double[] OIS_MARKET_QUOTES = new double[] {0.0016, 0.0016,
    0.00072000, 0.00082000, 0.00093000, 0.00090000, 0.00105000,
    0.00118500, 0.00318650, 0.00704000, 0.01121500, 0.01515000,
    0.01845500, 0.02111000, 0.02332000, 0.02513500, 0.02668500 }; //17
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_1_USD_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorUsdOnOisFfs(2, 15, 0);
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_1_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(10), Period.ofYears(20), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] DSC_1_USD_ATTR = new GeneratorAttributeIR[DSC_1_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      DSC_1_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_1_USD_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = 2; loopins < DSC_1_USD_TENOR.length; loopins++) {
      DSC_1_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_1_USD_TENOR[loopins]);
    }
  } 
  
  /** Market values for the HICP USD curve */ /** USSWITx Interpolation 3M lag */
  public static final double[] CPI_USD_MARKET_QUOTES = 
      new double[] {0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 
    0.0200, 0.0200, 0.0200, 0.0200, 0.0200 };
  /** Generators for the HICP USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] HICP_USD_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorUsdCpi(15);
  /** Tenors for the HICP USD curve */
  private static final Period[] HICP_USD_TENOR = new Period[] {
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), 
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10), 
    Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] HICP_USD_ATTR = new GeneratorAttributeIR[HICP_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < HICP_USD_TENOR.length; loopins++) {
      HICP_USD_ATTR[loopins] = new GeneratorAttributeIR(HICP_USD_TENOR[loopins]);
    }
  }
  
  /** Map of index/curves */
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexPrice[]> USD_HICP_MAP = new LinkedHashMap<>();

  static {
    DSC_MAP.put(CURVE_NAME_USD_OIS, USD);
    FWD_ON_MAP.put(CURVE_NAME_USD_OIS, new IndexON[] {USDFEDFUND });
    USD_HICP_MAP.put(CURVE_NAME_CPI_USD, new IndexPrice[] {USCPI });
  }
  
  /** Calculators */
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = 
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC =
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParSpreadInflationMarketQuoteDiscountingCalculator PSIMQC = 
      ParSpreadInflationMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator PSIMQCSDC =
      ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY_MULTICURVE = 
      CurveCalibrationConventionDataSets.curveBuildingRepositoryMulticurve();
  private static final InflationDiscountBuildingRepository CURVE_BUILDING_REPOSITORY_INFLATION = 
      CurveCalibrationConventionDataSets.curveBuildingRepositoryInflation();
  
  /**
   * Returns a set of calibrated curve: dsc/on with OIS and US CPI with zero-coupon swaps.
   * The curves are calibrated as two units in a unique calibration.
   * @param calibrationDate The calibration date.
   * @return  The calibrated curves and Jacobians.
   */
  public static Pair<InflationProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdOisUsCpi(
      ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[] oisDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        OIS_MARKET_QUOTES, DSC_1_USD_GENERATORS, DSC_1_USD_ATTR);
    InstrumentDefinition<?>[] inflDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        CPI_USD_MARKET_QUOTES, HICP_USD_GENERATORS, HICP_USD_ATTR);
    InstrumentDefinition<?>[][][] unitDefinition = new InstrumentDefinition<?>[][][] {{oisDefinition}, {inflDefinition}};
    GeneratorCurve[][] generator = new GeneratorCurve[][] {{GENERATOR_YD_MAT_LIN}, {GENERATOR_PI_FIX_LIN}};
    String[][] namesCurves = new String[][] {{CURVE_NAME_USD_OIS}, {CURVE_NAME_CPI_USD}};
    Map<IndexON, ZonedDateTimeDoubleTimeSeries> htsOn = getOnHts(calibrationDate, false);
    InflationProviderDiscount knownDataInflation = new InflationProviderDiscount(FX_MATRIX);
    Pair<InflationProviderDiscount, CurveBuildingBlockBundle> multicurveInflation = 
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsInflation(calibrationDate, unitDefinition, 
            generator, namesCurves, knownDataInflation, new CurveBuildingBlockBundle(), PSIMQC, PSIMQCSDC, DSC_MAP,
            FWD_ON_MAP, FWD_IBOR_MAP, USD_HICP_MAP, 
            CURVE_BUILDING_REPOSITORY_INFLATION, htsOn, HTS_IBOR, getCpiHts(calibrationDate));
    return multicurveInflation;
  }
  
  /**
   * Returns a set of calibrated curve: dsc/on with OIS and US CPI with zero-coupon swaps.
   * The curves are calibrated using two successive calibrations; one for the discounting curve and one for the inflation.
   * @param calibrationDate The calibration date.
   * @return  The calibrated curves and Jacobians.
   */
  public static Pair<InflationProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdOisUsCpi2(
      ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[] oisDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        OIS_MARKET_QUOTES, DSC_1_USD_GENERATORS, DSC_1_USD_ATTR);
    InstrumentDefinition<?>[][][] unitMulticurveDefinition = new InstrumentDefinition<?>[][][] {{oisDefinition}};
    GeneratorYDCurve[][] generatorMulticurve = new GeneratorYDCurve[][] {{GENERATOR_YD_MAT_LIN}};
    String[][] namesMulticurve = new String[][] {{CURVE_NAME_USD_OIS}};
    Map<IndexON, ZonedDateTimeDoubleTimeSeries> htsOn = getOnHts(calibrationDate, false);
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurvePair = 
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(calibrationDate, unitMulticurveDefinition, 
            generatorMulticurve, namesMulticurve, KNOWN_DATA_MULTICURVE, PSMQDC, PSMQCSDC, 
            DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, CURVE_BUILDING_REPOSITORY_MULTICURVE, htsOn, HTS_IBOR);
    InstrumentDefinition<?>[] inflDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        CPI_USD_MARKET_QUOTES, HICP_USD_GENERATORS, HICP_USD_ATTR);
    InstrumentDefinition<?>[][][] unitOnflationDefinition = new InstrumentDefinition<?>[][][] {{inflDefinition } };
    GeneratorPriceIndexCurve[][] generatorInflation = new GeneratorPriceIndexCurve[][] {{GENERATOR_PI_FIX_LIN}};
    InflationProviderDiscount knownDataInflation = new InflationProviderDiscount(multicurvePair.getFirst());
    CurveBuildingBlockBundle knownBlock = multicurvePair.getSecond();
    String[][] namesInflation = new String[][] {{CURVE_NAME_CPI_USD}};
    Pair<InflationProviderDiscount, CurveBuildingBlockBundle> multicurveInflation = 
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsInflation(calibrationDate, unitOnflationDefinition, 
            generatorInflation, namesInflation, knownDataInflation, knownBlock, PSIMQC, PSIMQCSDC, DSC_MAP,
            FWD_ON_MAP, FWD_IBOR_MAP, USD_HICP_MAP, 
            CURVE_BUILDING_REPOSITORY_INFLATION, htsOn, HTS_IBOR, getCpiHts(calibrationDate));
    return multicurveInflation;
  }
  
  /**
   * Returns a set of calibrated curve: dsc/on with OIS and US CPI with zero-coupon swaps.
   * The curves are calibrated as one units with the two curves calibrated simultaneously.
   * @param calibrationDate The calibration date.
   * @return  The calibrated curves and Jacobians.
   */
  public static Pair<InflationProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdOisUsCpi3(
      ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[] oisDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        OIS_MARKET_QUOTES, DSC_1_USD_GENERATORS, DSC_1_USD_ATTR);
    InstrumentDefinition<?>[] inflDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        CPI_USD_MARKET_QUOTES, HICP_USD_GENERATORS, HICP_USD_ATTR);
    InstrumentDefinition<?>[][][] unitDefinition = new InstrumentDefinition<?>[][][] {{oisDefinition, inflDefinition}};
    GeneratorCurve[][] generator = new GeneratorCurve[][] {{GENERATOR_YD_MAT_LIN, GENERATOR_PI_FIX_LIN}};
    String[][] namesCurves = new String[][] {{CURVE_NAME_USD_OIS, CURVE_NAME_CPI_USD}};
    Map<IndexON, ZonedDateTimeDoubleTimeSeries> htsOn = getOnHts(calibrationDate, false);
    InflationProviderDiscount knownDataInflation = new InflationProviderDiscount(FX_MATRIX);
    Pair<InflationProviderDiscount, CurveBuildingBlockBundle> multicurveInflation = 
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsInflation(calibrationDate, unitDefinition, 
            generator, namesCurves, knownDataInflation, new CurveBuildingBlockBundle(), PSIMQC, PSIMQCSDC, DSC_MAP,
            FWD_ON_MAP, FWD_IBOR_MAP, USD_HICP_MAP, 
            CURVE_BUILDING_REPOSITORY_INFLATION, htsOn, HTS_IBOR, getCpiHts(calibrationDate));
    return multicurveInflation;
  }
  
  /**
   * Returns a set of calibrated curve: dsc/on with OIS and US CPI with zero-coupon swaps.
   * The curves are calibrated as two units in a unique calibration.
   * Multiplicative seasonality adjustment is included in the curve.
   * @param calibrationDate The calibration date.
   * @return  The calibrated curves and Jacobians.
   */
  public static Pair<InflationProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdOisUsCpiSeasonality(
      ZonedDateTime calibrationDate) {
    ZonedDateTimeDoubleTimeSeries htsCpi = StandardTimeSeriesInflationDataSets.timeSeriesUsCpi(
        calibrationDate.minusMonths(7).with(TemporalAdjusters.lastDayOfMonth()), calibrationDate);
    List<ZonedDateTime> timesList = htsCpi.times();
    // Create seasonal adjustments
    ZonedDateTime currentDataEnd = timesList.get(timesList.size()-1);
    ZonedDateTime[] seasonalityDate = ScheduleCalculator.getUnadjustedDateSchedule(currentDataEnd, 
        currentDataEnd.plusYears(30), Period.ofMonths(1), true, false);
    double[] seasonalStep = new double[seasonalityDate.length];
    for (int loopins = 0; loopins < seasonalityDate.length; loopins++) {
        seasonalStep[loopins] = TimeCalculator.getTimeBetween(calibrationDate, seasonalityDate[loopins]);
    }
    SeasonalCurve seasonalCurve = new SeasonalCurve(seasonalStep, SEASONAL_FACTORS, false);
    GeneratorPriceIndexCurve genInfSeasonal = 
        new GeneratorPriceIndexCurveMultiplyFixedCurve(GENERATOR_PI_FIX_LIN, seasonalCurve);
    InstrumentDefinition<?>[] oisDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        OIS_MARKET_QUOTES, DSC_1_USD_GENERATORS, DSC_1_USD_ATTR);
    InstrumentDefinition<?>[] inflDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        CPI_USD_MARKET_QUOTES, HICP_USD_GENERATORS, HICP_USD_ATTR);
    InstrumentDefinition<?>[][][] unitDefinition = new InstrumentDefinition<?>[][][] {{oisDefinition}, {inflDefinition}};
    GeneratorCurve[][] generator = new GeneratorCurve[][] {{GENERATOR_YD_MAT_LIN}, {genInfSeasonal}};
    String[][] namesCurves = new String[][] {{CURVE_NAME_USD_OIS}, {CURVE_NAME_CPI_USD}};
    Map<IndexON, ZonedDateTimeDoubleTimeSeries> htsOn = getOnHts(calibrationDate, false);
    InflationProviderDiscount knownDataInflation = new InflationProviderDiscount(FX_MATRIX);
    Pair<InflationProviderDiscount, CurveBuildingBlockBundle> multicurveInflation = 
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsInflation(calibrationDate, unitDefinition, 
            generator, namesCurves, knownDataInflation, new CurveBuildingBlockBundle(), PSIMQC, PSIMQCSDC, DSC_MAP,
            FWD_ON_MAP, FWD_IBOR_MAP, USD_HICP_MAP, 
            CURVE_BUILDING_REPOSITORY_INFLATION, htsOn, HTS_IBOR, getCpiHts(calibrationDate));
    return multicurveInflation;
  }
  
  /**
   * Returns a set of calibrated curve: dsc/on with OIS and US CPI with zero-coupon swaps.
   * The curves are calibrated as two units in a unique calibration.
   * The inflation curve start with the known data (CPI up to calibration date).
   * @param calibrationDate The calibration date.
   * @return  The calibrated curves and Jacobians.
   */
  public static Pair<InflationProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdOisUsCpiCurrent(
      ZonedDateTime calibrationDate) {
    ZonedDateTimeDoubleTimeSeries htsCpi = StandardTimeSeriesInflationDataSets.timeSeriesUsCpi(
        calibrationDate.minusMonths(7).with(TemporalAdjusters.lastDayOfMonth()), calibrationDate);
    List<ZonedDateTime> timesList = htsCpi.times();
    List<Double> valuesList = htsCpi.values();
    int nbTimes = timesList.size();
    Double[] times = new Double[nbTimes];
    Double[] values = valuesList.toArray(new Double[0]);
    for(int i=0; i<nbTimes; i++) {
      times[i] = TimeCalculator.getTimeBetween(calibrationDate, timesList.get(i));
    }
    InterpolatedDoublesCurve startCurve = new InterpolatedDoublesCurve(times, values, INTERPOLATOR_STEP_FLAT, true);
    GeneratorPriceIndexCurve generatorFixLinAnchor = new GeneratorPriceIndexCurveInterpolatedAnchor(
        LAST_FIXING_END_CALCULATOR, INTERPOLATOR_LINEAR, times[nbTimes-1], 1.0);
    GeneratorPriceIndexCurve genInfCurrent = 
        new GeneratorPriceIndexCurveMultiplyFixedCurve(generatorFixLinAnchor, startCurve);
    InstrumentDefinition<?>[] oisDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        OIS_MARKET_QUOTES, DSC_1_USD_GENERATORS, DSC_1_USD_ATTR);
    InstrumentDefinition<?>[] inflDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        CPI_USD_MARKET_QUOTES, HICP_USD_GENERATORS, HICP_USD_ATTR);
    InstrumentDefinition<?>[][][] unitDefinition = new InstrumentDefinition<?>[][][] {{oisDefinition}, {inflDefinition}};
    GeneratorCurve[][] generator = new GeneratorCurve[][] {{GENERATOR_YD_MAT_LIN}, {genInfCurrent}};
    String[][] namesCurves = new String[][] {{CURVE_NAME_USD_OIS}, {CURVE_NAME_CPI_USD}};
    Map<IndexON, ZonedDateTimeDoubleTimeSeries> htsOn = getOnHts(calibrationDate, false);
    InflationProviderDiscount knownDataInflation = new InflationProviderDiscount(FX_MATRIX);
    Pair<InflationProviderDiscount, CurveBuildingBlockBundle> multicurveInflation = 
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsInflation(calibrationDate, unitDefinition, 
            generator, namesCurves, knownDataInflation, new CurveBuildingBlockBundle(), PSIMQC, PSIMQCSDC, DSC_MAP,
            FWD_ON_MAP, FWD_IBOR_MAP, USD_HICP_MAP, 
            CURVE_BUILDING_REPOSITORY_INFLATION, htsOn, HTS_IBOR, getCpiHts(calibrationDate));
    return multicurveInflation;
  }
  
  /**
   * Returns a set of calibrated curve: dsc/on with OIS and US CPI with zero-coupon swaps.
   * The curves are calibrated as two units in a unique calibration.
   * The inflation curve start with the known data (CPI up to calibration date) and a seasonality is used.
   * @param calibrationDate The calibration date.
   * @return  The calibrated curves and Jacobians.
   */
  public static Pair<InflationProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdOisUsCpiCurrentSeasonality(
      ZonedDateTime calibrationDate) {
    ZonedDateTimeDoubleTimeSeries htsCpi = StandardTimeSeriesInflationDataSets.timeSeriesUsCpi(
        calibrationDate.minusMonths(7).with(TemporalAdjusters.lastDayOfMonth()), calibrationDate);
    List<ZonedDateTime> timesList = htsCpi.times();
    List<Double> valuesList = htsCpi.values();
    int nbTimes = timesList.size();
    Double[] times = new Double[nbTimes];
    Double[] values = valuesList.toArray(new Double[0]);
    for(int i=0; i<nbTimes; i++) {
      times[i] = TimeCalculator.getTimeBetween(calibrationDate, timesList.get(i));
    }
    InterpolatedDoublesCurve startCurve = new InterpolatedDoublesCurve(times, values, INTERPOLATOR_STEP_FLAT, true);
    // Create seasonal adjustments
    ZonedDateTime currentDataEnd = timesList.get(timesList.size()-1);
    ZonedDateTime[] seasonalityDate = ScheduleCalculator.getUnadjustedDateSchedule(currentDataEnd, 
        currentDataEnd.plusYears(30), Period.ofMonths(1), true, false);
    double[] seasonalStep = new double[seasonalityDate.length];
    for (int loopins = 0; loopins < seasonalityDate.length; loopins++) {
        seasonalStep[loopins] = TimeCalculator.getTimeBetween(calibrationDate, seasonalityDate[loopins]);
    }
    SeasonalCurve seasonalCurve = new SeasonalCurve(seasonalStep, SEASONAL_FACTORS, false);
    // Total adjustment as multiplication between seasonal and start.
    DoublesCurve adjustmentCurve = new SpreadDoublesCurve(MultiplyCurveSpreadFunction.getInstance(), startCurve, seasonalCurve);
    GeneratorPriceIndexCurve generatorFixLinAnchor = new GeneratorPriceIndexCurveInterpolatedAnchor(
        LAST_FIXING_END_CALCULATOR, INTERPOLATOR_LINEAR, times[nbTimes-1], 1.0);
    GeneratorPriceIndexCurve genInfCurrent = 
        new GeneratorPriceIndexCurveMultiplyFixedCurve(generatorFixLinAnchor, adjustmentCurve);
    InstrumentDefinition<?>[] oisDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        OIS_MARKET_QUOTES, DSC_1_USD_GENERATORS, DSC_1_USD_ATTR);
    InstrumentDefinition<?>[] inflDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        CPI_USD_MARKET_QUOTES, HICP_USD_GENERATORS, HICP_USD_ATTR);
    InstrumentDefinition<?>[][][] unitDefinition = new InstrumentDefinition<?>[][][] {{oisDefinition}, {inflDefinition}};
    GeneratorCurve[][] generator = new GeneratorCurve[][] {{GENERATOR_YD_MAT_LIN}, {genInfCurrent}};
    String[][] namesCurves = new String[][] {{CURVE_NAME_USD_OIS}, {CURVE_NAME_CPI_USD}};
    Map<IndexON, ZonedDateTimeDoubleTimeSeries> htsOn = getOnHts(calibrationDate, false);
    InflationProviderDiscount knownDataInflation = new InflationProviderDiscount(FX_MATRIX);
    Pair<InflationProviderDiscount, CurveBuildingBlockBundle> multicurveInflation = 
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsInflation(calibrationDate, unitDefinition, 
            generator, namesCurves, knownDataInflation, new CurveBuildingBlockBundle(), PSIMQC, PSIMQCSDC, DSC_MAP,
            FWD_ON_MAP, FWD_IBOR_MAP, USD_HICP_MAP, 
            CURVE_BUILDING_REPOSITORY_INFLATION, htsOn, HTS_IBOR, getCpiHts(calibrationDate));
    return multicurveInflation;
  }
  
  private static Map<IndexON,ZonedDateTimeDoubleTimeSeries> getOnHts(ZonedDateTime calibrationDate, boolean withToday) {
    ZonedDateTime referenceDate = withToday ? calibrationDate : calibrationDate.minusDays(1);
    ZonedDateTimeDoubleTimeSeries htsOn = StandardTimeSeriesOnIborDataSets.timeSeriesUsdOn2014Jan(referenceDate);
    Map<IndexON,ZonedDateTimeDoubleTimeSeries> htsOnMap = new HashMap<>();
    htsOnMap.put(USDFEDFUND, htsOn);    
    return htsOnMap;
  }
  
  private static Map<IndexPrice,ZonedDateTimeDoubleTimeSeries> getCpiHts(ZonedDateTime calibrationDate) {
    ZonedDateTimeDoubleTimeSeries htsCpi = StandardTimeSeriesInflationDataSets.timeSeriesUsCpi(calibrationDate);
    Map<IndexPrice,ZonedDateTimeDoubleTimeSeries> htsCpiMap = new HashMap<>();
    htsCpiMap.put(USCPI, htsCpi);    
    return htsCpiMap;
  }
  
  private static final Map<IborIndex,ZonedDateTimeDoubleTimeSeries> HTS_IBOR = new HashMap<>();
  
}
