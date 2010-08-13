/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.util.InetAddressUtils;

/**
 * 
 */
public class LocalCalculationNode extends AbstractCalculationNode {

  private static int s_nodeUniqueID;

  private static synchronized String createNodeId() {
    final StringBuilder sb = new StringBuilder();
    sb.append(InetAddressUtils.getLocalHostName());
    sb.append('/');
    // TODO can we get the process ID in here ?
    sb.append('/');
    sb.append(++s_nodeUniqueID);
    return sb.toString();
  }

  public LocalCalculationNode(ViewComputationCacheSource cacheSource, FunctionExecutionContext functionExecutionContext, ComputationTargetResolver targetResolver,
      ViewProcessorQuerySender calcNodeQuerySender) {
    super(cacheSource, functionExecutionContext, targetResolver, calcNodeQuerySender, createNodeId());
  }

}
