/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch.db;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.batch.LiveDataValue;

/**
 * 
 */
public class LiveDataSnapshotEntry {
  
  private long _id;
  private LiveDataSnapshot _snapshot;
  private ComputationTarget _computationTarget;
  private LiveDataField _field;
  private double _value;
  
  public long getId() {
    return _id;
  }
  
  public void setId(long id) {
    _id = id;
  }
  
  public LiveDataSnapshot getSnapshot() {
    return _snapshot;
  }
  
  public void setSnapshot(LiveDataSnapshot snapshot) {
    _snapshot = snapshot;
  }
  
  public ComputationTarget getComputationTarget() {
    return _computationTarget;
  }

  public void setComputationTarget(ComputationTarget computationTarget) {
    _computationTarget = computationTarget;
  }

  public LiveDataField getField() {
    return _field;
  }
  
  public void setField(LiveDataField field) {
    _field = field;
  }
  
  public double getValue() {
    return _value;
  }
  
  public void setValue(double value) {
    _value = value;
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
  
  public LiveDataValue toLiveDataValue() {
    return new LiveDataValue(
        getComputationTarget().toSpec(),
        getField().getName(),
        getValue());
  }
  
}
