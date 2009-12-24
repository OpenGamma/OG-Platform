/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;

/**
 * A trivial implementation of {@link FunctionResolver}.
 *
 * @author kirk
 */
public class DefaultFunctionResolver implements
    FunctionResolver {
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultFunctionResolver.class);
  private final FunctionRepository _repository;
  
  public DefaultFunctionResolver(FunctionRepository repository) {
    if(repository == null) {
      throw new NullPointerException("Must provide an Analytic Function Repository.");
    }
    _repository = repository;
  }

  /**
   * @return the repository
   */
  public FunctionRepository getRepository() {
    return _repository;
  }
  
  // TODO kirk 2009-11-02 -- Much refactoring love required here.

  @Override
  public FunctionDefinition resolve(
      AnalyticValueDefinition<?> requiredValue) {
    assert requiredValue != null;
    Collection<FunctionDefinition> possibleFunctions = getRepository().getFunctionsProducing(Collections.<AnalyticValueDefinition<?>>singleton(requiredValue));
    assert possibleFunctions != null;
    if(possibleFunctions.isEmpty()) {
      return null;
    }
    // REVIEW kirk 2009-09-04 -- This is the extension point for better lookups.
    if(possibleFunctions.size() > 1) {
      s_logger.info("Got {} functions for output value {}", possibleFunctions.size(), requiredValue);
    }
    FunctionDefinition function = possibleFunctions.iterator().next();
    s_logger.debug("Chose function {} for output value {}", function.getShortName(), requiredValue);
    
    return function;
  }

  @Override
  public FunctionDefinition resolve(AnalyticValueDefinition<?> requiredValue,
      Security security) {
    assert requiredValue != null;
    Collection<FunctionDefinition> possibleFunctions = getRepository().getFunctionsProducing(Collections.<AnalyticValueDefinition<?>>singleton(requiredValue), security);
    assert possibleFunctions != null;
    if(possibleFunctions.isEmpty()) {
      return null;
    }
    // REVIEW kirk 2009-09-04 -- This is the extension point for better lookups.
    if(possibleFunctions.size() > 1) {
      s_logger.info("Got {} functions for output value {}", possibleFunctions.size(), requiredValue);
    }
    FunctionDefinition function = possibleFunctions.iterator().next();
    s_logger.debug("Chose function {} for output value {}", function.getShortName(), requiredValue);
    
    return function;
  }

  @Override
  public FunctionDefinition resolve(
      AnalyticValueDefinition<?> requiredValue, Position position) {
    assert requiredValue != null;
    Collection<FunctionDefinition> possibleFunctions = getRepository().getFunctionsProducing(Collections.<AnalyticValueDefinition<?>>singleton(requiredValue), position);
    assert possibleFunctions != null;
    if(possibleFunctions.isEmpty()) {
      return null;
    }
    // REVIEW kirk 2009-09-04 -- This is the extension point for better lookups.
    if(possibleFunctions.size() > 1) {
      s_logger.info("Got {} functions for output value {}", possibleFunctions.size(), requiredValue);
    }
    FunctionDefinition function = possibleFunctions.iterator().next();
    s_logger.debug("Chose function {} for output value {}", function.getShortName(), requiredValue);
    
    return function;
  }

  @Override
  public FunctionDefinition resolve(
        AnalyticValueDefinition<?> requiredValue, Collection<Position> positions) {
      assert requiredValue != null;
      Collection<FunctionDefinition> possibleFunctions = getRepository().getFunctionsProducing(Collections.<AnalyticValueDefinition<?>>singleton(requiredValue), positions);
      assert possibleFunctions != null;
      if(possibleFunctions.isEmpty()) {
        return null;
      }
      // REVIEW kirk 2009-09-04 -- This is the extension point for better lookups.
      if(possibleFunctions.size() > 1) {
        s_logger.info("Got {} functions for output value {}", possibleFunctions.size(), requiredValue);
      }
      FunctionDefinition function = possibleFunctions.iterator().next();
      s_logger.debug("Chose function {} for output value {}", function.getShortName(), requiredValue);
      
      return function;
  }

}
