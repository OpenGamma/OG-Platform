package com.opengamma.financial.securities;

import java.util.HashMap;
import java.util.Map;

public enum ExchangeType {
  EQUITY, // AMEX, ASX, HKEX, MLSE, MSE, NASDAQ, NSE, NYSE, NZX, OTCBB, PSE, SFE, SGX, TSXV
  FX, // FOREX
  COMMODITIES_METALS, // COMEX
  COMMODITIES_PHYSICAL, // NYMEX, NYBOT
  COMMODITIES_MIXED, // CBOT
  DERIVATIVES_GENERAL, // CME, LIFFE, NZFOE
  INDICES, // INDEX
  COMMODITIES_AGRICULTURAL, // MGEX, KCBT, WCE
  US_MUTUAL_FUNDS; // USMF
  
  private static Map<String, ExchangeType> s_exchangeTypeMap = new HashMap<String, ExchangeType>();
  // TODO: load this by injection of a file or database connection.
  static {
    for (String equityExchange : new String[] { "AMEX", "ASX", "HKEX", "MLSE", "MSE", "NASDAQ", "NSE", "NYSE", "NZX", "OTCBB", "PSE", "SFE", "SGX", "TSXV" }) {
      s_exchangeTypeMap.put(equityExchange, EQUITY);
    }
    s_exchangeTypeMap.put("FOREX", FX);
    s_exchangeTypeMap.put("CONEX", COMMODITIES_METALS);
    s_exchangeTypeMap.put("NYMEX", COMMODITIES_PHYSICAL);
    s_exchangeTypeMap.put("NYBOT", COMMODITIES_PHYSICAL);
    s_exchangeTypeMap.put("CBOT", COMMODITIES_MIXED);
    s_exchangeTypeMap.put("CNE", DERIVATIVES_GENERAL);
    s_exchangeTypeMap.put("LIFFE", DERIVATIVES_GENERAL);
    s_exchangeTypeMap.put("NZFOE", DERIVATIVES_GENERAL);
    s_exchangeTypeMap.put("INDEX", INDICES);
    s_exchangeTypeMap.put("MGEX", COMMODITIES_AGRICULTURAL);
    s_exchangeTypeMap.put("KCBT", COMMODITIES_AGRICULTURAL);
    s_exchangeTypeMap.put("WCE", COMMODITIES_AGRICULTURAL);
    s_exchangeTypeMap.put("USMF", US_MUTUAL_FUNDS);
  }
  public static ExchangeType getType(String exchangeCode) {
    return s_exchangeTypeMap.get(exchangeCode);
  }
}
