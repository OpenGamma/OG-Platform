/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.connector.Procedure;
import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.custom.CustomProcedureVisitor;
import com.opengamma.language.custom.CustomProcedureVisitorRegistry;
import com.opengamma.language.custom.CustomVisitors;
import com.opengamma.language.error.AbstractException;
import com.opengamma.language.error.Constants;
import com.opengamma.util.async.AsynchronousExecution;

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
  public UserMessagePayload visitInvoke(final Invoke message, final SessionContext context) throws AsynchronousExecution {
    try {
      final ProcedureRepository repository = context.getProcedureRepository();
      MetaProcedure procedure = repository.get(message.getIdentifier());
      if (procedure == null) {
        if (repository.initialize(context.getProcedureProvider(), false)) {
          s_logger.info("Initialized procedure repository");
          procedure = repository.get(message.getIdentifier());
        }
        if (procedure == null) {
          s_logger.error("Invalid procedure invocation ID {}", message.getIdentifier());
          return null;
        }
      }
      s_logger.debug("Invoking {}", procedure.getName());
      final List<Data> parameters = message.getParameter();
      // invoke produces a "Result", so allow its async. exception to propogate out
      return procedure.getInvoker().invoke(context, (parameters != null) ? parameters : Collections.<Data>emptyList());
    } catch (AbstractException e) {
      return new Result(Collections.singleton(DataUtils.of(e.getValue())));
    } catch (RuntimeException e) {
      s_logger.error("Invocation runtime exception", e);
      final Value err = ValueUtils.ofError(Constants.ERROR_INTERNAL);
      err.setStringValue(e.getMessage());
      return new Result(Collections.singleton(DataUtils.of(err)));
    }
  }

  @Override
  public UserMessagePayload visitQueryAvailable(final QueryAvailable message, final SessionContext data) {
    final ProcedureRepository repository = data.getProcedureRepository();
    repository.initialize(data.getProcedureProvider(), true);
    final Map<Integer, MetaProcedure> definitions = repository.getAll();
    s_logger.info("{} procedures available", definitions.size());
    final Available available = new Available();
    final ProcedureDefinitionFilter filter = data.getGlobalContext().getProcedureDefinitionFilter();
    for (Map.Entry<Integer, MetaProcedure> definition : definitions.entrySet()) {
      Definition logical = filter.createDefinition(definition.getValue());
      if (logical != null) {
        s_logger.debug("Publishing {} as {}", logical, definition.getKey());
        available.addProcedure(new Available.Entry(definition.getKey(), logical));
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
