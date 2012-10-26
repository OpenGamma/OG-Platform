/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CDSIndex;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Definition of a generic Index Credit Default Swap contract (different types of Index CDS will inherit from this)
 */
public abstract class IndexCreditDefaultSwapDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Cashflow Conventions are assumed to be as below (these will apply throughout the entire credit suite)
  // Note that the long/short credit convention is opposite to that for single name CDS's

  // Notional amount > 0 always - long/short positions are captured by the setting of the 'BuySellProtection' flag
  // This convention is chosen to avoid confusion about whether a negative notional means a long/short position etc

  // Buy index protection   -> Pay contingent leg, receive premium leg  -> 'short' protection  -> 'long' credit risk
  // Sell index protection  -> Receive contingent leg, pay premium leg  -> 'long' protection -> 'short' credit risk

  // Coupon conventions - coupons are always assumed to be entered in bps (therefore there are internal conversions to absolute values by division by 10,000)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work In Progress

  // TODO : Replace _series, _version with enums
  // TODO : Replace the _underlyingPool obligor with a dedicated 'pool' class

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables (all private and final) of the CDS index swap contract (defines what a CDS index swap is)

  // The number of obligors in the underlying pool (usually 125 for CDX and iTraxx - although defaults can reduce this)
  private final int _numberOfObligors;

  // From the users perspective, are we buying or selling protection
  private final BuySellProtection _buySellProtection;

  // The protection buyer
  private final Obligor _protectionBuyer;

  // The protection seller
  private final Obligor _protectionSeller;

  // A vector of obligors representing the names in the underlying pool
  private final Obligor[] _underlyingPool = new Obligor[125];

  private final CDSIndex _index;
  private final int _series;
  private final String _version;

  //The currency the trade is executed in e.g. USD
  private final Currency _currency;

  // Holiday calendar for the determination of adjusted business days in the cashflow schedule
  private final Calendar _calendar;

  // The date of the contract inception
  private final ZonedDateTime _startDate;

  // The effective date for protection to begin (usually T + 1bd)
  private final ZonedDateTime _effectiveDate;

  // The maturity date of the contract (when premium and protection coverage ceases)
  private final ZonedDateTime _maturityDate;

  // The date on which we want to calculate the CDS index MtM
  private final ZonedDateTime _valuationDate;

  // The date on which the upfront payment is exchanged (usually T + 3bd)
  private final ZonedDateTime _settlementDate;

  // The method for generating the schedule of premium payments
  private final StubType _stubType;

  // The frequency of coupon payments (usually quarterly for legacy and standard CDS)
  private final PeriodFrequency _couponFrequency;

  // Day-count convention (usually Act/360 for legacy and standard CDS)
  private final DayCount _daycountFractionConvention;

  // Business day adjustment convention (usually following for legacy and standard CDS)
  private final BusinessDayConvention _businessdayAdjustmentConvention;

  //The trade notional (in the trade currency)
  private final double _notional;

  // The spread to apply to the premium leg
  private final double _premuiumLegCoupon;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  //Constructor for a CDS index swap definition object (all fields are user specified)
  public IndexCreditDefaultSwapDefinition(BuySellProtection buySellProtection,
      Obligor protectionBuyer,
      Obligor protectionSeller,
      Obligor[] underlyingPool,
      CDSIndex cdsIndex,
      int series,
      String version,
      Currency currency,
      Calendar calendar,
      ZonedDateTime startDate,
      ZonedDateTime effectiveDate,
      ZonedDateTime maturityDate,
      ZonedDateTime valuationDate,
      ZonedDateTime settlementDate,
      StubType stubType,
      PeriodFrequency couponFrequency,
      DayCount daycountFractionConvention,
      BusinessDayConvention businessdayAdjustmentConvention,
      int numberOfObligors,
      double notional,
      double premiumLegCoupon) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(buySellProtection, "Buy/Sell");

    ArgumentChecker.notNull(protectionBuyer, "Protection buyer");
    ArgumentChecker.notNull(protectionSeller, "Protection seller");
    //ArgumentChecker.notNull(referenceEntity, "Reference entity");

    ArgumentChecker.notNull(cdsIndex, "CDS Index");

    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(calendar, "Calendar");

    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(effectiveDate, "Effective date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(settlementDate, "Settlement date");

    ArgumentChecker.notNull(stubType, "Stub type");
    ArgumentChecker.notNull(couponFrequency, "Coupon frequency");
    ArgumentChecker.notNull(daycountFractionConvention, "Daycount fraction convention");
    ArgumentChecker.notNull(businessdayAdjustmentConvention, "Business dat adjustment convention");

    ArgumentChecker.notNegative(numberOfObligors, "Number of obligors");

    ArgumentChecker.notNegative(notional, "Notional amount");
    ArgumentChecker.notNegative(premiumLegCoupon, "Premium Leg coupon");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _buySellProtection = buySellProtection;

    _protectionBuyer = protectionBuyer;
    _protectionSeller = protectionSeller;
    //_underlyingPool = underlyingPool;

    _index = cdsIndex;
    _version = version;
    _series = series;

    _currency = currency;
    _calendar = calendar;

    _startDate = startDate;
    _effectiveDate = effectiveDate;
    _maturityDate = maturityDate;
    _valuationDate = valuationDate;
    _settlementDate = settlementDate;

    _stubType = stubType;
    _couponFrequency = couponFrequency;
    _daycountFractionConvention = daycountFractionConvention;
    _businessdayAdjustmentConvention = businessdayAdjustmentConvention;

    _numberOfObligors = numberOfObligors;
    _notional = notional;
    _premuiumLegCoupon = premiumLegCoupon;
  }

  //----------------------------------------------------------------------------------------------------------------------------------------

  // Public member accessor methods

  public BuySellProtection getBuySellProtection() {
    return _buySellProtection;
  }

  public Obligor getProtectionBuyer() {
    return _protectionBuyer;
  }

  public Obligor getProtectionSeller() {
    return _protectionSeller;
  }

  public Obligor[] getUnderlyingPool() {
    return _underlyingPool;
  }

  public CDSIndex getIndex() {
    return _index;
  }

  public String getVersion() {
    return _version;
  }

  public int getSeries() {
    return _series;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public Calendar getCalendar() {
    return _calendar;
  }

  public ZonedDateTime getStartDate() {
    return _startDate;
  }

  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  public ZonedDateTime getMaturityDate() {
    return _maturityDate;
  }

  public ZonedDateTime getValuationDate() {
    return _valuationDate;
  }

  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  public StubType getStubType() {
    return _stubType;
  }

  public PeriodFrequency getCouponFrequency() {
    return _couponFrequency;
  }

  public DayCount getDaycountFractionConvention() {
    return _daycountFractionConvention;
  }

  public BusinessDayConvention getBusinessdayAdjustmentConvention() {
    return _businessdayAdjustmentConvention;
  }

  public int getNumberOfObligors() {
    return _numberOfObligors;
  }

  public double getNotional() {
    return _notional;
  }

  public double getPremiumLegCoupon() {
    return _premuiumLegCoupon;
  }

  //----------------------------------------------------------------------------------------------------------------------------------------
}
