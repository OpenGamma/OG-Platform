/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.MutableFudgeMsg;

/**
 * Abstract Fudge builder.
 */
public abstract class AbstractFudgeBuilder {

  /**
   * Adds an object to the specified message if non-null
   * 
   * @param msg  the msg to populate, not null
   * @param fieldName  the field name, may be null
   * @param value  the value, null ignored
   */
  protected static void addToMessage(final MutableFudgeMsg msg, final String fieldName, final Object value) {
    if (value != null) {
      msg.add(fieldName, null, value);
    }
  }

//  /**
//   * Adds an object to the specified message if non-null
//   * 
//   * @param msg  the msg to populate, not null
//   * @param fieldName  the field name, may be null
//   * @param wireType  the wire type to use, not null
//   * @param value  the value, null ignored
//   */
//  protected static void addToMessage(final MutableFudgeMsg msg, final String fieldName, final FudgeWireType wireType, final Object value) {
//    if (value != null) {
//      msg.add(fieldName, null, wireType, value);
//    }
//  }

}
