/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

/**
 * Callback interface for a JobInvoker to inform a JobDispatcher it is ready to accept
 * invocation requests. 
 */
public interface JobInvokerRegister {

  void registerJobInvoker(JobInvoker invoker);

}
