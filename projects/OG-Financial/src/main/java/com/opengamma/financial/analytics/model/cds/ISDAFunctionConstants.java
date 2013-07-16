/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

/**
 * Constant strings for value requirement/property names for use with the ISDA pricing functions
 *
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 */
public class ISDAFunctionConstants {

  /**
   * Name of the ISDA calculation method
   */
  public static final String ISDA_METHOD_NAME = "ISDA";

  /**
   * Value property name describing which implementation of the ISDA library to use
   */
  public static final String ISDA_IMPLEMENTATION = "ISDA Implementation";

  /**
   * Value property to require using the approximate (Java) re-implementation of the ISDA model
   */
  public static final String ISDA_IMPLEMENTATION_APPROX = "APPROX";

  /**
   * Value property to require using the original native (external C code) implementation of the ISDA model
   */
  public static final String ISDA_IMPLEMENTATION_NATIVE = "NATIVE";

  /**
   * Value property to require using the original native (external C code) implementation of the ISDA model
   */
  public static final String ISDA_IMPLEMENTATION_NEW = "NEW";

  /**
   * Value property name describing how the hazard rate should be modelled
   */
  public static final String ISDA_HAZARD_RATE_STRUCTURE = "ISDA Hazard Rate Structure";

  /**
   * Value property to require a hazard rate term structure
   */
  public static final String ISDA_HAZARD_RATE_TERM = "TERM";

  /**
   * Value property to require a flat hazard rate
   */
  public static final String ISDA_HAZARD_RATE_FLAT = "FLAT";

  /**
   * Value property to give the offset for the ISDA yield curve
   */
  public static final String ISDA_CURVE_OFFSET = "ISDACurveOffset";

  /**
   * Value property for the ISDA curve date
   */
  public static final String ISDA_CURVE_DATE = "ISDACurveDate";

  /**
   * Value property for the ISDA curve date
   */
  public static final String CDS_QUOTE_CONVENTION = "CDSQuoteConvention";

  /**
   * Value property for the ISDA bucket tenors
   */
  public static final String ISDA_BUCKET_TENORS = "ISDABucketTenors";
}
