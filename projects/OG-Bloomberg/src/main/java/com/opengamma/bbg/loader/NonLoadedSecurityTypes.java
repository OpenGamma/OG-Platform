/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.bbg.BloombergConstants;

/**
 * Security type mappings for types which cannot be loaded, either because no loader has been written or because they
 * cannot or do not need to be represented as a security in the security master.
 */
public final class NonLoadedSecurityTypes {

  // NOTE jonathan 2011-11-08 -- normally the constants below would be in a specific loader, but the point of this is
  // to allow securities for which we do not have a loader to be mapped to a security type.

  // Swaps
  private static final String BLOOMBERG_BASIS_SWAP_TYPE = "BASIS SWAP";
  private static final String BLOOMBERG_FWD_SWAP_TYPE = "FWD SWAP";
  private static final String BLOOMBERG_NDF_SWAP_TYPE = "NDF SWAP";
  private static final String BLOOMBERG_OVERNIGHT_INDEXED_SWAP_TYPE = "OVERNIGHT INDEXED SWAP";
  private static final String BLOOMBERG_SWAP_TYPE = "SWAP";
  private static final String BLOOMBERG_ONSHORE_SWAP_TYPE = "ONSHORE SWAP";
  private static final String BLOOMBERG_NON_DELIVERABLE_OIS_TYPE = "NON-DELIVERABLE OIS SWAP";
  /**
   * The valid Bloomberg security types for swaps
   */
  public static final Set<String> VALID_SWAP_SECURITY_TYPES = ImmutableSet.of(
      BLOOMBERG_SWAP_TYPE,
      BLOOMBERG_OVERNIGHT_INDEXED_SWAP_TYPE,
      BLOOMBERG_FWD_SWAP_TYPE,
      BLOOMBERG_NDF_SWAP_TYPE,
      BLOOMBERG_ONSHORE_SWAP_TYPE,
      BloombergConstants.BLOOMBERG_NON_DELIVERABLE_IRS_SWAP_TYPE,
      BloombergConstants.BLOOMBERG_IMM_SWAP_TYPE,
      BLOOMBERG_NON_DELIVERABLE_OIS_TYPE);
  /**
   * The valid Bloomberg security types for basis swaps
   */
  public static final Set<String> VALID_BASIS_SWAP_SECURITY_TYPES = ImmutableSet.of(
      BLOOMBERG_BASIS_SWAP_TYPE);

  // FRAs
  private static final String BLOOMBERG_FRA_TYPE = "FRA";
  /**
   * The valid Bloomberg security types for FRAs
   */
  public static final Set<String> VALID_FRA_SECURITY_TYPES = ImmutableSet.of(
      BLOOMBERG_FRA_TYPE);

  // Volatility quotes
  private static final String BLOOMBERG_OPTION_VOLATILITY_TYPE = "OPTION VOLATILITY";
  private static final String BLOOMBERG_SWAPTION_VOLATILITY_TYPE = "SWAPTION VOLATILITY";
  /**
   * The valid Bloomberg security types where quotes are provided
   */
  public static final Set<String> VALID_VOLATILITY_QUOTE_TYPES = ImmutableSet.of(
      BLOOMBERG_OPTION_VOLATILITY_TYPE,
      BLOOMBERG_SWAPTION_VOLATILITY_TYPE);

  // Misc rates
  private static final String BLOOMBERG_PHYSICAL_COMMODITY_SPOT_TYPE = "Physical commodity spot.";
  private static final String BLOOMBERG_SPOT_TYPE = "SPOT";
  private static final String BLOOMBERG_CROSS_TYPE = "CROSS";
  private static final String BLOOMBERG_DEPOSIT_TYPE = "DEPOSIT";
  private static final String BLOOMBERG_FX_FORWARD_TYPE = "FORWARD";
  private static final String BLOOMBERG_FX_ONSHORE_FORWARD_TYPE = "ONSHORE FORWARD";
  private static final String BLOOMBERG_FX_NDF = "NON-DELIVERABLE FORWARD";
  private static final String BLOOMBERG_CD = "CD";
  private static final String BLOOMBERG_INFLATION_SWAP_TYPE = "INFLATION SWAP";

  /**
   * CDS rates
   */
  private static final String BLOOMBERG_CDS_TYPE = "CREDIT DEFAULT SWAP";

  /**
   * The valid Bloomberg security types for spot rates
   */
  public static final Set<String> VALID_SPOT_RATE_TYPES = ImmutableSet.of(
      BLOOMBERG_PHYSICAL_COMMODITY_SPOT_TYPE,
      BLOOMBERG_SPOT_TYPE,
      BLOOMBERG_CROSS_TYPE,
      BLOOMBERG_CD);
  /**
   * The valid Bloomberg security types for rates
   */
  public static final Set<String> VALID_RATE_TYPES = ImmutableSet.of(
      BLOOMBERG_DEPOSIT_TYPE,
      BloombergConstants.BLOOMBERG_PHYSICAL_INDEX_FUTURE_TYPE);

  // Equity Indices
  /**
   * The valid Bloomberg security types for equity indices
   */
  public static final Set<String> VALID_EQUITY_INDEX_SECURITY_TYPES = ImmutableSet.of(
      BloombergConstants.BLOOMBERG_EQUITY_INDEX_TYPE);

  // Forward Cross
  private static final String BLOOMBERG_FORWARD_CROSS_TYPE = "FORWARD CROSS";
  /**
   * The valid Bloomberg security types for forward cross products
   */
  public static final Set<String> VALID_FORWARD_CROSS_SECURITY_TYPES = ImmutableSet.of(
      BLOOMBERG_FORWARD_CROSS_TYPE);

  // Bills
  private static final String BLOOMBERG_BANK_BILL_TYPE = "BANK BILL";
  /**
   * The valid Bloomberg security types for bills
   */
  public static final Set<String> VALID_BILL_TYPES = ImmutableSet.of(BLOOMBERG_BANK_BILL_TYPE);

  /**
   * The valid Bloomberg security types for FX forwards
   */
  public static final Set<String> VALID_FX_FORWARD_TYPES = ImmutableSet.of(BLOOMBERG_FX_FORWARD_TYPE, BLOOMBERG_FX_ONSHORE_FORWARD_TYPE, BLOOMBERG_FX_NDF);

  /**
   * The valid Bloomberg security types for CDS
   */
  public static final Set<String> VALID_CDS_TYPES = ImmutableSet.of(BLOOMBERG_CDS_TYPE);

  /**
   * The valid Bloomberg security types for inflation swaps.
   */
  public static final Set<String> VALID_INFLATION_SWAP_TYPES = ImmutableSet.of(BLOOMBERG_INFLATION_SWAP_TYPE);
}
