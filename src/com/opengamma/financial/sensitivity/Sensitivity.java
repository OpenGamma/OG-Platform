package com.opengamma.financial.sensitivity;


public enum Sensitivity {
  VALUE_DELTA {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitValueDelta();
    }
  },
  VALUE_GAMMA {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitValueGamma();
    }

  },
  VALUE_VEGA {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitValueVega();
    }

  },
  VALUE_THETA {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitValueTheta();
    }

  },
  PV01 {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitPV01();
    }

  },
  CONVEXITY {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitConvexity();
    }
  };

  public abstract <T> T accept(SensitivityVisitor<T> visitor);

}
