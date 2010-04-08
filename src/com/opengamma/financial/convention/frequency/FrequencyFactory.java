/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 * @author Andrew
 */
public class FrequencyFactory {
  
  public static final FrequencyFactory INSTANCE = new FrequencyFactory ();
  
  private final Map<String,Frequency> _conventionMap = new HashMap<String,Frequency> ();
  
  private FrequencyFactory () {
    store (SimpleFrequency.MONTHLY);
    store (SimpleFrequency.ANNUAL);
    store (SimpleFrequency.SEMI_ANNUAL);
    store (SimpleFrequency.QUARTERLY);
  }
  
  private void store (final Frequency freq) {
    _conventionMap.put (freq.getConventionName ().toLowerCase (), freq);
  }
  
  public Frequency getFrequency (final String name) {
    return _conventionMap.get (name.toLowerCase ());
  }
  
}