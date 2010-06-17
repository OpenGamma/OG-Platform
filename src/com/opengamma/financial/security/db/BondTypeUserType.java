/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;


/**
 * Custom Hibernate usertype for the BondType enum
 * 
 * @author andrew
 */
public class BondTypeUserType extends EnumUserType<BondType> {
  
  private static final String CORPORATE_BOND_TYPE = "Corporate";
  private static final String GOVERNMENT_BOND_TYPE = "Government";
  private static final String MUNICIPAL_BOND_TYPE = "Municipal";

  public BondTypeUserType() {
    super(BondType.class, BondType.values());
  }

  @Override
  protected String enumToStringNoCache(BondType value) {
    return value.accept(new BondType.Visitor<String>() {

      @Override
      public String visitCorporateBondType() {
        return CORPORATE_BOND_TYPE;
      }

      @Override
      public String visitGovernmentBondType() {
        return GOVERNMENT_BOND_TYPE;
      }

      @Override
      public String visitMunicipalBondType() {
        return MUNICIPAL_BOND_TYPE;
      }
    });
  }

}