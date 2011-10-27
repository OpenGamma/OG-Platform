/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.engine.value.ValueProperties;

/**
 * Hibernate bean.
 */
public class RiskValueSpecification extends RiskValueProperties {

  private int _valueRequirementId;

  public RiskValueSpecification() {
  }

  public RiskValueSpecification(ValueProperties properties) {
    super(properties);
  }

  public int getValueRequirementId() {
    return _valueRequirementId;
  }

  public void setValueRequirementId(int valueRequirementId) {
    this._valueRequirementId = valueRequirementId;
  }
}
