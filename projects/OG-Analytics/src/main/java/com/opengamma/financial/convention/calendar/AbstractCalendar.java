/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import java.io.Serializable;

import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;

/**
 * Abstract base class implementing the {@code Calendar} interface.
 * <p>
 * This class exists to simplify common patterns of normal+exception data.
 */
public abstract class AbstractCalendar implements Calendar, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The convention name.
   */
  private final String _name;

  /**
   * Creates an instance.
   * @param name  the convention name, not null
   */
  protected AbstractCalendar(final String name) {
    Validate.notNull(name, "name");
    _name = name;
  }

  // -------------------------------------------------------------------------
  /**
   * Checks if the date is a working date.
   * <p>
   * This invokes {@link #isNormallyWorkingDay(LocalDate)} followed by either
   * {@link #isWorkingDayException(LocalDate)} or {@link #isNonWorkingDayException(LocalDate)}.
   * 
   * @param date  the date to check, not null
   * @return true if the date is a working date
   */
  @Override
  public final boolean isWorkingDay(final LocalDate date) {
    Validate.notNull(date, "date");
    if (isNormallyWorkingDay(date)) {
      return !isWorkingDayException(date);
    }
    return isNonWorkingDayException(date);
  }

  /**
   * Gets the name of the convention.
   * 
   * @return the name, not null
   * @deprecated use getName()
   */
  @Override
  @Deprecated
  public String getConventionName() {
    return getName();
  }

  @Override
  public String getName() {
    return _name;
  }

  // -------------------------------------------------------------------------
  /**
   * Checks if the date would be a working day if no exceptions apply.
   * @param date  the date to check, not null
   * @return true if the date is normally a working day
   */
  protected abstract boolean isNormallyWorkingDay(final LocalDate date);

  /**
   * Checks if the date is a non-working day, but would be considered a working day
   * by the {@code isNormallyWorkingDay} method.
   * @param date  the date to check, not null
   * @return true if the date is, unusually, a non-working day
   */
  protected boolean isWorkingDayException(final LocalDate date) {
    return false;
  }

  /**
   * Checks if the date is a working day, but would be considered a non-working day
   * by the {@code isNormallyWorkingDay} method.
   * @param date  the date to check, not null
   * @return true if the date is, unusually, a working day
   */
  protected boolean isNonWorkingDayException(final LocalDate date) {
    return false;
  }

}
