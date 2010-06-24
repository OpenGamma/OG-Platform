/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch.db;

import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 */
public class LiveDataSnapshot {
  
  private int _id;
  private ObservationDateTime _snapshotTime;
  private Set<LiveDataSnapshotEntry> _snapshotEntries;
  private boolean _complete;
  
  public int getId() {
    return _id;
  }
  
  public void setId(int id) {
    _id = id;
  }
  
  public ObservationDateTime getSnapshotTime() {
    return _snapshotTime;
  }
  
  public void setSnapshotTime(ObservationDateTime snapshotTime) {
    _snapshotTime = snapshotTime;
  }
  
  public Set<LiveDataSnapshotEntry> getSnapshotEntries() {
    return _snapshotEntries;
  }
  
  public void setSnapshotEntries(Set<LiveDataSnapshotEntry> snapshotEntries) {
    _snapshotEntries = snapshotEntries;
  }
  
  public boolean isComplete() {
    return _complete;
  }
  
  public void setComplete(boolean complete) {
    _complete = complete;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(_id).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    LiveDataSnapshot rhs = (LiveDataSnapshot) obj;
    return new EqualsBuilder().append(_id, rhs._id).isEquals();
  }
  
  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("date", getSnapshotTime().getDate())
      .append("time", getSnapshotTime().getObservationTime().getLabel())
      .toString();
  }

}
