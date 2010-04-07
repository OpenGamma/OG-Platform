/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.yield;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 * @author Andrew
 */
public class YieldConventionFactory {
  
  public static final YieldConventionFactory INSTANCE = new YieldConventionFactory ();
  
  private final Map<String,YieldConvention> _conventionMap = new HashMap<String,YieldConvention> ();
  
  private YieldConventionFactory () {
    store (SimpleYieldConvention.US_STREET);
  }
  
  private void store (final YieldConvention yieldConvention) {
    _conventionMap.put (yieldConvention.getConventionName ().toLowerCase (), yieldConvention);
  }
  
  public YieldConvention getYieldConvention (final String name) {
    return _conventionMap.get (name.toLowerCase ());
  }
  
}