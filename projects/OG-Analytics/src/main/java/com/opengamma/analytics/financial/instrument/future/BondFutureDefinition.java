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
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of a bond future (definition version).
 * @deprecated Deprecated since 2.2.0.M4. Use the {@link BondFuturesSecurityDefinition} and {@link BondFuturesTransactionDefinition}.
 */
@Deprecated
public class BondFutureDefinition implements InstrumentDefinitionWithData<BondFuture, Double> {

  /**
   * The last trading date.
   */
  private final ZonedDateTime _tradingLastDate;
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
   * Constructor from the trading and notice dates and the basket.
   * @param tradingLastDate The last trading date.
   * @param noticeFirstDate The first notice date.
   * @param noticeLastDate The last notice date.
   * @param notional The bond future notional.
   * @param deliveryBasket The basket of deliverable bonds.
   * @param conversionFactor The conversion factor of each bond in the basket.
   */
  public BondFutureDefinition(final ZonedDateTime tradingLastDate, final ZonedDateTime noticeFirstDate, final ZonedDateTime noticeLastDate, final double notional,
      final BondFixedSecurityDefinition[] deliveryBasket, final double[] conversionFactor) {
    ArgumentChecker.notNull(tradingLastDate, "Last trading date");
    ArgumentChecker.notNull(noticeFirstDate, "First notice date");
    ArgumentChecker.notNull(noticeLastDate, "Last notice date");
    ArgumentChecker.notNull(deliveryBasket, "Delivery basket");
    ArgumentChecker.notNull(conversionFactor, "Conversion factor");
    ArgumentChecker.isTrue(deliveryBasket.length > 0, "At least one bond in basket");
    ArgumentChecker.isTrue(deliveryBasket.length == conversionFactor.length, "Conversion factor size");
    _tradingLastDate = tradingLastDate;
    _noticeFirstDate = noticeFirstDate;
    _noticeLastDate = noticeLastDate;
    _notional = notional;
    _deliveryBasket = deliveryBasket;
    _conversionFactor = conversionFactor;
    _settlementDays = _deliveryBasket[0].getSettlementDays();
    final Calendar calendar = _deliveryBasket[0].getCalendar();
    _deliveryFirstDate = ScheduleCalculator.getAdjustedDate(_noticeFirstDate, _settlementDays, calendar);
    _deliveryLastDate = ScheduleCalculator.getAdjustedDate(_noticeLastDate, _settlementDays, calendar);
  }

  /**
   * Gets the last trading date.
   * @return The last trading date.
   */
  public ZonedDateTime getTradingLastDate() {
    return _tradingLastDate;
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
   * The future currency.
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
  public BondFuture toDerivative(final ZonedDateTime valDate, final Double referencePrice, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BondFuture toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of " + getClass().getSimpleName()
        + " does not support the two argument method (without margin price data).");
  }

  @Override
  public BondFuture toDerivative(final ZonedDateTime valDate, final Double referencePrice) {
    ArgumentChecker.notNull(valDate, "valDate must always be provided to form a Derivative from a Definition");
    ArgumentChecker.isTrue(!valDate.isAfter(getDeliveryLastDate()), "Valuation date is after last delivery date");

    final double lastTradingTime = TimeCalculator.getTimeBetween(valDate, getTradingLastDate());
    final double firstNoticeTime = TimeCalculator.getTimeBetween(valDate, getNoticeFirstDate());
    final double lastNoticeTime = TimeCalculator.getTimeBetween(valDate, getNoticeLastDate());
    final double firstDeliveryTime = TimeCalculator.getTimeBetween(valDate, getDeliveryFirstDate());
    final double lastDeliveryTime = TimeCalculator.getTimeBetween(valDate, getDeliveryLastDate());

    final BondFixedSecurity[] basket = new BondFixedSecurity[_deliveryBasket.length];
    for (int loopbasket = 0; loopbasket < _deliveryBasket.length; loopbasket++) {
      basket[loopbasket] = _deliveryBasket[loopbasket].toDerivative(valDate, _deliveryLastDate);
    }

    final BondFuture futureDeriv = new BondFuture(lastTradingTime, firstNoticeTime, lastNoticeTime, firstDeliveryTime, lastDeliveryTime, _notional, basket,
        _conversionFactor, referencePrice);
    return futureDeriv;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFutureDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFutureDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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
    result = prime * result + _tradingLastDate.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BondFutureDefinition other = (BondFutureDefinition) obj;
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
    if (!ObjectUtils.equals(_tradingLastDate, other._tradingLastDate)) {
      return false;
    }
    return true;
  }

}
