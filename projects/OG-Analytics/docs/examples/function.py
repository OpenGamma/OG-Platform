### @export "create-matrix"
import com.opengamma.math.matrix.DoubleMatrix1D as DoubleMatrix1D
X = DoubleMatrix1D(range(4))
print X

### @export "square-elements"
import com.opengamma.tutorial.ExampleFunctions as pkgFcns
Y = pkgFcns.Squares.evaluate(X)
print Y

### @export "evaluate"
XX = pkgFcns.Squares.evaluate
print XX(X)

### @export "import-differentiate"
import com.opengamma.math.differentiation as pkgDiff

### @export "compute-derivative"
ddx = pkgDiff.VectorFieldFirstOrderDifferentiator()
dYdX = ddx.differentiate(pkgFcns.Squares)
print dYdX.evaluate(X)
print dYdX.evaluate(DoubleMatrix1D(range(-1,-10,-2)))
