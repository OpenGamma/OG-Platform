/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.proxy;

import com.opengamma.sesame.graph.FunctionIdProvider;
import com.opengamma.sesame.graph.ProxyNode;

/**
 * Factory for objects that provide the behaviour of a proxy.
 * This class is necessary because node decorators are invoked when the graph model is built but
 * the handler can't be created until the graph is built and the object being proxied is created. This interface
 * provides a level of indirection that separates the node decoration from handler creation.
 */
public interface InvocationHandlerFactory {

  /**
   * Creates a handler for a proxy that sits in front of the delegate.
   *
   * @param delegate  the object being proxied, not null
   * @param node  the proxy node, not null
   * @param functionIdProvider provides unique IDs for function instances
   * @return a handler that provides the proxy behaviour, not null
   */
  ProxyInvocationHandler create(Object delegate, ProxyNode node, FunctionIdProvider functionIdProvider);

}
