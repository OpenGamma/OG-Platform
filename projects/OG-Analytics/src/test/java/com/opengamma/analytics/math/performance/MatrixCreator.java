/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.performance;


/**
 * 
 */
public class MatrixCreator {
  private int _width;
  private int _height;
  private double [] _mem;
  
  public MatrixCreator(int height, int width) {
    this._width = width;
    this._height = height;
    this._mem = new double [width * height];
  }

  public MatrixCreator() {
    this._height = 0;
    this._width = 0;
    this._mem = null;        
  }
  
  /**
   * Gets the _width.
   * @return the _width
   */
  public int getwidth() {
    return _width;
  }

  /**
   * Gets the _height.
   * @return the _height
   */
  public int getheight() {
    return _height;
  }

  /**
   * Gets the _mem.
   * @return the _mem
   */
  public double[] getmem() {
    return _mem;
  }
  
  public double getmem(int val) {
    return _mem[val];
  }
  
  public double getval(int i, int j) {
    return _mem[i * this._width + j];
  }
  
  public void setval(int i, int j) {
    _mem[i * this._width + j] = i * this._width + j;
  }  
  
}
