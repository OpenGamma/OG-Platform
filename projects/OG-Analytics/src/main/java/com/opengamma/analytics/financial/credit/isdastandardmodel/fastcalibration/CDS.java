/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration;

import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;

/**
 * 
 */
public class CDS {

  private final CDSPremiumLeg _premLeg;
  private final CDSProtectionLeg _protLeg;

  public CDS(final CDSProtectionLeg protLeg, final CDSPremiumLeg premLeg) {
    _protLeg = protLeg;
    _premLeg = premLeg;
  }

  public double pv(final double coupon, final double lgd, final PriceType cleanOrDirty) {
    return lgd * _protLeg.pv() - coupon * _premLeg.pv(cleanOrDirty);
  }

  public double dPVdH(final double coupon, final double lgd, final int index) {
    return lgd * _protLeg.dPVdH(index) - coupon * _premLeg.dPVdH(index);
  }

  public double protLeg(final double lgd) {
    return lgd * _protLeg.pv();
  }

  public double premiumLeg(final double coupon, final PriceType cleanOrDirty) {
    return coupon * _premLeg.pv(cleanOrDirty);
  }

}
