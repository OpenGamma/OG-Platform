/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.collateral.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.collateral.CollateralRate;
import com.opengamma.analytics.financial.credit.collateral.CollateralType;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class to define the characteristics of a collateral agreement between two counterparties
 */
public class CollateralDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work - in - Progress

  // TODO : Add hashcode and equals methods
  // TODO : Can we think of better names for the counterparties to the collateral agreement?
  // TODO : Sort out the posting, processing and liquidation dates
  // TODO : Will probably require a builder method to dynamically set the collateral amount (equal to the MtM of the instrument it collateralises)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Specify the two obligors who the collateral agreement is between
  private final Obligor _counterpartyA;
  private final Obligor _counterpartyB;

  // The type of asset that the collateral is e.g. cash
  private final CollateralType _collateralType;

  // The rate at which a return is earnt on the posted collateral
  private final CollateralRate _collateralRate;

  // The date when a request for collateral to be posted is made
  private final ZonedDateTime _collateralCallDate;

  // The time in days it takes from the collateral call date for an obligor to post collateral
  private final int _timeToPostCollateral;

  // The time in days it takes for an obligor to process collateral once it has been received 
  private final int _timeToProcessCollateral;

  // The time in days it takes an obligor to liquidate collateral
  private final int _timeToLiquidateCollateral;

  // The date (after the calling date) when the collateral is posted 
  private final ZonedDateTime _collateralPostdate;

  // The date (after the calling date) when the collateral is processed
  private final ZonedDateTime _collateralProcessDate;

  // The date (after the calling date) when the collateral is liquidated
  private final ZonedDateTime _collateralLiquidationDate;

  // The currency that the collateral is denominated in
  private final Currency _collateralCurrency;

  // The notional amount of the collateral
  private final double _collateralAmount;

  // Calls below this amount will not result in additional collateral being posted
  private final double _minimumTransferAmount;

  // The exposure threshold that must be breached before collateral is called for
  private final double _collateralTriggerThreshold;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public CollateralDefinition(
      final Obligor counterpartyA,
      final Obligor counterpartyB,
      final CollateralType collateralType,
      final CollateralRate collateralRate,
      final ZonedDateTime collateralCallDate,
      final int timeToPostCollateral,
      final int timeToProcessCollateral,
      final int timeToLiquidateCollateral,
      final Currency collateralCurrency,
      final double collateralAmount,
      final double minimumTransferAmount,
      final double collateralTriggerThreshold) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(counterpartyA, "Counterparty A");
    ArgumentChecker.notNull(counterpartyB, "Counterparty B");

    ArgumentChecker.notNull(collateralType, "Collateral type");
    ArgumentChecker.notNull(collateralRate, "Collateral rate");
    ArgumentChecker.notNull(collateralCallDate, "Collateral call date");
    ArgumentChecker.notNull(collateralCurrency, "Collateral currency");

    ArgumentChecker.notNegative(timeToPostCollateral, "Time to post collateral");
    ArgumentChecker.notNegative(timeToProcessCollateral, "Time to process collateral");
    ArgumentChecker.notNegative(timeToLiquidateCollateral, "Time to liquidate collateral");

    ArgumentChecker.notNegative(minimumTransferAmount, "Minimum transfer amount");
    ArgumentChecker.notNegative(collateralTriggerThreshold, "Collateral trigger threshold");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _counterpartyA = counterpartyA;
    _counterpartyB = counterpartyB;

    _collateralType = collateralType;
    _collateralRate = collateralRate;

    _collateralCallDate = collateralCallDate;
    _timeToPostCollateral = timeToPostCollateral;
    _timeToProcessCollateral = timeToProcessCollateral;
    _timeToLiquidateCollateral = timeToLiquidateCollateral;

    _collateralPostdate = _collateralCallDate.plusDays(_timeToPostCollateral);
    _collateralProcessDate = _collateralPostdate.plusDays(_timeToProcessCollateral);
    _collateralLiquidationDate = _collateralProcessDate.plusDays(_timeToLiquidateCollateral);

    _collateralCurrency = collateralCurrency;

    _collateralAmount = collateralAmount;
    _minimumTransferAmount = minimumTransferAmount;
    _collateralTriggerThreshold = collateralTriggerThreshold;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  public Obligor getCounterpartyA() {
    return _counterpartyA;
  }

  public Obligor getCounterpartyB() {
    return _counterpartyB;
  }

  public CollateralType getCollateralType() {
    return _collateralType;
  }

  public CollateralRate getCollateralRate() {
    return _collateralRate;
  }

  public ZonedDateTime getCollateralCallDate() {
    return _collateralCallDate;
  }

  public int getTimeToPostCollateral() {
    return _timeToPostCollateral;
  }

  public int getTimeToProcessCollateral() {
    return _timeToProcessCollateral;
  }

  public int getTimeToLiquidateCollateral() {
    return _timeToLiquidateCollateral;
  }

  public ZonedDateTime getCollateralPostDate() {
    return _collateralPostdate;
  }

  public ZonedDateTime getCollateralProcessDate() {
    return _collateralProcessDate;
  }

  public ZonedDateTime getCollateralLiquidationDate() {
    return _collateralLiquidationDate;
  }

  public Currency getCollateralCurrency() {
    return _collateralCurrency;
  }

  public double getCollateralAmount() {
    return _collateralAmount;
  }

  public double getMinimumTransferAmount() {
    return _minimumTransferAmount;
  }

  public double getCollateralTriggerThreshold() {
    return _collateralTriggerThreshold;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Add builder method here

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
