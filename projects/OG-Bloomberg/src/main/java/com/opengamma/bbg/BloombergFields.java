/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

// REVIEW kirk 2009-10-12 -- This really should be an object that's created by parsing
// the bbfields.tbl file. For now, we put it here so that we can get at it easily.
/**
 * Fields used to access Bloomberg.
 */
public interface BloombergFields {

  /**
   * The last price.
   */
  String LAST_PRICE_FIELD = "LAST_PRICE";
  /**
   * The mid value.
   */
  String MID_FIELD = "MID";
  /**
   * The bid value.
   */
  String BID_FIELD = "BID";
  /**
   * The ask value.
   */
  String ASK_FIELD = "ASK";
  /**
   * The index members.
   */
  String INDEX_MEMBERS_FIELD = "INDX_MEMBERS";
  /**
   * The Current Market Cap.
   */
  String CURRENT_MARKET_CAP_FIELD = "CUR_MKT_CAP";

}
