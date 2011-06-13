print 'Start with a DoubleMatrix1D created from range(4)'
import com.opengamma.math.matrix.DoubleMatrix1D as DoubleMatrix1D
X = DoubleMatrix1D(range(4))
print X
print 'Import a static Function<DoubleMatrix1D,DoubleMatrix1D> that squares each element of X'
import com.opengamma.tutorial.ExampleFunctions as pkgFcns
Y = pkgFcns.Squares.evaluate(X)
print Y
print 'As functions are first class objects in jython, we can also do this'
XX = pkgFcns.Squares.evaluate
Y2 = XX(X)
print 'Y==Y2?  '+str(Y==Y2)+'\n'
print 'Y.equals(Y2) = '+str(Y.equals(Y2))
print 'Y.equals(X) = '+str(Y.equals(X))
print 'Finally, we import the differentiation package,'
import com.opengamma.math.differentiation as pkgDiff
print 'compute the derivative of our Squares function object'
ddx = pkgDiff.VectorFieldFirstOrderDifferentiator()
dYdX = ddx.derivative(pkgFcns.Squares)
print 'and evaluate it at [0,1,2,3]\n'
print dYdX.evaluate(X)
print 'or [-1, -3, -5, -7, -9]\n'
print dYdX.evaluate(DoubleMatrix1D(range(-1,-10,-2)))
