/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;

/**
 * {@link StructureManipulator} implementation that returns the data without any changes.
 */
public class NoOpStructureManipulator implements StructureManipulator<Object> {

  @Override
  public Object execute(Object structure) {
    return structure;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    return serializer.newMessage();
  }

  @Override
  public Class<Object> getExpectedType() {
    return Object.class;
  }

  public static <T> NoOpStructureManipulator fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return new NoOpStructureManipulator();
  }
}
