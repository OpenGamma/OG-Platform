/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.sesame.marketdata.MapMarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.RawId;

/**
 * Load key/value pair market data resources as a map of {@link ExternalIdBundle} to double.
 */
public class MarketDataResourcesLoader {

  public static Map<ExternalIdBundle, Double> getData(String path, String scheme)  throws IOException {
    return getData(path, scheme == null ? null : ExternalScheme.of(scheme));
  }

  public static Map<ExternalIdBundle, Double> getData(String path, ExternalScheme scheme) throws IOException {
    Properties properties = new Properties();
    try (InputStream stream = MarketDataResourcesLoader.class.getResourceAsStream(path);
         Reader reader = new BufferedReader(new InputStreamReader(stream))) {
      properties.load(reader);
    }
    Map<ExternalIdBundle, Double> data = Maps.newHashMap();

    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      String id = (String) entry.getKey();
      String value = (String) entry.getValue();
      
      if (scheme != null ) {
        data.put(ExternalId.of(scheme, id).toBundle(), Double.valueOf(value));
      } else {
        data.put(ExternalId.parse(id).toBundle(), Double.valueOf(value));
      }
    }
    return data;
  }

  public static MarketDataBundle getPreloadedBundle(String path, String scheme) throws IOException {
    MarketDataEnvironmentBuilder builder = new MarketDataEnvironmentBuilder();
    for (Map.Entry<ExternalIdBundle, Double> entry : getData(path, scheme).entrySet()) {
      builder.add(RawId.of(entry.getKey()), entry.getValue());
    }
    builder.valuationTime(ZonedDateTime.now());
    return new MapMarketDataBundle(builder.build());
  }
}
