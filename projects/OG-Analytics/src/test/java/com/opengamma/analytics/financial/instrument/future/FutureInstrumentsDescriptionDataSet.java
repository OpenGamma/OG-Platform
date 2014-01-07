/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Contains a set of Future instruments that can be used in tests.
 */
@Test(groups = TestGroup.UNIT)
public class FutureInstrumentsDescriptionDataSet {

  //EURIBOR 3M Index
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final Currency CUR = Currency.EUR;
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2012, 2, 29);
  // Future option
  private static final ZonedDateTime OPTION_EXPIRY = DateUtils.getUTCDate(2012, 6, 19);
  private static final double OPTION_STRIKE = 0.97;
  private static final boolean IS_CALL = false;
  // Derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 13);

  public static InterestRateFutureSecurityDefinition createInterestRateFutureSecurityDefinition() {
    final InterestRateFutureSecurityDefinition sec = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);
    return sec;
  }

  public static InterestRateFutureSecurity createInterestRateFutureSecurity() {
    return createInterestRateFutureSecurityDefinition().toDerivative(REFERENCE_DATE);
  }

  public static InterestRateFutureOptionMarginSecurityDefinition createInterestRateFutureOptionMarginSecurityDefinition() {
    final InterestRateFutureSecurityDefinition underlying = createInterestRateFutureSecurityDefinition();
    return new InterestRateFutureOptionMarginSecurityDefinition(underlying, OPTION_EXPIRY, OPTION_STRIKE, IS_CALL);
  }

  public static InterestRateFutureOptionMarginTransactionDefinition createInterestRateFutureOptionMarginTransactionDefinition() {
    final InterestRateFutureSecurityDefinition underlying = createInterestRateFutureSecurityDefinition();
    final InterestRateFutureOptionMarginSecurityDefinition option = new InterestRateFutureOptionMarginSecurityDefinition(underlying, OPTION_EXPIRY, OPTION_STRIKE, IS_CALL);
    return new InterestRateFutureOptionMarginTransactionDefinition(option, 1, TRADE_DATE, .99);
  }

  public static InterestRateFutureOptionPremiumSecurityDefinition createInterestRateFutureOptionPremiumSecurityDefinition() {
    final InterestRateFutureSecurityDefinition underlying = createInterestRateFutureSecurityDefinition();
    return new InterestRateFutureOptionPremiumSecurityDefinition(underlying, OPTION_EXPIRY, OPTION_STRIKE, IS_CALL);
  }

  public static InterestRateFutureOptionPremiumTransactionDefinition createInterestRateFutureOptionPremiumTransactionDefinition() {
    final InterestRateFutureSecurityDefinition underlying = createInterestRateFutureSecurityDefinition();
    final InterestRateFutureOptionPremiumSecurityDefinition option = new InterestRateFutureOptionPremiumSecurityDefinition(underlying, OPTION_EXPIRY, OPTION_STRIKE, IS_CALL);
    return new InterestRateFutureOptionPremiumTransactionDefinition(option, 1, TRADE_DATE, .99);
  }

  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final Currency BNDFUT_CUR = Currency.EUR;
  private static final String ISSUER_NAME = "Issuer";
  private static final Period BNDFUT_PAYMENT_TENOR = Period.ofMonths(6);
  private static final DayCount BNDFUT_DAY_COUNT = DayCounts.ACT_ACT_ISDA;
  private static final BusinessDayConvention BNDFUT_BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean BNDFUT_IS_EOM = false;
  private static final int BNDFUT_SETTLEMENT_DAYS = 1;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final int NB_BOND = 7;
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5), Period.ofYears(5),
    Period.ofYears(5)};
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
    DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31)};
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175};
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292};
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(BNDFUT_CUR, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], BNDFUT_PAYMENT_TENOR, RATE[loopbasket],
          BNDFUT_SETTLEMENT_DAYS, CALENDAR, BNDFUT_DAY_COUNT, BNDFUT_BUSINESS_DAY, YIELD_CONVENTION, BNDFUT_IS_EOM, ISSUER_NAME);
    }
  }
  private static final ZonedDateTime BNDFUT_LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 21);
  private static final ZonedDateTime BNDFUT_FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime BNDFUT_LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 9, 29);
  private static final double BNDFUT_NOTIONAL = 100000;
  private static final double BNDFUT_REFERENCE_PRICE = 0.0; // FIXME CASE Confirm BNDFUT_REFERENCE_PRICE. Was 1.23
  private static final ZonedDateTime BNDFUT_REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 21);

  public static BondFutureDefinition createBondFutureSecurityDefinition() {
    return new BondFutureDefinition(BNDFUT_LAST_TRADING_DATE, BNDFUT_FIRST_NOTICE_DATE, BNDFUT_LAST_NOTICE_DATE, BNDFUT_NOTIONAL, BASKET_DEFINITION, CONVERSION_FACTOR);
  }

  public static BondFuture createBondFutureSecurity() {
    return createBondFutureSecurityDefinition().toDerivative(BNDFUT_REFERENCE_DATE, BNDFUT_REFERENCE_PRICE);
  }

  public static BondFutureOptionPremiumSecurityDefinition createBondFutureOptionPremiumSecurityDefinition() {
    return new BondFutureOptionPremiumSecurityDefinition(createBondFutureSecurityDefinition(), OPTION_EXPIRY, OPTION_STRIKE, IS_CALL);
  }

}
