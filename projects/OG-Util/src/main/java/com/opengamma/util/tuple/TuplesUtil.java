/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import java.util.Map;

import com.opengamma.lambdava.tuple.IntDoublePair;
import com.opengamma.lambdava.tuple.IntObjectPair;
import com.opengamma.lambdava.tuple.LongDoublePair;
import com.opengamma.lambdava.tuple.LongObjectPair;
import com.opengamma.lambdava.tuple.Pair;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

/**
 * Utilities.
 */
@SuppressWarnings("rawtypes")
public class TuplesUtil {

  public static Map.Entry pairToEntry(final Pair pair) {
    return new Map.Entry() {
      @Override
      public Object getKey() {
        return pair._1();
      }

      @Override
      public Object getValue() {
        return pair._2();
      }

      @Override
      public Object setValue(Object value) {
        throw new UnsupportedOperationException("This entry is immutable");
      }
    };
  }

  public static Long2ObjectMap.Entry pairToEntry(final LongObjectPair pair) {
    return new Long2ObjectMap.Entry() {
      @Override
      public long getLongKey() {
        return pair.getFirstLong();
      }

      @Override
      public Object getKey() {
        return pair._1();
      }

      @Override
      public Object getValue() {
        return pair._2();
      }

      @Override
      public Object setValue(Object value) {
        throw new UnsupportedOperationException("This entry is immutable");
      }
    };
  }

  public static Long2DoubleMap.Entry pairToEntry(final LongDoublePair pair) {
    return new Long2DoubleMap.Entry() {
      @Override
      public long getLongKey() {
        return pair.getFirstLong();
      }

      @Override
      public double setValue(double value) {
        throw new UnsupportedOperationException("This entry is immutable");
      }

      @Override
      public double getDoubleValue() {
        return pair.getSecondDouble();
      }

      @Override
      public Long getKey() {
        return pair._1();
      }

      @Override
      public Double getValue() {
        return pair._2();
      }

      @Override
      public Double setValue(Double value) {
        throw new UnsupportedOperationException("This entry is immutable");
      }
    };
  }

  public static Int2DoubleMap.Entry pairToEntry(final IntDoublePair pair) {
    return new Int2DoubleMap.Entry() {
      @Override
      public int getIntKey() {
        return pair.getFirstInt();
      }

      @Override
      public double setValue(double value) {
        throw new UnsupportedOperationException("This entry is immutable");
      }

      @Override
      public double getDoubleValue() {
        return pair.getSecondDouble();
      }

      @Override
      public Integer getKey() {
        return pair._1();
      }

      @Override
      public Double getValue() {
        return pair._2();
      }

      @Override
      public Double setValue(Double value) {
        throw new UnsupportedOperationException("This entry is immutable");
      }
    };
  }

  public static Int2ObjectMap.Entry pairToEntry(final IntObjectPair pair) {
    return new Int2ObjectMap.Entry() {
      @Override
      public int getIntKey() {
        return pair.getFirstInt();
      }

      @Override
      public Object getKey() {
        return pair._1();
      }

      @Override
      public Object getValue() {
        return pair._2();
      }

      @Override
      public Object setValue(Object value) {
        throw new UnsupportedOperationException("This entry is immutable");
      }
    };
  }

}
