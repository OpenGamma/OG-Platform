/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.datasets;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarGBP;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapCouponFixedCouponDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Curves calibration in GBP: 
 * DSCON-OIS/LIBOR6M-FRAIRS
 * Recent market data. Standard instruments.
 */
public class GbpDatasetJuly16 {

  private static final Interpolator1D INTERPOLATOR_LINEAR = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  private static final Calendar LON = new CalendarGBP("LON");
  private static final Currency GBP = Currency.GBP;
  private static final FXMatrix FX_MATRIX = new FXMatrix(GBP);

  private static final double NOTIONAL = 1.0;
  private static final NotionalProvider NOTIONAL_PROV = new NotionalProvider() {
    @Override
    public double getAmount(final LocalDate date) {
      return NOTIONAL;
    }
  };

  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedON GENERATOR_OIS_GBP = GENERATOR_OIS_MASTER.getGenerator("GBP1YSONIA", LON);
  private static final IndexON GBPSONIA = GENERATOR_OIS_GBP.getIndex();
  private static final AdjustedDateParameters ADJUSTED_DATE_ON = 
      new AdjustedDateParameters(LON, GENERATOR_OIS_GBP.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_FIXING = 
      new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS, LON, BusinessDayConventionFactory.of("Following"));

  private static final String CURVE_NAME_DSC_GBP = "GBP-DSCON-OIS";

  /** Data as of 16-Jul-2014 */
  /** Market values for the curve with Central Bank meeting date OIS swaps */
  private static final double[] BOE_MARKET_QUOTES = 
      new double[] {0.00455, 0.00468, 0.004988, 0.006238, 0.006475, 0.00675 };
  /** Tenors for the dsc GBP curve */
  private static final ZonedDateTime[] BOE_DATES = new ZonedDateTime[] {
    DateUtils.getUTCDate(2014, 8, 7), DateUtils.getUTCDate(2014, 9, 4), DateUtils.getUTCDate(2014, 10, 9), 
    DateUtils.getUTCDate(2014, 11, 6), DateUtils.getUTCDate(2014, 12, 4), DateUtils.getUTCDate(2015, 1, 8), 
    DateUtils.getUTCDate(2015, 2, 5) };
  private static final int NB_DATES = BOE_DATES.length;

  /** Market values for curve with OIS swaps */
  private static final double[] STD_MARKET_QUOTES = new double[] {0.004263,
    0.004275, 0.00431, 0.004375, 0.004375, 0.004507,
    0.004592, 0.004805, 0.005107, 0.005339, 0.00562,
    0.005936, 0.006231, 0.006543, 0.006879, 0.007185};
  /** Generators for the dsc GBP curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] STD_GENERATORS =
      CurveCalibrationConventionDataSets.generatorGbpOnOis(1, STD_MARKET_QUOTES.length - 1);
  /** Tenors for the dsc GBP curve */
  private static final Period[] STD_TENOR = new Period[] {
    Period.ofDays(0), Period.ofDays(7), Period.ofDays(14), Period.ofDays(21), Period.ofMonths(1), Period.ofMonths(2),
    Period.ofMonths(3), Period.ofMonths(4), Period.ofMonths(5), Period.ofMonths(6), Period.ofMonths(7),
    Period.ofMonths(8), Period.ofMonths(9), Period.ofMonths(10), Period.ofMonths(11), Period.ofMonths(12)};
  private static final GeneratorAttributeIR[] STD_ATTR = new GeneratorAttributeIR[STD_TENOR.length];
  static {
    for (int loopins = 0; loopins < 1; loopins++) {
      STD_ATTR[loopins] = new GeneratorAttributeIR(STD_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = 1; loopins < STD_TENOR.length; loopins++) {
      STD_ATTR[loopins] = new GeneratorAttributeIR(STD_TENOR[loopins]);
    }
  }

  /** Units of curves */
  private static final int NB_UNITS = 1;
  private static final int NB_BLOCKS = 1;
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS][];
    }
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_GBP };
    DSC_MAP.put(CURVE_NAME_DSC_GBP, GBP);
    FWD_ON_MAP.put(CURVE_NAME_DSC_GBP, new IndexON[] {GBPSONIA });
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, 
      final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute, final ZonedDateTime referenceDate) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(referenceDate, marketQuotes[loopmv], NOTIONAL, 
          attribute[loopmv]);
    }
    return definitions;
  }

  /** Calculators */
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = 
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = 
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY =
      CurveCalibrationConventionDataSets.curveBuildingRepositoryMulticurve();

  
  private static InstrumentDefinition<?>[] getDefinitionForFirstInstruments(ZonedDateTime calibrationDate, 
      ZonedDateTime firstStartDate, InstrumentDefinition<?>[] definitionsDsc) {
    int howMany = 0;

    for (int i = 0; i < definitionsDsc.length; ++i) {
      if (definitionsDsc[i] instanceof CashDefinition) {
        CashDefinition definition = (CashDefinition) definitionsDsc[i];
        if (firstStartDate.isBefore(definition.getEndDate())) {
          howMany = i + 1;
          break;
        }
      } else if (definitionsDsc[i] instanceof SwapFixedONDefinition) {
        SwapFixedONDefinition definition = (SwapFixedONDefinition) definitionsDsc[i];
        if (firstStartDate.isBefore(
            definition.getFirstLeg().getNthPayment(definition.getFirstLeg().getNumberOfPayments() - 1).
            getPaymentDate())) {
          howMany = i + 1;
          break;
        }
      } else {
        throw new OpenGammaRuntimeException("Instrument definition type not supported: " 
            + definitionsDsc[i].getClass().getName());
      }
    }
    return Arrays.copyOf(definitionsDsc, howMany);
  }
  
  /**
   */
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getBoeCurve(
      ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[][][] definitionsUnits = new InstrumentDefinition<?>[1][][];
    InstrumentDefinition<?>[] definitionsBoe = generateOisSwaps(BOE_DATES, BOE_MARKET_QUOTES);
    InstrumentDefinition<?>[] definitionsStdInstruments = 
        getDefinitionForFirstInstruments(calibrationDate, BOE_DATES[0],
            getDefinitions(STD_MARKET_QUOTES, STD_GENERATORS, STD_ATTR, calibrationDate));
    
    definitionsUnits[0] = new InstrumentDefinition<?>[][] {
        (InstrumentDefinition<?>[]) ArrayUtils.addAll(definitionsStdInstruments, definitionsBoe)};
    
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(calibrationDate, definitionsUnits, 
        GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, 
        CURVE_BUILDING_REPOSITORY, TS_FIXED_OIS_GBP_WITH_TODAY, TS_FIXED_OIS_GBP_WITHOUT_TODAY, 
        TS_FIXED_IBOR_GBP6M_WITH_LAST, TS_FIXED_IBOR_GBP6M_WITHOUT_LAST);
  }
  
  /**
   */
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getStandardCurve(
      ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[][][] definitionsUnits = new InstrumentDefinition<?>[1][][];
    InstrumentDefinition<?>[] definitions = 
        getDefinitions(STD_MARKET_QUOTES, STD_GENERATORS, STD_ATTR, calibrationDate);
    definitionsUnits[0] = new InstrumentDefinition<?>[][] {definitions };
    
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(calibrationDate, definitionsUnits, 
        GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, 
        CURVE_BUILDING_REPOSITORY, TS_FIXED_OIS_GBP_WITH_TODAY, TS_FIXED_OIS_GBP_WITHOUT_TODAY, 
        TS_FIXED_IBOR_GBP6M_WITH_LAST, TS_FIXED_IBOR_GBP6M_WITHOUT_LAST);
  }

  /**
   * Returns the array of overnight index used in the curve data set. 
   * @return The array: GBPSONIA 
   */
  public static IndexON[] indexONArray() {
    return new IndexON[] {GBPSONIA };
  }

  /**
   * Returns the array of calendars used in the curve data set. 
   * @return The array: NYC 
   */
  public static Calendar[] calendarArray() {
    return new Calendar[] {LON };
  }

  /**
   * Returns an array with one time series corresponding to the GBP SONIA fixing up to and including the last date.
   * @return
   */
  public static ZonedDateTimeDoubleTimeSeries fixingGbpSoniaWithLast() {
    return TS_ON_GBP_WITH_TODAY;
  }

  /**
   * Returns an array with one time series corresponding to the GBP SONIA fixing up to and including the last date.
   * @return
   */
  public static ZonedDateTimeDoubleTimeSeries fixingGbpSoniaWithoutLast() {
    return TS_ON_GBP_WITHOUT_TODAY;
  }

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_GBP_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
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
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_GBP_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25) },
      new double[] {0.002318, 0.002346, 0.002321, 0.002331,
        0.002341, 0.002336, 0.002341, 0.002336, 0.002336,
        0.002326, 0.002331, 0.002336, 0.002336, 0.002316,
        0.002331, 0.002326, 0.002341, 0.002351, 0.002341 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_GBP_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_GBP_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_GBP_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_GBP_WITHOUT_TODAY };

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_GBP6M_WITH_LAST = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
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
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_GBP6M_WITHOUT_LAST = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25) },
      new double[] {0.002318, 0.002346, 0.002321, 0.002331,
        0.002341, 0.002336, 0.002341, 0.002336, 0.002336,
        0.002326, 0.002331, 0.002336, 0.002336, 0.002316,
        0.002331, 0.002326, 0.002341, 0.002351, 0.002341 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_GBP6M_WITH_LAST = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_GBP6M_WITH_LAST };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_GBP6M_WITHOUT_LAST = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_GBP6M_WITHOUT_LAST };

  @SuppressWarnings("unchecked")
  private static InstrumentDefinition<?>[] generateOisSwaps(ZonedDateTime[] dates, double[] fixedRate) {
    ArgumentChecker.isTrue(dates.length == fixedRate.length + 1, "dates and rate lengths not compatible");
    int nbSwap = dates.length - 1;
    SwapCouponFixedCouponDefinition[] swap = new SwapCouponFixedCouponDefinition[nbSwap];
    for (int loopimm = 0; loopimm < nbSwap; loopimm++) {
      PaymentDefinition[] cpn = new FixedAnnuityDefinitionBuilder().
          payer(true).
          currency(GBP).
          notional(NOTIONAL_PROV).
          startDate(dates[loopimm].toLocalDate()).
          endDate(dates[loopimm + 1].toLocalDate()).
          dayCount(GBPSONIA.getDayCount()).
          accrualPeriodFrequency(GENERATOR_OIS_GBP.getLegsPeriod()).
          rate(fixedRate[loopimm]).
          accrualPeriodParameters(ADJUSTED_DATE_ON).
          build().getPayments();
      CouponFixedDefinition[] cpnFixed = new CouponFixedDefinition[cpn.length];
      for (int loopcpn = 0; loopcpn < cpn.length; loopcpn++) {
        cpnFixed[loopcpn] = (CouponFixedDefinition) cpn[loopcpn];
      }
      AnnuityCouponFixedDefinition fixedLegDefinition = new AnnuityCouponFixedDefinition(cpnFixed, LON);
      AnnuityDefinition<? extends CouponDefinition> onLegDefinition = (AnnuityDefinition<? extends CouponDefinition>) 
          new FloatingAnnuityDefinitionBuilder().
          payer(false).
          notional(NOTIONAL_PROV).
          startDate(dates[loopimm].toLocalDate()).
          endDate(dates[loopimm + 1].toLocalDate()).
          index(GBPSONIA).
          accrualPeriodFrequency(GENERATOR_OIS_GBP.getLegsPeriod()).
          rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_ON).
          accrualPeriodParameters(ADJUSTED_DATE_ON).
          dayCount(GBPSONIA.getDayCount()).
          fixingDateAdjustmentParameters(OFFSET_FIXING).
          currency(GBP).
          compoundingMethod(CompoundingMethod.FLAT).
          build();
      swap[loopimm] = new SwapCouponFixedCouponDefinition(fixedLegDefinition, onLegDefinition);
    }
    return swap;
  }

}
