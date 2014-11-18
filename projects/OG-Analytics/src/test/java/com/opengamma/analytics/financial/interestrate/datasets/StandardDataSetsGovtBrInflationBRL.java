/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.google.common.collect.LinkedListMultimap;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurve;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurveInterpolatedAnchor;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurveMultiplyFixedCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompoundedMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationZeroCoupon;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.index.PriceIndexMaster;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.provider.calculator.generic.LastFixingEndTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.ParSpreadInflationMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.inflationissuer.InflationIssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Curves calibration in BRL: Dsc/ON, inflation curves. 
 * Discounting does not use correct BRL conventions. Only for testing purposes.
 * 1) BRL_DSCONIS-OIS_IPCA-ZC. Discounting, BR CPI (from ZC swaps). The Govt curve is the same as the DSC/ON one.
 * Know current CPI used to calibrate the inflation curve.
 * Data stored in snapshots for comparison with platform.
 */
public class StandardDataSetsGovtBrInflationBRL {

  private static final Calendar BR_CAL = new MondayToFridayCalendar("BRL");
  private static final Currency BRL = Currency.BRL;
  private static final FXMatrix FX_MATRIX = new FXMatrix(BRL);
  
  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedCompoundedONCompoundedMaster GENERATOR_ONCMP_MASTER = 
      GeneratorSwapFixedCompoundedONCompoundedMaster.getInstance();

  private static final GeneratorSwapFixedCompoundedONCompounded GENERATOR_ON_BRL = 
      GENERATOR_ONCMP_MASTER.getGenerator("BRLCDI", BR_CAL);
  private static final IndexON BRLCDI = GENERATOR_ON_BRL.getIndex();
  
  private static final String CURVE_NAME_OIS = "USD-OIS";
  private static final String CURVE_NAME_CPI = "USD-ZCIPCA";
  
  private static final IndexPrice BRIPCA = PriceIndexMaster.getInstance().getIndex("BRIPCA");
  private static final GeneratorYDCurve GENERATOR_YD_MAT_LIN = 
      CurveCalibrationConventionDataSets.generatorYDMatLin();
  private static final Interpolator1D INTERPOLATOR_STEP_FLAT =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.STEP,
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_EXP = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.EXPONENTIAL, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final LastFixingEndTimeCalculator LAST_FIXING_END_CALCULATOR = LastFixingEndTimeCalculator.getInstance();
  
  
  /** Market values for the ON BRL curve */
  private static final double[] OIS_MARKET_QUOTES = new double[] {
    0.1000, 0.1000, 0.1000, 0.1100, 0.1100, 0.1200, 0.1200, 0.1200, 0.1200, 0.1200, 
    0.1200, 0.1200, 0.1200, 0.1200, 0.1200 };
  /** Generators for the ON BRL curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] OIS_GENERATORS = 
      new GeneratorInstrument<?>[] {GENERATOR_ON_BRL, GENERATOR_ON_BRL, GENERATOR_ON_BRL, GENERATOR_ON_BRL, GENERATOR_ON_BRL,
    GENERATOR_ON_BRL, GENERATOR_ON_BRL, GENERATOR_ON_BRL, GENERATOR_ON_BRL, GENERATOR_ON_BRL,
    GENERATOR_ON_BRL, GENERATOR_ON_BRL, GENERATOR_ON_BRL, GENERATOR_ON_BRL, GENERATOR_ON_BRL};
  /** Tenors for the ON BRL curve */
  private static final Period[] OIS_TENOR = new Period[] {
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10) , Period.ofYears(15) , Period.ofYears(20) , Period.ofYears(30) };
  private static final GeneratorAttributeIR[] OIS_ATTR = new GeneratorAttributeIR[OIS_TENOR.length];
  static {
    for (int loopins = 0; loopins < OIS_TENOR.length; loopins++) {
      OIS_ATTR[loopins] = new GeneratorAttributeIR(OIS_TENOR[loopins]);
    }
  }

  private static final String BR_GOVT_NAME = "BR GOVT";
  private static final Set<CreditRating> RATING = new HashSet<>();
  static {
    RATING.add(CreditRating.of("BBB", "OG_RATING", true));
  }
  private static final LegalEntity BR_GOVT_LEGAL_ENTITY = new LegalEntity("BRGOVT", BR_GOVT_NAME, RATING, null, null);
  
  /** Market values for the BR IPCA curve */
  public static final double[] CPI_MARKET_QUOTES = 
      new double[] {0.0600, 0.0600, 0.0600};
  /** Generators for the BR IPCA curve */
  public static final GeneratorSwapFixedInflationZeroCoupon ZC_INFL_GENERATOR = 
      new GeneratorSwapFixedInflationZeroCoupon("BR IPCA", BRIPCA, BusinessDayConventions.MODIFIED_FOLLOWING, 
      BR_CAL, false, 1, 2, false);
  @SuppressWarnings("unchecked")
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] CPI_GENERATORS = 
      new GeneratorInstrument[] {ZC_INFL_GENERATOR, ZC_INFL_GENERATOR, ZC_INFL_GENERATOR};
  /** Tenors for the HICP USD curve */
  private static final Period[] CPI_TENOR = new Period[] {
    Period.ofYears(1), Period.ofYears(10), Period.ofYears(30) };
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
  private static final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> DSC_ISS_MAP = LinkedListMultimap.create();
  private static final LinkedHashMap<String, IndexPrice[]> USD_HICP_MAP = new LinkedHashMap<>();

  static {
    DSC_MAP.put(CURVE_NAME_OIS, BRL);
    FWD_ON_MAP.put(CURVE_NAME_OIS, new IndexON[] {BRLCDI });
    USD_HICP_MAP.put(CURVE_NAME_CPI, new IndexPrice[] {BRIPCA });
    DSC_ISS_MAP.put(CURVE_NAME_OIS, Pairs.of((Object) BR_GOVT_LEGAL_ENTITY.getShortName(), 
        (LegalEntityFilter<LegalEntity>) new LegalEntityShortName()));
  }
  
  /** Calculators */
  private static final ParSpreadInflationMarketQuoteIssuerDiscountingCalculator PSMQIssuerInflationC = 
      ParSpreadInflationMarketQuoteIssuerDiscountingCalculator.getInstance();
  private static final ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator PSMQCSIssuerInflationC = 
      ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();

  private static final InflationIssuerDiscountBuildingRepository CURVE_BUILDING_REPOSITORY_INFLATION_ISSUER = 
      CurveCalibrationConventionDataSets.curveBuildingRepositoryInflationIssuer();
  
  /**
   * Returns a set of calibrated curve: DSC/ON with OIS and BR CPI with zero-coupon swaps.
   * The curves are calibrated as two units in a unique calibration.
   * The inflation curve start with the known data (CPI up to calibration date).
   * @param calibrationDate The calibration date.
   * @return  The calibrated curves and Jacobians.
   */
  public static Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> getCurvesBrlOisBrCpiCurrent(
      ZonedDateTime calibrationDate) {
    ZonedDateTimeDoubleTimeSeries htsCpi = StandardTimeSeriesInflationDataSets.timeSeriesBrCpi(
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
    DoublesCurve adjustmentCurve = startCurve;
    GeneratorPriceIndexCurve generatorFixExpAnchor = new GeneratorPriceIndexCurveInterpolatedAnchor(
        LAST_FIXING_END_CALCULATOR, INTERPOLATOR_EXP, times[nbTimes-1], 1.0);
    GeneratorPriceIndexCurve genAdjustment = 
        new GeneratorPriceIndexCurveMultiplyFixedCurve(generatorFixExpAnchor, adjustmentCurve);
    InstrumentDefinition<?>[] oisDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        OIS_MARKET_QUOTES, OIS_GENERATORS, OIS_ATTR);
    InstrumentDefinition<?>[] inflDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        CPI_MARKET_QUOTES, CPI_GENERATORS, CPI_ATTR);
    InstrumentDefinition<?>[][][] unitDefinition = 
        new InstrumentDefinition<?>[][][] {{oisDefinition}, {inflDefinition}};
    GeneratorCurve[][] generator = 
        new GeneratorCurve[][] {{GENERATOR_YD_MAT_LIN}, {genAdjustment}};
    String[][] namesCurves = new String[][] {{CURVE_NAME_OIS}, {CURVE_NAME_CPI}};
    Map<IndexON, ZonedDateTimeDoubleTimeSeries> htsOn = getOnHts(calibrationDate, false);
    InflationIssuerProviderDiscount knownDataIssuer = new InflationIssuerProviderDiscount(FX_MATRIX);
    Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> multicurveInflation = 
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsInflationIssuer(calibrationDate, unitDefinition, 
            generator, namesCurves, knownDataIssuer, new CurveBuildingBlockBundle(), PSMQIssuerInflationC, 
            PSMQCSIssuerInflationC, DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, USD_HICP_MAP, DSC_ISS_MAP, 
            CURVE_BUILDING_REPOSITORY_INFLATION_ISSUER, htsOn, HTS_IBOR, getCpiHts(calibrationDate));
    return multicurveInflation;
  }
  
  private static Map<IndexON,ZonedDateTimeDoubleTimeSeries> getOnHts(ZonedDateTime calibrationDate, boolean withToday) {
    ZonedDateTime referenceDate = withToday ? calibrationDate : calibrationDate.minusDays(1);
    ZonedDateTimeDoubleTimeSeries htsOn = StandardTimeSeriesOnIborDataSets.timeSeriesUsdOn2014Jan(referenceDate);
    Map<IndexON,ZonedDateTimeDoubleTimeSeries> htsOnMap = new HashMap<>();
    htsOnMap.put(BRLCDI, htsOn);    
    return htsOnMap;
  }
  
  private static Map<IndexPrice,ZonedDateTimeDoubleTimeSeries> getCpiHts(ZonedDateTime calibrationDate) {
    ZonedDateTimeDoubleTimeSeries htsCpi = StandardTimeSeriesInflationDataSets.timeSeriesBrCpi(calibrationDate);
    Map<IndexPrice,ZonedDateTimeDoubleTimeSeries> htsCpiMap = new HashMap<>();
    htsCpiMap.put(BRIPCA, htsCpi);    
    return htsCpiMap;
  }
  
  private static final Map<IborIndex,ZonedDateTimeDoubleTimeSeries> HTS_IBOR = new HashMap<>();
  
}
