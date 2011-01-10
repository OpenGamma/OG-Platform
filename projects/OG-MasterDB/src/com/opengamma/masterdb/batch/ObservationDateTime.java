/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.sql.Date;
import java.sql.Time;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 */
public class ObservationDateTime {
  
  private int _id;
  private Date _date;
  private Time _time;
  private ObservationTime _observationTime;
  
  public int getId() {
    return _id;
  }
  
  public void setId(int id) {
    _id = id;
  }
  
  public Date getDate() {
    return _date;
  }
  
  public void setDate(Date date) {
    _date = date;
  }
  
  public Time getTime() {
    return _time;
  }
  
  public void setTime(Time time) {
    _time = time;
  }
  
  public ObservationTime getObservationTime() {
    return _observationTime;
  }
  
  public void setObservationTime(ObservationTime observationTime) {
    _observationTime = observationTime;
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
