/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;

/**
 * Detail about when an exchange opens and closes.
 */
public class ExchangeCalendarEntry {

  private String _group; /* e.g. Certificates, Derivatives, Debt Market etc. */ // NULLABLE
  private String _product; /* e.g. Transferrable, Treasury Bond, Options on Wheat etc... */ // REQUIRED
  private String _type; /* e.g. Derivative, Cash, OTC/Block etc... */ // REQUIRED
  private String _code; /* e.g. AFB, encodes the product type in some circumstances, can be null */ // NULLAVBLE
  private LocalDate _calendarStart; // NULLABLE
  private LocalDate _calendarEnd; // NULLABLE
  private String _dayStart; // Days of week or "Last trading day" // REQUIRED
  private String _rangeType; // blank (no second day), through or to // NULLABLE
  private String _dayEnd; // Days of week // NULLABLE
  private String _phase; // Block registration, Pre-opening, Trading, Closing, Electronic Trading, Night trading, etc... // REQUIRED
  private LocalTime _phaseStarts; // start of phase if appropriate // NULLABLE
  private LocalTime _phaseEnds; // end of phase if appropriate // NULLABLE
  private LocalTime _randomStartMin; // NULLABLE
  private LocalTime _randomStartMax; // NULLABLE
  private LocalTime _randomEndMin; // NULLABLE
  private LocalTime _randomEndMax; // NULLABLE
  private LocalDate _lastConfirmed; // NULLABLE
  private String _notes; // NULLABLE
  private TimeZone _timeZone; // Time zone // REQUIRED

  /**
   * Creates an instance.
   *
   * @param group  the product group e.g. Main Market, Debt Market, Cash Markets, Foreign Currency etc. NULLABLE
   * @param product  the product type e.g. Trasury Bond, Options on Wheat, etc. REQUIRED
   * @param type  the type applies to e.g. Derivative, Cash, OTC/Block nearly required, but one example of blank, hence NULLABLE
   * @param code  optional code for type, can be null
   * @param calendarStart  start of period of validity, can be null
   * @param calendarEnd  end of period of validity, can be null
   * @param dayStart  day of week or 'Last trading day' REQUIRED
   * @param rangeType  'though' for inclusive or 'to' for exclusive or null for not a range
   * @param dayEnd  day of the week if a range, null otherwise
   * @param phase  phase of trading e.g. Pre-openging, Trading, Closing, Electronic Trading, Night Trading, etc... NULLABLE (a couple are blank)
   * @param phaseStarts  start of a phase if relevant to phase type or null otherwise
   * @param phaseEnds  end of a phase if relevant to phase type or null otherwise
   * @param randomStartMin  the minimum typical start, may be null
   * @param randomStartMax  the maximum typical start, may be null
   * @param randomEndMin  the minimum typical end, may be null
   * @param randomEndMax  the minimum typical end, may be null
   * @param lastConfirmed  the last confirmed date of the data, can be null
   * @param notes  textual notes, may be null
   * @param timeZone  the JSR-310 time-zone, not null
   */
  public ExchangeCalendarEntry(String group, String product, String type, String code, LocalDate calendarStart, LocalDate calendarEnd, 
                               String dayStart, String rangeType, String dayEnd, String phase, LocalTime phaseStarts, LocalTime phaseEnds, 
                               LocalTime randomStartMin, LocalTime randomStartMax, LocalTime randomEndMin, LocalTime randomEndMax, 
                               LocalDate lastConfirmed, String notes, TimeZone timeZone) {
    _group = group;
    _product = product;
    _type = type;
    _code = code;
    _calendarStart = calendarStart;
    _calendarEnd = calendarEnd;
    _dayStart = dayStart;
    _rangeType = rangeType;
    _dayEnd = dayEnd;
    _phase = phase;
    _phaseStarts = phaseStarts;
    _phaseEnds = phaseEnds;
    _randomStartMin = randomStartMin;
    _randomStartMax = randomStartMax;
    _randomEndMin = randomEndMin;
    _randomEndMax = randomEndMax;
    _lastConfirmed = lastConfirmed;
    _notes = notes;
    _timeZone = timeZone;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the group field.
   * @return the group
   */
  public String getGroup() {
    return _group;
  }

  /**
   * Gets the product field.
   * @return the product
   */
  public String getProduct() {
    return _product;
  }

  /**
   * Gets the type field.
   * @return the type
   */
  public String getType() {
    return _type;
  }

  /**
   * Gets the code field.
   * @return the code
   */
  public String getCode() {
    return _code;
  }

  /**
   * Gets the calendarStart field.
   * @return the calendarStart
   */
  public LocalDate getCalendarStart() {
    return _calendarStart;
  }

  /**
   * Gets the calendarEnd field.
   * @return the calendarEnd
   */
  public LocalDate getCalendarEnd() {
    return _calendarEnd;
  }

  /**
   * Gets the dayStart field.
   * @return the dayStart
   */
  public String getDayStart() {
    return _dayStart;
  }

  /**
   * Gets the rangeType field.
   * @return the rangeType
   */
  public String getRangeType() {
    return _rangeType;
  }

  /**
   * Gets the dayEnd field.
   * @return the dayEnd
   */
  public String getDayEnd() {
    return _dayEnd;
  }

  /**
   * Gets the phase field.
   * @return the phase
   */
  public String getPhase() {
    return _phase;
  }

  /**
   * Gets the phaseStarts field.
   * @return the phaseStarts
   */
  public LocalTime getPhaseStarts() {
    return _phaseStarts;
  }

  /**
   * Gets the phaseEnds field.
   * @return the phaseEnds
   */
  public LocalTime getPhaseEnds() {
    return _phaseEnds;
  }

  /**
   * Gets the randomStartMin field.
   * @return the randomStartMin
   */
  public LocalTime getRandomStartMin() {
    return _randomStartMin;
  }

  /**
   * Gets the randomStartMax field.
   * @return the randomStartMax
   */
  public LocalTime getRandomStartMax() {
    return _randomStartMax;
  }

  /**
   * Gets the randomEndMin field.
   * @return the randomEndMin
   */
  public LocalTime getRandomEndMin() {
    return _randomEndMin;
  }

  /**
   * Gets the randomEndMax field.
   * @return the randomEndMax
   */
  public LocalTime getRandomEndMax() {
    return _randomEndMax;
  }

  /**
   * Gets the lastConfirmed field.
   * @return the lastConfirmed
   */
  public LocalDate getLastConfirmed() {
    return _lastConfirmed;
  }

  /**
   * Gets the notes field.
   * @return the notes
   */
  public String getNotes() {
    return _notes;
  }

  /**
   * Gets the timeZone field.
   * @return the timeZone
   */
  public TimeZone getTimeZone() {
    return _timeZone;
  }

}
