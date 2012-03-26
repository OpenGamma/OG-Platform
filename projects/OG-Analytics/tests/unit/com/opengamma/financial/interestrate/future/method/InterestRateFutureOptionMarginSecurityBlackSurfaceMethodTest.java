package com.opengamma.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.time.DateUtils;

public class InterestRateFutureOptionMarginSecurityBlackSurfaceMethodTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final IborIndex EURIBOR3M_INDEX = IndexIborTestsMaster.getInstance().getIndex("EURIBOR3M", CALENDAR);
  //  private static final Currency CUR = EURIBOR3M_INDEX.getCurrency();
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -EURIBOR3M_INDEX.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final double REFERENCE_PRICE = 0.0;
  private static final String NAME = "ERU2";
  private static final double STRIKE = 0.9850;
  private static final InterestRateFutureDefinition ERU2_DEFINITION = new InterestRateFutureDefinition(LAST_TRADING_DATE, STRIKE, LAST_TRADING_DATE, EURIBOR3M_INDEX, REFERENCE_PRICE, NOTIONAL,
      FUTURE_FACTOR, 1, NAME);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String[] CURVE_NAMES = TestsDataSetsBlack.curvesEURNames();
  private static final InterestRateFuture EDU2 = ERU2_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE, CURVE_NAMES);
  // Option 
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final double EXPIRATION_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, EXPIRATION_DATE);
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurity OPTION_ERU2 = new InterestRateFutureOptionMarginSecurity(EDU2, EXPIRATION_TIME, STRIKE, IS_CALL);
  private static final InterestRateFutureDiscountingMethod METHOD_FUTURES = InterestRateFutureDiscountingMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod METHOD_OPTION_BLACK = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  private static final YieldCurveBundle CURVES = TestsDataSetsBlack.createCurvesEUR();
  private static final InterpolatedDoublesSurface BLACK_PARAMETER = TestsDataSetsBlack.createBlackSurface();
  private static final YieldCurveWithBlackCubeBundle BLACK_BUNDLE = new YieldCurveWithBlackCubeBundle(BLACK_PARAMETER, CURVES);

  @Test
  /**
   * Test the option price from the future price. Standard option.
   */
  public void priceStandard() {
    final double rateStrike = 1.0 - STRIKE;
    final double expiry = OPTION_ERU2.getExpirationTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, expiry, !IS_CALL);
    final double priceFuture = METHOD_FUTURES.price(EDU2, BLACK_BUNDLE);
    final double forward = 1 - priceFuture;
    final double volatility = BLACK_BUNDLE.getVolatility(expiry, rateStrike);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double priceExpected = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    final double priceComputed = METHOD_OPTION_BLACK.optionPrice(OPTION_ERU2, BLACK_BUNDLE);
    assertEquals("Future option with Black volatilities: option security price", priceExpected, priceComputed);
  }

  //TODO: Add present value curve sensitivity and Black volatility sensitivity tests.

  // TODO: Add transaction method.
}
