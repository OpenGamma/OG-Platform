/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;

/**
 * A market data shift implementation that allows a set of individual market data shifts
 * to be bundled together.
 */
public final class CompositeMarketDataSelector implements MarketDataSelector {

  /** Field name for Fudge message */
  private static final String SELECTORS = "selectors";
  /**
   * The underlying shift specifications.
   */
  private final Set<MarketDataSelector> _underlyingSpecifications;

  private CompositeMarketDataSelector(Set<MarketDataSelector> underlyingSpecifications) {
    // TODO should this allow an empty set? what if you want a scenario that doesn't change anything as part of a simulation?
    ArgumentChecker.notEmpty(underlyingSpecifications, "underlyingSpecifications");
    _underlyingSpecifications = underlyingSpecifications;
  }

  /**
   * Create a composite specification for the specified underlying specifiations.
   *
   * @param specifications the specifications to be combined, neither null nor empty
   * @return a specification combined all the underlying specifications, not null
   */
  public static MarketDataSelector of(MarketDataSelector... specifications) {
    return new CompositeMarketDataSelector(ImmutableSet.copyOf(specifications));
  }

  /**
   * Create a composite specification for the specified underlying specifiations.
   *
   * @param specifications the specifications to be combined, neither null nor empty
   * @return a specification combined all the underlying specifications, not null
   */
  public static MarketDataSelector of(Set<? extends MarketDataSelector> specifications) {
    return new CompositeMarketDataSelector(ImmutableSet.copyOf(specifications));
  }

  @Override
  public DistinctMarketDataSelector findMatchingSelector(StructureIdentifier<?> structureId,
                                                         String calculationConfigurationName,
                                                         SelectorResolver resolver) {

    for (MarketDataSelector specification : _underlyingSpecifications) {

      DistinctMarketDataSelector matchingSelector =
          specification.findMatchingSelector(structureId, calculationConfigurationName, resolver);
      if (matchingSelector != null) {
        return matchingSelector;
      }
    }
    return null;
  }

  @Override
  public Set<StructureType> getApplicableStructureTypes() {

    Set<StructureType> types = new HashSet<>();
    for (MarketDataSelector specification : _underlyingSpecifications) {
      types.addAll(specification.getApplicableStructureTypes());
    }
    return Collections.unmodifiableSet(types);
  }

  @Override
  public boolean hasSelectionsDefined() {

    for (MarketDataSelector specification : _underlyingSpecifications) {
      if (specification.hasSelectionsDefined()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CompositeMarketDataSelector that = (CompositeMarketDataSelector) o;
    return _underlyingSpecifications.equals(that._underlyingSpecifications);
  }

  @Override
  public int hashCode() {
    return _underlyingSpecifications.hashCode();
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    for (MarketDataSelector selector : _underlyingSpecifications) {
      serializer.addToMessageWithClassHeaders(msg, SELECTORS, null, selector);
    }
    return msg;
  }

  public static MarketDataSelector fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    Set<MarketDataSelector> selectors = Sets.newHashSet();
    for (FudgeField field : msg.getAllByName(SELECTORS)) {
      MarketDataSelector selector = deserializer.fieldValueToObject(MarketDataSelector.class, field);
      selectors.add(selector);
    }
    return of(selectors);
  }
}
