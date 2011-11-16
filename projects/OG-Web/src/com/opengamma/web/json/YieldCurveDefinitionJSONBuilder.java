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
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Custom JSON builder to convert YieldCurveDefinition to JSON object and back again
 */
public final class YieldCurveDefinitionJSONBuilder extends AbstractJSONBuilder<YieldCurveDefinition> {
  
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
    return fudgeToJson(object);
  }
  
  private static String createTemplate() {
    return YieldCurveDefinitionJSONBuilder.INSTANCE.toJSON(getDummyYieldCurveDefinition());
  }

  private static YieldCurveDefinition getDummyYieldCurveDefinition() {
    YieldCurveDefinition dummy = new YieldCurveDefinition(Currency.GBP, RegionUtils.countryRegionId(Country.US), "", "");
    dummy.addStrip(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.DAY, ""));
    return dummy;
  }

  @Override
  public String getTemplate() {
    return TEMPLATE;
  }

}
