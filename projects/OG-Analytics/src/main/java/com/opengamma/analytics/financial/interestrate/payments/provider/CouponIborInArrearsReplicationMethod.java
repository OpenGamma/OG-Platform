/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderInterface;
import com.opengamma.analytics.financial.provider.method.CapFloorIborSABRCapMethodInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *  Class used to compute the price and sensitivity of a Ibor coupon in arrears. 
 *  The coupon are supposed to be exactly in arrears. The payment date is ignored and the start fixing period date is used instead.
 */
//TODO: Add a reference to Libor-with-delay pricing method when available.
public class CouponIborInArrearsReplicationMethod {

  /**
   * Method for the pricing of in arrears cap/floors.
   */
  private final CapFloorIborInArrearsSABRCapGenericReplicationMethod _capMethod;

  /**
   * Constructor of the in-arrears pricing method.
   * @param baseMethod The base method for the pricing of standard cap/floors.
   */
  public CouponIborInArrearsReplicationMethod(CapFloorIborSABRCapMethodInterface baseMethod) {
    _capMethod = new CapFloorIborInArrearsSABRCapGenericReplicationMethod(baseMethod);
  }

  /**
   * Computes the present value of an Ibor coupon in arrears by replication. The coupon is price as an cap with strike 0.
   * @param coupon The Ibor coupon.
   * @param sabr The SABR cap and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIbor coupon, final SABRCapProviderInterface sabr) {
    ArgumentChecker.notNull(coupon, "The coupon shoud not be null");
    ArgumentChecker.notNull(sabr, "SABR cap provider");
    CapFloorIbor cap0 = CapFloorIbor.from(coupon, 0.0, true);
    return _capMethod.presentValue(cap0, sabr);
  }

}
