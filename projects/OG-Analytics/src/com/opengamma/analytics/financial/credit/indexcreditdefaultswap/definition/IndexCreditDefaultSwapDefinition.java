/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CDSIndex;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Definition of a generic Index Credit Default Swap contract (different types of Index CDS will inherit from this)
 */
public abstract class IndexCreditDefaultSwapDefinition {

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

  // The effective date for protection to begin (usually T + 1 for legacy CDS)
  private final ZonedDateTime _effectiveDate;

  // The maturity date of the contract (when premium and protection coverage ceases)
  private final ZonedDateTime _maturityDate;

  // The date on which we want to calculate the CDS MtM
  private final ZonedDateTime _valuationDate;

  //The trade notional (in the trade currency)
  private final double _notional;

  private final double _premuiumLegCoupon;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  //Constructor for a CDS index swap definition object (all fields are user specified)
  public IndexCreditDefaultSwapDefinition(BuySellProtection buySellProtection,
      Obligor protectionBuyer,
      Obligor protectionSeller,
      Obligor[] underlyingPool,
      CDSIndex cdsIndex,
      String version,
      int series,
      Currency currency,
      Calendar calendar,
      ZonedDateTime startDate,
      ZonedDateTime effectiveDate,
      ZonedDateTime maturityDate,
      ZonedDateTime valuationDate,
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

  public int getNumberOfObligors() {
    return _numberOfObligors;
  }

  public double getNotional() {
    return _notional;
  }

  public double getPremiumLegCoupon() {
    return _premuiumLegCoupon;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public Calendar getCalendar() {
    return _calendar;
  }

  //----------------------------------------------------------------------------------------------------------------------------------------
}
