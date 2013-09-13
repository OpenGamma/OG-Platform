/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.json;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.json.ValueRequirementJSONBuilder;

/**
 * Wraps {@link ValueRequirement} so it can be used as a form parameter in a JAX-RS request.
 */
public class ValueRequirementFormParam {

  private final ValueRequirement _valueRequirement;

  /**
   * @param json A {@link ValueRequirement} encoded as Fudge JSON (see {@link ValueRequirementJSONBuilder}).
   */
  public ValueRequirementFormParam(String json) {
    ArgumentChecker.notEmpty(json, "json");
    _valueRequirement = new ValueRequirementJSONBuilder().fromJSON(json);
  }

  /**
   * @return The value requirement, not null
   */
  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
  }
}
