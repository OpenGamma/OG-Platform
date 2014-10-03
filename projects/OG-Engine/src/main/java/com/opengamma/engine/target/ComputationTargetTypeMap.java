/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.function.BinaryOperator;

/**
 * A map of {@link ComputationTargetType} instances to other values based on the target class (or classes). Instances are thread-safe for multiple readers, but only one may update the map at any one
 * time. Due to the caching behavior of class lookups, it is best not to read from the map until all of the required class entries have been written. Not doing so can give a collision between a cached
 * class entry and one that is intended to be added.
 * 
 * @param <V> the value type
 */
public class ComputationTargetTypeMap<V> {

  private static final Object NULL = new Object();

  private final ConcurrentMap<Class<? extends UniqueIdentifiable>, V> _underlying = new ConcurrentHashMap<Class<? extends UniqueIdentifiable>, V>();

  private volatile V _nullTypeValue;

  private final BinaryOperator<V> _fold;

  /**
   * Creates a new instance.
   */
  public ComputationTargetTypeMap() {
    this(null);
  }

  /**
   * Creates a new instance with a folding operation to handle union types in the map giving multiple matches on {@link #get} or {@link #put}. If there is no folding operation then the value returned
   * by {@link #get} is an arbitrary choice and {@link #put} will fail if multiple matches occur.
   * 
   * @param fold the folding operation, null for none
   */
  public ComputationTargetTypeMap(BinaryOperator<V> fold) {
    _fold = fold;
  }

  protected ConcurrentMap<Class<? extends UniqueIdentifiable>, V> getUnderlying() {
    return _underlying;
  }

  public BinaryOperator<V> getFoldFunction() {
    return _fold;
  }

  private V getNullValue() {
    return _nullTypeValue;
  }

  private synchronized boolean replaceNullValue(final V oldValue, final V newValue) {
    if (_nullTypeValue == oldValue) {
      _nullTypeValue = newValue;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Performs the target class lookup. Subclasses may override this to hook into the lookup operation and return a specific value.
   * 
   * @param queryType the tail class to query, not null
   * @return the value, or null for none
   */
  @SuppressWarnings("unchecked")
  protected V getImpl(final Class<? extends UniqueIdentifiable> queryType) {
    V value = getUnderlying().get(queryType);
    if (value != null) {
      return value;
    }
    for (Class<?> iface : queryType.getInterfaces()) {
      if (UniqueIdentifiable.class.isAssignableFrom(iface)) {
        final V newValue = getUnderlying().get(iface);
        if (newValue != null) {
          if (value == null) {
            value = newValue;
            if (getFoldFunction() == null) {
              final V previous = getUnderlying().putIfAbsent(queryType, value);
              if (previous != null) {
                value = previous;
              }
              return value;
            }
          } else {
            value = getFoldFunction().apply(value, newValue);
          }
        }
      }
    }
    final Class<?> superclazz = queryType.getSuperclass();
    if ((superclazz != null) && UniqueIdentifiable.class.isAssignableFrom(superclazz)) {
      final V newValue = getImpl((Class<? extends UniqueIdentifiable>) superclazz);
      if (newValue != null) {
        if (value == null) {
          value = newValue;
        } else {
          if (newValue != NULL) {
            value = getFoldFunction().apply(value, newValue);
          }
        }
      }
    }
    if (value != null) {
      final V previous = getUnderlying().putIfAbsent(queryType, value);
      if (previous != null) {
        value = previous;
      }
    } else {
      value = (V) NULL;
      getUnderlying().putIfAbsent(queryType, value);
    }
    return value;
  }

  protected V getDirectImpl(final Class<? extends UniqueIdentifiable> queryType) {
    return getUnderlying().get(queryType);
  }

  @SuppressWarnings("rawtypes")
  private static final ComputationTargetTypeVisitor s_get = new ComputationTargetTypeVisitor<ComputationTargetTypeMap<Object>, Object>() {

    @Override
    public Object visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final ComputationTargetTypeMap<Object> data) {
      Object result = null;
      for (ComputationTargetType type : types) {
        final Object v = type.accept(this, data);
        if (v != null) {
          if (result == null) {
            if (data.getFoldFunction() == null) {
              // No folding operation, so return the first match found
              return v;
            } else {
              result = v;
            }
          } else {
            result = data.getFoldFunction().apply(result, v);
          }
        }
      }
      return result;
    }

    @Override
    public Object visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final ComputationTargetTypeMap<Object> data) {
      return types.get(types.size() - 1).accept(this, data);
    }

    @Override
    public Object visitNullComputationTargetType(final ComputationTargetTypeMap<Object> data) {
      return data.getNullValue();
    }

    @Override
    public Object visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final ComputationTargetTypeMap<Object> data) {
      return data.get(type);
    }

  };

  /**
   * Queries a value based on the supplied target type, matching the closest superclass found. This operation can update the map as lookups based on the class hierarchy are cached.
   * 
   * @param key the target type to query, not null
   * @return the value, or null if there is no match
   */
  @SuppressWarnings("unchecked")
  public V get(final ComputationTargetType key) {
    return key.accept((ComputationTargetTypeVisitor<ComputationTargetTypeMap<V>, V>) s_get, this);
  }

  /**
   * Queries a value based on the supplied target type, matching the closest superclass found. This operation can update the map as lookups based on the class hierarchy are cached. This is the same as
   * calling {@link #get(ComputationTargetType)} with {@code ComputationTargetType.of(key)}.
   * 
   * @param key the class to query, not null
   * @return the value, or null if there is no match
   */
  public V get(final Class<? extends UniqueIdentifiable> key) {
    final V found = getImpl(key);
    if (found != NULL) {
      return found;
    } else {
      return null;
    }
  }

  @SuppressWarnings("rawtypes")
  private static final ComputationTargetTypeVisitor s_getDirect = new ComputationTargetTypeVisitor<ComputationTargetTypeMap<Object>, Object>() {

    @Override
    public Object visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final ComputationTargetTypeMap<Object> data) {
      Object result = null;
      for (ComputationTargetType type : types) {
        final Object v = type.accept(this, data);
        if (v != null) {
          if (result == null) {
            if (data.getFoldFunction() == null) {
              // No folding operation, so return the first match found
              return v;
            } else {
              result = v;
            }
          } else {
            result = data.getFoldFunction().apply(result, v);
          }
        }
      }
      return result;
    }

    @Override
    public Object visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final ComputationTargetTypeMap<Object> data) {
      return types.get(types.size() - 1).accept(this, data);
    }

    @Override
    public Object visitNullComputationTargetType(final ComputationTargetTypeMap<Object> data) {
      return data.getNullValue();
    }

    @Override
    public Object visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final ComputationTargetTypeMap<Object> data) {
      final Object found = data.getDirectImpl(type);
      if (found != NULL) {
        return found;
      } else {
        return null;
      }
    }

  };

  /**
   * Queries a value based on the supplied target type, matching the leaf class exactly. Unlike {@link #get} This operation will not update the map and will not return any matches based on
   * superclasses.
   * 
   * @param key the target type to query, not null
   * @return the value, or null if there is no match
   */
  @SuppressWarnings("unchecked")
  public V getDirect(final ComputationTargetType key) {
    return key.accept((ComputationTargetTypeVisitor<ComputationTargetTypeMap<V>, V>) s_getDirect, this);
  }

  /**
   * Stores a value in the map. If the map already contains an entry for the value the folding operation (if specified) will be used. This can occur if a lookup has been performed and the result was
   * cached, or union types have been added to the map.
   * 
   * @param key the target type key, not null
   * @param value the value to store, not null
   */
  public void put(final ComputationTargetType key, final V value) {
    key.accept(new ComputationTargetTypeVisitor<Void, Void>() {

      @Override
      public Void visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void data) {
        for (ComputationTargetType type : types) {
          type.accept(this, data);
        }
        return null;
      }

      @Override
      public Void visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void data) {
        return types.get(types.size() - 1).accept(this, data);
      }

      @Override
      public Void visitNullComputationTargetType(final Void data) {
        final V nullValue = getNullValue();
        if (nullValue == null) {
          if (replaceNullValue(null, value)) {
            return null;
          } else {
            throw new ConcurrentModificationException();
          }
        } else {
          if (getFoldFunction() != null) {
            final V newValue = getFoldFunction().apply(nullValue, value);
            if (replaceNullValue(nullValue, newValue)) {
              return null;
            } else {
              throw new ConcurrentModificationException();
            }
          } else {
            throw new IllegalStateException("Already held " + nullValue + " for NULL");
          }
        }
      }

      @SuppressWarnings("unchecked")
      @Override
      public Void visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void data) {
        V newValue = (value != null) ? value : (V) NULL;
        final V previous = getUnderlying().putIfAbsent(type, newValue);
        if (previous == null) {
          return null;
        } else {
          if (previous == NULL) {
            if (getUnderlying().replace(type, previous, newValue)) {
              return null;
            } else {
              throw new ConcurrentModificationException();
            }
          }
          if (getFoldFunction() != null) {
            newValue = getFoldFunction().apply(previous, value);
            if (newValue == null) {
              newValue = (V) NULL;
            }
            if (getUnderlying().replace(type, previous, newValue)) {
              return null;
            } else {
              throw new ConcurrentModificationException();
            }
          } else {
            throw new IllegalStateException("Already held " + previous + " for " + key);
          }
        }
      }

    }, null);
  }

  /**
   * Stores a value in the map. If the map already contains a value for a super-class entry the replacement callback function will be used to compose the two values. The first parameter to the
   * callback will be the existing value, the second parameter will be the new value to be added, the returned value will be used.
   * 
   * @param key the target type key, not null
   * @param value the value to store, not null
   * @param replace the callback function to handle values that are already present or null to just use the new value
   */
  public void put(final ComputationTargetType key, final V value, final BinaryOperator<V> replace) {
    key.accept(new ComputationTargetTypeVisitor<Void, Void>() {

      @Override
      public Void visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void data) {
        for (ComputationTargetType type : types) {
          type.accept(this, data);
        }
        return null;
      }

      @Override
      public Void visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void data) {
        return types.get(types.size() - 1).accept(this, data);
      }

      @Override
      public Void visitNullComputationTargetType(final Void data) {
        final V oldValue = getNullValue();
        final V newValue = ((replace != null) && (oldValue != null)) ? replace.apply(oldValue, value) : value;
        if (replaceNullValue(oldValue, newValue)) {
          return null;
        } else {
          throw new ConcurrentModificationException();
        }
      }

      @SuppressWarnings("unchecked")
      @Override
      public Void visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void data) {
        final V oldValue = getImpl(type);
        V newValue = ((replace != null) && (oldValue != NULL)) ? replace.apply(oldValue, value) : value;
        if (newValue == null) {
          newValue = (V) NULL;
        }
        if (getUnderlying().replace(type, oldValue, newValue)) {
          return null;
        } else {
          throw new ConcurrentModificationException();
        }
      }

    }, null);
  }

  public Iterable<V> values() {
    return new Iterable<V>() {
      @Override
      public Iterator<V> iterator() {
        return new Iterator<V>() {

          private final Iterator<V> _itr = getUnderlying().values().iterator();
          private V _nullValue = getNullValue();
          private V _nextValue = _nullValue;

          private V nextValue() {
            _nullValue = null;
            while (_itr.hasNext()) {
              final V value = _itr.next();
              if (value != NULL) {
                return value;
              }
            }
            return null;
          }

          @Override
          public boolean hasNext() {
            if (_nextValue == null) {
              _nextValue = nextValue();
              return _nextValue != null;
            } else {
              return true;
            }
          }

          @Override
          public V next() {
            if (_nextValue == null) {
              return nextValue();
            } else {
              final V value = _nextValue;
              _nextValue = null;
              return value;
            }
          }

          @Override
          public void remove() {
            if (_nullValue != null) {
              if (!replaceNullValue(_nullValue, null)) {
                throw new ConcurrentModificationException();
              }
              _nullValue = null;
            } else {
              _itr.remove();
            }
          }

        };
      }
    };
  }

  public Iterable<Map.Entry<ComputationTargetType, V>> entries() {
    return new Iterable<Map.Entry<ComputationTargetType, V>>() {
      @Override
      public Iterator<Entry<ComputationTargetType, V>> iterator() {
        return new Iterator<Entry<ComputationTargetType, V>>() {

          private final Iterator<Entry<Class<? extends UniqueIdentifiable>, V>> _itr = getUnderlying().entrySet().iterator();
          private Entry<ComputationTargetType, V> _nextEntry = makeNullEntry();
          private Entry<ComputationTargetType, V> _currentEntry;

          private Entry<ComputationTargetType, V> makeNullEntry() {
            final V nullValue = getNullValue();
            if (nullValue == null) {
              return null;
            }
            return new Entry<ComputationTargetType, V>() {

              @Override
              public ComputationTargetType getKey() {
                return ComputationTargetType.NULL;
              }

              @Override
              public V getValue() {
                return nullValue;
              }

              @Override
              public V setValue(final V value) {
                if (replaceNullValue(nullValue, value)) {
                  return nullValue;
                } else {
                  throw new ConcurrentModificationException();
                }
              }

            };
          }

          private Entry<ComputationTargetType, V> nextEntry() {
            while (_itr.hasNext()) {
              final Entry<Class<? extends UniqueIdentifiable>, V> entry = _itr.next();
              if (entry.getValue() != NULL) {
                return new Entry<ComputationTargetType, V>() {

                  @Override
                  public ComputationTargetType getKey() {
                    return ComputationTargetType.of(entry.getKey());
                  }

                  @Override
                  public V getValue() {
                    return entry.getValue();
                  }

                  @Override
                  public V setValue(V value) {
                    if (value == null) {
                      final V previous = entry.getValue();
                      _itr.remove();
                      return previous;
                    } else {
                      return entry.setValue(value);
                    }
                  }

                };
              }
            }
            return null;
          }

          @Override
          public boolean hasNext() {
            if (_nextEntry == null) {
              _nextEntry = nextEntry();
              return _nextEntry != null;
            } else {
              return true;
            }
          }

          @Override
          public Entry<ComputationTargetType, V> next() {
            if (_nextEntry == null) {
              _currentEntry = nextEntry();
              return _currentEntry;
            } else {
              _currentEntry = _nextEntry;
              _nextEntry = null;
              return _currentEntry;
            }
          }

          @Override
          public void remove() {
            _currentEntry.setValue(null);
            _currentEntry = null;
          }

        };
      }
    };
  }

}
