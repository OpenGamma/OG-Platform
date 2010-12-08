/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db.hibernate.bond;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.BondSecurityVisitor;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;

/**
 * Bond type enumeration.
 */
public enum BondType {
  /**
   * 
   */
  CORPORATE,
  /**
   * 
   */
  MUNICIPAL,
  /**
   * 
   */
  GOVERNMENT;
  
  public static BondType identify(final BondSecurity object) {
    return object.accept(new BondSecurityVisitor<BondType>() {

      @Override
      public BondType visitCorporateBondSecurity(CorporateBondSecurity security) {
        return CORPORATE;
      }

      @Override
      public BondType visitGovernmentBondSecurity(
          GovernmentBondSecurity security) {
        return GOVERNMENT;
      }

      @Override
      public BondType visitMunicipalBondSecurity(MunicipalBondSecurity security) {
        return MUNICIPAL;
      }
      
    });
  }
  
  public <T> T accept(final BondSecurityVisitor<T> visitor) {
    switch (this) {
      case CORPORATE:
        return visitor.visitCorporateBondSecurity(null);
      case GOVERNMENT:
        return visitor.visitGovernmentBondSecurity(null);
      case MUNICIPAL:
        return visitor.visitMunicipalBondSecurity(null);
      default:
        throw new OpenGammaRuntimeException("unexpected BondType: " + this);
    } 
  }

}
