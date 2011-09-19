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
      
      Map<Tenor, CurveInstrumentProvider> fra3MInstrumentProviders = null;
      Map<Tenor, CurveInstrumentProvider> fra6MInstrumentProviders = null;
      JSONArray oldFRAArray = configJSON.optJSONArray("fraInstrumentProviders");
      JSONArray fra3MArray = configJSON.optJSONArray("fra3MInstrumentProviders");
      JSONArray fra6MArray = configJSON.optJSONArray("fra6MInstrumentProviders");
      if (oldFRAArray != null) {
        if (fra3MArray != null || fra6MArray != null) {
          throw new OpenGammaRuntimeException("Have JSON array with the old FRA field but at least one of the new FRA fields (3m or 6m)");
        }
        // Treat all old FRAs as 3M (e.g. 1M x 4M)
        s_logger.warn("Curve specification uses a FRA strip that does not contain information about its tenor: assuming that it is 3m.");
        fra3MInstrumentProviders = processCurveInstrumentProvider(oldFRAArray);
      } else {
        fra3MInstrumentProviders = processCurveInstrumentProvider(fra3MArray);
        fra6MInstrumentProviders = processCurveInstrumentProvider(fra6MArray);
      }
      
      Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = processCurveInstrumentProvider(configJSON.optJSONArray("futureInstrumentProviders"));
      
      Map<Tenor, CurveInstrumentProvider> liborInstrumentProviders = null;
      Map<Tenor, CurveInstrumentProvider> euriborInstrumentProviders = null;
      Map<Tenor, CurveInstrumentProvider> cdorInstrumentProviders = null;
      Map<Tenor, CurveInstrumentProvider> ciborInstrumentProviders = null;
      Map<Tenor, CurveInstrumentProvider> stiborInstrumentProviders = null;
      JSONArray oldRateArray = configJSON.optJSONArray("rateInstrumentProviders");
      JSONArray liborArray = configJSON.optJSONArray("liborInstrumentProviders");
      JSONArray euriborArray = configJSON.optJSONArray("euriborInstrumentProviders");
      JSONArray cdorArray = configJSON.optJSONArray("cdorInstrumentProviders");
      JSONArray ciborArray = configJSON.optJSONArray("ciborInstrumentProviders");
      JSONArray stiborArray = configJSON.optJSONArray("stiborInstrumentProviders");
      if (oldRateArray != null) {
        if (liborArray != null || euriborArray != null) {
          throw new OpenGammaRuntimeException("Have JSON array with the old rate field but at least one of the new *ibor fields (libor, euribor or CDOR");
        }
        // Treat all old rates as libor
        s_logger.warn("Curve specification uses a RATE strip: assuming that it is a libor rate.");
        liborInstrumentProviders = processCurveInstrumentProvider(oldRateArray);
      } else {
        liborInstrumentProviders = processCurveInstrumentProvider(liborArray);
        euriborInstrumentProviders = processCurveInstrumentProvider(euriborArray);
        cdorInstrumentProviders = processCurveInstrumentProvider(cdorArray);
        ciborInstrumentProviders = processCurveInstrumentProvider(ciborArray);
        stiborInstrumentProviders = processCurveInstrumentProvider(stiborArray);
      }
      
      Map<Tenor, CurveInstrumentProvider> swap3MInstrumentProviders = null;
      Map<Tenor, CurveInstrumentProvider> swap6MInstrumentProviders = null;
      JSONArray oldSwapArray = configJSON.optJSONArray("swapInstrumentProviders");
      JSONArray swap3MArray = configJSON.optJSONArray("swap3MInstrumentProviders");
      JSONArray swap6MArray = configJSON.optJSONArray("swap6MInstrumentProviders");
      if (oldSwapArray != null) {
        if (swap3MArray != null || swap6MArray != null) {
          throw new OpenGammaRuntimeException("Have JSON array with the old swap field but at least one of the new swap fields (3m or 6m)");
        }
        // Treat all old swaps as if the floating leg was reset quarterly
        s_logger.warn("Curve specification uses a SWAP strip that does not contain information about its floating leg reset tenor: assuming that it is 3m.");
        swap3MInstrumentProviders = processCurveInstrumentProvider(oldSwapArray);
      } else {
        swap3MInstrumentProviders = processCurveInstrumentProvider(swap3MArray);
        swap6MInstrumentProviders = processCurveInstrumentProvider(swap6MArray);
      }
      
      Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = processCurveInstrumentProvider(configJSON.optJSONArray("basisSwapInstrumentProviders"));
      
      Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = processCurveInstrumentProvider(configJSON.optJSONArray("tenorSwapInstrumentProviders"));
      
      Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders = processCurveInstrumentProvider(configJSON.optJSONArray("oisSwapInstrumentProviders"));

      result = new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, 
                                                          fra3MInstrumentProviders, 
                                                          fra6MInstrumentProviders, 
                                                          liborInstrumentProviders, 
                                                          euriborInstrumentProviders,
                                                          cdorInstrumentProviders,
                                                          ciborInstrumentProviders,
                                                          stiborInstrumentProviders,
                                                          futureInstrumentProviders, 
                                                          swap6MInstrumentProviders, 
                                                          swap3MInstrumentProviders, 
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
      
      if (object.getFra3MInstrumentProviders() != null) {
        List<JSONObject> fraInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getFra3MInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject fraInstrumentProvidersMessage = new JSONObject();
          fraInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          fraInstrumentProviders.add(fraInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getFra3MInstrumentProviders().keySet());
        message.put("fra3MInstrumentProviders", fraInstrumentProviders);
      } else {
        s_logger.debug("No FRA 3M instrument providers");
      }
      
      if (object.getFra6MInstrumentProviders() != null) {
        List<JSONObject> fraInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getFra6MInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject fraInstrumentProvidersMessage = new JSONObject();
          fraInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          fraInstrumentProviders.add(fraInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getFra6MInstrumentProviders().keySet());
        message.put("fra6MInstrumentProviders", fraInstrumentProviders);
      } else {
        s_logger.debug("No FRA 6M instrument providers");
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
      
      if (object.getLiborInstrumentProviders() != null) {
        List<JSONObject> liborInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getLiborInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject liborInstrumentProvidersMessage = new JSONObject();
          liborInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          liborInstrumentProviders.add(liborInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getLiborInstrumentProviders().keySet());
        message.put("liborInstrumentProviders", liborInstrumentProviders);
      } else {
        s_logger.debug("No libor instrument providers");
      }

      if (object.getEuriborInstrumentProviders() != null) {
        List<JSONObject> euriborInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getEuriborInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject euriborInstrumentProvidersMessage = new JSONObject();
          euriborInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          euriborInstrumentProviders.add(euriborInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getEuriborInstrumentProviders().keySet());
        message.put("euriborInstrumentProviders", euriborInstrumentProviders);
      } else {
        s_logger.debug("No euribor instrument providers");
      }
      
      if (object.getCDORInstrumentProviders() != null) {
        List<JSONObject> cdorInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getCDORInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject cdorInstrumentProvidersMessage = new JSONObject();
          cdorInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          cdorInstrumentProviders.add(cdorInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getCDORInstrumentProviders().keySet());
        message.put("cdorInstrumentProviders", cdorInstrumentProviders);
      } else {
        s_logger.debug("No CDOR instrument providers");
      }

      if (object.getCiborInstrumentProviders() != null) {
        List<JSONObject> ciborInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getCiborInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject ciborInstrumentProvidersMessage = new JSONObject();
          ciborInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          ciborInstrumentProviders.add(ciborInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getCiborInstrumentProviders().keySet());
        message.put("ciborInstrumentProviders", ciborInstrumentProviders);
      } else {
        s_logger.debug("No cibor instrument providers");
      }

      if (object.getStiborInstrumentProviders() != null) {
        List<JSONObject> stiborInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getStiborInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject stiborInstrumentProvidersMessage = new JSONObject();
          stiborInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          stiborInstrumentProviders.add(stiborInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getEuriborInstrumentProviders().keySet());
        message.put("stiborInstrumentProviders", stiborInstrumentProviders);
      } else {
        s_logger.debug("No stibor instrument providers");
      }
      
      if (object.getSwap3MInstrumentProviders() != null) {
        List<JSONObject> swapInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getSwap3MInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject swapInstrumentProvidersMessage = new JSONObject();
          swapInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          swapInstrumentProviders.add(swapInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getSwap3MInstrumentProviders().keySet());
        message.put("swap3MInstrumentProviders", swapInstrumentProviders);
      } else {
        s_logger.debug("No swap 3M instrument providers");
      }

      if (object.getSwap6MInstrumentProviders() != null) {
        List<JSONObject> swapInstrumentProviders = Lists.newArrayList();
        for (Entry<Tenor, CurveInstrumentProvider> entry : object.getSwap6MInstrumentProviders().entrySet()) {
          if (entry.getKey().getPeriod().toString() == null) {
            throw new OpenGammaRuntimeException(" tenor is null");
          }
          JSONObject swapInstrumentProvidersMessage = new JSONObject();
          swapInstrumentProvidersMessage.put(entry.getKey().getPeriod().toString(), toJSONObject(entry.getValue()));
          swapInstrumentProviders.add(swapInstrumentProvidersMessage);
        }
        allTenors.addAll(object.getSwap6MInstrumentProviders().keySet());
        message.put("swap6MInstrumentProviders", swapInstrumentProviders);
      } else {
        s_logger.debug("No swap 6M instrument providers");
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
