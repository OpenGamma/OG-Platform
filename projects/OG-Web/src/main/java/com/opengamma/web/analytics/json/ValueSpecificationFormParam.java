/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.json;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.json.ValueRequirementJSONBuilder;
import com.opengamma.web.json.ValueSpecificationJSONBuilder;

/**
 * Wraps {@link ValueRequirement} so it can be used as a form parameter in a JAX-RS request.
 */
public class ValueSpecificationFormParam {

  private final ValueSpecification _valueSpecification;

  /**
   * @param json A {@link ValueRequirement} encoded as Fudge JSON (see {@link ValueRequirementJSONBuilder}).
   */
  public ValueSpecificationFormParam(String json) {
    ArgumentChecker.notEmpty(json, "json");
    _valueSpecification = new ValueSpecificationJSONBuilder().fromJSON(json);
  }

  /**
   * @return The value requirement, not null
   */
  public ValueSpecification getValueSpecification() {
    return _valueSpecification;
  }
}
