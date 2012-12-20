/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.io.Serializable;

import com.opengamma.util.ArgumentChecker;

/**
 * The key used to identify the component.
 * <p>
 * Components are defined in terms of a type and classifier.
 */
public final class ComponentKey implements Serializable {

  /** Serialization version.*/
  private static final long serialVersionUID = 1L;

  /**
   * The component type representing the available functionality.
   */
  private Class<?> _type;
  /**
   * The classifier of the type.
   * This acts as a key to disambiguate multiple options for the same component type.
   */
  private String _classifier;

  /**
   * Obtains an instance.
   * 
   * @param type  the type of the component, typically an interface
   * @param classifier  the classifier of the type, used to name instances of the same type
   * @return the component type, not null
   */
  public static ComponentKey of(Class<?> type, String classifier) {
    return new ComponentKey(type, classifier);
  }

  /**
   * Creates an instance.
   * 
   * @param type  the type of the component, typically an interface
   * @param classifier  the classifier of the type, used to name instances of the same type
   */
  private ComponentKey(Class<?> type, String classifier) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(classifier, "classifier");
    _type = type;
    _classifier = classifier;
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
   * Gets the classifier of the type, used to name instances of the same type.
   * 
   * @return the classifier, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ComponentKey) {
      ComponentKey other = (ComponentKey) obj;
      return _type.equals(other._type) && _classifier.equals(other._classifier);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _type.hashCode() ^ _classifier.hashCode();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return _type.getSimpleName() + "::" + _classifier;
  }

}
