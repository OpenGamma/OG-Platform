package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.financial.interestrate.capletstripping.BasisSplineVolatilityTermStructureProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;

public class CapletStripperPSplineTermStructure implements CapletStripper {
  private static final MatrixAlgebra MA = new OGMatrixAlgebra();
  private static final Function1D<DoubleMatrix1D, Boolean> POSITIVE = new PositiveOrZero();
  private final CapletStrippingImp _imp;
  private final DoubleMatrix2D _penalty;


  public CapletStripperPSplineTermStructure(MultiCapFloorPricer pricer, BasisSplineVolatilityTermStructureProvider vtsp, final double lambda) {
    DiscreteVolatilityFunctionProvider volFuncPro = new DiscreateVolatilityFunctionProviderFromVolSurface(vtsp);
    _imp = new CapletStrippingImp(pricer, volFuncPro);
    int size = vtsp.getNumModelParameters();
    _penalty = (DoubleMatrix2D) MA.scale(PenaltyMatrixGenerator.getPenaltyMatrix(size, 2),lambda);

    //     (DoubleMatrix2D) MA.scale(psf.getPenaltyMatrix(_nWeights, DIFFERENCE_ORDER), LAMBDA);
  }

  @Override
  public CapletStrippingResult solve(double[] marketValues, MarketDataType type) {
    return null;
  }

  @Override
  public CapletStrippingResult solve(double[] marketValues, MarketDataType type, double[] errors) {
    return null;
  }

  @Override
  public CapletStrippingResult solve(double[] marketValues, MarketDataType type, DoubleMatrix1D guess) {
    return null;
  }

  @Override
  public CapletStrippingResult solve(double[] marketValues, MarketDataType type, double[] errors, DoubleMatrix1D guess) {
    if (type == MarketDataType.PRICE) {
      return _imp.solveForCapPrices(marketValues, errors, guess, _penalty, POSITIVE);
    } else if (type == MarketDataType.VOL) {
      return _imp.solveForCapVols(marketValues, errors, guess, _penalty, POSITIVE);
    }
    throw new IllegalArgumentException("Unknown MarketDataType " + type.toString());
  }

}
