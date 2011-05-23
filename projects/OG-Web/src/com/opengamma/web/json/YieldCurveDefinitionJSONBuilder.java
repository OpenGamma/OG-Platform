/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.beust.jcommander.internal.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Custom JSON builder to convert YieldCurveDefinition to JSON object and vice versa
 */
public final class YieldCurveDefinitionJSONBuilder extends AbstractJSONBuilder<YieldCurveDefinition> {
  
  private static final String STRIP_FIELD = "strip";
  private static final String INTERPOLATOR_NAME_FIELD = "interpolatorName";
  private static final String REGION_FIELD = "region";
  private static final String CURRENCY_FIELD = "currency";

  /**
   * Creates an instance 
   */
  public YieldCurveDefinitionJSONBuilder() {
  }

  @Override
  public YieldCurveDefinition fromJSON(final String json) {
    ArgumentChecker.notNull(json, "JSON document");
    YieldCurveDefinition curveDefinition = null;
    try {
      JSONObject message = new JSONObject(json);
      Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      Identifier region = null;
      if (message.opt(REGION_FIELD) != null) {
        region = convertJsonToObject(Identifier.class, message.getJSONObject(REGION_FIELD));
      }
      String name = message.getString(NAME_FIELD);
      String interpolatorName = message.getString(INTERPOLATOR_NAME_FIELD);

      SortedSet<FixedIncomeStrip> strips = new TreeSet<FixedIncomeStrip>();
      if (message.opt(STRIP_FIELD) != null) {
        JSONArray jsonStrips = message.getJSONArray(STRIP_FIELD);
        for (int i = 0; i < jsonStrips.length(); i++) {
          strips.add(convertJsonToObject(FixedIncomeStrip.class, jsonStrips.getJSONObject(i)));
        }
      }

      curveDefinition = new YieldCurveDefinition(currency, region, name, interpolatorName, strips);

      if (message.opt(UNIQUE_ID_FIELD) != null) {
        UniqueIdentifier uid = convertJsonToObject(UniqueIdentifier.class, message.getJSONObject(UNIQUE_ID_FIELD));
        curveDefinition.setUniqueId(uid);
      }
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("Unable to create YieldCurveDefinition", ex);
    }
    return curveDefinition;
  }

  @Override
  public String toJSON(final YieldCurveDefinition object) {
    ArgumentChecker.notNull(object, "yield curve definition");
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put(String.valueOf(0), YieldCurveDefinition.class.getName());
      jsonObject.put(NAME_FIELD, object.getName());
      jsonObject.put(INTERPOLATOR_NAME_FIELD, object.getInterpolatorName());
      jsonObject.put(CURRENCY_FIELD, object.getCurrency().getCode());
      if (object.getRegion() != null) {
        jsonObject.put(REGION_FIELD, toJSONObject(object.getRegion()));
      }
      List<JSONObject> strips = Lists.newArrayList();
      for (FixedIncomeStrip strip : object.getStrips()) {
        strips.add(toJSONObject(strip));
      }
      if (!strips.isEmpty()) {
        jsonObject.put(STRIP_FIELD, strips);
      }
      jsonObject.put(UNIQUE_ID_FIELD, toJSONObject(object.getUniqueId()));
            
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("unable to convert view definition to JSON", ex);
    }
    return jsonObject.toString();
  }

}
