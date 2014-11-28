/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.LinkedListMultimap;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarGBP;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondDataSetsGbp;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeET;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorBill;
import com.opengamma.analytics.financial.instrument.index.GeneratorBondFixed;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Curves calibration in GBP: 
 * 0) DSCON-OIS/CCYIS-UKGVT
 * Data stored in snapshots for comparison with platform.
 */
public class StandardDataSetsBondCurveGBP {

  private static final ZonedDateTime[] REFERENCE_DATE = new ZonedDateTime[1];
  static {
    REFERENCE_DATE[0] = DateUtils.getUTCDate(2014, 7, 11);
  }

  private static final Calendar LON = new CalendarGBP("Lon");
  private static final Currency GBP = Currency.GBP;
  private static final FXMatrix FX_MATRIX = new FXMatrix(GBP);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedON GENERATOR_OIS_GBP = GENERATOR_OIS_MASTER.getGenerator("GBP1YSONIA", LON);
  private static final IndexON GBPSONIA = GENERATOR_OIS_GBP.getIndex();
  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_GBP_WITH_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_GBP_WITHOUT_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_GBP_WITH_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_GBP_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_GBP_WITHOUT_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_GBP_WITHOUT_TODAY };

  private static final String CURVE_NAME_DSC_GBP = "GBP-DSCON-OIS";
  private static final String CURVE_NAME_GOVTUK_GBP = "GBP-CCYIS-UKGVT";

  /** Market values for the dsc GBP curve */
  private static final double[] DSC_1_GBP_MARKET_QUOTES = new double[] {0.004225, 0.004215,
    0.00424, 0.00422, 0.004226, 0.004303, 0.0045095,
    0.0049, 0.0076675, 0.010975, 0.0136605, 0.01583,
    0.01768, 0.019249, 0.020603, 0.0218265, 0.022898,
    0.024725999999999998, 0.026638, 0.028471, 0.029667 }; //21
  /** Generators for the dsc GBP curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_1_GBP_GENERATORS =
      CurveCalibrationConventionDataSets.generatorGbpOnOis(2, 19);
  /** Tenors for the dsc GBP curve */
  private static final Period[] DSC_1_GBP_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10),
    Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] DSC_1_GBP_ATTR = new GeneratorAttributeIR[DSC_1_GBP_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      DSC_1_GBP_ATTR[loopins] = new GeneratorAttributeIR(DSC_1_GBP_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = 2; loopins < DSC_1_GBP_TENOR.length; loopins++) {
      DSC_1_GBP_ATTR[loopins] = new GeneratorAttributeIR(DSC_1_GBP_TENOR[loopins]);
    }
  }

  /** Market values for the UKT GBP curve */ /** ISIN: GB00BDNHF692 - GB00BDNJY806 */
  private static final ZonedDateTime[] BILL_MATURITY = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 10, 6),
    DateUtils.getUTCDate(2015, 1, 5) };
  private static final int NB_BILL = BILL_MATURITY.length;
  private static final BillSecurityDefinition[] BILL_SECURITY = new BillSecurityDefinition[NB_BILL];
  private static final GeneratorBill[] GENERATOR_BILL = new GeneratorBill[NB_BILL];
  static {
    for (int loopbill = 0; loopbill < BILL_MATURITY.length; loopbill++) {
      BILL_SECURITY[loopbill] = BondDataSetsGbp.billUK(NOTIONAL, BILL_MATURITY[loopbill]);
      GENERATOR_BILL[loopbill] = new GeneratorBill("GeneratorBill" + loopbill, BILL_SECURITY[loopbill]);
    }
  }
  private static final String GOVT_UK_ISSUER_NAME = BILL_SECURITY[0].getIssuer();
  private static final int NB_BOND = 3;
  private static final BondFixedSecurityDefinition[] BOND_SECURITY = new BondFixedSecurityDefinition[NB_BOND];
  private static final GeneratorBondFixed[] GENERATOR_BOND = new GeneratorBondFixed[NB_BOND];
  static {
    BOND_SECURITY[0] = BondDataSetsGbp.bondUKT2_20160122(NOTIONAL);
    BOND_SECURITY[1] = BondDataSetsGbp.bondUKT175_20190722(NOTIONAL);
    BOND_SECURITY[2] = BondDataSetsGbp.bondUKT225_20230907(NOTIONAL);
    for (int loopbnd = 0; loopbnd < NB_BOND; loopbnd++) {
      GENERATOR_BOND[loopbnd] = new GeneratorBondFixed("GeneratorBond" + loopbnd, BOND_SECURITY[loopbnd]);
    }
  }
  /** Market values for the govt GBP bill curve */
  private static final double[] GOVTUK_GBP_MARKET_QUOTES = new double[] {0.0040, 0.0050, 0.0070, 0.0150, 0.0250 };
  /** Generators for the govt GBP curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] GOVTUK_GBP_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_BILL[0], GENERATOR_BILL[1],
        GENERATOR_BOND[0], GENERATOR_BOND[1], GENERATOR_BOND[2] };
  /** Tenors for the govt USD curve */
  private static final GeneratorAttributeET[] GOVTUK_GBP_ATTR = new GeneratorAttributeET[GOVTUK_GBP_MARKET_QUOTES.length];
  static {
    for (int loopins = 0; loopins < GOVTUK_GBP_MARKET_QUOTES.length; loopins++) {
      GOVTUK_GBP_ATTR[loopins] = new GeneratorAttributeET(false);
    }
  }

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {2 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_MULTICURVES = new MulticurveProviderDiscount(FX_MATRIX);
  private static final IssuerProviderDiscount KNOWN_DATA =
      new IssuerProviderDiscount(KNOWN_MULTICURVES, new HashMap<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve>());
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();
  private static final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> DSC_ISS_MAP = LinkedListMultimap.create();

  static {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    final GeneratorYDCurve genIntLin = CurveCalibrationConventionDataSets.generatorYDMatLin();
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_GBP };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_GOVTUK_GBP };
    DSC_MAP.put(CURVE_NAME_DSC_GBP, GBP);
    FWD_ON_MAP.put(CURVE_NAME_DSC_GBP, new IndexON[] {GBPSONIA });
    DSC_ISS_MAP.put(CURVE_NAME_GOVTUK_GBP, Pairs.of((Object) GOVT_UK_ISSUER_NAME, (LegalEntityFilter<LegalEntity>) new LegalEntityShortName()));
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public static InstrumentDefinition<?>[] getDefinitions(double[] marketQuotes, GeneratorInstrument[] generators, 
      GeneratorAttribute[] attribute, ZonedDateTime referenceDate) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(referenceDate, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  // Calculator
  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator PSMQIC = 
      ParSpreadMarketQuoteIssuerDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator PSMQCSIC = 
      ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();

  private static final IssuerDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = 
      CurveCalibrationConventionDataSets.curveBuildingRepositoryIssuer();

  public static Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> getCurvesGBPSoniaGovt(ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[] dscDefinition = 
        getDefinitions(DSC_1_GBP_MARKET_QUOTES, DSC_1_GBP_GENERATORS, DSC_1_GBP_ATTR, calibrationDate);
    InstrumentDefinition<?>[] gvtDefinition = 
        getDefinitions(GOVTUK_GBP_MARKET_QUOTES, GOVTUK_GBP_GENERATORS, GOVTUK_GBP_ATTR, calibrationDate);
    InstrumentDefinition<?>[][][] unitsDefinition = new InstrumentDefinition<?>[2][][];
    unitsDefinition[0] = new InstrumentDefinition<?>[][] {dscDefinition };
    unitsDefinition[1] = new InstrumentDefinition<?>[][] {gvtDefinition };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsIssuer(
        calibrationDate, unitsDefinition, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA,
        PSMQIC, PSMQCSIC, false, DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, DSC_ISS_MAP, CURVE_BUILDING_REPOSITORY,
        TS_FIXED_OIS_GBP_WITH_TODAY, TS_FIXED_OIS_GBP_WITHOUT_TODAY, TS_FIXED_OIS_GBP_WITH_TODAY, TS_FIXED_OIS_GBP_WITHOUT_TODAY);
  }

}
