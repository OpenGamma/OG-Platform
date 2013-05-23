/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;

@FudgeBuilderFor(PredicateSelector.class)
public class PredicateSelectorFudgeBuilder implements FudgeBuilder<PredicateSelector> {

  private static final String CALC_CONFIG = "calcConfig";
  private static final String TYPE = "type";
  private static final String PREDICATES = "predicates";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, PredicateSelector selector) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, CALC_CONFIG, null, selector.getCalcConfigName());
    serializer.addToMessage(msg, TYPE, null, selector.getType().getName());
    serializer.addToMessage(msg, PREDICATES, null, selector.getPredicates());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public PredicateSelector buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    String calcConfig = deserializer.fieldValueToObject(String.class, msg.getByName(CALC_CONFIG));
    String typeName = deserializer.fieldValueToObject(String.class, msg.getByName(TYPE));
    List<?> predicates = deserializer.fieldValueToObject(List.class, msg.getByName(PREDICATES));
    Class<?> type = null;
    try {
      type = Class.forName(typeName);
      return new PredicateSelector(calcConfig, predicates, type);
    } catch (ClassNotFoundException e) {
      throw new OpenGammaRuntimeException("Unknown class " + typeName, e);
    }
  }
}
