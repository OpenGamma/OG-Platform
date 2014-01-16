/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of a bond future security (definition version).
 */
public class BondFuturesSecurityDefinition extends FuturesSecurityDefinition<BondFuturesSecurity> {

  /**
   * The first notice date.
   */
  private final ZonedDateTime _noticeFirstDate;
  /**
   * The last notice date.
   */
  private final ZonedDateTime _noticeLastDate;
  /**
   * The first delivery date. It is the first notice date plus the settlement days.
   */
  private final ZonedDateTime _deliveryFirstDate;
  /**
   * The last delivery date. It is the last notice date plus the settlement days.
   */
  private final ZonedDateTime _deliveryLastDate;
  /**
   * The number of days between notice date and delivery date.
   */
  private final int _settlementDays;
  /**
   * The basket of deliverable bonds.
   */
  private final BondFixedSecurityDefinition[] _deliveryBasket;
  /**
   * The conversion factor of each bond in the basket.
   */
  private final double[] _conversionFactor;
  /**
   * The notional of the bond future (also called face value or contract value).
   */
  private final double _notional;
  /**
   * The holiday calendar.
   */
  private final Calendar _calendar;

  /**
   * Constructor from the trading and notice dates and the basket.
   * @param tradingLastDate The last trading date.
   * @param noticeFirstDate The first notice date.
   * @param noticeLastDate The last notice date.
   * @param notional The bond future notional.
   * @param deliveryBasket The basket of deliverable bonds.
   * @param conversionFactor The conversion factor of each bond in the basket.
   */
  public BondFuturesSecurityDefinition(final ZonedDateTime tradingLastDate, final ZonedDateTime noticeFirstDate, final ZonedDateTime noticeLastDate, final double notional,
      final BondFixedSecurityDefinition[] deliveryBasket, final double[] conversionFactor) {
    super(tradingLastDate);
    ArgumentChecker.notNull(noticeFirstDate, "First notice date");
    ArgumentChecker.notNull(noticeLastDate, "Last notice date");
    ArgumentChecker.notNull(deliveryBasket, "Delivery basket");
    ArgumentChecker.notNull(conversionFactor, "Conversion factor");
    ArgumentChecker.isTrue(deliveryBasket.length > 0, "At least one bond in basket");
    ArgumentChecker.isTrue(deliveryBasket.length == conversionFactor.length, "Conversion factor size");
    _noticeFirstDate = noticeFirstDate;
    _noticeLastDate = noticeLastDate;
    _notional = notional;
    _deliveryBasket = deliveryBasket;
    _conversionFactor = conversionFactor;
    _settlementDays = _deliveryBasket[0].getSettlementDays();
    _calendar = _deliveryBasket[0].getCalendar();
    _deliveryFirstDate = ScheduleCalculator.getAdjustedDate(_noticeFirstDate, _settlementDays, _calendar);
    _deliveryLastDate = ScheduleCalculator.getAdjustedDate(_noticeLastDate, _settlementDays, _calendar);
  }

  /**
   * Gets the first notice date.
   * @return The first notice date.
   */
  public ZonedDateTime getNoticeFirstDate() {
    return _noticeFirstDate;
  }

  /**
   * Gets the last notice date.
   * @return The last notice date.
   */
  public ZonedDateTime getNoticeLastDate() {
    return _noticeLastDate;
  }

  /**
   * Gets the first delivery date. It is the first notice date plus the settlement days.
   * @return The first delivery date.
   */
  public ZonedDateTime getDeliveryFirstDate() {
    return _deliveryFirstDate;
  }

  /**
   * Gets the last delivery date. It is the last notice date plus the settlement days.
   * @return The last delivery date.
   */
  public ZonedDateTime getDeliveryLastDate() {
    return _deliveryLastDate;
  }

  /**
   * Gets the number of days between notice date and delivery date.
   * @return The number of days between notice date and delivery date.
   */
  public int getSettlementDays() {
    return _settlementDays;
  }

  /**
   * Gets the notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the basket of deliverable bonds.
   * @return The basket of deliverable bonds.
   */
  public BondFixedSecurityDefinition[] getDeliveryBasket() {
    return _deliveryBasket;
  }

  /**
   * Gets the conversion factor of each bond in the basket.
   * @return The conversion factors.
   */
  public double[] getConversionFactor() {
    return _conversionFactor;
  }

  /**
   * Gets the holiday calendar.
   * @return The holiday calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  /**
   * Returns the futures' currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _deliveryBasket[0].getCurrency();
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondFuturesSecurity toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    ArgumentChecker.isTrue(!date.isAfter(getNoticeLastDate()), "Date is after last notice date");
    final double lastTradingTime = TimeCalculator.getTimeBetween(date, getLastTradingDate());
    final double firstNoticeTime = TimeCalculator.getTimeBetween(date, getNoticeFirstDate());
    final double lastNoticeTime = TimeCalculator.getTimeBetween(date, getNoticeLastDate());
    final double firstDeliveryTime = TimeCalculator.getTimeBetween(date, getDeliveryFirstDate());
    final double lastDeliveryTime = TimeCalculator.getTimeBetween(date, getDeliveryLastDate());
    final BondFixedSecurity[] basketAtDeliveryDate = new BondFixedSecurity[_deliveryBasket.length];
    final BondFixedSecurity[] basketAtSpotDate = new BondFixedSecurity[_deliveryBasket.length];
    for (int loopbasket = 0; loopbasket < _deliveryBasket.length; loopbasket++) {
      basketAtDeliveryDate[loopbasket] = _deliveryBasket[loopbasket].toDerivative(date, _deliveryLastDate, yieldCurveNames);
      basketAtSpotDate[loopbasket] = _deliveryBasket[loopbasket].toDerivative(date, yieldCurveNames);
    }
    return new BondFuturesSecurity(lastTradingTime, firstNoticeTime, lastNoticeTime, firstDeliveryTime, lastDeliveryTime, _notional,
        basketAtDeliveryDate, basketAtSpotDate, _conversionFactor);
  }

  @Override
  public BondFuturesSecurity toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getNoticeLastDate()), "Date is after last notice date");
    final double lastTradingTime = TimeCalculator.getTimeBetween(date, getLastTradingDate());
    final double firstNoticeTime = TimeCalculator.getTimeBetween(date, getNoticeFirstDate());
    final double lastNoticeTime = TimeCalculator.getTimeBetween(date, getNoticeLastDate());
    final double firstDeliveryTime = TimeCalculator.getTimeBetween(date, getDeliveryFirstDate());
    final double lastDeliveryTime = TimeCalculator.getTimeBetween(date, getDeliveryLastDate());
    final BondFixedSecurity[] basketAtDeliveryDate = new BondFixedSecurity[_deliveryBasket.length];
    final BondFixedSecurity[] basketAtSpotDate = new BondFixedSecurity[_deliveryBasket.length];
    for (int loopbasket = 0; loopbasket < _deliveryBasket.length; loopbasket++) {
      basketAtDeliveryDate[loopbasket] = _deliveryBasket[loopbasket].toDerivative(date, _deliveryLastDate);
      basketAtSpotDate[loopbasket] = _deliveryBasket[loopbasket].toDerivative(date);
    }
    return new BondFuturesSecurity(lastTradingTime, firstNoticeTime, lastNoticeTime, firstDeliveryTime, lastDeliveryTime, _notional,
        basketAtDeliveryDate, basketAtSpotDate, _conversionFactor);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitBondFuturesSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondFuturesSecurityDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_conversionFactor);
    result = prime * result + Arrays.hashCode(_deliveryBasket);
    result = prime * result + _deliveryFirstDate.hashCode();
    result = prime * result + _deliveryLastDate.hashCode();
    result = prime * result + _noticeFirstDate.hashCode();
    result = prime * result + _noticeLastDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _settlementDays;
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BondFuturesSecurityDefinition other = (BondFuturesSecurityDefinition) obj;
    if (!Arrays.equals(_conversionFactor, other._conversionFactor)) {
      return false;
    }
    if (!Arrays.equals(_deliveryBasket, other._deliveryBasket)) {
      return false;
    }
    if (!ObjectUtils.equals(_noticeFirstDate, other._noticeFirstDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_noticeLastDate, other._noticeLastDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    return true;
  }

}
