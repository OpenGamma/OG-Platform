/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;

/**
 * Test LiveDataSpecification.
 */
@Test
public class LiveDataSpecificationTest {
  
  public static final IdentificationScheme TEST_IDENTIFICATION_SCHEME = IdentificationScheme.of("bar");
  public static final LiveDataSpecification TEST_LIVE_DATA_SPEC = new LiveDataSpecification("Foo", Identifier.of(TEST_IDENTIFICATION_SCHEME, "baz"));
  
  public void fudge() {
    FudgeFieldContainer container = TEST_LIVE_DATA_SPEC.toFudgeMsg(new FudgeContext());
    
    LiveDataSpecification deserialized = LiveDataSpecification.fromFudgeMsg(container);
    AssertJUnit.assertNotNull(deserialized);
    AssertJUnit.assertEquals("Foo", deserialized.getNormalizationRuleSetId());    
    AssertJUnit.assertEquals("baz", deserialized.getIdentifier(IdentificationScheme.of("bar")));
  }

}
