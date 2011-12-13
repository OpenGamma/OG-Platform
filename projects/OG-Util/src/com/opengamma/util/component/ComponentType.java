/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

/**
 * The type details of a component.
 * <p>
 * Components are defined in terms of an interface type. This can be assigned a
 * short name for URIs and other protocols.
 */
public interface ComponentType {

  /**
   * Gets the type representing the available functionality.
   * <p>
   * This is normally an interface type.
   * 
   * @return the type defining the functionality, not null
   */
  Class<?> getType();

  /**
   * Gets the type name of the component for URIs and other protocols.
   * 
   * @return the type name, not null
   */
  String getTypeName();

}
