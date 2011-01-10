/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;

/**
 * A complete defined set of conventions, such as for LIBOR, EURIBOR and STIBOR.
 */
public interface ConventionBundle extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the convention bundle.
   * @return the unique identifier for these conventions, not null
   */
  UniqueIdentifier getUniqueId();

  /**
   * Get the identifier bundle for this reference rate.
   * @return the identifier bundle
   */
  IdentifierBundle getIdentifiers();

  /**
   * Get the display name for the reference rate.
   * This should not be used for anything except display.
   * @return the display name
   */
  String getName();

  /**
   * Get the day count associated with this reference rate or NoDayCount if one isn't available.
   * @return the day count
   */
  DayCount getDayCount();

  /**
   * Get the business day convention (date adjust) for this reference rate.
   * @return the business day convention
   */
  BusinessDayConvention getBusinessDayConvention();

  /**
   * Get the frequency.
   * @return the frequency
   */
  Frequency getFrequency();

  /**
   * The time from now to when the contract is settled, in days.
   * @return the number of days
   */
  int getSettlementDays();

  /**
   * Future point value, if applicable.
   * @return The future point value
   */
  Double getFutureYearFraction();

  /**
   * Gets the swapFixedLegDayCount field.
   * @return the swapFixedLegDayCount
   */
  DayCount getSwapFixedLegDayCount();

  /**
   * Gets the swapFixedLegBusinessDayConvention field.
   * @return the swapFixedLegBusinessDayConvention
   */
  BusinessDayConvention getSwapFixedLegBusinessDayConvention();

  /**
   * Gets the swapFixedLegFrequency field.
   * @return the swapFixedLegFrequency
   */
  Frequency getSwapFixedLegFrequency();

  /**
   * Gets the swapFixedLegSettlementDays field.
   * @return the swapFixedLegSettlementDays
   */
  Integer getSwapFixedLegSettlementDays();

  /**
   * Gets the region identifier for the fixed leg.
   * @return the region identifier for the fixed leg
   */
  Identifier getSwapFixedLegRegion();

  /**
   * Gets the swapFloatingLegDayCount field.
   * @return the swapFloatingLegDayCount
   */
  DayCount getSwapFloatingLegDayCount();

  /**
   * Gets the swapFloatingLegBusinessDayConvention field.
   * @return the swapFloatingLegBusinessDayConvention
   */
  BusinessDayConvention getSwapFloatingLegBusinessDayConvention();

  /**
   * Gets the swapFloatingLegFrequency field.
   * @return the swapFloatingLegFrequency
   */
  Frequency getSwapFloatingLegFrequency();

  /**
   * Gets the swapFloatingLegSettlementDays field.
   * @return the swapFloatingLegSettlementDays
   */
  Integer getSwapFloatingLegSettlementDays();

  /**
   * Get the swapFloatingLegInitialRate field.
   * @return the swapFloatingLegInitialRate
   */
  Identifier getSwapFloatingLegInitialRate();

  /**
   * Gets the region identifier for the floating leg.
   * @return the region identifier for the floating leg
   */
  Identifier getSwapFloatingLegRegion();

  /**
   * Gets the name of the risk free rate for CAPM.
   * @return the name
   */
  String getCAPMRiskFreeRateName();

  /**
   * Gets the name of the market for CAPM.
   * @return the name
   */
  String getCAPMMarketName();

  /**
   * Gets the basisSwapPayFloatingLegDayCount field.
   * @return the basisSwapPayFloatingLegDayCount
   */
  DayCount getBasisSwapPayFloatingLegDayCount();

  /**
   * Gets the basisSwapPayFloatingLegBusinessDayConvention field.
   * @return the basisSwapPayFloatingLegBusinessDayConvention
   */
  BusinessDayConvention getBasisSwapPayFloatingLegBusinessDayConvention();

  /**
   * Gets the basisSwapPayFloatingLegFrequency field.
   * @return the basisSwapPayFloatingLegFrequency
   */
  Frequency getBasisSwapPayFloatingLegFrequency();

  /**
   * Gets the basisSwapPayFloatingLegSettlementDays field.
   * @return the basisSwapPayFloatingLegSettlementDays
   */
  Integer getBasisSwapPayFloatingLegSettlementDays();

  /**
   * Get the basisSwapPayFloatingLegInitialRate field.
   * @return the basisSwapPayFloatingLegInitialRate
   */
  Identifier getBasisSwapPayFloatingLegInitialRate();

  /**
   * Gets the region identifier for the pay floating leg of the basis swap.
   * @return the region identifier for the fixed leg
   */
  Identifier getBasisSwapPayFloatingLegRegion();

  /**
   * Gets the basisSwapReceiveFloatingLegDayCount field.
   * @return the basisSwapReceiveFloatingLegDayCount
   */
  DayCount getBasisSwapReceiveFloatingLegDayCount();

  /**
   * Gets the basisSwapReceiveFloatingLegBusinessDayConvention field.
   * @return the basisSwapReceiveFloatingLegBusinessDayConvention
   */
  BusinessDayConvention getBasisSwapReceiveFloatingLegBusinessDayConvention();

  /**
   * Gets the basisSwapReceiveFloatingLegFrequency field.
   * @return the basisSwapReceiveFloatingLegFrequency
   */
  Frequency getBasisSwapReceiveFloatingLegFrequency();

  /**
   * Gets the basisSwapReceiveFloatingLegSettlementDays field.
   * @return the basisSwapReceiveFloatingLegSettlementDays
   */
  Integer getBasisSwapReceiveFloatingLegSettlementDays();

  /**
   * Get the basisSwapReceiveFloatingLegInitialRate field.
   * @return the basisSwapReceiveFloatingLegInitialRate
   */
  Identifier getBasisSwapReceiveFloatingLegInitialRate();

  /**
   * Gets the region identifier for the receive floating leg of the basis swap.
   * @return the region identifier for the fixed leg
   */
  Identifier getBasisSwapReceiveFloatingLegRegion();

  /**
   * Whether or not the convention for payments is end-of-month or not.
   * In other words, if the maturity is on the last day of a month, are all other payments.
   * @return if the convention is EOM
   */
  boolean isEOMConvention();

  /**
   * Whether to calculate the payment schedule from maturity (i.e. backwards) or forward from the first coupon.
   * @return how to calculate the schedule
   */
  boolean calculateScheduleFromMaturity();

  /**
   * Gets the number of days that a bond is ex-dividend.
   * @return the number of ex-dividend days
   */
  int getExDividendDays();

}
