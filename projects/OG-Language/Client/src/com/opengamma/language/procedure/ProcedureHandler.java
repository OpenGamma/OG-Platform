/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.connector.Procedure;
import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.custom.CustomProcedureVisitor;
import com.opengamma.language.custom.CustomProcedureVisitorRegistry;
import com.opengamma.language.custom.CustomVisitors;

/**
 * Standard handling of incoming procedure messages.
 */
public class ProcedureHandler implements ProcedureVisitor<UserMessagePayload, SessionContext>,
    CustomProcedureVisitorRegistry<UserMessagePayload, SessionContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(ProcedureHandler.class);

  private final CustomVisitors<UserMessagePayload, SessionContext> _customVisitors = new CustomVisitors<UserMessagePayload, SessionContext>();

  // CustomProcedureVisitorRegistry

  @Override
  public <M extends Custom> void register(final Class<M> clazz,
      final CustomProcedureVisitor<M, UserMessagePayload, SessionContext> visitor) {
    _customVisitors.register(clazz, visitor);
  }

  // ProcedureVisitor

  @Override
  public UserMessagePayload visitCustom(final Custom message, final SessionContext data) {
    return _customVisitors.visit(message, data);
  }

  @Override
  public UserMessagePayload visitQueryAvailable(final QueryAvailable message, final SessionContext data) {
    final Set<MetaProcedure> definitions = data.getProcedureProvider().getDefinitions();
    final ProcedureRepository repository = data.getProcedureRepository();
    s_logger.info("{} procedures available", definitions.size());
    final Available available = new Available();
    for (MetaProcedure definition : definitions) {
      Definition logical = data.getGlobalContext().getProcedureDefinitionFilter().createDefinition(definition);
      if (logical != null) {
        final int identifier = repository.add(definition);
        s_logger.debug("Publishing {} as {}", logical, identifier);
        available.addProcedure(new Available.Entry(identifier, logical));
      } else {
        s_logger.debug("Discarding {} after applying filter", definition);
      }
    }
    return available;
  }

  @Override
  public UserMessagePayload visitUnexpected(final Procedure message, final SessionContext data) {
    throw new IllegalStateException("Message " + message + " should not have been sent by client");
  }

}
