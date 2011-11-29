/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.ArgumentChecker;

/**
 * An in-memory implementation of {@link FunctionRepository}.
 * This can either be used as-is through a factory which scans available functions,
 * or it can be used as a cache on top of a more costly function repository. 
 *
 */
public class InMemoryFunctionRepository implements FunctionRepository {

  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryFunctionRepository.class);

  private final Set<FunctionDefinition> _functions = new HashSet<FunctionDefinition>();

  public InMemoryFunctionRepository() {
  }

  public synchronized void addFunction(AbstractFunction function) {
    ArgumentChecker.notNull(function, "Function definition");
    if (function.getUniqueId() == null) {
      function.setUniqueId(Integer.toString(_functions.size() + 1) + " (" + function.getClass().getSimpleName() + ")");
    }
    _functions.add(function);
  }

  public synchronized void replaceFunction(FunctionDefinition searchFor, AbstractFunction replaceWith) {
    ArgumentChecker.notNull(searchFor, "searchFor");
    ArgumentChecker.notNull(replaceWith, "replaceWith");
    _functions.remove(searchFor);
    if (replaceWith.getUniqueId() == null) {
      replaceWith.setUniqueId(searchFor.getUniqueId());
    }
    _functions.add(replaceWith);
  }

  @Override
  public Collection<FunctionDefinition> getAllFunctions() {
    return Collections.unmodifiableCollection(_functions);
  }

  /**
   * This method is primarily useful for testing, as otherwise it will be
   * done explicitly by the {@link ViewProcessor} on startup.
   * 
   * @param compilationContext The context to provide to each function.
   */
  public void initFunctions(FunctionCompilationContext compilationContext) {
    compilationContext.setFunctionReinitializer(new DummyFunctionReinitializer());
    for (FunctionDefinition function : _functions) {
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
