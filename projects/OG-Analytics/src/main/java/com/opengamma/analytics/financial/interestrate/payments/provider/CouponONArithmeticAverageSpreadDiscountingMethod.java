package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

public class CouponONArithmeticAverageSpreadDiscountingMethod {
  // FIXME: Class under construction, don't use yet.

  /**
   * The method unique instance.
   */
  private static final CouponONArithmeticAverageSpreadDiscountingMethod INSTANCE = new CouponONArithmeticAverageSpreadDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponONArithmeticAverageSpreadDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponONArithmeticAverageSpreadDiscountingMethod() {
  }

  /**
   * Computes the present value.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponONArithmeticAverageSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curve provider");
    final double[] delta = coupon.getFixingPeriodAccrualFactors();
    final double[] times = coupon.getFixingPeriodTimes();
    final int nbFwd = delta.length;
    final double[] forwardON = new double[nbFwd];
    double rateAccrued = coupon.getRateAccrued();
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      forwardON[loopfwd] = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), times[loopfwd], times[loopfwd + 1], delta[loopfwd]);
      rateAccrued += forwardON[loopfwd] * delta[loopfwd];
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = df * (rateAccrued * coupon.getNotional() + coupon.getSpreadAmount()); // Does not use the payment accrual factor.
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Computes the present value.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value curve sensitivities.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponONArithmeticAverageSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curve provider");
    // Forward sweep
    final double[] delta = coupon.getFixingPeriodAccrualFactors();
    final double[] times = coupon.getFixingPeriodTimes();
    final int nbFwd = delta.length;
    final double[] forwardON = new double[nbFwd];
    double rateAccrued = coupon.getRateAccrued();
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      forwardON[loopfwd] = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), times[loopfwd], times[loopfwd + 1], delta[loopfwd]);
      rateAccrued += forwardON[loopfwd] * delta[loopfwd];
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfBar = (rateAccrued * coupon.getNotional() + coupon.getSpreadAmount()) * pvBar;
    final double rateAccruedBar = df * coupon.getNotional() * pvBar;
    final double[] forwardONBar = new double[nbFwd];
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      forwardONBar[loopfwd] = delta[loopfwd] * rateAccruedBar;
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      listForward.add(new SimplyCompoundedForwardSensitivity(times[loopfwd], times[loopfwd + 1], delta[loopfwd], forwardONBar[loopfwd]));
    }
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyMulticurveSensitivity result = MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
    return result;
  }
}
