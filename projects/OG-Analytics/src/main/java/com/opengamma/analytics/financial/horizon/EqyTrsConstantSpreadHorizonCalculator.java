/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.equity.EquityTotalReturnSwap;
import com.opengamma.analytics.financial.equity.EquityTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
<<<<<<< HEAD
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
=======
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the difference in the present value of an equity total return swap between two dates without
 * rate slide i.e. assumes that the market moves in such a way that the discount factors or rates for the
 * same maturity <b>dates</b> will be equal.
 * <p>
 * Only the funding leg is considered in this calculation.
 */
<<<<<<< HEAD
public final class EqyTrsConstantSpreadHorizonCalculator extends HorizonCalculator<EquityTotalReturnSwapDefinition, MulticurveProviderInterface, ZonedDateTimeDoubleTimeSeries> {
  /** Rolls down a yield curve provider */
  private static final CurveProviderConstantSpreadRolldownFunction CURVE_ROLLDOWN = CurveProviderConstantSpreadRolldownFunction.getInstance();
  /** The present value calculator */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, MultipleCurrencyAmount> PV_CALCULATOR =
      PresentValueDiscountingCalculator.getInstance();
  /** The singleton instance */
  private static final HorizonCalculator<EquityTotalReturnSwapDefinition, MulticurveProviderInterface, ZonedDateTimeDoubleTimeSeries> INSTANCE =
=======
public final class EqyTrsConstantSpreadHorizonCalculator extends HorizonCalculator<EquityTotalReturnSwapDefinition, IssuerProviderInterface, Double> {
  /** Rolls down a yield curve provider */
  private static final CurveProviderConstantSpreadRolldownFunction CURVE_ROLLDOWN = CurveProviderConstantSpreadRolldownFunction.getInstance();
  /** The present value calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MultipleCurrencyAmount> PV_CALCULATOR =
      PresentValueIssuerCalculator.getInstance();
  /** The singleton instance */
  private static final HorizonCalculator<EquityTotalReturnSwapDefinition, IssuerProviderInterface, Double> INSTANCE =
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
      new EqyTrsConstantSpreadHorizonCalculator();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
<<<<<<< HEAD
  public static HorizonCalculator<EquityTotalReturnSwapDefinition, MulticurveProviderInterface, ZonedDateTimeDoubleTimeSeries> getInstance() {
=======
  public static HorizonCalculator<EquityTotalReturnSwapDefinition, IssuerProviderInterface, Double> getInstance() {
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private EqyTrsConstantSpreadHorizonCalculator() {
  }

  @Override
<<<<<<< HEAD
  public MultipleCurrencyAmount getTheta(final EquityTotalReturnSwapDefinition definition, final ZonedDateTime date, final MulticurveProviderInterface data,
      final int daysForward, final Calendar calendar, final ZonedDateTimeDoubleTimeSeries fixingSeries) {
=======
  public MultipleCurrencyAmount getTheta(final EquityTotalReturnSwapDefinition definition, final ZonedDateTime date, final IssuerProviderInterface data,
      final int daysForward, final Calendar calendar) {
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
<<<<<<< HEAD
    final EquityTotalReturnSwap instrumentToday = definition.toDerivative(date, fixingSeries);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final EquityTotalReturnSwap instrumentTomorrow = definition.toDerivative(horizonDate, fixingSeries);
    final MulticurveProviderInterface dataTomorrow = (MulticurveProviderInterface) CURVE_ROLLDOWN.rollDown(data, shiftTime);
=======
    final EquityTotalReturnSwap instrumentToday = definition.toDerivative(date);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final EquityTotalReturnSwap instrumentTomorrow = definition.toDerivative(horizonDate);
    final ParameterIssuerProviderInterface dataTomorrow = (ParameterIssuerProviderInterface) CURVE_ROLLDOWN.rollDown(data, shiftTime);
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
    final MultipleCurrencyAmount pvTomorrow = instrumentTomorrow.getFundingLeg().accept(PV_CALCULATOR, dataTomorrow);
    final MultipleCurrencyAmount pvToday = instrumentToday.getFundingLeg().accept(PV_CALCULATOR, data);
    return subtract(pvTomorrow, pvToday);
  }

<<<<<<< HEAD
  @Override
  public MultipleCurrencyAmount getTheta(final EquityTotalReturnSwapDefinition definition, final ZonedDateTime date, final MulticurveProviderInterface data,
      final int daysForward, final Calendar calendar) {
    return getTheta(definition, date, data, daysForward, calendar, ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC());
  }

=======
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
}
