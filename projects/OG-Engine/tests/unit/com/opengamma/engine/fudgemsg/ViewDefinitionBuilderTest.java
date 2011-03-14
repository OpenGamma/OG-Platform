/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.junit.Test;

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
  
  @Test
  public void testEncoding() {
    assertEncodeDecodeCycle(ViewDefinition.class, getTestViewDefinition());
  }

  private ViewDefinition getTestViewDefinition() {
    final ViewDefinition viewDefinition = new ViewDefinition(TEST_VIEW_DEFINITION_NAME, TEST_PORTFOLIO_ID, TEST_USER, new ResultModelDefinition());
    final ViewCalculationConfiguration calcConfig1 = new ViewCalculationConfiguration (viewDefinition, "1");
    calcConfig1.addSpecificRequirement(new ValueRequirement ("Value1", UniqueIdentifier.of ("Test", "Foo")));
    calcConfig1.addSpecificRequirement(new ValueRequirement ("Value1", UniqueIdentifier.of ("Test", "Bar")));
    calcConfig1.setDefaultProperties (ValueProperties.with(ValuePropertyNames.CURRENCY, "GBP").get ());
    final ViewCalculationConfiguration calcConfig2 = new ViewCalculationConfiguration (viewDefinition, "2");
    calcConfig2.addSpecificRequirement(new ValueRequirement ("Value2", UniqueIdentifier.of ("Test", "Foo")));
    calcConfig2.addSpecificRequirement(new ValueRequirement ("Value2", UniqueIdentifier.of ("Test", "Bar")));
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.addViewCalculationConfiguration(calcConfig1);
    viewDefinition.addViewCalculationConfiguration(calcConfig2);
    return viewDefinition;
  }

}
