/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.collateralmodel;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class to define a CSA to an ISDA Master Agreement used to define the terms of a collateral agreement between two counterparties 
 */
public class CreditSupportAnnexDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work - in - Progress

  // TODO : Will need a builder method to change the collateral call date

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Specify the two obligors who the collateral agreement is between
  private final Obligor _counterpartyA;
  private final Obligor _counterpartyB;

  // Specify the obligor designated as the Calculation Agent (can be one of the two counterparties) - calculates the collateral flowing between the two counterparties
  private final Obligor _calculationAgent;

  // The date when a request for collateral to be posted is made
  private final ZonedDateTime _collateralCallDate;

  // The time (in days) between calling for collateral and receiving it and processing of it
  private final int _marginPeriodOfRisk;

  // The time (in days) it takes to liquidate the specified collateral (this is left as a user defined 'guesstimate')
  private final int _collateralLiquidationHorizon;

  // The type of asset that the collateral is e.g. cash
  private final CollateralType _collateralType;

  // The rate at which a return is earned on the posted collateral
  private final CollateralRate _collateralRate;

  // The currency that the collateral is denominated in
  private final Currency _collateralCurrency;

  // The number of days between calls (or returns) for collateral
  private final MarginCallFrequency _marginCallFrequency;

  // Is the collateral specified in CollateralType substitutable for any other form of collateral
  private final boolean _substituteCollateral;

  // If the collateral is non-cash, can it be reused?
  private final boolean _rehypothecateCollateral;

  // The convention for rounding the collateral amount called for
  private final CollateralRoundingConvention _collateralRoundingConvention;

  // The specification of the collateral to be called
  private final CollateralDefinition _collateral;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public CreditSupportAnnexDefinition(
      final Obligor counterpartyA,
      final Obligor counterpartyB,
      final Obligor valuationAgent,
      final ZonedDateTime collateralCallDate,
      final int marginPeriodOfRisk,
      final int collateralLiquidationHorizon,
      final CollateralType collateralType,
      CollateralRate collateralRate,
      final Currency collateralCurrency,
      final MarginCallFrequency marginCallFrequency,
      final boolean substituteCollateral,
      final boolean rehypothecateCollateral,
      CollateralRoundingConvention collateralRoundingConvention,
      final CollateralDefinition collateral) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(counterpartyA, "Counterparty A");
    ArgumentChecker.notNull(counterpartyB, "Counterparty B");
    ArgumentChecker.notNull(valuationAgent, "Valuation Agent");

    ArgumentChecker.notNull(collateralCallDate, "Collateral call date");

    ArgumentChecker.notNegative(marginPeriodOfRisk, "Margin perioD of risk");
    ArgumentChecker.notNegative(collateralLiquidationHorizon, "Collateral liquidation horizon");

    ArgumentChecker.notNull(collateralType, "Collateral type");
    ArgumentChecker.notNull(collateralRate, "Collateral rate");
    ArgumentChecker.notNull(collateralCurrency, "Collateral currency");

    ArgumentChecker.notNull(marginCallFrequency, "Margin call frequency");

    ArgumentChecker.notNull(collateralRoundingConvention, "Collateral rounding convention");

    ArgumentChecker.notNull(collateral, "Collateral");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _counterpartyA = counterpartyA;
    _counterpartyB = counterpartyB;
    _calculationAgent = valuationAgent;

    _collateralCallDate = collateralCallDate;

    _marginPeriodOfRisk = marginPeriodOfRisk;
    _collateralLiquidationHorizon = collateralLiquidationHorizon;

    _collateralType = collateralType;
    _collateralRate = collateralRate;
    _collateralCurrency = collateralCurrency;

    _marginCallFrequency = marginCallFrequency;

    _substituteCollateral = substituteCollateral;
    _rehypothecateCollateral = rehypothecateCollateral;

    _collateralRoundingConvention = collateralRoundingConvention;

    _collateral = collateral;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  public Obligor getCounterpartyA() {
    return _counterpartyA;
  }

  public Obligor getCounterpartyB() {
    return _counterpartyB;
  }

  public Obligor getValuationAgent() {
    return _calculationAgent;
  }

  public ZonedDateTime getCollateralCallDate() {
    return _collateralCallDate;
  }

  public int getMarginPeriodOfRisk() {
    return _marginPeriodOfRisk;
  }

  public int getCollateralLiquidationHorizon() {
    return _collateralLiquidationHorizon;
  }

  public CollateralType getCollateralType() {
    return _collateralType;
  }

  public CollateralRate getCollateralRate() {
    return _collateralRate;
  }

  public Currency getCollateralCurrency() {
    return _collateralCurrency;
  }

  public MarginCallFrequency getMarginCallFrequency() {
    return _marginCallFrequency;
  }

  public boolean getSubstituteCollateral() {
    return _substituteCollateral;
  }

  public boolean getRehypothecateCollateral() {
    return _rehypothecateCollateral;
  }

  public CollateralRoundingConvention getCollateralRoundingConvention() {
    return _collateralRoundingConvention;
  }

  public CollateralDefinition getCollateral() {
    return _collateral;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
