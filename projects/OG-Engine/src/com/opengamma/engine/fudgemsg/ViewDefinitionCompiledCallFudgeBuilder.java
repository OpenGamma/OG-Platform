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

import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.listener.ViewDefinitionCompiledCall;

/**
 * Fudge message builder for {@link ViewDefinitionCompiledCall}
 */
@FudgeBuilderFor(ViewDefinitionCompiledCall.class)
public class ViewDefinitionCompiledCallFudgeBuilder implements FudgeBuilder<ViewDefinitionCompiledCall> {

  private static final String COMPILED_VIEW_DEFINITION_FIELD = "compiledViewDefinition";
  private static final String HAS_MARKET_DATA_PERMISSIONS_FIELD = "hasMarketDataPermissions";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ViewDefinitionCompiledCall object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, COMPILED_VIEW_DEFINITION_FIELD, null, object.getCompiledViewDefinition());
    msg.add(HAS_MARKET_DATA_PERMISSIONS_FIELD, object.hasMarketDataPermissions());
    return msg;
  }

  @Override
  public ViewDefinitionCompiledCall buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    CompiledViewDefinition compiledViewDefinition = deserializer.fieldValueToObject(CompiledViewDefinition.class, msg.getByName(COMPILED_VIEW_DEFINITION_FIELD));
    boolean hasMarketDataPermissions = msg.getBoolean(HAS_MARKET_DATA_PERMISSIONS_FIELD);
    return new ViewDefinitionCompiledCall(compiledViewDefinition, hasMarketDataPermissions);
  }

}
