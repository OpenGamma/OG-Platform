/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.util.ArgumentChecker;

/**
 * Fudge message builder for {@link ResultModelDefinition}.
 */
@FudgeBuilderFor(ResultModelDefinition.class)
public class ResultModelDefinitionFudgeBuilder implements FudgeBuilder<ResultModelDefinition> {

  private static final String AGGREGATE_POSITION_OUTPUT_MODE_FIELD = "aggregatePositionOutputMode";
  private static final String POSITION_OUTPUT_MODE_FIELD = "positionOutputMode";
  private static final String TRADE_OUTPUT_MODE_FIELD = "tradeOutputMode";
  private static final String SECURITY_OUTPUT_MODE_FIELD = "securityOutputMode";
  private static final String PRIMITIVE_OUTPUT_MODE_FIELD = "primitiveOutputMode";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ResultModelDefinition object) {
    ArgumentChecker.notNull(serializer, "Fudge Context");
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(AGGREGATE_POSITION_OUTPUT_MODE_FIELD, object.getAggregatePositionOutputMode().name());
    msg.add(POSITION_OUTPUT_MODE_FIELD, object.getPositionOutputMode().name());
    msg.add(TRADE_OUTPUT_MODE_FIELD, object.getTradeOutputMode().name());
    msg.add(SECURITY_OUTPUT_MODE_FIELD, object.getSecurityOutputMode().name());
    msg.add(PRIMITIVE_OUTPUT_MODE_FIELD, object.getPrimitiveOutputMode().name());
    return msg;
  }

  @Override
  public ResultModelDefinition buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    ResultModelDefinition result = new ResultModelDefinition();
    result.setAggregatePositionOutputMode(message.getFieldValue(ResultOutputMode.class, message.getByName(AGGREGATE_POSITION_OUTPUT_MODE_FIELD)));
    result.setPositionOutputMode(message.getFieldValue(ResultOutputMode.class, message.getByName(POSITION_OUTPUT_MODE_FIELD)));
    ResultOutputMode tradeMode = message.getFieldValue(ResultOutputMode.class, message.getByName(TRADE_OUTPUT_MODE_FIELD)); // added later, so handle null
    result.setTradeOutputMode(tradeMode == null ? ResultOutputMode.TERMINAL_OUTPUTS : tradeMode);
    result.setSecurityOutputMode(message.getFieldValue(ResultOutputMode.class, message.getByName(SECURITY_OUTPUT_MODE_FIELD)));
    result.setPrimitiveOutputMode(message.getFieldValue(ResultOutputMode.class, message.getByName(PRIMITIVE_OUTPUT_MODE_FIELD)));
    return result;
  }

}
