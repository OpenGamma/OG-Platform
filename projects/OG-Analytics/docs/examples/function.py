print '\nStart with a DoubleMatrix1D created from range(4)'
import com.opengamma.math.matrix.DoubleMatrix1D as DoubleMatrix1D
X = DoubleMatrix1D(range(4))
print X

print 'Import a static Function<DoubleMatrix1D,DoubleMatrix1D> that squares each element of X'
import com.opengamma.tutorial.ExampleFunctions as pkgFcns
print 'Y = pkgFcns.Squares.evaluate(X)'
Y = pkgFcns.Squares.evaluate(X)
print Y

print 'As functions are first class objects in jython, we can also do this'
print 'XX = pkgFcns.Squares.evaluate'
XX = pkgFcns.Squares.evaluate
print 'XX(X)'
print XX(X)

print 'Finally, we import the differentiation package'
print 'import com.opengamma.math.differentiation as pkgDiff\n'
import com.opengamma.math.differentiation as pkgDiff

print 'compute the derivative of our Squares function object'
print 'ddx = pkgDiff.VectorFieldFirstOrderDifferentiator()'
ddx = pkgDiff.VectorFieldFirstOrderDifferentiator()
print 'dYdX = ddx.differentiate(pkgFcns.Squares)\n'
dYdX = ddx.differentiate(pkgFcns.Squares)
print 'and evaluate it at [0,1,2,3]\n'
print dYdX.evaluate(X)
print 'or at [-1, -3, -5, -7, -9]\n'
print dYdX.evaluate(DoubleMatrix1D(range(-1,-10,-2)))
