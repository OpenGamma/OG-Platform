package com.opengamma.financial.securities;

import com.opengamma.financial.timeseries.update.EquityCodeProvider;
import com.opengamma.financial.timeseries.update.FXCodeProvider;

public interface CodeProviderVisitor<T> {
  public T visitEquityCodeProvider(EquityCodeProvider equityCodeProvider);
  public T visitFXCodeProvider(FXCodeProvider fxCodeProvider);
}
