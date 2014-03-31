/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageSecurity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of a bond future security with cash settlement against a price deduced from a yield average. 
 * In particular used for AUD-SFE bond futures.
 * <P>Reference: Add a reference.
 */
public class BondFuturesYieldAverageSecurityDefinition extends FuturesSecurityDefinition<BondFuturesYieldAverageSecurity> {

  /**
   * The number of days between notice date and delivery date.
   */
  private final int _settlementDays;
  /**
   * The basket of deliverable bonds.
   */
  private final BondFixedSecurityDefinition[] _deliveryBasket;
  /**
   * Theoretical coupon rate. The coupon of the synthetic bond used to compute the settlement price from the yield.
   */
  private final double _couponRate;
  /**
   * Underlying tenor in year. The tenor of the synthetic bond used to compute the settlement price from the yield.
   */
  private final int _tenor;
  /**
   * The synthetic delivery date. The settlement date used for the computation of the yield at expiry.
   */
  private final ZonedDateTime _deliveryDate;
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
   * @param deliveryBasket The basket of deliverable bonds.
   * @param couponRate The coupon rate of the synthetic bond used to compute the settlement price from the yield.
   * @param tenor The underlying synthetic bond tenor (in years).
   * @param notional The bond future notional.
   */
  public BondFuturesYieldAverageSecurityDefinition(final ZonedDateTime tradingLastDate, final BondFixedSecurityDefinition[] deliveryBasket, final double couponRate,
      final int tenor, final double notional) {
    super(tradingLastDate);
    ArgumentChecker.notNull(tradingLastDate, "Last trading date");
    ArgumentChecker.notNull(deliveryBasket, "Delivery basket");
    ArgumentChecker.isTrue(deliveryBasket.length > 0, "At least one bond in basket");
    _deliveryBasket = deliveryBasket;
    _couponRate = couponRate;
    _tenor = tenor;
    _settlementDays = _deliveryBasket[0].getSettlementDays();
    _calendar = _deliveryBasket[0].getCalendar();
    _deliveryDate = ScheduleCalculator.getAdjustedDate(tradingLastDate, _settlementDays, _calendar);
    _notional = notional;
  }

  /**
   * Gets the number of days between notice date and delivery date.
   * @return The number of days between notice date and delivery date.
   */
  public int getSettlementDays() {
    return _settlementDays;
  }

  /**
   * Gets the basket of deliverable bonds.
   * @return The basket of deliverable bonds.
   */
  public BondFixedSecurityDefinition[] getDeliveryBasket() {
    return _deliveryBasket;
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

  /**
   * Returns the theoretical delivery date for the bond underlying the futures.
   * @return The delivery date.
   */
  public ZonedDateTime getDeliveryDate() {
    return _deliveryDate;
  }

  /**
   * Gets the notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the holiday calendar.
   * @return The holiday calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondFuturesYieldAverageSecurity toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new NotImplementedException("toDerivative with curve names not implemented.");
  }

  @Override
  public BondFuturesYieldAverageSecurity toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getLastTradingDate()), "Date is after last trading date");
    final double lastTradingTime = TimeCalculator.getTimeBetween(date, getLastTradingDate());
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(date, _settlementDays, _calendar);
    final BondFixedSecurity[] basketAtDelivery = new BondFixedSecurity[_deliveryBasket.length];
    final BondFixedSecurity[] basketAtSpot = new BondFixedSecurity[_deliveryBasket.length];
    for (int loopbasket = 0; loopbasket < _deliveryBasket.length; loopbasket++) {
      basketAtDelivery[loopbasket] = _deliveryBasket[loopbasket].toDerivative(date, _deliveryDate);
      basketAtSpot[loopbasket] = _deliveryBasket[loopbasket].toDerivative(date, spotDate);
    }
    return new BondFuturesYieldAverageSecurity(lastTradingTime, basketAtDelivery, basketAtDelivery, _couponRate, _tenor, _notional);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitBondFuturesYieldAverageSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondFuturesYieldAverageSecurityDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _calendar.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_couponRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_deliveryBasket);
    result = prime * result + _deliveryDate.hashCode();
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _settlementDays;
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BondFuturesYieldAverageSecurityDefinition other = (BondFuturesYieldAverageSecurityDefinition) obj;
    if (Double.doubleToLongBits(_couponRate) != Double.doubleToLongBits(other._couponRate)) {
      return false;
    }
    if (!Arrays.equals(_deliveryBasket, other._deliveryBasket)) {
      return false;
    }
    if (!ObjectUtils.equals(_deliveryDate, other._deliveryDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (_settlementDays != other._settlementDays) {
      return false;
    }
    if (_tenor != other._tenor) {
      return false;
    }
    return true;
  }

}
