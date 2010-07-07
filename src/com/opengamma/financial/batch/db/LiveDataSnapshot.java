/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.ComputationTargetSpecification;

/**
 * 
 */
public class LiveDataSnapshot {
  
  private int _id;
  private ObservationDateTime _snapshotTime;
  private Set<LiveDataSnapshotEntry> _snapshotEntries = new HashSet<LiveDataSnapshotEntry>();
  private Map<ComputationTargetSpecification, Map<String, LiveDataSnapshotEntry>> _ct2FieldName2Entry = 
    new HashMap<ComputationTargetSpecification, Map<String, LiveDataSnapshotEntry>>();
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
    _ct2FieldName2Entry.clear();
    for (LiveDataSnapshotEntry entry : snapshotEntries) {
      buildIndex(entry);
    }
  }
  
  public void addEntry(LiveDataSnapshotEntry entry) {
    buildIndex(entry);
    _snapshotEntries.add(entry);
  }

  private void buildIndex(LiveDataSnapshotEntry entry) {
    Map<String, LiveDataSnapshotEntry> fieldName2Entry = _ct2FieldName2Entry.get(entry.getComputationTarget().toSpec());
    if (fieldName2Entry == null) {
      fieldName2Entry = new HashMap<String, LiveDataSnapshotEntry>();
      _ct2FieldName2Entry.put(entry.getComputationTarget().toSpec(), fieldName2Entry);
    }

    if (fieldName2Entry.get(entry.getField().getName()) != null) {
      throw new IllegalArgumentException("Already has entry for " + 
          entry.getComputationTarget().toSpec() + "/" + entry.getField().getName());
    }
    fieldName2Entry.put(entry.getField().getName(), entry);
  }
  
  public LiveDataSnapshotEntry getEntry(ComputationTargetSpecification computationTargetSpec, String fieldName) {
    Map<String, LiveDataSnapshotEntry> fieldName2Entry = _ct2FieldName2Entry.get(computationTargetSpec);
    if (fieldName2Entry == null) {
      return null;
    }
    return fieldName2Entry.get(fieldName);
  }
  
  public boolean isComplete() {
    return _complete;
  }
  
  public void setComplete(boolean complete) {
    _complete = complete;
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
    return new ToStringBuilder(this)
      .append("date", getSnapshotTime().getDate())
      .append("time", getSnapshotTime().getObservationTime().getLabel())
      .toString();
  }

}
