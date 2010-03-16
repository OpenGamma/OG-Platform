/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Factory for obtaining instances of a particular convention. Convention names are read from a
 * "BusinessDayConvention" resource.
 * 
 * @author Andrew
 */
public class BusinessDayConventionFactory {
  
  public static final BusinessDayConventionFactory INSTANCE = new BusinessDayConventionFactory ();
  
  private final Map<String,BusinessDayConvention> _conventionMap = new HashMap<String,BusinessDayConvention> ();
  
  private BusinessDayConventionFactory () {
    final ResourceBundle conventions = ResourceBundle.getBundle (BusinessDayConvention.class.getName ());
    final Map<String,BusinessDayConvention> instances = new HashMap<String,BusinessDayConvention> ();
    for (final String convention : conventions.keySet ()) {
      final String clazz = conventions.getString (convention);
      BusinessDayConvention instance = instances.get (clazz);
      if (instance == null) {
        try {
          instances.put (clazz, instance = (BusinessDayConvention)Class.forName (clazz).newInstance ());
        } catch (InstantiationException e) {
          throw new OpenGammaRuntimeException ("Error initialising BusinessDay conventions", e);
        } catch (IllegalAccessException e) {
          throw new OpenGammaRuntimeException ("Error initialising BusinessDay conventions", e);
        } catch (ClassNotFoundException e) {
          throw new OpenGammaRuntimeException ("Error initialising BusinessDay conventions", e);
        }
      }
      _conventionMap.put (convention.toLowerCase (), instance);
    }
  }
  
  /**
   * Retrieves a named BusinessDayConvention. Note that the lookup is not case sensitive.
   */
  public BusinessDayConvention getBusinessDayConvention (final String name) {
    return _conventionMap.get (name.toLowerCase ());
  }
  
}