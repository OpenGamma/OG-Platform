/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CDSOptionExerciseType;
import com.opengamma.analytics.financial.credit.CDSOptionKnockoutType;
import com.opengamma.analytics.financial.credit.CDSOptionType;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.util.money.Currency;

/**
 * Definition of a generic Single Name Credit Default Swap Option contract 
 */
public class CreditDefaultSwapOptionDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Cashflow conventions

  // Payer/Receiver conventions

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO :

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables (all private and final) of the CDS option contract (defines what a CDS option is)

  // From the users perspective, are we buying or selling protection
  private final BuySellProtection _buySellProtection;

  // The counterparties in the trade
  private final Obligor _protectionBuyer;
  private final Obligor _protectionSeller;

  // The currency the trade is executed in e.g. USD
  private final Currency _currency;

  // The date of the contract inception
  private final ZonedDateTime _startDate;

  // The effective date for protection to begin (usually T + 1d for a legacy CDS, T - 60d or T - 90d for a standard CDS)
  private final ZonedDateTime _effectiveDate;

  private final ZonedDateTime _optionExerciseDate;

  // The maturity date of the contract (when premium and protection coverage ceases)
  private final ZonedDateTime _maturityDate;

  // The date on which we want to calculate the CDS option MtM
  private final ZonedDateTime _valuationDate;

  // The trade notional (in the trade currency), convention is that this will always be a positive amount
  private final double _notional;

  private final double _optionStrike;

  private final CDSOptionKnockoutType _optionKnockoutType;
  private final CDSOptionType _optionType;
  private final CDSOptionExerciseType _optionExerciseType;

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
      CDSOptionExerciseType optionExerciseType) {

    // ------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

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

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
