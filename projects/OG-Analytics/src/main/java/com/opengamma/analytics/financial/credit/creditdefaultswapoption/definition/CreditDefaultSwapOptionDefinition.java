/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Definition of a generic Single Name Credit Default Swap Option contract 
 */
public class CreditDefaultSwapOptionDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Cashflow conventions
  // See conventions in CreditDefaultSwapDefinition.java

  // Payer/Receiver conventions
  // An option to exercise into a buy protection CDS position is a payer swaption
  // An option to exercise into a sell protection CDS position is a receiver swaption

  // NOTE : We assume that the user creates the CDS underlying the contract (with all the market conventions associated with that contract e.g.
  // NOTE : the deliverable debt obligation characteristics) externally to the CDS swaption. This CDS object is then passed into the CDS swaption definition

  // NOTE : We allow the two counterparties in the CDS swaption to be distinct from the (three) counterparties in the underlying CDS
  // NOTE : In practice it is likely that the counterparties in the option contract will be the same as the protection buyer and seller
  // NOTE : in the underlying CDS (but the reference entity in the underlying CDS will be distinct from these two counterparties)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add the argument checkers to check the temporal ordering of the input dates
  // TODO : Add equals and hashcode

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables (all private and final) of the CDS option contract (defines what a CDS option is)

  // From the users perspective, are we buying or selling protection
  private final BuySellProtection _buySellProtection;

  // The counterparties in the CDS swaption trade (can in principle be different to the counterparties in the underlying CDS)
  private final Obligor _protectionBuyer;
  private final Obligor _protectionSeller;

  // The currency the CDS swaption trade is executed in e.g. USD (can be different to the currency of the underlying CDS)
  private final Currency _currency;

  // The date of the CDS swaption contract inception
  private final ZonedDateTime _startDate;

  // The effective date for protection to begin (usually T + 1d for a legacy CDS, T - 60d or T - 90d for a standard CDS)
  private final ZonedDateTime _effectiveDate;

  // Exercise date for the CDS swaption to enter into a CDS contract (this is the start date of the underlying CDS)
  private final ZonedDateTime _optionExerciseDate;

  // The maturity date of the underlying CDS contract (when premium and protection coverage ceases)
  private final ZonedDateTime _maturityDate;

  // The date on which we want to calculate the CDS option MtM
  private final ZonedDateTime _valuationDate;

  // The CDS swaption trade notional (in the trade currency), convention is that this will always be a positive amount
  private final double _notional;

  // The par CDS spread into which the CDS swaption can be exercised into
  private final double _optionStrike;

  // Does the CDS swaption knock-out if there is a default between [_effectiveDate, _optionExercisedate]
  private final CDSOptionKnockoutType _optionKnockoutType;

  // The option type (payer or receiver swaptions)
  private final CDSOptionType _optionType;

  // The option exercise type (typically only European exercise is traded)
  private final CDSOptionExerciseType _optionExerciseType;

  // The underlying CDS referenced in the CDS swaption contract
  private final LegacyCreditDefaultSwapDefinition _underlyingCDS;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public CreditDefaultSwapOptionDefinition(
      BuySellProtection buySellProtection,
      Obligor protectionBuyer,
      Obligor protectionSeller,
      Currency currency,
      ZonedDateTime startDate,
      ZonedDateTime effectiveDate,
      ZonedDateTime optionExerciseDate,
      ZonedDateTime maturityDate,
      ZonedDateTime valuationDate,
      double notional,
      double optionStrike,
      CDSOptionKnockoutType optionKnockoutType,
      CDSOptionType optionType,
      CDSOptionExerciseType optionExerciseType,
      LegacyCreditDefaultSwapDefinition underlyingCDS) {

    // ------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(buySellProtection, "Buy/Sell");

    ArgumentChecker.notNull(protectionBuyer, "Protection buyer");
    ArgumentChecker.notNull(protectionSeller, "Protection seller");

    ArgumentChecker.notNull(currency, "Currency");

    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(effectiveDate, "Effective date");
    ArgumentChecker.notNull(optionExerciseDate, "Option exercise date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(valuationDate, "Valuation date");

    // Check the temporal ordering of the input dates (these are the unadjusted dates entered by the user)

    ArgumentChecker.notNegative(notional, "Notional amount");
    ArgumentChecker.notNegative(optionStrike, "Option strike");

    ArgumentChecker.notNull(optionKnockoutType, "Option knockout type");
    ArgumentChecker.notNull(optionType, "Option type");
    ArgumentChecker.notNull(optionExerciseType, "Option exercise type");

    ArgumentChecker.notNull(underlyingCDS, "CDS");

    // ------------------------------------------------------------------------------------------------

    // Assign the member variables for the CDS option object

    _buySellProtection = buySellProtection;

    _protectionBuyer = protectionBuyer;
    _protectionSeller = protectionSeller;

    _currency = currency;

    _startDate = startDate;
    _effectiveDate = effectiveDate;
    _optionExerciseDate = optionExerciseDate;
    _maturityDate = maturityDate;
    _valuationDate = valuationDate;

    _notional = notional;
    _optionStrike = optionStrike;
    _optionKnockoutType = optionKnockoutType;
    _optionType = optionType;
    _optionExerciseType = optionExerciseType;

    _underlyingCDS = underlyingCDS;

    // ------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public BuySellProtection getBuySellProtection() {
    return _buySellProtection;
  }

  public Obligor getProtectionBuyer() {
    return _protectionBuyer;
  }

  public Obligor getProtectionSeller() {
    return _protectionSeller;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public ZonedDateTime getStartDate() {
    return _startDate;
  }

  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  public ZonedDateTime getOptionExerciseDate() {
    return _optionExerciseDate;
  }

  public ZonedDateTime getMaturityDate() {
    return _maturityDate;
  }

  public ZonedDateTime getValuationDate() {
    return _valuationDate;
  }

  public double getNotional() {
    return _notional;
  }

  public double getOptionStrike() {
    return _optionStrike;
  }

  public CDSOptionKnockoutType getOptionKnockoutType() {
    return _optionKnockoutType;
  }

  public CDSOptionType getOptionType() {
    return _optionType;
  }

  public CDSOptionExerciseType getOptionExerciseType() {
    return _optionExerciseType;
  }

  public LegacyCreditDefaultSwapDefinition getUnderlyingCDS() {
    return _underlyingCDS;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
