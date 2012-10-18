/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFuturesUtils;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Class used to store future description and generate instruments.
 */
public class GeneratorInterestRateFutures extends GeneratorInstrument {

  /**
   * The Ibor index to which the future is linked.
   */
  private final IborIndex _iborIndex;
  /**
   * The accrual factor associated to the futures.
   */
  private final double _futuresAccrualFactor;

  /**
   * Constructor.
   * @param name The generator name.
   * @param iborIndex The Ibor index to which the future is linked.
   * @param futuresAccrualFactor The accrual factor associated to the futures.
   */
  public GeneratorInterestRateFutures(String name, IborIndex iborIndex, double futuresAccrualFactor) {
    super(name);
    ArgumentChecker.notNull(iborIndex, "Ibor index");
    _iborIndex = iborIndex;
    _futuresAccrualFactor = futuresAccrualFactor;
  }

  /**
   * Gets the underlying Ibor index..
   * @return The ibor index
   */
  public IborIndex getIborIndex() {
    return _iborIndex;
  }

  @Override
  /**
   * The future is the first futures after the date+tenor.
   */
  public InterestRateFutureDefinition generateInstrument(ZonedDateTime date, Period tenor, double marketQuote, double notional, Object... objects) {
    //TODO: Add monthly futures
    ArgumentChecker.isTrue(objects.length >= 1, "Number of quarterly futures required");
    ArgumentChecker.isTrue(objects[0] instanceof Integer, "Number of futures should be an Integer");
    Integer num = (Integer) objects[0];
    ZonedDateTime periodDate = ScheduleCalculator.getAdjustedDate(date, tenor, _iborIndex.getBusinessDayConvention(), _iborIndex.getCalendar(), _iborIndex.isEndOfMonth());
    ZonedDateTime referenceDate = InterestRateFuturesUtils.nextQuarterlyDate(num, periodDate);
    return InterestRateFutureDefinition.fromFixingPeriodStartDate(date, marketQuote, 1, referenceDate, _iborIndex, notional, _futuresAccrualFactor, "IRF");
  }

  @Override
  /**
   * The future is the first futures after the date+startTenor. The endTenor is not used.
   */
  public InterestRateFutureDefinition generateInstrument(final ZonedDateTime date, final Period startTenor, final Period endTenor, double marketQuote, double notional, Object... objects) {
    return generateInstrument(date, startTenor, marketQuote, notional, objects);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _iborIndex.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    GeneratorInterestRateFutures other = (GeneratorInterestRateFutures) obj;
    if (!ObjectUtils.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    return true;
  }

}
