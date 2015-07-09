/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.measures;

import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AccrualOnDefaultFormulae;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FiniteDifferenceSpreadSensitivityCalculator;
import com.opengamma.financial.analytics.TenorLabelledMatrix1D;
import com.opengamma.sesame.credit.CdsData;
import com.opengamma.sesame.credit.IsdaCompliantCreditCurveFn;
import com.opengamma.sesame.credit.IsdaCreditCurve;
import com.opengamma.sesame.credit.converter.IndexCdsConverterFn;
import com.opengamma.sesame.credit.converter.LegacyCdsConverterFn;
import com.opengamma.sesame.credit.converter.StandardCdsConverterFn;
import com.opengamma.sesame.credit.market.IndexCdsMarketDataResolverFn;
import com.opengamma.sesame.credit.market.LegacyCdsMarketDataResolverFn;
import com.opengamma.sesame.credit.market.StandardCdsMarketDataResolverFn;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.Tenor;

/**
 * Default implementation of the credit Bucketed CS01 function
 */
public class DefaultCreditBucketedCs01Fn extends AbstractCreditRiskMeasureFn<TenorLabelledMatrix1D> implements CreditBucketedCs01Fn {

  /**
   * The fraction bump amount, so a 1bp bump is 1e-4
   */
  private static final double SCALE = 1e-4;
  /**
   * Calculator for CDS measures
   */
  private final FiniteDifferenceSpreadSensitivityCalculator _calculator;

  /**
   * Creates an instance.
   *
   * @param legacyCdsConverterFn a legacy cds converter
   * @param standardCdsConverterFn a standard cds converter
   * @param standardCdsMarketDataResolverFn a market data resolver for standard cds
   * @param legacyCdsMarketDataResolverFn a market data resolver for legacy cds
   * @param creditCurveFn the credit curve function
   * @param accrualOnDefaultFormulae the accrual on default formulae to use
   */
  public DefaultCreditBucketedCs01Fn(LegacyCdsConverterFn legacyCdsConverterFn,
                                     StandardCdsConverterFn standardCdsConverterFn,
                                     IndexCdsConverterFn indexCdsConverterFn,
                                     StandardCdsMarketDataResolverFn standardCdsMarketDataResolverFn,
                                     LegacyCdsMarketDataResolverFn legacyCdsMarketDataResolverFn,
                                     IndexCdsMarketDataResolverFn indexCdsMarketDataResolverFn,
                                     IsdaCompliantCreditCurveFn creditCurveFn,
                                     AccrualOnDefaultFormulae accrualOnDefaultFormulae) {
    super(legacyCdsConverterFn,
          standardCdsConverterFn,
          indexCdsConverterFn,
          indexCdsMarketDataResolverFn,
          standardCdsMarketDataResolverFn,
          legacyCdsMarketDataResolverFn,
          creditCurveFn);
    ArgumentChecker.notNull(accrualOnDefaultFormulae, "accrualOnDefaultFormulae");
    _calculator = new FiniteDifferenceSpreadSensitivityCalculator(accrualOnDefaultFormulae);
  }


  @Override
  protected Result<TenorLabelledMatrix1D> price(CdsData cdsData, CDSAnalytic cds, IsdaCreditCurve curve) {

    ImmutableSortedSet<Tenor> tenors = cdsData.getTenors();
    Tenor[] tenorArray = tenors.toArray(new Tenor[tenors.size()]);
    CDSAnalytic[] cdsAnalytics = curve.getCalibratedCds().toArray(new CDSAnalytic[curve.getCalibratedCds().size()]);
    double[] bucketedCs01 = _calculator.bucketedCS01FromCreditCurve(cds,
                                                                    cdsData.getCoupon(),
                                                                    cdsAnalytics,
                                                                    curve.getYieldCurve().getCalibratedCurve(),
                                                                    curve.getCalibratedCurve(),
                                                                    SCALE);

    int sign = cdsData.isBuy() ? 1 : -1;
    double adjust = sign * cdsData.getInterestRateNotional().getAmount() * SCALE;
    double[] adjustedBCS01 = new double[bucketedCs01.length];
    for (int i = 0; i < bucketedCs01.length; i++) {
      adjustedBCS01[i] = bucketedCs01[i] * adjust;
    }

    TenorLabelledMatrix1D matrix1D = new TenorLabelledMatrix1D(tenorArray, adjustedBCS01);
    return Result.success(matrix1D);
  }
}
