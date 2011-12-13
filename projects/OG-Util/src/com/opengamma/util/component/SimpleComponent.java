/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

/**
 * A principal component of the OpenGamma system.
 */
public final class SimpleComponent implements Component {

  /**
   * The underlying service object.
   */
  private final Object _underlying;
  /**
   * The type that the underlying service object exposes.
   */
  private final Class<?> _type;
  /**
   * The name used for the type in communications.
   */
  private final String _typeName;
  /**
   * The classifier of the type.
   */
  private final String _classifier;

  /**
   * Creates an instance.
   * 
   * @param underlying
   * @param type
   * @param typeName
   * @param classifier
   */
  public SimpleComponent(Object underlying, Class<?> type, String typeName, String classifier) {
    _underlying = underlying;
    _type = type;
    _typeName = typeName;
    _classifier = classifier;
  }

  //-------------------------------------------------------------------------
  @Override
  public Object getUnderlying() {
    return _underlying;
  }

  @Override
  public Class<?> getType() {
    return _type;
  }

  @Override
  public String getTypeName() {
    return _typeName;
  }

  @Override
  public String getClassifier() {
    return _classifier;
  }

}
