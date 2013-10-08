/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.fastcalibration;

/**
 * 
 */
public class CDSProtectionLeg {

  private final double _valuationDF;
  private final ProtectionLegElement[] _elements;

  public CDSProtectionLeg(final double valuationDF, final ProtectionLegElement[] elements) {
    _valuationDF = valuationDF;
    _elements = elements;
  }

  public double pv() {
    double pv = 0;
    for (final ProtectionLegElement e : _elements) {
      pv += e.getPV();
    }
    pv /= _valuationDF;
    return pv;
  }

  public double dPVdH(final int index) {
    double dPVdH = 0;
    for (final ProtectionLegElement e : _elements) {
      dPVdH += e.dPVdH(index);
    }
    dPVdH /= _valuationDF;
    return dPVdH;
  }
}
