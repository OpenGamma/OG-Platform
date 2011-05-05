package com.opengamma.engine.json;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.engine.fudgemsg.ViewDefinitionBuilderTest;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueIdentifier;


public class ViewDefintionJSONBuilderTest {
  
  private static final UniqueIdentifier TEST_VIEW_DEFINITION_ID = UniqueIdentifier.of("AView", "B");
  
  private final ViewDefinitionJSONBuilder _jsonBuilder = new ViewDefinitionJSONBuilder();
  
  @Test
  public void test_encode_decode_cycle() {
    ViewDefinition testViewDefinition = ViewDefinitionBuilderTest.getTestViewDefinition();
    testViewDefinition.setUniqueId(TEST_VIEW_DEFINITION_ID);
    
    String json = _jsonBuilder.toJSON(testViewDefinition);
    assertNotNull(json);    
    ViewDefinition fromJSON = _jsonBuilder.fromJSON(json);
    assertNotNull(fromJSON);
    
    assertEquals(testViewDefinition, fromJSON);    
  }

}
