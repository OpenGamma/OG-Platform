/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.linearalgebra.Decomposer;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.linearalgebra.SingularValueDecomposition;
import com.opengamma.math.linearalgebra.SingularValueDecompositionColt;
import com.opengamma.math.linearalgebra.SingularValueDecompositionResult;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * Attempts to find the multi-dimensional root of a series of N equations with N variables, i.e. a square problem. If the analytic Jacobian is not known, it will be calculated using central difference 
 */
public class NewtonVectorRootFinder extends VectorRootFinder {

  private static final double DEF_TOL = 1e-7;
  private static final int MAX_STEPS = 100;
  private static final double EPS = 1e-8;
  private final double _relTol, _absTol;
  private final int _maxSteps;
  private final Decomposer _decon;

  public NewtonVectorRootFinder(final double atol, final double rtol, final int maxSteps) {
    _absTol = atol;
    _relTol = rtol;
    _maxSteps = maxSteps;
   // _decon = new SingularValueDecomposition();
 _decon = new SingularValueDecompositionColt();
  }

  public NewtonVectorRootFinder(final double atol, final double rtol) {
    this(atol, rtol, MAX_STEPS);
  }

  public NewtonVectorRootFinder(final double tol, final int maxSteps) {
    this(tol, tol, maxSteps);
  }

  public NewtonVectorRootFinder(final double tol) {
    this(tol, tol);
  }

  public NewtonVectorRootFinder(final int maxSteps) {
    this(DEF_TOL, DEF_TOL, maxSteps);
  }

  public NewtonVectorRootFinder() {
    this(DEF_TOL, DEF_TOL, MAX_STEPS);
  }

  /**
  * Use this if you do NOT have access to an analytic Jacobian 
  */
  @Override
  public DoubleMatrix1D getRoot(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function,
      final DoubleMatrix1D startPosition) {
    final FirstDerivative jacobian = new FirstDerivative(function);
    return getRoot(function, jacobian, startPosition);
  }

  /**
   * Use this if you DO have access to an analytic Jacobian
   */
  @Override
  public DoubleMatrix1D getRoot(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function,
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobian, final DoubleMatrix1D startPosition) {
    
    NewtonRootFinderHelper helper = new NewtonRootFinderHelper(function, jacobian);
    return helper.getRoot(startPosition);
  }

  

 
  
  private class NewtonRootFinderHelper{
    private final Function1D<DoubleMatrix1D, DoubleMatrix1D> _function;
    private final Function1D<DoubleMatrix1D, DoubleMatrix2D> _jacobian;
    DoubleMatrix1D _y, _pos;
    private double _lambda1, _lambda2;
    private double _g0, _g1, _g2;
    private static final double LAMBDA_MIN =0.1;
   
    public NewtonRootFinderHelper(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function, final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobian){
      if(function==null||jacobian==null)
        throw new IllegalArgumentException("function or jacobian is null");
      _function=function;
      _jacobian=jacobian;
    }
    
    public DoubleMatrix1D getRoot(DoubleMatrix1D x) {
      
      _y = _function.evaluate(x);
      _g0 =  _y.dotProduct(_y);
      
      DoubleMatrix1D x1 = x;
      getNextPosition(x1);
      DoubleMatrix1D x2 = _pos;
      int count = 0;
      while (!closeEnough(x1, x2)) {
        x1 = x2;
        getNextPosition(x1);
        x2 = _pos;
        count++;
        if (count > _maxSteps)
          throw new RootNotFoundException("Failed to converge");
      }

      return x2;
    } 
    
    
    private void getNextPosition(final DoubleMatrix1D x) {      
      
      final DoubleMatrix2D h = _jacobian.evaluate(x);
      
      final DecompositionResult deconResult = _decon.evaluate(h);
      DoubleMatrix1D p = deconResult.Solve(_y);
      _pos = x.subtract(p);
      _y= _function.evaluate(_pos);
      _g1 = _y.dotProduct(_y);
      
        
      if(_g1<_g0){
        _g0=_g1;
        return;
      }
     
      _lambda1 = 1.0;
      if(Double.isInfinite(_g1))
        bisectBacktrack(x,p);
     
    
      quadraticBacktrack(x,p);
      do cubicBacktrack(x,p);
      while(_g1>_g0);
      _g0=_g1;
      
    }
    
  
    private void bisectBacktrack(final DoubleMatrix1D x, final DoubleMatrix1D p){   
    do{
       _lambda1 *=0.1;
       _pos = x.subtract(p.multiply(_lambda1));
       _y= _function.evaluate(_pos);
       _g1 = _y.dotProduct(_y);
     } while(Double.isInfinite(_g1)); 
    }
  
    
    private void quadraticBacktrack(final DoubleMatrix1D x, final DoubleMatrix1D p){
      double lambda = _g0*_lambda1*_lambda1/(_g1+_g0*(2*_lambda1-1));
      _lambda2=_lambda1;
      _lambda1 = Math.max(_lambda1*0.01, lambda);//don't make the linear guess too small
     _pos = x.subtract(p.multiply(_lambda1));
      _y= _function.evaluate(_pos);
      _g2=_g1;
      _g1= _y.dotProduct(_y);
    }
    
    private  void cubicBacktrack(final DoubleMatrix1D x, final DoubleMatrix1D p) {
   
      double temp1, temp2, temp3, temp4, temp5;
  
      temp1=1.0/_lambda1/_lambda1;
      temp2=1.0/_lambda2/_lambda2;
      temp3 = _g1+_g0*(2*_lambda1-1.0);
      temp4=  _g2+_g0*(2*_lambda2-1.0);
      temp5=1.0/(_lambda1-_lambda2);
      double a=temp5*(temp1*temp3-temp2*temp4);
      double b=temp5*(-_lambda2*temp1*temp3+_lambda1*temp2*temp4);
      
      double lambda=(-b+Math.sqrt(b*b+6*a*_g0))/3/a;
       lambda = Math.min(Math.max(lambda, 0.01*_lambda1),0.75*_lambda2);//make sure new lambda is between 1% & 50% of old value 
    
       _lambda2=_lambda1;
      _g2=_g1;
      _lambda1=lambda;
      _pos = x.subtract(p.multiply(_lambda1));
      _y= _function.evaluate(_pos);
      _g1 = _y.dotProduct(_y);
     
    }
    
    private boolean closeEnough(final DoubleMatrix1D x, final DoubleMatrix1D y) {
      
      final int n = x.getNumberOfElements();
      if (y.getNumberOfElements() != n)
        throw new IllegalArgumentException("Different length inputs");
      double diff, scale;
      for (int i = 0; i < n; i++) {
        diff = Math.abs(x.getEntry(i) - y.getEntry(i));
        scale = Math.max(Math.abs(x.getEntry(i)), Math.abs(y.getEntry(i)));
        if (diff > _absTol + scale * _relTol)
          return false;
      }
      return true;
    }
    
  }

  private class FirstDerivative extends Function1D<DoubleMatrix1D, DoubleMatrix2D> {

    private final Function1D<DoubleMatrix1D, DoubleMatrix1D> f;

    public FirstDerivative(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
      f = function;
    }

    @Override
    public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
      final double[] pos = x.toArray();
      final int m = pos.length;
      final double twoEPS = 2.0*EPS;

      pos[0] += EPS;
      DoubleMatrix1D yp = f.evaluate(new DoubleMatrix1D(pos));
      pos[0] -= twoEPS;
      DoubleMatrix1D ym = f.evaluate(new DoubleMatrix1D(pos));
      pos[0]=x.getEntry(0);
      final int n = yp.getNumberOfElements();
      final double[][] res = new double[n][m];
      for (int i = 0; i < n; i++)
        res[i][0] = (yp.getEntry(i) - ym.getEntry(i)) /twoEPS;

      for (int j = 1; j < m; j++) {
        pos[j] += EPS;
        yp = f.evaluate(new DoubleMatrix1D(pos));
        pos[j] -= twoEPS;
        ym = f.evaluate(new DoubleMatrix1D(pos));
        pos[j]=x.getEntry(j);
        for (int i = 0; i < n; i++)
          res[i][j] = (yp.getEntry(i) - ym.getEntry(i)) / twoEPS;
      }

      return new DoubleMatrix2D(res);
    }

  }

}
