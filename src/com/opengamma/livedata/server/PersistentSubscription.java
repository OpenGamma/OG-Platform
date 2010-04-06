/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.io.Serializable;

import com.opengamma.util.ArgumentChecker;

/**
 * A market data subscription that should survive server restarts.
 *
 * @author pietari
 */
public class PersistentSubscription implements Serializable {
  
  /** A unique ID for the market data. Server type specific (Bloomberg unique ID, RIC, ...). */
  private String _id;
  
  protected PersistentSubscription() {
  }
  
  public PersistentSubscription(String id) {
    ArgumentChecker.checkNotNull(id, "ID");
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public void setId(String id) {
    _id = id;
  }
  
  public String toString() {
    return _id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_id == null) ? 0 : _id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PersistentSubscription other = (PersistentSubscription) obj;
    if (_id == null) {
      if (other._id != null)
        return false;
    } else if (!_id.equals(other._id))
      return false;
    return true;
  }
  
}
