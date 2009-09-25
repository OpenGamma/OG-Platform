/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.concurrent.TimeUnit;

/**
 * Allows a calculation node to retrieve jobs for execution.
 *
 * @author kirk
 */
public interface CalculationNodeJobSource {
  
  CalculationNodeJob getJob(long time, TimeUnit unit);

}
