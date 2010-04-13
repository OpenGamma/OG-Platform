/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.monitor;

import org.slf4j.Logger;

/**
 * Reports on how long particular operations, maintained by {@link OperationTimer} instances,
 * took to perform.
 *
 * @author kirk
 */
public interface OperationTimeReporter {
  
  void report(long duration, Logger logger, String format, Object[] arguments);

}
