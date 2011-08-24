/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import java.util.Set;

/**
 * A source of definitions.
 * 
 * @param <T> the definition type
 */
public interface DefinitionProvider<T extends Definition> {

  /**
   * Return the set of definitions. The set returned should not be modified
   * after it has been returned. If definitions are expected to have been
   * changed, it will be invoked again and the differences in set contents noted.
   * <p>
   * An implementation should determine the definitions at the point of first invocation
   * and not at the point of creation, for example the relevant contexts it has access
   * to may not be fully configured or initialized until then. If constructing the
   * definitions is expensive, an implementation may cache the result. If the infrastructure
   * requires a non-cached version, {@link #flush} will be called first.
   * 
   * @return the set of definitions, may be null or the empty set if there are none
   */
  Set<T> getDefinitions();

  /**
   * Discards any cached value, forcing a recalculation of the available definitions the
   * next time {@link #getDefinitions} is called.
   */
  void flush();

}
