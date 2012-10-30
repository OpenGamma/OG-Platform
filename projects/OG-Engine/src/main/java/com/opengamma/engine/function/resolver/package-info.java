/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Provides function definitions to the dependency graph builder when a view is compiled.
 * Function resolvers are backed by a function repository and can return candidate functions for
 * calculating any requested result. A dependency graph builder selects functions that can produce
 * the requested outputs, and then works recursively with the resolver to provide the
 * inputs needed for these until the graph is complete or discovered to be unsatisfiable
 */
@PublicSPI
package com.opengamma.engine.function.resolver;

import com.opengamma.util.PublicSPI;

