/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneynessFcnBackedByGrid;
import com.opengamma.engine.value.ValueSpecification;

/* package */ class BlackVolatilitySurfaceMoneynessFcnBackedByGridFormatter
    extends AbstractFormatter<BlackVolatilitySurfaceMoneynessFcnBackedByGrid> {

  /* package */ BlackVolatilitySurfaceMoneynessFcnBackedByGridFormatter() {
    super(BlackVolatilitySurfaceMoneynessFcnBackedByGrid.class);
    addFormatter(new Formatter<BlackVolatilitySurfaceMoneynessFcnBackedByGrid>(Format.EXPANDED) {
      @Override
      Object format(BlackVolatilitySurfaceMoneynessFcnBackedByGrid value,
                    ValueSpecification valueSpec,
                    Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  @Override
  public Object formatCell(BlackVolatilitySurfaceMoneynessFcnBackedByGrid value,
                           ValueSpecification valueSpec,
                           Object inlineKey) {
    return SurfaceFormatterUtils.formatCell(value.getSurface());
  }

  private Object formatExpanded(BlackVolatilitySurfaceMoneynessFcnBackedByGrid value) {
    SmileSurfaceDataBundle gridData = value.getGridData();
    Set<Double> strikes = new TreeSet<Double>();
    for (double[] outer : gridData.getStrikes()) {
      for (double inner : outer) {
        strikes.add(inner);
      }
    }
    List<Double> vol = Lists.newArrayList();
    // x values (outer loop of vol) strikes
    // y values (inner loop of vol) expiries
    List<Double> expiries = Lists.newArrayListWithCapacity(gridData.getExpiries().length);
    for (double expiry : gridData.getExpiries()) {
      expiries.add(expiry);
    }
    for (Double strike : strikes) {
      for (Double expiry : expiries) {
        vol.add(value.getVolatility(expiry, strike));
      }
    }
    Map<String, Object> results = Maps.newHashMap();
    results.put(SurfaceFormatterUtils.X_VALUES, expiries);
    results.put(SurfaceFormatterUtils.X_LABELS, SurfaceFormatterUtils.getAxisLabels(expiries));
    results.put(SurfaceFormatterUtils.X_TITLE, "Time to Expiry");
    results.put(SurfaceFormatterUtils.Y_VALUES, strikes);
    results.put(SurfaceFormatterUtils.Y_LABELS, SurfaceFormatterUtils.getAxisLabels(strikes));
    results.put(SurfaceFormatterUtils.Y_TITLE, "Strike");
    results.put(SurfaceFormatterUtils.VOL, vol);
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.SURFACE_DATA;
  }
}
