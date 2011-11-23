/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 *  Class used to compute the price and sensitivity of a Ibor coupon in arrears. 
 *  The coupon are supposed to be exactly in arrears. The payment date is ignored and the start fixing period date is used instead.
 */
//TODO: Add a reference to Libor-with-delay pricing method when available.
public class CouponIborInArrearsReplicationMethod implements PricingMethod {

  /**
   * Base method for the pricing of standard cap/floors.
   */
  private final PricingMethod _baseMethod;

  /**
   * Constructor of the in-arrears pricing method.
   * @param baseMethod The base method for the pricing of standard cap/floors.
   */
  public CouponIborInArrearsReplicationMethod(PricingMethod baseMethod) {
    this._baseMethod = baseMethod;
  }

  /**
   * Computes the present value of an Ibor coupon in arrears by replication. The coupon is price as an cap with strike 0.
   * @param coupon The Ibor coupon.
   * @param sabrData The SABR data.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CouponIbor coupon, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(coupon);
    Validate.notNull(sabrData);
    CapFloorIbor cap0 = CapFloorIbor.from(coupon, 0.0, true);
    CapFloorIborInArrearsGenericReplicationMethod method = new CapFloorIborInArrearsGenericReplicationMethod(_baseMethod);
    return method.presentValue(cap0, sabrData);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CouponIbor, "Coupon Ibor");
    Validate.isTrue(curves instanceof SABRInterestRateDataBundle, "SABR interest rate data bundle required");
    return presentValue((CouponIbor) instrument, (SABRInterestRateDataBundle) curves);
  }

}
