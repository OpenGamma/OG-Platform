/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calc.ComputationCacheQuery;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

public class ComputationCacheQueryBuilderTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void testEmptyQuery() {
    ComputationCacheQuery query = new ComputationCacheQuery();
    query.setCalculationConfigurationName("DEFAULT");
    query.setValueSpecifications(new ArrayList<ValueSpecification>());
    checkCycle(query);
  }
  
  @Test
  public void testSingleQuery() {
    ComputationCacheQuery query = new ComputationCacheQuery();
    query.setCalculationConfigurationName("DEFAULT");
    ValueSpecification spec = ValueSpecification.of("SomeValue", UniqueId.of("SomeScheme","SomeValue"), "SomeFunc", "USD", ValueProperties.none());
    query.setValueSpecifications(Lists.newArrayList(spec));
    
    checkCycle(query);
  }
  
  @Test
  public void testMultipleQuery() {
    ComputationCacheQuery query = new ComputationCacheQuery();
    query.setCalculationConfigurationName("DEFAULT");
    ValueSpecification spec = ValueSpecification.of("SomeValue", UniqueId.of("SomeScheme","SomeValue"), "SomeFunc", "USD", ValueProperties.none());
    ValueSpecification spec2 = ValueSpecification.of("SomeOtherValue", UniqueId.of("SomeScheme","SomeOtherValue"), "SomeOtherFunc", "USD", ValueProperties.none());
    query.setValueSpecifications(Lists.newArrayList(spec, spec2));
    
    checkCycle(query);
  }

  private void checkCycle(ComputationCacheQuery query) {
    ComputationCacheQuery cycled = cycleObject(ComputationCacheQuery.class, query);
    
    
    assertEquals(query.getCalculationConfigurationName(), cycled.getCalculationConfigurationName());
    assertEquals(query.getValueSpecifications(), cycled.getValueSpecifications());
  }
}
