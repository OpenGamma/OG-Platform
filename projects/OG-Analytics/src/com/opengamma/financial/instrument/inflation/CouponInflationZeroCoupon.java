/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.inflation;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.util.money.Currency;

/**
 * Class describing an zero-coupon inflation coupon. 
 * The start index value is known when the coupon is traded/issued.
 */
public class CouponInflationZeroCoupon extends CouponDefinition {

  /**
   * The price index associated to the coupon.
   */
  private final PriceIndex _priceIndex;
  /**
   * The reference date for the index at the coupon start. May not be relevant has the index value is known.
   */
  private final ZonedDateTime _referenceStartDate;
  /**
   * The index value at the start of the coupon.
   */
  private final double _indexStartValue;
  /**
   * The reference date for the index at the coupon end.
   */
  private final ZonedDateTime _referenceEndDate;
  /**
   * The date on which the end index is expected to be known. There is usually a difference of two or three month between the reference date and the fixing date.
   * The index is usually known two week after the end of the reference month. The date is only an "expected date" as the index publication could be delayed for 
   * different reasons. The date should not be enforced to strictly in pricing and instrument creation.
   */
  private final ZonedDateTime _fixingEndDate;

  /**
   * Constructor for zero-coupon inflation coupon.
   * @param currency The coupon currency.
   * @param paymentDate The payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon. 
   * @param referenceStartDate The reference date for the index at the coupon start.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param fixingEndDate The date on which the end index is expected to be known.
   */
  public CouponInflationZeroCoupon(Currency currency, ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double paymentYearFraction, double notional,
      PriceIndex priceIndex, ZonedDateTime referenceStartDate, double indexStartValue, ZonedDateTime referenceEndDate, ZonedDateTime fixingEndDate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional);
    Validate.notNull(priceIndex, "Price index");
    Validate.notNull(referenceStartDate, "Reference start date");
    Validate.notNull(referenceEndDate, "Reference end date");
    Validate.notNull(fixingEndDate, "Fixing end date");
    this._priceIndex = priceIndex;
    this._referenceStartDate = referenceStartDate;
    this._indexStartValue = indexStartValue;
    this._referenceEndDate = referenceEndDate;
    this._fixingEndDate = fixingEndDate;
  }

  /**
   * Builder for Constructor for zero-coupon inflation coupon. 
   * The accrualStartDate is used for the referenceStartDate. The paymentDate is used for accrualEndDate. The paymentYearFraction is 1.0.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon. 
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param fixingEndDate The date on which the end index is expected to be known.
   * @return The coupon.
   */
  public CouponInflationZeroCoupon from(ZonedDateTime accrualStartDate, ZonedDateTime paymentDate, double notional, PriceIndex priceIndex, double indexStartValue, ZonedDateTime referenceEndDate,
      ZonedDateTime fixingEndDate) {
    Validate.notNull(priceIndex, "Price index");
    return new CouponInflationZeroCoupon(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex, accrualStartDate, indexStartValue, referenceEndDate,
        fixingEndDate);
  }

  // TODO: builder with lag.

  /**
   * Gets the price index associated to the coupon. 
   * @return The price index.
   */
  public PriceIndex getPriceIndex() {
    return _priceIndex;
  }

  /**
   * Gets the reference date for the index at the coupon start.
   * @return The reference date for the index at the coupon start.
   */
  public ZonedDateTime getReferenceStartDate() {
    return _referenceStartDate;
  }

  /**
   * Gets the index value at the start of the coupon.
   * @return The index value at the start of the coupon.
   */
  public double getIndexStartValue() {
    return _indexStartValue;
  }

  /**
   * Gets the reference date for the index at the coupon end.
   * @return The reference date for the index at the coupon end.
   */
  public ZonedDateTime getReferenceEndDate() {
    return _referenceEndDate;
  }

  /**
   * Gets the date on which the end index is expected to be known.
   * @return The date on which the end index is expected to be known.
   */
  public ZonedDateTime getFixingEndDate() {
    return _fixingEndDate;
  }

  @Override
  public Payment toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    return null;
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return null;
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return null;
  }

}
