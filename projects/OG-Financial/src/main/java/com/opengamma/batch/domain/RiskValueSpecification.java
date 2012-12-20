/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.domain;

import com.opengamma.engine.value.ValueProperties;


public class RiskValueSpecification extends RiskValueProperties {

  public RiskValueSpecification() {
  }

  public RiskValueSpecification(ValueProperties properties) {
    super(properties);
  }

}
