/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.loadsave.sheet;

/**
 * Known sheet formats
 */
public enum SheetFormat {

  /** CSV sheet */
  CSV, 

  /** XLS sheet */
  XLS, 

  /** XLSX sheet */
  XLSX,
  
  /** ZIP sheet */
  ZIP,

  /** Unknown sheet */
  UNKNOWN;
 
  public static SheetFormat of(String filename) {
    String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase().trim();
    if (extension.equals(".csv")) {
      return SheetFormat.CSV;
    } else if (extension.equals(".xls")) {
      return SheetFormat.XLS;
    } else if (extension.equals(".xlsx")) {
      return SheetFormat.XLSX;
    } else if (extension.equals(".zip")) {
      return SheetFormat.ZIP;
    } else {
      return SheetFormat.UNKNOWN;
    }
  }

}
