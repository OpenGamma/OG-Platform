/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Class with the description of swap characteristics.
 */
public class GeneratorSwapIborCompoundingIbor extends GeneratorInstrument<GeneratorAttributeIR> {

  /**
   * The Ibor index of the first leg.
   */
  private final IborIndex _iborIndex1;
  /**
   * The period on which the first leg is compounded. 
   */
  private final Period _compoundingPeriod1;
  /**
   * The Ibor index of the second leg. The two index should be related to the same currency.
   */
  private final IborIndex _iborIndex2;
  /**
   * The business day convention associated to the swap.
   */
  private final BusinessDayConvention _businessDayConvention;
  /**
   * The flag indicating if the end-of-month rule is used for the swap.
   */
  private final boolean _endOfMonth;
  /**
   * The index spot lag in days between trade and settlement date (usually 2 or 0).
   */
  private final int _spotLag;
  /**
   * The holiday calendar for the first ibor leg.
   */
  private final Calendar _calendar1;
  /**
   * The holiday calendar for the second ibor leg.
   */
  private final Calendar _calendar2;

  // REVIEW: Do we need stubShort and stubFirst flags?

  /**
   * Constructor from the details. The business day conventions, end-of-month and spot lag are from the first Ibor index.
   * @param name The generator name. Not null.
   * @param iborIndex1 The Ibor index of the first leg.
   * @param compoundingPeriod1 The compounding period.
   * @param iborIndex2 The Ibor index of the second leg.
   * @param calendar1 The holiday calendar for the first ibor leg.
   * @param calendar2 The holiday calendar for the second ibor leg.
   */
  public GeneratorSwapIborCompoundingIbor(final String name, final IborIndex iborIndex1, Period compoundingPeriod1, final IborIndex iborIndex2, final Calendar calendar1, final Calendar calendar2) {
    super(name);
    ArgumentChecker.notNull(iborIndex1, "ibor index 1");
    ArgumentChecker.notNull(compoundingPeriod1, "compounding period 1");
    ArgumentChecker.notNull(iborIndex2, "ibor index 2");
    ArgumentChecker.notNull(calendar1, "calendar 1");
    ArgumentChecker.notNull(calendar2, "calendar 2");
    ArgumentChecker.isTrue(iborIndex1.getCurrency().equals(iborIndex2.getCurrency()), "Currencies of both index should be identical");
    _iborIndex1 = iborIndex1;
    _compoundingPeriod1 = compoundingPeriod1;
    _iborIndex2 = iborIndex2;
    _businessDayConvention = iborIndex1.getBusinessDayConvention();
    _endOfMonth = iborIndex1.isEndOfMonth();
    _spotLag = iborIndex1.getSpotLag();
    _calendar1 = calendar1;
    _calendar2 = calendar2;
  }

  /**
   * Constructor from the details. The business day conventions, end-of-month and spot lag are from the Ibor index.
   * @param name The generator name. Not null.
   * @param iborIndex1 The Ibor index of the first leg.
   * @param compoundingPeriod1 The compounding period.
   * @param iborIndex2 The Ibor index of the second leg.
   * @param businessDayConvention The business day convention associated to the index.
   * @param endOfMonth The end-of-month flag.
   * @param spotLag The swap spot lag (usually 2 or 0).
   * @param calendar1 The holiday calendar for the first ibor leg.
   * @param calendar2 The holiday calendar for the second ibor leg.
   */
  public GeneratorSwapIborCompoundingIbor(final String name, final IborIndex iborIndex1, Period compoundingPeriod1, final IborIndex iborIndex2, final BusinessDayConvention businessDayConvention,
      final boolean endOfMonth, final int spotLag, final Calendar calendar1, final Calendar calendar2) {
    super(name);
    ArgumentChecker.notNull(iborIndex1, "ibor index 1");
    ArgumentChecker.notNull(compoundingPeriod1, "compounding period 1");
    ArgumentChecker.notNull(iborIndex2, "ibor index 2");
    ArgumentChecker.notNull(calendar1, "calendar 1");
    ArgumentChecker.notNull(calendar2, "calendar 2");
    ArgumentChecker.isTrue(iborIndex1.getCurrency().equals(iborIndex2.getCurrency()), "Currencies of both index should be identical");
    _iborIndex1 = iborIndex1;
    _compoundingPeriod1 = compoundingPeriod1;
    _iborIndex2 = iborIndex2;
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
    _spotLag = spotLag;
    _calendar1 = calendar1;
    _calendar2 = calendar2;
  }

  /**
   * Gets the Ibor index of the first leg.
   * @return The index.
   */
  public IborIndex getIborIndex1() {
    return _iborIndex1;
  }

  /**
   * Returns the compounding period of the first leg.
   * @return The period.
   */
  public Period getCompoundingPeriod1() {
    return _compoundingPeriod1;
  }

  /**
   * Gets the Ibor index of the second leg.
   * @return The index.
   */
  public IborIndex getIborIndex2() {
    return _iborIndex2;
  }

  /**
   * Gets the swap generator business day convention.
   * @return The convention.
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the swap generator spot lag.
   * @return The lag (in days).
   */
  public int getSpotLag() {
    return _spotLag;
  }

  /**
   * Gets the swap generator end-of-month rule.
   * @return The EOM.
   */
  public Boolean isEndOfMonth() {
    return _endOfMonth;
  }

  /**
   * Gets the holiday calendar for the first leg.
   * @return The holiday calendar
   */
  public Calendar getCalendar1() {
    return _calendar1;
  }

  /**
   * Gets the holiday calendar for the second leg.
   * @return The holiday calendar
   */
  public Calendar getCalendar2() {
    return _calendar2;
  }

  @Override
  public SwapDefinition generateInstrument(final ZonedDateTime date, final double spread, final double notional, final GeneratorAttributeIR attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(attribute, "Attributes");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotLag, _calendar1);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), _iborIndex1, _calendar1);
    final ZonedDateTime maturityDate = startDate.plus(attribute.getEndPeriod());
    final AnnuityDefinition<CouponIborCompoundingFlatSpreadDefinition> leg1 = AnnuityDefinitionBuilder.couponIborCompoundingFlatSpread(startDate, maturityDate,
        _compoundingPeriod1, notional, spread, _iborIndex1, StubType.SHORT_START, true, _businessDayConvention, _endOfMonth, _calendar1, StubType.SHORT_START);
    final AnnuityDefinition<CouponIborDefinition> leg2 = AnnuityDefinitionBuilder.couponIbor(startDate, maturityDate, _iborIndex2.getTenor(), notional, _iborIndex2, false,
        _iborIndex2.getDayCount(), _businessDayConvention, _endOfMonth, _calendar2, StubType.SHORT_START, 0);
    return new SwapDefinition(leg1, leg2);
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _businessDayConvention.hashCode();
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + _iborIndex1.hashCode();
    result = prime * result + _iborIndex2.hashCode();
    result = prime * result + _spotLag;
    result = prime * result + _calendar1.hashCode();
    result = prime * result + _calendar2.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final GeneratorSwapIborCompoundingIbor other = (GeneratorSwapIborCompoundingIbor) obj;
    if (!ObjectUtils.equals(_businessDayConvention, other._businessDayConvention)) {
      return false;
    }
    if (_endOfMonth != other._endOfMonth) {
      return false;
    }
    if (!ObjectUtils.equals(_iborIndex2, other._iborIndex2)) {
      return false;
    }
    if (!ObjectUtils.equals(_iborIndex1, other._iborIndex1)) {
      return false;
    }
    if (_spotLag != other._spotLag) {
      return false;
    }
    if (!ObjectUtils.equals(_calendar1, other._calendar1)) {
      return false;
    }
    if (!ObjectUtils.equals(_calendar2, other._calendar2)) {
      return false;
    }
    return true;
  }

}
