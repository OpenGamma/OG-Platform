/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.calcnode.CalculationJobItem;

/**
 * Dummy implementation of {@link FunctionBlacklistMaintainer} that just logs at info level.
 */
public class DummyFunctionBlacklistMaintainer implements FunctionBlacklistMaintainer {

  private static final Logger s_logger = LoggerFactory.getLogger(DummyFunctionBlacklistMaintainer.class);

  @Override
  public void failedJobItem(final CalculationJobItem item) {
    s_logger.info("Failed job item: {}", item);
  }

  @Override
  public void failedJobItems(final Collection<CalculationJobItem> items) {
    s_logger.info("Failed job item(s): {}", items);
  }

}
