/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.connector.Function;
import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.custom.CustomFunctionVisitor;
import com.opengamma.language.custom.CustomFunctionVisitorRegistry;
import com.opengamma.language.custom.CustomVisitors;
import com.opengamma.language.error.AbstractException;
import com.opengamma.language.error.Constants;
import com.opengamma.util.async.AsynchronousExecution;

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
  public Result visitInvoke(final Invoke message, final SessionContext context) throws AsynchronousExecution {
    try {
      final FunctionRepository repository = context.getFunctionRepository();
      MetaFunction function = repository.get(message.getIdentifier());
      if (function == null) {
        if (repository.initialize(context.getFunctionProvider(), false)) {
          s_logger.info("Initialized function repository");
          function = repository.get(message.getIdentifier());
        }
        if (function == null) {
          s_logger.error("Invalid function invocation ID {}", message.getIdentifier());
          return null;
        }
      }
      s_logger.debug("Invoking {}", function.getName());
      final List<Data> parameters = message.getParameter();
      // invoke produces a "Result", so allow its async. exception to propogate out
      return function.getInvoker().invoke(context, (parameters != null) ? parameters : Collections.<Data>emptyList());
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
  public Available visitQueryAvailable(final QueryAvailable message, final SessionContext context) {
    final FunctionRepository repository = context.getFunctionRepository();
    repository.initialize(context.getFunctionProvider(), true);
    final Map<Integer, MetaFunction> definitions = repository.getAll();
    s_logger.info("{} functions available", definitions.size());
    final Available available = new Available();
    final FunctionDefinitionFilter filter = context.getGlobalContext().getFunctionDefinitionFilter();
    for (Map.Entry<Integer, MetaFunction> definition : definitions.entrySet()) {
      Definition logical = filter.createDefinition(definition.getValue());
      if (logical != null) {
        s_logger.debug("Publishing {} as {}", logical, definition.getKey());
        available.addFunction(new Available.Entry(definition.getKey(), logical));
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
