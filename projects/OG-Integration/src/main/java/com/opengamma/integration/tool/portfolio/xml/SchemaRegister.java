/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.integration.tool.portfolio.xml.v1_0.conversion.PortfolioConversionV1_0;

public class SchemaRegister {

  private static final Map<SchemaVersion, ? extends PortfolioConversion> register = ImmutableMap.of(
      new SchemaVersion("1.0"), new PortfolioConversionV1_0()
  );

  public PortfolioConversion getConverterForSchema(SchemaVersion version) {
    return register.get(version);
  }

}
