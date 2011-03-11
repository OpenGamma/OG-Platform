/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  public UserMessagePayload visitCustom(final Custom message, final SessionContext data) {
    return _customVisitors.visit(message, data);
  }

  @Override
  public UserMessagePayload visitQueryAvailable(final QueryAvailable message, final SessionContext data) {
    final Set<MetaFunction> definitions = data.getFunctionProvider().getDefinitions();
    s_logger.info("{} functions available", definitions.size());
    final Available available = new Available();
    for (MetaFunction definition : definitions) {
      Definition logical = data.getGlobalContext().getFunctionDefinitionFilter().createDefinition(definition);
      if (logical != null) {
        s_logger.debug("Publishing {}", logical);
        available.addDefinition(logical);
      } else {
        s_logger.debug("Discarding {} after applying filter", definition);
      }
    }
    return available;
  }

  @Override
  public UserMessagePayload visitUnexpected(final Function message, final SessionContext data) {
    throw new IllegalStateException("Message " + message + " should not have been sent by client");
  }

}
