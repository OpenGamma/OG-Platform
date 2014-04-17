/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExpiredException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of an interest rate future security.
 */
public class InterestRateFutureTransactionDefinition extends FuturesTransactionDefinition<InterestRateFutureSecurityDefinition>
    implements InstrumentDefinitionWithData<InstrumentDerivative, Double> {

  /**
   * Constructor.
   * @param underlying The underlying futures.
   * @param quantity The quantity/number of contract.
   * @param transactionDate The date at which the transaction was done.
   * @param transactionPrice The price at which the transaction was done.
   */
  public InterestRateFutureTransactionDefinition(final InterestRateFutureSecurityDefinition underlying, final long quantity, final ZonedDateTime transactionDate,
      final double transactionPrice) {
    super(underlying, quantity, transactionDate, transactionPrice);
  }

  /**
   * @param transactionDate The transaction date of the future, not null
   * @param transactionPrice The transaction price
   * @param quantity The quantity
   * @param lastTradingDate The last trading date, not null
   * @param fixingPeriodStartDate The start date of the Ibor fixing period, not null
   * @param fixingPeriodEndDate The end date of the Ibor fixing period, not null. Must be after the fixing period start date
   * @param iborIndex The Ibor index, not null
   * @param notional  The notional
   * @param paymentAccrualFactor The payment accrual factor, not negative or zero
   * @param name The name, not null
   * @param calendar The holiday calendar, not null
   * @deprecated Deprecated since 2.2.0.M17. Use the constructor with underlying.
   */
  @Deprecated
  public InterestRateFutureTransactionDefinition(final ZonedDateTime transactionDate, final double transactionPrice, final int quantity, final ZonedDateTime lastTradingDate,
      final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate, final IborIndex iborIndex, final double notional, final double paymentAccrualFactor,
      final String name, final Calendar calendar) {
    super(new InterestRateFutureSecurityDefinition(lastTradingDate, fixingPeriodStartDate, fixingPeriodEndDate, iborIndex, notional, paymentAccrualFactor,
        name, calendar), quantity, transactionDate, transactionPrice);
  }

  /**
   * Build a interest rate futures transaction from the fixing period start date.
   * @param transactionDate The date at which the transaction was done.
   * @param transactionPrice The price at which the transaction was done.
   * @param quantity The quantity/number of contract.
   * @param fixingPeriodStartDate The start date of the fixing period.
   * @param iborIndex The Ibor index associated to the future.
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor.
   * @param name The future name.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The interest rate futures.
   * @deprecated Deprecated since 2.2.0.M17. Use the constructor with underlying.
   */
  @Deprecated
  public static InterestRateFutureTransactionDefinition fromFixingPeriodStartDate(final ZonedDateTime transactionDate, final double transactionPrice, final int quantity,
      final ZonedDateTime fixingPeriodStartDate, final IborIndex iborIndex, final double notional, final double paymentAccrualFactor, final String name,
      final Calendar calendar) {
    ArgumentChecker.notNull(fixingPeriodStartDate, "Fixing period start date");
    ArgumentChecker.notNull(iborIndex, "Ibor index");
    final ZonedDateTime lastTradingDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, -iborIndex.getSpotLag(), calendar);
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, iborIndex, calendar);
    return new InterestRateFutureTransactionDefinition(transactionDate, transactionPrice, quantity, lastTradingDate, fixingPeriodStartDate, fixingPeriodEndDate, iborIndex, notional,
        paymentAccrualFactor, name, calendar);
  }

  public InterestRateFutureTransactionDefinition withNewNotionalAndTransactionPrice(final double notional, final double transactionPrice) {
    final InterestRateFutureSecurityDefinition sec = new InterestRateFutureSecurityDefinition(getUnderlyingSecurity().getLastTradingDate(),
        getUnderlyingSecurity().getIborIndex(), notional, getUnderlyingSecurity().getPaymentAccrualFactor(), getUnderlyingSecurity().getName(),
        getUnderlyingSecurity().getCalendar());
    return new InterestRateFutureTransactionDefinition(sec, getQuantity(), getTradeDate(), transactionPrice);
  }

  /**
   * {@inheritDoc}
   * @param lastMarginPrice The price on which the last margining was done.
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public InterestRateFutureTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice, final String... yieldCurveNames) {
    ArgumentChecker.notNull(dateTime, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    final LocalDate date = dateTime.toLocalDate();
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    final LocalDate transactionDateLocal = getTradeDate().toLocalDate();
    final LocalDate lastMarginDateLocal = getUnderlyingSecurity().getFixingPeriodStartDate().toLocalDate();
    if (date.isAfter(lastMarginDateLocal)) {
      throw new ExpiredException("Valuation date, " + date + ", is after last margin date, " + lastMarginDateLocal);
    }
    double referencePrice;
    if (transactionDateLocal.isBefore(date)) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = getTradePrice();
    }
    final InterestRateFutureSecurity underlying = getUnderlyingSecurity().toDerivative(dateTime, yieldCurveNames);
    final InterestRateFutureTransaction future = new InterestRateFutureTransaction(underlying, referencePrice, getQuantity());
    return future;
  }

  @Override
  public InstrumentDerivative toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() + " does not support the two argument method (without margin price data).");
  }

  /**
   * {@inheritDoc}
   * @param lastMarginPrice The price on which the last margining was done.
   */
  @Override
  public InterestRateFutureTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice) {
    final double referencePrice = referencePrice(dateTime, lastMarginPrice);
    final InterestRateFutureSecurity underlying = getUnderlyingSecurity().toDerivative(dateTime);
    final InterestRateFutureTransaction future = new InterestRateFutureTransaction(underlying, referencePrice, getQuantity());
    return future;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public InstrumentDerivative toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() + " does not support the two argument method (without margin price data).");
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureTransactionDefinition(this);
  }

}
