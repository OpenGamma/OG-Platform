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
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PresentValueCurveSensitivityNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PresentValueNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.NormalDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesSmileProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.normalstirfutures.NormalSTIRFuturesSensitivityFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
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
public class InterestRateFutureOptionMarginNormalSmileMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final InterpolatedDoublesSurface NORMAL_PARAMETERS = NormalDataSets.createNormalSurfaceFuturesPrices();
  private static final NormalSTIRFuturesSmileProviderDiscount NORMAL_MULTICURVES = new NormalSTIRFuturesSmileProviderDiscount(MULTICURVES, NORMAL_PARAMETERS, EURIBOR3M);

  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -EURIBOR3M.getSpotLag(), TARGET);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final double STRIKE = 0.9850;
  private static final InterestRateFutureSecurityDefinition ERU2_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, EURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, TARGET);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18, 10, 0);
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
  private static final InterestRateFutureOptionMarginSecurityNormalSmileMethod METHOD_SECURITY_OPTION_NORMAL = InterestRateFutureOptionMarginSecurityNormalSmileMethod.getInstance();
  private static final InterestRateFutureOptionMarginTransactionNormalSmileMethod METHOD_TRANSACTION_OPTION_NORMAL = InterestRateFutureOptionMarginTransactionNormalSmileMethod.getInstance();

  private static final PresentValueNormalSTIRFuturesCalculator PVNFC = PresentValueNormalSTIRFuturesCalculator.getInstance();
  private static final PresentValueCurveSensitivityNormalSTIRFuturesCalculator PVCSNFC = PresentValueCurveSensitivityNormalSTIRFuturesCalculator.getInstance();
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityParameterCalculator<NormalSTIRFuturesSmileProviderInterface> PSNFC = new ParameterSensitivityParameterCalculator<>(
      PVCSNFC);
  private static final NormalSTIRFuturesSensitivityFDCalculator PSNFC_FD = new NormalSTIRFuturesSensitivityFDCalculator(PVNFC, SHIFT);

  private static final NormalPriceFunction NORMAL_FUNCTION = new NormalPriceFunction();

  private static final double VOL_SHIFT = 1.0E-8;

  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-4;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  /**
   * Test the option price from the future price. Standard option.
   */
  @Test
  public void price() {
    final double expiry = OPTION_ERU2.getExpirationTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE, expiry, IS_CALL);
    final double priceFuture = METHOD_FUTURES.price(ERU2, MULTICURVES);
    final double volatility = NORMAL_PARAMETERS.getZValue(expiry, STRIKE);
    final NormalFunctionData dataNormal = new NormalFunctionData(priceFuture, 1.0, volatility);
    final double priceExpected = NORMAL_FUNCTION.getPriceFunction(option).evaluate(dataNormal);
    final double priceComputed = METHOD_SECURITY_OPTION_NORMAL.price(OPTION_ERU2, NORMAL_MULTICURVES);
    assertEquals("Future option with Black volatilities: option security price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  /**
   * Test the option transaction present value.
   */
  @Test
  public void presentValue() {
    final double priceOption = METHOD_SECURITY_OPTION_NORMAL.price(OPTION_ERU2, NORMAL_MULTICURVES);
    final double presentValue1Expected = (priceOption - MARGIN_PRICE) * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    final MultipleCurrencyAmount presentValue1Computed = METHOD_TRANSACTION_OPTION_NORMAL.presentValue(TRANSACTION_1, NORMAL_MULTICURVES);
    assertEquals("Future option with Black volatilities: option transaction pv", presentValue1Expected, presentValue1Computed.getAmount(EUR), TOLERANCE_PV);
    final double presentValue2Expected = (priceOption - TRADE_PRICE) * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    final MultipleCurrencyAmount presentValue2Computed = METHOD_TRANSACTION_OPTION_NORMAL.presentValue(TRANSACTION_2, NORMAL_MULTICURVES);
    assertEquals("Future option with Black volatilities: option transaction pv", presentValue2Expected, presentValue2Computed.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount presentValue1Calculator = TRANSACTION_1.accept(PVNFC, NORMAL_MULTICURVES);
    assertEquals("Future option with Black volatilities: option transaction pv", presentValue1Computed.getAmount(EUR), presentValue1Calculator.getAmount(EUR), TOLERANCE_PV);
  }

  /**
   * Tests present value curve sensitivity.
   */
  @Test
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PSNFC.calculateSensitivity(TRANSACTION_1, NORMAL_MULTICURVES, NORMAL_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PSNFC_FD.calculateSensitivity(TRANSACTION_1, NORMAL_MULTICURVES);
    AssertSensitivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  /**
   * Test the option price Black sensitivity
   */
  @Test
  public void priceNormalSensitivity() {
    final InterpolatedDoublesSurface normalParameterPlus = NormalDataSets.createNormalSurfaceFuturesPricesShift(VOL_SHIFT);
    final InterpolatedDoublesSurface NormalParameterMinus = NormalDataSets.createNormalSurfaceFuturesPricesShift(-VOL_SHIFT);
    final NormalSTIRFuturesSmileProviderDiscount blackPlus = new NormalSTIRFuturesSmileProviderDiscount(MULTICURVES, normalParameterPlus, EURIBOR3M);
    final NormalSTIRFuturesSmileProviderDiscount blackMinus = new NormalSTIRFuturesSmileProviderDiscount(MULTICURVES, NormalParameterMinus, EURIBOR3M);
    final double pricePlus = METHOD_SECURITY_OPTION_NORMAL.price(OPTION_ERU2, blackPlus);
    final double priceMinus = METHOD_SECURITY_OPTION_NORMAL.price(OPTION_ERU2, blackMinus);
    final double priceSensiExpected = (pricePlus - priceMinus) / (2 * VOL_SHIFT);
    final SurfaceValue priceSensiComputed = METHOD_SECURITY_OPTION_NORMAL.priceNormalSensitivity(OPTION_ERU2, NORMAL_MULTICURVES);
    final DoublesPair point = DoublesPair.of(OPTION_ERU2.getExpirationTime(), STRIKE);
    assertEquals("Future option with Black volatilities: option security vol sensi", priceSensiExpected, priceSensiComputed.getMap().get(point), TOLERANCE_PRICE_DELTA);
    assertEquals("Future option with Black volatilities: option security vol sensi", 1, priceSensiComputed.getMap().size());
  }

  /**
   * Test the option price Black sensitivity
   */
  @Test
  public void presentValueNormalSensitivity() {
    final SurfaceValue pvnsSecurity = METHOD_SECURITY_OPTION_NORMAL.priceNormalSensitivity(OPTION_ERU2, NORMAL_MULTICURVES);
    final SurfaceValue pvnsTransactionComputed = METHOD_TRANSACTION_OPTION_NORMAL.presentValueNormalSensitivity(TRANSACTION_1, NORMAL_MULTICURVES);
    final SurfaceValue pvnsTransactionExpected = SurfaceValue.multiplyBy(pvnsSecurity, QUANTITY * NOTIONAL * FUTURE_FACTOR);
    assertTrue("Future option with Black volatilities: option security vol sensi", SurfaceValue.compare(pvnsTransactionComputed, pvnsTransactionExpected, TOLERANCE_PV_DELTA));
  }

}
