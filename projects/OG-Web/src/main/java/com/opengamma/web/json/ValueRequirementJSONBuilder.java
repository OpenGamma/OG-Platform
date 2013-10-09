/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts {@link ValueRequirement} instances to and from JSON using Fudge encoding.
 */
public class ValueRequirementJSONBuilder extends AbstractJSONBuilder<ValueRequirement> {

  @Override
  public ValueRequirement fromJSON(String json) {
    ArgumentChecker.notEmpty(json, "json");
    return fromJSON(ValueRequirement.class, json);
  }

  @Override
  public String toJSON(ValueRequirement valueRequirement) {
    ArgumentChecker.notNull(valueRequirement, "valueRequirement");
    return fudgeToJson(valueRequirement);
  }

  @Override
  public String getTemplate() {
    return null; // not needed
  }
}
