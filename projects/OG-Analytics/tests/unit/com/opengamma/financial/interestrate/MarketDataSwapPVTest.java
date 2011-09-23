/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * 
 */
public class MarketDataSwapPVTest {
  private static final double[] FUNDING_TIMES = new double[] {0.0027397260273972603, 0.005479452054794521, 0.0821917808219178, 0.16712328767123288, 0.2493150684931507, 0.33408189235721236,
      0.4187813459091249, 0.4980163185867206, 0.7493824388052998, 1.0034807994610375, 2.0027397260273974, 3.0, 4.0, 5.000748559023879, 10.0};
  private static final double[] FUNDING_YIELDS = new double[] {0.0013687474974368302, 0.0022305420277649083, 0.0012513719265854874, 0.0010636582500760411, 0.00101828702599128, 9.78165513963152E-4,
      8.959750791691782E-4, 8.943219128571554E-4, 9.938558765821793E-4, 9.239289700537953E-4, 9.936400853191142E-4, 0.0023392448443792023, 0.0044981802665752255, 0.006992645012259607,
      0.017680725566676783};
  private static final double[] FORWARD_TIMES = new double[] {0.0027397260273972603, 0.019178082191780823, 0.038356164383561646, 0.0821917808219178, 0.16712328767123288, 0.2493150684931507,
      0.49255183771240363, 0.7439179579309829, 0.9925518377124036, 1.238453477056666, 1.484931506849315, 1.7397260273972601, 2.0027397260273974, 3.0, 4.0, 5.000748559023879, 6.005479452054795,
      7.005479452054795, 8.002739726027396, 9.00074855902388, 10.0, 15.0, 20.0, 25.00074855902388, 30.002739726027396};
  private static final double[] FORWARD_YIELDS = new double[] {0.0014813901115749944, 0.0017545499377800639, 0.00194835932012297, 0.0022782899792324744, 0.002856875387467537, 0.003540919715057364,
      0.004438310251984415, 0.004852425503556177, 0.005017013047413322, 0.005084721597160578, 0.005119824771379274, 0.005137978289853433, 0.005191486919563363, 0.006333160551707626,
      0.008470126926325294, 0.010937769614837807, 0.01326825307368076, 0.015274635199645477, 0.016921786925804233, 0.01832073337206936, 0.01954285376126713, 0.023822140671398637,
      0.025254205542934515, 0.02597355690489602, 0.026370398520735996};
  private static final double[] FORWARD_BUMPED_YIELDS = new double[] {0.0014813901449354796, 0.00175454999482515, 0.001948359535212128, 0.0022782905824874702, 0.0028568765968019516,
      0.0035409212470074187, 0.00443831145255293, 0.004852426563767288, 0.00501701405289842, 0.005084722579606985, 0.005119825741040314, 0.005137979235589777, 0.005191925200579883,
      0.01629286582680315, 0.008344998893172821, 0.01084192698861046, 0.013188441808502899, 0.015206216995252457, 0.016861893537895873, 0.018267480206991333, 0.019494922517323467,
      0.023790185677964593, 0.025230233662100494, 0.02595437952038944, 0.026354416226774164};
  private static final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final YieldCurve FUNDING_CURVE = new YieldCurve(InterpolatedDoublesCurve.from(FUNDING_TIMES, FUNDING_YIELDS, INTERPOLATOR));
  private static final YieldCurve FORWARD_CURVE = new YieldCurve(InterpolatedDoublesCurve.from(FORWARD_TIMES, FORWARD_YIELDS, INTERPOLATOR));
  private static final YieldCurve FORWARD_BUMPED_CURVE = new YieldCurve(InterpolatedDoublesCurve.from(FORWARD_TIMES, FORWARD_BUMPED_YIELDS, INTERPOLATOR));
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2011, 5, 16);
  private static final ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2018, 5, 16);
  private static final double NOTIONAL = 9.6657e7;
  private static final Currency CURRENCY = Currency.USD;
  private static final SimpleFrequency FIXED_FREQUENCY = SimpleFrequency.SEMI_ANNUAL;
  private static final DayCount FIXED_DAYCOUNT = DayCountFactory.INSTANCE.getDayCount("30U/360");
  private static final BusinessDayConvention FIXED_BUSINESS_DAY_CONVENTION = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean FIXED_EOM = true;
  private static final double FIXED_RATE = 0.03164417;
  private static final boolean FIXED_IS_PAYER = true;
  private static final boolean FLOATING_IS_PAYER = false;
  private static final IborIndex FLOATING_INDEX = new IborIndex(CURRENCY, Period.ofMonths(3), 2, CALENDAR, DayCountFactory.INSTANCE.getDayCount("Actual/360"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), false);
  private static final AnnuityCouponFixedDefinition FIXED_LEG = AnnuityCouponFixedDefinition.from(CURRENCY, EFFECTIVE_DATE, MATURITY_DATE, FIXED_FREQUENCY, CALENDAR, FIXED_DAYCOUNT,
      FIXED_BUSINESS_DAY_CONVENTION, FIXED_EOM, NOTIONAL, FIXED_RATE, FIXED_IS_PAYER);
  private static final AnnuityCouponIborDefinition FLOATING_LEG = AnnuityCouponIborDefinition.from(EFFECTIVE_DATE, MATURITY_DATE, NOTIONAL, FLOATING_INDEX, FLOATING_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_LEG, FLOATING_LEG);
  private static final double LAST_FIXING = 0.00358056;
  private static final ZonedDateTime LAST_FIXING_DATE = DateUtils.getUTCDate(2011, 5, 16);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward_3m";
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 23);
  private static final DoubleTimeSeries<ZonedDateTime> TS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {LAST_FIXING_DATE}, new double[] {LAST_FIXING});
  private static final FixedCouponSwap<Coupon> SWAP = SWAP_DEFINITION.toDerivative(NOW, new DoubleTimeSeries[] {TS}, FUNDING_CURVE_NAME, FORWARD_CURVE_NAME);
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  @Test
  public void test() {
    System.out.println(SWAP);
    final YieldCurveBundle bundle1 = new YieldCurveBundle(new String[] {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME}, new YieldAndDiscountCurve[] {FUNDING_CURVE, FORWARD_CURVE});
    final double pv1 = CALCULATOR.visit(SWAP, bundle1);
    final YieldCurveBundle bundle2 = new YieldCurveBundle(new String[] {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME}, new YieldAndDiscountCurve[] {FUNDING_CURVE, FORWARD_BUMPED_CURVE});
    final double pv2 = CALCULATOR.visit(SWAP, bundle2);
    System.out.println(pv1);
  }

}
