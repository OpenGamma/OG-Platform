/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db.bond;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.BondSecurityVisitor;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;

public enum BondType {
  CORPORATE,
  MUNICIPAL,
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
  
  public static interface Visitor<T> {
    T visitCorporateBondType();
    T visitGovernmentBondType();
    T visitMunicipalBondType();
  }
  
  public <T> T accept(final Visitor<T> visitor) {
    switch (this) {
      case CORPORATE:
        return visitor.visitCorporateBondType();
      case GOVERNMENT:
        return visitor.visitGovernmentBondType();
      case MUNICIPAL:
        return visitor.visitMunicipalBondType();
      default:
        throw new OpenGammaRuntimeException("unexpected BondType: " + this);
    } 
  }
  
}