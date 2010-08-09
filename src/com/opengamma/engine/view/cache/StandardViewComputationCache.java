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
 * a pair of {@link ValueSpecificationIdentifierSource} and {@link ValueSpecificationIdentifierBinaryDataStore}.
 */
public class StandardViewComputationCache implements ViewComputationCache, Iterable<Pair<ValueSpecification,byte[]>> {

  private static final int NATIVE_FIELD_INDEX = -1;

  private final ValueSpecificationIdentifierSource _identifierSource;
  private final ValueSpecificationIdentifierBinaryDataStore _dataStore;
  private final FudgeContext _fudgeContext;

  public StandardViewComputationCache(ValueSpecificationIdentifierSource identifierSource, ValueSpecificationIdentifierBinaryDataStore dataStore, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(identifierSource, "Identifier Source");
    ArgumentChecker.notNull(dataStore, "Data Store");
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    _identifierSource = identifierSource;
    _dataStore = dataStore;
    _fudgeContext = fudgeContext;
  }

  /**
   * Gets the identifierSource field.
   * @return the identifierSource
   */
  public ValueSpecificationIdentifierSource getIdentifierSource() {
    return _identifierSource;
  }

  /**
   * Gets the dataStore field.
   * @return the dataStore
   */
  public ValueSpecificationIdentifierBinaryDataStore getDataStore() {
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
    long identifier = getIdentifierSource().getIdentifier(specification);
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
    long identifier = getIdentifierSource().getIdentifier(value.getSpecification());
    final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeContext());
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsgWithClassHeaders(message, null, NATIVE_FIELD_INDEX, value.getValue());
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
    // TODO 2008-08-09 Implement this; iterate over the values
    throw new UnsupportedOperationException();
  }

}
