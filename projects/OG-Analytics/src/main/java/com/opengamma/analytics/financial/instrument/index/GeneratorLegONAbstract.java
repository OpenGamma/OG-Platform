/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.Period;

import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Generator (or template) for leg paying overnight rate (plus a spread).
 */
public abstract class GeneratorLegONAbstract extends GeneratorLeg {

  /** The ON index on which the fixing is done. */
  private final IndexON _indexON;
  /** The period between two payments. */
  private final Period _paymentPeriod;
  /** The offset in business days between trade and settlement date (usually 2 or 0). */
  private final int _spotOffset;
  /** The offset in days between the last ON fixing date and the coupon payment. */
  private final int _paymentOffset;
  /** The business day convention for the payments. */
  private final BusinessDayConvention _businessDayConvention;
  /** The flag indicating if the end-of-month rule is used. */
  private final boolean _endOfMonth;
  /** The stub type. */
  private final StubType _stubType;
  /** Whether the notional exchanged (at start and at end). */
  private final boolean _isExchangeNotional;
  /** The calendar associated with the overnight index. */
  private final Calendar _indexCalendar;
  /** The calendar used for the payments. */
  private final Calendar _paymentCalendar;

  /**
   * Constructor from all the details.
   * @param name The generator name.
   * @param ccy The leg currency.
   * @param indexON The overnight index underlying the leg.
   * @param paymentPeriod The period between two payments.
   * @param spotOffset The offset in business days between trade and settlement date (usually 2 or 0).
   * @param paymentOffset The offset in business days between the end of the accrual period and the coupon payment.
   * @param businessDayConvention The business day convention for the payments.
   * @param endOfMonth The flag indicating if the end-of-month rule is used.
   * @param stubType The stub type.
   * @param isExchangeNotional Whether the notional exchanged (at start and at end).
   * @param indexCalendar The calendar associated with the overnight index.
   * @param paymentCalendar The calendar used for the payments.
   */
  public GeneratorLegONAbstract(String name, Currency ccy, IndexON indexON, Period paymentPeriod, int spotOffset, int paymentOffset,
      BusinessDayConvention businessDayConvention, boolean endOfMonth, StubType stubType, boolean isExchangeNotional,
      Calendar indexCalendar, Calendar paymentCalendar) {
    super(name, ccy);
    ArgumentChecker.notNull(indexON, "Index ON");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(stubType, "Stub type");
    ArgumentChecker.notNull(indexCalendar, "Index calendar");
    ArgumentChecker.notNull(paymentCalendar, "payment calendar");
    _indexON = indexON;
    _paymentPeriod = paymentPeriod;
    _spotOffset = spotOffset;
    _paymentOffset = paymentOffset;
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
    _stubType = stubType;
    _isExchangeNotional = isExchangeNotional;
    _indexCalendar = indexCalendar;
    _paymentCalendar = paymentCalendar;
  }

  /**
   * Gets the indexON.
   * @return the indexON
   */
  public IndexON getIndexON() {
    return _indexON;
  }

  /**
   * Gets the paymentPeriod.
   * @return the paymentPeriod
   */
  public Period getPaymentPeriod() {
    return _paymentPeriod;
  }

  /**
   * Gets the spotOffset.
   * @return the spotOffset
   */
  public int getSpotOffset() {
    return _spotOffset;
  }

  /**
   * Gets the paymentOffset.
   * @return the paymentOffset
   */
  public int getPaymentOffset() {
    return _paymentOffset;
  }

  /**
   * Gets the businessDayConvention.
   * @return the businessDayConvention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the endOfMonth.
   * @return the endOfMonth
   */
  public boolean isEndOfMonth() {
    return _endOfMonth;
  }

  /**
   * Gets the stubType.
   * @return the stubType
   */
  public StubType getStubType() {
    return _stubType;
  }

  /**
   * Gets the isExchangeNotional.
   * @return the isExchangeNotional
   */
  public boolean isExchangeNotional() {
    return _isExchangeNotional;
  }

  /**
   * Gets the indexCalendar.
   * @return the indexCalendar
   */
  public Calendar getIndexCalendar() {
    return _indexCalendar;
  }

  /**
   * Gets the paymentCalendar.
   * @return the paymentCalendar
   */
  public Calendar getPaymentCalendar() {
    return _paymentCalendar;
  }

}
