/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.wire.FudgeSize;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * An implementation of {@link ViewComputationCache} which backs value storage on
 * a pair of {@link IdentifierMap} and {@link FudgeMessageStore}.
 */
public class DefaultViewComputationCache implements ViewComputationCache,
    Iterable<Pair<ValueSpecification, FudgeFieldContainer>> {

  /**
   * Callback to locate missing data.
   */
  public static interface MissingValueLoader {

    FudgeFieldContainer findMissingValue(long identifier);

    Map<Long, FudgeFieldContainer> findMissingValues(Collection<Long> identifiers);

  };

  private static final int NATIVE_FIELD_INDEX = -1;

  private final IdentifierMap _identifierMap;
  private final FudgeMessageStore _privateDataStore;
  private final FudgeMessageStore _sharedDataStore;
  private final FudgeContext _fudgeContext;

  private MissingValueLoader _missingValueLoader;

  /**
   * The size of recent values that have gone into or come out of this cache.
   */
  private final Map<ValueSpecification, Integer> _valueSizeCache = Collections.synchronizedMap(new WeakHashMap<ValueSpecification, Integer>());

  protected DefaultViewComputationCache(final IdentifierMap identifierMap, final FudgeMessageStore dataStore,
      final FudgeContext fudgeContext) {
    this(identifierMap, dataStore, dataStore, fudgeContext);
  }

  public DefaultViewComputationCache(final IdentifierMap identifierMap, final FudgeMessageStore privateDataStore,
      final FudgeMessageStore sharedDataStore, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(identifierMap, "Identifier map");
    ArgumentChecker.notNull(privateDataStore, "Private data store");
    ArgumentChecker.notNull(sharedDataStore, "Shared data store");
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    _identifierMap = identifierMap;
    _privateDataStore = privateDataStore;
    _sharedDataStore = sharedDataStore;
    _fudgeContext = fudgeContext;
  }

  public void setMissingValueLoader(final MissingValueLoader missingValueLoader) {
    _missingValueLoader = missingValueLoader;
  }

  public MissingValueLoader getMissingValueLoader() {
    return _missingValueLoader;
  }

  /**
   * Gets the identifierSource field.
   * @return the identifierSource
   */
  public IdentifierMap getIdentifierMap() {
    return _identifierMap;
  }

  /**
   * Gets the private / local data store.
   * @return the dataStore
   */
  public FudgeMessageStore getPrivateDataStore() {
    return _privateDataStore;
  }

  public FudgeMessageStore getSharedDataStore() {
    return _sharedDataStore;
  }

  /**
   * Gets the fudgeContext field.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public Object getValue(final ValueSpecification specification) {
    ArgumentChecker.notNull(specification, "Specification");
    final long identifier = getIdentifierMap().getIdentifier(specification);
    FudgeFieldContainer data = getPrivateDataStore().get(identifier);
    if (data == null) {
      data = getSharedDataStore().get(identifier);
    }
    if (data == null) {
      final MissingValueLoader loader = getMissingValueLoader();
      if (loader == null) {
        return null;
      }
      data = loader.findMissingValue(identifier);
      if (data == null) {
        return null;
      }
    }
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getFudgeContext());
    _valueSizeCache.put(specification, FudgeSize.calculateMessageSize(data));
    return deserializeValue(context, data);
  }

  @Override
  public Object getValue(final ValueSpecification specification, final CacheSelectHint filter) {
    ArgumentChecker.notNull(specification, "Specification");
    final long identifier = getIdentifierMap().getIdentifier(specification);
    FudgeFieldContainer data = (filter.isPrivateValue(specification) ? getPrivateDataStore() : getSharedDataStore())
        .get(identifier);
    if (data == null) {
      return null;
    }
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getFudgeContext());
    _valueSizeCache.put(specification, FudgeSize.calculateMessageSize(data));
    return deserializeValue(context, data);
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications) {
    ArgumentChecker.notNull(specifications, "specifications");
    final Map<ValueSpecification, Long> identifiers = getIdentifierMap().getIdentifiers(specifications);
    final Collection<Pair<ValueSpecification, Object>> returnValues = new ArrayList<Pair<ValueSpecification, Object>>(specifications.size());
    final Collection<Long> identifierValues = identifiers.values();
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getFudgeContext());
    Map<Long, FudgeFieldContainer> rawValues = getPrivateDataStore().get(identifierValues);
    if (!rawValues.isEmpty()) {
      final Iterator<Map.Entry<ValueSpecification, Long>> identifierIterator = identifiers.entrySet().iterator();
      while (identifierIterator.hasNext()) {
        final Map.Entry<ValueSpecification, Long> identifier = identifierIterator.next();
        final FudgeFieldContainer data = rawValues.get(identifier.getValue());
        if (data != null) {
          _valueSizeCache.put(identifier.getKey(), FudgeSize.calculateMessageSize(data));
          returnValues.add(Pair.of(identifier.getKey(), deserializeValue(context, data)));
          identifierIterator.remove();
        }
      }
      if (identifiers.isEmpty()) {
        return returnValues;
      }
    }
    rawValues = getSharedDataStore().get(identifierValues);
    if (!rawValues.isEmpty()) {
      final Iterator<Map.Entry<ValueSpecification, Long>> identifierIterator = identifiers.entrySet().iterator();
      while (identifierIterator.hasNext()) {
        final Map.Entry<ValueSpecification, Long> identifier = identifierIterator.next();
        final FudgeFieldContainer data = rawValues.get(identifier.getValue());
        if (data != null) {
          _valueSizeCache.put(identifier.getKey(), FudgeSize.calculateMessageSize(data));
          returnValues.add(Pair.of(identifier.getKey(), deserializeValue(context, data)));
          identifierIterator.remove();
        }
      }
      if (identifiers.isEmpty()) {
        return returnValues;
      }
    }
    final MissingValueLoader loader = getMissingValueLoader();
    if (loader != null) {
      rawValues = loader.findMissingValues(identifierValues);
      if (!rawValues.isEmpty()) {
        final Iterator<Map.Entry<ValueSpecification, Long>> identifierIterator = identifiers.entrySet().iterator();
        while (identifierIterator.hasNext()) {
          final Map.Entry<ValueSpecification, Long> identifier = identifierIterator.next();
          final FudgeFieldContainer data = rawValues.get(identifier.getValue());
          if (data != null) {
            _valueSizeCache.put(identifier.getKey(), FudgeSize.calculateMessageSize(data));
            returnValues.add(Pair.of(identifier.getKey(), deserializeValue(context, data)));
          }
        }
      }
    }
    return returnValues;
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications, final CacheSelectHint filter) {
    ArgumentChecker.notNull(specifications, "specifications");
    final Map<ValueSpecification, Long> identifiers = getIdentifierMap().getIdentifiers(specifications);
    final Collection<Pair<ValueSpecification, Object>> returnValues = new ArrayList<Pair<ValueSpecification, Object>>(specifications.size());
    List<Long> privateIdentifiers = null;
    List<Long> sharedIdentifiers = null;
    for (ValueSpecification specification : specifications) {
      if (filter.isPrivateValue(specification)) {
        if (privateIdentifiers == null) {
          privateIdentifiers = new ArrayList<Long>(specifications.size());
        }
        privateIdentifiers.add(identifiers.get(specification));
      } else {
        if (sharedIdentifiers == null) {
          sharedIdentifiers = new ArrayList<Long>(specifications.size());
        }
        sharedIdentifiers.add(identifiers.get(specification));
      }
    }
    final Map<Long, FudgeFieldContainer> rawValues = new HashMap<Long, FudgeFieldContainer>();
    // TODO Can we overlay the fetch of shared and private data?
    if (sharedIdentifiers != null) {
      if (sharedIdentifiers.size() == 1) {
        final FudgeFieldContainer data = getSharedDataStore().get(sharedIdentifiers.get(0));
        rawValues.put(sharedIdentifiers.get(0), data);
      } else {
        rawValues.putAll(getSharedDataStore().get(sharedIdentifiers));
      }
    }
    if (privateIdentifiers != null) {
      if (privateIdentifiers.size() == 1) {
        final FudgeFieldContainer data = getPrivateDataStore().get(privateIdentifiers.get(0));
        rawValues.put(privateIdentifiers.get(0), data);
      } else {
        rawValues.putAll(getPrivateDataStore().get(privateIdentifiers));
      }
    }
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getFudgeContext());
    for (Map.Entry<ValueSpecification, Long> identifier : identifiers.entrySet()) {
      final FudgeFieldContainer data = rawValues.get(identifier.getValue());
      if (data != null) {
        _valueSizeCache.put(identifier.getKey(), FudgeSize.calculateMessageSize(data));
        returnValues.add(Pair.of(identifier.getKey(), deserializeValue(context, data)));
      } else {
        returnValues.add(Pair.of(identifier.getKey(), null));
      }
    }
    return returnValues;
  }

  protected void putValue(final ComputedValue value, final FudgeMessageStore dataStore) {
    ArgumentChecker.notNull(value, "value");
    final long identifier = getIdentifierMap().getIdentifier(value.getSpecification());
    final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeContext());
    final FudgeFieldContainer data = serializeValue(context, value.getValue());
    _valueSizeCache.put(value.getSpecification(), FudgeSize.calculateMessageSize(data));
    dataStore.put(identifier, data);
  }

  @Override
  public void putPrivateValue(final ComputedValue value) {
    putValue(value, getPrivateDataStore());
  }

  @Override
  public void putSharedValue(final ComputedValue value) {
    putValue(value, getSharedDataStore());
  }

  @Override
  public void putValue(final ComputedValue value, final CacheSelectHint filter) {
    AbstractViewComputationCache.putValue(this, value, filter);
  }

  protected void putValues(final Collection<ComputedValue> values, final FudgeMessageStore dataStore) {
    ArgumentChecker.notNull(values, "values");
    final Collection<ValueSpecification> specifications = new ArrayList<ValueSpecification>(values.size());
    for (ComputedValue value : values) {
      specifications.add(value.getSpecification());
    }
    final Map<ValueSpecification, Long> identifiers = getIdentifierMap().getIdentifiers(specifications);
    final Map<Long, FudgeFieldContainer> data = new HashMap<Long, FudgeFieldContainer>();
    final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeContext());
    for (ComputedValue value : values) {
      final FudgeFieldContainer valueData = serializeValue(context, value.getValue());
      _valueSizeCache.put(value.getSpecification(), FudgeSize.calculateMessageSize(valueData));
      data.put(identifiers.get(value.getSpecification()), valueData);
    }
    dataStore.put(data);
  }

  @Override
  public void putPrivateValues(final Collection<ComputedValue> values) {
    putValues(values, getPrivateDataStore());
  }

  @Override
  public void putSharedValues(final Collection<ComputedValue> values) {
    putValues(values, getSharedDataStore());
  }

  @Override
  public void putValues(final Collection<ComputedValue> values, final CacheSelectHint filter) {
    ArgumentChecker.notNull(values, "values");
    final Collection<ValueSpecification> specifications = new ArrayList<ValueSpecification>(values.size());
    for (ComputedValue value : values) {
      specifications.add(value.getSpecification());
    }
    final Map<ValueSpecification, Long> identifiers = getIdentifierMap().getIdentifiers(specifications);
    final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeContext());
    Map<Long, FudgeFieldContainer> privateData = null;
    Map<Long, FudgeFieldContainer> sharedData = null;
    for (ComputedValue value : values) {
      final FudgeFieldContainer valueData = serializeValue(context, value.getValue());
      _valueSizeCache.put(value.getSpecification(), FudgeSize.calculateMessageSize(valueData));
      if (filter.isPrivateValue(value.getSpecification())) {
        if (privateData == null) {
          privateData = new HashMap<Long, FudgeFieldContainer>();
        }
        privateData.put(identifiers.get(value.getSpecification()), valueData);
      } else {
        if (sharedData == null) {
          sharedData = new HashMap<Long, FudgeFieldContainer>();
        }
        sharedData.put(identifiers.get(value.getSpecification()), valueData);
      }
    }
    // TODO 2010-08-31 Andrew -- can we overlay the shared and private puts ?
    if (sharedData != null) {
      getSharedDataStore().put(sharedData);
    }
    if (privateData != null) {
      getPrivateDataStore().put(privateData);
    }
  }

  protected static FudgeFieldContainer serializeValue(final FudgeSerializationContext context, final Object value) {
    context.reset();
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsgWithClassHeaders(message, null, NATIVE_FIELD_INDEX, value);
    // Optimize the "value encoded as sub-message" case to reduce space requirement
    Object svalue = message.getValue(NATIVE_FIELD_INDEX);
    if (svalue instanceof FudgeFieldContainer) {
      return (FudgeFieldContainer) svalue;
    } else {
      return message;
    }
  }

  protected static Object deserializeValue(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    context.reset();
    if (message.getNumFields() == 1) {
      Object value = message.getValue(NATIVE_FIELD_INDEX);
      if (value != null) {
        return value;
      }
    }
    return context.fudgeMsgToObject(message);
  }

  @Override
  public Integer estimateValueSize(final ComputedValue value) {
    return _valueSizeCache.get(value.getSpecification());
  }

  @Override
  public Iterator<Pair<ValueSpecification, FudgeFieldContainer>> iterator() {
    // TODO 2008-08-09 Implement this; iterate over the values in the data store
    throw new UnsupportedOperationException();
  }

  /**
   * Remove any underlying resources from the data stores and make the size cache available for garbage
   * collection. 
   */
  public void delete() {
    _valueSizeCache.clear();
    getPrivateDataStore().delete();
    if (getSharedDataStore() != getPrivateDataStore()) {
      getSharedDataStore().delete();
    }
  }

}
