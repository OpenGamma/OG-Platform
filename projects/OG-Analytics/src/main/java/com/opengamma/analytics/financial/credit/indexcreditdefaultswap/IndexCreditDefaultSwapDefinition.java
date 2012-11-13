/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
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

  // Cashflow Conventions are assumed to be as below (these will apply throughout the entire credit suite for index credit default swaps)
  // Note that the long/short credit convention is opposite to that for single name CDS's

  // Notional amount > 0 always - long/short positions are captured by the setting of the 'BuySellProtection' flag
  // This convention is chosen to avoid confusion about whether a negative notional means a long/short position etc

  // Buy index protection   -> Pay contingent leg, receive premium leg  -> 'short' protection  -> 'long' credit risk
  // Sell index protection  -> Receive contingent leg, pay premium leg  -> 'long' protection -> 'short' credit risk

  // Coupon conventions - coupons are always assumed to be entered in bps (therefore there are internal conversions to absolute values by division by 10,000)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work In Progress

  // TODO : Add the hashCode and equals methods
  // TODO : Replace _series, _version with enums
  // TODO : Add the argument checkers to verify the input dates are in the right temporal order
  // TODO : Need to sort out the quoting conventions for the different indices
  // TODO : Do we need the flag to adjust the maturity date to an IMM date - standard CDS index positions always mature on an IMM date anyway
  // TODO : Generalise the model so that if the underlying pool has only a single name, the code knows we are modelling the index as a single name CDS
  // TODO : Include the standard indices which inherit from this super class (include a bespoke index that allows the user to create their own index)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables (all private and final) of the CDS index swap contract (defines what a CDS index swap is)

  // From the users perspective, are we buying or selling protection
  private final BuySellProtection _buySellProtection;

  // The protection buyer
  private final Obligor _protectionBuyer;

  // The protection seller
  private final Obligor _protectionSeller;

  // The pool of obligors which constitute the index e.g. the names in the CDX.NA.IG index for a particular series
  private final UnderlyingPool _underlyingPool;

  private final CDSIndex _index;
  private final int _series;
  private final String _version;

  //The currency the trade is executed in e.g. USD
  private final Currency _currency;

  // Holiday calendar for the determination of adjusted business days in the cashflow schedule
  private final Calendar _calendar;

  // The restructuring type in the event of a credit event deemed to be a restructuring of the reference entities debt (CDX is NORE, but iTraxx can have restructuring as a default trigger)
  private final RestructuringClause _restructuringClause;

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

  // The frequency of coupon payments (usually quarterly)
  private final PeriodFrequency _couponFrequency;

  // Day-count convention (usually Act/360)
  private final DayCount _daycountFractionConvention;

  // Business day adjustment convention (usually following)
  private final BusinessDayConvention _businessdayAdjustmentConvention;

  // Flag to determine if we adjust the maturity date to fall on the next IMM date (not a standard feature of index CDS positions)
  private final boolean _immAdjustMaturityDate;

  //Flag to determine if we business day adjust the user input effective date (not a standard feature of index CDS positions)
  private final boolean _adjustEffectiveDate;

  // Flag to determine if we business day adjust the final maturity date (not a standard feature of index CDS positions)
  private final boolean _adjustMaturityDate;

  //The trade notional (in the trade currency)
  private final double _notional;

  // The amount of upfront exchanged (usually on T + 3bd)
  private final double _upfrontPayment;

  // The fixed index coupon (fixed at the issuance of the index)
  private final double _indexCoupon;

  // The current market observed index spread (can differ from the fixed coupon)
  private final double _indexSpread;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  //Constructor for a CDS index swap definition object (all fields are user specified)
  public IndexCreditDefaultSwapDefinition(BuySellProtection buySellProtection,
      Obligor protectionBuyer,
      Obligor protectionSeller,
      UnderlyingPool underlyingPool,
      CDSIndex cdsIndex,
      int series,
      String version,
      Currency currency,
      Calendar calendar,
      RestructuringClause restructuringClause,
      ZonedDateTime startDate,
      ZonedDateTime effectiveDate,
      ZonedDateTime maturityDate,
      ZonedDateTime valuationDate,
      ZonedDateTime settlementDate,
      StubType stubType,
      PeriodFrequency couponFrequency,
      DayCount daycountFractionConvention,
      BusinessDayConvention businessdayAdjustmentConvention,
      boolean immAdjustMaturityDate,
      boolean adjustEffectiveDate,
      boolean adjustMaturityDate,
      double notional,
      double upfrontPayment,
      double indexCoupon,
      double indexSpread) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(buySellProtection, "Buy/Sell");

    ArgumentChecker.notNull(protectionBuyer, "Protection buyer");
    ArgumentChecker.notNull(protectionSeller, "Protection seller");
    ArgumentChecker.notNull(underlyingPool, "Underlying Pool");

    ArgumentChecker.notNull(cdsIndex, "CDS Index");

    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(calendar, "Calendar");

    ArgumentChecker.notNull(restructuringClause, "Restructuring clause");

    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(effectiveDate, "Effective date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(settlementDate, "Settlement date");

    ArgumentChecker.notNull(stubType, "Stub type");
    ArgumentChecker.notNull(couponFrequency, "Coupon frequency");
    ArgumentChecker.notNull(daycountFractionConvention, "Daycount fraction convention");
    ArgumentChecker.notNull(businessdayAdjustmentConvention, "Business dat adjustment convention");

    ArgumentChecker.notNegative(notional, "Notional amount");
    ArgumentChecker.notNegative(upfrontPayment, "Upfront payment");
    ArgumentChecker.notNegative(indexCoupon, "Index coupon");
    ArgumentChecker.notNegative(indexSpread, "Index spread");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _buySellProtection = buySellProtection;

    _protectionBuyer = protectionBuyer;
    _protectionSeller = protectionSeller;
    _underlyingPool = underlyingPool;

    _index = cdsIndex;
    _version = version;
    _series = series;

    _currency = currency;
    _calendar = calendar;

    _restructuringClause = restructuringClause;

    _startDate = startDate;
    _effectiveDate = effectiveDate;
    _maturityDate = maturityDate;
    _valuationDate = valuationDate;
    _settlementDate = settlementDate;

    _stubType = stubType;
    _couponFrequency = couponFrequency;
    _daycountFractionConvention = daycountFractionConvention;
    _businessdayAdjustmentConvention = businessdayAdjustmentConvention;

    _immAdjustMaturityDate = immAdjustMaturityDate;
    _adjustEffectiveDate = adjustEffectiveDate;
    _adjustMaturityDate = adjustMaturityDate;

    _notional = notional;
    _upfrontPayment = upfrontPayment;
    _indexCoupon = indexCoupon;
    _indexSpread = indexSpread;
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

  public UnderlyingPool getUnderlyingPool() {
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

  public RestructuringClause getRestructuringClause() {
    return _restructuringClause;
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

  public boolean getIMMAdjustMaturityDate() {
    return _immAdjustMaturityDate;
  }

  public boolean getAdjustEffectiveDate() {
    return _adjustEffectiveDate;
  }

  public boolean getAdjustMaturityDate() {
    return _adjustMaturityDate;
  }

  public double getNotional() {
    return _notional;
  }

  public double getUpfrontPayment() {
    return _upfrontPayment;
  }

  public double getIndexCoupon() {
    return _indexCoupon;
  }

  public double getIndexSpread() {
    return _indexSpread;
  }

  //----------------------------------------------------------------------------------------------------------------------------------------
}
