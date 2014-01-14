/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of a bond future security (derivative version).
 */
public class BondFuturesSecurity extends FuturesSecurity {

  /**
   * The last trading time.
   */
  private final double _tradingLastTime;
  /**
   * The first notice time.
   */
  private final double _noticeFirstTime;
  /**
   * The last notice time.
   */
  private final double _noticeLastTime;
  /**
   * The first delivery time. It is the first notice date plus the settlement days.
   */
  private final double _deliveryFirstTime;
  /**
   * The last delivery time. It is the last notice date plus the settlement days.
   */
  private final double _deliveryLastTime;
  /**
   * The notional of the bond future (also called face value or contract value).
   */
  private final double _notional;
  /**
   * The basket of deliverable bonds with settlement at the futures last delivery date.
   */
  private final BondFixedSecurity[] _deliveryBasketAtDeliveryDate;
  /**
   * The basket of deliverable bonds with settlement at the standard spot date.
   */
  private final BondFixedSecurity[] _deliveryBasketAtSpotDate;
  /**
   * The conversion factor of each bond in the basket. Same size as _deliveryBasket.
   */
  private final double[] _conversionFactor;

  /**
   * Constructor from all the details.
   * @param tradingLastTime The last trading time.
   * @param noticeFirstTime The first notice time.
   * @param noticeLastTime The last notice time.
   * @param deliveryFirstTime The first delivery time.
   * @param deliveryLastTime The last delivery time.
   * @param notional The notional of the bond future.
   * @param deliveryBasketAtDeliveryDate The basket of deliverable bonds at the last delivery date.
   * @param deliveryBasketAtSpotDate The basket of deliverable bonds at the bonds standard spot date.
   * @param conversionFactor The conversion factor of each bond in the basket.
   */
  public BondFuturesSecurity(final double tradingLastTime, final double noticeFirstTime, final double noticeLastTime, final double deliveryFirstTime,
      final double deliveryLastTime, final double notional, final BondFixedSecurity[] deliveryBasketAtDeliveryDate, final BondFixedSecurity[] deliveryBasketAtSpotDate,
      final double[] conversionFactor) {
    super(tradingLastTime);
    ArgumentChecker.notNull(deliveryBasketAtDeliveryDate, "Delivery basket at delivery date");
    ArgumentChecker.notNull(deliveryBasketAtSpotDate, "Delivery basket at spot date");
    ArgumentChecker.notNull(conversionFactor, "Conversion factors");
    ArgumentChecker.isTrue(deliveryBasketAtDeliveryDate.length > 0, "At least one bond in basket");
    ArgumentChecker.isTrue(deliveryBasketAtDeliveryDate.length == conversionFactor.length, "Conversion factor size");
    ArgumentChecker.isTrue(deliveryBasketAtDeliveryDate.length == deliveryBasketAtSpotDate.length, "Delivery basket size");
    _tradingLastTime = tradingLastTime;
    _noticeFirstTime = noticeFirstTime;
    _noticeLastTime = noticeLastTime;
    _deliveryFirstTime = deliveryFirstTime;
    _deliveryLastTime = deliveryLastTime;
    _deliveryBasketAtDeliveryDate = deliveryBasketAtDeliveryDate;
    _deliveryBasketAtSpotDate = deliveryBasketAtSpotDate;
    _conversionFactor = conversionFactor;
    _notional = notional;
  }

  /**
   * Gets the last trading time.
   * @return The last trading time.
   */
  @Override
  public double getTradingLastTime() {
    return _tradingLastTime;
  }

  /**
   * Gets the first notice time.
   * @return The first notice time.
   */
  public double getNoticeFirstTime() {
    return _noticeFirstTime;
  }

  /**
   * Gets the last notice time.
   * @return The last notice time.
   */
  public double getNoticeLastTime() {
    return _noticeLastTime;
  }

  /**
   * Gets the first delivery time.
   * @return The first delivery time.
   */
  public double getDeliveryFirstTime() {
    return _deliveryFirstTime;
  }

  /**
   * Gets the last delivery time.
   * @return The last delivery time.
   */
  public double getDeliveryLastTime() {
    return _deliveryLastTime;
  }

  /**
   * Gets the notional of the bond future.
   * @return The notional of the bond future.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the basket of deliverable bonds with settlement at the futures last delivery date.
   * @return The basket.
   */
  public BondFixedSecurity[] getDeliveryBasketAtDeliveryDate() {
    return _deliveryBasketAtDeliveryDate;
  }

  /**
   * Gets the basket of deliverable bonds with settlement at the bonds standard spot date.
   * @return The basket.
   */
  public BondFixedSecurity[] getDeliveryBasketAtSpotDate() {
    return _deliveryBasketAtSpotDate;
  }

  /**
   * Gets the conversion factor of each bond in the basket.
   * @return The conversion factor of each bond in the basket.
   */
  public double[] getConversionFactor() {
    return _conversionFactor;
  }

  /**
   * Gets the future currency.
   * @return The currency.
   */
  @Override
  public Currency getCurrency() {
    return _deliveryBasketAtDeliveryDate[0].getCurrency();
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitBondFuturesSecurity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondFuturesSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_conversionFactor);
    result = prime * result + Arrays.hashCode(_deliveryBasketAtDeliveryDate);
    long temp;
    temp = Double.doubleToLongBits(_deliveryFirstTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_deliveryLastTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_noticeFirstTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_noticeLastTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_tradingLastTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final BondFuturesSecurity other = (BondFuturesSecurity) obj;
    if (!Arrays.equals(_conversionFactor, other._conversionFactor)) {
      return false;
    }
    if (!Arrays.equals(_deliveryBasketAtDeliveryDate, other._deliveryBasketAtDeliveryDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_deliveryFirstTime) != Double.doubleToLongBits(other._deliveryFirstTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_deliveryLastTime) != Double.doubleToLongBits(other._deliveryLastTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_noticeFirstTime) != Double.doubleToLongBits(other._noticeFirstTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_noticeLastTime) != Double.doubleToLongBits(other._noticeLastTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_tradingLastTime) != Double.doubleToLongBits(other._tradingLastTime)) {
      return false;
    }
    return true;
  }

}
