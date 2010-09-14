/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db.hibernate.bond;

import com.opengamma.financial.security.bond.BondSecurityVisitor;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.master.db.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the BondType enum
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
    return value.accept(new BondSecurityVisitor<String>() {

      @Override
      public String visitCorporateBondSecurity(CorporateBondSecurity bond) {
        return CORPORATE_BOND_TYPE;
      }

      @Override
      public String visitGovernmentBondSecurity(GovernmentBondSecurity bond) {
        return GOVERNMENT_BOND_TYPE;
      }

      @Override
      public String visitMunicipalBondSecurity(MunicipalBondSecurity bond) {
        return MUNICIPAL_BOND_TYPE;
      }
    });
  }

}
