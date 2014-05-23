package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

public class OISForwardRateProvider implements ForwardRateProvider<IndexON> {

  /**
   * Singleton instance.
   */
  private static final OISForwardRateProvider INSTANCE = new OISForwardRateProvider();
  
  /**
   * Singleton constructor.
   */
  private OISForwardRateProvider() {
  }
  
  /**
   * Returns a singleton.
   * @return a singleton.
   */
  public static OISForwardRateProvider getInstance() {
    return INSTANCE;
  }
  
  @Override
  public <T extends DepositIndexCoupon<IndexON>> double getRate(
      final MulticurveProviderInterface multicurves,
      final T coupon,
      final double fixingPeriodStartTime,
      final double fixingPeriodEndTime,
      final double fixingPeriodYearFraction) {
    return multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodYearFraction);
  }

}
