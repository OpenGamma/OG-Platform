/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.map;

import com.google.common.collect.MapMaker;

/**
 * Implementation of {@link Map2} that holds its values by soft reference. Keys are held by strong reference.
 * 
 * @param <K1> key 1 type
 * @param <K2> key 2 type
 * @param <V> value type
 */
public class SoftValueHashMap2<K1, K2, V> extends HashMap2<K1, K2, V> {

  protected MapMaker mapMaker() {
    return super.mapMaker().softValues();
  }

}
