/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.index;

/**
 * An enum representing the weighting type of an index:<p>
 * <ul>
 *   <li> Price weighted. The price of the index member is used to calculate the weighting.
 *   <li> Full capitalization weighted. The market capitalization of the index member is used to
 *   calculate the weighting. All outstanding shares are included.
 *   <li> Float-adjusted capitalization weighted. The market capitalization of the index member
 *   is used to calculate the weighting. Only the public float is included.
 *   <li> Modified capitalization weighted. The market capitalization of the index member capped to
 *   a percentage of the index is used to calculate the weighting. If the weighting is higher than
 *   the cap, the remaining weight is distributed equally amongst the other members
 *   <li> Equal weighted. All index members have the same weighting.
 *   <li> Attribute weighted. An attribute (e.g. value, growth) is used to determine the weighting
 *   of the index member.
 * </ul>
 */
public enum IndexWeightingType {

  /**
   * Attribute.
   */
  ATTRIBUTE,
  /**
   * Equal.
   */
  EQUAL,
  /**
   * Float-adjusted capitalization weighted.
   */
  FLOAT_ADJUSTED_CAPITALIZATION,
  /**
   * Full capitalization weighted.
   */
  FULL_CAPITALIZATION,
  /**
   * Modified capitalization.
   */
  MODIFIED_CAPITALIZATION,
  /**
   * Price weighted.
   */
  PRICE

}
