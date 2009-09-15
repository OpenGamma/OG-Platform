package com.opengamma.financial.securities;

public class DataSourceInternalSource extends Dimension {
  public static final String NONE="NONE";
  public static final String CMPL="CMPL";
  public static final String CMPT="CMPT";
  public static final String CMPN="CMPN";
  public static final String ICPL="ICPL";
  
  public DataSourceInternalSource(String name) {
    super(name);
  }
}
