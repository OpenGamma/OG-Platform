/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Partial implementation of {@link IdentifierMap}. A real implementation should
 * handle the multiple value lookup more efficiently.
 */
public abstract class AbstractIdentifierMap implements IdentifierMap {

  @Override
  public Object2LongMap<ValueSpecification> getIdentifiers(final Collection<ValueSpecification> specifications) {
    return getIdentifiers(this, specifications);
  }

  public static Object2LongMap<ValueSpecification> getIdentifiers(final IdentifierMap map, final Collection<ValueSpecification> specifications) {
    final Object2LongMap<ValueSpecification> identifiers = new Object2LongOpenHashMap<ValueSpecification>();
    for (ValueSpecification specification : specifications) {
      identifiers.put(specification, map.getIdentifier(specification));
    }
    return identifiers;
  }

  @Override
  public Long2ObjectMap<ValueSpecification> getValueSpecifications(final LongCollection identifiers) {
    return getValueSpecifications(this, identifiers);
  }

  public static Long2ObjectMap<ValueSpecification> getValueSpecifications(final IdentifierMap map, final LongCollection identifiers) {
    final Long2ObjectMap<ValueSpecification> specifications = new Long2ObjectOpenHashMap<ValueSpecification>();
    for (Long identifier : identifiers) {
      specifications.put(identifier, map.getValueSpecification(identifier));
    }
    return specifications;
  }

  public static void convertIdentifiers(final IdentifierMap map, final IdentifierEncodedValueSpecifications object) {
    final Set<ValueSpecification> valueSpecifications = new HashSet<ValueSpecification>();
    object.collectValueSpecifications(valueSpecifications);
    object.convertValueSpecifications(map.getIdentifiers(valueSpecifications));
  }

  public static void resolveIdentifiers(final IdentifierMap map, final IdentifierEncodedValueSpecifications object) {
    final LongSet identifiers = new LongOpenHashSet();
    object.collectIdentifiers(identifiers);
    object.convertIdentifiers(map.getValueSpecifications(identifiers));
  }

}
