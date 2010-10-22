/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.junit.Test;

import com.opengamma.engine.value.ValueProperties;

/**
 * Test the {@link ValuePropertiesBuilder} class.
 */
public class ValuePropertiesBuilderTest extends AbstractBuilderTestCase {

  @Test
  public void testEmptyProperties() {
    assertEncodeDecodeCycle(ValueProperties.class, ValueProperties.none());
  }

  @Test
  public void testValues() {
    assertEncodeDecodeCycle(ValueProperties.class, ValueProperties.builder().withAny("Any").with("One", "a").with("Two", "b", "c").get());
  }

}
