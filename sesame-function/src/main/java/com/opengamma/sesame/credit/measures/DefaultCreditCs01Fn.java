/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.measures;

import com.opengamma.analytics.financial.credit.isdastandardmodel.AccrualOnDefaultFormulae;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FiniteDifferenceSpreadSensitivityCalculator;
import com.opengamma.sesame.credit.CdsData;
import com.opengamma.sesame.credit.IsdaCompliantCreditCurveFn;
import com.opengamma.sesame.credit.IsdaCreditCurve;
import com.opengamma.sesame.credit.converter.LegacyCdsConverterFn;
import com.opengamma.sesame.credit.converter.StandardCdsConverterFn;
import com.opengamma.sesame.credit.market.LegacyCdsMarketDataResolverFn;
import com.opengamma.sesame.credit.market.StandardCdsMarketDataResolverFn;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.Result;

/**
 * Default implementation of the credit CS01 function
 */
public class DefaultCreditCs01Fn extends AbstractCreditRiskMeasureFn<CurrencyAmount> implements CreditCs01Fn {

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
  public DefaultCreditCs01Fn(LegacyCdsConverterFn legacyCdsConverterFn,
                             StandardCdsConverterFn standardCdsConverterFn,
                             StandardCdsMarketDataResolverFn standardCdsMarketDataResolverFn,
                             LegacyCdsMarketDataResolverFn legacyCdsMarketDataResolverFn,
                             IsdaCompliantCreditCurveFn creditCurveFn,
                             AccrualOnDefaultFormulae accrualOnDefaultFormulae) {
    super(legacyCdsConverterFn,
          standardCdsConverterFn,
          standardCdsMarketDataResolverFn,
          legacyCdsMarketDataResolverFn,
          creditCurveFn);
    ArgumentChecker.notNull(accrualOnDefaultFormulae, "accrualOnDefaultFormulae");
    _calculator = new FiniteDifferenceSpreadSensitivityCalculator(accrualOnDefaultFormulae);
  }


  @Override
  protected Result<CurrencyAmount> price(CdsData cdsData, CDSAnalytic cds, IsdaCreditCurve curve) {

    CDSAnalytic[] cdsAnalytics = curve.getCalibratedCds().toArray(new CDSAnalytic[curve.getCalibratedCds().size()]);
    double cs01 = _calculator.parallelCS01FromCreditCurve(cds,
                                                          cdsData.getCoupon(),
                                                          cdsAnalytics,
                                                          curve.getYieldCurve().getCalibratedCurve(),
                                                          curve.getCalibratedCurve(),
                                                          SCALE);
    int sign = cdsData.isBuy() ? 1 : -1;
    double adjusted = cs01 * sign * cdsData.getInterestRateNotional().getAmount() * SCALE;
    CurrencyAmount currencyAmount =  CurrencyAmount.of(cdsData.getInterestRateNotional().getCurrency(), adjusted);
    return Result.success(currencyAmount);
  }
}
