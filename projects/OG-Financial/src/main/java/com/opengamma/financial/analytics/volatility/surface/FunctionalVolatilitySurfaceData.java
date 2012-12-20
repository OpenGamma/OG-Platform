/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.ArgumentChecker;

/**
 * Data structure to hold information about a functional volatility surface and information to help
 * with plotting (minimum and maximum axis values and the number of values to sample between the two).
 */
public class FunctionalVolatilitySurfaceData {
  private final VolatilitySurface _surface;
  private final String _xLabel;
  private final double _xMinimum;
  private final double _xMaximum;
  private final int _nX;
  private final String _yLabel;
  private final double _yMinimum;
  private final double _yMaximum;
  private final int _nY;
  private final double _zMinimum;
  private final double _zMaximum;

  public FunctionalVolatilitySurfaceData(final VolatilitySurface surface, final String xLabel, final double xMinimum, final double xMaximum,
      final int nX, final String yLabel, final double yMinimum, final double yMaximum, final int nY, final double zMinimum, final double zMaximum) {
    ArgumentChecker.notNull(surface, "surface");
    ArgumentChecker.notNull(xLabel, "x label");
    ArgumentChecker.notNull(yLabel, "y label");
    ArgumentChecker.isTrue(surface.getSurface() instanceof FunctionalDoublesSurface, "surface must be a FunctionalVolatilitySurface; have {}", surface.getSurface().getClass());
    ArgumentChecker.isTrue(xMinimum < xMaximum, "minimum value of x {} must be less than the maximum value {}", xMinimum, xMaximum);
    ArgumentChecker.isTrue(nX > 0, "number of x samples {} must be greater than zero", nX);
    ArgumentChecker.isTrue(yMinimum < yMaximum, "minimum value of y {} must be less than the maximum value {}", yMinimum, yMaximum);
    ArgumentChecker.isTrue(nY > 0, "number of y samples {} must be greater than zero", nY);
    _surface = surface;
    _xLabel = xLabel;
    _xMinimum = xMinimum;
    _xMaximum = xMaximum;
    _nX = nX;
    _yLabel = yLabel;
    _yMinimum = yMinimum;
    _yMaximum = yMaximum;
    _nY = nY;
    _zMinimum = zMinimum;
    _zMaximum = zMaximum;
  }

  public VolatilitySurface getSurface() {
    return _surface;
  }

  public String getXLabel() {
    return _xLabel;
  }

  public double getXMinimum() {
    return _xMinimum;
  }

  public double getXMaximum() {
    return _xMaximum;
  }

  public int getNXSamples() {
    return _nX;
  }

  public String getYLabel() {
    return _yLabel;
  }

  public double getYMinimum() {
    return _yMinimum;
  }

  public double getYMaximum() {
    return _yMaximum;
  }

  public int getNYSamples() {
    return _nY;
  }

  public double getZMinimum() {
    return _zMinimum;
  }

  public double getZMaximum() {
    return _zMaximum;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _nX;
    long temp;
    temp = Double.doubleToLongBits(_nY);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _surface.hashCode();
    result = prime * result + _xLabel.hashCode();
    temp = Double.doubleToLongBits(_xMaximum);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_xMinimum);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _yLabel.hashCode();
    temp = Double.doubleToLongBits(_yMaximum);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_yMinimum);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_zMaximum);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_zMinimum);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FunctionalVolatilitySurfaceData)) {
      return false;
    }
    final FunctionalVolatilitySurfaceData other = (FunctionalVolatilitySurfaceData) obj;
    if (_nX != other._nX) {
      return false;
    }
    if (_nY != other._nY) {
      return false;
    }
    if (Double.compare(_xMinimum, other._xMinimum) != 0) {
      return false;
    }
    if (Double.compare(_xMaximum, other._xMaximum) != 0) {
      return false;
    }
    if (Double.compare(_yMinimum, other._yMinimum) != 0) {
      return false;
    }
    if (Double.compare(_yMaximum, other._yMaximum) != 0) {
      return false;
    }
    if (Double.compare(_zMinimum, other._zMinimum) != 0) {
      return false;
    }
    if (Double.compare(_zMaximum, other._zMaximum) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_xLabel, other._xLabel)) {
      return false;
    }
    if (!ObjectUtils.equals(_yLabel, other._yLabel)) {
      return false;
    }
    if (!ObjectUtils.equals(_surface, other._surface)) {
      return false;
    }
    return true;
  }

}
