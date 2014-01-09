/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Abstract commodity future security definition.
 *
 * @param <T> concrete derivative class toDerivative() returns
 */
public abstract class CommodityFutureSecurityDefinition<T extends InstrumentDerivative> implements InstrumentDefinition<T> {

  /**
   * Future last trading date. Corresponds to the expiry date. Usually define using the delivery and some rules.
   * For cash-settle futures the fixing use generally the fixing of an index the day after the last trading date.  
   */
  private final ZonedDateTime _lastTradingDate;
  /**
   * The underlying commodity
   */
  private final CommodityUnderlying _underlying;

  /** 
   * Description of unit size (for example : a barrel)
   */
  private final String _unitName;

  /** 
   * Size of a unit 
   */
  private final double _unitAmount;

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
   * Settlement type - PHYSICAL or CASH
   * Some future are physical but with the option to settle it with cash, in this case the settlement type is  PHYSICAL.
   */
  private final SettlementType _settlementType;

  /** 
   * Settlement date. Usually one day after the last trading date.
   */
  private final ZonedDateTime _settlementDate;

  /**
   * Future name.
   */
  private final String _name;
  /**
   * The holiday calendar.
   */
  private final Calendar _calendar;

  /**
   * Constructor with all details.
   * @param lastTradingDate The last trading date, not null
   * @param underlying The commodity underlying, not null
   * @param unitName name of the unit of the commodity delivered, not null
   * @param unitAmount The size of a unit, not null
   * @param noticeFirstDate  The notice first date, can be null 
   * @param noticeLastDate  The notice last date, can be null 
   * @param firstDeliveryDate The first delivery date, not null for physical contract
   * @param lastDeliveryDate The last delivery date, not null for physical contract
   * @param settlementType The settlement type, CASH or PHYSICAL
   * @param settlementDate The settlement date, not null
   * @param name The name of the future, not null
   * @param calendar The holiday calendar, not null
   */
  public CommodityFutureSecurityDefinition(final ZonedDateTime lastTradingDate, final CommodityUnderlying underlying, final String unitName, final double unitAmount,
      final ZonedDateTime noticeFirstDate, final ZonedDateTime noticeLastDate, final ZonedDateTime firstDeliveryDate, final ZonedDateTime lastDeliveryDate,
      final SettlementType settlementType, final ZonedDateTime settlementDate, final String name, final Calendar calendar) {
    ArgumentChecker.notNull(lastTradingDate, "Last trading date");
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNegativeOrZero(unitAmount, "unit amount");
    ArgumentChecker.notEmpty(unitName, "unit name");
    ArgumentChecker.notNull(settlementType, "settlement type");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    if (settlementType.equals(SettlementType.PHYSICAL)) {
      ArgumentChecker.inOrderOrEqual(firstDeliveryDate, lastDeliveryDate, "first delivery date", "last delivery date");
    } else {
      ArgumentChecker.isTrue(firstDeliveryDate == null, "first delivery date must be null for non physical settlement");
      ArgumentChecker.isTrue(lastDeliveryDate == null, "last delivery date must be null for non physical settlement");
    }
    ArgumentChecker.notNull(name, "Name");
    ArgumentChecker.notNull(calendar, "calendar");
    _lastTradingDate = lastTradingDate;
    _underlying = underlying;
    _unitAmount = unitAmount;
    _noticeFirstDate = noticeFirstDate;
    _noticeLastDate = noticeLastDate;
    _firstDeliveryDate = firstDeliveryDate;
    _lastDeliveryDate = lastDeliveryDate;
    _unitName = unitName;
    _settlementType = settlementType;
    _settlementDate = settlementDate;
    _name = name;
    _calendar = calendar;
  }

  /**
   * Gets the future last trading date.
   * @return The last trading date.
   */
  public ZonedDateTime getLastTradingDate() {
    return _lastTradingDate;
  }

  /**
   * Gets the commodity underlying.
   * @return commodity underlying.
   */
  public CommodityUnderlying getUnderlying() {
    return _underlying;
  }

  /**
   * @return the _unitName
   */
  public String getUnitName() {
    return _unitName;
  }

  /**
   * @return the _unitAmount
   */
  public double getUnitAmount() {
    return _unitAmount;
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

  /**
   * @return the _settlementType
   */
  public SettlementType getSettlementType() {
    return _settlementType;
  }

  /**
   * @return the _settlementDate
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Gets the future name.
   * @return The name
   */
  public String getName() {
    return _name;
  }

  /**
   * The future currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlying.getCurrency();
  }

  /**
   * Gets the holiday calendar.
   * @return The holiday calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_firstDeliveryDate == null) ? 0 : _firstDeliveryDate.hashCode());
    result = prime * result + ((_lastDeliveryDate == null) ? 0 : _lastDeliveryDate.hashCode());
    result = prime * result + ((_lastTradingDate == null) ? 0 : _lastTradingDate.hashCode());
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    result = prime * result + ((_noticeFirstDate == null) ? 0 : _noticeFirstDate.hashCode());
    result = prime * result + ((_noticeLastDate == null) ? 0 : _noticeLastDate.hashCode());
    result = prime * result + ((_settlementDate == null) ? 0 : _settlementDate.hashCode());
    result = prime * result + ((_settlementType == null) ? 0 : _settlementType.hashCode());
    result = prime * result + ((_underlying == null) ? 0 : _underlying.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_unitAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_unitName == null) ? 0 : _unitName.hashCode());
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CommodityFutureSecurityDefinition<?> other = (CommodityFutureSecurityDefinition<?>) obj;
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
    if (_lastTradingDate == null) {
      if (other._lastTradingDate != null) {
        return false;
      }
    } else if (!_lastTradingDate.equals(other._lastTradingDate)) {
      return false;
    }
    if (_name == null) {
      if (other._name != null) {
        return false;
      }
    } else if (!_name.equals(other._name)) {
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
    if (_settlementDate == null) {
      if (other._settlementDate != null) {
        return false;
      }
    } else if (!_settlementDate.equals(other._settlementDate)) {
      return false;
    }
    if (_settlementType != other._settlementType) {
      return false;
    }
    if (_underlying == null) {
      if (other._underlying != null) {
        return false;
      }
    } else if (!_underlying.equals(other._underlying)) {
      return false;
    }
    if (Double.doubleToLongBits(_unitAmount) != Double.doubleToLongBits(other._unitAmount)) {
      return false;
    }
    if (_unitName == null) {
      if (other._unitName != null) {
        return false;
      }
    } else if (!_unitName.equals(other._unitName)) {
      return false;
    }
    return true;
  }

}
