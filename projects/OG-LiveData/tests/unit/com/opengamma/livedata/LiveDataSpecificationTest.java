/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ExternalId;

/**
 * Test LiveDataSpecification.
 */
@Test
public class LiveDataSpecificationTest {
  
  public static final ExternalScheme TEST_IDENTIFICATION_SCHEME = ExternalScheme.of("bar");
  public static final LiveDataSpecification TEST_LIVE_DATA_SPEC = new LiveDataSpecification("Foo", ExternalId.of(TEST_IDENTIFICATION_SCHEME, "baz"));
  
  public void fudge() {
    FudgeContext fudgeContext = new FudgeContext();
    FudgeMsg container = TEST_LIVE_DATA_SPEC.toFudgeMsg(fudgeContext);
    
    LiveDataSpecification deserialized = LiveDataSpecification.fromFudgeMsg(new FudgeDeserializer(fudgeContext), container);
    AssertJUnit.assertNotNull(deserialized);
    AssertJUnit.assertEquals("Foo", deserialized.getNormalizationRuleSetId());    
    AssertJUnit.assertEquals("baz", deserialized.getIdentifier(ExternalScheme.of("bar")));
  }

}
