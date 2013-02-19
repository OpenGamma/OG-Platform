/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.io.Serializable;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.id.ExternalIdBundle;

/**
 * Mutable snapshot of market data.
 */
public class ManageableUnstructuredMarketDataSnapshot implements UnstructuredMarketDataSnapshot, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The values.
   */
  private Map<ExternalIdBundle, Map<String, ValueSnapshot>> _values;

  /**
   * Gets the values.
   * 
   * @return the values
   */
  @Override
  public Map<ExternalIdBundle, Map<String, ValueSnapshot>> getValues() {
    return _values;
  }

  /**
   * Sets the values.
   * 
   * @param values the values
   */
  public void setValues(final Map<ExternalIdBundle, Map<String, ValueSnapshot>> values) {
    _values = values;
  }

}
