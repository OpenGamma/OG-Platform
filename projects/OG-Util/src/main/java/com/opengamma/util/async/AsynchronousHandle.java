/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.async;

/**
 * Represents a handle to an asynchronously produced object. The object is returned by this handle indirection to allow better control over which thread performs which action. The asynchronous
 * callback thread will post the handle - another thread may then resolve the handle. Resolving the handle may in turn be an asynchronous operation.
 * 
 * @param <T> the type of object returned by this handle
 */
public interface AsynchronousHandle<T> {

  T get() throws AsynchronousHandleExecution;

}
