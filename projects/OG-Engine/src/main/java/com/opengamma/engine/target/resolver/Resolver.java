/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.id.UniqueIdentifiable;

/**
 * Common interface for a resolver component to work with an object type. This is the union of the {@link ObjectResolver} and {@link IdentifierResolver} type as a common resolution strategy should be
 * used for all major data types.
 * 
 * @param <T> the common type of the item produced by the resolution
 */
public interface Resolver<T extends UniqueIdentifiable> extends ObjectResolver<T>, IdentifierResolver {

}
