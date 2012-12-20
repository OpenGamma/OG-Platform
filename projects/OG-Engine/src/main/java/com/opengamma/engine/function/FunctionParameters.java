/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import com.opengamma.util.PublicSPI;

/**
 * A marker interface for objects that are to be passed to functions at execution time to
 * control their behavior. Implementations must be Fudge-Serializable, and should implement
 * appropriate {@link Object#hashCode} and {@link Object#equals} methods so that graph
 * comparisons are possible (e.g. for caching execution plans).
 */
@PublicSPI
public interface FunctionParameters {

}
