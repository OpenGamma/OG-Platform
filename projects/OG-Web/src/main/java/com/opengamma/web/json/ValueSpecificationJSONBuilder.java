/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts {@link ValueRequirement} instances to and from JSON using Fudge encoding.
 */
public class ValueSpecificationJSONBuilder extends AbstractJSONBuilder<ValueSpecification> {

  @Override
  public ValueSpecification fromJSON(String json) {
    ArgumentChecker.notEmpty(json, "json");
    return fromJSON(ValueSpecification.class, json);
  }

  @Override
  public String toJSON(ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(valueSpecification, "valueRequirement");
    return fudgeToJson(valueSpecification);
  }

  @Override
  public String getTemplate() {
    return null; // not needed
  }
}
