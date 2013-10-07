/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.fastcalibration;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDAPremiumLegSchedule;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CDSDescription {

  private final LocalDate _tradeDate;
  private final LocalDate _stepinDate;
  private final LocalDate _valueDate;
  private final LocalDate _startDate;
  private final LocalDate _effectiveStartDate;
  private final LocalDate _endDate;
  private final ISDAPremiumLegSchedule _premLeg;
  private final boolean _payAccOnDefault;
  private final boolean _protectionFromStartOfDay;
  private final DayCount _accrualDayCount;

  public CDSDescription(final LocalDate tradeDate, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate endDate, final boolean payAccOnDefault,
      final Period paymentInterval, final StubType stubType, final boolean protectStart, final BusinessDayConvention businessdayAdjustmentConvention, final Calendar calendar,
      final DayCount accrualDayCount) {

    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(stepinDate, "stepinDate");
    ArgumentChecker.notNull(valueDate, "valueDate");
    ArgumentChecker.notNull(startDate, "startDate");
    ArgumentChecker.notNull(endDate, "endDate");
    ArgumentChecker.notNull(paymentInterval, "tenor");
    ArgumentChecker.notNull(stubType, "stubType");
    ArgumentChecker.notNull(businessdayAdjustmentConvention, "businessdayAdjustmentConvention");
    ArgumentChecker.notNull(accrualDayCount, "accuralDayCount");
    ArgumentChecker.isFalse(valueDate.isBefore(tradeDate), "Require valueDate >= today");
    ArgumentChecker.isFalse(stepinDate.isBefore(tradeDate), "Require stepin >= today");
    ArgumentChecker.isFalse(tradeDate.isAfter(endDate), "CDS has expired");

    _tradeDate = tradeDate;
    _stepinDate = stepinDate;
    _valueDate = valueDate;
    _startDate = startDate;
    _endDate = endDate;

    final ISDAPremiumLegSchedule fullPaymentSchedule = new ISDAPremiumLegSchedule(startDate, endDate, paymentInterval, stubType, businessdayAdjustmentConvention, calendar, protectStart);
    _premLeg = ISDAPremiumLegSchedule.truncateSchedule(stepinDate, fullPaymentSchedule);

    _payAccOnDefault = payAccOnDefault;
    _protectionFromStartOfDay = protectStart;
    _accrualDayCount = accrualDayCount;

    final LocalDate temp = stepinDate.isAfter(startDate) ? stepinDate : startDate;
    _effectiveStartDate = protectStart ? temp.minusDays(1) : temp;
  }

  /**
   * Gets the tradeDate.
   * @return the tradeDate
   */
  public LocalDate getTradeDate() {
    return _tradeDate;
  }

  /**
   * Gets the stepinDate.
   * @return the stepinDate
   */
  public LocalDate getStepinDate() {
    return _stepinDate;
  }

  /**
   * Gets the valueDate.
   * @return the valueDate
   */
  public LocalDate getValueDate() {
    return _valueDate;
  }

  /**
   * Gets the startDate.
   * @return the startDate
   */
  public LocalDate getStartDate() {
    return _startDate;
  }

  /**
   * Gets the effectiveStartDate.
   * @return the effectiveStartDate
   */
  public LocalDate getEffectiveStartDate() {
    return _effectiveStartDate;
  }

  /**
   * Gets the endDate.
   * @return the endDate
   */
  public LocalDate getEndDate() {
    return _endDate;
  }

  /**
   * Gets the premLeg.
   * @return the premLeg
   */
  public ISDAPremiumLegSchedule getPremLeg() {
    return _premLeg;
  }

  /**
   * Gets the payAccOnDefault.
   * @return the payAccOnDefault
   */
  public boolean isPayAccOnDefault() {
    return _payAccOnDefault;
  }

  /**
   * Gets the protectionFromStartOfDay.
   * @return the protectionFromStartOfDay
   */
  public boolean isProtectionFromStartOfDay() {
    return _protectionFromStartOfDay;
  }

  /**
   * Gets the accrualDayCount.
   * @return the accrualDayCount
   */
  public DayCount getAccrualDayCount() {
    return _accrualDayCount;
  }

}
