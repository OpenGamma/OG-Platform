package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.PresentValueBlackSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.blackstirfutures.ParameterSensitivityBlackSTIRFuturesDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureOptionMarginBlackSmileMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final InterpolatedDoublesSurface BLACK_PARAMETERS = BlackDataSets.createBlackSurfaceExpiryTenor();
  private static final BlackSTIRFuturesSmileProviderDiscount BLACK_MULTICURVES = new BlackSTIRFuturesSmileProviderDiscount(MULTICURVES, BLACK_PARAMETERS, EURIBOR3M);

  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -EURIBOR3M.getSpotLag(), TARGET);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final double STRIKE = 0.9850;
  private static final InterestRateFutureSecurityDefinition ERU2_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, EURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, TARGET);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final InterestRateFutureSecurity ERU2 = ERU2_DEFINITION.toDerivative(REFERENCE_DATE);
  // Option
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final boolean IS_CALL = true;

  private static final InterestRateFutureOptionMarginSecurityDefinition OPTION_ERU2_DEFINITION = new InterestRateFutureOptionMarginSecurityDefinition(ERU2_DEFINITION, EXPIRATION_DATE, STRIKE, IS_CALL);
  private static final InterestRateFutureOptionMarginSecurity OPTION_ERU2 = OPTION_ERU2_DEFINITION.toDerivative(REFERENCE_DATE);

  // Transaction
  private static final int QUANTITY = -123;
  private static final double TRADE_PRICE = 0.0050;
  private static final ZonedDateTime TRADE_DATE_1 = DateUtils.getUTCDate(2010, 8, 17, 13, 00);
  private static final ZonedDateTime TRADE_DATE_2 = DateUtils.getUTCDate(2010, 8, 18, 9, 30);
  private static final double MARGIN_PRICE = 0.0025; // Settle price for 17-Aug
  private static final InterestRateFutureOptionMarginTransactionDefinition TRANSACTION_1_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(OPTION_ERU2_DEFINITION, QUANTITY,
      TRADE_DATE_1, TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransaction TRANSACTION_1 = TRANSACTION_1_DEFINITION.toDerivative(REFERENCE_DATE, MARGIN_PRICE);
  private static final InterestRateFutureOptionMarginTransactionDefinition TRANSACTION_2_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(OPTION_ERU2_DEFINITION, QUANTITY,
      TRADE_DATE_2, TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransaction TRANSACTION_2 = TRANSACTION_2_DEFINITION.toDerivative(REFERENCE_DATE, MARGIN_PRICE);

  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURES = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityBlackSmileMethod METHOD_SECURITY_OPTION_BLACK = InterestRateFutureOptionMarginSecurityBlackSmileMethod.getInstance();
  private static final InterestRateFutureOptionMarginTransactionBlackSmileMethod METHOD_TRANSACTION_OPTION_BLACK = InterestRateFutureOptionMarginTransactionBlackSmileMethod.getInstance();

  private static final PresentValueBlackSTIRFutureOptionCalculator PVBFC = PresentValueBlackSTIRFutureOptionCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator PVCSBFC = PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator.getInstance();
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityParameterCalculator<BlackSTIRFuturesProviderInterface> PSHWC = new ParameterSensitivityParameterCalculator<>(
      PVCSBFC);
  private static final ParameterSensitivityBlackSTIRFuturesDiscountInterpolatedFDCalculator PSHWC_DSC_FD = new ParameterSensitivityBlackSTIRFuturesDiscountInterpolatedFDCalculator(PVBFC, SHIFT);

  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  private static final double VOL_SHIFT = 1.0E-6;

  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-4;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  @Test
  /**
   * Test the option price from the future price. Standard option.
   */
  public void price() {
    final double rateStrike = 1.0 - STRIKE;
    final double expiry = OPTION_ERU2.getExpirationTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, expiry, !IS_CALL);
    final double priceFuture = METHOD_FUTURES.price(ERU2, MULTICURVES);
    final double forward = 1 - priceFuture;
    final double volatility = BLACK_PARAMETERS.getZValue(expiry, rateStrike);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double priceExpected = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    final double priceComputed = METHOD_SECURITY_OPTION_BLACK.price(OPTION_ERU2, BLACK_MULTICURVES);
    assertEquals("Future option with Black volatilities: option security price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the option transaction present value.
   */
  public void presentValue() {
    final double priceOption = METHOD_SECURITY_OPTION_BLACK.price(OPTION_ERU2, BLACK_MULTICURVES);
    final double presentValue1Expected = (priceOption - MARGIN_PRICE) * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    final MultipleCurrencyAmount presentValue1Computed = METHOD_TRANSACTION_OPTION_BLACK.presentValue(TRANSACTION_1, BLACK_MULTICURVES);
    assertEquals("Future option with Black volatilities: option transaction pv", presentValue1Expected, presentValue1Computed.getAmount(EUR), TOLERANCE_PV);
    final double presentValue2Expected = (priceOption - TRADE_PRICE) * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    final MultipleCurrencyAmount presentValue2Computed = METHOD_TRANSACTION_OPTION_BLACK.presentValue(TRANSACTION_2, BLACK_MULTICURVES);
    assertEquals("Future option with Black volatilities: option transaction pv", presentValue2Expected, presentValue2Computed.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount presentValue1Calculator = TRANSACTION_1.accept(PVBFC, BLACK_MULTICURVES);
    assertEquals("Future option with Black volatilities: option transaction pv", presentValue1Computed.getAmount(EUR), presentValue1Calculator.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PSHWC.calculateSensitivity(TRANSACTION_1, BLACK_MULTICURVES, BLACK_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PSHWC_DSC_FD.calculateSensitivity(TRANSACTION_1, BLACK_MULTICURVES);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the option price Black sensitivity
   */
  public void priceBlackSensitivity() {
    final InterpolatedDoublesSurface blackParameterPlus = BlackDataSets.createBlackSurfaceExpiryTenorShift(VOL_SHIFT);
    final InterpolatedDoublesSurface blackParameterMinus = BlackDataSets.createBlackSurfaceExpiryTenorShift(-VOL_SHIFT);
    final BlackSTIRFuturesSmileProviderDiscount blackPlus = new BlackSTIRFuturesSmileProviderDiscount(MULTICURVES, blackParameterPlus, EURIBOR3M);
    final BlackSTIRFuturesSmileProviderDiscount blackMinus = new BlackSTIRFuturesSmileProviderDiscount(MULTICURVES, blackParameterMinus, EURIBOR3M);
    final double pricePlus = METHOD_SECURITY_OPTION_BLACK.price(OPTION_ERU2, blackPlus);
    final double priceMinus = METHOD_SECURITY_OPTION_BLACK.price(OPTION_ERU2, blackMinus);
    final double priceSensiExpected = (pricePlus - priceMinus) / (2 * VOL_SHIFT);
    final SurfaceValue priceSensiComputed = METHOD_SECURITY_OPTION_BLACK.priceBlackSensitivity(OPTION_ERU2, BLACK_MULTICURVES);
    final DoublesPair point = DoublesPair.of(OPTION_ERU2.getExpirationTime(), STRIKE);
    assertEquals("Future option with Black volatilities: option security vol sensi", priceSensiExpected, priceSensiComputed.getMap().get(point), TOLERANCE_PRICE_DELTA);
    assertEquals("Future option with Black volatilities: option security vol sensi", 1, priceSensiComputed.getMap().size());
  }

  @Test
  /**
   * Test the option price Black sensitivity
   */
  public void presentValueBlackSensitivity() {
    final SurfaceValue pvbsSecurity = METHOD_SECURITY_OPTION_BLACK.priceBlackSensitivity(OPTION_ERU2, BLACK_MULTICURVES);
    final SurfaceValue pvbsTransactionComputed = METHOD_TRANSACTION_OPTION_BLACK.presentValueBlackSensitivity(TRANSACTION_1, BLACK_MULTICURVES);
    final SurfaceValue pvbsTransactionExpected = SurfaceValue.multiplyBy(pvbsSecurity, QUANTITY * NOTIONAL * FUTURE_FACTOR);
    assertTrue("Future option with Black volatilities: option security vol sensi", SurfaceValue.compare(pvbsTransactionComputed, pvbsTransactionExpected, TOLERANCE_PV_DELTA));
  }

}
