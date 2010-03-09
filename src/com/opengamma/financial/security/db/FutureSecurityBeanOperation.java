/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.BondFutureSecurity;
import com.opengamma.financial.security.FXFutureSecurity;
import com.opengamma.financial.security.FutureSecurity;
import com.opengamma.financial.security.VanillaFutureSecurity;
import com.opengamma.id.DomainSpecificIdentifier;

/* package */ class FutureSecurityBeanOperation extends Converters implements BeanOperation<FutureSecurity,FutureSecurityBean> {
  
  public static final FutureSecurityBeanOperation INSTANCE = new FutureSecurityBeanOperation ();
  
  private FutureSecurityBeanOperation () {
  }
  
  @Override
  public FutureSecurity createSecurity (final DomainSpecificIdentifier identifier, final FutureSecurityBean bean) {
    switch (bean.getFutureType ()) {
    case BOND :
      return new BondFutureSecurity (
          dateToExpiry (bean.getExpiry ()),
          bean.getMonth (),
          bean.getYear (),
          bean.getTradingExchange ().getName (),
          bean.getSettlementExchange ().getName ()
          );
    case FX :
      return new FXFutureSecurity (
          dateToExpiry (bean.getExpiry ()),
          bean.getMonth (),
          bean.getYear (),
          bean.getTradingExchange ().getName (),
          bean.getSettlementExchange ().getName ()
          );
    case VANILLA :
      return new VanillaFutureSecurity (
          dateToExpiry (bean.getExpiry ()),
          bean.getMonth (),
          bean.getYear (),
          bean.getTradingExchange ().getName (),
          bean.getSettlementExchange ().getName ()
          );
    default :
      throw new OpenGammaRuntimeException ("Bad value for futureSecurityType (" + bean.getFutureType () + ")");
    }
  }

  @Override
  public boolean beanEquals(FutureSecurityBean bean, FutureSecurity security) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public FutureSecurityBean createBean(final HibernateSecurityMasterSession secMasterSession, final FutureSecurity security) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<? extends FutureSecurityBean> getBeanClass() {
    return FutureSecurityBean.class;
  }

  @Override
  public Class<? extends FutureSecurity> getSecurityClass() {
    return FutureSecurity.class;
  }

  @Override
  public String getSecurityType() {
    return "FUTURE";
  }
  
}