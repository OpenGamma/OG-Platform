/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;

/**
 * Extracts the structure id for a market data point from a value specification.
 */
public class MarketDataPointNodeExtractor extends NodeExtractor<ExternalId> {

  /**
   * Constructs an market data point extractor.
   */
  public MarketDataPointNodeExtractor() {
    super(MarketDataRequirementNames.MARKET_VALUE);
  }

  /**
   * Gets the structured key from the passed value specification. The specification will previously have
   * been matched against the configured specification name so can be assumed to be of the correct type.
   *
   * @param spec the specification to construct a key for, not null
   * @return a structured key for the structure handled by the value spec.
   */
  @Override
  public StructureIdentifier<ExternalId> getStructuredIdentifier(ValueSpecification spec) {
    if (spec.getProperty("Id") != null) {
      return StructureIdentifier.of(ExternalId.parse(spec.getProperty("Id")));
    } else {
      // Id may not always be present - maybe with snapshots? (get External from UniqueId)
      UniqueId uniqueId = spec.getTargetSpecification().getUniqueId();
      String scheme = uniqueId.getScheme();
      if (scheme.startsWith("ExternalId-")) {
        scheme = scheme.substring(11);
      }
      return StructureIdentifier.of(ExternalId.of(scheme, uniqueId.getValue()));
    }
  }
}
