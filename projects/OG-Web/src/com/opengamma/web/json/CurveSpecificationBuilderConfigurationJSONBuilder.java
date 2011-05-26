/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.time.calendar.Period;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.beust.jcommander.internal.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;


/**
 * Custom JSON builder to convert CurveSpecificationBuilderConfiguration to JSON object and back again
 */
public class CurveSpecificationBuilderConfigurationJSONBuilder extends AbstractJSONBuilder<CurveSpecificationBuilderConfiguration> {

  @Override
  public CurveSpecificationBuilderConfiguration fromJSON(String json) {
    
    ArgumentChecker.notNull(json, "JSON document");
    CurveSpecificationBuilderConfiguration result = null;
    try {
      JSONObject configJSON = new JSONObject(json);
      Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      processCurveInstrumentProvider(configJSON.getJSONArray("cashInstrumentProviders"), cashInstrumentProviders);
      
      Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      processCurveInstrumentProvider(configJSON.getJSONArray("fraInstrumentProviders"), fraInstrumentProviders);

      Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      processCurveInstrumentProvider(configJSON.getJSONArray("futureInstrumentProviders"), futureInstrumentProviders);
          
      Map<Tenor, CurveInstrumentProvider> rateInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      processCurveInstrumentProvider(configJSON.getJSONArray("rateInstrumentProviders"), rateInstrumentProviders);

      Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      processCurveInstrumentProvider(configJSON.getJSONArray("swapInstrumentProviders"), swapInstrumentProviders);
      
      Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      processCurveInstrumentProvider(configJSON.getJSONArray("basisSwapInstrumentProviders"), basisSwapInstrumentProviders);
      
      Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      processCurveInstrumentProvider(configJSON.getJSONArray("tenorSwapInstrumentProviders"), tenorSwapInstrumentProviders);
      
      result = new CurveSpecificationBuilderConfiguration(
          cashInstrumentProviders, fraInstrumentProviders, rateInstrumentProviders, futureInstrumentProviders,
          swapInstrumentProviders, basisSwapInstrumentProviders, tenorSwapInstrumentProviders);
      
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("Unable to create CurveSpecificationBuilderConfiguration", ex);
    }
    return result;
  }

  private void processCurveInstrumentProvider(final JSONArray messages, final Map<Tenor, CurveInstrumentProvider> curveInstrumentProvider) throws JSONException {
    for (int i = 0; i < messages.length(); i++) {
      JSONObject instrument = messages.getJSONObject(i);
      String name = instrument.names().getString(0);
      curveInstrumentProvider.put(new Tenor(Period.parse(name)), convertJsonToObject(CurveInstrumentProvider.class, instrument.getJSONObject(name)));
    }
  }

  @Override
  public String toJSON(CurveSpecificationBuilderConfiguration object) {
    
    ArgumentChecker.notNull(object, "curveSpecificationBuilderConfiguration");
    JSONObject message = new JSONObject();
    try {
      message.put(String.valueOf(0), CurveSpecificationBuilderConfiguration.class.getName());
      List<JSONObject> cashInstrumentProviders = Lists.newArrayList();
      for (Entry<Tenor, CurveInstrumentProvider> entry : object.getCashInstrumentProviders().entrySet()) {
        if (entry.getKey().getPeriod().toString() == null) {
          throw new OpenGammaRuntimeException("tenor is null");
        }
        JSONObject cashInstrumentProvidersMessage = new JSONObject();
        cashInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
        cashInstrumentProviders.add(cashInstrumentProvidersMessage);
      }
      message.put("cashInstrumentProviders", cashInstrumentProviders);
      
      List<JSONObject> fraInstrumentProviders = Lists.newArrayList();
      for (Entry<Tenor, CurveInstrumentProvider> entry : object.getFraInstrumentProviders().entrySet()) {
        if (entry.getKey().getPeriod().toString() == null) {
          throw new OpenGammaRuntimeException(" tenor is null");
        }
        JSONObject fraInstrumentProvidersMessage = new JSONObject();
        fraInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
        fraInstrumentProviders.add(fraInstrumentProvidersMessage);
      }
      message.put("fraInstrumentProviders", fraInstrumentProviders);
      
      List<JSONObject> futureInstrumentProviders = Lists.newArrayList();
      for (Entry<Tenor, CurveInstrumentProvider> entry : object.getFutureInstrumentProviders().entrySet()) {
        if (entry.getKey().getPeriod().toString() == null) {
          throw new OpenGammaRuntimeException(" tenor is null");
        }
        JSONObject futureInstrumentProvidersMessage = new JSONObject();
        futureInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
        futureInstrumentProviders.add(futureInstrumentProvidersMessage);
      }
      message.put("futureInstrumentProviders", futureInstrumentProviders);
      
      List<JSONObject> rateInstrumentProviders = Lists.newArrayList();
      for (Entry<Tenor, CurveInstrumentProvider> entry : object.getRateInstrumentProviders().entrySet()) {
        if (entry.getKey().getPeriod().toString() == null) {
          throw new OpenGammaRuntimeException(" tenor is null");
        }
        JSONObject rateInstrumentProvidersMessage = new JSONObject();
        rateInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
        rateInstrumentProviders.add(rateInstrumentProvidersMessage);
      }
      message.put("rateInstrumentProviders", rateInstrumentProviders);

      List<JSONObject> swapInstrumentProviders = Lists.newArrayList();
      for (Entry<Tenor, CurveInstrumentProvider> entry : object.getSwapInstrumentProviders().entrySet()) {
        if (entry.getKey().getPeriod().toString() == null) {
          throw new OpenGammaRuntimeException(" tenor is null");
        }
        JSONObject swapInstrumentProvidersMessage = new JSONObject();
        swapInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
        swapInstrumentProviders.add(swapInstrumentProvidersMessage);
      }
      message.put("swapInstrumentProviders", swapInstrumentProviders);

      List<JSONObject> basisSwapInstrumentProviders = Lists.newArrayList();
      for (Entry<Tenor, CurveInstrumentProvider> entry : object.getBasisSwapInstrumentProviders().entrySet()) {
        if (entry.getKey().getPeriod().toString() == null) {
          throw new OpenGammaRuntimeException(" tenor is null");
        }
        JSONObject basisSwapInstrumentProvidersMessage = new JSONObject();
        basisSwapInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
        basisSwapInstrumentProviders.add(basisSwapInstrumentProvidersMessage);
      }
      message.put("basisSwapInstrumentProviders", basisSwapInstrumentProviders);

      List<JSONObject> tenorSwapInstrumentProviders = Lists.newArrayList();
      for (Entry<Tenor, CurveInstrumentProvider> entry : object.getTenorSwapInstrumentProviders().entrySet()) {
        if (entry.getKey().getPeriod().toString() == null) {
          throw new OpenGammaRuntimeException(" tenor is null");
        }
        JSONObject tenorSwapInstrumentProvidersMessage = new JSONObject();
        tenorSwapInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
        tenorSwapInstrumentProviders.add(tenorSwapInstrumentProvidersMessage);
      }
      message.put("tenorSwapInstrumentProviders", tenorSwapInstrumentProviders);
      
            
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("unable to convert CurveSpecificationBuilderConfiguration to JSON", ex);
    }
        
    return message.toString();
  }

}
