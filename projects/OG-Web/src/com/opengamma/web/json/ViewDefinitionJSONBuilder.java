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
 
//  private static final String CALCULATION_CONFIGURATION_FIELD = "calculationConfiguration";
//  private static final String CURRENCY_FIELD = "currency";

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
    return toJSON(viewDefinition, ViewDefinition.class);
  }
  
  private static String createTemplate() {
    ViewDefinitionJSONBuilder builder = ViewDefinitionJSONBuilder.INSTANCE; 
    return builder.toJSON(getDummyView());
//    String result = null;
//    try {
//      JSONObject jsonObject = new JSONObject(builder.toJSON(getDummyView()));
//      jsonObject.put(CURRENCY_FIELD, "");
//      jsonObject.put(CALCULATION_CONFIGURATION_FIELD, new JSONArray());
//      result = jsonObject.toString();
//    } catch (JSONException ex) {
//      throw new OpenGammaRuntimeException("invalid json produced from dummy view definition", ex);
//    }
//    return result;
  }

  private static ViewDefinition getDummyView() {
    ViewDefinition dummy = new ViewDefinition("", new UserPrincipal("", ""));
    dummy.setDefaultCurrency(Currency.GBP);
    dummy.setMaxDeltaCalculationPeriod(0L);
    dummy.setMaxFullCalculationPeriod(0L);
    dummy.setMinDeltaCalculationPeriod(0L);
    dummy.setMinFullCalculationPeriod(0L);
    dummy.addPortfolioRequirementName("", "", "");
    return dummy;
  }

  @Override
  public String getTemplate() {
    return TEMPLATE;
  }
  
}
