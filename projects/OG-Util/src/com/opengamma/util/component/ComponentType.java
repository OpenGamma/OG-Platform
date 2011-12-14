/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

import java.io.Serializable;

import com.opengamma.util.ArgumentChecker;

/**
 * The type details of a component.
 * <p>
 * Components are defined in terms of an interface type. This can be assigned a
 * short name for URIs and other protocols.
 */
public final class ComponentType implements Serializable {

  /** Serialization version.*/
  private static final long serialVersionUID = 1L;

  /**
   * The type of the component, typically an interface.
   */
  private final Class<?> _type;
  /**
   * The type name of the component, typically used remotely.
   */
  private final String _name;

  /**
   * Obtains an instance.
   * 
   * @param type  the type of the component, typically an interface
   * @param name  the name of the type, typically used remotely
   * @return the component type, not null
   */
  public static ComponentType of(Class<?> type, String name) {
    return new ComponentType(type, name);
  }

  /**
   * Creates an instance.
   * 
   * @param type  the type of the component, typically an interface
   * @param name  the name of the type, typically used remotely
   */
  private ComponentType(Class<?> type, String name) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(name, "name");
    _type = type;
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the type representing the available functionality.
   * <p>
   * This is normally an interface type.
   * 
   * @return the type defining the functionality, not null
   */
  public Class<?> getType() {
    return _type;
  }

  /**
   * Gets the type name of the component for URIs and other protocols.
   * 
   * @return the type name, not null
   */
  public String getTypeName() {
    return _name;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ComponentType) {
      ComponentType other = (ComponentType) obj;
      return _type.equals(other._type) && _name.equals(other._name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _type.hashCode() ^ _name.hashCode();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "ComponentType[" + _type + "]";
  }

}
