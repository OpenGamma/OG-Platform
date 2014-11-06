/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityPhysicalSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CouponCommodityPhysicalSettleDefinition extends CouponCommodityDefinition {

  /**
   * The first notice date.
   * Some future contracts have no notice date. The seller of the future have to notice the delivery but without an explicit notice date.
   * In this case the notice date can be null
   */
  private final ZonedDateTime _noticeFirstDate;
  /**
   * The last notice date.
   * Some future contracts have no notice date. The seller of the future have to notice the delivery but without an explicit notice date.
   * In this case the notice date can be null
   */
  private final ZonedDateTime _noticeLastDate;

  /** 
   * Date of first delivery -  for PHYSICAL settlement only
   * The first delivery date is the first business day of this month.  
   */
  private final ZonedDateTime _firstDeliveryDate;

  /** 
   * Date of last delivery - for PHYSICAL settlement only
   * The delivery is done during a month, the first delivery date is the first business day of this month. 
   */
  private final ZonedDateTime _lastDeliveryDate;

  /**
   * Constructor with all details.
   * @param paymentYearFraction The last trading date, not null
   * @param underlying The commodity underlying, not null
   * @param unitName name of the unit of the commodity delivered, not null
   * @param notional notional
   * @param settlementDate The settlement date, not null
   * @param calendar The holiday calendar, not null
   * @param noticeFirstDate  The notice first date, can be null 
   * @param noticeLastDate  The notice last date, can be null 
   * @param firstDeliveryDate The first delivery date, not null for physical contract
   * @param lastDeliveryDate The last delivery date, not null for physical contract
   */
  public CouponCommodityPhysicalSettleDefinition(final double paymentYearFraction, final CommodityUnderlying underlying, final String unitName, final double notional,
      final ZonedDateTime settlementDate, final Calendar calendar, final ZonedDateTime noticeFirstDate, final ZonedDateTime noticeLastDate, final ZonedDateTime firstDeliveryDate,
      final ZonedDateTime lastDeliveryDate) {
    super(paymentYearFraction, underlying, unitName, notional, settlementDate, calendar);
    _noticeFirstDate = noticeFirstDate;
    _noticeLastDate = noticeLastDate;
    _firstDeliveryDate = firstDeliveryDate;
    _lastDeliveryDate = lastDeliveryDate;
  }

  /**
   * @return the _noticeFirstDate
   */
  public ZonedDateTime getNoticeFirstDate() {
    return _noticeFirstDate;
  }

  /**
   * @return the _noticeLastDate
   */
  public ZonedDateTime getNoticeLastDate() {
    return _noticeLastDate;
  }

  /**
   * @return the _firstDeliveryDate
   */
  public ZonedDateTime getFirstDeliveryDate() {
    return _firstDeliveryDate;
  }

  /**
   * @return the _lastDeliveryDate
   */
  public ZonedDateTime getLastDeliveryDate() {
    return _lastDeliveryDate;
  }

  @Override
  public double getReferenceAmount() {
    return getNotional();
  }

  @Override
  public CouponCommodity toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public CouponCommodity toDerivative(final ZonedDateTime date) {
    ArgumentChecker.inOrderOrEqual(date, getSettlementDate(), "date", "expiry date");
    final double settlementTime = TimeCalculator.getTimeBetween(date, getSettlementDate());
    final double noticeFirstTime = TimeCalculator.getTimeBetween(date, _noticeFirstDate);
    final double noticeLastTime = TimeCalculator.getTimeBetween(date, _noticeLastDate);
    final double firstDeliveryTime = TimeCalculator.getTimeBetween(date, _firstDeliveryDate);
    final double lastDeliveryTime = TimeCalculator.getTimeBetween(date, _lastDeliveryDate);
    return new CouponCommodityPhysicalSettle(getPaymentYearFractione(), getUnderlying(), getUnitName(), getNotional(), settlementTime, getCalendar(), noticeFirstTime, noticeLastTime,
        firstDeliveryTime, lastDeliveryTime);
  }

  public Payment toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries, final String... yieldCurveNames) {
    return toDerivative(date, priceIndexTimeSeries);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponCommodityPhysicalSettleDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponCommodityPhysicalSettleDefinition(this);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_firstDeliveryDate == null) ? 0 : _firstDeliveryDate.hashCode());
    result = prime * result + ((_lastDeliveryDate == null) ? 0 : _lastDeliveryDate.hashCode());
    result = prime * result + ((_noticeFirstDate == null) ? 0 : _noticeFirstDate.hashCode());
    result = prime * result + ((_noticeLastDate == null) ? 0 : _noticeLastDate.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    final CouponCommodityPhysicalSettleDefinition other = (CouponCommodityPhysicalSettleDefinition) obj;
    if (_firstDeliveryDate == null) {
      if (other._firstDeliveryDate != null) {
        return false;
      }
    } else if (!_firstDeliveryDate.equals(other._firstDeliveryDate)) {
      return false;
    }
    if (_lastDeliveryDate == null) {
      if (other._lastDeliveryDate != null) {
        return false;
      }
    } else if (!_lastDeliveryDate.equals(other._lastDeliveryDate)) {
      return false;
    }
    if (_noticeFirstDate == null) {
      if (other._noticeFirstDate != null) {
        return false;
      }
    } else if (!_noticeFirstDate.equals(other._noticeFirstDate)) {
      return false;
    }
    if (_noticeLastDate == null) {
      if (other._noticeLastDate != null) {
        return false;
      }
    } else if (!_noticeLastDate.equals(other._noticeLastDate)) {
      return false;
    }
    return true;
  }

}
