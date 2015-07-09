/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.measures;

import com.opengamma.analytics.financial.credit.isdastandardmodel.AccrualOnDefaultFormulae;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
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
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.Result;

/**
 * Default implementation of the credit PV function
 */
public class DefaultCreditPvFn extends AbstractCreditRiskMeasureFn<CurrencyAmount> implements CreditPvFn {

  /**
   * Calculator for CDS measures
   */
  private final AnalyticCDSPricer _calculator;
  /**
   * Enumerate the types of PV that can be returned (usually clean or dirty)
   */
  private final PriceType _priceType;

  /**
   * Creates an instance.
   *
   * @param legacyCdsConverterFn a legacy cds converter
   * @param standardCdsConverterFn a standard cds converter
   * @param indexCdsConverterFn a index cds converter
   * @param standardCdsMarketDataResolverFn a market data resolver for standard cds
   * @param legacyCdsMarketDataResolverFn a market data resolver for legacy cds
   * @param creditCurveFn the credit curve function
   * @param priceType the type of PV that will be returned
   * @param accrualOnDefaultFormulae the accrual on default formulae to use
   */
  public DefaultCreditPvFn(LegacyCdsConverterFn legacyCdsConverterFn,
                           StandardCdsConverterFn standardCdsConverterFn,
                           IndexCdsConverterFn indexCdsConverterFn,
                           StandardCdsMarketDataResolverFn standardCdsMarketDataResolverFn,
                           LegacyCdsMarketDataResolverFn legacyCdsMarketDataResolverFn,
                           IndexCdsMarketDataResolverFn indexCdsMarketDataResolverFn,
                           IsdaCompliantCreditCurveFn creditCurveFn,
                           PriceType priceType,
                           AccrualOnDefaultFormulae accrualOnDefaultFormulae) {
    super(legacyCdsConverterFn,
          standardCdsConverterFn,
          indexCdsConverterFn,
          indexCdsMarketDataResolverFn,
          standardCdsMarketDataResolverFn,
          legacyCdsMarketDataResolverFn,
          creditCurveFn);

    ArgumentChecker.notNull(accrualOnDefaultFormulae, "accrualOnDefaultFormulae");
    _priceType = ArgumentChecker.notNull(priceType, "priceType");
    _calculator = new AnalyticCDSPricer(accrualOnDefaultFormulae);
  }

  @Override
  protected Result<CurrencyAmount> price(CdsData cdsData, CDSAnalytic cds, IsdaCreditCurve curve) {
    double pv = _calculator.pv(cds,
                               curve.getYieldCurve().getCalibratedCurve(),
                               curve.getCalibratedCurve(),
                               cdsData.getCoupon(),
                               _priceType);

    int sign = cdsData.isBuy() ? 1 : -1;
    double adjusted = pv * cdsData.getInterestRateNotional().getAmount() * sign;
    CurrencyAmount currencyAmount =  CurrencyAmount.of(cdsData.getInterestRateNotional().getCurrency(), adjusted);
    return Result.success(currencyAmount);
  }


}
