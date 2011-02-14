/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.custom.CustomLiveDataVisitor;
import com.opengamma.language.custom.CustomLiveDataVisitorRegistry;
import com.opengamma.language.custom.CustomVisitors;

/**
 * Standard handling of live-data messages.
 */
public class LiveDataHandler implements LiveDataVisitor<UserMessagePayload, SessionContext>,
    CustomLiveDataVisitorRegistry<UserMessagePayload, SessionContext> {

  private final CustomVisitors<UserMessagePayload, SessionContext> _customVisitors = new CustomVisitors<UserMessagePayload, SessionContext>();

  // CustomLiveDataVisitorRegistry

  @Override
  public <M extends Custom> void register(final Class<M> clazz,
      final CustomLiveDataVisitor<M, UserMessagePayload, SessionContext> visitor) {
    _customVisitors.register(clazz, visitor);
  }

  // LiveDataVisitor

  @Override
  public UserMessagePayload visitCustom(final Custom message, final SessionContext data) {
    return _customVisitors.visit(message, data);
  }

}
