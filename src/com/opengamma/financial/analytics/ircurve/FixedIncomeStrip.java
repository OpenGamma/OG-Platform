/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.io.Serializable;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A fixed income strip.
 */
public class FixedIncomeStrip implements Comparable<FixedIncomeStrip>, Serializable {

  private final LocalDate _startDate;
  private final LocalDate _endDate;
  private final UniqueIdentifier _marketDataKey;
  private final StripInstrument _instrumentType;
  private DayCount _dayCount;
  private BusinessDayConvention _businessDayConvention;
  private String _regionISO;

  /**
   * Creates the strip.  REVIEW: jim 30-June-2010 -- we need to change the isFuture parameter to get OGLD to do it.
   * @param startDate the startDate of the strip
   * @param endDate the endDate of the strip
   * @param marketDataKey the market data key, not null
   * @param instrumentType the instrument type
   * @param dayCount the daycount for this instrument
   * @param businessDayConvention the business day convention for this instrument or null if not applicable
   * @param regionISO the region ISO code for this instrument or null if not applicable
   */
  public FixedIncomeStrip(LocalDate startDate, LocalDate endDate, UniqueIdentifier marketDataKey, StripInstrument instrumentType, 
                          DayCount dayCount, BusinessDayConvention businessDayConvention, String regionISO) {
    ArgumentChecker.notNull(marketDataKey, "Market data key");
    _startDate = startDate;
    _endDate = endDate;
    _marketDataKey = marketDataKey;
    _instrumentType = instrumentType;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _regionISO = regionISO;
  }
  
  /**
   * Creates the strip.  REVIEW: jim 30-June-2010 -- we need to change the isFuture parameter to get OGLD to do it.
   * This constructor assumes that the DayCountConvention is NONE.
   * @param period from today when the strip ends
   * @param marketDataKey the market data key, not null
   * @param instrumentType the instrument type
   */
  public FixedIncomeStrip(Period period, UniqueIdentifier marketDataKey, StripInstrument instrumentType) {
    ArgumentChecker.notNull(marketDataKey, "Market data key");
    ArgumentChecker.notNegative(period.getDays(), "period");
    _startDate = Clock.systemDefaultZone().today();
    _endDate = _startDate.plus(period);
    _marketDataKey = marketDataKey;
    _instrumentType = instrumentType;
    _dayCount = DayCountFactory.INSTANCE.getDayCount("30/360");
    _businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified");
    _regionISO = "US";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the start date of the strip
   * @return the start date
   */
  public LocalDate getStartDate() {
    return _startDate;
  }
  
  /**
   * Gets the end date of the strip
   * @return the end date
   */
  public LocalDate getEndDate() {
    return _endDate;
  }

  /**
   * Gets the market data identifier.
   * @return the market data key, not null
   */
  public UniqueIdentifier getMarketDataKey() {
    return _marketDataKey;
  }

  /**
   * Gets the specification for the market data key.
   * @return the specification, not null
   */
  public ComputationTargetSpecification getMarketDataSpecification() {
    // REVIEW kirk 2009-12-30 -- We might want to cache this on construction if it's called a lot.
    return new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, getMarketDataKey());
  }
  
  /**
   * Returns the type of instrument used to construct the strip.
   * should be removed as soon as we can put the appropriate instruments in as securities.
   * @return an enum describing the instrument type used to construct this strip
   */
  public StripInstrument getInstrumentType() {
    return _instrumentType;
  }

  /**
   * @return the day count convention for this strip's instrument
   */
  public DayCount getDayCount() {
    return _dayCount;
  }
  
  /**
   * @return the business day convention for this strips instrument
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }
  
  /**
   * @return the 2 digit ISO code for this strips country/region
   */
  public String getRegion() {
    return _regionISO;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public int compareTo(FixedIncomeStrip other) {
    int dates = getEndDate().compareTo(other.getEndDate());
    if (dates != 0) {
      return dates;
    } else {
      return getMarketDataKey().compareTo(other.getMarketDataKey());
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FixedIncomeStrip) {
      FixedIncomeStrip other = (FixedIncomeStrip) obj;
      return ObjectUtils.equals(_startDate, other._startDate) &&
             ObjectUtils.equals(_endDate, other._endDate) &&
             ObjectUtils.equals(_marketDataKey, other._marketDataKey) &&
             ObjectUtils.equals(_dayCount, other._dayCount) &&
             ObjectUtils.equals(_businessDayConvention, other._businessDayConvention) &&
             ObjectUtils.equals(_regionISO, other._regionISO) &&
             _instrumentType == other._instrumentType;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _startDate.hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
