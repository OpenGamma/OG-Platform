/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.fastcalibration;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;

/**
 * 
 */
public class SuperFastCreditCurveBuilder implements ISDACompliantCreditCurveBuilder {

  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic calibrationCDS, final CDSQuoteConvention marketQuote, final ISDACompliantYieldCurve yieldCurve) {
    return null;
  }

  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] calibrationCDSs, final CDSQuoteConvention[] marketQuotes, final ISDACompliantYieldCurve yieldCurve) {
    return null;
  }

  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic cds, final double parSpread, final ISDACompliantYieldCurve yieldCurve) {
    return null;
  }

  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double pointsUpfront) {
    return null;
  }

  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] calibrationCDSs, final double[] parSpreads, final ISDACompliantYieldCurve yieldCurve) {
    final CreditCurveCalibrator calibrator = new CreditCurveCalibrator(calibrationCDSs, yieldCurve);
    final int n = calibrationCDSs.length;
    final CDSMarketInfo[] info = new CDSMarketInfo[n];
    for (int i = 0; i < n; i++) {
      info[i] = new CDSMarketInfo(parSpreads[i], 0.0, 1 - calibrationCDSs[i].getLGD());
    }

    return calibrator.calibrate(info);
  }

  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] calibrationCDSs, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {

    final CreditCurveCalibrator calibrator = new CreditCurveCalibrator(calibrationCDSs, yieldCurve);
    final int n = calibrationCDSs.length;
    final CDSMarketInfo[] info = new CDSMarketInfo[n];
    for (int i = 0; i < n; i++) {
      info[i] = new CDSMarketInfo(premiums[i], pointsUpfront[i], 1 - calibrationCDSs[i].getLGD());
    }

    return calibrator.calibrate(info);
  }

  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate endDate,
      final double fractionalParSpread, final boolean payAccOnDefault, final Period tenor, final StubType stubType, final boolean protectStart, final ISDACompliantYieldCurve yieldCurve,
      final double recoveryRate) {
    return null;
  }

  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate[] endDates,
      final double[] fractionalParSpreads, final boolean payAccOnDefault, final Period tenor, final StubType stubType, final boolean protectStart, final ISDACompliantYieldCurve yieldCurve,
      final double recoveryRate) {
    return null;
  }

}
