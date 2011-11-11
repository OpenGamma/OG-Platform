package com.opengamma.engine.test;

import com.opengamma.engine.function.resolver.SimpleResolutionRuleTransform;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.money.Currency;

/**
 * Provides shared Test View Definition used across projects
 */
public final class TestViewDefinitionProvider {
  

  private static final UserPrincipal TEST_USER = UserPrincipal.getLocalUser();

  private static final String TEST_VIEW_DEFINITION_NAME = "Test View";

  private static final UniqueId TEST_PORTFOLIO_ID = UniqueId.of("A", "B");

  private static final UniqueId FUNCTION_ID = UniqueId.of("AFunc", "B");

  private static final UniqueId TEST_VIEW_DEFINITION_ID = UniqueId.of("AView", "B");

  /**
   * Restricted constructor
   */
  private TestViewDefinitionProvider() {
  }

  public static ViewDefinition getTestViewDefinition() {

    ValueProperties constraints = ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID.toString()).withAny(ValuePropertyNames.CURVE).get();
    ValueProperties allConstraints = ValueProperties.all();
    ValueProperties noConstraints = ValueProperties.none();
    ValueProperties nearlyAllConstraints = ValueProperties.all().withoutAny("SomePropName");

    final ViewDefinition viewDefinition = new ViewDefinition(TEST_VIEW_DEFINITION_NAME, TEST_PORTFOLIO_ID, TEST_USER, new ResultModelDefinition());
    final ViewCalculationConfiguration calcConfig1 = new ViewCalculationConfiguration(viewDefinition, "1");
    calcConfig1.addSpecificRequirement(new ValueRequirement("Value1", UniqueId.of("Test", "Foo")));
    calcConfig1.addSpecificRequirement(new ValueRequirement("Value1", UniqueId.of("Test", "Bar"), constraints));
    calcConfig1.setDefaultProperties(ValueProperties.with(ValuePropertyNames.CURRENCY, "GBP").get());
    calcConfig1.addPortfolioRequirement("SomeSecType", "SomeOutput", constraints);
    calcConfig1.addPortfolioRequirement("SomeSecType", "SomeOtherOutput", allConstraints);
    calcConfig1.addPortfolioRequirement("SomeSecType", "SomeOtherOutput", allConstraints);
    calcConfig1.addPortfolioRequirement("SomeSecType", "YetAnotherOutput", noConstraints);
    calcConfig1.addPortfolioRequirement("SomeOtherSecType", "YetAnotherOutput", nearlyAllConstraints);

    final ViewCalculationConfiguration calcConfig2 = new ViewCalculationConfiguration(viewDefinition, "2");
    calcConfig2.addSpecificRequirement(new ValueRequirement("Value2", UniqueId.of("Test", "Foo")));
    calcConfig2.addSpecificRequirement(new ValueRequirement("Value2", UniqueId.of("Test", "Bar")));
    final SimpleResolutionRuleTransform transform = new SimpleResolutionRuleTransform();
    transform.suppressRule("Foo");
    calcConfig2.setResolutionRuleTransform(transform);

    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.addViewCalculationConfiguration(calcConfig1);
    viewDefinition.addViewCalculationConfiguration(calcConfig2);

    viewDefinition.setUniqueId(TEST_VIEW_DEFINITION_ID);
    return viewDefinition;
  }

}
