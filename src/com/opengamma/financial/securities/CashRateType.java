/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.securities;

public class CashRateType extends Dimension {
  public static final String LIBOR="LIBOR"; // London
  public static final String EURIBOR="EURIBOR"; // Euro
  public static final String STIBOR="STIBOR"; // Stockholm
  public static final String NIBOR="NIBOR"; // Norway
  public static final String WIBOR="WIBOR"; // Warsaw
  public static final String SIBOR="SIBOR"; // Singapore
  public static final String TIBOR="TIBOR"; // Tokyo
  public static final String RIGIBOR="RIGIBOR"; // Latvia (Riga)
  public static final String TALIBOR="TALIBOR"; // Estonia
  public static final String HELIBOR="HELIBOR"; // Helsinki, obsolete
  public static final String MIBOR="MIBOR"; // Moscow, same name used for Mumbai
  public static final String KIBOR="KIBOR"; // Kiev
  public CashRateType(String name) {
    super(name);
  }
}
