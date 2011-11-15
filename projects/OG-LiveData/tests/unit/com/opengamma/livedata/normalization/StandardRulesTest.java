/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * 
 */
public class StandardRulesTest {
  
  private FieldHistoryStore _store = new FieldHistoryStore();
  
  @Test
  public void noNormalization() {
    NormalizationRuleSet ruleSet = StandardRules.getNoNormalization();
    
    MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    FudgeMsg normalizedMsg = ruleSet.getNormalizedMessage(msg, "123", _store);
    assertEquals(msg, normalizedMsg);
  }
  
}
