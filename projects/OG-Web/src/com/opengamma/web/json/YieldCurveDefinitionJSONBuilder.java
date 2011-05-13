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
      JSONObject message = new JSONObject(json).getJSONObject(FUDGE_ENVELOPE_FIELD);
      Currency currency = Currency.of(message.getString("currency"));
      Identifier region = null;
      if (message.opt("region") != null) {
        region = convertJsonToObject(Identifier.class, message.getJSONObject("region"));
      }
      String name = message.getString("name");
      String interpolatorName = message.getString("interpolatorName");

      SortedSet<FixedIncomeStrip> strips = new TreeSet<FixedIncomeStrip>();
      if (message.opt("strip") != null) {
        JSONArray jsonStrips = message.getJSONArray("strip");
        for (int i = 0; i < jsonStrips.length(); i++) {
          strips.add(convertJsonToObject(FixedIncomeStrip.class, jsonStrips.getJSONObject(i)));
        }
      }

      curveDefinition = new YieldCurveDefinition(currency, region, name, interpolatorName, strips);

      if (message.opt("uniqueId") != null) {
        UniqueIdentifier uid = convertJsonToObject(UniqueIdentifier.class, message.getJSONObject("uniqueId"));
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
    JSONObject result = new JSONObject();
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("name", object.getName());
      jsonObject.put("interpolatorName", object.getInterpolatorName());
      jsonObject.put("currency", object.getCurrency().getCode());
      if (object.getRegion() != null) {
        jsonObject.put("region", toJSONObject(object.getRegion()));
      }
      List<JSONObject> strips = Lists.newArrayList();
      for (FixedIncomeStrip strip : object.getStrips()) {
        strips.add(toJSONObject(strip));
      }
      if (!strips.isEmpty()) {
        jsonObject.put("strip", strips);
      }
      jsonObject.put("uniqueId", toJSONObject(object.getUniqueId()));
      
      result.put(FUDGE_ENVELOPE_FIELD, jsonObject);
      
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("unable to convert view definition to JSON", ex);
    }
    return result.toString();
  }

}
