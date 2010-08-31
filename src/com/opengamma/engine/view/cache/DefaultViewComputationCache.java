/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * An implementation of {@link ViewComputationCache} which backs value storage on
 * a pair of {@link IdentifierMap} and {@link BinaryDataStore}.
 */
public class DefaultViewComputationCache implements ViewComputationCache, Iterable<Pair<ValueSpecification, byte[]>> {

  private static final int NATIVE_FIELD_INDEX = -1;

  private final IdentifierMap _identifierMap;
  private final BinaryDataStore _privateDataStore;
  private final BinaryDataStore _sharedDataStore;
  private final FudgeContext _fudgeContext;

  protected DefaultViewComputationCache(final IdentifierMap identifierMap, final BinaryDataStore dataStore, final FudgeContext fudgeContext) {
    this(identifierMap, dataStore, dataStore, fudgeContext);
  }

  public DefaultViewComputationCache(final IdentifierMap identifierMap, final BinaryDataStore privateDataStore, final BinaryDataStore sharedDataStore, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(identifierMap, "Identifier map");
    ArgumentChecker.notNull(privateDataStore, "Private data store");
    ArgumentChecker.notNull(sharedDataStore, "Shared data store");
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    _identifierMap = identifierMap;
    _privateDataStore = privateDataStore;
    _sharedDataStore = sharedDataStore;
    _fudgeContext = fudgeContext;
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
  public BinaryDataStore getPrivateDataStore() {
    return _privateDataStore;
  }

  public BinaryDataStore getSharedDataStore() {
    return _sharedDataStore;
  }

  /**
   * Gets the fudgeContext field.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  // TODO Remove this debug timing code and print statements etc ...

  private int _identifierRequests;
  private long _getIdentifierTime;
  private int _dataRequests;
  private long _getDataTime;
  private long _deserializeTime;
  private long _serializeTime;
  private int _putRequests;
  private long _putDataTime;

  public void reportTimes() {
    System.err.println("getIdentifier=" + ((double) _getIdentifierTime / 1000000d) + "ms (" + _identifierRequests + "), getData=" + ((double) _getDataTime / 1000000d) + "ms (" + _dataRequests
        + "), deserialize=" + ((double) _deserializeTime / 1000000d) + "ms, serialize=" + ((double) _serializeTime / 1000000d) + "ms, putData=" + ((double) _putDataTime / 1000000d) + "ms ("
        + _putRequests + ")");
  }

  public void resetTimes() {
    _identifierRequests = 0;
    _getIdentifierTime = 0;
    _dataRequests = 0;
    _getDataTime = 0;
    _deserializeTime = 0;
    _serializeTime = 0;
    _putRequests = 0;
    _putDataTime = 0;
  }

  @Override
  public Object getValue(final ValueSpecification specification) {
    ArgumentChecker.notNull(specification, "Specification");
    _identifierRequests++;
    _getIdentifierTime -= System.nanoTime();
    final long identifier = getIdentifierMap().getIdentifier(specification);
    _getIdentifierTime += System.nanoTime();
    _dataRequests++;
    _getDataTime -= System.nanoTime();
    byte[] data = getPrivateDataStore().get(identifier);
    if (data == null) {
      data = getSharedDataStore().get(identifier);
    }
    _getDataTime += System.nanoTime();
    if (data == null) {
      return null;
    }
    _deserializeTime -= System.nanoTime();
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getFudgeContext());
    final Object value = deserializeValue(context, data);
    _deserializeTime += System.nanoTime();
    return value;
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications) {
    ArgumentChecker.notNull(specifications, "specifications");
    _identifierRequests += specifications.size();
    _getIdentifierTime -= System.nanoTime();
    final Map<ValueSpecification, Long> identifiers = getIdentifierMap().getIdentifiers(specifications);
    _getIdentifierTime += System.nanoTime();
    final Collection<Pair<ValueSpecification, Object>> returnValues = new ArrayList<Pair<ValueSpecification, Object>>(specifications.size());
    _dataRequests += specifications.size();
    _getDataTime -= System.nanoTime();
    final Collection<Long> identifierValues = identifiers.values();
    Map<Long, byte[]> rawValues = getPrivateDataStore().get(identifierValues);
    if (!rawValues.isEmpty()) {
      _getDataTime += System.nanoTime();
      _deserializeTime -= System.nanoTime();
      final FudgeDeserializationContext context = new FudgeDeserializationContext(getFudgeContext());
      for (Map.Entry<ValueSpecification, Long> identifier : identifiers.entrySet()) {
        final byte[] data = rawValues.get(identifier.getValue());
        returnValues.add(Pair.of(identifier.getKey(), (data != null) ? deserializeValue(context, data) : null));
      }
      _deserializeTime += System.nanoTime();
      if (returnValues.size() == identifierValues.size()) {
        return returnValues;
      }
      _getDataTime -= System.nanoTime();
    }
    rawValues = getSharedDataStore().get(identifierValues);
    _getDataTime += System.nanoTime();
    _deserializeTime -= System.nanoTime();
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getFudgeContext());
    for (Map.Entry<ValueSpecification, Long> identifier : identifiers.entrySet()) {
      final byte[] data = rawValues.get(identifier.getValue());
      returnValues.add(Pair.of(identifier.getKey(), (data != null) ? deserializeValue(context, data) : null));
    }
    _deserializeTime += System.nanoTime();
    return returnValues;
  }

  protected void putValue(final ComputedValue value, final BinaryDataStore dataStore) {
    ArgumentChecker.notNull(value, "value");
    _identifierRequests++;
    _getIdentifierTime -= System.nanoTime();
    final long identifier = getIdentifierMap().getIdentifier(value.getSpecification());
    _getIdentifierTime += System.nanoTime();
    _putRequests++;
    _serializeTime -= System.nanoTime();
    final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeContext());
    final byte[] data = serializeValue(context, value.getValue());
    _serializeTime += System.nanoTime();
    _putDataTime -= System.nanoTime();
    dataStore.put(identifier, data);
    _putDataTime += System.nanoTime();
  }

  @Override
  public void putPrivateValue(final ComputedValue value) {
    putValue(value, getPrivateDataStore());
  }

  @Override
  public void putSharedValue(final ComputedValue value) {
    putValue(value, getSharedDataStore());
  }

  protected void putValues(final Collection<ComputedValue> values, final BinaryDataStore dataStore) {
    ArgumentChecker.notNull(values, "values");
    _identifierRequests += values.size();
    _getIdentifierTime -= System.nanoTime();
    final Collection<ValueSpecification> specifications = new ArrayList<ValueSpecification>(values.size());
    for (ComputedValue value : values) {
      specifications.add(value.getSpecification());
    }
    final Map<ValueSpecification, Long> identifiers = getIdentifierMap().getIdentifiers(specifications);
    _getIdentifierTime += System.nanoTime();
    _putRequests += values.size();
    _serializeTime -= System.nanoTime();
    final Map<Long, byte[]> data = new HashMap<Long, byte[]>();
    final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeContext());
    for (ComputedValue value : values) {
      data.put(identifiers.get(value.getSpecification()), serializeValue(context, value.getValue()));
    }
    _serializeTime += System.nanoTime();
    _putDataTime -= System.nanoTime();
    dataStore.put(data);
    _putDataTime += System.nanoTime();
  }

  @Override
  public void putPrivateValues(final Collection<ComputedValue> values) {
    putValues(values, getPrivateDataStore());
  }

  @Override
  public void putSharedValues(final Collection<ComputedValue> values) {
    putValues(values, getSharedDataStore());
  }

  protected static byte[] serializeValue(final FudgeSerializationContext context, final Object value) {
    context.reset();
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsgWithClassHeaders(message, null, NATIVE_FIELD_INDEX, value);
    final byte[] data;
    // Optimize the "value encoded as sub-message" case to reduce space requirement
    Object svalue = message.getValue(NATIVE_FIELD_INDEX);
    if (svalue instanceof FudgeFieldContainer) {
      data = context.getFudgeContext().toByteArray((FudgeFieldContainer) svalue);
    } else {
      data = context.getFudgeContext().toByteArray(message);
    }
    return data;
  }

  protected static Object deserializeValue(final FudgeDeserializationContext context, final byte[] data) {
    context.reset();
    final FudgeFieldContainer message = context.getFudgeContext().deserialize(data).getMessage();
    if (message.getNumFields() == 1) {
      Object value = message.getValue(NATIVE_FIELD_INDEX);
      if (value != null) {
        return value;
      }
    }
    return context.fudgeMsgToObject(message);
  }

  @Override
  public Iterator<Pair<ValueSpecification, byte[]>> iterator() {
    // TODO 2008-08-09 Implement this; iterate over the values in the data store
    throw new UnsupportedOperationException();
  }

}
