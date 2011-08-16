/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.io.Serializable;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;

public class ManageableUnstructuredMarketDataSnapshot implements UnstructuredMarketDataSnapshot, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  private Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> _values;

  /**
   * Gets the values field.
   * @return the values
   */
  public Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> getValues() {
    return _values;
  }

  /**
   * Sets the values field.
   * @param values  the values
   */
  public void setValues(Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> values) {
    _values = values;
  }
  
  
}
