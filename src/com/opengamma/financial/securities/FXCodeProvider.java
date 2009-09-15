/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.securities;

import javax.time.InstantProvider;

import com.opengamma.financial.securities.keys.FXKey;

/**
 * 
 *
 * @author jim
 */
public abstract class FXCodeProvider extends CodeProvider {
  private final String _code;

  public FXCodeProvider(FXKey fx) {
    _code = buildCode(fx);
  }
  
  protected abstract String buildCode(FXKey fx);
  
  @Override
  public <T> T accept(CodeProviderVisitor<T> visitor) {
    return visitor.visitFXCodeProvider(this);
  }

  @Override
  public String getCode() {
    return _code;
  }

  @Override
  public boolean isValidOn(InstantProvider instant) {
    return true;
  }

  @Override
  public void updateCode() {
  }

}
