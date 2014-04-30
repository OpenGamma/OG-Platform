/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration;

import com.opengamma.analytics.financial.credit.isdastandardmodel.AccrualOnDefaultFormulae;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;

/**
 * 
 */
public class SuperFastCreditCurveBuilder extends ISDACompliantCreditCurveBuilder {

  public SuperFastCreditCurveBuilder() {
    super();

  }

  public SuperFastCreditCurveBuilder(final AccrualOnDefaultFormulae formula) {
    super(formula);
  }

  public SuperFastCreditCurveBuilder(final AccrualOnDefaultFormulae formula, final ArbitrageHandling arbHandle) {
    super(formula, arbHandle);
  }

  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] calibrationCDSs, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {

    final CreditCurveCalibrator calibrator = new CreditCurveCalibrator(calibrationCDSs, yieldCurve, getAccOnDefaultFormula(), getArbHanding());
    return calibrator.calibrate(premiums, pointsUpfront);
  }

}
