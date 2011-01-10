/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.util.ArgumentChecker;

/**
 * Fudge message builder for {@link ResultModelDefinition}.
 */
@FudgeBuilderFor(ResultModelDefinition.class)
public class ResultModelDefinitionBuilder implements FudgeBuilder<ResultModelDefinition> {

  private static final String AGGREGATE_POSITION_OUTPUT_MODE_FIELD = "aggregatePositionOutputMode";
  private static final String POSITION_OUTPUT_MODE_FIELD = "positionOutputMode";
  private static final String TRADE_OUTPUT_MODE_FIELD = "tradeOutputMode";
  private static final String SECURITY_OUTPUT_MODE_FIELD = "securityOutputMode";
  private static final String PRIMITIVE_OUTPUT_MODE_FIELD = "primitiveOutputMode";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ResultModelDefinition object) {
    ArgumentChecker.notNull(context, "Fudge Context");
    MutableFudgeFieldContainer msg = context.newMessage();
    msg.add(AGGREGATE_POSITION_OUTPUT_MODE_FIELD, object.getAggregatePositionOutputMode().name());
    msg.add(POSITION_OUTPUT_MODE_FIELD, object.getPositionOutputMode().name());
    msg.add(TRADE_OUTPUT_MODE_FIELD, object.getTradeOutputMode().name());
    msg.add(SECURITY_OUTPUT_MODE_FIELD, object.getSecurityOutputMode().name());
    msg.add(PRIMITIVE_OUTPUT_MODE_FIELD, object.getPrimitiveOutputMode().name());
    return msg;
  }

  @Override
  public ResultModelDefinition buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    ResultModelDefinition result = new ResultModelDefinition();
    result.setAggregatePositionOutputMode(message.getFieldValue(ResultOutputMode.class, message.getByName(AGGREGATE_POSITION_OUTPUT_MODE_FIELD)));
    result.setPositionOutputMode(message.getFieldValue(ResultOutputMode.class, message.getByName(POSITION_OUTPUT_MODE_FIELD)));
    result.setTradeOutputMode(message.getFieldValue(ResultOutputMode.class, message.getByName(TRADE_OUTPUT_MODE_FIELD)));
    result.setSecurityOutputMode(message.getFieldValue(ResultOutputMode.class, message.getByName(SECURITY_OUTPUT_MODE_FIELD)));
    result.setPrimitiveOutputMode(message.getFieldValue(ResultOutputMode.class, message.getByName(PRIMITIVE_OUTPUT_MODE_FIELD)));
    return result;
  }

}
