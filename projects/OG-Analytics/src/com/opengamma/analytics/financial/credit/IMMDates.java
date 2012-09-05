/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.time.DateUtils;

/**
 * Class to construct IMM Date objects for the purposes of CDS schedule generation and valuation
 * Note that these are not the 'true' IMM dates in the sense that official IMM dates fall on the 3rd Wednesday of the month 
 */
public class IMMDates {

  // ------------------------------------------------------------------------

  // TODO : Check the level of access to these ctors/methods (private, public etc)
  // TODO : Remove the previous December date (not needed)

  // ------------------------------------------------------------------------

  // Private (final) member variables

  private final ZonedDateTime _immDatePreviousDecember;
  private final ZonedDateTime _immDateMarch;
  private final ZonedDateTime _immDateJune;
  private final ZonedDateTime _immDateSeptember;
  private final ZonedDateTime _immDateDecember;
  private final ZonedDateTime _immDateNextMarch;

  // ------------------------------------------------------------------------

  // IMM Date object constructor
  public IMMDates(int year) {

    _immDatePreviousDecember = DateUtils.getUTCDate(year - 1, 12, 20);
    _immDateMarch = DateUtils.getUTCDate(year, 3, 20);
    _immDateJune = DateUtils.getUTCDate(year, 6, 20);
    _immDateSeptember = DateUtils.getUTCDate(year, 9, 20);
    _immDateDecember = DateUtils.getUTCDate(year, 12, 20);
    _immDateNextMarch = DateUtils.getUTCDate(year + 1, 3, 20);

  }

  // ------------------------------------------------------------------------

  // Member variable accessor functions (all public)

  public ZonedDateTime getImmDatePreviousDecember() {
    return _immDatePreviousDecember;
  }

  public ZonedDateTime getImmDateMarch() {
    return _immDateMarch;
  }

  public ZonedDateTime getImmDateJune() {
    return _immDateJune;
  }

  public ZonedDateTime getImmDateSeptember() {
    return _immDateSeptember;
  }

  public ZonedDateTime getImmDateDecember() {
    return _immDateDecember;
  }

  public ZonedDateTime getImmDateNextMarch() {
    return _immDateNextMarch;
  }

  // ------------------------------------------------------------------------
}
