/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Description of a bond future security with cash settlement against a price deduced from a yield average. 
 * In particular used for AUD-SFE bond futures.
 * <P>Reference: Add a reference.
 */
public class BondFuturesYieldAverageSecurity extends FuturesSecurity {

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
   * Theoretical coupon rate. The coupon of the synthetic bond used to compute the settlement price from the yield.
   */
  private final double _couponRate;
  /**
   * Underlying tenor in year. The tenor of the synthetic bond used to compute the settlement price from the yield.
   */
  private final int _tenor;

  /**
   * Constructor from all the details.
   * @param tradingLastTime The last trading time.
   * @param deliveryBasketAtDeliveryDate The basket of deliverable bonds at the last delivery date.
   * @param deliveryBasketAtSpotDate The basket of deliverable bonds at the bonds standard spot date.
   * @param couponRate The coupon rate of the synthetic bond used to compute the settlement price from the yield.
   * @param tenor The underlying synthetic bond tenor (in years).
   * @param notional The notional of the bond future.
   */
  public BondFuturesYieldAverageSecurity(final double tradingLastTime, final BondFixedSecurity[] deliveryBasketAtDeliveryDate, 
      final BondFixedSecurity[] deliveryBasketAtSpotDate, final double couponRate, final int tenor, final double notional) {
    super(tradingLastTime);
    ArgumentChecker.notNull(deliveryBasketAtDeliveryDate, "Delivery basket at delivery date");
    ArgumentChecker.notNull(deliveryBasketAtSpotDate, "Delivery basket at spot date");
    ArgumentChecker.isTrue(deliveryBasketAtDeliveryDate.length > 0, "At least one bond in basket");
    ArgumentChecker.isTrue(deliveryBasketAtDeliveryDate.length == deliveryBasketAtSpotDate.length, "Delivery basket size");
    _deliveryBasketAtDeliveryDate = deliveryBasketAtDeliveryDate;
    _deliveryBasketAtSpotDate = deliveryBasketAtSpotDate;
    _couponRate = couponRate;
    _tenor = tenor;
    _notional = notional;
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
   * Returns the coupon rate of the synthetic bond used to compute the settlement price from the yield.
   * @return The rate.
   */
  public double getCouponRate() {
    return _couponRate;
  }

  /**
   * Returns The tenor of the synthetic bond used to compute the settlement price from the yield (in year).
   * @return The tenor.
   */
  public int getTenor() {
    return _tenor;
  }


  @Override
  public Currency getCurrency() {
    return _deliveryBasketAtDeliveryDate[0].getCurrency();
  }
  
  /**
   * Returns the number of coupon per year for the first bond in the basket.
   * @return The number of coupon per year.
   */
  public int getNumberCouponPerYear() {
    return _deliveryBasketAtDeliveryDate[0].getCouponPerYear();
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitBondFuturesYieldAverageSecurity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondFuturesYieldAverageSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_couponRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_deliveryBasketAtDeliveryDate);
    result = prime * result + Arrays.hashCode(_deliveryBasketAtSpotDate);
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _tenor;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BondFuturesYieldAverageSecurity other = (BondFuturesYieldAverageSecurity) obj;
    if (Double.doubleToLongBits(_couponRate) != Double.doubleToLongBits(other._couponRate)) {
      return false;
    }
    if (!Arrays.equals(_deliveryBasketAtDeliveryDate, other._deliveryBasketAtDeliveryDate)) {
      return false;
    }
    if (!Arrays.equals(_deliveryBasketAtSpotDate, other._deliveryBasketAtSpotDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (_tenor != other._tenor) {
      return false;
    }
    return true;
  }

}
