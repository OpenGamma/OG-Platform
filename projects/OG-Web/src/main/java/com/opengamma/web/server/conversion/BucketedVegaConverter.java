/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.greeks.BucketedGreekResultCollection;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class BucketedVegaConverter implements ResultConverter<BucketedGreekResultCollection> {
  private static final Logger s_logger = LoggerFactory.getLogger(BucketedVegaConverter.class);
  private static final DecimalFormat FORMAT = new DecimalFormat("##.###");
  
  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, BucketedGreekResultCollection value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    if (value.getBucketedGreeks(BucketedGreekResultCollection.BUCKETED_VEGA) != null) {      
      double[] expiries = value.getExpiries();
      double[][] strikes = value.getStrikes();
      double[] uniqueStrikes = strikes[0];
      for (int i = 1; i < strikes.length; i++) {
        if (strikes[i].length != uniqueStrikes.length) {
          s_logger.warn("Did not have a rectangular bucketed vega surface");
          return result;
        }
      }
      result.put("yCount", expiries.length);
      result.put("xCount", uniqueStrikes.length);
      if (mode == ConversionMode.FULL) {
        double[][] surface = value.getBucketedGreeks(BucketedGreekResultCollection.BUCKETED_VEGA);
        boolean[][] missingValues = new boolean[surface.length][surface[0].length];
        Object[] yLabels = new Object[expiries.length];
        Object[] xLabels = new Object[uniqueStrikes.length];
        for (int i = 0; i < expiries.length; i++) {
          yLabels[i] = FORMAT.format(expiries[i]);        
        }
        for (int i = 0; i < uniqueStrikes.length; i++) {
          xLabels[i] = FORMAT.format(i);
        }
        result.put("xs", xLabels);
        result.put("ys", yLabels);
        result.put("surface", surface);
        result.put("missingValues", missingValues);
      } 
    }
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, BucketedGreekResultCollection value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, BucketedGreekResultCollection value) {
    return "Bucketed Vega";
  }

  @Override
  public String getFormatterName() {
    return "SURFACE_DATA";
  }

}
