/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.ComputationTargetSpecification;

/**
 * Hibernate bean.
 */
public class LiveDataSnapshot {
  
  private int _id;
  private ObservationDateTime _snapshotTime;
  private Set<LiveDataSnapshotEntry> _snapshotEntries = new HashSet<LiveDataSnapshotEntry>();
  private Map<ComputationTargetSpecification, Map<String, LiveDataSnapshotEntry>> _ct2FieldName2Entry; 
    
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
    _ct2FieldName2Entry = null;
  }
  
  public void addEntry(LiveDataSnapshotEntry entry) {
    _snapshotEntries.add(entry);
    buildIndex(entry);
  }

  private void buildIndex(LiveDataSnapshotEntry entry) {
    if (_ct2FieldName2Entry == null) {
      _ct2FieldName2Entry = new HashMap<ComputationTargetSpecification, Map<String, LiveDataSnapshotEntry>>();
    }

    Map<String, LiveDataSnapshotEntry> fieldName2Entry = _ct2FieldName2Entry.get(entry.getComputationTarget().toComputationTargetSpec());
    if (fieldName2Entry == null) {
      fieldName2Entry = new HashMap<String, LiveDataSnapshotEntry>();
      _ct2FieldName2Entry.put(entry.getComputationTarget().toComputationTargetSpec(), fieldName2Entry);
    }

    if (fieldName2Entry.get(entry.getField().getName()) != null) {
      throw new IllegalArgumentException("Already has entry for " + 
          entry.getComputationTarget().toComputationTargetSpec() + "/" + entry.getField().getName());
    }
    fieldName2Entry.put(entry.getField().getName(), entry);
  }
  
  public LiveDataSnapshotEntry getEntry(ComputationTargetSpecification computationTargetSpec, String fieldName) {
    if (_ct2FieldName2Entry == null) {
      _ct2FieldName2Entry = new HashMap<ComputationTargetSpecification, Map<String, LiveDataSnapshotEntry>>();
      for (LiveDataSnapshotEntry entry : getSnapshotEntries()) {
        buildIndex(entry);
      }
    }

    Map<String, LiveDataSnapshotEntry> fieldName2Entry = _ct2FieldName2Entry.get(computationTargetSpec);
    if (fieldName2Entry == null) {
      return null;
    }
    return fieldName2Entry.get(fieldName);
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
