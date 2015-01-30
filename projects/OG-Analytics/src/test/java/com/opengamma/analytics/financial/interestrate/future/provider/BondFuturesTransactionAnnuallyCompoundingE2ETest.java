/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceIssuerCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.CleanPriceFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ZSpreadIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderIssuerAnnuallyCompoundeding;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings({"unchecked", "rawtypes" })
public class BondFuturesTransactionAnnuallyCompoundingE2ETest {
  private static final BondAndSTIRFuturesE2EExamplesData DATA = new BondAndSTIRFuturesE2EExamplesData();
  private static final Calendar EUR_CALENDAR = DATA.getEURCalendar();
  private static final Calendar GBP_CALENDAR = DATA.getGBPCalendar();
  private static final Currency EUR = Currency.EUR;
  private static final Currency GBP = Currency.GBP;
  private static final ZonedDateTime VALUATION_DATE = ZonedDateTime.of(2014, 2, 17, 9, 0, 0, 0, ZoneId.of("Z"));

  /* names */
  private static final String ISSUER_NAME_LGT = "LGT GBP";
  private static final String ISSUER_NAME_SCH = "SCH EUR";
  private static final String ISSUER_NAME_BUN = "BUN EUR";
  private static final String ISSUER_NAME_BOB = "BOB EUR";
  private static final String CURVE_NAME_LGT = "LGT GBPBond";
  private static final String CURVE_NAME_SCH = "SCH EURBond";
  private static final String CURVE_NAME_BUN = "BUN EURBond";
  private static final String CURVE_NAME_BOB = "BOB EURBond";

  /* issuer curves */
  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final IssuerProviderIssuerAnnuallyCompoundeding ISSUER_PROVIDER_LGT;
  static {
    IssuerProviderDiscount issuerProvider = new IssuerProviderDiscount();
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(DATA.getTimeLGT(),
        DATA.getRateLGT(), INTERPOLATOR, CURVE_NAME_LGT);
    YieldPeriodicCurve yieldCurve = YieldPeriodicCurve.from(1, interpolatedCurve);
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    issuerProvider.setCurve(Pairs.of((Object) ISSUER_NAME_LGT, filter), yieldCurve);
    ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(DATA.getRepoLGT());
    YieldPeriodicCurve repoCurve = YieldPeriodicCurve.from(1, constantDoublesCurve);
    issuerProvider.setCurve(GBP, repoCurve);
    ISSUER_PROVIDER_LGT = new IssuerProviderIssuerAnnuallyCompoundeding(issuerProvider);
  }
  private static final IssuerProviderIssuerAnnuallyCompoundeding ISSUER_PROVIDER_SCH;
  static {
    IssuerProviderDiscount issuerProvider = new IssuerProviderDiscount();
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(DATA.getTimeSCH(),
        DATA.getRateSCH(), INTERPOLATOR, CURVE_NAME_SCH);
    YieldPeriodicCurve yieldCurve = YieldPeriodicCurve.from(1, interpolatedCurve);
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    issuerProvider.setCurve(Pairs.of((Object) ISSUER_NAME_SCH, filter), yieldCurve);
    ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(DATA.getRepoSCH());
    YieldPeriodicCurve repoCurve = YieldPeriodicCurve.from(1, constantDoublesCurve);
    issuerProvider.setCurve(EUR, repoCurve);
    ISSUER_PROVIDER_SCH = new IssuerProviderIssuerAnnuallyCompoundeding(issuerProvider);
  }
  private static final IssuerProviderIssuerAnnuallyCompoundeding ISSUER_PROVIDER_BUN;
  static {
    IssuerProviderDiscount issuerProvider = new IssuerProviderDiscount();
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(DATA.getTimeBUN(),
        DATA.getRateBUN(), INTERPOLATOR, CURVE_NAME_BUN);
    YieldPeriodicCurve yieldCurve = YieldPeriodicCurve.from(1, interpolatedCurve);
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    issuerProvider.setCurve(Pairs.of((Object) ISSUER_NAME_BUN, filter), yieldCurve);
    ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(DATA.getRepoBUN());
    YieldPeriodicCurve repoCurve = YieldPeriodicCurve.from(1, constantDoublesCurve);
    issuerProvider.setCurve(EUR, repoCurve);
    ISSUER_PROVIDER_BUN = new IssuerProviderIssuerAnnuallyCompoundeding(issuerProvider);
  }
  private static final IssuerProviderIssuerAnnuallyCompoundeding ISSUER_PROVIDER_BOB;
  static {
    IssuerProviderDiscount issuerProvider = new IssuerProviderDiscount();
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(DATA.getTimeBOB(),
        DATA.getRateBOB(), INTERPOLATOR, CURVE_NAME_BOB);
    YieldPeriodicCurve yieldCurve = YieldPeriodicCurve.from(1, interpolatedCurve);
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    issuerProvider.setCurve(Pairs.of((Object) ISSUER_NAME_BOB, filter), yieldCurve);
    ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(DATA.getRepoBOB());
    YieldPeriodicCurve repoCurve = YieldPeriodicCurve.from(1, constantDoublesCurve);
    issuerProvider.setCurve(EUR, repoCurve);
    ISSUER_PROVIDER_BOB = new IssuerProviderIssuerAnnuallyCompoundeding(issuerProvider);
  }

  /* Common setting */
  private static final int QUANTITY = 1;
  private static final double NOTIONAL = 1000.0;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static ZonedDateTime TRADE_DATE = ZonedDateTime.of(2008, 8, 27, 1, 0, 0, 0, ZoneId.of("Z")); // 2008-08-27T01:00Z
  private static final double TRADE_PRICE = 0.0;
  private static final double LAST_MARGIN_PRICE = 0.0;

  /* Long Gilt bond futures */
  private static final BondFuturesTransaction TRANSACTION_LGT;
  private static final double BOND_MARKET_PRICE_LGT = 120.0325;
  static {
    ZonedDateTime tradingLastDate = ZonedDateTime.of(2014, 3, 31, 23, 59, 0, 0, ZoneId.of("Z")); // 2014-03-31T23:59Z
    ZonedDateTime noticeFirstDate = ZonedDateTime.of(2014, 3, 31, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime noticeLastDate = ZonedDateTime.of(2014, 3, 31, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryFirstDate = ZonedDateTime.of(2014, 3, 31, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryLastDate = ZonedDateTime.of(2014, 3, 31, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime firstAccrualDate = ZonedDateTime.of(2001, 9, 27, 0, 0, 0, 0, ZoneId.of("Z")); // 2001-09-27T00:00Z
    ZonedDateTime firstCouponDate = ZonedDateTime.of(2002, 3, 7, 0, 0, 0, 0, ZoneId.of("Z")); // 2002-03-07T00:00Z
    ZonedDateTime maturityDate = ZonedDateTime.of(2025, 3, 7, 0, 0, 0, 0, ZoneId.of("Z")); // 2025-03-07T00:00Z
    Period paymentPeriod = Period.ofMonths(6);
    double fixedRate = 0.05;
    int settlementDays = 1;
    YieldConvention yieldConvention = SimpleYieldConvention.UK_BUMP_DMO_METHOD;
    boolean isEOM = false;
    LegalEntity legalEntity = new LegalEntity(null, ISSUER_NAME_LGT, null, Sector.of("GB"), Region.of("GB",
        Country.of("GB"), GBP));
    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(GBP, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, fixedRate, settlementDays, GBP_CALENDAR, DAY_COUNT, BUSINESS_DAY, yieldConvention,
        isEOM, legalEntity);
    BondFixedSecurityDefinition[] deliveryBasket = new BondFixedSecurityDefinition[] {bondFixed };
    double[] conversionFactor = new double[] {1.088405 };
    BondFuturesSecurityDefinition bondFuturesDefinition = new BondFuturesSecurityDefinition(tradingLastDate,
        noticeFirstDate, noticeLastDate, deliveryFirstDate, deliveryLastDate, NOTIONAL, deliveryBasket,
        conversionFactor);
    BondFuturesTransactionDefinition transactionDefinition = new BondFuturesTransactionDefinition(
        bondFuturesDefinition, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_LGT = transactionDefinition.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE);
  }

  /* Schatz */
  private static final BondFuturesTransaction TRANSACTION_SCH;
  private static final double BOND_MARKET_PRICE_SCH = 109.17;
  static {
    ZonedDateTime tradingLastDate = ZonedDateTime.of(2014, 9, 10, 23, 59, 0, 0, ZoneId.of("Z")); // 2014-09-10T23:59Z
    ZonedDateTime noticeFirstDate = ZonedDateTime.of(2014, 9, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime noticeLastDate = ZonedDateTime.of(2014, 9, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryFirstDate = ZonedDateTime.of(2014, 9, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryLastDate = ZonedDateTime.of(2014, 9, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime firstAccrualDate = ZonedDateTime.of(2006, 5, 19, 0, 0, 0, 0, ZoneId.of("Z")); // 2006-05-19T00:00Z
    ZonedDateTime firstCouponDate = ZonedDateTime.of(2007, 7, 4, 0, 0, 0, 0, ZoneId.of("Z")); // 2007-07-04T00:00Z
    ZonedDateTime maturityDate = ZonedDateTime.of(2016, 7, 4, 0, 0, 0, 0, ZoneId.of("Z")); // 2016-07-04T00:00Z
    Period paymentPeriod = Period.ofMonths(12);
    double fixedRate = 0.04;
    int settlementDays = 3;
    YieldConvention yieldConvention = SimpleYieldConvention.GERMAN_BOND;
    boolean isEOM = false;
    LegalEntity legalEntity = new LegalEntity(null, ISSUER_NAME_SCH, null, Sector.of("EU"), Region.of("EU",
        Country.of("EU"), EUR));
    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(EUR, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, fixedRate, settlementDays, EUR_CALENDAR, DAY_COUNT, BUSINESS_DAY, yieldConvention,
        isEOM, legalEntity);
    BondFixedSecurityDefinition[] deliveryBasket = new BondFixedSecurityDefinition[] {bondFixed };
    double[] conversionFactor = new double[] {0.966395 };
    BondFuturesSecurityDefinition bondFuturesDefinition = new BondFuturesSecurityDefinition(tradingLastDate,
        noticeFirstDate, noticeLastDate, deliveryFirstDate, deliveryLastDate, NOTIONAL, deliveryBasket,
        conversionFactor);
    BondFuturesTransactionDefinition transactionDefinition = new BondFuturesTransactionDefinition(
        bondFuturesDefinition, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_SCH = transactionDefinition.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE);
  }

  /* Bund */
  private static final BondFuturesTransaction TRANSACTION_BUN;
  private static final double BOND_MARKET_PRICE_BUN = 103.61;
  static {
    ZonedDateTime tradingLastDate = ZonedDateTime.of(2014, 9, 10, 23, 59, 0, 0, ZoneId.of("Z")); // 2014-09-10T23:59Z
    ZonedDateTime noticeFirstDate = ZonedDateTime.of(2014, 9, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime noticeLastDate = ZonedDateTime.of(2014, 9, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryFirstDate = ZonedDateTime.of(2014, 9, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryLastDate = ZonedDateTime.of(2014, 9, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime firstAccrualDate = ZonedDateTime.of(2013, 9, 13, 0, 0, 0, 0, ZoneId.of("Z")); // 2013-09-13T00:00Z
    ZonedDateTime firstCouponDate = ZonedDateTime.of(2014, 8, 15, 0, 0, 0, 0, ZoneId.of("Z")); // 2014-08-15T00:00Z
    ZonedDateTime maturityDate = ZonedDateTime.of(2023, 8, 15, 0, 0, 0, 0, ZoneId.of("Z")); // 2023-08-15T00:00Z
    Period paymentPeriod = Period.ofMonths(12);
    double fixedRate = 0.02;
    int settlementDays = 3;
    YieldConvention yieldConvention = SimpleYieldConvention.GERMAN_BOND;
    boolean isEOM = false;
    LegalEntity legalEntity = new LegalEntity(null, ISSUER_NAME_BUN, null, Sector.of("EU"), Region.of("EU",
        Country.of("EU"), EUR));
    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(EUR, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, fixedRate, settlementDays, EUR_CALENDAR, DAY_COUNT, BUSINESS_DAY, yieldConvention,
        isEOM, legalEntity);
    BondFixedSecurityDefinition[] deliveryBasket = new BondFixedSecurityDefinition[] {bondFixed };
    double[] conversionFactor = new double[] {0.729535 };
    BondFuturesSecurityDefinition bondFuturesDefinition = new BondFuturesSecurityDefinition(tradingLastDate,
        noticeFirstDate, noticeLastDate, deliveryFirstDate, deliveryLastDate, NOTIONAL, deliveryBasket,
        conversionFactor);
    BondFuturesTransactionDefinition transactionDefinition = new BondFuturesTransactionDefinition(
        bondFuturesDefinition, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_BUN = transactionDefinition.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE);
  }

  /* Bobl */
  private static final BondFuturesTransaction TRANSACTION_BOB;
  private static final double BOND_MARKET_PRICE_BOB = 115.195;
  static {
    ZonedDateTime tradingLastDate = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z")); // 2014-06-10T23:59Z
    ZonedDateTime noticeFirstDate = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime noticeLastDate = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryFirstDate = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryLastDate = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime firstAccrualDate = ZonedDateTime.of(2008, 11, 14, 0, 0, 0, 0, ZoneId.of("Z")); // 2008-11-14T00:00Z
    ZonedDateTime firstCouponDate = ZonedDateTime.of(2010, 1, 4, 0, 0, 0, 0, ZoneId.of("Z")); // 2010-01-04T00:00Z
    ZonedDateTime maturityDate = ZonedDateTime.of(2019, 1, 4, 0, 0, 0, 0, ZoneId.of("Z")); // 2019-01-04T00:00Z
    Period paymentPeriod = Period.ofMonths(12);
    double fixedRate = 0.0375;
    int settlementDays = 3;
    YieldConvention yieldConvention = SimpleYieldConvention.GERMAN_BOND;
    boolean isEOM = false;
    LegalEntity legalEntity = new LegalEntity(null, ISSUER_NAME_BOB, null, Sector.of("EU"), Region.of("EU",
        Country.of("EU"), EUR));
    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(EUR, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, fixedRate, settlementDays, EUR_CALENDAR, DAY_COUNT, BUSINESS_DAY, yieldConvention,
        isEOM, legalEntity);
    BondFixedSecurityDefinition[] deliveryBasket = new BondFixedSecurityDefinition[] {bondFixed };
    double[] conversionFactor = new double[] {0.912067 };
    BondFuturesSecurityDefinition bondFuturesDefinition = new BondFuturesSecurityDefinition(
        tradingLastDate, noticeFirstDate, noticeLastDate, deliveryFirstDate, deliveryLastDate, NOTIONAL,
        deliveryBasket, conversionFactor);
    BondFuturesTransactionDefinition transactionDefinition = new BondFuturesTransactionDefinition(
        bondFuturesDefinition, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_BOB = transactionDefinition.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE);
  }

  /* underlying calculators */
  private static final BondSecurityDiscountingMethod BOND_METHOD = BondSecurityDiscountingMethod.getInstance();

  /* calculator */
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator
      .getInstance();
  private static final ZSpreadIssuerCalculator ZSIC = ZSpreadIssuerCalculator.getInstance();
  private static final FuturesPriceIssuerCalculator FPIC = FuturesPriceIssuerCalculator.getInstance();
  private static final CleanPriceFromCurvesCalculator BCPCC = CleanPriceFromCurvesCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterIssuerProviderInterface> PSSFC =
      new ParameterSensitivityParameterCalculator<>(PVCSIC);
  private static final PV01CurveParametersCalculator<ParameterIssuerProviderInterface> PV01PC =
      new PV01CurveParametersCalculator<>(PVCSIC);

  private static final double HUNDRED = 100.0;
  private static final double BP1 = 1.0E-4;
  private static final double TOL = 1.0e-12;

  /**
   * LGT.
   */
  @Test
  public void LGTTest() {
    BondFuturesSecurity futures = TRANSACTION_LGT.getUnderlyingSecurity();
    BondFixedSecurity bondAtSpot = futures.getDeliveryBasketAtSpotDate()[0];
    LegalEntity legalEntity = bondAtSpot.getIssuerEntity();
    Pair pair = Pairs.of(ISSUER_PROVIDER_LGT, BOND_MARKET_PRICE_LGT / HUNDRED);
    double spreadComputed = bondAtSpot.accept(ZSIC, pair);
    IssuerProviderIssuerAnnuallyCompoundeding curveWithSpread = new IssuerProviderIssuerAnnuallyCompoundeding(
        ISSUER_PROVIDER_LGT, legalEntity, spreadComputed * BP1);
    double priceFuturesComputed = futures.accept(FPIC, curveWithSpread) * HUNDRED;
    MultipleCurrencyAmount pvTransactionComputed = TRANSACTION_LGT.accept(PVIC, curveWithSpread);
    DoubleMatrix1D bucketedTransactionComputed = PSSFC.calculateSensitivity(TRANSACTION_LGT,
        curveWithSpread).multipliedBy(BP1).getSensitivity(CURVE_NAME_LGT, GBP);
    double pv01TransactionComputed = TRANSACTION_LGT.accept(PV01PC, curveWithSpread).getMap()
        .get(Pairs.of(CURVE_NAME_LGT, GBP));
    
    double[] bucketedTransactionExpected = new double[] {0.0, 0.0, 0.0, 0.0, -0.0011122245888023643,
        -0.003997854867319321, -0.008988291553698725, -0.013186926257806753, -0.016986996812854194,
        -0.02042640862462137, -0.02365558027478078, -0.026633232407914608, -0.029110509385756628, -0.03125200300991818,
        -0.6131706345490692, -0.1539265852345107, 0.0, 0.0 };
    assertRelative("LGT", 2.1717086134868425, spreadComputed, TOL);
    assertRelative("LGT", 109.7890588277471, priceFuturesComputed, TOL);
    assertRelative("LGT", 1097.890588277471, pvTransactionComputed.getAmount(GBP), TOL);
    assertArrayRelative("LGT", bucketedTransactionExpected, bucketedTransactionComputed.getData(), TOL);
    assertRelative("LGT", -0.9424472475670528, pv01TransactionComputed, TOL);
  }

  /**
   * SCH
   */
  @Test
  public void SCHTest() {
    BondFuturesSecurity futures = TRANSACTION_SCH.getUnderlyingSecurity();
    BondFixedSecurity bondAtSpot = futures.getDeliveryBasketAtSpotDate()[0];
    LegalEntity legalEntity = bondAtSpot.getIssuerEntity();
    Pair pair = Pairs.of(ISSUER_PROVIDER_SCH, BOND_MARKET_PRICE_SCH / HUNDRED);
    double spreadComputed = bondAtSpot.accept(ZSIC, pair);
    IssuerProviderIssuerAnnuallyCompoundeding curveWithSpread = new IssuerProviderIssuerAnnuallyCompoundeding(
        ISSUER_PROVIDER_SCH, legalEntity, spreadComputed * BP1);
    double priceFuturesComputed = futures.accept(FPIC, curveWithSpread) * HUNDRED;
    MultipleCurrencyAmount pvTransactionComputed = TRANSACTION_SCH.accept(PVIC, curveWithSpread);
    DoubleMatrix1D bucketedTransactionComputed = PSSFC.calculateSensitivity(TRANSACTION_SCH,
        curveWithSpread).multipliedBy(BP1).getSensitivity(CURVE_NAME_SCH, EUR);
    double pv01TransactionComputed = TRANSACTION_SCH.accept(PV01PC, curveWithSpread).getMap()
        .get(Pairs.of(CURVE_NAME_SCH, EUR));

    double[] bucketedTransactionExpected = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, -0.003532003180154463,
        -0.16089239249709827, -0.09592358666007135, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    assertRelative("SCH", -2.861164353150635, spreadComputed, TOL);
    assertRelative("SCH", 110.66399796476674, priceFuturesComputed, TOL);
    assertRelative("SCH", 1106.6399796476674, pvTransactionComputed.getAmount(EUR), TOL);
    assertArrayRelative("SCH", bucketedTransactionExpected, bucketedTransactionComputed.getData(), TOL);
    assertRelative("SCH", -0.2603479823373241, pv01TransactionComputed, TOL);
  }

  /**
   * BUN
   */
  @Test
  public void BUNTest() {
    BondFuturesSecurity futures = TRANSACTION_BUN.getUnderlyingSecurity();
    BondFixedSecurity bondAtSpot = futures.getDeliveryBasketAtSpotDate()[0];
    LegalEntity legalEntity = bondAtSpot.getIssuerEntity();
    Pair pair = Pairs.of(ISSUER_PROVIDER_BUN, BOND_MARKET_PRICE_BUN / HUNDRED);
    double spreadComputed = bondAtSpot.accept(ZSIC, pair);
    IssuerProviderIssuerAnnuallyCompoundeding curveWithSpread = new IssuerProviderIssuerAnnuallyCompoundeding(
        ISSUER_PROVIDER_BUN, legalEntity, spreadComputed * BP1);
    double priceFuturesComputed = futures.accept(FPIC, curveWithSpread) * HUNDRED;
    MultipleCurrencyAmount pvTransactionComputed = TRANSACTION_BUN.accept(PVIC, curveWithSpread);
    DoubleMatrix1D bucketedTransactionComputed = PSSFC.calculateSensitivity(TRANSACTION_BUN,
        curveWithSpread).multipliedBy(BP1).getSensitivity(CURVE_NAME_BUN, EUR);
    double pv01TransactionComputed = TRANSACTION_BUN.accept(PV01PC, curveWithSpread).getMap()
        .get(Pairs.of(CURVE_NAME_BUN, EUR));

    double[] bucketedTransactionExpected = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, -0.002063178043659581,
        -0.005484813614777467, -0.008148845219070796, -0.01071954339963743, -0.013167123470076732,
        -0.01537421236987675, -0.017655933808399228, -0.01952754726336682, -0.5798346316930093, -0.5484111675709569,
        0.0, 0.0, 0.0 };
    assertRelative("BUN", 0.807136177649383, spreadComputed, TOL);
    assertRelative("BUN", 140.57600798338933, priceFuturesComputed, TOL);
    assertRelative("BUN", 1405.7600798338933, pvTransactionComputed.getAmount(EUR), TOL);
    assertArrayRelative("BUN", bucketedTransactionExpected, bucketedTransactionComputed.getData(), TOL);
    assertRelative("BUN", -1.2203869964528309, pv01TransactionComputed, TOL);
  }

  /**
   * BOB
   */
  @Test
  public void BOBTest() {
    BondFuturesSecurity futures = TRANSACTION_BOB.getUnderlyingSecurity();
    BondFixedSecurity bondAtSpot = futures.getDeliveryBasketAtSpotDate()[0];
    LegalEntity legalEntity = bondAtSpot.getIssuerEntity();
    Pair pair = Pairs.of(ISSUER_PROVIDER_BOB, BOND_MARKET_PRICE_BOB / HUNDRED);
    double spreadComputed = bondAtSpot.accept(ZSIC, pair);
    IssuerProviderIssuerAnnuallyCompoundeding curveWithSpread = new IssuerProviderIssuerAnnuallyCompoundeding(
        ISSUER_PROVIDER_BOB, legalEntity, spreadComputed * BP1);
    double priceFuturesComputed = futures.accept(FPIC, curveWithSpread) * HUNDRED;
    MultipleCurrencyAmount pvTransactionComputed = TRANSACTION_BOB.accept(PVIC, curveWithSpread);
    DoubleMatrix1D bucketedTransactionComputed = PSSFC.calculateSensitivity(TRANSACTION_BOB,
        curveWithSpread).multipliedBy(BP1).getSensitivity(CURVE_NAME_BOB, EUR);
    double pv01TransactionComputed = TRANSACTION_BOB.accept(PV01PC, curveWithSpread).getMap()
        .get(Pairs.of(CURVE_NAME_BOB, EUR));

    double[] bucketedTransactionExpected = new double[] {0.0, 0.0, 0.0, 0.0, -8.416957774922316E-4,
        -0.003708734026681847, -0.008194588656256685, -0.012228161187586226, -0.07831615746868531, -0.4708218337870807,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    assertRelative("BOB", -4.008114404257235, spreadComputed, TOL);
    assertRelative("BOB", 125.01222173864899, priceFuturesComputed, TOL);
    assertRelative("BOB", 1250.1222173864899, pvTransactionComputed.getAmount(EUR), TOL);
    assertArrayRelative("BOB", bucketedTransactionExpected, bucketedTransactionComputed.getData(), TOL);
    assertRelative("BOB", -0.574111170903783, pv01TransactionComputed, TOL);
  }

  /**
   * Test correct method and data are used by calculators, and check rescalings. 
   */
  @Test
  public void calculatorTest() {
    double sampleSpread = 0.034;
    BondFuturesSecurity futures = TRANSACTION_SCH.getUnderlyingSecurity();
    BondFixedSecurity bondAtDeliv = futures.getDeliveryBasketAtDeliveryDate()[0]; // future delivery
    BondFixedSecurity bondAtSpot = futures.getDeliveryBasketAtSpotDate()[0]; // spot
    LegalEntity legalEntity = bondAtDeliv.getIssuerEntity();
    IssuerProviderIssuerAnnuallyCompoundeding curveWithSpread = new IssuerProviderIssuerAnnuallyCompoundeding(
        ISSUER_PROVIDER_SCH, legalEntity, sampleSpread);

    /* bond clean price - including factor */
    MultipleCurrencyAmount pv = BOND_METHOD.presentValue(bondAtSpot, curveWithSpread);
    double cleanPriceFromPV = HUNDRED * (pv.getAmount(EUR) / curveWithSpread.getMulticurveProvider()
        .getDiscountFactor(EUR, bondAtSpot.getSettlementTime()) - bondAtSpot.getAccruedInterest());
    double cleanPriceExpected = BOND_METHOD.cleanPriceFromCurves(bondAtSpot, curveWithSpread) * HUNDRED;
    double cleanPrice = bondAtSpot.accept(BCPCC, curveWithSpread); // *** use this
    assertRelative("calculatorTest", cleanPriceExpected, cleanPrice, TOL);
    assertRelative("calculatorTest", cleanPriceFromPV, cleanPrice, TOL);

    /* spread - including double factor, input should be un-factored */
    Pair pair = Pairs.of(ISSUER_PROVIDER_SCH, cleanPrice / HUNDRED);
    double computedSpread = bondAtSpot.accept(ZSIC, pair) / HUNDRED / HUNDRED; // *** use this
    assertRelative("calculatorTest", sampleSpread, computedSpread, TOL);

    /* futures price -  excluding factor and notional */
    double futuresPriceComputed = futures.accept(FPIC, curveWithSpread) * HUNDRED;  // *** use this
    double futuresPriceExpected = bondAtDeliv.accept(BCPCC, curveWithSpread) /
        futures.getConversionFactor()[0];
    assertRelative("calculatorTest", futuresPriceExpected, futuresPriceComputed, TOL);

    /* futures transaction pv - including notional but excluding factor */
    MultipleCurrencyAmount pvComputed = TRANSACTION_SCH.accept(PVIC, curveWithSpread);
    double pvExpected = bondAtDeliv.accept(BCPCC, curveWithSpread) * futures.getNotional() / HUNDRED /
        futures.getConversionFactor()[0];
    assertRelative("calculatorTest", pvExpected, pvComputed.getAmount(EUR), TOL);
  }

  private static void assertArrayRelative(String message, double[] expected, double[] obtained, double relativeTol) {
    int nData = expected.length;
    assertEquals(message, nData, obtained.length);
    for (int i = 0; i < nData; ++i) {
      assertRelative(message, expected[i], obtained[i], relativeTol);
    }
  }

  private static void assertRelative(String message, double expected, double obtained, double relativeTol) {
    double ref = Math.max(Math.abs(expected), 1.0);
    assertEquals(message, expected, obtained, ref * relativeTol);
  }
}
