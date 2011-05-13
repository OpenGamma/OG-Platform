/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import javax.time.calendar.LocalDate;

import com.opengamma.util.ArgumentChecker;

/**
 * An id for a batch in the batch database.
 * <p>
 * This class is immutable and thread-safe.
 */
public class BatchId {

  /**
   * The date of the batch.
   */
  private final LocalDate _observationDate;
  /**
   * The descriptive time of the batch, such as LDN_CLOSE.
   */
  private final String _observationTime;

  /**
   * Creates an instance.
   * 
   * @param observationDate  the observation date, not null
   * @param observationTimeKey  the descriptive time key, not null
   */
  public BatchId(LocalDate observationDate, String observationTimeKey) {
    ArgumentChecker.notNull(observationDate, "observationDate");
    ArgumentChecker.notNull(observationTimeKey, "observationTimeKey");
    _observationDate = observationDate;
    _observationTime = observationTimeKey;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the observation date.
   * 
   * @return the date of the batch, not null
   */
  public LocalDate getObservationDate() {
    return _observationDate;
  }

  /**
   * Gets the descriptive observation time key, such as LDN_CLOSE.
   * 
   * @return the descriptive time key, not null
   */
  public String getObservationTime() {
    return _observationTime;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof SnapshotId) {
      SnapshotId other = (SnapshotId) obj;
      return getObservationDate().equals(other.getObservationDate()) &&
          getObservationTime().equals(other.getObservationTime());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getObservationDate().hashCode() ^ getObservationTime().hashCode();
  }

  @Override
  public String toString() {
    return getObservationDate() + "/" + getObservationTime();
  }

}
