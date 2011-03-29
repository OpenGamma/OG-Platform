/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.testng.annotations.Test;
import com.opengamma.engine.value.ValueProperties;

/**
 * Test the {@link ValuePropertiesBuilder} class.
 */
@Test
public class ValuePropertiesBuilderTest extends AbstractBuilderTestCase {

  public void testEmptyProperties() {
    assertEncodeDecodeCycle(ValueProperties.class, ValueProperties.none());
  }

  public void testAllProperties() {
    assertEncodeDecodeCycle(ValueProperties.class, ValueProperties.all());
  }
  
  public void testNearlyAllProperties() {//PLAT-1126
    assertEncodeDecodeCycle(ValueProperties.class, ValueProperties.all().withoutAny("SomeProp"));
  }

  public void testValues() {
    assertEncodeDecodeCycle(ValueProperties.class, ValueProperties.builder().withAny("Any").with("One", "a").with("Two", "b", "c").withOptional("Three").get());
  }

}
