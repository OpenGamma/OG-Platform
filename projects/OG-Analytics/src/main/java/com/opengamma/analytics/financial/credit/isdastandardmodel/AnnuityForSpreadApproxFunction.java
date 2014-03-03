/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import com.opengamma.analytics.math.utilities.Epsilon;

/**
 */
public class AnnuityForSpreadApproxFunction extends AnnuityForSpreadFunction {
  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();
  private final CDSAnalytic _cds;
  private final ISDACompliantYieldCurve _yieldCurve;
  private final double _eta;

  /**
  * For a given quoted spread (aka 'flat' spread), this function returns the risky annuity (aka risky PV01, RPV01 or risky duration).
  * This works by first calibrating a constant hazard rate that recovers the given spread, then computing the value of the annuity from this
  * constant hazard rate. The ISDA standard CDS model is used for these calculations. 
   * @param cds analytic description of a CDS traded at a certain time 
   * @param yieldCurve Calibrated yield curve 
   */
  public AnnuityForSpreadApproxFunction(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve) {
    _cds = cds;
    _yieldCurve = yieldCurve;
    _eta = cds.getCoupon(0).getYFRatio();
  }

  @Override
  public Double evaluate(final Double spread) {
    final double lambda = _eta * spread / _cds.getLGD();
    final ISDACompliantCreditCurve cc = new ISDACompliantCreditCurve(1.0, lambda);
    return PRICER.annuity(_cds, _yieldCurve, cc, PriceType.CLEAN);
  }
  


}
