/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.sun.org.apache.xml.internal.utils.UnImplNode;

/**
 * 
 *
 * @author jim
 */
public class FieldXPathRenderVisitor implements RenderVisitor<Object> {
  private String _fieldName;

  public FieldXPathRenderVisitor(String fieldName) {
    _fieldName = fieldName;
  }
  @Override
  public Object visitDiscountCurve(DiscountCurve discountCurve) {
    return discountCurve.getData().get(Double.parseDouble(_fieldName));
  }

  @Override
  public Object visitDoubleTimeSeries(DoubleTimeSeries doubleTimeSeries) {
    throw new UnsupportedOperationException("Time series rendering not supported");
  }

  @Override
  public Object visitGreekResultCollection(
      GreekResultCollection greekResultCollection) {
    return greekResultCollection.get(Greek.valueOf(_fieldName)).getResult();
  }

  @Override
  public Object visitVolatilitySurface(VolatilitySurface volatilitySurface) {
    String[] coords = _fieldName.split(",");
    return volatilitySurface.getVolatility(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
  }

}
