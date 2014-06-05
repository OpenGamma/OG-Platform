/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
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
 * Calculates the difference in the present value of a bond total return swap between two dates without
 * rate slide i.e. assumes that the market moves in such a way that the discount factors or rates for the
 * same maturity <b>dates</b> will be equal.
 */
public final class BondTrsConstantSpreadHorizonCalculator extends HorizonCalculator<BondTotalReturnSwapDefinition, IssuerProviderInterface, ZonedDateTimeDoubleTimeSeries> {
  /** Rolls down a yield curve provider */
  private static final CurveProviderConstantSpreadRolldownFunction CURVE_ROLLDOWN = CurveProviderConstantSpreadRolldownFunction.getInstance();
  /** The present value calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MultipleCurrencyAmount> PV_CALCULATOR =
      PresentValueIssuerCalculator.getInstance();
  /** The singleton instance */
  private static final HorizonCalculator<BondTotalReturnSwapDefinition, IssuerProviderInterface, ZonedDateTimeDoubleTimeSeries> INSTANCE =
      new BondTrsConstantSpreadHorizonCalculator();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static HorizonCalculator<BondTotalReturnSwapDefinition, IssuerProviderInterface, ZonedDateTimeDoubleTimeSeries> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private BondTrsConstantSpreadHorizonCalculator() {
  }

  @Override
  public MultipleCurrencyAmount getTheta(final BondTotalReturnSwapDefinition definition, final ZonedDateTime date, final IssuerProviderInterface data,
      final int daysForward, final Calendar calendar, final ZonedDateTimeDoubleTimeSeries fixingSeries) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final BondTotalReturnSwap instrumentToday = definition.toDerivative(date, fixingSeries);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final BondTotalReturnSwap instrumentTomorrow = definition.toDerivative(horizonDate, fixingSeries);
    final IssuerProviderInterface dataTomorrow = (IssuerProviderInterface) CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final MultipleCurrencyAmount fundingLegPvTomorrow = instrumentTomorrow.getFundingLeg().accept(PV_CALCULATOR, dataTomorrow);
    final MultipleCurrencyAmount fundingLegPvToday = instrumentToday.getFundingLeg().accept(PV_CALCULATOR, data);
    final MultipleCurrencyAmount bondLegPvTomorrow = instrumentTomorrow.getAsset().accept(PV_CALCULATOR, dataTomorrow).multipliedBy(instrumentTomorrow.getQuantity());
    final MultipleCurrencyAmount bondLegPvToday = instrumentToday.getAsset().accept(PV_CALCULATOR, data).multipliedBy(instrumentToday.getQuantity());
    final Currency assetCurrency = instrumentToday.getAsset().getCurrency();
    final Currency fundingCurrency = instrumentToday.getFundingLeg().getCurrency();
    final double fxRate = data.getMulticurveProvider().getFxRate(fundingCurrency, assetCurrency);
    final MultipleCurrencyAmount pvToday = bondLegPvToday.plus(CurrencyAmount.of(assetCurrency, fundingLegPvToday.getAmount(fundingCurrency) * fxRate));
    final MultipleCurrencyAmount pvTomorrow = bondLegPvTomorrow.plus(CurrencyAmount.of(assetCurrency, fundingLegPvTomorrow.getAmount(fundingCurrency) * fxRate));
    return subtract(pvTomorrow, pvToday);
  }

  @Override
  public MultipleCurrencyAmount getTheta(final BondTotalReturnSwapDefinition definition, final ZonedDateTime date, final IssuerProviderInterface data,
      final int daysForward, final Calendar calendar) {
    return getTheta(definition, date, data, daysForward, calendar, ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC());
  }

}
