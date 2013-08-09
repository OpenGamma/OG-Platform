/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.swing;

import com.opengamma.id.UniqueId;

/**
 * An entry in a list for a given view definition.
 */
public final class ViewEntry {
  private final UniqueId _uniqueId;
  private final String _name;
  
  private ViewEntry(UniqueId uniqueId, String name) {
    _uniqueId = uniqueId;
    _name = name;
  }
  
  public static ViewEntry of(UniqueId uniqueId, String name) {
    return new ViewEntry(uniqueId, name);
  }
  
  public UniqueId getUniqueId() {
    return _uniqueId;
  }
  public String getName() {
    return _name;
  }
  
  public boolean equals(Object other) {
    if (!(other instanceof ViewEntry)) {
      return false;  
    } 
    ViewEntry o = (ViewEntry) other;
    if (!o.getName().equals(getName())) {
      return false;
    }
    return o.getUniqueId().equals(getUniqueId());
  }
  
  public int hashCode() {
    return _name.hashCode();
  }
}
