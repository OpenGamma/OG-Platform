/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.CreditCurveCalibrator;

/**
 */
public class AnnuityForSpreadISDAFunction extends AnnuityForSpreadFunction {
  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();
  private final CDSAnalytic _cds;
  private final ISDACompliantYieldCurve _yieldCurve;
  private final CreditCurveCalibrator _calibrator;

  /**
  * For a given quoted spread (aka 'flat' spread), this function returns the risky annuity (aka risky PV01, RPV01 or risky duration).
  * This works by first calibrating a constant hazard rate that recovers the given spread, then computing the value of the annuity from this
  * constant hazard rate. The ISDA standard CDS model is used for these calculations. 
   * @param cds analytic description of a CDS traded at a certain time 
   * @param yieldCurve Calibrated yield curve 
   */
  public AnnuityForSpreadISDAFunction(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve) {
    _cds = cds;
    _yieldCurve = yieldCurve;
    _calibrator = new CreditCurveCalibrator(new CDSAnalytic[] {cds }, yieldCurve);
  }

  @Override
  public Double evaluate(final Double spread) {
    final ISDACompliantCreditCurve cc = _calibrator.calibrate(new double[] {spread });
    return PRICER.annuity(_cds, _yieldCurve, cc, PriceType.CLEAN);
  }

}
