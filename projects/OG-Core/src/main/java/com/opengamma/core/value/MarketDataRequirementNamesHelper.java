/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.value;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Utility methods to simplify managing market data requirement names.
 * <p>
 * This is a static thread-safe utility class.
 */
/**
 * Constructs a list of all valid {@link MarketDataRequirementNames}.
 */
public class MarketDataRequirementNamesHelper {

  /**
   * Constructs a list of all valid {@link MarketDataRequirementNames}.
   * 
   * @return a list of all valid {@link MarketDataRequirementNames}.
   */
  public static Set<String> constructValidRequirementNames() {
    Set<String> result = new HashSet<String>();
    
    // All fields are implicitly public static final
    assert MarketDataRequirementNames.class.isInterface();
    
    try {
      for (Field field : MarketDataRequirementNames.class.getFields()) {
        if (String.class.equals(field.getType())) {
          result.add((String) field.get(null));
        }
      }
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Error querying fields of " + MarketDataRequirementNames.class);
    }
    return result;
  }

}
