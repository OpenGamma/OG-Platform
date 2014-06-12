/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.ArgumentChecker;

/**
 * An in-memory implementation of {@link FunctionRepository}. This can either be used as-is through a factory which scans available functions, or it can be used as a cache on top of a more costly
 * function repository.
 */
public class InMemoryFunctionRepository implements FunctionRepository {

  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryFunctionRepository.class);

  private final Map<String, FunctionDefinition> _functions = new HashMap<String, FunctionDefinition>();
  private final AtomicInteger _nextIdentifier = new AtomicInteger();

  public InMemoryFunctionRepository() {
  }

  private static final class IdentifiedFunction implements FunctionDefinition {

    private final FunctionDefinition _underlying;
    private final String _id;

    public IdentifiedFunction(final FunctionDefinition underlying, final String id) {
      _underlying = underlying;
      _id = id;
    }

    @Override
    public void init(final FunctionCompilationContext context) {
      _underlying.init(context);
    }

    @Override
    public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
      return _underlying.compile(context, atInstant);
    }

    @Override
    public String getUniqueId() {
      return _id;
    }

    @Override
    public String getShortName() {
      return _underlying.getShortName();
    }

    @Override
    public FunctionParameters getDefaultParameters() {
      return _underlying.getDefaultParameters();
    }

  }

  private String createId(final String shortName) {
    String id;
    do {
      id = new StringBuilder().append(_nextIdentifier.getAndIncrement()).append(" (").append(shortName).append(")").toString();
    } while (_functions.containsKey(id));
    return id;
  }

  public synchronized void addFunction(FunctionDefinition function) {
    ArgumentChecker.notNull(function, "Function definition");
    if (function.getUniqueId() == null) {
      if (function instanceof AbstractFunction) {
        ((AbstractFunction) function).setUniqueId(createId(function.getShortName()));
      } else {
        function = new IdentifiedFunction(function, createId(function.getShortName()));
      }
    } else if (_functions.containsKey(function.getUniqueId())) {
      function = new IdentifiedFunction(function, createId(function.getShortName()));
    }
    _functions.put(function.getUniqueId(), function);
  }

  public synchronized void replaceFunction(String functionIdentifier, FunctionDefinition function) {
    ArgumentChecker.notNull(functionIdentifier, "functionIdentifier");
    ArgumentChecker.notNull(function, "function");
    _functions.remove(functionIdentifier);
    addFunction(function);
  }

  @Override
  public Collection<FunctionDefinition> getAllFunctions() {
    return Collections.unmodifiableCollection(_functions.values());
  }

  @Override
  public FunctionDefinition getFunction(final String uniqueId) {
    return _functions.get(uniqueId);
  }

  /**
   * This method is primarily useful for testing, as otherwise it will be done explicitly by the {@link ViewProcessor} on startup.
   * 
   * @param compilationContext The context to provide to each function.
   */
  public void initFunctions(FunctionCompilationContext compilationContext) {
    compilationContext.setFunctionReinitializer(new DummyFunctionReinitializer());
    for (FunctionDefinition function : _functions.values()) {
      try {
        function.init(compilationContext);
      } catch (Throwable t) {
        s_logger.warn("Couldn't initialise function {}", function);
        s_logger.debug("Caught exception", t);
      }
    }
    compilationContext.setFunctionReinitializer(null);
  }

}
