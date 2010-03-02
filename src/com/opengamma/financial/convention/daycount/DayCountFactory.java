/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Factory for obtaining instances of a particular convention. Convention names are read from a
 * "DayCount" resource.
 * 
 * @author Andrew
 */
public class DayCountFactory {
  
  public static final DayCountFactory INSTANCE = new DayCountFactory ();
  
  private final Map<String,DayCount> _conventionMap = new HashMap<String,DayCount> ();
  
  private DayCountFactory () {
    final ResourceBundle conventions = ResourceBundle.getBundle ("com.opengamma.financial.convention.daycount.DayCount");
    final Map<String,DayCount> instances = new HashMap<String,DayCount> ();
    for (final String convention : conventions.keySet ()) {
      final String clazz = conventions.getString (convention);
      DayCount instance = instances.get (clazz);
      if (instance == null) {
        try {
          instances.put (clazz, instance = (DayCount)Class.forName (clazz).newInstance ());
        } catch (InstantiationException e) {
          throw new OpenGammaRuntimeException ("Error initialising DayCount conventions", e);
        } catch (IllegalAccessException e) {
          throw new OpenGammaRuntimeException ("Error initialising DayCount conventions", e);
        } catch (ClassNotFoundException e) {
          throw new OpenGammaRuntimeException ("Error initialising DayCount conventions", e);
        }
      }
      _conventionMap.put (convention.toLowerCase (), instance);
    }
  }
  
  /**
   * Returns a DayCount convention by symbolic name. Note that the lookup is not case sensitive.
   */
  public DayCount getDayCount (final String name) {
    return _conventionMap.get (name.toLowerCase ());
  }
  
}