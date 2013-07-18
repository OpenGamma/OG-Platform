/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.web.server.conversion.LabelFormatter;

/**
 *
 */
/* package */ class SurfaceFormatterUtils {

  static final String X_LABELS = "xLabels";
  static final String Y_LABELS = "yLabels";
  static final String Y_VALUES = "yValues";
  static final String X_VALUES = "xValues";
  static final String X_TITLE = "xTitle";
  static final String Y_TITLE = "yTitle";
  static final String VOL = "vol";

  /* package */ static Object formatExpanded(Surface<Double, Double, Double> surface) {
    if (surface instanceof InterpolatedDoublesSurface) {
      List<Double> vol = Lists.newArrayList();
      // the x and y values won't necessarily be unique and won't necessarily map to a rectangular grid
      // this projects them onto a grid with values at every point
      Set<Double> xData = Sets.newTreeSet(Arrays.asList(surface.getXData()));
      Set<Double> yData = Sets.newTreeSet(Arrays.asList(surface.getYData()));
      for (Double y : yData) {
        for (Double x : xData) {
          vol.add(surface.getZValue(x, y));
        }
      }
      Map<String, Object> results = Maps.newHashMap();
      results.put(X_VALUES, xData);
      results.put(X_LABELS, SurfaceFormatterUtils.getAxisLabels(xData));
      results.put(X_TITLE, ""); // TODO use labels from VolatilitySurface once they exist
      results.put(Y_VALUES, yData);
      results.put(Y_LABELS, SurfaceFormatterUtils.getAxisLabels(yData));
      results.put(Y_TITLE, ""); // TODO use labels from VolatilitySurface once they exist
      results.put(VOL, vol);
      return results;
    } else if (surface instanceof ConstantDoublesSurface) {
      Map<String, Object> results = Maps.newHashMap();
      results.put(LabelledMatrix2DFormatter.X_LABELS, Collections.singletonList("All"));
      results.put(LabelledMatrix2DFormatter.Y_LABELS, Collections.singletonList("All"));
      results.put(LabelledMatrix2DFormatter.MATRIX, Collections.singletonList(surface.getZData()));
      return results;
    } else {
      // TODO format as matrix
      // TODO this won't work - the cell value isn't an error so this makes no difference
      return new MissingValueFormatter("Unable to format surface of type " + surface.getClass().getSimpleName());
    }
  }

  /* package */ static DataType getDataType(Surface<Double, Double, Double> surface) {
    if (surface instanceof InterpolatedDoublesSurface) {
      return DataType.SURFACE_DATA;
    } else {
      return DataType.LABELLED_MATRIX_2D;
    }
  }

  /* package */ static Object formatCell(Surface<Double, Double, Double> surface) {
    if (surface instanceof InterpolatedDoublesSurface || surface instanceof NodalDoublesSurface) {
      return "Volatility Surface (" + surface.getXData().length + " x " + surface.getYData().length + ")";
    } else {
      return "Volatility Surface";
    }
  }

  /* package */ static List<String> getAxisLabels(Collection<?> values) {
    List<String> labels = Lists.newArrayListWithCapacity(values.size());
    for (Object value : values) {
      labels.add(LabelFormatter.format(value));
    }
    return labels;
  }

}
