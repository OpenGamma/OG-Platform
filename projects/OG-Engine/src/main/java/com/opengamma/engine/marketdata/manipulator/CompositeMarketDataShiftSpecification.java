/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.util.ArgumentChecker;

/**
 * A market data shift implementation that allows a set of individual market data shifts
 * to be bundled together.
 */
public class CompositeMarketDataShiftSpecification implements MarketDataShiftSpecification {

  /**
   * The underlying shift specifications.
   */
  private final Set<MarketDataShiftSpecification> _underlyingSpecifications;

  private CompositeMarketDataShiftSpecification(Set<MarketDataShiftSpecification> underlyingSpecifications) {
    ArgumentChecker.notEmpty(underlyingSpecifications, "underlyingSpecifications");
    _underlyingSpecifications = underlyingSpecifications;
  }

  /**
   * Create a composite specification for the specified underlying specifiations.
   *
   * @param specifications the specifications to be combined, neither null nor empty
   * @return a specification combined all the underlying specifications, not null
   */
  public static MarketDataShiftSpecification of(MarketDataShiftSpecification... specifications) {
    return new CompositeMarketDataShiftSpecification(ImmutableSet.copyOf(specifications));
  }

  /**
   * Create a composite specification for the specified underlying specifiations.
   *
   * @param specifications the specifications to be combined, neither null nor empty
   * @return a specification combined all the underlying specifications, not null
   */
  public static MarketDataShiftSpecification of(Set<MarketDataShiftSpecification> specifications) {
    return new CompositeMarketDataShiftSpecification(ImmutableSet.copyOf(specifications));
  }

  @Override
  public boolean appliesTo(StructureIdentifier structureId,
                           String calculationConfigurationName) {

    for (MarketDataShiftSpecification specification : _underlyingSpecifications) {
      if (specification.appliesTo(structureId, calculationConfigurationName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public StructureType getApplicableStructureType() {
    return StructureType.NONE;
  }

  @Override
  public StructuredMarketDataSnapshot apply(StructuredMarketDataSnapshot structuredSnapshot) {
    return null;
  }

  @Override
  public boolean containsShifts() {

    for (MarketDataShiftSpecification specification : _underlyingSpecifications) {
      if (specification.containsShifts()) {
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

    CompositeMarketDataShiftSpecification that = (CompositeMarketDataShiftSpecification) o;
    return _underlyingSpecifications.equals(that._underlyingSpecifications);
  }

  @Override
  public int hashCode() {
    return _underlyingSpecifications.hashCode();
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();

    int count = 0;
    for (MarketDataShiftSpecification specification : _underlyingSpecifications) {
      msg.add(count, specification);
      count++;
    }
    return msg;
  }

  public static MarketDataShiftSpecification fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {

    Set<MarketDataShiftSpecification> specs = new HashSet<>();
    for (FudgeField field : msg) {
      msg.getFieldValue(MarketDataShiftSpecification.class, field);
    }

    return of(specs);
  }
}
