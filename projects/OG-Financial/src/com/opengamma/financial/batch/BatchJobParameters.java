/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.time.CalendricalException;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.format.CalendricalParseException;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.beans.PropertyDefinition;

/**
 * Configuration parameters for running a batch job.
 * <p> 
 * These parameters are information that does not typically vary from day to day
 * and can therefore be stored in a configuration database.
 * <p>
 * This class is mutable and not thread-safe.
 */
public class BatchJobParameters {

  /**
   * Used as a default "observation time" for ad hoc batches, i.e., batches that are
   * started manually by users and whose results should NOT flow to downstream systems.  
   */
  public static final String AD_HOC_OBSERVATION_TIME = "AD_HOC_RUN";
  /**
   * Date-time format: yyyyMMddHHmmss[Z]
   */
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatters.pattern("yyyyMMddHHmmss[Z]");
  /**
   * Date-time format: yyyyMMdd
   */
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatters.pattern("yyyyMMdd");

  /* package */static OffsetDateTime parseDateTime(String dateTimeStr) {
    if (dateTimeStr == null) {
      return null;
    }
    // TODO: JSR-310, parse a or b
    try {
      // try first to parse as if offset explicitly provided, e.g., 20100621162200+0000
      return OffsetDateTime.parse(dateTimeStr,  DATE_TIME_FORMATTER);
    } catch (CalendricalParseException e) {
      // try to parse as if no offset provided, e.g. 20100621162200. Use the system time zone.
      LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
      return localDateTime.atOffset(OffsetDateTime.now().getOffset());
    }
  }

  /* package */static String formatDateTime(OffsetDateTime dateTime) {
    return dateTime.toString(DATE_TIME_FORMATTER);
  }

  /* package */static LocalDate parseDate(String date) {
    return LocalDate.parse(date, DATE_FORMATTER);
  }

  /* package */static String formatDate(LocalDate date) {
    return DATE_FORMATTER.print(date);   
  }

  /* package */static LocalTime parseTime(String time) {
    return LocalTime.parse(time);
  }

  /* package */static String formatTime(LocalTime time) {
    return time.toString();   
  }

  //-------------------------------------------------------------------------
  // private field names are exposed in #getParameters()
  /**
   * The Spring config file defining BatchJob.
   */
  @PropertyDefinition
  private String _springXml;
  /**
   * The reason why the batch is being run.
   * Would typically tell whether the run is an automatic/manual run, and if
   * manual, who started it and maybe why.
   */
  @PropertyDefinition
  private String _reason;
  /**
   * The observation time key, such as LDN_CLOSE or AD_HOC_RUN.
   * The exact time of LDN_CLOSE could vary daily due to this time being set by the head trader.
   * So one day it might be 16:32, the next 16:46, etc. 
   */
  @PropertyDefinition
  private String _observationTime;
  /**
   * The batch will run against a defined set of market data.
   * <p> 
   * This variable tells which set exactly.
   * The contents are similar to {@link #_observationTime}.
   */
  @PropertyDefinition
  private String _snapshotObservationTime;
  /**
   * The valuation time for purposes of calculating all risk figures.
   * Often referred to as 'T' in mathematical formulas.
   * Here, this is just a time, not a date-time.
   */
  @PropertyDefinition
  private String _valuationTime;
  /**
   * The view name referencing the OpenGamma configuration database.
   * The view will define the portfolio of trades the batch should be run for.
   */
  @PropertyDefinition
  private String _view;
  /**
   * The historical time used for loading entities out of Config DB.
   * Here, this is just a time, not a date-time.
   */
  @PropertyDefinition
  private String _configDbTime;
  /**
   * The historical time used for loading entities out of PositionMaster,
   * SecurityMaster, etc. Here, this is just a time, not a date-time.
   */
  @PropertyDefinition
  private String _staticDataTime;
  /**
   * The time-zone id for the user-provided times.
   */
  @PropertyDefinition
  private String _timeZone;

  //-------------------------------------------------------------------------
  public LocalTime getValuationTimeObject() {
    return parseTime(getValuationTime());
  }

  public LocalTime getStaticDataTimeObject() {
    return parseTime(getStaticDataTime());
  }

  public LocalTime getConfigDbTimeObject() {
    return parseTime(getConfigDbTime());
  }

  public TimeZone getTimeZoneObject() {
    return TimeZone.of(getTimeZone());    
  }

  //-------------------------------------------------------------------------
  public String getSpringXml() {
    return _springXml;
  }

  public void setSpringXml(String springXml) {
    _springXml = springXml;
  }

  public String getRunReason() {
    return _reason;
  }

  public void setRunReason(String runReason) {
    _reason = runReason;
  }
  
  public String getObservationTime() {
    return _observationTime;
  }

  public void setObservationTime(String observationTime) {
    _observationTime = observationTime;
  }
  
  public String getValuationTime() {
    return _valuationTime;
  }
  
  public void setValuationTime(String valuationTime) {
    _valuationTime = valuationTime;
  }

  public String getViewName() {
    return _view;
  }

  public void setViewName(String viewName) {
    _view = viewName;
  }

  public String getViewTime() {
    return _configDbTime;
  }
  
  public void setViewTime(String viewDateTime) {
    _configDbTime = viewDateTime;
  }
  
  public String getStaticDataTime() {
    return _staticDataTime;
  }
  
  public void setStaticDataTime(String staticDataTime) {
    _staticDataTime = staticDataTime;
  }

  public String getConfigDbTime() {
    return _configDbTime;
  }
  
  public void setConfigDbTime(String configDbTime) {
    _configDbTime = configDbTime;
  }

  public String getTimeZone() {
    return _timeZone;
  }

  public void setTimeZone(String timeZone) {
    _timeZone = timeZone;
  }

  public void setDataMasterTime(String dataMasterTime) {
    _staticDataTime = dataMasterTime;
  }

  public String getSnapshotObservationTime() {
    if (_snapshotObservationTime == null) {
      return getObservationTime();
    }
    return _snapshotObservationTime;
  }
  
  public void setSnapshotObservationTime(String snapshotObservationTime) {
    _snapshotObservationTime = snapshotObservationTime;
  }
  
  // --------------------------------------------------------------------------
  
  public void initializeDefaults(CommandLineBatchJob batchJob) {
    _reason = "Manual run started on " + 
        batchJob.getCreationTime() + " by " + 
        batchJob.getUser().getUserName();                   
    
    _observationTime = AD_HOC_OBSERVATION_TIME;      

    _valuationTime = formatTime(batchJob.getCreationTime().toLocalTime()); 
    
    _configDbTime = _valuationTime;
    
    _staticDataTime = _valuationTime;
    
    _timeZone = batchJob.getCreationTime().getZone().toString();  
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes this set of parameters from another set.
   * 
   * @param other  the other set of parameters, not null
   */
  public void initialize(BatchJobParameters other) {
    Map<String, String> parameters = other.getParameters();
    initialize(parameters);        
  }

  /**
   * Initializes this set of parameters from another set.
   * <p>
   * The mapping is performed based on field names.
   * 
   * @param other  the other set of parameters, not null
   */
  public void initialize(Map<String, String> other) {
    Field[] fields = getClass().getDeclaredFields();
    for (Field field : fields) {
      if (!Modifier.isStatic(field.getModifiers())) {
        String fieldName = field.getName();
        if (fieldName.startsWith("_")) {
          fieldName = fieldName.substring(1);
        }
        String value = other.get(fieldName);
        if (value == null) {
          continue;
        }
        try {
          field.set(this, value);
        } catch (IllegalAccessException ex) {
          throw new RuntimeException("Unexpected IllegalAccessException", ex);
        }
      }
    }
  }

  /**
   * Gets the parameters as a map.
   * 
   * @return the parameter map, defined by the field names of this class.
   *  If a field is null, it will not be in the map.
   */
  public Map<String, String> getParameters() {
    Map<String, String> result = new HashMap<String, String>();
    Field[] fields = getClass().getDeclaredFields();
    for (Field field : fields) {
      if (!Modifier.isStatic(field.getModifiers())) {
        try {
          Object fieldValue = field.get(this);
          if (fieldValue != null) {
            String fieldName = field.getName();
            if (fieldName.startsWith("_")) {
              fieldName = fieldName.substring(1);
            }
            result.put(fieldName, fieldValue.toString());
          }
        } catch (IllegalAccessException ex) {
          throw new RuntimeException("Unexpected IllegalAccessException", ex);
        }
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Validates that all necessary parameters have been correctly set.
   * 
   * @throws IllegalStateException if not all necessary parameters have been set
   *  or if they have been set in an invalid format
   */
  public void validate() {
    if (_springXml == null) {
      throw new IllegalStateException("The Spring XML file must not be null");
    }
    if (_view == null) {
      throw new IllegalStateException("The view name must not be null");
    }
    try {
      parseTime(_valuationTime);
    } catch (CalendricalParseException ex) {
      throw new IllegalStateException("The valuation time " + _valuationTime + " is not in valid format", ex);
    }
    try {
      parseTime(_configDbTime);
    } catch (CalendricalParseException ex) {
      throw new IllegalStateException("The view time " + _configDbTime + " is not in valid format", ex);
    }
    try {
      parseTime(_staticDataTime);
    } catch (CalendricalParseException ex) {
      throw new IllegalStateException("The entity time " + _staticDataTime + " is not in valid format", ex);
    }
    try {
      getTimeZoneObject();
    } catch (CalendricalException ex) {
      throw new IllegalStateException("The time-zone ID " + getTimeZone() + " is not a valid ID", ex);
    }
  }
  
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }


}
