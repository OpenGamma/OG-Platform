/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.map;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * Implementation of {@link Map2} that holds its values by weak reference. Keys are held by strong reference.
 * 
 * @param <K1> key 1 type
 * @param <K2> key 2 type
 * @param <V> value type
 */
public class WeakValueHashMap2<K1, K2, V> extends ReferenceHashMap2<K1, K2, V> {

  private static final class Ref<K1, K2, V> extends WeakReference<V> {

    private final ReferenceHashMap2<K1, K2, V>.ReferenceMap _map;
    private final K2 _key;

    private Ref(final V value, final ReferenceHashMap2<K1, K2, V>.ReferenceMap map, final K2 key, final ReferenceQueue<V> queue) {
      super(value, queue);
      _map = map;
      _key = key;
    }

    private void housekeep() {
      _map.housekeep(_key, this);
    }

  }

  public WeakValueHashMap2(final KeyStrategy key1Strategy) {
    super(key1Strategy);
  }

  @Override
  protected Reference<? extends V> createReference(final ReferenceHashMap2<K1, K2, V>.ReferenceMap map, final K2 key, final V value) {
    return new Ref<K1, K2, V>(value, map, key, getGarbageQueue());
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void housekeep(final Reference<? extends V> ref) {
    ((Ref<K1, K2, V>) ref).housekeep();
  }

}
