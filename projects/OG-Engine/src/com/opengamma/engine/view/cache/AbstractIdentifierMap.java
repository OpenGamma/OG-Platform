/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Partial implementation of {@link IdentifierMap}. A real implementation should
 * handle the multiple value lookup more efficiently.
 */
public abstract class AbstractIdentifierMap implements IdentifierMap {

  @Override
  public Map<ValueSpecification, Long> getIdentifiers(final Collection<ValueSpecification> specifications) {
    return getIdentifiers(this, specifications);
  }

  public static Map<ValueSpecification, Long> getIdentifiers(final IdentifierMap map, final Collection<ValueSpecification> specifications) {
    final Map<ValueSpecification, Long> identifiers = new HashMap<ValueSpecification, Long>();
    for (ValueSpecification specification : specifications) {
      identifiers.put(specification, map.getIdentifier(specification));
    }
    return identifiers;
  }

  @Override
  public Map<Long, ValueSpecification> getValueSpecifications(final Collection<Long> identifiers) {
    return getValueSpecifications(this, identifiers);
  }

  public static Map<Long, ValueSpecification> getValueSpecifications(final IdentifierMap map, final Collection<Long> identifiers) {
    final Map<Long, ValueSpecification> specifications = new HashMap<Long, ValueSpecification>();
    for (Long identifier : identifiers) {
      specifications.put(identifier, map.getValueSpecification(identifier));
    }
    return specifications;
  }

}
