/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Definition of a generic Index Credit Default Swap contract
 */
public class IndexCreditDefaultSwapDefinition {

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
  // TODO : Do we need to allow negative notionals to be consistent with end users (convention above is sensible, but might not be market practice)
  // TODO : Need to sort out the quoting conventions for the different indices

  // NOTE : The restructuring clause and debt seniority of the index constituents is contained within the UnderlyingPool class

  // NOTE : The stub type, coupon frequency, daycount fraction and business day convention fields are part of the CDS index definition.
  // NOTE : This is because we need to specify at the index level what the schedule of cashflow payments is (not at the individual
  // NOTE : index constituent level). These fields are input to the CDS index definition and then used to construct the underlying CDS's

  // NOTE : The price type, include accrued and protection start fields are also defined at the level of the index and applied to
  // NOTE : all the underlying CDS's in the index

  // NOTE : In the index ctor we only construct the CDS objects for the obligors in the underlying pool. The calibration of these CDS's
  // NOTE : to the user input CDS par spread term structures

  // NOTE : In principle the user can create an index with an UnderlyingPool consisting of a single Obligor. In this case we are
  // NOTE : essentially approximating the full pool with a one single name CDS. The pricing analytics should be ambivalent to the
  // NOTE : number of obligors in the underlying pool i.e. the correct answer should fall out

  // NOTE : A standard CDS index is uniquely identified by the three-tuple of (_index, _series, _version). This combination is sufficient to 
  // NOTE : identify what UnderlyingPool should be attached to the index (the UnderlyingPool object in turn will be composed of the Obligors
  // NOTE : corresponding to the index identified by the three-tuple)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables (all private and final) of the CDS index swap contract (defines what a CDS index swap is)

  // The name of the index e.g. Bespoke_1
  private final String _indexName;

  // From the users perspective, are we buying or selling protection
  private final BuySellProtection _buySellProtection;

  // The protection buyer
  private final Obligor _protectionBuyer;

  // The protection seller
  private final Obligor _protectionSeller;

  // The pool of obligors which constitute the index e.g. the names in the CDX.NA.IG index for a particular series
  private final UnderlyingPool _underlyingPool;

  // The name of the index (e.g. CDXNAIG)
  private final CDSIndex _index;

  // The series of the above index (e.g. 18)
  private final int _series;

  // The version of the above series
  private final String _version;

  //The currency the trade is executed in e.g. USD
  private final Currency _currency;

  // Holiday calendar for the determination of adjusted business days in the cashflow schedule
  private final Calendar _calendar;

  // The date of the contract inception
  private final ZonedDateTime _startDate;

  // The effective date for protection to begin (usually T + 1bd)
  private final ZonedDateTime _effectiveDate;

  //The date on which the upfront payment is exchanged (usually T + 3bd)
  private final ZonedDateTime _settlementDate;

  // The maturity date of the contract (when premium and protection coverage ceases)
  private final ZonedDateTime _maturityDate;

  // The type of stub (front/back and long/short)
  private final StubType _stubType;

  // The frequency of coupon payments (usually quarterly)
  private final PeriodFrequency _couponFrequency;

  // Day-count convention (usually Act/360)
  private final DayCount _daycountFractionConvention;

  // Business day adjustment convention (usually following)
  private final BusinessDayConvention _businessdayAdjustmentConvention;

  // Flag to determine if we adjust the maturity date to fall on the next IMM date (not a standard feature of index CDS positions)
  private final boolean _immAdjustMaturityDate;

  // Flag to determine if we business day adjust the user input effective date (not a standard feature of index CDS positions)
  private final boolean _adjustEffectiveDate;

  // Flag to determine if we business day adjust the user input settlement date (not a standard feature of index CDS positions)
  private final boolean _adjustSettlementDate;

  // Flag to determine if we business day adjust the final maturity date (not a standard feature of index CDS positions which usually mature on IMM dates)
  private final boolean _adjustMaturityDate;

  // Flag to determine whether the accrued coupons should be included in the underlying pool CDS premium leg calculation
  private final boolean _includeAccruedPremium;

  // Flag to determine if survival probabilities are calculated at the beginning or end of the day (hard coded to TRUE in ISDA CDS model)
  private final boolean _protectionStart;

  //The trade notional (in the trade currency)
  private final double _notional;

  // The amount of upfront exchanged (usually on T + 3bd) - can be positive or negative
  private final double _upfrontPayment;

  // The fixed index coupon (fixed at the issuance of the index)
  private final double _indexCoupon;

  // The current market observed index spread (can differ from the fixed coupon)
  private final double _indexSpread;

  // Vector of single name CDS objects (one for each obligor in the underlying pool)
  private final LegacyCreditDefaultSwapDefinition[] _underlyingCDS;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  //Constructor for a CDS index swap definition object (all fields are user specified)
  public IndexCreditDefaultSwapDefinition(
      final String indexName,
      final BuySellProtection buySellProtection,
      final Obligor protectionBuyer,
      final Obligor protectionSeller,
      final UnderlyingPool underlyingPool,
      final CDSIndex cdsIndex,
      final int series,
      final String version,
      final Currency currency,
      final Calendar calendar,
      final ZonedDateTime startDate,
      final ZonedDateTime effectiveDate,
      final ZonedDateTime settlementDate,
      final ZonedDateTime maturityDate,
      final StubType stubType,
      final PeriodFrequency couponFrequency,
      final DayCount daycountFractionConvention,
      final BusinessDayConvention businessdayAdjustmentConvention,
      final boolean immAdjustMaturityDate,
      final boolean adjustEffectiveDate,
      final boolean adjustSettlementDate,
      final boolean adjustMaturityDate,
      final boolean includeAccruedPremium,
      final boolean protectionStart,
      final double notional,
      final double upfrontPayment,
      final double indexCoupon,
      final double indexSpread) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(indexName, "Index name");

    ArgumentChecker.notNull(buySellProtection, "Buy/Sell");

    ArgumentChecker.notNull(protectionBuyer, "Protection buyer");
    ArgumentChecker.notNull(protectionSeller, "Protection seller");
    ArgumentChecker.notNull(underlyingPool, "Underlying Pool");

    ArgumentChecker.notNull(cdsIndex, "CDS Index");
    ArgumentChecker.notNegative(series, "CDS series");

    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(calendar, "Calendar");

    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(effectiveDate, "Effective date");
    ArgumentChecker.notNull(settlementDate, "Settlement date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");

    // Check the temporal ordering of the input dates (these are the unadjusted dates entered by the user)
    ArgumentChecker.isTrue(!startDate.isAfter(effectiveDate), "Start date {} must be on or before effective date {}", startDate, effectiveDate);
    ArgumentChecker.isTrue(!startDate.isAfter(maturityDate), "Start date {} must be on or before maturity date {}", startDate, maturityDate);
    ArgumentChecker.isTrue(!startDate.isAfter(settlementDate), "Start date {} must be on or before settlement date {}", startDate, settlementDate);
    ArgumentChecker.isTrue(!effectiveDate.isAfter(maturityDate), "Effective date {} must be on or before maturity date {}", effectiveDate, maturityDate);
    ArgumentChecker.isTrue(!settlementDate.isAfter(maturityDate), "Settlement date {} must be on or before maturity date {}", settlementDate, maturityDate);

    ArgumentChecker.notNull(stubType, "Stub type");
    ArgumentChecker.notNull(couponFrequency, "Coupon frequency");
    ArgumentChecker.notNull(daycountFractionConvention, "Daycount fraction convention");
    ArgumentChecker.notNull(businessdayAdjustmentConvention, "Business day adjustment convention");

    ArgumentChecker.notNegative(notional, "Notional amount");
    ArgumentChecker.notNegative(indexCoupon, "Index coupon");
    ArgumentChecker.notNegative(indexSpread, "Index spread");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Initialise the member variables for the CDS index object

    _indexName = indexName;

    _buySellProtection = buySellProtection;

    _protectionBuyer = protectionBuyer;
    _protectionSeller = protectionSeller;
    _underlyingPool = underlyingPool;

    _index = cdsIndex;
    _version = version;
    _series = series;

    _currency = currency;
    _calendar = calendar;

    _startDate = startDate;
    _effectiveDate = effectiveDate;
    _settlementDate = settlementDate;
    _maturityDate = maturityDate;

    _stubType = stubType;
    _couponFrequency = couponFrequency;
    _daycountFractionConvention = daycountFractionConvention;
    _businessdayAdjustmentConvention = businessdayAdjustmentConvention;

    _immAdjustMaturityDate = immAdjustMaturityDate;
    _adjustEffectiveDate = adjustEffectiveDate;
    _adjustSettlementDate = adjustSettlementDate;
    _adjustMaturityDate = adjustMaturityDate;

    _includeAccruedPremium = includeAccruedPremium;

    _protectionStart = protectionStart;

    _notional = notional;
    _upfrontPayment = upfrontPayment;
    _indexCoupon = indexCoupon;
    _indexSpread = indexSpread;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Now build the individual CDS objects for the pool constituents

    // Determine how many obligors are in the underlying pool
    final int numberOfObligors = _underlyingPool.getNumberOfObligors();

    // Construct a vector of CDS objects
    _underlyingCDS = new LegacyCreditDefaultSwapDefinition[numberOfObligors];

    // For each obligor in the underlying pool ...
    for (int i = 0; i < numberOfObligors; i++) {

      // ... build a CDS object for obligor i
      final LegacyCreditDefaultSwapDefinition cds = new LegacyCreditDefaultSwapDefinition(
          _buySellProtection,                             // Specified in the CDS index contract - applies to all underlying CDS's
          _protectionBuyer,                               // Specified in the CDS index contract
          _protectionSeller,                              // Specified in the CDS index contract
          _underlyingPool.getObligors()[i],               // Part of the information carried in the UnderlyingPool object - can vary from obligor to obligor
          _underlyingPool.getCurrency()[i],               // Part of the information carried in the UnderlyingPool object - can vary from obligor to obligor
          _underlyingPool.getDebtSeniority()[i],          // Part of the information carried in the UnderlyingPool object - can vary from obligor to obligor
          _underlyingPool.getRestructuringClause()[i],    // Part of the information carried in the UnderlyingPool object - can vary from obligor to obligor
          _calendar,                                      // Specified in the CDS index contract - applies to all underlying CDS's
          _startDate,                                     // Specified in the CDS index contract - applies to all underlying CDS's
          _effectiveDate,                                 // Specified in the CDS index contract - applies to all underlying CDS's
          _maturityDate,                                  // Specified in the CDS index contract - applies to all underlying CDS's
          _stubType,                                      // Specified in the CDS index contract - applies to all underlying CDS's
          _couponFrequency,                               // Specified in the CDS index contract - applies to all underlying CDS's
          _daycountFractionConvention,                    // Specified in the CDS index contract - applies to all underlying CDS's
          _businessdayAdjustmentConvention,               // Specified in the CDS index contract - applies to all underlying CDS's
          _immAdjustMaturityDate,                         // Specified in the CDS index contract - applies to all underlying CDS's
          _adjustEffectiveDate,                           // Specified in the CDS index contract - applies to all underlying CDS's
          _adjustMaturityDate,                            // Specified in the CDS index contract - applies to all underlying CDS's
          _underlyingPool.getObligorNotionals()[i],       // Part of the information carried in the UnderlyingPool object - can vary from obligor to obligor
          _underlyingPool.getRecoveryRates()[i],          // Part of the information carried in the UnderlyingPool object - can vary from obligor to obligor
          _includeAccruedPremium,                         // Specified in the CDS index contract - applies to all underlying CDS's
          _protectionStart,                               // Specified in the CDS index contract - applies to all underlying CDS's
          _underlyingPool.getCoupons()[i]);               // Part of the information carried in the UnderlyingPool object - can vary from obligor to obligor

      // Assign the CDS just created to obligor i in the underlying pool
      _underlyingCDS[i] = cds;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public member accessor methods

  public String getIndexName() {
    return _indexName;
  }

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

  public ZonedDateTime getStartDate() {
    return _startDate;
  }

  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  public ZonedDateTime getMaturityDate() {
    return _maturityDate;
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

  public boolean getAdjustSettlementDate() {
    return _adjustSettlementDate;
  }

  public boolean getAdjustMaturityDate() {
    return _adjustMaturityDate;
  }

  public boolean getIncludeAccruedPremium() {
    return _includeAccruedPremium;
  }

  public boolean getProtectionStart() {
    return _protectionStart;
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

  public LegacyCreditDefaultSwapDefinition[] getUnderlyingCDS() {
    return _underlyingCDS;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
