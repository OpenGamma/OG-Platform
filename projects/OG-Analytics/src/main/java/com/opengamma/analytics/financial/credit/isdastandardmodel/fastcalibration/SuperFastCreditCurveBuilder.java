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

  private final AccrualOnDefaultFormulae _formula;

  public SuperFastCreditCurveBuilder() {
    super(AccrualOnDefaultFormulae.OrignalISDA);
    _formula = AccrualOnDefaultFormulae.OrignalISDA;
  }

  public SuperFastCreditCurveBuilder(final AccrualOnDefaultFormulae formula) {
    super(formula);
    _formula = formula;
  }

  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] calibrationCDSs, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {

    final CreditCurveCalibrator calibrator = new CreditCurveCalibrator(calibrationCDSs, yieldCurve, _formula);
    final int n = calibrationCDSs.length;
    final CDSMarketInfo[] info = new CDSMarketInfo[n];
    for (int i = 0; i < n; i++) {
      info[i] = new CDSMarketInfo(premiums[i], pointsUpfront[i], 1 - calibrationCDSs[i].getLGD());
    }

    return calibrator.calibrate(info);
  }

}
