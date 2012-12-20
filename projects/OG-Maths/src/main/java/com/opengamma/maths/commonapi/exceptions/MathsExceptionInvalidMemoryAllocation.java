/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.commonapi.exceptions;

/**
 * Provides a manner in which maths exceptions relating to incorrect mallocs can be thrown
 */
public class MathsExceptionInvalidMemoryAllocation extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Enumerated variable type as vector or matrix for the sake of throwing more comprehensive error messages.
   */
  public enum varType   {
    /**
     * matrix indicates that the variable referenced is considered a matrix in a mathematical sense.
     */
    matrix,
    /**
     * vector indicates that the variable referenced is considered a vector in a mathematical sense.
     */    
    vector;
  }

  public MathsExceptionInvalidMemoryAllocation() {
    super();
  }

  public MathsExceptionInvalidMemoryAllocation(final varType type, final String varName, final double[] var, final int m, final int n) {
    super("Malloc problem:" + (type == varType.matrix ? "Matrix" : "Vector") + varName + " has " + var.length + " data entries but dimensions "
        + "[" + m + " x " + m + "] suggests " + m * n + " entries");
  }

  public MathsExceptionInvalidMemoryAllocation(final varType type, final String varName, final double[] var, final int m, final int n, final Throwable cause) {
    super("Malloc problem:" + (type == varType.matrix ? "Matrix" : "Vector") + varName + " has " + var.length + " data entries but dimensions "
        + "[" + m + " x " + m + "] suggests " + m * n + " entries", cause);
  }

  public MathsExceptionInvalidMemoryAllocation(final Throwable cause) {
    super(cause);
  }
}
