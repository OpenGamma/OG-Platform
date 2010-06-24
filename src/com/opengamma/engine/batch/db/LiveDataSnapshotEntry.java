/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch.db;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.id.Identifier;

/**
 * 
 */
public class LiveDataSnapshotEntry {
  
  private long _id;
  private LiveDataSnapshot _snapshot;
  private Identifier _identifier;
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
  
  public Identifier getIdentifier() {
    return _identifier;
  }
  
  public void setIdentifier(Identifier identifier) {
    _identifier = identifier;
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
    LiveDataSnapshotEntry rhs = (LiveDataSnapshotEntry) obj;
    return new EqualsBuilder().append(_id, rhs._id).isEquals();
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  
}
