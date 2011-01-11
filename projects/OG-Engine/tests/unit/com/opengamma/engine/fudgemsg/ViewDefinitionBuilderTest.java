/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.junit.Test;

import com.opengamma.core.common.Currency;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;


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
    ViewDefinition test =  new ViewDefinition(TEST_VIEW_DEFINITION_NAME, TEST_PORTFOLIO_ID, TEST_USER, new ResultModelDefinition());
    test.setDefaultCurrency(Currency.getInstance("USD"));
    return test;
  }

}
