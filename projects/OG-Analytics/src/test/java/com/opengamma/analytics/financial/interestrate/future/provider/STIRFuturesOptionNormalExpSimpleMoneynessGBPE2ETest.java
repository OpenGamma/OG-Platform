/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceMulticurveCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.DeltaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.GammaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionDeltaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionGammaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionThetaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionVegaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PresentValueCurveSensitivityNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PresentValueNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.ThetaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.VegaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesExpSimpleMoneynessProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
public class STIRFuturesOptionNormalExpSimpleMoneynessGBPE2ETest {
  private static final IndexIborMaster INDEX_MASTER = IndexIborMaster.getInstance();
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 2, 17, 9, 0);
  private static final BondAndSTIRFuturesE2EExamplesData DATA = new BondAndSTIRFuturesE2EExamplesData();

  /* curve and surface */
  private static final IborIndex GBPLIBOR3M = INDEX_MASTER.getIndex("GBPLIBOR3M");
  private static final Currency GBP = GBPLIBOR3M.getCurrency();
  private static final MulticurveProviderDiscount MULTICURVES;
  static {
    Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    String curveName = "GBP curve";
    InterpolatedDoublesCurve rawCurve = InterpolatedDoublesCurve.from(DATA.getTimeGBP(), DATA.getRateGBP(),
        interpolator, curveName);
    int compoundPeriodsPerYear = 1;
    YieldAndDiscountCurve singleCurve = YieldPeriodicCurve.from(compoundPeriodsPerYear, rawCurve);
    MULTICURVES = new MulticurveProviderDiscount();
    MULTICURVES.setCurve(GBPLIBOR3M, singleCurve);
  }
  private static final Interpolator1D VALUESSQUARE_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.SQUARE_LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D TIME_VALUESSQUARE_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.TIME_SQUARE, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(TIME_VALUESSQUARE_FLAT, VALUESSQUARE_FLAT);
  private static final InterpolatedDoublesSurface VOL_SURFACE_SIMPLEMONEY;
  static {
    VOL_SURFACE_SIMPLEMONEY = InterpolatedDoublesSurface.from(DATA.getExpiry(), DATA.getSimpleMoneyness(),
        DATA.getVolatility(), INTERPOLATOR_2D);
  }
  private static final NormalSTIRFuturesExpSimpleMoneynessProviderDiscount NORMAL_MULTICURVES = 
      new NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(MULTICURVES, VOL_SURFACE_SIMPLEMONEY, GBPLIBOR3M, false);
  
  /* Rate futures */
  private static final InterestRateFutureSecurityDefinition RATE_FUTURE_Q; // Quarterly
  private static final InterestRateFutureSecurityDefinition RATE_FUTURE_S; // Serial
  private static final InterestRateFutureSecurityDefinition RATE_FUTURE_M; // Mid-curve
  private static final ZonedDateTime LAST_TRADING_DATE_Q = DateUtils.getUTCDate(2014, 6, 19, 0, 0);
  private static final ZonedDateTime LAST_TRADING_DATE_S = DateUtils.getUTCDate(2014, 6, 19, 0, 0);
  private static final ZonedDateTime LAST_TRADING_DATE_M = DateUtils.getUTCDate(2015, 6, 17, 0, 0);
  private static final double NOTIONAL = 5000.0;
  private static final String FUTURE_NAME = "20140300";
  private static final Calendar CALENDAR = DATA.getGBPCalendar();
  private static final double PAYMENT_ACCRUAL_FACTOR = 0.25;
  static {
    RATE_FUTURE_Q = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE_Q, GBPLIBOR3M, NOTIONAL,
        PAYMENT_ACCRUAL_FACTOR, FUTURE_NAME, CALENDAR);
    RATE_FUTURE_S = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE_S, GBPLIBOR3M, NOTIONAL,
        PAYMENT_ACCRUAL_FACTOR, FUTURE_NAME, CALENDAR);
    RATE_FUTURE_M = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE_M, GBPLIBOR3M, NOTIONAL,
        PAYMENT_ACCRUAL_FACTOR, FUTURE_NAME, CALENDAR);
  }

  /* Rate futures option */
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2014, 6, 19, 0, 0);
  private static final ZonedDateTime EXPIRATION_DATE_S = DateUtils.getUTCDate(2014, 5, 21, 0, 0);
  private static final double STRIKE = 0.995;
  private static final boolean IS_CALL = false;
  private static final double STRIKE_S = 0.925;
  private static final boolean IS_CALL_S = true;
  private static final InterestRateFutureOptionMarginSecurityDefinition RATE_FUTURE_OPTION_Q = 
      new InterestRateFutureOptionMarginSecurityDefinition(RATE_FUTURE_Q, EXPIRATION_DATE, STRIKE, IS_CALL);
  private static final InterestRateFutureOptionMarginSecurityDefinition RATE_FUTURE_OPTION_S = 
      new InterestRateFutureOptionMarginSecurityDefinition(RATE_FUTURE_S, EXPIRATION_DATE_S, STRIKE_S, IS_CALL_S);
  private static final InterestRateFutureOptionMarginSecurityDefinition RATE_FUTURE_OPTION_M = 
      new InterestRateFutureOptionMarginSecurityDefinition(RATE_FUTURE_M, EXPIRATION_DATE, STRIKE, IS_CALL);

  /* Rate futures option transaction */
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2008, 8, 27, 1, 0);
  private static final long QUANTITY = 1;
  private static final double TRADE_PRICE = 0.0;
  private static final InterestRateFutureOptionMarginTransactionDefinition TRANSACTION_DEFINITION_Q =
      new InterestRateFutureOptionMarginTransactionDefinition(RATE_FUTURE_OPTION_Q, QUANTITY, TRADE_DATE, TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransactionDefinition TRANSACTION_DEFINITION_S =
      new InterestRateFutureOptionMarginTransactionDefinition(RATE_FUTURE_OPTION_S, QUANTITY, TRADE_DATE, TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransactionDefinition TRANSACTION_DEFINITION_M =
      new InterestRateFutureOptionMarginTransactionDefinition(RATE_FUTURE_OPTION_M, QUANTITY, TRADE_DATE, TRADE_PRICE);

  private static final Double LAST_MARGIN_PRICE = 0.004;
  private static final InterestRateFutureOptionMarginTransaction TRANSACTION_Q = TRANSACTION_DEFINITION_Q.toDerivative(
      VALUATION_DATE, LAST_MARGIN_PRICE);
  private static final InterestRateFutureOptionMarginTransaction TRANSACTION_S = TRANSACTION_DEFINITION_S.toDerivative(
      VALUATION_DATE, LAST_MARGIN_PRICE);
  private static final InterestRateFutureOptionMarginTransaction TRANSACTION_M = TRANSACTION_DEFINITION_M.toDerivative(
      VALUATION_DATE, LAST_MARGIN_PRICE);

  private static final FuturesPriceNormalSTIRFuturesCalculator POC =
      FuturesPriceNormalSTIRFuturesCalculator.getInstance();
  private static final PresentValueNormalSTIRFuturesCalculator PVC =
      PresentValueNormalSTIRFuturesCalculator.getInstance();
  private static final PresentValueCurveSensitivityNormalSTIRFuturesCalculator PVSC =
      PresentValueCurveSensitivityNormalSTIRFuturesCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<NormalSTIRFuturesProviderInterface> PSSFC =
      new ParameterSensitivityParameterCalculator<>(PVSC);
  private static final PV01CurveParametersCalculator<NormalSTIRFuturesProviderInterface> PV01PC =
      new PV01CurveParametersCalculator<>(PVSC);
  private static final PositionDeltaNormalSTIRFutureOptionCalculator PDEC =
      PositionDeltaNormalSTIRFutureOptionCalculator.getInstance();
  private static final PositionGammaNormalSTIRFutureOptionCalculator PGAC =
      PositionGammaNormalSTIRFutureOptionCalculator.getInstance();
  private static final PositionThetaNormalSTIRFutureOptionCalculator PTHC =
      PositionThetaNormalSTIRFutureOptionCalculator.getInstance();
  private static final PositionVegaNormalSTIRFutureOptionCalculator PVEC =
      PositionVegaNormalSTIRFutureOptionCalculator.getInstance();
  private static final FuturesPriceMulticurveCalculator FPC = FuturesPriceMulticurveCalculator.getInstance();

  private static final double TOL = 1.0e-10;
  private static final double TOLERANCE_PRICE = 1.0e-8;
  private static final double BP1 = 1.0E-4;

  /**
   * Options on GBPLIBOR3M futures
   */
  @Test
  public void testGBP() {
    double optionPriceQ = TRANSACTION_Q.accept(POC, NORMAL_MULTICURVES) * 100.0;
    MultipleCurrencyAmount pvQ = TRANSACTION_Q.accept(PVC, NORMAL_MULTICURVES);
    MultipleCurrencyParameterSensitivity bucketedPv01Q = PSSFC.calculateSensitivity(TRANSACTION_Q, NORMAL_MULTICURVES)
        .multipliedBy(BP1);
    ReferenceAmount<Pair<String, Currency>> pv01Q = TRANSACTION_Q.accept(PV01PC, NORMAL_MULTICURVES);
    double deltaQ = TRANSACTION_Q.accept(PDEC, NORMAL_MULTICURVES);
    double gammaQ = TRANSACTION_Q.accept(PGAC, NORMAL_MULTICURVES);
    double thetaQ = TRANSACTION_Q.accept(PTHC, NORMAL_MULTICURVES);
    double vegaQ = TRANSACTION_Q.accept(PVEC, NORMAL_MULTICURVES);

    double optionPriceS = TRANSACTION_S.accept(POC, NORMAL_MULTICURVES) * 100.0;
    MultipleCurrencyAmount pvS = TRANSACTION_S.accept(PVC, NORMAL_MULTICURVES);
    MultipleCurrencyParameterSensitivity bucketedPv01S = PSSFC.calculateSensitivity(TRANSACTION_S, NORMAL_MULTICURVES)
        .multipliedBy(BP1);
    ReferenceAmount<Pair<String, Currency>> pv01S = TRANSACTION_S.accept(PV01PC, NORMAL_MULTICURVES);
    double deltaS = TRANSACTION_S.accept(PDEC, NORMAL_MULTICURVES);
    double gammaS = TRANSACTION_S.accept(PGAC, NORMAL_MULTICURVES);
    double thetaS = TRANSACTION_S.accept(PTHC, NORMAL_MULTICURVES);
    double vegaS = TRANSACTION_S.accept(PVEC, NORMAL_MULTICURVES);

    double optionPriceM = TRANSACTION_M.accept(POC, NORMAL_MULTICURVES) * 100.0;
    MultipleCurrencyAmount pvM = TRANSACTION_M.accept(PVC, NORMAL_MULTICURVES);
    MultipleCurrencyParameterSensitivity bucketedPv01M = PSSFC.calculateSensitivity(TRANSACTION_M, NORMAL_MULTICURVES)
        .multipliedBy(BP1);
    ReferenceAmount<Pair<String, Currency>> pv01M = TRANSACTION_M.accept(PV01PC, NORMAL_MULTICURVES);
    double deltaM = TRANSACTION_M.accept(PDEC, NORMAL_MULTICURVES);
    double gammaM = TRANSACTION_M.accept(PGAC, NORMAL_MULTICURVES);
    double thetaM = TRANSACTION_M.accept(PTHC, NORMAL_MULTICURVES);
    double vegaM = TRANSACTION_M.accept(PVEC, NORMAL_MULTICURVES);

    double[] bucketedPv01ExpectedQ = new double[] {-0.054806430508358664, 0.06707132607404294, 0.05004545341589028,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityQ = new LinkedHashMap<>();
    sensitivityQ.put(ObjectsPair.of(MULTICURVES.getName(GBPLIBOR3M), GBP), new DoubleMatrix1D(bucketedPv01ExpectedQ));
    MultipleCurrencyParameterSensitivity expectedbucketQ = new MultipleCurrencyParameterSensitivity(sensitivityQ);

    double[] bucketedPv01ExpectedS = new double[] {0.0629242351204289, -0.07700577929588694, -0.057458072861900376,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityS = new LinkedHashMap<>();
    sensitivityS.put(ObjectsPair.of(MULTICURVES.getName(GBPLIBOR3M), GBP), new DoubleMatrix1D(bucketedPv01ExpectedS));
    MultipleCurrencyParameterSensitivity expectedbucketS = new MultipleCurrencyParameterSensitivity(sensitivityS);

    double[] bucketedPv01ExpectedM = new double[] {0.0, 0.0, 0.0, 0.0, -0.22709713168560872, 0.16225955718689586,
        0.12742291520626903, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityM = new LinkedHashMap<>();
    sensitivityM.put(ObjectsPair.of(MULTICURVES.getName(GBPLIBOR3M), GBP), new DoubleMatrix1D(bucketedPv01ExpectedM));
    MultipleCurrencyParameterSensitivity expectedbucketM = new MultipleCurrencyParameterSensitivity(sensitivityM);

    assertRelative("testGBP, Quarterly, option price", 17.54034187127257, optionPriceQ, TOL);
    assertRelative("testGBP, Quarterly, PV", 214.25427339090714, pvQ.getAmount(GBP), TOL);
    AssertSensitivityObjects.assertEquals("testGBP, Quarterly, bucketed pv01", expectedbucketQ, bucketedPv01Q, TOL);
    assertRelative("testGBP, Quarterly, pv01", 0.062310348981574545,
        pv01Q.getMap().get(Pairs.of(MULTICURVES.getName(GBPLIBOR3M), GBP)), TOL);
    assertRelative("testGBP, Quarterly, delta", -625.7416121580532, deltaQ, TOL);
    assertRelative("testGBP, Quarterly, gamma", 1136.3204100189546, gammaQ, TOL);
    assertRelative("testGBP, Quarterly, theta", -327.37192184568823, thetaQ, TOL);
    assertRelative("testGBP, Quarterly, vega", 288.3056023589055, vegaQ, TOL);

    assertRelative("testGBP, Serial, option price", 18.407490643468012, optionPriceS, TOL);
    assertRelative("testGBP, Serial, PV", 225.09363304335017, pvS.getAmount(GBP), TOL);
    AssertSensitivityObjects.assertEquals("testGBP, Serial, bucketed pv01", expectedbucketS, bucketedPv01S, TOL);
    assertRelative("testGBP, Serial, pv01", -0.07153961703735841,
        pv01S.getMap().get(Pairs.of(MULTICURVES.getName(GBPLIBOR3M), GBP)), TOL);
    assertRelative("testGBP, Serial, delta", 718.4250454344859, deltaS, TOL);
    assertRelative("testGBP, Serial, gamma", 1331.3307281718564, gammaS, TOL);
    assertRelative("testGBP, Serial, theta", -353.7610514224842, thetaS, TOL);
    assertRelative("testGBP, Serial, vega", 247.2880146963564, vegaS, TOL);

    assertRelative("testGBP, Mid-curve, option price", 21.609363997913587, optionPriceM, TOL);
    assertRelative("testGBP, Mid-curve, PV", 265.11704997391985, pvM.getAmount(GBP), TOL);
    AssertSensitivityObjects.assertEquals("testGBP, Mid-curve, bucketed pv01", expectedbucketM, bucketedPv01M, TOL);
    assertRelative("testGBP, Mid-curve, pv01", 0.06258534070755618,
        pv01M.getMap().get(Pairs.of(MULTICURVES.getName(GBPLIBOR3M), GBP)), TOL);
    assertRelative("testGBP, Mid-curve, delta", -631.4378001022222, deltaM, TOL);
    assertRelative("testGBP, Mid-curve, gamma", 935.5314630568365, gammaM, TOL);
    assertRelative("testGBP, Mid-curve, theta", -397.5689087443393, thetaM, TOL);
    assertRelative("testGBP, Mid-curve, vega", 288.2818961163769, vegaM, TOL);
  }

  /* Tests options on futures with 0 volatility.  */
  @Test
  public void volatility0() {
    InterpolatedDoublesSurface vol0 = InterpolatedDoublesSurface.from(new double[]{0.25, 5.0, 0.25, 5.0}, 
        new double[] {-0.10, -0.10, 0.10, 0.10}, new double[] {0.0, 0.0, 0.0, 0.0}, INTERPOLATOR_2D);
    NormalSTIRFuturesExpSimpleMoneynessProviderDiscount multicurveNormal0 = 
        new NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(MULTICURVES, vol0, GBPLIBOR3M, false);
    double optionPriceVol0 = TRANSACTION_Q.accept(POC, multicurveNormal0);    
    double futuresPrice = TRANSACTION_Q.getUnderlyingSecurity().getUnderlyingFuture().accept(FPC, MULTICURVES);
    double optionPriceIntrinsic =  STRIKE - futuresPrice; // Put
    assertEquals("Option price for a volatilituy of 0", optionPriceIntrinsic, optionPriceVol0, TOLERANCE_PRICE);
  }

  /**
   * Test volatility surface interpolation.
   */
  @Test
  public void volatilitySurfaceSamplingTest() {
    double expiry1 = 17.0 / 365.0;
    double moneyness1 = -0.0063;
    double interpolatedVol1 = VOL_SURFACE_SIMPLEMONEY.getZValue(expiry1, moneyness1);
    assertRelative("volatilitySurfaceSamplingPrintTest", 1.0623, interpolatedVol1, TOL);

    double expiry2 = 72.0 / 365.0;
    double moneyness2 = 0.0036;
    double interpolatedVol2 = VOL_SURFACE_SIMPLEMONEY.getZValue(expiry2, moneyness2);
    assertRelative("volatilitySurfaceSamplingPrintTest", 0.7228061226912788, interpolatedVol2, TOL);

    double expiry3 = 25.0 / 365.0;
    double moneyness3 = 0.005;
    double interpolatedVol3 = VOL_SURFACE_SIMPLEMONEY.getZValue(expiry3, moneyness3);
    assertRelative("volatilitySurfaceSamplingPrintTest", 0.8395130870530448, interpolatedVol3, TOL);
  }

  /**
   * Test negative strike rate, strike > 1.0
   */
  @Test
  public void negativeRateStrikeTest() {
    /*
     *  Put with negative strike rate.
     */
    double strikePut = 1.01;  // greater than 1
    InterestRateFutureOptionMarginSecurityDefinition optionNegativeStrikeQ =
        new InterestRateFutureOptionMarginSecurityDefinition(RATE_FUTURE_Q, EXPIRATION_DATE, strikePut, IS_CALL);
    InterestRateFutureOptionMarginTransactionDefinition transacQrtNegativeDfnQ = new InterestRateFutureOptionMarginTransactionDefinition(
        optionNegativeStrikeQ, QUANTITY, TRADE_DATE, TRADE_PRICE);
    InterestRateFutureOptionMarginTransaction transacQrtNegativeQ = transacQrtNegativeDfnQ.toDerivative(VALUATION_DATE,
        LAST_MARGIN_PRICE);
    InterestRateFutureOptionMarginSecurity futureOptionQ = transacQrtNegativeQ.getUnderlyingSecurity();
    InterestRateFutureSecurity futureQ = futureOptionQ.getUnderlyingFuture();
    double futurePriceQ = METHOD_FUTURE.price(futureQ, NORMAL_MULTICURVES.getMulticurveProvider());
    // Option price
    double optionPriceQ = METHOD_OPTION.priceFromFuturePrice(futureOptionQ, NORMAL_MULTICURVES, futurePriceQ) * 100.0;
    assertRelative("negativeRateStrikeTest", 22.948758005868182, optionPriceQ, TOL);
    // PV
    MultipleCurrencyAmount pvQ = METHOD_OPTION_MARGIN_TRANSA.presentValueFromFuturePrice(transacQrtNegativeQ,
        NORMAL_MULTICURVES, futurePriceQ);
    assertRelative("negativeRateStrikeTest", 281.8594750733523, pvQ.getAmount(GBP), TOL);
    // Option Greeks
    Double deltaQ = METHOD_OPTION_MARGIN.priceDeltaFromFuturePrice(futureOptionQ, NORMAL_MULTICURVES, futurePriceQ);
    assertRelative("negativeRateStrikeTest", -0.5112417249707829, deltaQ, TOL);
    Double gammaQ = METHOD_OPTION_MARGIN.priceGammaFromFuturePrice(futureOptionQ, NORMAL_MULTICURVES, futurePriceQ);
    assertRelative("negativeRateStrikeTest", 0.7180096667060368, gammaQ, TOL);
    Double thetaQ = METHOD_OPTION_MARGIN.priceThetaFromFuturePrice(futureOptionQ, NORMAL_MULTICURVES, futurePriceQ);
    assertRelative("negativeRateStrikeTest", -0.33132021712054266, thetaQ, TOL);
    Double vegaQ = METHOD_OPTION_MARGIN.priceVegaFromFuturePrice(futureOptionQ, NORMAL_MULTICURVES, futurePriceQ);
    assertRelative("negativeRateStrikeTest", 0.2305531595874542, vegaQ, TOL);
    // bucketed PV01
    MultipleCurrencyMulticurveSensitivity pvSenseQ = METHOD_OPTION_MARGIN_TRANSA
        .presentValueCurveSensitivityFromPrice(transacQrtNegativeQ, NORMAL_MULTICURVES, futurePriceQ);
    MultipleCurrencyParameterSensitivity bucketedPv01Q = PSSFC
        .pointToParameterSensitivity(pvSenseQ, NORMAL_MULTICURVES);
    MultipleCurrencyParameterSensitivity bucketedPv01RescaledQ = bucketedPv01Q.multipliedBy(BP1);
    double[] bucketedPv01ExpectedQ = new double[] {-0.055972252620278336, 0.06849804250654201, 0.05111000178461486,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityQ = new LinkedHashMap<>();
    sensitivityQ.put(ObjectsPair.of(MULTICURVES.getName(GBPLIBOR3M), GBP), new DoubleMatrix1D(bucketedPv01ExpectedQ));
    MultipleCurrencyParameterSensitivity expectedbucketQ = new MultipleCurrencyParameterSensitivity(sensitivityQ);
    AssertSensitivityObjects.assertEquals("negativeRateStrikeTest", expectedbucketQ, bucketedPv01RescaledQ, TOL);
    // PV01
    ReferenceAmount<Pair<String, Currency>> pv01Q = PV01PC.pv01CurveParameters(bucketedPv01Q);
    assertRelative("negativeRateStrikeTest", 0.06363579167087854,
        pv01Q.getMap().get(Pairs.of(MULTICURVES.getName(GBPLIBOR3M), GBP)), TOL);

    /*
     *  Call with negative strike rate.
     */
    double strikeCall = 1.015;  // greater than 1
    InterestRateFutureOptionMarginSecurityDefinition optionNegativeStrikeS =
        new InterestRateFutureOptionMarginSecurityDefinition(RATE_FUTURE_S, EXPIRATION_DATE, strikeCall, IS_CALL_S);
    InterestRateFutureOptionMarginTransactionDefinition transacSrtNegativeDfnS = new InterestRateFutureOptionMarginTransactionDefinition(
        optionNegativeStrikeS, QUANTITY, TRADE_DATE, TRADE_PRICE);
    InterestRateFutureOptionMarginTransaction transacSrtNegativeS = transacSrtNegativeDfnS.toDerivative(VALUATION_DATE,
        LAST_MARGIN_PRICE);
    InterestRateFutureOptionMarginSecurity futureOptionS = transacSrtNegativeS.getUnderlyingSecurity();
    InterestRateFutureSecurity futureS = futureOptionS.getUnderlyingFuture();
    double futurePriceS = METHOD_FUTURE.price(futureS, NORMAL_MULTICURVES.getMulticurveProvider());
    // Option price
    double optionPriceS = METHOD_OPTION.priceFromFuturePrice(futureOptionS, NORMAL_MULTICURVES, futurePriceS) * 100.0;
    assertRelative("negativeRateStrikeTest", 21.140011994079945, optionPriceS, TOL);
    // PV
    MultipleCurrencyAmount pvS = METHOD_OPTION_MARGIN_TRANSA.presentValueFromFuturePrice(transacSrtNegativeS,
        NORMAL_MULTICURVES, futurePriceS);
    assertRelative("negativeRateStrikeTest", 259.25014992599927, pvS.getAmount(GBP), TOL);
    // Option Greeks
    Double deltaS = METHOD_OPTION_MARGIN.priceDeltaFromFuturePrice(futureOptionS, NORMAL_MULTICURVES, futurePriceS);
    assertRelative("negativeRateStrikeTest", 0.4851687305611848, deltaS, TOL);
    Double gammaS = METHOD_OPTION_MARGIN.priceGammaFromFuturePrice(futureOptionS, NORMAL_MULTICURVES, futurePriceS);
    assertRelative("negativeRateStrikeTest", 0.7177984337559097, gammaS, TOL);
    Double thetaS = METHOD_OPTION_MARGIN.priceThetaFromFuturePrice(futureOptionS, NORMAL_MULTICURVES, futurePriceS);
    assertRelative("negativeRateStrikeTest", -0.3312227452477471, thetaS, TOL);
    Double vegaS = METHOD_OPTION_MARGIN.priceVegaFromFuturePrice(futureOptionS, NORMAL_MULTICURVES, futurePriceS);
    assertRelative("negativeRateStrikeTest", 0.23048533261197038, vegaS, TOL);
    // bucketed PV01
    MultipleCurrencyMulticurveSensitivity pvSenseS = METHOD_OPTION_MARGIN_TRANSA
        .presentValueCurveSensitivityFromPrice(transacSrtNegativeS, NORMAL_MULTICURVES, futurePriceS);
    MultipleCurrencyParameterSensitivity bucketedPv01S = PSSFC
        .pointToParameterSensitivity(pvSenseS, NORMAL_MULTICURVES);
    MultipleCurrencyParameterSensitivity bucketedPv01RescaledS = bucketedPv01S.multipliedBy(BP1);
    double[] bucketedPv01ExpectedS = new double[] {0.053117704256205496, -0.06500468703082543, -0.048503425040744896,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityS = new LinkedHashMap<>();
    sensitivityS.put(ObjectsPair.of(MULTICURVES.getName(GBPLIBOR3M), GBP), new DoubleMatrix1D(bucketedPv01ExpectedS));
    MultipleCurrencyParameterSensitivity expectedbucketS = new MultipleCurrencyParameterSensitivity(sensitivityS);
    AssertSensitivityObjects.assertEquals("negativeRateStrikeTest", expectedbucketS, bucketedPv01RescaledS, TOL);
    // PV01
    ReferenceAmount<Pair<String, Currency>> pv01S = PV01PC.pv01CurveParameters(bucketedPv01S);
    assertRelative("negativeRateStrikeTest", -0.060390407815364824,
        pv01S.getMap().get(Pairs.of(MULTICURVES.getName(GBPLIBOR3M), GBP)), TOL);
  }

  /**
   * Test negative forward rate, fwd > 1.0
   */
  @Test
  public void negativeRateFutureTest() {
    // new curve producing negative forward rate
    int nRates = DATA.getRateGBP().length;
    double[] bumpedRates = Arrays.copyOf(DATA.getRateGBP(), nRates);
    bumpedRates[0] = 0.00121959;
    bumpedRates[1] = -0.00332545;
    bumpedRates[2] = -0.00263185;
    bumpedRates[3] = 0.00163185;
    final MulticurveProviderDiscount multiCurve;
    Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    String curveName = "GBP curve";
    InterpolatedDoublesCurve rawCurve = InterpolatedDoublesCurve.from(DATA.getTimeGBP(), bumpedRates,
        interpolator, curveName);
    int compoundPeriodsPerYear = 1;
    YieldAndDiscountCurve singleCurve = YieldPeriodicCurve.from(compoundPeriodsPerYear, rawCurve);
    multiCurve = new MulticurveProviderDiscount();
    multiCurve.setCurve(GBPLIBOR3M, singleCurve);
    NormalSTIRFuturesExpSimpleMoneynessProviderDiscount normalMulticurve =
        new NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(multiCurve, VOL_SURFACE_SIMPLEMONEY, GBPLIBOR3M, false);

    /*
     * put option
     */
    InterestRateFutureOptionMarginSecurity futureOptionQ = TRANSACTION_Q.getUnderlyingSecurity();
    InterestRateFutureSecurity futureQ = futureOptionQ.getUnderlyingFuture();
    double futurePriceQ = METHOD_FUTURE.price(futureQ, normalMulticurve.getMulticurveProvider()); // greater than 1
    // Option price
    double optionPriceQ = METHOD_OPTION.priceFromFuturePrice(futureOptionQ, normalMulticurve, futurePriceQ) * 100.0;
    assertRelative("negativeRateFutureTest", 15.781077242671984, optionPriceQ, TOL);
    // PV
    MultipleCurrencyAmount pvQ = METHOD_OPTION_MARGIN_TRANSA.presentValueFromFuturePrice(TRANSACTION_Q,
        normalMulticurve, futurePriceQ);
    assertRelative("negativeRateStrikeTest", 192.2634655333998, pvQ.getAmount(GBP), TOL);
    // Option Greeks
    Double deltaQ = METHOD_OPTION_MARGIN.priceDeltaFromFuturePrice(futureOptionQ, normalMulticurve, futurePriceQ);
    assertRelative("negativeRateFutureTest", -0.488553523088012, deltaQ, TOL);
    Double gammaQ = METHOD_OPTION_MARGIN.priceGammaFromFuturePrice(futureOptionQ, normalMulticurve, futurePriceQ);
    assertRelative("negativeRateFutureTest", 0.972260987489144, gammaQ, TOL);
    Double thetaQ = METHOD_OPTION_MARGIN.priceThetaFromFuturePrice(futureOptionQ, normalMulticurve, futurePriceQ);
    assertRelative("negativeRateFutureTest", -0.2446711062778777, thetaQ, TOL);
    Double vegaQ = METHOD_OPTION_MARGIN.priceVegaFromFuturePrice(futureOptionQ, normalMulticurve, futurePriceQ);
    assertRelative("negativeRateFutureTest", 0.23054979307619397, vegaQ, TOL);
    // bucketed PV01
    MultipleCurrencyMulticurveSensitivity pvSenseQ = METHOD_OPTION_MARGIN_TRANSA
        .presentValueCurveSensitivityFromPrice(TRANSACTION_Q, normalMulticurve, futurePriceQ);
    MultipleCurrencyParameterSensitivity bucketedPv01UnscaledQ = PSSFC.pointToParameterSensitivity(pvSenseQ,
        normalMulticurve);
    MultipleCurrencyParameterSensitivity bucketedPv01Q = bucketedPv01UnscaledQ.multipliedBy(BP1);
    double[] bucketedPv01ExpectedQ = new double[] {-0.05361798658810737, 0.06589164141372715, 0.04910507773660669, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityQ = new LinkedHashMap<>();
    sensitivityQ.put(ObjectsPair.of(multiCurve.getName(GBPLIBOR3M), GBP), new DoubleMatrix1D(bucketedPv01ExpectedQ));
    MultipleCurrencyParameterSensitivity expectedbucketQ = new MultipleCurrencyParameterSensitivity(sensitivityQ);
    AssertSensitivityObjects.assertEquals("negativeRateStrikeTest", expectedbucketQ, bucketedPv01Q, TOL);
    // PV01
    ReferenceAmount<Pair<String, Currency>> pv01Q = PV01PC.pv01CurveParameters(bucketedPv01UnscaledQ);
    assertRelative("negativeRateFutureTest", 0.06137873256222647,
        pv01Q.getMap().get(Pairs.of(multiCurve.getName(GBPLIBOR3M), GBP)), TOL);

    /*
     * call option
     */
    InterestRateFutureOptionMarginSecurity futureOptionS = TRANSACTION_S.getUnderlyingSecurity();
    InterestRateFutureSecurity futureS = futureOptionS.getUnderlyingFuture();
    double futurePriceS = METHOD_FUTURE.price(futureS, normalMulticurve.getMulticurveProvider()); // greater than 1
    // Option price
    double optionPriceS = METHOD_OPTION.priceFromFuturePrice(futureOptionS, normalMulticurve, futurePriceS) * 100.0;
    assertRelative("negativeRateFutureTest", 19.129658973690766, optionPriceS, TOL);
    // PV
    MultipleCurrencyAmount pvS = METHOD_OPTION_MARGIN_TRANSA.presentValueFromFuturePrice(TRANSACTION_S,
        normalMulticurve, futurePriceS);
    assertRelative("negativeRateStrikeTest", 234.12073717113458, pvS.getAmount(GBP), TOL);
    // Option Greeks
    Double deltaS = METHOD_OPTION_MARGIN.priceDeltaFromFuturePrice(futureOptionS, normalMulticurve, futurePriceS);
    assertRelative("negativeRateFutureTest", 0.5879262616822158, deltaS, TOL);
    Double gammaS = METHOD_OPTION_MARGIN.priceGammaFromFuturePrice(futureOptionS, normalMulticurve, futurePriceS);
    assertRelative("negativeRateFutureTest", 1.05770727450262, gammaS, TOL);
    Double thetaS = METHOD_OPTION_MARGIN.priceThetaFromFuturePrice(futureOptionS, normalMulticurve, futurePriceS);
    assertRelative("negativeRateFutureTest", -0.2810538580740668, thetaS, TOL);
    Double vegaS = METHOD_OPTION_MARGIN.priceVegaFromFuturePrice(futureOptionS, normalMulticurve, futurePriceS);
    assertRelative("negativeRateFutureTest", 0.19646382863919248, vegaS, TOL);
    // bucketed PV01
    MultipleCurrencyMulticurveSensitivity pvSenseS = METHOD_OPTION_MARGIN_TRANSA
        .presentValueCurveSensitivityFromPrice(TRANSACTION_S, normalMulticurve, futurePriceS);
    MultipleCurrencyParameterSensitivity bucketedPv01UnscaledS = PSSFC.pointToParameterSensitivity(pvSenseS,
        normalMulticurve);
    MultipleCurrencyParameterSensitivity bucketedPv01S = bucketedPv01UnscaledS.multipliedBy(BP1);
    double[] bucketedPv01ExpectedS = new double[] {0.06452398954043419, -0.07929412967409682, -0.059093146234659076,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityS = new LinkedHashMap<>();
    sensitivityS.put(ObjectsPair.of(multiCurve.getName(GBPLIBOR3M), GBP), new DoubleMatrix1D(bucketedPv01ExpectedS));
    MultipleCurrencyParameterSensitivity expectedbucketS = new MultipleCurrencyParameterSensitivity(sensitivityS);
    AssertSensitivityObjects.assertEquals("negativeRateStrikeTest", expectedbucketS, bucketedPv01S, TOL);
    // PV01
    ReferenceAmount<Pair<String, Currency>> pv01S = PV01PC.pv01CurveParameters(bucketedPv01UnscaledS);
    assertRelative("negativeRateFutureTest", -0.07386328636832172,
        pv01S.getMap().get(Pairs.of(multiCurve.getName(GBPLIBOR3M), GBP)), TOL);

  }

  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURE = InterestRateFutureSecurityDiscountingMethod
      .getInstance();
  private static final InterestRateFutureOptionMarginSecurityNormalSmileMethod METHOD_OPTION = InterestRateFutureOptionMarginSecurityNormalSmileMethod
      .getInstance();
  private static final InterestRateFutureOptionMarginSecurityNormalSmileMethod METHOD_OPTION_MARGIN = InterestRateFutureOptionMarginSecurityNormalSmileMethod
      .getInstance();
  private static final InterestRateFutureOptionMarginTransactionNormalSmileMethod METHOD_OPTION_MARGIN_TRANSA = InterestRateFutureOptionMarginTransactionNormalSmileMethod
      .getInstance();
  private static final DeltaNormalSTIRFutureOptionCalculator DEC = DeltaNormalSTIRFutureOptionCalculator.getInstance();
  private static final GammaNormalSTIRFutureOptionCalculator GAC = GammaNormalSTIRFutureOptionCalculator.getInstance();
  private static final ThetaNormalSTIRFutureOptionCalculator THC = ThetaNormalSTIRFutureOptionCalculator.getInstance();
  private static final VegaNormalSTIRFutureOptionCalculator VEC = VegaNormalSTIRFutureOptionCalculator.getInstance();

  /**
   * Replicating the calculators by the underlying methods in which futures price is taken as an input. 
   */
  @Test
  public void underlyingMethodTest() {
    double localTol = 1.0e-14;
    InterestRateFutureOptionMarginSecurity futureOption = TRANSACTION_Q.getUnderlyingSecurity();
    InterestRateFutureSecurity future = futureOption.getUnderlyingFuture();
    double futurePrice = METHOD_FUTURE.price(future, NORMAL_MULTICURVES.getMulticurveProvider());

    // Option price and PV
    double optionPriceQ = TRANSACTION_Q.accept(POC, NORMAL_MULTICURVES);
    double optionPriceQRe = METHOD_OPTION.priceFromFuturePrice(futureOption, NORMAL_MULTICURVES, futurePrice);
    assertRelative("underlyingMethodTest", optionPriceQ, optionPriceQRe, localTol);
    MultipleCurrencyAmount pvQ = TRANSACTION_Q.accept(PVC, NORMAL_MULTICURVES);
    MultipleCurrencyAmount pvQRe = METHOD_OPTION_MARGIN_TRANSA.presentValueFromFuturePrice(TRANSACTION_Q,
        NORMAL_MULTICURVES, futurePrice);
    assertRelative("underlyingMethodTest", pvQ.getAmount(GBP), pvQRe.getAmount(GBP), localTol);

    // Option Greeks
    Double delta = TRANSACTION_Q.accept(DEC, NORMAL_MULTICURVES);
    Double deltaRe = METHOD_OPTION_MARGIN.priceDeltaFromFuturePrice(futureOption, NORMAL_MULTICURVES, futurePrice);
    assertRelative("underlyingMethodTest", delta, deltaRe, localTol);
    Double gamma = TRANSACTION_Q.accept(GAC, NORMAL_MULTICURVES);
    Double gammaRe = METHOD_OPTION_MARGIN.priceGammaFromFuturePrice(futureOption, NORMAL_MULTICURVES, futurePrice);
    assertRelative("underlyingMethodTest", gamma, gammaRe, localTol);
    Double theta = TRANSACTION_Q.accept(THC, NORMAL_MULTICURVES);
    Double thetaRe = METHOD_OPTION_MARGIN.priceThetaFromFuturePrice(futureOption, NORMAL_MULTICURVES, futurePrice);
    assertRelative("underlyingMethodTest", theta, thetaRe, localTol);
    Double vega = TRANSACTION_Q.accept(VEC, NORMAL_MULTICURVES);
    Double vegaRe = METHOD_OPTION_MARGIN.priceVegaFromFuturePrice(futureOption, NORMAL_MULTICURVES, futurePrice);
    assertRelative("underlyingMethodTest", vega, vegaRe, localTol);

    // PV01
    MultipleCurrencyParameterSensitivity bucketedPv01Q = PSSFC.calculateSensitivity(TRANSACTION_Q, NORMAL_MULTICURVES);
    MultipleCurrencyMulticurveSensitivity pvSense = METHOD_OPTION_MARGIN_TRANSA
        .presentValueCurveSensitivityFromPrice(TRANSACTION_Q, NORMAL_MULTICURVES, futurePrice);
    MultipleCurrencyParameterSensitivity bucketedPv01QRe = PSSFC.pointToParameterSensitivity(pvSense,
        NORMAL_MULTICURVES);
    AssertSensitivityObjects.assertEquals("underlyingMethodTest", bucketedPv01Q, bucketedPv01QRe, localTol);
    ReferenceAmount<Pair<String, Currency>> pv01Q = TRANSACTION_Q.accept(PV01PC, NORMAL_MULTICURVES);
    ReferenceAmount<Pair<String, Currency>> pv01QRe = PV01PC.pv01CurveParameters(bucketedPv01QRe);
    assertRelative("underlyingMethodTest", pv01Q.getMap().get(Pairs.of(MULTICURVES.getName(GBPLIBOR3M), GBP)), pv01QRe
        .getMap().get(Pairs.of(MULTICURVES.getName(GBPLIBOR3M), GBP)), localTol);
  }

  private void assertRelative(String message, double expected, double obtained, double relativeTol) {
    double ref = Math.max(Math.abs(expected), 1.0);
    assertEquals(message, expected, obtained, ref * relativeTol);
  }
}
