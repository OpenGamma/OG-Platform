/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeSize;
import org.fudgemsg.wire.types.FudgeWireType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.WriteReplaceHelper;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * An implementation of {@link ViewComputationCache} which backs value storage on a pair of {@link IdentifierMap} and {@link FudgeMessageStore}.
 */
public class DefaultViewComputationCache implements ViewComputationCache,
    Iterable<Pair<ValueSpecification, FudgeMsg>> {
  
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultViewComputationCache.class);

  /**
   * Callback to locate missing data.
   */
  public interface MissingValueLoader {

    FudgeMsg findMissingValue(long identifier);

    Map<Long, FudgeMsg> findMissingValues(Collection<Long> identifiers);

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
  @SuppressWarnings({"rawtypes", "unchecked" })
  private final ThreadLocal<Map<ValueSpecification, Integer>> _valueSizeCache = new ThreadLocal(); //NOTE: this being thread local is dangerous, but avoids blocking

  private Map<ValueSpecification, Integer> getValueSizeCache() {
    Map<ValueSpecification, Integer> c = _valueSizeCache.get();
    if (c == null) {
      c = new HashMap<ValueSpecification, Integer>();
      _valueSizeCache.set(c);
    }
    return c;
  }

  /**
   * The size of classes which will always have the same size
   */
  private final Map<Class<?>, Integer> _valueSizeByClassCache;

  private void cacheValueSize(final ValueSpecification specification, final FudgeMsg data, final Object value) {
    if (value != null && _valueSizeByClassCache.containsKey(value.getClass())) {
      return;
    }
    final int calculateMessageSize = FudgeSize.calculateMessageSize(data);
    getValueSizeCache().put(specification, calculateMessageSize);
  }

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
    _valueSizeByClassCache = buildValueSizeByClassMap();
  }

  private Map<Class<?>, Integer> buildValueSizeByClassMap() {
    //All of these classes must be have consistent sizes
    final ArrayList<Object> templates = Lists.<Object>newArrayList(Double.valueOf(12.0), MissingInput.MISSING_MARKET_DATA);

    final Map<Class<?>, Integer> valueSizeByClass = new HashMap<Class<?>, Integer>(templates.size());
    for (final Object obj : templates) {
      final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
      final FudgeMsg data = serializeValue(serializer, obj);
      final int size = FudgeSize.calculateMessageSize(data);
      valueSizeByClass.put(obj.getClass(), size);
    }
    return valueSizeByClass;
  }

  public void setMissingValueLoader(final MissingValueLoader missingValueLoader) {
    _missingValueLoader = missingValueLoader;
  }

  public MissingValueLoader getMissingValueLoader() {
    return _missingValueLoader;
  }

  /**
   * Gets the identifierSource field.
   * 
   * @return the identifierSource
   */
  public IdentifierMap getIdentifierMap() {
    return _identifierMap;
  }

  /**
   * Gets the private / local data store.
   * 
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
   * 
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public Object getValue(final ValueSpecification specification) {
    ArgumentChecker.notNull(specification, "Specification");
    final long identifier = getIdentifierMap().getIdentifier(specification);
    FudgeMsg data = getPrivateDataStore().get(identifier);
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
    final FudgeDeserializer deserializer = new FudgeDeserializer(getFudgeContext());
    final Object obj = deserializeValue(deserializer, data);
    cacheValueSize(specification, data, obj);
    return obj;
  }

  @Override
  public Object getValue(final ValueSpecification specification, final CacheSelectHint filter) {
    ArgumentChecker.notNull(specification, "Specification");
    final long identifier = getIdentifierMap().getIdentifier(specification);
    final boolean isPrivate = filter.isPrivateValue(specification);
    final FudgeMsg data = (isPrivate ? getPrivateDataStore() : getSharedDataStore()).get(identifier);
    if (data == null) {
      return null;
    }
    final FudgeDeserializer deserializer = new FudgeDeserializer(getFudgeContext());
    final Object obj = deserializeValue(deserializer, data);
    cacheValueSize(specification, data, obj);
    return obj;
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications) {
    ArgumentChecker.notNull(specifications, "specifications");
    final Map<ValueSpecification, Long> identifiers = getIdentifierMap().getIdentifiers(specifications);
    final Collection<Pair<ValueSpecification, Object>> returnValues = new ArrayList<Pair<ValueSpecification, Object>>(specifications.size());
    final Collection<Long> identifierValues = identifiers.values();
    final FudgeDeserializer deserializer = new FudgeDeserializer(getFudgeContext());
    Map<Long, FudgeMsg> rawValues = getPrivateDataStore().get(identifierValues);
    if (!rawValues.isEmpty()) {
      final Iterator<Map.Entry<ValueSpecification, Long>> identifierIterator = identifiers.entrySet().iterator();
      while (identifierIterator.hasNext()) {
        final Map.Entry<ValueSpecification, Long> identifier = identifierIterator.next();
        final FudgeMsg data = rawValues.get(identifier.getValue());
        if (data != null) {
          final Object value = deserializeValue(deserializer, data);
          cacheValueSize(identifier.getKey(), data, value);
          returnValues.add(Pairs.of(identifier.getKey(), value));
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
        final FudgeMsg data = rawValues.get(identifier.getValue());
        if (data != null) {
          final Object value = deserializeValue(deserializer, data);
          cacheValueSize(identifier.getKey(), data, value);
          returnValues.add(Pairs.of(identifier.getKey(), value));
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
          final FudgeMsg data = rawValues.get(identifier.getValue());
          if (data != null) {
            final Object value = deserializeValue(deserializer, data);
            cacheValueSize(identifier.getKey(), data, value);
            returnValues.add(Pairs.of(identifier.getKey(), value));
            identifierIterator.remove();
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
    for (final ValueSpecification specification : specifications) {
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
    final Map<Long, FudgeMsg> rawValues = new HashMap<Long, FudgeMsg>();
    // TODO Can we overlay the fetch of shared and private data?
    if (sharedIdentifiers != null) {
      if (sharedIdentifiers.size() == 1) {
        final FudgeMsg data = getSharedDataStore().get(sharedIdentifiers.get(0));
        rawValues.put(sharedIdentifiers.get(0), data);
      } else {
        rawValues.putAll(getSharedDataStore().get(sharedIdentifiers));
      }
    }
    if (privateIdentifiers != null) {
      if (privateIdentifiers.size() == 1) {
        final FudgeMsg data = getPrivateDataStore().get(privateIdentifiers.get(0));
        rawValues.put(privateIdentifiers.get(0), data);
      } else {
        rawValues.putAll(getPrivateDataStore().get(privateIdentifiers));
      }
    }
    final FudgeDeserializer deserializer = new FudgeDeserializer(getFudgeContext());
    for (final Map.Entry<ValueSpecification, Long> identifier : identifiers.entrySet()) {
      final FudgeMsg data = rawValues.get(identifier.getValue());
      if (data != null) {
        final Object value = deserializeValue(deserializer, data);
        cacheValueSize(identifier.getKey(), data, value);
        returnValues.add(Pairs.of(identifier.getKey(), value));
      } else {
        returnValues.add(Pairs.of(identifier.getKey(), null));
      }
    }
    return returnValues;
  }

  protected void putValue(final ComputedValue value, final FudgeMessageStore dataStore) {
    ArgumentChecker.notNull(value, "value");
    final long identifier = getIdentifierMap().getIdentifier(value.getSpecification());
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final Object obj = value.getValue();
    final FudgeMsg data = serializeValue(serializer, obj);
    cacheValueSize(value.getSpecification(), data, obj);
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

  protected void putValues(final Collection<? extends ComputedValue> values, final FudgeMessageStore dataStore) {
    ArgumentChecker.notNull(values, "values");
    final Collection<ValueSpecification> specifications = new ArrayList<ValueSpecification>(values.size());
    for (final ComputedValue value : values) {
      specifications.add(value.getSpecification());
    }
    final Map<ValueSpecification, Long> identifiers = getIdentifierMap().getIdentifiers(specifications);
    final Map<Long, FudgeMsg> data = new HashMap<Long, FudgeMsg>();
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    for (final ComputedValue value : values) {
      final Object obj = value.getValue();
      final FudgeMsg valueData = serializeValue(serializer, obj);
      cacheValueSize(value.getSpecification(), valueData, obj);
      data.put(identifiers.get(value.getSpecification()), valueData);
    }
    dataStore.put(data);
  }

  @Override
  public void putPrivateValues(final Collection<? extends ComputedValue> values) {
    putValues(values, getPrivateDataStore());
  }

  @Override
  public void putSharedValues(final Collection<? extends ComputedValue> values) {
    putValues(values, getSharedDataStore());
  }

  @Override
  public void putValues(final Collection<? extends ComputedValue> values, final CacheSelectHint filter) {
    ArgumentChecker.notNull(values, "values");
    final Collection<ValueSpecification> specifications = new ArrayList<ValueSpecification>(values.size());
    for (final ComputedValue value : values) {
      specifications.add(value.getSpecification());
    }
    final Map<ValueSpecification, Long> identifiers = getIdentifierMap().getIdentifiers(specifications);
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    Map<Long, FudgeMsg> privateData = null;
    Map<Long, FudgeMsg> sharedData = null;
    for (final ComputedValue value : values) {
      final Object obj = value.getValue();
      final FudgeMsg valueData = serializeValue(serializer, obj);
      cacheValueSize(value.getSpecification(), valueData, value.getValue());
      if (filter.isPrivateValue(value.getSpecification())) {
        if (privateData == null) {
          privateData = new HashMap<Long, FudgeMsg>();
        }
        privateData.put(identifiers.get(value.getSpecification()), valueData);
      } else {
        if (sharedData == null) {
          sharedData = new HashMap<Long, FudgeMsg>();
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

  protected static FudgeMsg serializeValue(final FudgeSerializer serializer, final Object value) {
    if (value instanceof Double) {
      //Make sure fudge doesn't faff around with reflection
      final MutableFudgeMsg newMessage = serializer.newMessage();
      final FudgeFieldType doubleFieldType = FudgeWireType.DOUBLE;
      newMessage.add(null, NATIVE_FIELD_INDEX, doubleFieldType, value);
      return newMessage;
    } else if (value instanceof FudgeMsg) {
      final MutableFudgeMsg newMessage = serializer.newMessage();
      final FudgeFieldType messageFieldType = FudgeWireType.SUB_MESSAGE;
      newMessage.add(null, NATIVE_FIELD_INDEX, messageFieldType, value);
      return newMessage;
    }
    serializer.reset();
    final MutableFudgeMsg message = serializer.newMessage();
    try {
      serializer.addToMessageWithClassHeaders(message, null, NATIVE_FIELD_INDEX, WriteReplaceHelper.writeReplace(value));
    } catch (FudgeRuntimeException e) {
      s_logger.error("Can't encode value {}", value);
      s_logger.warn("Caught exception", e);
    }
    // Optimize the "value encoded as sub-message" case to reduce space requirement
    final Object svalue = message.getValue(NATIVE_FIELD_INDEX);
    if (svalue instanceof FudgeMsg) {
      return (FudgeMsg) svalue;
    } else {
      return message;
    }
  }

  protected static Object deserializeValue(final FudgeDeserializer deserializer, final FudgeMsg message) {
    deserializer.reset();
    if (message.getNumFields() == 1) {
      final Object value = message.getValue(NATIVE_FIELD_INDEX);
      if (value != null) {
        return value;
      }
    }
    return deserializer.fudgeMsgToObject(message);
  }

  @Override
  public Integer estimateValueSize(final ComputedValue value) {
    if (value.getValue() == null) {
      return null;
    }
    final Integer classSize = _valueSizeByClassCache.get(value.getValue().getClass());
    if (classSize != null) {
      return classSize;
    }
    return getValueSizeCache().get(value.getSpecification());
  }

  @Override
  public Iterator<Pair<ValueSpecification, FudgeMsg>> iterator() {
    // TODO 2008-08-09 Implement this; iterate over the values in the data store
    throw new UnsupportedOperationException();
  }

  /**
   * Remove any underlying resources from the data stores and make the size cache available for garbage collection.
   */
  public void delete() {
    _valueSizeCache.remove(); //TODO this is not right
    getPrivateDataStore().delete();
    if (getSharedDataStore() != getPrivateDataStore()) {
      getSharedDataStore().delete();
    }
  }

}
