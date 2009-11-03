package com.opengamma.financial.render;

import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.timeseries.DoubleTimeSeries;

public interface RenderVisitor<T> {
  public T visitDiscountCurve(InterpolatedDiscountCurve discountCurve);
  public T visitVolatilitySurface(VolatilitySurface volatilitySurface);
  public T visitDoubleTimeSeries(DoubleTimeSeries doubleTimeSeries);
  public T visitGreekResultCollection(GreekResultCollection greekResultCollection);
}
