/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Interface for all Reference Rates e.g. LIBOR, EURIBOR, STIBOR etc.
 */
public interface ReferenceRate {
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
   * Get the number of days offset for settlement
   * TODO: Elaine/Richard, could you improve this description?
   * @return the number of days
   */
  int getSettlementDays();
}
