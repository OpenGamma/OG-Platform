/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

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

}
