/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderIssuerAnnuallyCompoundeding;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
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


  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final IssuerProviderIssuerAnnuallyCompoundeding ISSUER_PROVIDER_SCH;
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
    ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(DATA.getRepoBUNOp());
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
    ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(DATA.getRepoBOBOp());
    YieldPeriodicCurve repoCurve = YieldPeriodicCurve.from(1, constantDoublesCurve);
    issuerProvider.setCurve(EUR, repoCurve);
    ISSUER_PROVIDER_BOB = new IssuerProviderIssuerAnnuallyCompoundeding(issuerProvider);
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
    LegalEntity legalEntity = new LegalEntity(null, ISSUER_NAME_SCH, null, Sector.of("EU"), Region.of("EU",
        Country.of("EU"), EUR));
    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(EUR, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, fixedRate, settlementDays, EUR_CALENDAR, DAY_COUNT, BUSINESS_DAY, yieldConvention,
        isEOM, legalEntity);
    BondFixedSecurityDefinition[] deliveryBusket = new BondFixedSecurityDefinition[] {bondFixed };
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
    BondFuturesOptionMarginTransactionDefinition transactionDefinition = new BondFuturesOptionMarginTransactionDefinition(underlyingOption, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_SCH = transactionDefinition.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE);
  }

  /* Bund */
  //private static final BondFuturesOptionMarginTransactionDefinition TRANSACTION_BUN;
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
    LegalEntity legalEntity = new LegalEntity(null, ISSUER_NAME_BUN, null, Sector.of("EU"), Region.of("EU",
        Country.of("EU"), EUR));
    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(EUR, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, fixedRate, settlementDays, EUR_CALENDAR, DAY_COUNT, BUSINESS_DAY, yieldConvention,
        isEOM, legalEntity);
    BondFixedSecurityDefinition[] deliveryBusket = new BondFixedSecurityDefinition[] {bondFixed };
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
  //private static final BondFuturesOptionMarginTransactionDefinition TRANSACTION_BOB;
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
    LegalEntity legalEntity = new LegalEntity(null, ISSUER_NAME_BOB, null, Sector.of("EU"), Region.of("EU",
        Country.of("EU"), EUR));
    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(EUR, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, fixedRate, settlementDays, EUR_CALENDAR, DAY_COUNT, BUSINESS_DAY, yieldConvention,
        isEOM, legalEntity);
    BondFixedSecurityDefinition[] deliveryBusket = new BondFixedSecurityDefinition[] {bondFixed };
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
  
  @Test
  public void SCHTest() {

  }

}
