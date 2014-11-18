/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueDiscountingInflationIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of theta with curve using constant time shift. The theta is computed as a finite difference between the 
 * valuation on a given date and the valuation at a forward date. The curve provider is moved forward using the
 * CurveProviderConstantSpreadRolldownFunction.
 */
public class BondCapitalIndexedConstantSpreadHorizonCalculator {  

  /** The function to create the shifted provider. */
  private static final CurveProviderConstantSpreadRolldownFunction ROLLDOWN_PROVIDER = 
      CurveProviderConstantSpreadRolldownFunction.getInstance();
  /** The present value calculator for inflation and issuer. */
  private static final PresentValueDiscountingInflationIssuerCalculator PVDIIC = 
      PresentValueDiscountingInflationIssuerCalculator.getInstance();
  
  /**
   * Computes the theta for a capital indexed (inflation) bond. The theta is computed as a finite difference between 
   * the valuation on a given date and the valuation at a forward date. The curve provider is moved forward using the
   * CurveProviderConstantSpreadRolldownFunction.
   * @param bond The bond transaction.
   * @param valuationDate The starting date.
   * @param daysForward The numbers of days for the computation of the theta.
   * @param inflationIssuerValuation The inflation and issuer provider at the valuation date.
   * @param cpiFixing The historical time series of CPI fixing for the bond conversion.
   * @return The theta (as a MultipleCurrencyAmount).
   */
  public static MultipleCurrencyAmount getTheta(BondCapitalIndexedTransactionDefinition<? extends CouponInflationDefinition> bond,
      ZonedDateTime valuationDate, int daysForward, InflationIssuerProviderDiscount inflationIssuerValuation,
      DoubleTimeSeries<ZonedDateTime> cpiFixing) {
    ZonedDateTime valuationDateForward = valuationDate.plusDays(daysForward);
    double timeShift = TimeCalculator.getTimeBetween(valuationDate, valuationDateForward);
    InflationIssuerProviderDiscount inflationIssuerForward = 
        (InflationIssuerProviderDiscount) ROLLDOWN_PROVIDER.rollDown(inflationIssuerValuation, timeShift);    
    BondCapitalIndexedTransaction<Coupon> bondToday = bond.toDerivative(valuationDate, cpiFixing);  
    BondCapitalIndexedTransaction<Coupon> bondForward = 
        bond.toDerivative(valuationDateForward, cpiFixing);
    MultipleCurrencyAmount pvToday = bondToday.accept(PVDIIC, inflationIssuerValuation);
    MultipleCurrencyAmount pvForward = bondForward.accept(PVDIIC, inflationIssuerForward);
    MultipleCurrencyAmount theta = pvForward.plus(pvToday.multipliedBy(-1.0d));
    return theta;
  }

}
