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
public class RiskValueRequirement extends RiskValueProperties {


  public RiskValueRequirement() {
  }

  public RiskValueRequirement(ValueProperties requirement) {
    super(requirement);
  }

}
