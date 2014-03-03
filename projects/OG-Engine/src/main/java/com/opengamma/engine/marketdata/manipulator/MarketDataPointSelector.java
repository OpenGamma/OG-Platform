/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Objects;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Selector for market data points in a dependency graph.
 */
public final class MarketDataPointSelector implements DistinctMarketDataSelector {

  private static final String EXTERNAL_ID = "externalId";
  private final ExternalId _externalId;

  /**
   * Construct a selector for the supplied external id.
   * 
   * @param dataPointId the external id of the market data point to be selected, not null
   * @return a new MarketDataSelector for the market data point, not null
   */
  public static DistinctMarketDataSelector of(ExternalId dataPointId) {
    return new MarketDataPointSelector(dataPointId);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param externalId the data point id, not null
   */
  private MarketDataPointSelector(ExternalId externalId) {
    _externalId = ArgumentChecker.notNull(externalId, EXTERNAL_ID);
  }

  @Override
  public boolean hasSelectionsDefined() {
    return true;
  }

  @Override
  public DistinctMarketDataSelector findMatchingSelector(ValueSpecification valueSpecification, String calculationConfigurationName, SelectorResolver resolver) {
    if (_externalId.equals(createId(valueSpecification))) {
      return this;
    } else {
      return null;
    }
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(EXTERNAL_ID, _externalId);
    return msg;
  }

  @SuppressWarnings("unchecked")
  public static MarketDataSelector fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return new MarketDataPointSelector(msg.getValue(ExternalId.class, EXTERNAL_ID));
  }

  @Override
  public int hashCode() {
    return Objects.hash(_externalId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final MarketDataPointSelector other = (MarketDataPointSelector) obj;
    return Objects.equals(this._externalId, other._externalId);
  }

  private static ExternalId createId(ValueSpecification valueSpecification) {
    if (valueSpecification.getProperty("Id") != null) {
      return ExternalId.parse(valueSpecification.getProperty("Id"));
    } else {
      // Id may not always be present - maybe with snapshots? (get External from UniqueId)
      UniqueId uniqueId = valueSpecification.getTargetSpecification().getUniqueId();
      String scheme = uniqueId.getScheme();
      if (scheme.startsWith("ExternalId-")) {
        scheme = scheme.substring(11);
      }
      // REVIEW 2013-10-11 Andrew -- The above logic is only correct if the requirement was for a single identifier and not a bundle,
      // for example data might have been asked for with tickers from a number of alternative data providers
      return ExternalId.of(scheme, uniqueId.getValue());
    }
  }

  @Override
  public String toString() {
    return _externalId.toString();
  }

}
