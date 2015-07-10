/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BillTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTotalReturnSwap;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the difference in the present value of a bill total return swap between two dates without
 * rate slide i.e. assumes that the market moves in such a way that the discount factors or rates for the
 * same maturity <b>dates</b> will be equal.
 */
public final class BillTrsConstantSpreadHorizonCalculator extends HorizonCalculator<BillTotalReturnSwapDefinition, IssuerProviderInterface, ZonedDateTimeDoubleTimeSeries> {
  /** Rolls down a yield curve provider */
  private static final CurveProviderConstantSpreadRolldownFunction CURVE_ROLLDOWN = CurveProviderConstantSpreadRolldownFunction.getInstance();
  /** The present value calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MultipleCurrencyAmount> PV_CALCULATOR =
      PresentValueIssuerCalculator.getInstance();
  /** The singleton instance */
  private static final HorizonCalculator<BillTotalReturnSwapDefinition, IssuerProviderInterface, ZonedDateTimeDoubleTimeSeries> INSTANCE =
      new BillTrsConstantSpreadHorizonCalculator();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static HorizonCalculator<BillTotalReturnSwapDefinition, IssuerProviderInterface, ZonedDateTimeDoubleTimeSeries> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private BillTrsConstantSpreadHorizonCalculator() {
  }

  @Override
  public MultipleCurrencyAmount getTheta(BillTotalReturnSwapDefinition definition, ZonedDateTime date, 
                                         IssuerProviderInterface data, int daysForward, Calendar calendar, 
                                         ZonedDateTimeDoubleTimeSeries fixingSeries) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    BillTotalReturnSwap instrumentToday = definition.toDerivative(date, fixingSeries);
    ZonedDateTime horizonDate = date.plusDays(daysForward);
    double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    BillTotalReturnSwap instrumentTomorrow = definition.toDerivative(horizonDate, fixingSeries);
    IssuerProviderInterface dataTomorrow = (IssuerProviderInterface) CURVE_ROLLDOWN.rollDown(data, shiftTime);
    MultipleCurrencyAmount fundingLegPvTomorrow = instrumentTomorrow.getFundingLeg().accept(PV_CALCULATOR, dataTomorrow);
    MultipleCurrencyAmount fundingLegPvToday = instrumentToday.getFundingLeg().accept(PV_CALCULATOR, data);
    MultipleCurrencyAmount billLegPvTomorrow = instrumentTomorrow.getAsset().accept(PV_CALCULATOR, dataTomorrow).multipliedBy(instrumentTomorrow.getQuantity());
    MultipleCurrencyAmount billLegPvToday = instrumentToday.getAsset().accept(PV_CALCULATOR, data).multipliedBy(instrumentToday.getQuantity());
    Currency assetCurrency = instrumentToday.getAsset().getCurrency();
    Currency fundingCurrency = instrumentToday.getFundingLeg().getCurrency();
    double fxRate = data.getMulticurveProvider().getFxRate(fundingCurrency, assetCurrency);
    MultipleCurrencyAmount pvToday = billLegPvToday.plus(CurrencyAmount.of(assetCurrency, fundingLegPvToday.getAmount(fundingCurrency) * fxRate));
    MultipleCurrencyAmount pvTomorrow = billLegPvTomorrow.plus(CurrencyAmount.of(assetCurrency, fundingLegPvTomorrow.getAmount(fundingCurrency) * fxRate));
    return subtract(pvTomorrow, pvToday);
  }

  @Override
  public MultipleCurrencyAmount getTheta(BillTotalReturnSwapDefinition definition, ZonedDateTime date, 
                                         IssuerProviderInterface data, int daysForward, 
                                         Calendar calendar) {
    return getTheta(definition, date, data, daysForward, calendar, ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC());
  }

}
