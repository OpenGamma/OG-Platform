/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.io.Serializable;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.ArgumentChecker;

/**
 * A market data selector the performs an exact match on a node in the dependency graph.
 *
 * @param <K> the type of the identifier of the underlying data
 */
public abstract class ExactMatchMarketDataSelector<K extends Serializable> implements DistinctMarketDataSelector {

  /**
   * The external id of the data point to be selected.
   */
  private final StructureIdentifier<K> _structureId;

  /**
   * Creates a new seelctor with the specified structure id.
   *
   * @param structureId the structure id to match with
   */
  public ExactMatchMarketDataSelector(StructureIdentifier<K> structureId) {
    ArgumentChecker.notNull(structureId, "structureId");
    _structureId = structureId;
  }

  @Override
  public boolean hasSelectionsDefined() {
    return true;
  }

  @Override
  public DistinctMarketDataSelector findMatchingSelector(StructureIdentifier<?> structureId,
                                                         String calculationConfigurationName,
                                                         SelectorResolver resolver) {
    return _structureId.equals(structureId) ? this : null;
  }

  protected StructureIdentifier<K> getStructureId() {
    return _structureId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return _structureId.equals(((ExactMatchMarketDataSelector) o)._structureId);
  }

  @Override
  public int hashCode() {
    return _structureId.hashCode();
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("structureId", _structureId);
    return msg;
  }
}
