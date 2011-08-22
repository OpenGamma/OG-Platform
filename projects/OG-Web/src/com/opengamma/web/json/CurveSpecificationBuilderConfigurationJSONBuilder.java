/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.time.calendar.Period;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;


/**
 * Custom JSON builder to convert CurveSpecificationBuilderConfiguration to JSON object and back again
 */
public final class CurveSpecificationBuilderConfigurationJSONBuilder extends AbstractJSONBuilder<CurveSpecificationBuilderConfiguration> {

  private static final Logger s_logger = LoggerFactory.getLogger(CurveSpecificationBuilderConfigurationJSONBuilder.class);
  /**
   * Singleton
   */
  public static final CurveSpecificationBuilderConfigurationJSONBuilder INSTANCE = new CurveSpecificationBuilderConfigurationJSONBuilder();
  
  /**
   * JSON template
   */
  private static final String TEMPLATE = createTemplate();
  
  /**
   * Restricted constructor
   */
  private CurveSpecificationBuilderConfigurationJSONBuilder() {
  }

  @Override
  public CurveSpecificationBuilderConfiguration fromJSON(String json) {
    
    ArgumentChecker.notNull(json, "JSON document");
    CurveSpecificationBuilderConfiguration result = null;
    try {
      JSONObject configJSON = new JSONObject(json);
      
      Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = processCurveInstrumentProvider(configJSON.optJSONArray("cashInstrumentProviders"));
      Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders = processCurveInstrumentProvider(configJSON.optJSONArray("fraInstrumentProviders"));
      Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = processCurveInstrumentProvider(configJSON.optJSONArray("futureInstrumentProviders"));
      Map<Tenor, CurveInstrumentProvider> rateInstrumentProviders = processCurveInstrumentProvider(configJSON.optJSONArray("rateInstrumentProviders"));
      Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders = processCurveInstrumentProvider(configJSON.optJSONArray("swapInstrumentProviders"));
      Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = processCurveInstrumentProvider(configJSON.optJSONArray("basisSwapInstrumentProviders"));
      Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = processCurveInstrumentProvider(configJSON.optJSONArray("tenorSwapInstrumentProviders"));
      Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders = processCurveInstrumentProvider(configJSON.optJSONArray("oisSwapInstrumentProviders"));

      result = new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, 
                                                          fraInstrumentProviders, 
                                                          rateInstrumentProviders, 
                                                          futureInstrumentProviders, 
                                                          swapInstrumentProviders, 
                                                          basisSwapInstrumentProviders, 
                                                          tenorSwapInstrumentProviders,
                                                          oisSwapInstrumentProviders);
      
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("Unable to create CurveSpecificationBuilderConfiguration", ex);
    }
    return result;
  }

  private Map<Tenor, CurveInstrumentProvider> processCurveInstrumentProvider(final JSONArray messages) throws JSONException {
    if (messages == null) {
      return null;
    }
    final Map<Tenor, CurveInstrumentProvider> curveInstrumentProvider = new HashMap<Tenor, CurveInstrumentProvider>();
    for (int i = 0; i < messages.length(); i++) {
      JSONObject instrument = messages.getJSONObject(i);
      String name = instrument.names().getString(0);
      curveInstrumentProvider.put(new Tenor(Period.parse(name)), convertJsonToObject(CurveInstrumentProvider.class, instrument.getJSONObject(name)));
    }
    return curveInstrumentProvider;
  }

  @Override
  public String toJSON(CurveSpecificationBuilderConfiguration object) {
    
    ArgumentChecker.notNull(object, "curveSpecificationBuilderConfiguration");
    JSONObject message = new JSONObject();
    try {
      SortedSet<Tenor> allTenors = new TreeSet<Tenor>();
      message.put(String.valueOf(0), CurveSpecificationBuilderConfiguration.class.getName());

      if (object.getCashInstrumentProviders() != null) {
        List<JSONObject> cashInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getCashInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException("tenor is null");
          }
          JSONObject cashInstrumentProvidersMessage = new JSONObject();
          cashInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          cashInstrumentProviders.add(cashInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getCashInstrumentProviders().keySet());
        message.put("cashInstrumentProviders", cashInstrumentProviders);
      } else {
        s_logger.debug("No cash instrument providers");
      }
      
      if (object.getFraInstrumentProviders() != null) {
        List<JSONObject> fraInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getFraInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject fraInstrumentProvidersMessage = new JSONObject();
          fraInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          fraInstrumentProviders.add(fraInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getFraInstrumentProviders().keySet());
        message.put("fraInstrumentProviders", fraInstrumentProviders);
      } else {
        s_logger.debug("No FRA instrument providers");
      }
      
      if (object.getFutureInstrumentProviders() != null) {
        List<JSONObject> futureInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getFutureInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject futureInstrumentProvidersMessage = new JSONObject();
          futureInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          futureInstrumentProviders.add(futureInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getFutureInstrumentProviders().keySet());
        message.put("futureInstrumentProviders", futureInstrumentProviders);
      } else {
        s_logger.debug("No future instrument providers");
      }
      
      if (object.getRateInstrumentProviders() != null) {
        List<JSONObject> rateInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getRateInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject rateInstrumentProvidersMessage = new JSONObject();
          rateInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          rateInstrumentProviders.add(rateInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getRateInstrumentProviders().keySet());
        message.put("rateInstrumentProviders", rateInstrumentProviders);
      } else {
        s_logger.debug("No rate instrument providers");
      }

      if (object.getSwapInstrumentProviders() != null) {
        List<JSONObject> swapInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getSwapInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject swapInstrumentProvidersMessage = new JSONObject();
          swapInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          swapInstrumentProviders.add(swapInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getSwapInstrumentProviders().keySet());
        message.put("swapInstrumentProviders", swapInstrumentProviders);
      } else {
        s_logger.debug("No swap instrument providers");
      }

      if (object.getBasisSwapInstrumentProviders() != null) {
        List<JSONObject> basisSwapInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getBasisSwapInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject basisSwapInstrumentProvidersMessage = new JSONObject();
          basisSwapInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          basisSwapInstrumentProviders.add(basisSwapInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getBasisSwapInstrumentProviders().keySet());
        message.put("basisSwapInstrumentProviders", basisSwapInstrumentProviders);
      } else {
        s_logger.debug("No basis swap instrument providers");
      }

      if (object.getTenorSwapInstrumentProviders() != null) {
        List<JSONObject> tenorSwapInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getTenorSwapInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject tenorSwapInstrumentProvidersMessage = new JSONObject();
          tenorSwapInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          tenorSwapInstrumentProviders.add(tenorSwapInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getTenorSwapInstrumentProviders().keySet());
        message.put("tenorSwapInstrumentProviders", tenorSwapInstrumentProviders);
      } else {
        s_logger.debug("No tenor swap instrument providers");
      }
      
      if (object.getOISSwapInstrumentProviders() != null) {
        List<JSONObject> oisSwapInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getOISSwapInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject oisSwapInstrumentProvidersMessage = new JSONObject();
          oisSwapInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          oisSwapInstrumentProviders.add(oisSwapInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getOISSwapInstrumentProviders().keySet());
        message.put("oisSwapInstrumentProviders", oisSwapInstrumentProviders);
      } else {
        s_logger.debug("No OIS swap instrument providers");
      }
      
      String[] periods = new String[allTenors.size()];
      Iterator<Tenor> iterator = allTenors.iterator();
      for (int i = 0; i < allTenors.size(); i++) {
        periods[i] = iterator.next().getPeriod().toString();
      }
      message.put("tenors", new JSONArray(periods));
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("unable to convert CurveSpecificationBuilderConfiguration to JSON", ex);
    }
        
    return message.toString();
  }

  private static String createTemplate() {
    return null;
  }

  @Override
  public String getTemplate() {
    return TEMPLATE;
  }

}
