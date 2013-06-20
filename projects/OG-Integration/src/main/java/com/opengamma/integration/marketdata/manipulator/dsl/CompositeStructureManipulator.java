/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CompositeStructureManipulator<T> implements StructureManipulator<T> {

  private static final String TYPE_FIELD = "type";
  private static final String MANIPULATOR_FIELD = "manipulator";

  private final List<StructureManipulator<T>> _manipulators;
  private final Class<T> _expectedType;

  public CompositeStructureManipulator(Class<T> expectedType, List<StructureManipulator<T>> manipulators) {
    ArgumentChecker.notNull(expectedType, "expectedType");
    ArgumentChecker.notEmpty(manipulators, "manipulators");
    _expectedType = expectedType;
    _manipulators = ImmutableList.copyOf(manipulators);
  }

  @Override
  public T execute(T structure) {
    T value = structure;
    for (StructureManipulator<T> manipulator : _manipulators) {
      value = manipulator.execute(value);
    }
    return value;
  }

  @Override
  public Class<T> getExpectedType() {
    return _expectedType;
  }

  public MutableFudgeMsg toFudgeMsg(FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(msg, TYPE_FIELD, null, _expectedType);
    for (StructureManipulator<T> manipulator : _manipulators) {
      serializer.addToMessageWithClassHeaders(msg, MANIPULATOR_FIELD, null, manipulator);
    }
    return msg;
  }

  public static <T> CompositeStructureManipulator<T> fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    List<StructureManipulator<T>> manipulators = Lists.newArrayList();
    @SuppressWarnings("unchecked")
    Class<T> expectedType = (Class<T>) msg.getValue(Class.class, TYPE_FIELD);
    for (FudgeField field : msg.getAllByName(MANIPULATOR_FIELD)) {
      @SuppressWarnings("unchecked")
      StructureManipulator<T> manipulator = deserializer.fieldValueToObject(StructureManipulator.class, field);
      manipulators.add(manipulator);
    }
    return new CompositeStructureManipulator<>(expectedType, manipulators);
  }
}
