/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Custom JSON builder to convert YieldCurveDefinition to JSON object and back again
 */
public final class YieldCurveDefinitionJSONBuilder extends AbstractJSONBuilder<YieldCurveDefinition> {
  
//  private static final String REGION_FIELD = "region";
//  private static final String CURRENCY_FIELD = "currency";
  /**
   * Singleton
   */
  public static final YieldCurveDefinitionJSONBuilder INSTANCE = new YieldCurveDefinitionJSONBuilder();
  /**
   * JSON template
   */
  private static final String TEMPLATE = createTemplate();

  /**
   * Restricted constructor 
   */
  private YieldCurveDefinitionJSONBuilder() {
  }
  
  @Override
  public YieldCurveDefinition fromJSON(final String json) {
    ArgumentChecker.notNull(json, "JSON document");
    return fromJSON(YieldCurveDefinition.class, json);
  }

  @Override
  public String toJSON(final YieldCurveDefinition object) {
    ArgumentChecker.notNull(object, "yield curve definition");
    return toJSON(object, YieldCurveDefinition.class);
  }
  
  private static String createTemplate() {
    return YieldCurveDefinitionJSONBuilder.INSTANCE.toJSON(getDummyYieldCurveDefinition());
//    String result = null;
//    try {
//      JSONObject jsonObject = new JSONObject(builder.toJSON(getDummyYieldCurveDefinition()));
//      jsonObject.put(CURRENCY_FIELD, "");
//      jsonObject.put(REGION_FIELD, getBlankIdentifier());
//      result = jsonObject.toString();
//    } catch (JSONException ex) {
//      throw new OpenGammaRuntimeException("invalid json produced from dummy yield curve definition", ex);
//    }
//    return result;
  }

//  private static JSONObject getBlankIdentifier() {
//    JSONObject blankIdentifier = null;
//    try {
//      blankIdentifier = new JSONObject();
//      blankIdentifier.put(ExternalIdFudgeBuilder.SCHEME_FIELD_NAME, "");
//      blankIdentifier.put(ExternalIdFudgeBuilder.VALUE_FIELD_NAME, "");
//    } catch (JSONException ex) {
//      throw new OpenGammaRuntimeException("invalid json produced from blank region identifier", ex);
//    }
//    return blankIdentifier;
//  }

  private static YieldCurveDefinition getDummyYieldCurveDefinition() {
    YieldCurveDefinition dummy = new YieldCurveDefinition(Currency.GBP, RegionUtils.currencyRegionId(Currency.USD), "", "");
    dummy.addStrip(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.DAY, ""));
    return dummy;
  }

  @Override
  public String getTemplate() {
    return TEMPLATE;
  }

}
