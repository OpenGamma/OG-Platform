/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Iterator;

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
  private final BinaryDataStore _dataStore;
  private final FudgeContext _fudgeContext;

  public DefaultViewComputationCache(IdentifierMap identifierMap, BinaryDataStore dataStore, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(identifierMap, "Identifier map");
    ArgumentChecker.notNull(dataStore, "Data Store");
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    _identifierMap = identifierMap;
    _dataStore = dataStore;
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
   * Gets the dataStore field.
   * @return the dataStore
   */
  public BinaryDataStore getDataStore() {
    return _dataStore;
  }

  /**
   * Gets the fudgeContext field.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public Object getValue(ValueSpecification specification) {
    ArgumentChecker.notNull(specification, "Specification");
    final long identifier = getIdentifierMap().getIdentifier(specification);
    return getValue(identifier);
  }

  public Object getValue(final long identifier) {
    byte[] data = getDataStore().get(identifier);
    if (data == null) {
      return null;
    }
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getFudgeContext());
    final FudgeFieldContainer message = getFudgeContext().deserialize(data).getMessage();
    if (message.getNumFields() == 1) {
      Object value = message.getValue(-1);
      if (value != null) {
        return value;
      }
    }
    return context.fudgeMsgToObject(message);
  }

  @Override
  public void putValue(ComputedValue value) {
    ArgumentChecker.notNull(value, "Computed value");
    final long identifier = getIdentifierMap().getIdentifier(value.getSpecification());
    putValue(identifier, value.getValue());
  }

  public void putValue(final long identifier, final Object value) {
    final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeContext());
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsgWithClassHeaders(message, null, NATIVE_FIELD_INDEX, value);
    final byte[] data;
    // Optimize the "value encoded as sub-message" case to reduce space requirement
    Object svalue = message.getValue(NATIVE_FIELD_INDEX);
    if (svalue instanceof FudgeFieldContainer) {
      data = getFudgeContext().toByteArray((FudgeFieldContainer) svalue);
    } else {
      data = getFudgeContext().toByteArray(message);
    }
    getDataStore().put(identifier, data);
  }

  @Override
  public Iterator<Pair<ValueSpecification, byte[]>> iterator() {
    // TODO 2008-08-09 Implement this; iterate over the values in the data store
    throw new UnsupportedOperationException();
  }

}
