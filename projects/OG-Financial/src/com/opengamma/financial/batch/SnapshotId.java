/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.util.ArgumentChecker;

/**
 * Identifies a LiveData snapshot in the batch database.  
 */
public class SnapshotId {
  
  /**
   * @return The date of the snapshot, not null
   */
  private final LocalDate _observationDate;
  
  /**
   * @return The time of the snapshot (e.g., LDN_CLOSE), not null
   */
  private final String _observationTime;
  
  public SnapshotId(LocalDate observationDate,
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
