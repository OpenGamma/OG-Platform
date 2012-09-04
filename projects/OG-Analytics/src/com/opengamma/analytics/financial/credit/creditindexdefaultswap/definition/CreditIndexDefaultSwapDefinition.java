/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditindexdefaultswap.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CreditIndexDefaultSwapDefinition {

  // From the users perspective, are we buying or selling protection
  private final String _buySellProtection;

  // Identifiers for the (three) counterparties in the trade
  private final String _protectionBuyer;
  private final String _protectionSeller;
  private final String _referenceEntity;

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

  // The nuber of obligors in the underlying pool (usually 125 for CDX and iTraxx - although defaults can reduce this)
  private final int _numberOfObligors;

  //The trade notional (in the trade currency)
  private final double _notional;

  private final double _spread;

  private final int _series;
  private final String _version;

  //Constructor for a CDS index swap definition object (all fields are user specified)
  public CreditIndexDefaultSwapDefinition(String buySellProtection,
      String protectionBuyer,
      String protectionSeller,
      String referenceEntity,
      Currency currency,
      Calendar calendar,
      ZonedDateTime startDate,
      ZonedDateTime effectiveDate,
      ZonedDateTime maturityDate,
      ZonedDateTime valuationDate,
      int numberOfObligors,
      double notional,
      double spread,
      String version,
      int series) {

    _buySellProtection = buySellProtection;
    _protectionBuyer = protectionBuyer;
    _protectionSeller = protectionSeller;
    _referenceEntity = referenceEntity;

    _currency = currency;

    _calendar = calendar;

    _startDate = startDate;
    _effectiveDate = effectiveDate;
    _maturityDate = maturityDate;
    _valuationDate = valuationDate;

    _numberOfObligors = numberOfObligors;
    _notional = notional;
    _spread = spread;

    _version = version;
    _series = series;

    //Vector<String> underlyingPool = new Vector(_numberOfObligors);
  }

  //----------------------------------------------------------------------------------------------------------------------------------------

  ZonedDateTime getStartDate() {
    return _startDate;
  }

  ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  ZonedDateTime getMaturityDate() {
    return _maturityDate;
  }

  ZonedDateTime getValuationDate() {
    return _valuationDate;
  }

  int getNumberOfObligors() {
    return _numberOfObligors;
  }

  double getNotional() {
    return _notional;
  }

  double getSpread() {
    return _spread;
  }

  String getBuySellProtection() {
    return _buySellProtection;
  }

  String getProtectionBuyer() {
    return _protectionBuyer;
  }

  String getProtectionSeller() {
    return _protectionSeller;
  }

  String getReferenceEntity() {
    return _referenceEntity;
  }

  Currency getCurrency() {
    return _currency;
  }

  Calendar getCalendar() {
    return _calendar;
  }

  //----------------------------------------------------------------------------------------------------------------------------------------
}
