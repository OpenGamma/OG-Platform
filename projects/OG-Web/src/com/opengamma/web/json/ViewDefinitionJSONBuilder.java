/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Custom JSON builder to convert ViewDefinition to JSON object and back again
 */
public final class ViewDefinitionJSONBuilder extends AbstractJSONBuilder<ViewDefinition> {
 
  /**
   * Singleton
   */
  public static final ViewDefinitionJSONBuilder INSTANCE = new ViewDefinitionJSONBuilder();
  
  /**
   * JSON template
   */
  private static final String TEMPLATE = createTemplate();
  
  /**
   * Restricted constructor
   */
  private ViewDefinitionJSONBuilder() {
  }

  @Override
  public ViewDefinition fromJSON(final String json) {
    ArgumentChecker.notNull(json, "JSON document");
    return fromJSON(ViewDefinition.class, json);
  }
 
  @Override
  public String toJSON(final ViewDefinition viewDefinition) {
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    return fudgeToJson(viewDefinition);
  }
  
  private static String createTemplate() {
    ViewDefinitionJSONBuilder builder = ViewDefinitionJSONBuilder.INSTANCE; 
    return builder.toJSON(getBlankView());
  }

  private static ViewDefinition getBlankView() {
    ViewDefinition blank = new ViewDefinition("Untitled", new UserPrincipal("", ""));
    blank.setDefaultCurrency(Currency.GBP);
    blank.setMaxDeltaCalculationPeriod(0L);
    blank.setMaxFullCalculationPeriod(0L);
    blank.setMinDeltaCalculationPeriod(0L);
    blank.setMinFullCalculationPeriod(0L);
    blank.addPortfolioRequirementName("", "", "");
    return blank;
  }

  @Override
  public String getTemplate() {
    return TEMPLATE;
  }
  
}
