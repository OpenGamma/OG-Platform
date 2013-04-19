/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Definition of a generic Single Name Credit Default Swap Option contract (the underlying CDS can be of any type e.g. SNAC, Sov etc)
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
  // NOTE : but for the sake of flexibility we allow the more general case where all the contract counterparties are different

  // NOTE : The maturity of the underlying CDS is not included as part of this contract definition as it is assumed that
  // NOTE : the maturity of the underlying CDS is included as part of the underlying CDS contract definition. It is up to the 
  // NOTE : User to specify what the maturity of the underlying CDS is e.g. a 1Y option to exercise into a 5Y CDS requires the 
  // NOTE : user to construct the underlying CDS with a maturity of the option exercise date + 5Y (as an absolute value)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add equals and hashcode
  // TODO : Do we need the buy/sell flag? Should use the isPayer flag to determine the direction of the cashflows in the underlying CDS contract

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

  // Exercise date for the CDS swaption to enter into a CDS contract (this is the start date of the underlying CDS)
  private final ZonedDateTime _optionExerciseDate;

  // The CDS swaption trade notional (in the trade currency), convention is that this will always be a positive amount
  private final double _notional;

  // The par CDS spread into which the CDS swaption can be exercised into
  private final double _optionStrike;

  // Does the CDS swaption knock-out if there is a default between [_effectiveDate, _optionExercisedate]
  private final boolean _isKnockOut;

  // The option type (true for payer, false for receiver)
  private final boolean _isPayer;

  // The option exercise type (typically only European exercise is traded)
  private final CDSOptionExerciseType _optionExerciseType;

  // The underlying CDS referenced in the CDS swaption contract
  private final CreditDefaultSwapDefinition _underlyingCDS;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public CreditDefaultSwapOptionDefinition(
      final BuySellProtection buySellProtection,
      final Obligor protectionBuyer,
      final Obligor protectionSeller,
      final Currency currency,
      final ZonedDateTime startDate,
      final ZonedDateTime optionExerciseDate,
      final double notional,
      final double optionStrike,
      final boolean isKnockOut,
      final boolean isPayer,
      final CDSOptionExerciseType optionExerciseType,
      final CreditDefaultSwapDefinition underlyingCDS) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(buySellProtection, "Buy/Sell");

    ArgumentChecker.notNull(protectionBuyer, "Protection buyer");
    ArgumentChecker.notNull(protectionSeller, "Protection seller");

    ArgumentChecker.notNull(currency, "Currency");

    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(optionExerciseDate, "Option exercise date");

    ArgumentChecker.notNull(underlyingCDS, "underlying CDS");

    final ZonedDateTime cdsEffectiveDate = underlyingCDS.getEffectiveDate();
    final ZonedDateTime cdsMaturityDate = underlyingCDS.getMaturityDate();

    // Check the temporal ordering of the input dates (these are the unadjusted dates entered by the user)
    ArgumentChecker.isTrue(!startDate.isAfter(cdsEffectiveDate), "Start date {} must be on or before CDS effective date {}", startDate, cdsEffectiveDate);
    ArgumentChecker.isTrue(!startDate.isAfter(optionExerciseDate), "Start date {} must be on or before option exercise date {}", startDate, optionExerciseDate);
    ArgumentChecker.isTrue(!startDate.isAfter(cdsMaturityDate), "Start date {} must be on or before CDS maturity date {}", startDate, cdsMaturityDate);
    ArgumentChecker.isTrue(!cdsEffectiveDate.isBefore(optionExerciseDate), "CDS effective date {} must be on or after option exercise date {}", cdsEffectiveDate, optionExerciseDate);
    ArgumentChecker.isTrue(!cdsEffectiveDate.isAfter(cdsMaturityDate), "CDS effective date {} must be on or before CDS maturity date {}", cdsEffectiveDate, cdsMaturityDate);
    ArgumentChecker.isTrue(!optionExerciseDate.isAfter(cdsMaturityDate), "Option exercise date {} must be on or before CDS maturity date {}", optionExerciseDate, cdsMaturityDate);

    ArgumentChecker.notNegative(notional, "Notional amount");
    ArgumentChecker.notNegative(optionStrike, "Option strike");

    ArgumentChecker.notNull(optionExerciseType, "Option exercise type");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Assign the member variables for the CDS option object

    _buySellProtection = buySellProtection;

    _protectionBuyer = protectionBuyer;
    _protectionSeller = protectionSeller;

    _currency = currency;

    _startDate = startDate;
    _optionExerciseDate = optionExerciseDate;

    _notional = notional;
    _optionStrike = optionStrike;
    _isKnockOut = isKnockOut;
    _isPayer = isPayer;
    _optionExerciseType = optionExerciseType;

    _underlyingCDS = underlyingCDS;

    // ----------------------------------------------------------------------------------------------------------------------------------------
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

  //TODO rename me
  public ZonedDateTime getOptionExerciseDate() {
    return _optionExerciseDate;
  }

  public double getNotional() {
    return _notional;
  }

  public double getOptionStrike() {
    return _optionStrike;
  }

  public boolean isKnockOut() {
    return _isKnockOut;
  }

  public boolean isPayer() {
    return _isPayer;
  }

  public CDSOptionExerciseType getOptionExerciseType() {
    return _optionExerciseType;
  }

  public CreditDefaultSwapDefinition getUnderlyingCDS() {
    return _underlyingCDS;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to modify the recovery rate of the underlying CDS in a CDS Swaption contract

  public CreditDefaultSwapOptionDefinition withRecoveryRate(final double recoveryRate) {

    // Extract the underlying CDS from the CDS Swaption contract
    CreditDefaultSwapDefinition modifiedCDS = getUnderlyingCDS();

    // Modify the recovery rate of the underlying CDS
    modifiedCDS = modifiedCDS.withRecoveryRate(recoveryRate);

    // Return the modified CDS Swaption contract
    return new CreditDefaultSwapOptionDefinition(
        getBuySellProtection(),
        getProtectionBuyer(),
        getProtectionSeller(),
        getCurrency(),
        getStartDate(),
        getOptionExerciseDate(),
        getNotional(),
        getOptionStrike(),
        isKnockOut(),
        isPayer(),
        getOptionExerciseType(),
        modifiedCDS);                 // This is the CDS Swaption contract field that has been modified
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to modify the option strike in a CDS Swaption contract

  public CreditDefaultSwapOptionDefinition withOptionstrike(final double optionStrike) {

    // Return the modified CDS Swaption contract
    return new CreditDefaultSwapOptionDefinition(
        getBuySellProtection(),
        getProtectionBuyer(),
        getProtectionSeller(),
        getCurrency(),
        getStartDate(),
        getOptionExerciseDate(),
        getNotional(),
        optionStrike,
        isKnockOut(),
        isPayer(),
        getOptionExerciseType(),
        getUnderlyingCDS());                 // This is the CDS Swaption contract field that has been modified
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to modify the payer/receiver flag in a CDS Swaption contract

  public CreditDefaultSwapOptionDefinition withIsPayer(final boolean isPayer) {

    // Return the modified CDS Swaption contract
    return new CreditDefaultSwapOptionDefinition(
        getBuySellProtection(),
        getProtectionBuyer(),
        getProtectionSeller(),
        getCurrency(),
        getStartDate(),
        getOptionExerciseDate(),
        getNotional(),
        getOptionStrike(),
        isKnockOut(),
        isPayer,
        getOptionExerciseType(),
        getUnderlyingCDS());                 // This is the CDS Swaption contract field that has been modified
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to modify the knockout/non-knockout flag in a CDS Swaption contract

  public CreditDefaultSwapOptionDefinition withIsKnockout(final boolean isKnockOut) {

    // Return the modified CDS Swaption contract
    return new CreditDefaultSwapOptionDefinition(
        getBuySellProtection(),
        getProtectionBuyer(),
        getProtectionSeller(),
        getCurrency(),
        getStartDate(),
        getOptionExerciseDate(),
        getNotional(),
        getOptionStrike(),
        isKnockOut,
        isPayer(),
        getOptionExerciseType(),
        getUnderlyingCDS());                 // This is the CDS Swaption contract field that has been modified
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
