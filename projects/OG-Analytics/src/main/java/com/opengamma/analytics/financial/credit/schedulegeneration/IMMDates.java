/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.time.DateUtils;

/**
 * Class to construct IMM Date objects for the purposes of CDS schedule generation and valuation
 * Note that these are not the 'true' IMM dates in the sense that official IMM dates fall on the 3rd Wednesday of the month 
 */
public class IMMDates {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Remove the previous December date (not needed because we always adjust to the following IMM date)
  // TODO : Eventually will replace this with methods based on the TemporalAdjuster class
  // TODO : Add relevant arg checkers

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // NOTE : Note that we only check for an unadjusted IMM date i.e. one that falls on the 20th of the month

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Private (final) member variables

  private final ZonedDateTime _immDatePreviousDecember;
  private final ZonedDateTime _immDateMarch;
  private final ZonedDateTime _immDateJune;
  private final ZonedDateTime _immDateSeptember;
  private final ZonedDateTime _immDateDecember;
  private final ZonedDateTime _immDateNextMarch;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public IMM Date object constructor

  public IMMDates(int year) {

    _immDatePreviousDecember = DateUtils.getUTCDate(year - 1, 12, 20);
    _immDateMarch = DateUtils.getUTCDate(year, 3, 20);
    _immDateJune = DateUtils.getUTCDate(year, 6, 20);
    _immDateSeptember = DateUtils.getUTCDate(year, 9, 20);
    _immDateDecember = DateUtils.getUTCDate(year, 12, 20);
    _immDateNextMarch = DateUtils.getUTCDate(year + 1, 3, 20);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

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

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
