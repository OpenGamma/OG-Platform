/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opengamma.web.json;


import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.taxonomy.FudgeTaxonomy;
import org.fudgemsg.taxonomy.ImmutableMapTaxonomyResolver;
import org.fudgemsg.taxonomy.MapFudgeTaxonomy;
import org.fudgemsg.test.FudgeUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge JSON.
 */
@Test(groups = TestGroup.UNIT)
public class FudgeJSONTest {
  
  private final FudgeContext _fudgeContext;

  private static FudgeTaxonomy getTaxonomy() {
    return new MapFudgeTaxonomy (
        new int[] { 1, 2, 3, 4, 5, 6 },
        new String[] { "boolean", "byte", "int", "string", "float", "double" }
        );
  }
  
  /**
   * 
   */
  public FudgeJSONTest() {
    _fudgeContext = new FudgeContext();
    final Map<Short,FudgeTaxonomy> tr = new HashMap<Short,FudgeTaxonomy>();
    tr.put ((short)1, getTaxonomy());
    _fudgeContext.setTaxonomyResolver (new ImmutableMapTaxonomyResolver(tr));
  }
  
  private FudgeMsg[] createMessages() {
    return new FudgeMsg[] {
        StandardFudgeMessages.createMessageAllNames(_fudgeContext),
        StandardFudgeMessages.createMessageAllOrdinals(_fudgeContext),
        StandardFudgeMessages.createMessageWithSubMsgs(_fudgeContext),
        StandardFudgeMessages.createMessageAllByteArrayLengths(_fudgeContext) };
  }
    
  /**
   * 
   */
  @Test
  public void cycleJSONMessages_noTaxonomy() {
//    System.out.println("cycleJSONMessages:");
    
    final FudgeMsg[] messages = createMessages();
    for (int i = 0; i < messages.length; i++) {
      final StringWriter sw = new StringWriter();
      try (final FudgeMsgJSONWriter fmw = new FudgeMsgJSONWriter(_fudgeContext, sw)) {
        fmw.writeMessage (messages[i], 0);
//        System.out.println(messages[i]);
//        System.out.println(sw.toString()); 
        final StringReader sr = new StringReader(sw.toString());
        final FudgeMsgJSONReader fmr = new FudgeMsgJSONReader(_fudgeContext, sr);
        FudgeMsg message = fmr.readMessage();
        AssertJUnit.assertNotNull(message);
//        System.out.println (message);
        FudgeUtils.assertAllFieldsMatch(messages[i], message, false);
      }
    }
  }
  
//  /**
//   * 
//   */
//  @Test
//  public void cycleJSONMessages_withTaxonomy() {
//    System.out.println("cycleJSONMessages:");
//    
//    final FudgeMsg[] messages = createMessages();
//    for (int i = 0; i < messages.length; i++) {
//      
//      final StringWriter sw = new StringWriter();
//      final FudgeMsgJSONWriter fmw = new FudgeMsgJSONWriter(_fudgeContext, sw);
//      fmw.writeMessage (messages[i], 1);
//      System.out.println(messages[i]);
//      System.out.println(sw.toString()); 
//      final StringReader sr = new StringReader(sw.toString());
//      final FudgeMsgJSONReader fmr = new FudgeMsgJSONReader(_fudgeContext, sr);
//      FudgeMsg message = fmr.readMessage();
//      AssertJUnit.assertNotNull(message);
//      System.out.println (message);
//      FudgeUtils.assertAllFieldsMatch(messages[i], message, false);
//    }
//  }
  
}