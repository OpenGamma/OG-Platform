/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.map;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

/**
 * Implementation of {@link Map2} that holds its values by soft reference. Keys are held by strong reference.
 * 
 * @param <K1> key 1 type
 * @param <K2> key 2 type
 * @param <V> value type
 */
public class SoftValueHashMap2<K1, K2, V> extends ReferenceHashMap2<K1, K2, V> {

  private static final class Ref<K1, K2, V> extends SoftReference<V> {

    private final ReferenceHashMap2<K1, K2, V>.ReferenceMap _map;
    private final K2 _key;

    private Ref(final V value, final ReferenceHashMap2<K1, K2, V>.ReferenceMap map, final K2 key) {
      super(value);
      _map = map;
      _key = key;
    }

    private void housekeep() {
      _map.housekeep(_key, this);
    }

  }

  public SoftValueHashMap2(final KeyStrategy key1Strategy) {
    super(key1Strategy);
  }

  @Override
  protected Reference<? extends V> createReference(final ReferenceHashMap2<K1, K2, V>.ReferenceMap map, K2 key, V value) {
    return new Ref<K1, K2, V>(value, map, key);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void housekeep(final Reference<? extends V> ref) {
    ((Ref<K1, K2, V>) ref).housekeep();
  }

}
