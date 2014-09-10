/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.opengamma.core.position.Trade;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

/**
 * Exposure function that combines ids returned by another {@link ExposureFunction}.
 * Can be used to obtain multiple attributes from a trade which should be combined to obtain the curve configuration.
 * for instance, can be used with {@link UnderlyingExposureFunction} to map a basis swap to a single curve based on the
 * two floating rate indicies. The Exposure function to specify for this would be "Combining_Underlying"
 *
 * Values will be obtained from the underlying exposure function and any with the same scheme will be concatenated
 * (separated by _) ordered as returned by the wrapped exposure function).
 */
public class CombiningExposureFunction implements ExposureFunction {

  /**
   * The name of the exposure function.
   */
  public static final String NAME = "Combining" + SEPARATOR;

  private final ExposureFunction _exposureFunction;

  public CombiningExposureFunction(ExposureFunction exposureFunction) {
    _exposureFunction = exposureFunction;
  }

  @Override
  public String getName() {
    return NAME;
  }
  
  @Override
  public List<ExternalId> getIds(Trade trade) {
    List<ExternalId> idList = _exposureFunction.getIds(trade);
    if (idList == null) {
      return null;
    }
    Set<ExternalId> ids = new HashSet<>(idList);

    Map<ExternalScheme, List<String>> schemeMap = new HashMap<>();
    for (ExternalId id : ids) {
      List<String> beforeValue = schemeMap.get(id.getScheme());
      if (beforeValue == null) {
        beforeValue = new ArrayList<>();
        schemeMap.put(id.getScheme(), beforeValue);
      }
      beforeValue.add(id.getValue());
    }
    List<ExternalId> result = new ArrayList<>();
    for (Map.Entry<ExternalScheme, List<String>> entry : schemeMap.entrySet()) {
      String value = StringUtils.join(entry.getValue(), SEPARATOR);
      result.add(ExternalId.of(entry.getKey(), value));
    }
    return result;
  }
}
