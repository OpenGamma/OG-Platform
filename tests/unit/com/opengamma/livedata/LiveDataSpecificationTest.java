/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import static org.junit.Assert.*;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.junit.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentificationScheme;

/**
 * 
 *
 * @author pietari
 */
public class LiveDataSpecificationTest {
  
  @Test
  public void fudge() {
    LiveDataSpecification lds = new LiveDataSpecification("Foo", new Identifier(new IdentificationScheme("bar"), "baz"));
    FudgeFieldContainer container = lds.toFudgeMsg(new FudgeContext());
    
    LiveDataSpecification deserialized = LiveDataSpecification.fromFudgeMsg(container);
    assertNotNull(deserialized);
    assertEquals("Foo", deserialized.getNormalizationRuleSetId());    
    assertEquals("baz", deserialized.getIdentifier(new IdentificationScheme("bar")));
  }

}
