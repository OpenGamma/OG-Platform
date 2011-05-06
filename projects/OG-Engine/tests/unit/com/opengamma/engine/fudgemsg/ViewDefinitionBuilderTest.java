/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.testng.annotations.Test;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.money.Currency;

/**
 * Tests ViewDefinitionBuilder
 */

public class ViewDefinitionBuilderTest extends AbstractBuilderTestCase {
  
  private static final UserPrincipal TEST_USER = UserPrincipal.getLocalUser();
  
  private static final String TEST_VIEW_DEFINITION_NAME = "Test View";
  
  private static final UniqueIdentifier TEST_PORTFOLIO_ID = UniqueIdentifier.of("A", "B");
  
  private static final UniqueIdentifier FUNCTION_ID = UniqueIdentifier.of("AFunc", "B");
  
  private static final UniqueIdentifier TEST_VIEW_DEFINITION_ID = UniqueIdentifier.of("AView", "B");
  
  @Test
  public void test_viewDefinition_NoUniqueId() {
    ViewDefinition testViewDefinition = getTestViewDefinition();
    testViewDefinition.setUniqueId(null);
    assertEncodeDecodeCycle(ViewDefinition.class, getTestViewDefinition());
  }
  
  @Test
  public void test_viewDefinition_UniqueId() {
    ViewDefinition testViewDefinition = getTestViewDefinition();
    testViewDefinition.setUniqueId(TEST_VIEW_DEFINITION_ID);
    assertEncodeDecodeCycle(ViewDefinition.class, testViewDefinition);
  }

  private ViewDefinition getTestViewDefinition() {
    
    ValueProperties constraints = ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID.toString()).withAny(ValuePropertyNames.CURVE).get();
    ValueProperties allConstraints = ValueProperties.all();
    ValueProperties noConstraints = ValueProperties.none();
    ValueProperties nearlyAllConstraints = ValueProperties.all().withoutAny("SomePropName");
    
    final ViewDefinition viewDefinition = new ViewDefinition(TEST_VIEW_DEFINITION_NAME, TEST_PORTFOLIO_ID, TEST_USER, new ResultModelDefinition());
    final ViewCalculationConfiguration calcConfig1 = new ViewCalculationConfiguration (viewDefinition, "1");
    calcConfig1.addSpecificRequirement(new ValueRequirement ("Value1", UniqueIdentifier.of ("Test", "Foo")));
    calcConfig1.addSpecificRequirement(new ValueRequirement ("Value1", UniqueIdentifier.of ("Test", "Bar"), constraints));
    calcConfig1.setDefaultProperties (ValueProperties.with(ValuePropertyNames.CURRENCY, "GBP").get ());
    
    calcConfig1.addPortfolioRequirement("SomeSecType", "SomeOutput", constraints);
    
    calcConfig1.addPortfolioRequirement("SomeSecType", "SomeOtherOutput", allConstraints);
    calcConfig1.addPortfolioRequirement("SomeSecType", "SomeOtherOutput", allConstraints);
    calcConfig1.addPortfolioRequirement("SomeSecType", "YetAnotherOutput", noConstraints);
    calcConfig1.addPortfolioRequirement("SomeOtherSecType", "YetAnotherOutput", nearlyAllConstraints);
        
    final ViewCalculationConfiguration calcConfig2 = new ViewCalculationConfiguration (viewDefinition, "2");
    calcConfig2.addSpecificRequirement(new ValueRequirement ("Value2", UniqueIdentifier.of ("Test", "Foo")));
    calcConfig2.addSpecificRequirement(new ValueRequirement ("Value2", UniqueIdentifier.of ("Test", "Bar")));
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.addViewCalculationConfiguration(calcConfig1);
    viewDefinition.addViewCalculationConfiguration(calcConfig2);
    return viewDefinition;
  }

}
