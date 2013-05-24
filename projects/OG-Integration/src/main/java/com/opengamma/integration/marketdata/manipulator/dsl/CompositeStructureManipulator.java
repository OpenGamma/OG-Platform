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
import com.opengamma.engine.marketdata.manipulator.StructureManipulator;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CompositeStructureManipulator<T> implements StructureManipulator<T> {

  private static final String MANIPULATOR = "manipulator";
  private final List<StructureManipulator<T>> _manipulators;

  public CompositeStructureManipulator(List<StructureManipulator<T>> manipulators) {
    ArgumentChecker.notEmpty(manipulators, "manipulators");
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

  public MutableFudgeMsg toFudgeMsg(FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    for (StructureManipulator<T> manipulator : _manipulators) {
      serializer.addToMessageWithClassHeaders(msg, MANIPULATOR, null, manipulator);
    }
    return msg;
  }

  public static <T> CompositeStructureManipulator<T> fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    List<StructureManipulator<T>> manipulators = Lists.newArrayList();
    for (FudgeField field : msg.getAllByName(MANIPULATOR)) {
      @SuppressWarnings("unchecked")
      StructureManipulator<T> manipulator = deserializer.fieldValueToObject(StructureManipulator.class, field);
      manipulators.add(manipulator);
    }
    return new CompositeStructureManipulator<>(manipulators);
  }
}
