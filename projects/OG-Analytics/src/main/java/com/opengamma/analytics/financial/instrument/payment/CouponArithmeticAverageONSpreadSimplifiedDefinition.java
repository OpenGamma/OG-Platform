/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponArithmeticAverageON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Fed Fund swap-like floating coupon (arithmetic average on overnight rates) with a spread.
 * Simplified definition which contains only the starting date and end date (not all intermediary dates). The fixing and accrual dates are equal.
 */
public class CouponArithmeticAverageONSpreadSimplifiedDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The overnight index on which the coupon fixes. The index currency should be the same as the coupon currency. Not null.
   */
  private final IndexON _index;
  /**
   * The spread rate paid above the arithmetic average.
   */
  private final double _spread;
  /**
   * The fixed amount related to the spread.
   */
  private final double _spreadAmount;

  /**
   * Constructor from all the coupon details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes.
   * @param spread The spread rate paid above the arithmetic average.
   */
  public CouponArithmeticAverageONSpreadSimplifiedDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentYearFraction, final double notional, final IndexON index, final double spread) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional);
    ArgumentChecker.notNull(index, "CouponOISDefinition: index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "Coupon and index currencies are not compatible. Expected to be the same");
    _index = index;
    _spread = spread;
    _spreadAmount = spread * paymentYearFraction * notional;
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param settlementDate The coupon settlement date.
   * @param tenor The coupon tenor.
   * @param notional The notional.
   * @param settlementDays The number of days between last fixing and the payment (also called spot lag).
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @param spread The spread rate paid above the arithmetic average.
   * @return The OIS coupon.
   */
  public static CouponArithmeticAverageONSpreadSimplifiedDefinition from(final IndexON index, final ZonedDateTime settlementDate, final Period tenor, final double notional, final int settlementDays,
      final BusinessDayConvention businessDayConvention, final boolean isEOM, final double spread) {
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(settlementDate, tenor, businessDayConvention, index.getCalendar(), isEOM);
    return from(index, settlementDate, fixingPeriodEndDate, notional, settlementDays, spread);
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param settlementDate The coupon settlement date.
   * @param fixingPeriodEndDate The last date of the fixing period. Interest accrues up to this date. If publicationLag==0, 1 day following publication. If lag==1, the publication date.
   * @param notional The notional.
   * @param paymentLag The number of days between last fixing date and the payment fate (also called payment lag).
   * @param spread The spread rate paid above the arithmetic average.
   * @return The OIS coupon.
   */
  public static CouponArithmeticAverageONSpreadSimplifiedDefinition from(final IndexON index, final ZonedDateTime settlementDate, final ZonedDateTime fixingPeriodEndDate, final double notional,
      final int paymentLag, final double spread) {
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(fixingPeriodEndDate, -1 + index.getPublicationLag() + paymentLag, index.getCalendar());
    final double paymentYearFraction = index.getDayCount().getDayCountFraction(settlementDate, fixingPeriodEndDate);
    return new CouponArithmeticAverageONSpreadSimplifiedDefinition(index.getCurrency(), paymentDate, settlementDate, fixingPeriodEndDate, paymentYearFraction, notional, index, spread);
  }

  /**
   * Gets the OIS index of the instrument.
   * @return The index.
   */
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Returns the spread rate paid above the arithmetic average.
   * @return The spread.
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Returns the fixed amount related to the spread.
   * @return The amount.
   */
  public double getSpreadAmount() {
    return _spreadAmount;
  }

  @Override
  public CouponArithmeticAverageON toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.isTrue(!getAccrualStartDate().plusDays(_index.getPublicationLag()).isBefore(date), "First fixing publication strictly before reference date");
    return null;
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    return null;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return null;
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return null;
  }

}
