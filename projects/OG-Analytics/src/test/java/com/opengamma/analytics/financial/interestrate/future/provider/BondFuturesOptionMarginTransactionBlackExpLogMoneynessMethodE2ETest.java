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
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.future.calculator.DeltaBlackBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceBlackBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.GammaBlackBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.ThetaBlackBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.VegaBlackBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.financial.provider.calculator.blackbondfutures.PresentValueBlackBondFuturesOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackbondfutures.PresentValueCurveSensitivityBlackBondFuturesOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ZSpreadIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesExpLogMoneynessProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderIssuerAnnuallyCompoundeding;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
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
 * End-to-end test for bond futures option based on Black model with moneyness-expiry volatility surface
 */
@SuppressWarnings({"rawtypes", "unchecked" })
@Test(groups = TestGroup.UNIT)
public class BondFuturesOptionMarginTransactionBlackExpLogMoneynessMethodE2ETest {
  private static final BondAndSTIRFuturesE2EExamplesData DATA = new BondAndSTIRFuturesE2EExamplesData();
  private static final Calendar EUR_CALENDAR = DATA.getEURCalendar();
  private static final Currency EUR = Currency.EUR;
  private static final ZonedDateTime VALUATION_DATE = ZonedDateTime.of(2014, 2, 17, 9, 0, 0, 0, ZoneId.of("Z"));

  /* names */
  private static final String ISSUER_NAME_SCH = "SCH EUR";
  private static final String ISSUER_NAME_BUN = "BUN EUR";
  private static final String ISSUER_NAME_BOB = "BOB EUR";
  private static final String CURVE_NAME_SCH = "SCH EURBond";
  private static final String CURVE_NAME_BUN = "BUN EURBond";
  private static final String CURVE_NAME_BOB = "BOB EURBond";
  private static final LegalEntity LEGAL_ENTITY_SCH = new LegalEntity(null, ISSUER_NAME_SCH, null, Sector.of("EU"),
      Region.of("EU", Country.of("EU"), EUR));
  private static final LegalEntity LEGAL_ENTITY_BUN = new LegalEntity(null, ISSUER_NAME_BUN, null, Sector.of("EU"),
      Region.of("EU", Country.of("EU"), EUR));
  private static final LegalEntity LEGAL_ENTITY_BOB = new LegalEntity(null, ISSUER_NAME_BOB, null, Sector.of("EU"),
      Region.of("EU", Country.of("EU"), EUR));

  private static final InterpolatedDoublesSurface VOL_SURFACE_MONEYNESS;
  static {
    Interpolator1D squareFlat = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.SQUARE_LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    Interpolator1D timeSquareFlat = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.TIME_SQUARE, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(timeSquareFlat, squareFlat);
    VOL_SURFACE_MONEYNESS = InterpolatedDoublesSurface.from(DATA.getExpiry(), DATA.getSimpleMoneyness(),
        DATA.getVolatility(), INTERPOLATOR_2D);
  }

  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  // Note that repo rate is contained as a currency based discount curve. 
  // However, this is not used in the option pricing part as the numeraire is always 1. 
  private static final BlackBondFuturesExpLogMoneynessProviderDiscount BLACK_PROVIDER_SCH;
  static {
    IssuerProviderDiscount issuerProvider = new IssuerProviderDiscount();
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(DATA.getTimeSCH(),
        DATA.getRateSCH(), INTERPOLATOR, CURVE_NAME_SCH);
    YieldPeriodicCurve yieldCurve = YieldPeriodicCurve.from(1, interpolatedCurve);
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    issuerProvider.setCurve(Pairs.of((Object) ISSUER_NAME_SCH, filter), yieldCurve);
    ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(DATA.getRepoSCHOp());
    YieldPeriodicCurve repoCurve = YieldPeriodicCurve.from(1, constantDoublesCurve);
    issuerProvider.setCurve(EUR, repoCurve);
    IssuerProviderIssuerAnnuallyCompoundeding wrapper = new IssuerProviderIssuerAnnuallyCompoundeding(issuerProvider);
    BLACK_PROVIDER_SCH = new BlackBondFuturesExpLogMoneynessProviderDiscount(wrapper, VOL_SURFACE_MONEYNESS,
        LEGAL_ENTITY_SCH);
  }
  private static final BlackBondFuturesExpLogMoneynessProviderDiscount BLACK_PROVIDER_BUN;
  static {
    IssuerProviderDiscount issuerProvider = new IssuerProviderDiscount();
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(DATA.getTimeBUN(),
        DATA.getRateBUN(), INTERPOLATOR, CURVE_NAME_BUN);
    YieldPeriodicCurve yieldCurve = YieldPeriodicCurve.from(1, interpolatedCurve);
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    issuerProvider.setCurve(Pairs.of((Object) ISSUER_NAME_BUN, filter), yieldCurve);
    ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(DATA.getRepoBUNOp());
    YieldPeriodicCurve repoCurve = YieldPeriodicCurve.from(1, constantDoublesCurve);
    issuerProvider.setCurve(EUR, repoCurve);
    IssuerProviderIssuerAnnuallyCompoundeding wrapper = new IssuerProviderIssuerAnnuallyCompoundeding(issuerProvider);
    BLACK_PROVIDER_BUN = new BlackBondFuturesExpLogMoneynessProviderDiscount(wrapper, VOL_SURFACE_MONEYNESS,
        LEGAL_ENTITY_BUN);
  }
  private static final BlackBondFuturesExpLogMoneynessProviderDiscount BLACK_PROVIDER_BOB;
  static {
    IssuerProviderDiscount issuerProvider = new IssuerProviderDiscount();
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(DATA.getTimeBOB(),
        DATA.getRateBOB(), INTERPOLATOR, CURVE_NAME_BOB);
    YieldPeriodicCurve yieldCurve = YieldPeriodicCurve.from(1, interpolatedCurve);
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    issuerProvider.setCurve(Pairs.of((Object) ISSUER_NAME_BOB, filter), yieldCurve);
    ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(DATA.getRepoBOBOp());
    YieldPeriodicCurve repoCurve = YieldPeriodicCurve.from(1, constantDoublesCurve);
    issuerProvider.setCurve(EUR, repoCurve);
    IssuerProviderIssuerAnnuallyCompoundeding wrapper = new IssuerProviderIssuerAnnuallyCompoundeding(issuerProvider);
    BLACK_PROVIDER_BOB = new BlackBondFuturesExpLogMoneynessProviderDiscount(wrapper, VOL_SURFACE_MONEYNESS,
        LEGAL_ENTITY_BOB);
  }
  
  private static final int QUANTITY = 1;
  private static final double NOTIONAL = 1000.0;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static ZonedDateTime TRADE_DATE = ZonedDateTime.of(2008, 8, 27, 1, 0, 0, 0, ZoneId.of("Z")); // 2008-08-27T01:00Z 
  private static final double TRADE_PRICE = 0.0;
  private static final double LAST_MARGIN_PRICE = 0.0;
  
  /* Schatz */
  private static final BondFuturesOptionMarginTransaction TRANSACTION_SCH;
  private static final double BOND_MARKET_PRICE_SCH = 99.825;
  static {
    ZonedDateTime tradingLastDate = ZonedDateTime.of(2014, 3, 10, 23, 59, 0, 0, ZoneId.of("Z")); // 2014-03-10T23:59Z
    ZonedDateTime noticeFirstDate = ZonedDateTime.of(2014, 3, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime noticeLastDate = ZonedDateTime.of(2014, 3, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryFirstDate = ZonedDateTime.of(2014, 3, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryLastDate = ZonedDateTime.of(2014, 3, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime firstAccrualDate = ZonedDateTime.of(2013, 2, 15, 0, 0, 0, 0, ZoneId.of("Z")); // 2013-02-15T00:00Z 
    ZonedDateTime firstCouponDate = ZonedDateTime.of(2014, 2, 15, 0, 0, 0, 0, ZoneId.of("Z")); // 2014-02-15T00:00Z 
    ZonedDateTime maturityDate = ZonedDateTime.of(2023, 2, 15, 0, 0, 0, 0, ZoneId.of("Z")); // 2023-02-15T00:00Z
    Period paymentPeriod = Period.ofMonths(12);
    double fixedRate = 0.015;
    int settlementDays = 3;
    YieldConvention yieldConvention = SimpleYieldConvention.GERMAN_BOND;
    boolean isEOM = false;
    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(EUR, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, fixedRate, settlementDays, EUR_CALENDAR, DAY_COUNT, BUSINESS_DAY, yieldConvention,
        isEOM, LEGAL_ENTITY_SCH);
    BondFixedSecurityDefinition[] deliveryBasket = new BondFixedSecurityDefinition[] {bondFixed };
    double[] conversionFactor = new double[] {0.695531 };
    BondFuturesSecurityDefinition bondFuturesDefinition = new BondFuturesSecurityDefinition(tradingLastDate,
        noticeFirstDate, noticeLastDate, deliveryFirstDate, deliveryLastDate, NOTIONAL, deliveryBusket,
        conversionFactor);
    ZonedDateTime lastTradingDate = ZonedDateTime.of(2014, 3, 6, 0, 0, 0, 0, ZoneId.of("Z")); // 2014-03-06T00:00Z
    ZonedDateTime expirationDate = ZonedDateTime.of(2014, 3, 6, 0, 0, 0, 0, ZoneId.of("Z")); // 2014-03-06T00:00Z
    double strike = 1.1;
    boolean isCall = true;
    BondFuturesOptionMarginSecurityDefinition underlyingOption = new BondFuturesOptionMarginSecurityDefinition(
        bondFuturesDefinition, lastTradingDate, expirationDate, strike, isCall);
    BondFuturesOptionMarginTransactionDefinition transactionDefinition = new BondFuturesOptionMarginTransactionDefinition(
        underlyingOption, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_SCH = transactionDefinition.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE);
  }

  /* Bund */
  private static final BondFuturesOptionMarginTransaction TRANSACTION_BUN;
  private static final double BOND_MARKET_PRICE_BUN = 99.935;
  static {
    ZonedDateTime tradingLastDate = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z")); // 2014-06-10T23:59Z 
    ZonedDateTime noticeFirstDate = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime noticeLastDate = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryFirstDate = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryLastDate = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime firstAccrualDate = ZonedDateTime.of(2013, 2, 15, 0, 0, 0, 0, ZoneId.of("Z")); // 2013-02-15T00:00Z 
    ZonedDateTime firstCouponDate = ZonedDateTime.of(2014, 2, 15, 0, 0, 0, 0, ZoneId.of("Z")); // 2014-02-15T00:00Z 
    ZonedDateTime maturityDate = ZonedDateTime.of(2023, 2, 15, 0, 0, 0, 0, ZoneId.of("Z")); // 2023-02-15T00:00Z
    Period paymentPeriod = Period.ofMonths(12);
    double fixedRate = 0.015;
    int settlementDays = 3;
    YieldConvention yieldConvention = SimpleYieldConvention.GERMAN_BOND;
    boolean isEOM = false;
    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(EUR, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, fixedRate, settlementDays, EUR_CALENDAR, DAY_COUNT, BUSINESS_DAY, yieldConvention,
        isEOM, LEGAL_ENTITY_BUN);
    BondFixedSecurityDefinition[] deliveryBasket = new BondFixedSecurityDefinition[] {bondFixed };
    double[] conversionFactor = new double[] {0.702055 };
    BondFuturesSecurityDefinition bondFuturesDefinition = new BondFuturesSecurityDefinition(tradingLastDate,
        noticeFirstDate, noticeLastDate, deliveryFirstDate, deliveryLastDate, NOTIONAL, deliveryBusket,
        conversionFactor);
    ZonedDateTime lastTradingDate = ZonedDateTime.of(2014, 6, 6, 0, 0, 0, 0, ZoneId.of("Z")); // 2014-06-06T00:00Z 
    ZonedDateTime expirationDate = ZonedDateTime.of(2014, 6, 6, 0, 0, 0, 0, ZoneId.of("Z")); // 2014-06-06T00:00Z 
    double strike = 1.4;
    boolean isCall = true;
    BondFuturesOptionMarginSecurityDefinition underlyingOption = new BondFuturesOptionMarginSecurityDefinition(
        bondFuturesDefinition, lastTradingDate, expirationDate, strike, isCall);
    BondFuturesOptionMarginTransactionDefinition transactionDefinition = new BondFuturesOptionMarginTransactionDefinition(
        underlyingOption, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_BUN = transactionDefinition.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE);
  }

  /* Bobl */
  private static final BondFuturesOptionMarginTransaction TRANSACTION_BOB;
  private static final double BOND_MARKET_PRICE_BOB = 115.195;
  static {
    ZonedDateTime tradingLastDate = ZonedDateTime.of(2014, 3, 10, 23, 59, 0, 0, ZoneId.of("Z")); // 2014-03-10T23:59Z 
    ZonedDateTime noticeFirstDate = ZonedDateTime.of(2014, 3, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime noticeLastDate = ZonedDateTime.of(2014, 3, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryFirstDate = ZonedDateTime.of(2014, 3, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime deliveryLastDate = ZonedDateTime.of(2014, 3, 10, 23, 59, 0, 0, ZoneId.of("Z"));
    ZonedDateTime firstAccrualDate = ZonedDateTime.of(2009, 1, 4, 0, 0, 0, 0, ZoneId.of("Z")); // 2009-01-04T00:00Z 
    ZonedDateTime firstCouponDate = ZonedDateTime.of(2010, 1, 4, 0, 0, 0, 0, ZoneId.of("Z")); // 2010-01-04T00:00Z 
    ZonedDateTime maturityDate = ZonedDateTime.of(2019, 1, 4, 0, 0, 0, 0, ZoneId.of("Z")); // 2019-01-04T00:00Z 
    Period paymentPeriod = Period.ofMonths(12);
    double fixedRate = 0.0375;
    int settlementDays = 3;
    YieldConvention yieldConvention = SimpleYieldConvention.GERMAN_BOND;
    boolean isEOM = false;
    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(EUR, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, fixedRate, settlementDays, EUR_CALENDAR, DAY_COUNT, BUSINESS_DAY, yieldConvention,
        isEOM, LEGAL_ENTITY_BOB);
    BondFixedSecurityDefinition[] deliveryBasket = new BondFixedSecurityDefinition[] {bondFixed };
    double[] conversionFactor = new double[] {0.907986 };
    BondFuturesSecurityDefinition bondFuturesDefinition = new BondFuturesSecurityDefinition(tradingLastDate,
        noticeFirstDate, noticeLastDate, deliveryFirstDate, deliveryLastDate, NOTIONAL, deliveryBusket,
        conversionFactor);
    ZonedDateTime lastTradingDate = ZonedDateTime.of(2014, 3, 6, 0, 0, 0, 0, ZoneId.of("Z")); // 2014-03-06T00:00Z 
    ZonedDateTime expirationDate = ZonedDateTime.of(2014, 3, 6, 0, 0, 0, 0, ZoneId.of("Z")); // 2014-03-06T00:00Z 
    double strike = 1.27;
    boolean isCall = false;
    BondFuturesOptionMarginSecurityDefinition underlyingOption = new BondFuturesOptionMarginSecurityDefinition(
        bondFuturesDefinition, lastTradingDate, expirationDate, strike, isCall);
    BondFuturesOptionMarginTransactionDefinition transactionDefinition = new BondFuturesOptionMarginTransactionDefinition(
        underlyingOption, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_BOB = transactionDefinition.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE);
  }

  /* calculator */
  private static final ZSpreadIssuerCalculator ZSIC = ZSpreadIssuerCalculator.getInstance();
  private static final FuturesPriceBlackBondFuturesCalculator FPBFC = FuturesPriceBlackBondFuturesCalculator
      .getInstance();
  private static final PresentValueBlackBondFuturesOptionCalculator PVBFC = PresentValueBlackBondFuturesOptionCalculator
      .getInstance();
  private static final PresentValueCurveSensitivityBlackBondFuturesOptionCalculator PVCSBFC = PresentValueCurveSensitivityBlackBondFuturesOptionCalculator
      .getInstance();
  private static final ParameterSensitivityParameterCalculator<BlackBondFuturesProviderInterface> PSSFC =
      new ParameterSensitivityParameterCalculator<>(PVCSBFC);
  private static final PV01CurveParametersCalculator<BlackBondFuturesProviderInterface> PV01PC =
      new PV01CurveParametersCalculator<>(PVCSBFC);
  private static final DeltaBlackBondFuturesCalculator DBFC = DeltaBlackBondFuturesCalculator.getInstance();
  private static final GammaBlackBondFuturesCalculator GBFC = GammaBlackBondFuturesCalculator.getInstance();
  private static final ThetaBlackBondFuturesCalculator TBFC = ThetaBlackBondFuturesCalculator.getInstance();
  private static final VegaBlackBondFuturesCalculator VBFC = VegaBlackBondFuturesCalculator.getInstance();

  private final double HUNDRED = 100.0;
  private static final double BP1 = 1.0E-4;
  private static final double TOL = 1.0e-10;

  /**
   * Schatz
   */
  @Test
  public void SCHTest() {
    BondFuturesSecurity futures = TRANSACTION_SCH.getUnderlyingSecurity().getUnderlyingFuture();
    BondFixedSecurity bondAtSpot = futures.getDeliveryBasketAtSpotDate()[0];
    LegalEntity legalEntity = bondAtSpot.getIssuerEntity();
    Pair pair = Pairs.of((IssuerProviderIssuerAnnuallyCompoundeding) BLACK_PROVIDER_SCH.getIssuerProvider(),
        BOND_MARKET_PRICE_SCH / HUNDRED);
    double spread = bondAtSpot.accept(ZSIC, pair);
    IssuerProviderIssuerAnnuallyCompoundeding curveWithSpread = new IssuerProviderIssuerAnnuallyCompoundeding(
        (IssuerProviderIssuerAnnuallyCompoundeding) BLACK_PROVIDER_SCH.getIssuerProvider(), legalEntity, spread * BP1);
    BlackBondFuturesExpLogMoneynessProviderDiscount blackNew = new BlackBondFuturesExpLogMoneynessProviderDiscount(
        curveWithSpread, BLACK_PROVIDER_SCH.getBlackParameters(), BLACK_PROVIDER_SCH.getLegalEntity());

    double price = TRANSACTION_SCH.getUnderlyingSecurity().accept(FPBFC, blackNew) * HUNDRED;
    MultipleCurrencyAmount pv = TRANSACTION_SCH.accept(PVBFC, blackNew); // multiplied by notional
    DoubleMatrix1D bucketedPv01 = PSSFC.calculateSensitivity(TRANSACTION_SCH, blackNew)
        .multipliedBy(BP1).getSensitivity(CURVE_NAME_SCH, EUR);
    double pv01 = TRANSACTION_SCH.accept(PV01PC, blackNew).getMap().get(Pairs.of(CURVE_NAME_SCH, EUR));
    double delta = TRANSACTION_SCH.getUnderlyingSecurity().accept(DBFC, blackNew);
    double gamma = TRANSACTION_SCH.getUnderlyingSecurity().accept(GBFC, blackNew);
    double theta = TRANSACTION_SCH.getUnderlyingSecurity().accept(TBFC, blackNew);
    double vega = TRANSACTION_SCH.getUnderlyingSecurity().accept(VBFC, blackNew);

    double[] bucketExpected = new double[] {0.0, 0.0, 0.0, 0.0, -1.0425303352154139E-5, -0.0019406475278737731,
        -0.0038589334744579236, -0.005760045132848445, -0.007585603291942061, -0.009240224129369862,
        -0.011016975355809236, -0.012434155737048338, -0.019216928224809117, -1.0044100631419868, 0.0, 0.0, 0.0, 0.0 };

    assertRelative("SCHTest", 1.8985746330408364, spread, TOL);
    assertRelative("SCHTest", 35.183034488344354, price, TOL);
    assertRelative("SCHTest", 351.83034488344356, pv.getAmount(EUR), TOL);
    assertArrayRelative("", bucketExpected, bucketedPv01.getData(), TOL);
    assertRelative("SCHTest", -1.0754740013194977, pv01, TOL);
    assertRelative("SCHTest", 0.898331709678049, delta, TOL);
    assertRelative("SCHTest", 0.540194277796906, gamma, TOL);
    assertRelative("SCHTest", -0.6264331723356397, theta, TOL);
    assertRelative("SCHTest", 0.05498292195291558, vega, TOL);
  }

  /**
   * Bund
   */
  @Test
  public void BUNTest() {
    BondFuturesSecurity futures = TRANSACTION_BUN.getUnderlyingSecurity().getUnderlyingFuture();
    BondFixedSecurity bondAtSpot = futures.getDeliveryBasketAtSpotDate()[0];
    LegalEntity legalEntity = bondAtSpot.getIssuerEntity();
    Pair pair = Pairs.of((IssuerProviderIssuerAnnuallyCompoundeding) BLACK_PROVIDER_BUN.getIssuerProvider(),
        BOND_MARKET_PRICE_BUN / HUNDRED);
    double spread = bondAtSpot.accept(ZSIC, pair);
    IssuerProviderIssuerAnnuallyCompoundeding curveWithSpread = new IssuerProviderIssuerAnnuallyCompoundeding(
        (IssuerProviderIssuerAnnuallyCompoundeding) BLACK_PROVIDER_BUN.getIssuerProvider(), legalEntity, spread * BP1);
    BlackBondFuturesExpLogMoneynessProviderDiscount blackNew = new BlackBondFuturesExpLogMoneynessProviderDiscount(
        curveWithSpread, BLACK_PROVIDER_BUN.getBlackParameters(), BLACK_PROVIDER_BUN.getLegalEntity());

    double price = TRANSACTION_BUN.getUnderlyingSecurity().accept(FPBFC, blackNew) * HUNDRED;
    MultipleCurrencyAmount pv = TRANSACTION_BUN.accept(PVBFC, blackNew); // multiplied by notional
    DoubleMatrix1D bucketedPv01 = PSSFC.calculateSensitivity(TRANSACTION_BUN, blackNew)
        .multipliedBy(BP1).getSensitivity(CURVE_NAME_BUN, EUR);
    double pv01 = TRANSACTION_BUN.accept(PV01PC, blackNew).getMap().get(Pairs.of(CURVE_NAME_BUN, EUR));
    double delta = TRANSACTION_BUN.getUnderlyingSecurity().accept(DBFC, blackNew);
    double gamma = TRANSACTION_BUN.getUnderlyingSecurity().accept(GBFC, blackNew);
    double theta = TRANSACTION_BUN.getUnderlyingSecurity().accept(TBFC, blackNew);
    double vega = TRANSACTION_BUN.getUnderlyingSecurity().accept(VBFC, blackNew);

    double[] bucketExpected = new double[] {0.0, 0.0, 0.0, 0.0, -7.044267721686774E-6, -0.0013112771184815367,
        -0.0026077906901950704, -0.003893042244768844, -0.005127560684760342, -0.006246833287533004,
        -0.007448992271228702, -0.008408292192481213, -0.012997136051315432, -0.6793830569801992, 0.0, 0.0, 0.0, 0.0 };

    assertRelative("BUNTest", 0.5612849466205121, spread, TOL);
    assertRelative("BUNTest", 30.113033433034087, price, TOL);
    assertRelative("BUNTest", 301.1303343303409, pv.getAmount(EUR), TOL);
    assertArrayRelative("", bucketExpected, bucketedPv01.getData(), TOL);
    assertRelative("BUNTest", -0.727431025788685, pv01, TOL);
    assertRelative("BUNTest", 0.6126650490480057, delta, TOL);
    assertRelative("BUNTest", 0.5131248273894041, gamma, TOL);
    assertRelative("BUNTest", -0.47845638404975527, theta, TOL);
    assertRelative("BUNTest", 0.2965232966302743, vega, TOL);
  }

  /**
   * Bobl
   */
  @Test
  public void BOBTest() {
    BondFuturesSecurity futures = TRANSACTION_BOB.getUnderlyingSecurity().getUnderlyingFuture();
    BondFixedSecurity bondAtSpot = futures.getDeliveryBasketAtSpotDate()[0];
    LegalEntity legalEntity = bondAtSpot.getIssuerEntity();
    Pair pair = Pairs.of((IssuerProviderIssuerAnnuallyCompoundeding) BLACK_PROVIDER_BOB.getIssuerProvider(),
        BOND_MARKET_PRICE_BOB / HUNDRED);
    double spread = bondAtSpot.accept(ZSIC, pair);
    IssuerProviderIssuerAnnuallyCompoundeding curveWithSpread = new IssuerProviderIssuerAnnuallyCompoundeding(
        (IssuerProviderIssuerAnnuallyCompoundeding) BLACK_PROVIDER_BOB.getIssuerProvider(), legalEntity, spread * BP1);
    BlackBondFuturesExpLogMoneynessProviderDiscount blackNew = new BlackBondFuturesExpLogMoneynessProviderDiscount(
        curveWithSpread, BLACK_PROVIDER_BOB.getBlackParameters(), BLACK_PROVIDER_BOB.getLegalEntity());

    double price = TRANSACTION_BOB.getUnderlyingSecurity().accept(FPBFC, blackNew) * HUNDRED;
    MultipleCurrencyAmount pv = TRANSACTION_BOB.accept(PVBFC, blackNew); // multiplied by notional
    DoubleMatrix1D bucketedPv01 = PSSFC.calculateSensitivity(TRANSACTION_BOB, blackNew)
        .multipliedBy(BP1).getSensitivity(CURVE_NAME_BOB, EUR);
    double pv01 = TRANSACTION_BOB.accept(PV01PC, blackNew).getMap().get(Pairs.of(CURVE_NAME_BOB, EUR));
    double delta = TRANSACTION_BOB.getUnderlyingSecurity().accept(DBFC, blackNew);
    double gamma = TRANSACTION_BOB.getUnderlyingSecurity().accept(GBFC, blackNew);
    double theta = TRANSACTION_BOB.getUnderlyingSecurity().accept(TBFC, blackNew);
    double vega = TRANSACTION_BOB.getUnderlyingSecurity().accept(VBFC, blackNew);

    double[] bucketExpected = new double[] {0.0, 0.0, 0.0, 0.0, 3.9923623024940084E-4, 0.0017591397765423418,
        0.003886881591029152, 0.005800090468418725, 0.03714702801470695, 0.2233208135383925, 0.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0 };

    assertRelative("BOBTest", -3.9954021134089603, spread, TOL);
    assertRelative("BOBTest", 8.837066933825167, price, TOL);
    assertRelative("BOBTest", 88.37066933825166, pv.getAmount(EUR), TOL);
    assertArrayRelative("", bucketExpected, bucketedPv01.getData(), TOL);
    assertRelative("BOBTest", 0.2723131896193391, pv01, TOL);
    assertRelative("BOBTest", -0.47202802974084884, delta, TOL);
    assertRelative("BOBTest", 1.8320961337443291, gamma, TOL);
    assertRelative("BOBTest", -0.9280515056338662, theta, TOL);
    assertRelative("BOBTest", 0.1087833907611784, vega, TOL);
  }

  private void assertArrayRelative(String message, double[] expected, double[] obtained, double relativeTol) {
    int nData = expected.length;
    assertEquals(message, nData, obtained.length);
    for (int i = 0; i < nData; ++i) {
      assertRelative(message, expected[i], obtained[i], relativeTol);
    }
  }

  private void assertRelative(String message, double expected, double obtained, double relativeTol) {
    double ref = Math.max(Math.abs(expected), 1.0);
    assertEquals(message, expected, obtained, ref * relativeTol);
  }
}
