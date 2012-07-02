/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.connector.LiveData;
import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.custom.CustomLiveDataVisitor;
import com.opengamma.language.custom.CustomLiveDataVisitorRegistry;
import com.opengamma.language.custom.CustomVisitors;
import com.opengamma.language.error.AbstractException;
import com.opengamma.language.error.Constants;
import com.opengamma.util.async.AsynchronousExecution;

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
  public Result visitConnect(final Connect message, final SessionContext context) throws AsynchronousExecution {
    try {
      final LiveDataRepository repository = context.getLiveDataRepository();
      MetaLiveData liveData = repository.get(message.getIdentifier());
      if (liveData == null) {
        if (repository.initialize(context.getLiveDataProvider(), false)) {
          s_logger.info("Initialized live datarepository");
          liveData = repository.get(message.getIdentifier());
        }
        if (liveData == null) {
          s_logger.error("Invalid live data connection ID {}", message.getIdentifier());
          return null;
        }
      }
      s_logger.debug("Connecting to {}", liveData.getName());
      final List<Data> parameters = message.getParameter();
      final Connection connection = liveData.getConnector().connect(context, (parameters != null) ? parameters : Collections.<Data>emptyList());
      if (message.getConnection() != null) {
        context.getConnections().add(message.getConnection(), connection);
        return context.getGlobalContext().getLiveDataDispatcher().createResult(context, message.getConnection(), connection.getValue());
      } else {
        return context.getGlobalContext().getLiveDataDispatcher().createResult(context, context.getConnections().add(connection), connection.getValue());
      }
    } catch (AbstractException e) {
      return new Result(null, DataUtils.of(e.getValue()));
    } catch (RuntimeException e) {
      s_logger.error("Invocation runtime exception", e);
      final Value err = ValueUtils.ofError(Constants.ERROR_INTERNAL);
      err.setStringValue(e.getMessage());
      return new Result(null, DataUtils.of(err));
    }

  }

  @Override
  public UserMessagePayload visitCustom(final Custom message, final SessionContext context) {
    return _customVisitors.visit(message, context);
  }

  @Override
  public UserMessagePayload visitDisconnect(final Disconnect message, final SessionContext context) {
    context.getConnections().cancel(message.getConnection());
    return UserMessagePayload.EMPTY_PAYLOAD;
  }

  @Override
  public Available visitQueryAvailable(final QueryAvailable message, final SessionContext context) {
    final LiveDataRepository repository = context.getLiveDataRepository();
    repository.initialize(context.getLiveDataProvider(), true);
    final Map<Integer, MetaLiveData> definitions = repository.getAll();
    s_logger.info("{} live data available", definitions.size());
    final Available available = new Available();
    final LiveDataDefinitionFilter filter = context.getGlobalContext().getLiveDataDefinitionFilter();
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
  public Result visitQueryValue(final QueryValue message, final SessionContext context) {
    return context.getConnections().queryValue(message.getIdentifier());
  }

  @Override
  public UserMessagePayload visitUnexpected(final LiveData message, final SessionContext context) {
    throw new IllegalStateException("Message " + message + " should not have been sent by client");
  }

}
