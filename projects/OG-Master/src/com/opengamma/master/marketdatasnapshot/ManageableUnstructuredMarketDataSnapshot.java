package com.opengamma.master.marketdatasnapshot;

import java.io.Serializable;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;

public class ManageableUnstructuredMarketDataSnapshot implements UnstructuredMarketDataSnapshot, Serializable {

  private Map<MarketDataValueSpecification, ValueSnapshot> _values;

  /**
   * Gets the values field.
   * @return the values
   */
  public Map<MarketDataValueSpecification, ValueSnapshot> getValues() {
    return _values;
  }

  /**
   * Sets the values field.
   * @param values  the values
   */
  public void setValues(Map<MarketDataValueSpecification, ValueSnapshot> values) {
    _values = values;
  }
  
  
}
