/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurve;
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
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.inflation.InflationDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Curves calibration in USD: Dsc/ON, US Govt and inflation. 
 * 1) DSCON-OIS_USDIS-USGOVT_HICP-ZC. All curves calibrated in a single process using three units.
 * Data stored in snapshots for comparison with platform.
 */
public class StandardDataSetsGovtUsInflationUSD {

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final Currency USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);
  private static final MulticurveProviderDiscount KNOWN_DATA_MULTICURVE = new MulticurveProviderDiscount(FX_MATRIX);
  
  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();

  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GENERATOR_OIS_MASTER.getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON USDFEDFUND = GENERATOR_OIS_USD.getIndex();
  
  private static final String CURVE_NAME_USD_OIS = "USD-OIS";
  private static final String CURVE_NAME_USD_USGVT = "USD-USGVT";
  private static final String CURVE_NAME_USD_CPI = "USD-ZCHICP";
  
  private static final IndexPrice USCPI = IndexPriceMaster.getInstance().getIndex("USCPI");
  private static final GeneratorPriceIndexCurve GENERATOR_PI_FIX_LIN = 
      CurveCalibrationConventionDataSets.generatorPiFixLin();
  private static final GeneratorYDCurve GENERATOR_YD_MAT_LIN = 
      CurveCalibrationConventionDataSets.generatorYDMatLin();  
  
  /** Market quotess for the USD-OIS curve */
  private static final double[] OIS_MARKET_QUOTES = new double[] {0.0016, 0.0016,
    0.00072000, 0.00082000, 0.00093000, 0.00090000, 0.00105000,
    0.00118500, 0.00318650, 0.00704000, 0.01121500, 0.01515000,
    0.01845500, 0.02111000, 0.02332000, 0.02513500, 0.02668500 }; //17
  /** Generators for the USD-OIS curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_1_USD_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorUsdOnOisFfs(2, 15, 0);
  /** Tenors for the dsc USD curve */
  private static final Period[] OIS_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] OIS_ATTR = new GeneratorAttributeIR[OIS_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      OIS_ATTR[loopins] = new GeneratorAttributeIR(OIS_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = 2; loopins < OIS_TENOR.length; loopins++) {
      OIS_ATTR[loopins] = new GeneratorAttributeIR(OIS_TENOR[loopins]);
    }
  } 

  /** Market quotes for the USD-USGVT curve */
  
  /** Market values for the HICP USD curve */ /** USSWITx Interpolation 3M lag */
  public static final double[] CPI_MARKET_QUOTES = 
      new double[] {0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 
    0.0200, 0.0200, 0.0200, 0.0200, 0.0200 };
  /** Generators for the HICP USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] HICP_USD_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorUsdCpi(15);
  /** Tenors for the HICP USD curve */
  private static final Period[] CPI_TENOR = new Period[] {
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), 
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10), 
    Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] CPI_ATTR = new GeneratorAttributeIR[CPI_TENOR.length];
  static {
    for (int loopins = 0; loopins < CPI_TENOR.length; loopins++) {
      CPI_ATTR[loopins] = new GeneratorAttributeIR(CPI_TENOR[loopins]);
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
    USD_HICP_MAP.put(CURVE_NAME_USD_CPI, new IndexPrice[] {USCPI });
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
        OIS_MARKET_QUOTES, DSC_1_USD_GENERATORS, OIS_ATTR);
    InstrumentDefinition<?>[] inflDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        CPI_MARKET_QUOTES, HICP_USD_GENERATORS, CPI_ATTR);
    InstrumentDefinition<?>[][][] unitDefinition = new InstrumentDefinition<?>[][][] {{oisDefinition}, {inflDefinition}};
    GeneratorCurve[][] generator = new GeneratorCurve[][] {{GENERATOR_YD_MAT_LIN}, {GENERATOR_PI_FIX_LIN}};
    String[][] namesCurves = new String[][] {{CURVE_NAME_USD_OIS}, {CURVE_NAME_USD_CPI}};
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
        OIS_MARKET_QUOTES, DSC_1_USD_GENERATORS, OIS_ATTR);
    InstrumentDefinition<?>[][][] unitMulticurveDefinition = new InstrumentDefinition<?>[][][] {{oisDefinition}};
    GeneratorYDCurve[][] generatorMulticurve = new GeneratorYDCurve[][] {{GENERATOR_YD_MAT_LIN}};
    String[][] namesMulticurve = new String[][] {{CURVE_NAME_USD_OIS}};
    Map<IndexON, ZonedDateTimeDoubleTimeSeries> htsOn = getOnHts(calibrationDate, false);
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurvePair = 
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(calibrationDate, unitMulticurveDefinition, 
            generatorMulticurve, namesMulticurve, KNOWN_DATA_MULTICURVE, PSMQDC, PSMQCSDC, 
            DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, CURVE_BUILDING_REPOSITORY_MULTICURVE, htsOn, HTS_IBOR);
    InstrumentDefinition<?>[] inflDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        CPI_MARKET_QUOTES, HICP_USD_GENERATORS, CPI_ATTR);
    InstrumentDefinition<?>[][][] unitOnflationDefinition = new InstrumentDefinition<?>[][][] {{inflDefinition } };
    GeneratorPriceIndexCurve[][] generatorInflation = new GeneratorPriceIndexCurve[][] {{GENERATOR_PI_FIX_LIN}};
    InflationProviderDiscount knownDataInflation = new InflationProviderDiscount(multicurvePair.getFirst());
    CurveBuildingBlockBundle knownBlock = multicurvePair.getSecond();
    String[][] namesInflation = new String[][] {{CURVE_NAME_USD_CPI}};
    Pair<InflationProviderDiscount, CurveBuildingBlockBundle> multicurveInflation = 
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsInflation(calibrationDate, unitOnflationDefinition, 
            generatorInflation, namesInflation, knownDataInflation, knownBlock, PSIMQC, PSIMQCSDC, DSC_MAP,
            FWD_ON_MAP, FWD_IBOR_MAP, USD_HICP_MAP, 
            CURVE_BUILDING_REPOSITORY_INFLATION, htsOn, HTS_IBOR, getCpiHts(calibrationDate));
    return multicurveInflation;
  }
  
  /**
   * Returns a set of calibrated curve: dsc/on with OIS and US CPI with zero-coupon swaps.
   * The curves are calibrated as two units in a unique calibration.
   * @param calibrationDate The calibration date.
   * @return  The calibrated curves and Jacobians.
   */
  public static Pair<InflationProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdOisUsCpi3(
      ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[] oisDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        OIS_MARKET_QUOTES, DSC_1_USD_GENERATORS, OIS_ATTR);
    InstrumentDefinition<?>[] inflDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        CPI_MARKET_QUOTES, HICP_USD_GENERATORS, CPI_ATTR);
    InstrumentDefinition<?>[][][] unitDefinition = new InstrumentDefinition<?>[][][] {{oisDefinition, inflDefinition}};
    GeneratorCurve[][] generator = new GeneratorCurve[][] {{GENERATOR_YD_MAT_LIN, GENERATOR_PI_FIX_LIN}};
    String[][] namesCurves = new String[][] {{CURVE_NAME_USD_OIS, CURVE_NAME_USD_CPI}};
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
