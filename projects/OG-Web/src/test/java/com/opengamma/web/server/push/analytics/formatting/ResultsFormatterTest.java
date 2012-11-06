/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;

/**
 *
 */
public class ResultsFormatterTest {

  /**
   * For a specific bug
   */
  @Test
  public void formatHistoryForValueNameWithUnknownType() {
    ResultsFormatter formatter = new ResultsFormatter();
    UniqueId uid = UniqueId.of("scheme", "value");
    ComputationTargetSpecification spec = new ComputationTargetSpecification(ComputationTargetType.POSITION, uid);
    ValueProperties props = ValueProperties.builder().with(ValuePropertyNames.FUNCTION, "fn").get();
    // if this works without an exception then the bug is fixed
    formatter.format(123d, new ValueSpecification("unknown value name", spec, props), TypeFormatter.Format.HISTORY);
  }
}
