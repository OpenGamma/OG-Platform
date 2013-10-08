/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.fastcalibration;

import com.opengamma.analytics.financial.credit.PriceType;

/**
 * 
 */
public class CDSPremiumLeg {

  private final double _valuationDF;
  private final double _accPremium;
  private final CDSPremiumPayment[] _coupons;

  public CDSPremiumLeg(final double valuationDF, final double accPrem, final CDSPremiumPayment[] coupons) {
    _valuationDF = valuationDF;
    _accPremium = accPrem;
    _coupons = coupons;
  }

  public double pv(final PriceType cleanOrDirty) {
    double pv = 0;
    for (final CDSPremiumPayment c : _coupons) {
      pv += c.getPV();
    }
    pv /= _valuationDF;
    if (cleanOrDirty == PriceType.CLEAN) {
      pv -= _accPremium;
    }
    return pv;
  }

  public double dPVdH(final int index) {
    double pvGrad = 0;
    for (final CDSPremiumPayment c : _coupons) {
      pvGrad += c.dPVdH(index);
    }
    pvGrad /= _valuationDF;
    return pvGrad;
  }

}
