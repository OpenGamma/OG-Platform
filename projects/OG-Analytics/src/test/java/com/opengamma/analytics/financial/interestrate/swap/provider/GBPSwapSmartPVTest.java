/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.env.AnalyticsEnvironment;
import com.opengamma.analytics.financial.datasets.CalendarGBP;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponFloatingDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * debug the Baker St. GBP swap pricing difference
 */
public class GBPSwapSmartPVTest {
  private static final InstrumentDerivativeVisitor<ParameterProviderInterface, MultipleCurrencyMulticurveSensitivity> PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  /** The parameter sensitivity calculator */
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  /** The market quote sensitivity calculator */
  private static final MarketQuoteSensitivityBlockCalculator<ParameterProviderInterface> BUCKETED_PV01_CALCULATOR = new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();
  private static final double NOTIONAL = 2e9;
  private static final double COUPON = 0.02;
  private static final Calendar baseCalendar = new CalendarGBP("GBP");
  // private static final Calendar baseCalendar = new CalendarNoHoliday("No Holidays");
  private static final DayCount ACT365 = DayCounts.ACT_365;
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2014, 10, 6);
  // private static final ZonedDateTime SPOT_DATE = DateUtils.getUTCDate(2014, 2, 18);
  // // This just ensures the the swap does start on the spot date
  // private static final ZonedDateTime REF_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, 0, baseCalendar);

  // this is the single curve for IM
  private static final double[] GBP_LIBOR_KNOT_TIMES = new double[] {0.002739726027397, 0.019178082191781, 0.082191780821918, 0.164383561643836, 0.249315068493151, 0.498630136986301,
    0.747945205479452, 1, 1.4986301369863, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30, 35, 40, 45, 50 };
  private static final double[] GBP_LIBOR_ZERO_RATES = new double[] {0.00475626901070822, 0.00483107619161134, 0.00504645027587814, 0.00531456932134962, 0.00551204367300484, 0.0061026272153353,
    0.00684153896499341, 0.00765126878343251, 0.00940584623535082, 0.0111463649353094, 0.0151993630408899, 0.017520513620551, 0.0192799279116312, 0.0207051221084842, 0.0218964288340252,
    0.0229171448309875, 0.0238054222996467, 0.0245820797759827, 0.025876126744085, 0.0272638169223121, 0.0285519785481517, 0.0289179058336173, 0.0290094237232156, 0.028796927786485,
    0.0286085652864115, 0.0285639181095462, 0.0285319734875664 };

  // these multi curves are used for VM
  private static final double[] GBP_SONIA_KNOT_TIMES = new double[] {0.002739726027397, 0.019178082191781, 0.038356164383562, 0.084931506849315, 0.172602739726027, 0.252054794520548,
    0.336986301369863, 0.413698630136986, 0.501369863013699, 0.580821917808219, 0.671232876712329, 0.747945205479452, 0.832876712328767, 0.920547945205479, 1, 1.25205479452055, 1.5013698630137,
    1.75068493150685, 2.0027397260274, 3.0027397260274, 4.00821917808219, 5.00547945205479, 6.00547945205479, 7.00547945205479, 8.00547945205479, 9.00547945205479, 10.0109589041096, 12.0082191780822,
    15.0164383561644, 20.013698630137, 25.0164383561644, 30.0219178082192, 35.0246575342466, 40.027397260274, 45.0301369863014, 50.0356164383562 };
  private static final double[] GBP_SONIA_ZERO_RATES = new double[] {0.00430997455348493, 0.00431882113819674, 0.00431314320653989, 0.00431970749752497, 0.00444129725993973, 0.00447764229051919,
    0.00454934099818509, 0.00480522065276289, 0.00503962777637656, 0.00520911179002989, 0.00543690213062051, 0.00563610380093184, 0.00583579456204562, 0.00612718766319607, 0.00636519907010272,
    0.00717310918750252, 0.00795749811802235, 0.00870119236161661, 0.00946155387022128, 0.0123334511255753, 0.0143921749914485, 0.0159592596727554, 0.0172361877785297, 0.0183436791467994,
    0.0192793358953591, 0.0201222559973813, 0.0208986383710833, 0.0222510994961065, 0.023877100242259, 0.0256026136259111, 0.026288001664418, 0.0266311744778153, 0.0264014166694374,
    0.0263527169318637, 0.0262906111905873, 0.0263674066536486 };
  private static final double[] GBP_LIBOR1M_KNOT_TIMES = new double[] {0.084931506849315, 0.172602739726027, 0.252054794520548, 0.501369863013699, 0.747945205479452, 1, 2.0027397260274,
    3.0027397260274, 4.00821917808219, 5.00547945205479, 6.00547945205479, 7.00547945205479, 8.00547945205479, 9.00547945205479, 10.0109589041096, 12.0082191780822, 15.0164383561644, 20.013698630137,
    25.0164383561644, 30.0219178082192, 35.0246575342466, 40.027397260274, 45.0301369863014, 50.0356164383562 };
  private static final double[] GBP_LIBOR1M_ZERO_RATES = new double[] {0.00505581436649715, 0.00503291190352002, 0.00514256786002282, 0.00569033898394734, 0.00639149506676498, 0.00723232237139851,
    0.0105298436453575, 0.0135410574559515, 0.0158030551363141, 0.0174456995488895, 0.0187732338667046, 0.0199055537021988, 0.0208531180033394, 0.0216810024002466, 0.0224286195281712,
    0.0237374945104143, 0.0252160567356682, 0.0267142361493482, 0.0272605000077873, 0.0275097501922553, 0.0272973928154239, 0.02732399981293, 0.0272679024586238, 0.0273500955580994 };
  private static final double[] GBP_LIBOR3M_KNOT_TIMES = new double[] {0.252054794520548, 0.446575342465753, 0.695890410958904, 0.945205479452055, 1.19452054794521, 1.44383561643836,
    1.69315068493151, 1.96164383561644, 2.21095890410959, 3.0027397260274, 4.00821917808219, 5.00547945205479, 6.00547945205479, 7.00547945205479, 8.00547945205479, 9.00547945205479,
    10.0109589041096, 12.0082191780822, 15.0164383561644, 20.013698630137, 25.0164383561644, 30.0219178082192, 35.0246575342466, 40.027397260274, 45.0301369863014, 50.0356164383562 };
  private static final double[] GBP_LIBOR3M_ZERO_RATES = new double[] {0.00562351263918076, 0.00605140841711946, 0.00674163439039939, 0.00751172986806733, 0.00835257242596894, 0.00924368840059903,
    0.0101182341187527, 0.0110392349927795, 0.0118815761688227, 0.0141891783018791, 0.0164362685797301, 0.0180908568757903, 0.0194306164834304, 0.0205622948941999, 0.021509365473766,
    0.0223369044043144, 0.0230980379156519, 0.0243918201709714, 0.0258555923419827, 0.0273230838882434, 0.0278368027650289, 0.0280342598539863, 0.0278259808180962, 0.0277923406323035,
    0.0277421859831982, 0.0278284728253182 };
  private static final double[] GBP_LIBOR6M_KNOT_TIMES = new double[] {0.501369863013699, 1, 1.5013698630137, 2.0027397260274, 3.0027397260274, 4.00821917808219, 5.00547945205479, 6.00547945205479,
    7.00547945205479, 8.00547945205479, 9.00547945205479, 10.0109589041096, 12.0082191780822, 15.0164383561644, 20.013698630137, 25.0164383561644, 30.0219178082192, 35.0246575342466, 40.027397260274,
    45.0301369863014, 50.0356164383562 };
  private static final double[] GBP_LIBOR6M_ZERO_RATES = new double[] {0.00710174175249047, 0.00860451137005419, 0.0103551375418353, 0.0121086290753976, 0.015178756626544, 0.0175003704137641,
    0.0192427630940745, 0.0206581597595687, 0.0218403443388774, 0.0228520936339576, 0.0237315739821965, 0.0245039864998691, 0.025779797672448, 0.0271525952264722, 0.0284196247149596,
    0.0287909199539885, 0.0288932996886611, 0.0286980928245931, 0.0285278998759854, 0.0284929581122082, 0.0284672034337625 };

  private static final YieldAndDiscountCurve GBP_LIBOR_CURVE;
  private static final YieldAndDiscountCurve GBP_SONIA_CURVE;
  private static final YieldAndDiscountCurve GBP_LIBOR1M_CURVE;
  private static final YieldAndDiscountCurve GBP_LIBOR3M_CURVE;
  private static final YieldAndDiscountCurve GBP_LIBOR6M_CURVE;

  private static final MulticurveProviderDiscount SINGLE_CURVE;
  private static final MulticurveProviderDiscount MULTI_CURVE;
  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final IborIndex GBPLIBOR1M = MASTER_IBOR.getIndex("GBPLIBOR1M");
  private static final IborIndex GBPLIBOR3M = MASTER_IBOR.getIndex("GBPLIBOR3M");
  private static final IborIndex GBPLIBOR6M = MASTER_IBOR.getIndex("GBPLIBOR6M");
  private static final Currency CCY = GBPLIBOR1M.getCurrency();

  private static final ZonedDateTime[] FIXING_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 8, 18) };

  private static final ZonedDateTimeDoubleTimeSeries TS_GBPLIBOR3M = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(FIXING_DATES, new double[] {0.005605 });
  private static final ZonedDateTimeDoubleTimeSeries[] FIXINGS = new ZonedDateTimeDoubleTimeSeries[] {TS_GBPLIBOR3M };
  private static final Interpolator1D interpolator;
  static {
    interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    GBP_LIBOR_CURVE = new YieldCurve("GBP Libor", new InterpolatedDoublesCurve(GBP_LIBOR_KNOT_TIMES, GBP_LIBOR_ZERO_RATES, interpolator, true));
    GBP_SONIA_CURVE = new YieldCurve("GBP Sonia", new InterpolatedDoublesCurve(GBP_SONIA_KNOT_TIMES, GBP_SONIA_ZERO_RATES, interpolator, true));
    GBP_LIBOR1M_CURVE = new YieldCurve("GBP Libor 1M", new InterpolatedDoublesCurve(GBP_LIBOR1M_KNOT_TIMES, GBP_LIBOR1M_ZERO_RATES, interpolator, true));
    GBP_LIBOR3M_CURVE = new YieldCurve("GBP Libor 3M", new InterpolatedDoublesCurve(GBP_LIBOR3M_KNOT_TIMES, GBP_LIBOR3M_ZERO_RATES, interpolator, true));
    GBP_LIBOR6M_CURVE = new YieldCurve("GBP Libor 6M", new InterpolatedDoublesCurve(GBP_LIBOR6M_KNOT_TIMES, GBP_LIBOR6M_ZERO_RATES, interpolator, true));

    YieldCurve zero = new YieldCurve("zero", ConstantDoublesCurve.from(0.0));
    SINGLE_CURVE = new MulticurveProviderDiscount();
    SINGLE_CURVE.setOrReplaceCurve(CCY, GBP_LIBOR_CURVE);
    SINGLE_CURVE.setOrReplaceCurve(GBPLIBOR1M, GBP_LIBOR_CURVE);
    SINGLE_CURVE.setOrReplaceCurve(GBPLIBOR3M, GBP_LIBOR_CURVE);
    MULTI_CURVE = new MulticurveProviderDiscount();
    MULTI_CURVE.setOrReplaceCurve(CCY, GBP_SONIA_CURVE);
    // TRIPLE_CURVE.setOrReplaceCurve(GBPLIBOR1M, GBP_LIBOR1M_CURVE);
    MULTI_CURVE.setOrReplaceCurve(GBPLIBOR3M, GBP_LIBOR3M_CURVE);

    AnalyticsEnvironment.setInstance(AnalyticsEnvironment.getInstance().toBuilder().modelDayCount(DayCounts.ACT_365).build());
  }

  @Test
  public void testMulticurve() {
    System.out.println(SINGLE_CURVE);
    System.out.println(CCY);
    System.out.println(GBPLIBOR1M.getDayCount() + "\t" + GBPLIBOR1M.getTenor() + "\t" + GBPLIBOR1M.getSpotLag());
    System.out.println(GBPLIBOR3M.getDayCount() + "\t" + GBPLIBOR3M.getTenor() + "\t" + GBPLIBOR3M.getSpotLag());
    System.out.println(GBPLIBOR3M);
  }

  /**
   * Test the IM NPV and delta for a seasoned swap. 
   * This is a 3M fixed Vs float with a coupon of 2% - the effective date is 18-Feb-2014 and the termination date is the
   * 18_Feb-2019 (i.e. this was originally a 5Y swap) 
   */
  @Test
  public void testSeasonedSwap() {
    double coupon = 0.02;
    ZonedDateTime effDate = DateUtils.getUTCDate(2014, 2, 18);
    Period swapTenor = Period.ofYears(5); // termination date 18/02/2019
    SwapFixedIborDefinition swapDef = makeSwapDef(GBPLIBOR3M, effDate, swapTenor, coupon, NOTIONAL);

    //these number come from the SMART API
    double expImNpv = -2.012506863816616E7;
    DoubleMatrix1D expDelta = new DoubleMatrix1D(0, 0, -13295.06565595203, -10166.814913375081, 192.18569490757258, 500.95144492207777, 737.1719141579288, 1610.7473112728212, 2960.684803071864,
        5797.798336051921, 12428.744989705048, 517803.6258600666, 304194.28278619424, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    testImPriceAndDelta(swapDef, TRADE_DATE, expImNpv, expDelta);
  }

  private void testImPriceAndDelta(SwapFixedIborDefinition swapDef, ZonedDateTime tradeDate, double expImNpv, DoubleMatrix1D expDelta) {
    double oneBps = 1e-4;
    SwapFixedCoupon<Coupon> swap = swapDef.toDerivative(tradeDate, FIXINGS);
    MultipleCurrencyAmount price = swap.accept(PVDC, SINGLE_CURVE);
    assertEquals(expImNpv, price.getAmount(CCY), 1e-15 * NOTIONAL);

    //    MultipleCurrencyParameterSensitivity senseZeroRates = PSC.calculateSensitivity(swap, SINGLE_CURVE);
    //    DoubleMatrix1D vSense = senseZeroRates.getSensitivityByName("GBP Libor").get(CCY);

    Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getDeltaFunction(swap, GBPLIBOR3M, GBP_LIBOR_KNOT_TIMES, interpolator);
    DoubleMatrix1D vSense = func.evaluate(new DoubleMatrix1D(GBP_LIBOR_ZERO_RATES));

    // scale to be bps sense
    vSense = (DoubleMatrix1D) MA.scale(vSense, oneBps);
    AssertMatrix.assertEqualsVectors(expDelta, vSense, 1e-14 * NOTIONAL * oneBps);
  }

  private SwapFixedIborDefinition makeSwapDef(IborIndex index, ZonedDateTime effDate, Period swapTenor, double coupon, double notional) {
    // This just ensures the the swap does start on the spot date
    ZonedDateTime refDate = ScheduleCalculator.getAdjustedDate(effDate, -index.getSpotLag(), baseCalendar);
    GeneratorAttributeIR att = new GeneratorAttributeIR(swapTenor);
    Period tenor = index.getTenor();
    GeneratorSwapFixedIbor swapGen = new GeneratorSwapFixedIbor(index.toString() + " Swap", tenor, index.getDayCount(), index, baseCalendar);
    return swapGen.generateInstrument(refDate, coupon, notional, att);
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix1D> getDeltaFunction(final Swap<? extends Payment, ? extends Payment> swap, final IborIndex index, final double[] t, final Interpolator1D interpolator){
    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      String curveName = "GBP Libor";

      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D zeroRates) {
        YieldCurve curve = new YieldCurve(curveName, new InterpolatedDoublesCurve(t, zeroRates.getData(), interpolator, true));
        MulticurveProviderDiscount multiCurve = new MulticurveProviderDiscount();
        multiCurve.setOrReplaceCurve(CCY, curve);
        multiCurve.setOrReplaceCurve(index, curve);
        MultipleCurrencyParameterSensitivity senseZeroRates = PSC.calculateSensitivity(swap, multiCurve);
        return senseZeroRates.getSensitivityByName(curveName).get(CCY);
      }
    };
  }

  private double[] pv01FD(Swap<? extends Payment, ? extends Payment> swap, double[] t, double[] r, Interpolator1D interpolator) {
    double eps = 1e-6;
    int n = t.length;
    double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      double[] temp = r.clone();
      temp[i] += eps;
      double pUp = price(swap, t, temp, interpolator);
      temp[i] -= 2 * eps;
      double pDown = price(swap, t, temp, interpolator);
      res[i] = (pUp - pDown) / 2 / eps;
    }
    return res;
  }

  private double price(Swap<? extends Payment, ? extends Payment> swap, double[] t, double[] r, Interpolator1D interpolator) {
    YieldCurve curve = new YieldCurve("curve", new InterpolatedDoublesCurve(t, r, interpolator, true));
    MulticurveProviderDiscount multiCurve = new MulticurveProviderDiscount();
    multiCurve.setOrReplaceCurve(CCY, curve);
    multiCurve.setOrReplaceCurve(GBPLIBOR3M, curve);
    MultipleCurrencyAmount pv = swap.accept(PVDC, multiCurve);
    return pv.getAmount(CCY);
  }

  private ZonedDateTime getLastFixing(AnnuityDefinition<?> leg, ZonedDateTime now) {
    for (PaymentDefinition p : leg.getPayments()) {
      if (p.getPaymentDate().isAfter(now)) {
        return ((CouponFloatingDefinition) p).getFixingDate();
      }
    }
    throw new IllegalArgumentException("all payments in past");
  }

}
