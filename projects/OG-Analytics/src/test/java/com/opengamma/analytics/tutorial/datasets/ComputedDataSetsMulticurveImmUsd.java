/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.datasets;

import java.util.LinkedHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
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
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapCouponFixedCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.QuarterlyIMMRollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterUtils;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Curves calibration in USD: 
 * Calibrating instruments are IMM dates OIS and FRAs.
 * The calibration rates are computed from an existing MulticurveProvider.
 */
public class ComputedDataSetsMulticurveImmUsd {

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final Currency USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);

  private static final double NOTIONAL = 1.0;
  private static final NotionalProvider NOTIONAL_PROV = new NotionalProvider() {
    @Override
    public double getAmount(final LocalDate date) {
      return NOTIONAL;
    }
  };

  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();

  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GENERATOR_OIS_MASTER.getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON USDFEDFUND = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final AdjustedDateParameters ADJUSTED_DATE_LIBOR = new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_LIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final RollDateAdjuster IMM_QUARTERLY_ADJUSTER = QuarterlyIMMRollDateAdjuster.getAdjuster();

  private static final String CURVE_NAME_DSC_USD = "USD-DSCON-OIS";
  private static final String CURVE_NAME_FWD3_USD = "USD-LIBOR3M-FRAIRS";

  /** Units of curves */
  private static final int NB_UNITS = 2;
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
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_USD };
    DSC_MAP.put(CURVE_NAME_DSC_USD, USD);
    FWD_ON_MAP.put(CURVE_NAME_DSC_USD, new IndexON[] {USDFEDFUND });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_USD, new IborIndex[] {USDLIBOR3M });
  }

  /** Calculators */
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY =
      CurveCalibrationConventionDataSets.curveBuildingRepositoryMulticurve();

  /**
   * Calibrate curves with hard-coded date and with calibration date the date provided. The curves are discounting/overnight forward,
   * Libor3M forward, Libor1M forward and Libor6M forward.
   * @param calibrationDate The calibration date.
   * @return The curves and the Jacobian matrices.
   */
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUSDOisL3(ZonedDateTime calibrationDate,
      int nbImmSwaps, MulticurveProviderInterface multicurve) {
    ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(calibrationDate, USDLIBOR3M.getSpotLag(), NYC);
    ZonedDateTime[] immDates = new ZonedDateTime[nbImmSwaps + 1];
    for (int loopimm = 0; loopimm < nbImmSwaps + 1; loopimm++) {
      immDates[loopimm] = RollDateAdjusterUtils.nthDate(spotDate, IMM_QUARTERLY_ADJUSTER, loopimm + 1);
    }
    // Steps: 1) Create the instruments with market quote 0
    //        2) Compute the par spread market quote. As the initial quote is 0, the spread is equal to the actual market quote required.
    //        3) Create the instrument with market quote ATM
    /** Forward 3M curve **/
    /** Instruments 0 */
    InstrumentDefinition<?> dep0Definitions = new DepositIborDefinition(USD, calibrationDate, immDates[0], NOTIONAL, 0.0d,
        USDLIBOR3M.getDayCount().getDayCountFraction(calibrationDate, immDates[0]), USDLIBOR3M);
    InstrumentDefinition<?>[] swp0Definitions = generateImmIrs(immDates, new double[nbImmSwaps]);
    InstrumentDerivative[] fwd3m0 = new InstrumentDerivative[nbImmSwaps + 1];
    fwd3m0[0] = dep0Definitions.toDerivative(calibrationDate);
    for (int loopimm = 0; loopimm < nbImmSwaps; loopimm++) {
      fwd3m0[loopimm + 1] = swp0Definitions[loopimm].toDerivative(calibrationDate);
    }
    /** Market quote (using PSMQC) */
    double[] marketQuoteFwd3m = new double[nbImmSwaps + 1];
    for (int loopimm = 0; loopimm < nbImmSwaps + 1; loopimm++) {
      marketQuoteFwd3m[loopimm] = fwd3m0[loopimm].accept(PSMQC, multicurve);
    }
    /** Instruments ATM */
    InstrumentDefinition<?>[][][] definitionsUnits = new InstrumentDefinition<?>[NB_UNITS][][];
    InstrumentDefinition<?>[] fwd3Definitions = new InstrumentDefinition<?>[nbImmSwaps + 1];
    fwd3Definitions[0] = new DepositIborDefinition(USD, calibrationDate, immDates[0], NOTIONAL,
        marketQuoteFwd3m[0], USDLIBOR3M.getDayCount().getDayCountFraction(calibrationDate, immDates[0]), USDLIBOR3M);
    double[] parRateSwp = ArrayUtils.subarray(marketQuoteFwd3m, 1, nbImmSwaps + 1);
    InstrumentDefinition<?>[] swpDefinition = generateImmIrs(immDates, parRateSwp);
    for (int loopimm = 0; loopimm < nbImmSwaps; loopimm++) {
      fwd3Definitions[loopimm + 1] = swpDefinition[loopimm];
    }
    /** Dsc curve */
    /** Instruments 0 */
    InstrumentDefinition<?> dep0DscDefinitions = new CashDefinition(USD, calibrationDate, immDates[0], NOTIONAL, 0.0d,
        USDFEDFUND.getDayCount().getDayCountFraction(calibrationDate, immDates[0]));
    InstrumentDefinition<?>[] ois0Definitions = generateImmOis(immDates, new double[nbImmSwaps]);
    InstrumentDerivative[] dsc0 = new InstrumentDerivative[nbImmSwaps + 1];
    dsc0[0] = dep0DscDefinitions.toDerivative(calibrationDate);
    for (int loopimm = 0; loopimm < nbImmSwaps; loopimm++) {
      dsc0[loopimm + 1] = ois0Definitions[loopimm].toDerivative(calibrationDate);
    }
    /** Market quote (using PSMQC) */
    double[] marketQuoteDsc = new double[nbImmSwaps + 1];
    for (int loopimm = 0; loopimm < nbImmSwaps + 1; loopimm++) {
      marketQuoteDsc[loopimm] = dsc0[loopimm].accept(PSMQC, multicurve);
    }
    /** Instruments ATM */
    InstrumentDefinition<?>[] dscDefinitions = new InstrumentDefinition<?>[nbImmSwaps + 1];
    dscDefinitions[0] = new CashDefinition(USD, calibrationDate, immDates[0], NOTIONAL,
        marketQuoteDsc[0], USDFEDFUND.getDayCount().getDayCountFraction(calibrationDate, immDates[0]));
    InstrumentDefinition<?>[] oisDefinition = generateImmOis(immDates, ArrayUtils.subarray(marketQuoteDsc, 1, nbImmSwaps + 1));
    for (int loopimm = 0; loopimm < nbImmSwaps; loopimm++) {
      dscDefinitions[loopimm + 1] = oisDefinition[loopimm];
    }
    definitionsUnits[0] = new InstrumentDefinition<?>[][] {dscDefinitions };
    definitionsUnits[1] = new InstrumentDefinition<?>[][] {fwd3Definitions };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(calibrationDate, definitionsUnits,
        GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP,
        CURVE_BUILDING_REPOSITORY, TS_FIXED_OIS_USD_WITH_TODAY, TS_FIXED_OIS_USD_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITH_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY);
  }

  /**
   * Returns the array of Ibor index used in the curve data set. 
   * @return The array: USDLIBOR3M
   */
  public static IborIndex[] indexIborArrayUSDOisL3() {
    return new IborIndex[] {USDLIBOR3M };
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

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY };

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0035 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITHOUT_TODAY };

  private static InstrumentDefinition<?>[] generateImmIrs(ZonedDateTime[] immDates, double[] fixedRate) {
    int nbSwap = immDates.length - 1;
    SwapCouponFixedCouponDefinition[] swap = new SwapCouponFixedCouponDefinition[nbSwap];
    for (int loopimm = 0; loopimm < nbSwap; loopimm++) {
      PaymentDefinition[] cpn = new FixedAnnuityDefinitionBuilder().
          payer(true).
          currency(USD6MLIBOR3M.getCurrency()).
          notional(NOTIONAL_PROV).
          startDate(immDates[loopimm].toLocalDate()).
          endDate(immDates[loopimm + 1].toLocalDate()).
          dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
          accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).
          rate(fixedRate[loopimm]).
          accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
          build().getPayments();
      CouponFixedDefinition[] cpnFixed = new CouponFixedDefinition[cpn.length];
      for (int loopcpn = 0; loopcpn < cpn.length; loopcpn++) {
        cpnFixed[loopcpn] = (CouponFixedDefinition) cpn[loopcpn];
      }
      AnnuityCouponFixedDefinition fixedLegDefinition = new AnnuityCouponFixedDefinition(cpnFixed, NYC);
      AnnuityDefinition<? extends CouponDefinition> iborLegDefinition = (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().
          payer(false).
          notional(NOTIONAL_PROV).
          startDate(immDates[loopimm].toLocalDate()).
          endDate(immDates[loopimm + 1].toLocalDate()).
          index(USDLIBOR3M).
          accrualPeriodFrequency(USDLIBOR3M.getTenor()).
          rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).
          accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
          dayCount(USDLIBOR3M.getDayCount()).
          fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).
          currency(USDLIBOR3M.getCurrency()).
          build();
      swap[loopimm] = new SwapCouponFixedCouponDefinition(fixedLegDefinition, iborLegDefinition);
    }
    return swap;
  }

  private static InstrumentDefinition<?>[] generateImmOis(ZonedDateTime[] immDates, double[] fixedRate) {
    int nbSwap = immDates.length - 1;
    SwapCouponFixedCouponDefinition[] swap = new SwapCouponFixedCouponDefinition[nbSwap];
    for (int loopimm = 0; loopimm < nbSwap; loopimm++) {
      PaymentDefinition[] cpn = new FixedAnnuityDefinitionBuilder().
          payer(true).
          currency(USD6MLIBOR3M.getCurrency()).
          notional(NOTIONAL_PROV).
          startDate(immDates[loopimm].toLocalDate()).
          endDate(immDates[loopimm + 1].toLocalDate()).
          dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
          accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).
          rate(fixedRate[loopimm]).
          accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
          build().getPayments();
      CouponFixedDefinition[] cpnFixed = new CouponFixedDefinition[cpn.length];
      for (int loopcpn = 0; loopcpn < cpn.length; loopcpn++) {
        cpnFixed[loopcpn] = (CouponFixedDefinition) cpn[loopcpn];
      }
      AnnuityCouponFixedDefinition fixedLegDefinition = new AnnuityCouponFixedDefinition(cpnFixed, NYC);
      AnnuityDefinition<? extends CouponDefinition> onLegDefinition = (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().
          payer(false).
          notional(NOTIONAL_PROV).
          startDate(immDates[loopimm].toLocalDate()).
          endDate(immDates[loopimm + 1].toLocalDate()).
          index(USDFEDFUND).
          accrualPeriodFrequency(USDLIBOR3M.getTenor()).
          rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).
          accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
          dayCount(USDLIBOR3M.getDayCount()).
          fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).
          currency(USDLIBOR3M.getCurrency()).
          compoundingMethod(CompoundingMethod.FLAT).
          build();
      swap[loopimm] = new SwapCouponFixedCouponDefinition(fixedLegDefinition, onLegDefinition);
    }
    return swap;
  }

}
