/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract base implementation of {@link Resolver}.
 * <p>
 * You must override one or other of the two {@code resolve()} methods.
 * <p>
 * Override the individual {@link #resolve(Object)} if you just want an easy life.
 * <p> 
 * Override the bulk {@link #resolve(Collection)} if you need to make a remote call
 * to an external service and want your implementation to be efficient.
 * 
 * @param <A> unresolved object
 * @param <B> resolved object
 */
public abstract class AbstractResolver<A, B> implements Resolver<A, B> {
  
  @Override
  public B resolve(A spec) {
    Map<A, B> result = resolve(Collections.singleton(spec));
    return result.get(spec);
  }
  
  @Override
  public Map<A, B> resolve(Collection<A> specs) {
    Map<A, B> returnValue = new HashMap<A, B>();
    for (A spec : specs) {
      B resolved = resolve(spec);
      returnValue.put(spec, resolved);
    }
    return returnValue;
  }

}
