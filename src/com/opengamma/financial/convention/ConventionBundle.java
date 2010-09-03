/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Interface for all Reference Rates e.g. LIBOR, EURIBOR, STIBOR etc.
 */
public interface ConventionBundle {
  /**
   * Get the unique id for this reference rate
   * @return the unique id
   */
  UniqueIdentifier getUniqueIdentifier();
  /**
   * Get the identifier bundle for this reference rate
   * @return the identifier bundle
   */
  IdentifierBundle getIdentifiers();
  /**
   * Get the display name for the refernce rate - this should not be used for anything except display
   * @return the display name
   */
  String getName();
  /**
   * Get the day count associated with this reference rate or NoDayCount if one isn't available
   * @return the day count
   */
  DayCount getDayCount();
  /**
   * Get the business day convention (date adjust) for this reference rate
   * @return the business day convention
   */   
  BusinessDayConvention getBusinessDayConvention();
  /**
   * Get the frequency
   * @return the frequency
   */
  Frequency getFrequency();
  /**
   * The time from now to when the contract is settled, in days.
   * @return the number of days
   */
  int getSettlementDays();
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
   * Gets the swapFixedLegSettlementDays field
   * @return the swapFixedLegSettlementDays
   */
  Integer getSwapFixedLegSettlementDays();
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
}
