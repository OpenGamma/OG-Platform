/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative;

import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Abstract commodity future security.
 */
public abstract class CommodityFutureSecurity implements InstrumentDerivative {

  /**
   * Future last trading time. Corresponds to the expiry time.  
   */
  private final double _lastTradingTime;
  /**
   * The underlying commodity
   */
  private final CommodityUnderlying _underlying;

  /** 
   * Description of unit size (for example :  barrel)
   */
  private final String _unitName;

  /** 
   * Size of a unit 
   */
  private final double _unitAmount;

  /**
   * The first notice time.
   * Some future contracts have no notice date. The seller of the future have to notice the delivery but without an explicit notice date.
   * In this case the notice time can be null
   */
  private final double _noticeFirstTime;
  /**
   * The last notice time.
   * Some future contracts have no notice date. The seller of the future have to notice the delivery but without an explicit notice date.
   * In this case the notice time can be null
   */
  private final double _noticeLastTime;

  /** 
   * time of first delivery - for PHYSICAL settlement only
   *   
   */
  private final double _firstDeliveryTime;

  /** 
   * Date of last delivery - for PHYSICAL settlement only
   * The delivery is done during a month, the first delivery date is the first business day of this month. 
   */
  private final double _lastDeliveryTime;

  /** 
   * Settlement type - PHYSICAL or CASH
   * Some future are physical but with the option to settle it with cash, in this case the settlement type is  PHYSICAL.
   */
  private final SettlementType _settlementType;

  /** 
   * Settlement time. 
   */
  private final double _settlementTime;

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
   * @param lastTradingTime The last trading date, not null
   * @param underlying The commodity underlying, not null
   * @param unitName name of the unit of the commodity delivered, not null
   * @param unitAmount The size of a unit, not null
   * @param noticeFirstTime  The notice first date, can be null 
   * @param noticeLastTime  The notice last date, can be null 
   * @param firstDeliveryTime The first delivery date, not null for physical contract
   * @param lastDeliveryTime The last delivery date, not null for physical contract
   * @param settlementType The settlement type, CASH or PHYSICAL
   * @param settlementTime The settlement date, not null
   * @param name The name of the future, not null
   * @param calendar The holiday calendar, not null
   */
  public CommodityFutureSecurity(final double lastTradingTime, final CommodityUnderlying underlying, final String unitName, final double unitAmount,
      final double noticeFirstTime, final double noticeLastTime, final double firstDeliveryTime, final double lastDeliveryTime,
      final SettlementType settlementType, final double settlementTime, final String name, final Calendar calendar) {
    ArgumentChecker.isTrue(lastTradingTime >= 0, "last trading time must be positive");
    ArgumentChecker.isTrue(settlementTime >= 0, "settlement time must be positive");
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNegativeOrZero(unitAmount, "unit amount");
    ArgumentChecker.notEmpty(unitName, "unit name");
    ArgumentChecker.notNull(settlementType, "settlement type");
    ArgumentChecker.notNull(name, "Name");
    ArgumentChecker.notNull(calendar, "calendar");
    _lastTradingTime = lastTradingTime;
    _underlying = underlying;
    _unitAmount = unitAmount;
    _noticeFirstTime = noticeFirstTime;
    _noticeLastTime = noticeLastTime;
    _firstDeliveryTime = firstDeliveryTime;
    _lastDeliveryTime = lastDeliveryTime;
    _unitName = unitName;
    _settlementType = settlementType;
    _settlementTime = settlementTime;
    _name = name;
    _calendar = calendar;
  }

  /**
   * @return the _lastTradingTime
   */
  public double getLastTradingTime() {
    return _lastTradingTime;
  }

  /**
   * @return the _underlying
   */
  public CommodityUnderlying getUnderlying() {
    return _underlying;
  }

  /**
   * @return the _underlying
   */
  public Currency getCurrency() {
    return _underlying.getCurrency();
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
   * @return the _noticeFirstTime
   */
  public double getNoticeFirstTime() {
    return _noticeFirstTime;
  }

  /**
   * @return the _noticeLastTime
   */
  public double getNoticeLastTime() {
    return _noticeLastTime;
  }

  /**
   * @return the _firstDeliveryTime
   */
  public double getFirstDeliveryTime() {
    return _firstDeliveryTime;
  }

  /**
   * @return the _lastDeliveryTime
   */
  public double getLastDeliveryTime() {
    return _lastDeliveryTime;
  }

  /**
   * @return the _settlementType
   */
  public SettlementType getSettlementType() {
    return _settlementType;
  }

  /**
   * @return the _settlementTime
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  /**
   * @return the _name
   */
  public String getName() {
    return _name;
  }

  /**
   * @return the _calendar
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
    long temp;
    temp = Double.doubleToLongBits(_firstDeliveryTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_lastDeliveryTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_lastTradingTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    temp = Double.doubleToLongBits(_noticeFirstTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_noticeLastTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_settlementTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_settlementType == null) ? 0 : _settlementType.hashCode());
    result = prime * result + ((_underlying == null) ? 0 : _underlying.hashCode());
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
    final CommodityFutureSecurity other = (CommodityFutureSecurity) obj;
    if (Double.doubleToLongBits(_firstDeliveryTime) != Double.doubleToLongBits(other._firstDeliveryTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_lastDeliveryTime) != Double.doubleToLongBits(other._lastDeliveryTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_lastTradingTime) != Double.doubleToLongBits(other._lastTradingTime)) {
      return false;
    }
    if (_name == null) {
      if (other._name != null) {
        return false;
      }
    } else if (!_name.equals(other._name)) {
      return false;
    }
    if (Double.doubleToLongBits(_noticeFirstTime) != Double.doubleToLongBits(other._noticeFirstTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_noticeLastTime) != Double.doubleToLongBits(other._noticeLastTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_settlementTime) != Double.doubleToLongBits(other._settlementTime)) {
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
