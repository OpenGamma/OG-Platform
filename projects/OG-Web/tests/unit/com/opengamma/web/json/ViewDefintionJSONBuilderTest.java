package com.opengamma.web.json;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.engine.test.TestViewDefinitionProvider;
import com.opengamma.engine.view.ViewDefinition;


public class ViewDefintionJSONBuilderTest {
  
  private final ViewDefinitionJSONBuilder _jsonBuilder = new ViewDefinitionJSONBuilder();
  
  @Test
  public void test_encode_decode_cycle() {
    ViewDefinition testViewDefinition = TestViewDefinitionProvider.getTestViewDefinition();
    
    String json = _jsonBuilder.toJSON(testViewDefinition);
    assertNotNull(json);    
    ViewDefinition fromJSON = _jsonBuilder.fromJSON(json);
    assertNotNull(fromJSON);
    
    assertEquals(testViewDefinition, fromJSON);    
  }

}
