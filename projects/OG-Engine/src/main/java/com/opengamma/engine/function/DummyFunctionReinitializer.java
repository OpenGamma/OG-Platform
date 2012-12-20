/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ObjectId;

/**
 * Implementation of the function re-initialization hook that discards any requests.
 */
public class DummyFunctionReinitializer implements FunctionReinitializer {

  private static final Logger s_logger = LoggerFactory.getLogger(DummyFunctionReinitializer.class);

  @Override
  public void reinitializeFunction(final FunctionDefinition function, final ObjectId identifier) {
    s_logger.info("Reinitialize {} on changes to {}", function, identifier);
  }

  @Override
  public void reinitializeFunction(final FunctionDefinition function, final Collection<ObjectId> identifiers) {
    s_logger.info("Reinitialize {} on changes to {}", function, identifiers);
  }

}
