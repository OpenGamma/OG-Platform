/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.listener.ViewDefinitionCompiledCall;

/**
 * Fudge message builder for {@link ViewDefinitionCompiledCall}
 */
@FudgeBuilderFor(ViewDefinitionCompiledCall.class)
public class ViewDefinitionCompiledCallBuilder implements FudgeBuilder<ViewDefinitionCompiledCall> {

  private static final String COMPILED_VIEW_DEFINITION_FIELD = "compiledViewDefinition";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ViewDefinitionCompiledCall object) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessage(msg, COMPILED_VIEW_DEFINITION_FIELD, null, object.getCompiledViewDefinition());
    return msg;
  }

  @Override
  public ViewDefinitionCompiledCall buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    CompiledViewDefinition compiledViewDefinition = context.fieldValueToObject(CompiledViewDefinition.class, msg.getByName(COMPILED_VIEW_DEFINITION_FIELD));
    return new ViewDefinitionCompiledCall(compiledViewDefinition);
  }

}
