/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import javax.time.calendar.ZonedDateTime;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

/**
 *  Definition of a vanilla legacy Credit Default Swap contract (i.e. transacted pre 8th April 2009)
 */
public class CreditDefaultSwapDefinition {
  
  //----------------------------------------------------------------------------------------------------------------------------------------

  // From the users perspective, are we buying or selling protection
  private final String _buysellprotection;

  // Identifiers for the (three) counterparties in the trade
  private final String _protectionbuyer;
  private final String _protectionseller;
  private final String _referenceentity;
  
  // The currency the trade is executed in e.g. USD
  private final Currency _currency;
  
  // The seniority of the debt of the reference entity the CDS is written on (e.g. senior or subordinated)
  private final String _debtseniority;
  
  // The restructuring type in the event of a credit event deemed to be a restructuring of the reference entities debt (e.g. OR, MR, MMR or NR)
  private final String _restructuringclause;
  
  // Holiday calendar for the determination of adjusted business days in the cashflow schedule
  private final Calendar _calendar;
  
  // The date of the contract inception
  private final ZonedDateTime _startdate;
  
  // The effective date for protection to begin (usually T + 1 for legacy CDS)
  private final ZonedDateTime _effectivedate;
  
  // The maturity date of the contract (when premium and protection coverage ceases)
  private final ZonedDateTime _maturitydate;
  
  // The date on which we want to calculate the CDS MtM
  private final ZonedDateTime _valuationdate;
  
  // The method for generating the schedule of premium payments
  private final String _schedulegenerationmethod;
  
  // The frequency of coupon payments (usually quarterly)
  private final String _couponfrequency;
  
  // Day-count convention (usually Act/360)
  private final String _daycountfractionconvention;
  
  // Business day adjustment convention (usually following)
  private final String _businessdayadjustmentconvention;
  
  // Flag to determine if we business day adjust the final maturity date
  private final boolean _adjustmaturitydate;
  
  // The trade notional (in the trade currency)
  private final double _notional;
  
  // The quoted par spread of the trade (as this is a legacy CDS, this is the coupon applied to the premium leg 
  // to give the trade a value of zero at the contract start date (there is no exchange of payments upfront) 
  private final double _parspread;
  
  // The recovery rate to be used in the calculation of the CDS MtM
  private final double _valuationrecoveryrate;
  
  // The recovery rate to be used when calibrating the hazard rate term structure to the market par CDS spread quotes
  private final double _curverecoveryrate;
  
  // Flag to determine whether the accrued coupons should be included in the CDS premium lag calculation
  private final boolean _includeaccruedpremium;
  
  // Vector of dates for which interest rates are provided
  private final ZonedDateTime[] _interestratetenors;
  
  // Vector of interest rates from which to bootstrap the discount factor curve 
  private final ZonedDateTime[] _interestrates;
  
  // Vector of dates for which market observed CDS par spread quotes are provided
  private final ZonedDateTime[] _creditspreadtenors;
  
  // Vector of market observed CDS par spread quotes from which to bootstrap the survival probability curve
  private final ZonedDateTime[] _creditspreads;
  
  // ----------------------------------------------------------------------------------------------------------------------------------------
  
  // Constructor for a CDS definition object (all fields are user specified)
 
  // Probably not the best way of handling schedule generation and interest rate and credit spread curves - need to look at another product to see how it is done
  
  // How can we reduce the number of parameters?
  
  public CreditDefaultSwapDefinition(String buysellprotection,
                                      String protectionbuyer, 
                                      String protectionseller, 
                                      String referenceentity, 
                                      Currency currency, 
                                      String debtseniority, 
                                      String restructuringclause, 
                                      Calendar calendar,
                                      ZonedDateTime startdate,
                                      ZonedDateTime effectivedate,
                                      ZonedDateTime maturitydate,
                                      ZonedDateTime valuationdate,
                                      String schedulegenerationmethod,
                                      String couponfrequency,
                                      String daycountfractionconvention,
                                      String businessdayadjustmentconvention,
                                      double notional, 
                                      double parspread, 
                                      double valuationrecoveryrate, 
                                      double curverecoveryrate, 
                                      boolean includeaccruedpremium,
                                      boolean adjustmaturitydate,
                                      ZonedDateTime[] interestratetenors,
                                      ZonedDateTime[] interestrates,
                                      ZonedDateTime[] creditspreadtenors,
                                      ZonedDateTime[] creditspreads) {
    
    Validate.notNull(buysellprotection, "buysellprotection");
    Validate.notNull(protectionseller, "protectionseller");
    Validate.notNull(protectionbuyer, "protectionbuyer");
    Validate.notNull(referenceentity, "referenceentity");
    Validate.notNull(currency, "currency");
    Validate.notNull(debtseniority, "debtseniority");
    Validate.notNull(restructuringclause, "restructuringclause");
    Validate.notNull(calendar, "calendar");
    
    Validate.isTrue(parspread > 0.0, "CDS par spread should be greater than zero");
    Validate.isTrue(notional >= 0.0, "Notional should be greater than or equal to zero");
    
    Validate.isTrue(valuationrecoveryrate >= 0.0, "Valuation recovery rate should be in the range [0%, 100%]");
    Validate.isTrue(valuationrecoveryrate <= 1.0, "Valuation recovery rate should be in the range [0%, 100%]");
    Validate.isTrue(curverecoveryrate >= 0.0, "Curve recovery rate should be in the range [0%, 100%]");
    Validate.isTrue(curverecoveryrate <= 1.0, "Curve recovery rate should be in the range [0%, 100%]");
    
    ArgumentChecker.isTrue(startdate.isBefore(valuationdate), "Start date {} must be before valuation date {}", startdate, valuationdate);
    // Remember have to check that the dates are in the right order e.g. maturity < start
    // This will be a problem because we are using ZoneddateTime objects (need to convert to a double?)
    
    _buysellprotection = buysellprotection;
    _protectionbuyer = protectionbuyer;
    _protectionseller = protectionseller;
    _referenceentity = referenceentity;
    
    _currency = currency;
    _debtseniority = debtseniority;
    _restructuringclause = restructuringclause;
    
    _calendar = calendar;
    
    _startdate = startdate;
    _effectivedate = effectivedate;
    _maturitydate = maturitydate;
    _valuationdate = valuationdate;
    
    _schedulegenerationmethod = schedulegenerationmethod;
    _couponfrequency = couponfrequency;
    _daycountfractionconvention = daycountfractionconvention;
    _businessdayadjustmentconvention = businessdayadjustmentconvention;
    _adjustmaturitydate = adjustmaturitydate;
    
    _notional = notional;
    _parspread = parspread;
    
    _valuationrecoveryrate = valuationrecoveryrate;
    _curverecoveryrate = curverecoveryrate;
    
    _includeaccruedpremium = includeaccruedpremium;
    
    _interestratetenors = interestratetenors;               // This should be a vector - need to add get method
    _interestrates = interestrates;                         // This should be a vector - need to add get method
    _creditspreadtenors = creditspreadtenors;               // This should be a vector - need to add get method
    _creditspreads = creditspreads;                         // This should be a vector - need to add get method
  }
  
//----------------------------------------------------------------------------------------------------------------------------------------

  public String getBuySellProtection() {
    return _buysellprotection;
  }
   
  public String getProtectionBuyer() {
    return _protectionbuyer;
  }
   
  public String getProtectionSeller() {
    return _protectionseller;
  }
   
  public String getReferenceEntity() {
    return _referenceentity;
  }
  
//----------------------------------------------------------------------------------------------------------------------------------------
  
  public Currency getCurrency() {
    return _currency;
  }
  
  public String getDebtSeniority() {
    return _debtseniority;
  }
  
  public String getRestructuringClause() {
    return _restructuringclause;
  }
  
  public Calendar getCalendar() {
    return _calendar;
  }
  
//----------------------------------------------------------------------------------------------------------------------------------------
  
  public ZonedDateTime getStartDate() {
    return _startdate;
  }
  
  public ZonedDateTime getEffectiveDate() {
    return _effectivedate;
  }
  
  public ZonedDateTime getMaturityDate() {
    return _maturitydate;
  }
  
  public ZonedDateTime getValuationDate() {
    return _valuationdate;
  }

//----------------------------------------------------------------------------------------------------------------------------------------
  
  public String getScheduleGenerationMethod() {
    return _schedulegenerationmethod;
  }
  
  public String getCouponFrequency() {
    return _couponfrequency;
  }
  
  public String getDayCountFractionConvention() {
    return _daycountfractionconvention;
  }
  
  public String getBusinessDayAdjustmentConvention() {
    return _businessdayadjustmentconvention;
  }
  
  public boolean getAdjustMaturityDate() {
    return _adjustmaturitydate;
  }
  
//----------------------------------------------------------------------------------------------------------------------------------------
  
  public double getNotional() {
    return _notional;
  }
  
  public double getParSpread() {
    return _parspread;
  }
  
  public double getValuationRecoveryRate() {
    return _valuationrecoveryrate;
  }
  
  public double getCurveRecoveryRate() {
    return _curverecoveryrate;
  }
  
  public boolean getIncludeAccruedPremium() {
    return _includeaccruedpremium;
  }
  
  //----------------------------------------------------------------------------------------------------------------------------------------
  
  ZonedDateTime[] getInterestratetenors() {
    return _interestratetenors;
  }

  ZonedDateTime[] getInterestrates() {
    return _interestrates;
  }
  
  ZonedDateTime[] getCreditspreadtenors() {
    return _creditspreadtenors;
  }
  

  ZonedDateTime[] getCreditspreads() {
    return _creditspreads;
  }
  
}

//----------------------------------------------------------------------------------------------------------------------------------------
