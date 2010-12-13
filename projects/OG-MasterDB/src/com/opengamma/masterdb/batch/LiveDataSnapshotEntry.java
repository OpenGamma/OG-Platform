/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.financial.batch.LiveDataValue;

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
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  
  public LiveDataValue toLiveDataValue() {
    return new LiveDataValue(
        getComputationTarget().toComputationTargetSpec(),
        getField().getName(),
        getValue());
  }
  
}
