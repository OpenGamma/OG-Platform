/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.connector.LiveData;
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

  private static final Logger s_logger = LoggerFactory.getLogger(LiveDataHandler.class);

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

  @Override
  public UserMessagePayload visitQueryAvailable(final QueryAvailable message, final SessionContext data) {
    final LiveDataRepository repository = data.getLiveDataRepository();
    repository.initialize(data.getLiveDataProvider(), true);
    final Map<Integer, MetaLiveData> definitions = repository.getAll();
    s_logger.info("{} live data available", definitions.size());
    final Available available = new Available();
    final LiveDataDefinitionFilter filter = data.getGlobalContext().getLiveDataDefinitionFilter();
    for (Map.Entry<Integer, MetaLiveData> definition : definitions.entrySet()) {
      Definition logical = filter.createDefinition(definition.getValue());
      if (logical != null) {
        s_logger.debug("Publishing {} as {}", logical, definition.getKey());
        available.addLiveData(new Available.Entry(definition.getKey(), logical));
      } else {
        s_logger.debug("Discarding {} after applying filter", definition);
      }
    }
    return available;
  }

  @Override
  public UserMessagePayload visitUnexpected(final LiveData message, final SessionContext data) {
    throw new IllegalStateException("Message " + message + " should not have been sent by client");
  }

}
