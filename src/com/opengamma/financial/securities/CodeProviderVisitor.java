package com.opengamma.financial.securities;

import com.opengamma.financial.securities.EquityCodeProvider;
import com.opengamma.financial.securities.FXCodeProvider;

public interface CodeProviderVisitor<T> {
  public T visitEquityCodeProvider(EquityCodeProvider equityCodeProvider);
  public T visitFXCodeProvider(FXCodeProvider fxCodeProvider);
}
