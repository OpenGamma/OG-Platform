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
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CompositeStructureManipulator<T> implements StructureManipulator<T> {

  private static final String MANIPULATOR_FIELD = "manipulator";

  private final List<StructureManipulator<T>> _manipulators;
  private final Class<T> _expectedType;

  public CompositeStructureManipulator(List<StructureManipulator<T>> manipulators) {
    ArgumentChecker.notEmpty(manipulators, "manipulators");
    // TODO need to check all the manipulators and get the common supertype
    _expectedType = manipulators.get(0).getExpectedType();
    _manipulators = ImmutableList.copyOf(manipulators);
  }

  @Override
  public T execute(T structure, ValueSpecification valueSpecification, FunctionExecutionContext executionContext) {
    T value = structure;
    for (StructureManipulator<T> manipulator : _manipulators) {
      value = manipulator.execute(value, valueSpecification, executionContext);
    }
    return value;
  }

  @Override
  public Class<T> getExpectedType() {
    return _expectedType;
  }

  /* package */ List<StructureManipulator<T>> getManipulators() {
    return _manipulators;
  }

  public MutableFudgeMsg toFudgeMsg(FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    for (StructureManipulator<T> manipulator : _manipulators) {
      serializer.addToMessageWithClassHeaders(msg, MANIPULATOR_FIELD, null, manipulator);
    }
    return msg;
  }

  public static <T> CompositeStructureManipulator<T> fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    List<StructureManipulator<T>> manipulators = Lists.newArrayList();
    for (FudgeField field : msg.getAllByName(MANIPULATOR_FIELD)) {
      @SuppressWarnings("unchecked")
      StructureManipulator<T> manipulator = deserializer.fieldValueToObject(StructureManipulator.class, field);
      manipulators.add(manipulator);
    }
    return new CompositeStructureManipulator<>(manipulators);
  }

  @Override
  public String toString() {
    return "CompositeStructureManipulator [" +
        "_manipulators=" + _manipulators +
        "]";
  }
}
