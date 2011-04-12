/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.util.ArgumentChecker;

/**
 * Uniquely identifies a batch in the batch database.  
 */
public class BatchId {
  
  /**
   * @return The date of the batch, not null
   */
  private final LocalDate _observationDate;
  
  /**
   * @return The time of the batch (e.g., LDN_CLOSE), not null
   */
  private final String _observationTime;
  
  public BatchId(LocalDate observationDate,
      String observationTime) {
    ArgumentChecker.notNull(observationDate, "Observation date");
    ArgumentChecker.notNull(observationTime, "Observation time");
    
    _observationDate = observationDate;
    _observationTime = observationTime;
  }
 
  public LocalDate getObservationDate() {
    return _observationDate;
  }
  
  public String getObservationTime() {
    return _observationTime;
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
    return getObservationDate() + "/" + getObservationTime();
  }

}
