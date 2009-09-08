package com.opengamma.financial.securities;

import javax.time.InstantProvider;



// unsure about this, am thinking generate the codes from the key and then cache the result.
// e.g. have a FutureCodeProvider that takes a FutureKey and generates the code.
public abstract class CodeProvider {
  public abstract void updateCode();
  public abstract String getCode();
  public abstract <T> T accept(CodeProviderVisitor<T> visitor);
  public abstract boolean isValidOn(InstantProvider instant);
}
