/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionRepository;

/**
 * Fudge message builder for {@code FunctionRepository}.
 */
public class FunctionRepositoryBuilder implements FudgeBuilder<FunctionRepository> {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionRepositoryBuilder.class);

  private static final String FIELD_DEFINITION = "definition";
  private static final String FIELD_INVOKER = "invoker";

  @Override
  public FunctionRepository buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final Collection<FunctionDefinition> functions = new HashSet<FunctionDefinition>();
    final Map<String, FunctionInvoker> invokers = new HashMap<String, FunctionInvoker>();
    for (FudgeField field : message) {
      if (field.getName() == null) {
        continue;
      }
      final FudgeFieldContainer subMessage = message.getFieldValue(FudgeFieldContainer.class, field);
      try {
        final FunctionDefinition functionDefinition = context.fieldValueToObject(FunctionDefinition.class, subMessage.getByName(FIELD_DEFINITION));
        final FunctionInvoker functionInvoker = (functionDefinition instanceof FunctionInvoker) ? (FunctionInvoker) functionDefinition : context.fieldValueToObject(FunctionInvoker.class, subMessage
            .getByName(FIELD_INVOKER));
        functions.add(functionDefinition);
        invokers.put(functionDefinition.getUniqueIdentifier(), functionInvoker);
        //s_logger.debug("Deserialized function {} from {}", field.getName(), subMessage);
      } catch (FudgeRuntimeException e) {
        s_logger.error("Couldn't deserialise function {} from {}", field.getName(), subMessage);
        e.printStackTrace ();
      }
    }
    final Collection<FunctionDefinition> unmodifiableFunctions = Collections.unmodifiableCollection(functions);
    return new FunctionRepository() {

      @Override
      public Collection<FunctionDefinition> getAllFunctions() {
        return unmodifiableFunctions;
      }

      @Override
      public FunctionInvoker getInvoker(String uniqueIdentifier) {
        return invokers.get(uniqueIdentifier);
      }
    };
  }

  @Override
  public MutableFudgeFieldContainer buildMessage(final FudgeSerializationContext context, final FunctionRepository object) {
    final MutableFudgeFieldContainer message = context.newMessage();
    message.add(0, FunctionRepository.class.getName());
    for (FunctionDefinition function : object.getAllFunctions()) {
      final MutableFudgeFieldContainer subMessage = context.newMessage();
      try {
        context.objectToFudgeMsgWithClassHeaders(subMessage, FIELD_DEFINITION, null, function, FunctionDefinition.class);
        final FunctionInvoker invoker = object.getInvoker(function.getUniqueIdentifier());
        if (invoker != function) {
          context.objectToFudgeMsgWithClassHeaders(subMessage, FIELD_INVOKER, null, invoker, FunctionInvoker.class);
        }
        message.add(function.getUniqueIdentifier(), null, subMessage);
      } catch (FudgeRuntimeException e) {
        s_logger.warn("Couldn't serialise function - {}", function, e);
      }
    }
    return message;
  }

}
