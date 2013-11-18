/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;

/**
 * Extracts the structure id for a yield curve from a value specification.
 */
public class YieldCurveNodeExtractor extends NodeExtractor<YieldCurveKey> {

  /**
   * Constructs a yield curve extractor.
   */
  public YieldCurveNodeExtractor() {
    super(ValueRequirementNames.YIELD_CURVE);
  }

  @Override
  public StructureIdentifier<YieldCurveKey> getStructuredIdentifier(final ValueSpecification spec) {
    final Currency currency = Currency.of(spec.getTargetSpecification().getUniqueId().getValue());
    final String curve = getProperty(spec, ValuePropertyNames.CURVE);
    if (curve == null) {
      return null;
    }
    return StructureIdentifier.of(YieldCurveKey.of(currency, curve));
  }
}
