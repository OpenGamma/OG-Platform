/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.LinkedListMultimap;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeET;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorBill;
import com.opengamma.analytics.financial.instrument.index.GeneratorBondFixed;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositONCounterpart;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *  Build of curve in several blocks with relevant Jacobian matrices.
 *  Here we build a discount curve as usual using OIS instruments and simultaneously we are building a governmental discount curve using US Bonds, bills and notes.
 */
@Test(groups = TestGroup.UNIT)
public class MulticurveBuildingDiscountingBillNoteBondUSDTest {

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2012, 8, 22);

  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final Currency USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON INDEX_ON_USD = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", USD, NYC, INDEX_ON_USD.getDayCount());
  private static final String NAME_COUNTERPART = "US GOVT";
  private static final DayCount DAY_COUNT_ON = DayCounts.ACT_360;
  private static final GeneratorDepositONCounterpart GENERATOR_DEPOSIT_ON_USGOVT = new GeneratorDepositONCounterpart("US GOVT Deposit ON", USD, NYC, DAY_COUNT_ON, NAME_COUNTERPART);

  private static final YieldConvention YIELD_BILL_USGOVT = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");
  private static final DayCount DAY_COUNT_BILL_USGOVT = DayCounts.ACT_360;
  private static final int SPOT_LAG_BILL = 1;
  private static final ZonedDateTime[] BILL_MATURITY = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2012, 11, 30), DateUtils.getUTCDate(2013, 2, 28) };
  private static final int NB_BILL = BILL_MATURITY.length;
  private static final BillSecurityDefinition[] BILL_SECURITY = new BillSecurityDefinition[NB_BILL];
  private static final GeneratorBill[] GENERATOR_BILL = new GeneratorBill[NB_BILL];
  static {
    for (int loopbill = 0; loopbill < BILL_MATURITY.length; loopbill++) {
      BILL_SECURITY[loopbill] = new BillSecurityDefinition(USD, BILL_MATURITY[loopbill], NOTIONAL, SPOT_LAG_BILL, NYC, YIELD_BILL_USGOVT, DAY_COUNT_BILL_USGOVT, NAME_COUNTERPART);
      GENERATOR_BILL[loopbill] = new GeneratorBill("GeneratorBill" + loopbill, BILL_SECURITY[loopbill]);
    }
  }
  // Here we define US NOTES and US BONDS, we are no doing no distinction between notes and bonds because the instrument is exactly the same.
  // typically US NOTES are short maturity interest rate bonds(ie under 10Y) and US BONDS are long maturity interest rate bonds (ie more than 10y but mostly 30y in practice).
  // To build the curve we choose six bonds, the most recent 2y, 3y, 5y, 7y, 10yand 30y bond :
  // USA, Note 0.125 31jul2014 2Y (ISIN US912828TF73)
  // USA, Note 0.25 15aug2015 3Y (ISIN US912828TK68)
  // USA, Note 0.5 31jul2017 5Y (ISIN US912828TG56)
  // USA, Note 0.875 31jul2019 7Y (ISIN US912828TH30)
  // USA, Note 1.625 15aug2022 10Y (ISIN US912828TJ95)
  // USA, Bond 2.75 15aug2042 30Y (ISIN US912810QX90)

  private static final YieldConvention YIELD_BOND_USGOVT = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");
  private static final DayCount DAY_COUNT_BOND_USGOVT = DayCounts.ACT_360;
  private static final Period BOND_PAYMENT_TENOR = Period.ofMonths(6);
  private static final ZonedDateTime[] BOND_START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 07, 31), DateUtils.getUTCDate(2012, 8, 15), DateUtils.getUTCDate(2012, 07, 31),
    DateUtils.getUTCDate(2012, 07, 31), DateUtils.getUTCDate(2012, 8, 15), DateUtils.getUTCDate(2012, 8, 15) };
  private static final ZonedDateTime[] BOND_MATURITY = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 07, 31), DateUtils.getUTCDate(2015, 05, 15), DateUtils.getUTCDate(2017, 07, 31),
    DateUtils.getUTCDate(2019, 07, 31), DateUtils.getUTCDate(2022, 8, 15), DateUtils.getUTCDate(2042, 8, 15) };
  private static final double[] RATE_FIXED = new double[] {0.00125, 0.00250, 0.00500, 0.00875, 0.01625, 0.02750 };
  private static final int NB_BOND = BOND_MATURITY.length;
  private static final int SETTLEMENT_DAYS_US = 3;
  private static final boolean IS_EOM_FIXED = false;
  private static final String REPO_TYPE = "General collateral";
  private static final BusinessDayConvention BOND_BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final BondFixedSecurityDefinition[] BOND_SECURITY = new BondFixedSecurityDefinition[NB_BOND];
  private static final GeneratorBondFixed[] GENERATOR_BOND = new GeneratorBondFixed[NB_BOND];
  static {
    for (int loopbill = 0; loopbill < BOND_MATURITY.length; loopbill++) {
      BOND_SECURITY[loopbill] = BondFixedSecurityDefinition.from(USD, BOND_MATURITY[loopbill], BOND_START_ACCRUAL_DATE[loopbill], BOND_PAYMENT_TENOR, RATE_FIXED[loopbill], SETTLEMENT_DAYS_US,
          NOTIONAL, NYC,
          DAY_COUNT_BOND_USGOVT, BOND_BUSINESS_DAY, YIELD_BOND_USGOVT, IS_EOM_FIXED, NAME_COUNTERPART, REPO_TYPE);
      GENERATOR_BOND[loopbill] = new GeneratorBondFixed("GeneratorBond" + loopbill, BOND_SECURITY[loopbill]);
    }
  }

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY };

  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  private static final String CURVE_NAME_GOVTUS_USD = "USD GOVT US";

  /** Market values for the dsc USD curve */
  private static final double[] DSC_USD_MARKET_QUOTES = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };

  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_USD_ATTR = new GeneratorAttributeIR[DSC_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < DSC_USD_TENOR.length; loopins++) {
      DSC_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_USD_TENOR[loopins]);
    }
  }

  /** Market values for the govt USD bill curve */
  private static final double[] GOVTUS_USD_MARKET_QUOTES = 
      new double[] {0.0010, 0.0015, 0.0020, 0.0015, 0.99642, 0.9981, 0.99587, 0.99466, 0.99496, 0.98489 };
  /** Generators for the govt USD curve */
  private static final int NB_ON_GOVT = 1;
  private static final int NB_BILL_GOVT = 3;
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] GOVTUS_USD_GENERATORS = 
      new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USGOVT, 
    GENERATOR_BILL[0], GENERATOR_BILL[1], GENERATOR_BILL[2], 
    GENERATOR_BOND[0], GENERATOR_BOND[1], GENERATOR_BOND[2], GENERATOR_BOND[3], GENERATOR_BOND[4], GENERATOR_BOND[5] };
  /** Tenors for the govt USD curve */
  private static final Period[] GOVTUS_USD_TENOR = new Period[] {Period.ofDays(0) };
  private static final GeneratorAttribute[] GOVTUS_USD_ATTR = new GeneratorAttribute[GOVTUS_USD_GENERATORS.length];
  static {
    for (int loopins = 0; loopins < NB_ON_GOVT; loopins++) {
      GOVTUS_USD_ATTR[loopins] = new GeneratorAttributeIR(GOVTUS_USD_TENOR[loopins]);
    }
    for (int loopins = NB_ON_GOVT; loopins < NB_ON_GOVT + NB_BILL_GOVT; loopins++) {
      GOVTUS_USD_ATTR[loopins] = new GeneratorAttributeET(false);
    }
    for (int loopins = NB_ON_GOVT + NB_BILL_GOVT; loopins < GOVTUS_USD_GENERATORS.length; loopins++) {
      GOVTUS_USD_ATTR[loopins] = new GeneratorAttributeET(true);
    }
  }

  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_GOVTUS_USD;

  /** Units of curves */
  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {2 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_MULTICURVES = new MulticurveProviderDiscount(FX_MATRIX);
  private static final IssuerProviderDiscount KNOWN_DATA = new IssuerProviderDiscount(KNOWN_MULTICURVES, new HashMap<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve>());
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();
  private static final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> DSC_ISS_MAP = LinkedListMultimap.create();

  static {
    DEFINITIONS_DSC_USD = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_ATTR);
    DEFINITIONS_GOVTUS_USD = getDefinitions(GOVTUS_USD_MARKET_QUOTES, GOVTUS_USD_GENERATORS, GOVTUS_USD_ATTR);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_GOVTUS_USD };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_GOVTUS_USD };
    DSC_MAP.put(CURVE_NAME_DSC_USD, USD);
    FWD_ON_MAP.put(CURVE_NAME_DSC_USD, new IndexON[] {INDEX_ON_USD });
    DSC_ISS_MAP.put(CURVE_NAME_GOVTUS_USD, Pairs.of((Object) NAME_COUNTERPART, (LegalEntityFilter<LegalEntity>) new LegalEntityShortName()));
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  private static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(NOW, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  private static List<Pair<IssuerProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();

  // Calculator
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator PSMQIC = ParSpreadMarketQuoteIssuerDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator PSMQCSIC = ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();

  private static final IssuerDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = new IssuerDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final double TOLERANCE_CAL = 1.0E-9;

  @BeforeSuite
  static void initClass() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], KNOWN_DATA, PSMQIC, PSMQCSIC, false));
    }
  }

  @Test
  public void curveConstruction() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      curveConstructionCode(DEFINITIONS_UNITS[loopblock], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), false, loopblock);
    }
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQIC, PSMQCSIC, false);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units: 16-Aygust-13: On Dell Precision T1850 3.5 GHz Quad-Core Intel Xeon: 270 (no Jac)/951 ms for 100 sets.

  }

  private void curveConstructionCode(final InstrumentDefinition<?>[][][] definitions, final IssuerProviderDiscount curves, final boolean withToday, final int block) {
    final int nbBlocks = definitions.length;
    for (int loopblock = 0; loopblock < nbBlocks; loopblock++) {
      final InstrumentDerivative[][] instruments = convert(definitions[loopblock], loopblock, withToday);
      final double[][] pv = new double[instruments.length][];
      for (int loopcurve = 0; loopcurve < instruments.length; loopcurve++) {
        pv[loopcurve] = new double[instruments[loopcurve].length];
        for (int loopins = 0; loopins < instruments[loopcurve].length; loopins++) {
          pv[loopcurve][loopins] = curves.getMulticurveProvider().getFxRates().convert(instruments[loopcurve][loopins].accept(PVIC, curves), USD).getAmount();
          assertEquals("Curve construction: block " + block + ", unit " + loopblock + " - instrument " + loopins, 0, pv[loopcurve][loopins], TOLERANCE_CAL);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators,
      final String[][] curveNames, final IssuerProviderDiscount knownData, final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] initialGuess = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k], i, withToday);
          initialGuess[k] = initialGuess(definitions[i][j][k]);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, DSC_ISS_MAP, calculator,
        sensitivityCalculator);
  }

  private static InstrumentDerivative[][] convert(final InstrumentDefinition<?>[][] definitions, final int unit, final boolean withToday) {
    final InstrumentDerivative[][] instruments = new InstrumentDerivative[definitions.length][];
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      instruments[loopcurve] = new InstrumentDerivative[definitions[loopcurve].length];
      int loopins = 0;
      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
        InstrumentDerivative ird;
        if (instrument instanceof SwapFixedONDefinition) {
          ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(withToday, unit));
        } else {
          ird = instrument.toDerivative(NOW);
        }
        instruments[loopcurve][loopins++] = ird;
      }
    }
    return instruments;
  }

  private static InstrumentDerivative convert(final InstrumentDefinition<?> instrument, final int unit, final boolean withToday) {
    InstrumentDerivative ird;
    if (instrument instanceof SwapFixedONDefinition) {
      ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(withToday, unit));
    } else {
      ird = instrument.toDerivative(NOW);
    }
    return ird;
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedON(final Boolean withToday, final Integer unit) {
    switch (unit) {
      case 0:
        return withToday ? TS_FIXED_OIS_USD_WITH_TODAY : TS_FIXED_OIS_USD_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  private static double initialGuess(final InstrumentDefinition<?> instrument) {
    if (instrument instanceof SwapFixedONDefinition) {
      return ((SwapFixedONDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
    }
    if (instrument instanceof SwapFixedIborDefinition) {
      return ((SwapFixedIborDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
    }
    if (instrument instanceof ForwardRateAgreementDefinition) {
      return ((ForwardRateAgreementDefinition) instrument).getRate();
    }
    if (instrument instanceof CashDefinition) {
      return ((CashDefinition) instrument).getRate();
    }
    return 0.01;
  }

}
