/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests the constant spread horizon calculator for bonds.
 */
@Test(groups = TestGroup.UNIT)
public class BondConstantSpreadHorizonCalculatorTest {
  /** Currency */
  private static final Currency USD = Currency.USD;
  /** Coupon frequency */
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  /** Holiday calendar */
  private static final Calendar CALENDAR = new NoHolidayCalendar();
  /** Issuer name */
  private static final String US_GOVT = "US";
  /** Day-count */
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  /** Business day convention */
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  /** Is EOM */
  private static final boolean IS_EOM = false;
  /** Settlement days */
  private static final int SETTLEMENT_DAYS = 1;
  /** Bond yield convention */
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  /** The coupon */
  private static final double COUPON = 0.0123;
  /** Bond future security */
  private static final BondFixedSecurityDefinition SECURITY;
  /** Quantity */
  private static final int QUANTITY = 1000;
  /** Reference date */
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  /** Reference price */
  private static final double REFERENCE_PRICE = 1.0;
  /** Bond future transaction */
  private static final BondTransactionDefinition<?, ?> TRANSACTION;
  /** Horizon calculation date */
  private static final ZonedDateTime HORIZON_DATE = REFERENCE_DATE.plusYears(1).plusDays(15);
  /** Present value calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MultipleCurrencyAmount> PV_CALCULATOR = PresentValueIssuerCalculator.getInstance();
  /** Horizon calculator */
  private static final HorizonCalculator<BondTransactionDefinition<?, ?>, IssuerProviderInterface, Double> CALCULATOR = BondConstantSpreadHorizonCalculator.getInstance();
  /** Constant issuer and discounting curves */
  private static final IssuerProviderDiscount FLAT_ISSUER_MULTICURVES;
  /** Accuracy */
  private static final double EPS = 1e-9;

  static {
    SECURITY = BondFixedSecurityDefinition.from(USD, REFERENCE_DATE.plusYears(10), REFERENCE_DATE, PAYMENT_TENOR, COUPON, SETTLEMENT_DAYS,
        CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, new LegalEntity(US_GOVT, US_GOVT, null, null, null));
    TRANSACTION = new BondFixedTransactionDefinition(SECURITY, QUANTITY, REFERENCE_DATE, REFERENCE_PRICE);
    final YieldCurve flatIssuer = YieldCurve.from(ConstantDoublesCurve.from(0.02));
    final YieldCurve flatDiscounting = YieldCurve.from(ConstantDoublesCurve.from(0.01));
    final Map<Currency, YieldAndDiscountCurve> discounting = Collections.<Currency, YieldAndDiscountCurve>singletonMap(USD, flatDiscounting);
    final Map<IborIndex, YieldAndDiscountCurve> ibor = Collections.emptyMap();
    final Map<IndexON, YieldAndDiscountCurve> on = Collections.emptyMap();
    final Pair<Object, LegalEntityFilter<LegalEntity>> issuerKey = Pairs.<Object, LegalEntityFilter<LegalEntity>>of(US_GOVT, new LegalEntityShortName());
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuer = Collections.<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve>singletonMap(issuerKey, flatIssuer);
    FLAT_ISSUER_MULTICURVES = new IssuerProviderDiscount(discounting, ibor, on, issuer, new FXMatrix());
  }

  /**
   * Tests the exception thrown when the bond future is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    CALCULATOR.getTheta(null, HORIZON_DATE, FLAT_ISSUER_MULTICURVES, 1, CALENDAR);

  }

  /**
   * Tests the exception thrown when the horizon date is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    CALCULATOR.getTheta(TRANSACTION, null, FLAT_ISSUER_MULTICURVES, 1, CALENDAR);
  }

  /**
   * Tests the exception thrown when the curve data is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.getTheta(TRANSACTION, HORIZON_DATE, null, 1, CALENDAR);
  }

  /**
   * Tests the exception thrown when the number of days forward is too large
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDaysForward1() {
    CALCULATOR.getTheta(TRANSACTION, HORIZON_DATE, FLAT_ISSUER_MULTICURVES, 2, CALENDAR);
  }

  /**
   * Tests the exception thrown when the number of days backward is too large
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDaysForward2() {
    CALCULATOR.getTheta(TRANSACTION, HORIZON_DATE, FLAT_ISSUER_MULTICURVES, -2, CALENDAR);
  }

  /**
   * Tests the horizon calculation when the curves are flat.
   */
  @Test
  public void testConstantCurves() {
    final BondFixedTransaction today = (BondFixedTransaction) TRANSACTION.toDerivative(HORIZON_DATE);
    final BondFixedTransaction tomorrow = (BondFixedTransaction) TRANSACTION.toDerivative(HORIZON_DATE.plusDays(1));
    final MultipleCurrencyAmount pvToday = today.accept(PV_CALCULATOR, FLAT_ISSUER_MULTICURVES);
    final MultipleCurrencyAmount pvTomorrow = tomorrow.accept(PV_CALCULATOR, FLAT_ISSUER_MULTICURVES);
    MultipleCurrencyAmount expected = HorizonCalculator.subtract(pvTomorrow, pvToday);
    MultipleCurrencyAmount actual = CALCULATOR.getTheta(TRANSACTION, HORIZON_DATE, FLAT_ISSUER_MULTICURVES, 1, CALENDAR, REFERENCE_PRICE);
    assertMCAEquals(expected, actual);
    final BondTransaction<?> yesterday = TRANSACTION.toDerivative(HORIZON_DATE.minusDays(1));
    final MultipleCurrencyAmount pvYesterday = yesterday.accept(PV_CALCULATOR, FLAT_ISSUER_MULTICURVES);
    expected = HorizonCalculator.subtract(pvYesterday, pvToday);
    actual = CALCULATOR.getTheta(TRANSACTION, HORIZON_DATE, FLAT_ISSUER_MULTICURVES, -1, CALENDAR, REFERENCE_PRICE);
    assertMCAEquals(expected, actual);
  }

  /**
   * Checks multiple currency amounts for equality to within a specific accuracy.
   * @param expected The expected object
   * @param actual The actual object
   */
  private void assertMCAEquals(final MultipleCurrencyAmount expected, final MultipleCurrencyAmount actual) {
    final int length = expected.getCurrencyAmounts().length;
    assertEquals(length, actual.getCurrencyAmounts().length);
    for (int i = 0; i < length; i++) {
      final CurrencyAmount ca = expected.getCurrencyAmounts()[i];
      assertEquals(ca.getAmount(), actual.getAmount(ca.getCurrency()), EPS);
    }
  }
}
