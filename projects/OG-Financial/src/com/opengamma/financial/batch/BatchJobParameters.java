/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.CalendricalParseException;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Batch job parameters.
 * <p> 
 * The parameters are information that does not vary from day to day and
 * can therefore be stored in a configuration database.
 */
public class BatchJobParameters {
  
  /**
   * Used as a default "observation time" for ad hoc batches, i.e., batches that are
   * started manually by users and whose results should NOT flow to downstream
   * systems.  
   */
  public static final String AD_HOC_OBSERVATION_TIME = "AD_HOC_RUN";

  // --------------------------------------------------------------------------

  /** yyyyMMddHHmmss[Z] */
  private static final DateTimeFormatter s_dateTimeFormatter;

  /** yyyyMMdd */
  private static final DateTimeFormatter s_dateFormatter;

  static {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyyMMddHHmmss[Z]");
    s_dateTimeFormatter = builder.toFormatter();

    builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyyMMdd");
    s_dateFormatter = builder.toFormatter();
  }

  /* package */static OffsetDateTime parseDateTime(String dateTime) {
    if (dateTime == null) {
      return null;
    }
    try {
      // try first to parse as if time zone explicitly provided, e.g., 20100621162200+0000
      return s_dateTimeFormatter.parse(dateTime, OffsetDateTime.rule());
    } catch (CalendricalParseException e) {
      // try to parse as if no time zone provided, e.g. 20100621162200. Use the system time zone.
      LocalDateTime localDateTime = s_dateTimeFormatter.parse(dateTime, LocalDateTime.rule());
      return OffsetDateTime.of(localDateTime, ZonedDateTime.now().toOffsetDateTime().getOffset());
    }
  }
  
  /* package */static String formatDateTime(OffsetDateTime dateTime) {
    return s_dateTimeFormatter.print(dateTime);   
  }

  /* package */static LocalDate parseDate(String date) {
    return s_dateFormatter.parse(date, LocalDate.rule());
  }
  
  /* package */static String formatDate(LocalDate date) {
    return s_dateFormatter.print(date);   
  }

  /* package */static LocalTime parseTime(String time) {
    return LocalTime.parse(time);
  }
  
  /* package */static String formatTime(LocalTime time) {
    return time.toString();   
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * Spring config file defining BatchJob 
   */
  private String _springXml;
  
  /** 
   * Why the batch is being run. Would typically tell whether the run is an automatic/manual
   * run, and if manual, who started it and maybe why.
   */
  private String _reason;
  
  /** 
   * A label for the run. Examples: LDN_CLOSE, AD_HOC_RUN. The exact time of LDN_CLOSE could vary
   * daily due to this time being set by the head trader.
   * So one day it might be 16:32, the next 16:46, etc. 
   */
  private String _observationTime;
  
  /**
   * The batch will run against a defined set of market data.
   * <p> 
   * This variable tells which set exactly. The contents are 
   * similar to {@link #_observationTime}.
   */
  private String _snapshotObservationTime;
  
  /**
   * Valuation time for purposes of calculating all risk figures. Often referred to as 'T'
   * in mathematical formulas. Here, this is just a time, not a datetime.
   */
  private String _valuationTime;

  /**
   * This view name references the OpenGamma configuration database.
   * The view will define the portfolio of trades the batch should be run for.
   */
  private String _view;

  /**
   * Historical time used for loading entities out of Config DB.
   * Here, this is just a time, not a datetime.
   */
  private String _configDbTime;
  
  /**
   * Historical time used for loading entities out of PositionMaster,
   * SecurityMaster, etc. Here, this is just a time, not a datetime.
   */
  private String _staticDataTime;
  
  /**
   * Time zone for the user-provided times.
   */
  private String _timeZone;
  
  // --------------------------------------------------------------------------
  
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
  
  public LocalTime getValuationTimeObject() {
    return parseTime(getValuationTime());
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

  public LocalTime getStaticDataTimeObject() {
    return parseTime(getStaticDataTime());
  }
  
  public String getConfigDbTime() {
    return _configDbTime;
  }
  
  public LocalTime getConfigDbTimeObject() {
    return parseTime(getConfigDbTime());
  }

  public void setConfigDbTime(String configDbTime) {
    _configDbTime = configDbTime;
  }

  public TimeZone getTimeZoneObject() {
    return TimeZone.of(getTimeZone());    
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
  
  public void initializeDefaults(BatchJob batchJob) {
    _reason = "Manual run started on " + 
        batchJob.getCreationTime() + " by " + 
        batchJob.getUser().getUserName();                   
    
    _observationTime = AD_HOC_OBSERVATION_TIME;      

    _valuationTime = formatTime(batchJob.getCreationTime().toLocalTime()); 
    
    _configDbTime = _valuationTime;
    
    _staticDataTime = _valuationTime;
    
    _timeZone = batchJob.getCreationTime().getZone().toString();  
  }
  
  public void initialize(BatchJobParameters another) {
    Map<String, String> parameters = another.getParameters();
    initialize(parameters);        
  }
  
  /**
   * Fills in parameters from a parameter map.
   * 
   * @param parameters the parameter map. Keys of the map must
   * match field names of this class.
   */
  public void initialize(Map<String, String> parameters) {
    Field[] fields = getClass().getDeclaredFields();
    for (Field field : fields) {
      if (!Modifier.isStatic(field.getModifiers())) {
        String fieldName = field.getName();
        if (fieldName.startsWith("_")) {
          fieldName = fieldName.substring(1);
        }
        String value = parameters.get(fieldName);
        if (value == null) {
          continue;
        }
        
        try {
          field.set(this, value);
        } catch (IllegalAccessException e) {
          throw new RuntimeException("Unexpected IllegalAccessException", e);
        }
      }
    }
  }
  
  /**
   * Gets the parameters as a map.
   * 
   * @return the map. Keys of the map match field names of this class.
   * If a field is null, it will not be in the map.
   */
  public Map<String, String> getParameters() {
    Map<String, String> returnValue = new HashMap<String, String>();
    
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
            returnValue.put(fieldName, fieldValue.toString());
          }
        } catch (IllegalAccessException e) {
          throw new RuntimeException("Unexpected IllegalAccessException", e);
        }
      }
    }
    
    return returnValue;
  }
  
  /**
   * Validates that all necessary parameters have been correctly set.
   * 
   * @throws IllegalStateException if not all necessary parameters have been set
   * or if they have been set in an invalid format. 
   */
  public void validate() {
    
    if (_springXml == null) {
      throw new IllegalStateException("Please specify Spring XML file.");
    }
    
    if (_view == null) {
      throw new IllegalStateException("Please specify view name.");
    }
    
    try {
      parseTime(_valuationTime);
    } catch (CalendricalParseException e) {
      throw new IllegalStateException("Valuation time " + _valuationTime + " is not in valid format", e);
    }
    
    try {
      parseTime(_configDbTime);
    } catch (CalendricalParseException e) {
      throw new IllegalStateException("View time " + _configDbTime + " is not in valid format", e);
    }
    
    try {
      parseTime(_staticDataTime);
    } catch (CalendricalParseException e) {
      throw new IllegalStateException("Entity time " + _staticDataTime + " is not in valid format", e);
    }
    
    try {
      getTimeZoneObject();
    } catch (CalendricalException e) {
      throw new IllegalStateException("Time zone ID " + getTimeZone() + " is not a valid ID", e);
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
