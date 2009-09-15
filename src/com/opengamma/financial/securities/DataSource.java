/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.securities;

/**
 * 
 *
 * @author jim
 */
public class DataSource extends Dimension {
  public static final String BLOOMBERG="BLOOMBERG";
  public static final String REUTERS="REUTERS";
  public static final String JPM="JPM";
  public static final String BARCLAYS="BARCLAYS";
  public DataSource(String name) {
    super(name);
  }
}
