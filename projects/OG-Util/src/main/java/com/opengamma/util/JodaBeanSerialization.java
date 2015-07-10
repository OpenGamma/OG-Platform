/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerDeserializers;
import org.joda.convert.RenameHandler;

/**
 * Utilities for working with Joda-Bean serialization.
 * <p>
 * Call the methods on {@link SerDeserializers#INSTANCE} or
 * {@link RenameHandler#INSTANCE} to handle refactoring.
 * This will need to be done in locations that have access to
 * the relevant refactored code.
 */
public final class JodaBeanSerialization {

  /**
   * Pretty printed serialization.
   */
  private static final JodaBeanSer PRETTY = JodaBeanSer.PRETTY;
  /**
   * Compact serialization.
   */
  private static final JodaBeanSer COMPACT = JodaBeanSer.COMPACT;

  /**
   * Restricted constructor.
   */
  private JodaBeanSerialization() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a serializer which can write XML.
   * 
   * @param pretty  true for pretty output, false for compact
   * @return the serialization settings object, not null
   */
  public static JodaBeanSer serializer(boolean pretty) {
    return pretty ? PRETTY : COMPACT;
  }

  /**
   * Gets a deserializer which can read XML.
   * 
   * @return the serialization settings object, not null
   */
  public static JodaBeanSer deserializer() {
    return PRETTY;
  }

}
