/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

/**
 * Callback interface for a JobInvoker to inform a JobDispatcher it is ready to accept
 * invocation requests. 
 */
public interface JobInvokerRegister {

  void registerJobInvoker(JobInvoker invoker);

}
