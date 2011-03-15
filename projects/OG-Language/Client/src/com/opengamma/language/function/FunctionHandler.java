/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.Data;
import com.opengamma.language.connector.Function;
import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.custom.CustomFunctionVisitor;
import com.opengamma.language.custom.CustomFunctionVisitorRegistry;
import com.opengamma.language.custom.CustomVisitors;

/**
 * Standard handling of function messages.
 */
public class FunctionHandler implements FunctionVisitor<UserMessagePayload, SessionContext>,
    CustomFunctionVisitorRegistry<UserMessagePayload, SessionContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionHandler.class);

  private final CustomVisitors<UserMessagePayload, SessionContext> _customVisitors = new CustomVisitors<UserMessagePayload, SessionContext>();

  // CustomFunctionVisitorRegistry

  @Override
  public <M extends Custom> void register(final Class<M> clazz,
      final CustomFunctionVisitor<M, UserMessagePayload, SessionContext> visitor) {
    _customVisitors.register(clazz, visitor);
  }

  // FunctionVisitor

  @Override
  public UserMessagePayload visitCustom(final Custom message, final SessionContext context) {
    return _customVisitors.visit(message, context);
  }

  @Override
  public UserMessagePayload visitInvoke(final Invoke message, final SessionContext context) {
    final FunctionRepository repository = context.getFunctionRepository();
    final MetaFunction function = repository.get(message.getIdentifier());
    if (function == null) {
      s_logger.error("Invalid function invocation ID {}", message.getIdentifier());
      return null;
    }
    s_logger.debug("Invoking {}", function.getName());
    final List<Data> parameters = message.getParameter();
    return function.getInvoker().invoke(context, (parameters != null) ? parameters : Collections.<Data> emptyList());
  }

  @Override
  public UserMessagePayload visitQueryAvailable(final QueryAvailable message, final SessionContext context) {
    final Set<MetaFunction> definitions = context.getFunctionProvider().getDefinitions();
    final FunctionRepository repository = context.getFunctionRepository();
    s_logger.info("{} functions available", definitions.size());
    final Available available = new Available();
    for (MetaFunction definition : definitions) {
      Definition logical = context.getGlobalContext().getFunctionDefinitionFilter().createDefinition(definition);
      if (logical != null) {
        final int identifier = repository.add(definition);
        s_logger.debug("Publishing {} as {}", logical, identifier);
        available.addFunction(new Available.Entry(identifier, logical));
      } else {
        s_logger.debug("Discarding {} after applying filter", definition);
      }
    }
    return available;
  }

  @Override
  public UserMessagePayload visitUnexpected(final Function message, final SessionContext context) {
    throw new IllegalStateException("Message " + message + " should not have been sent by client");
  }

}
