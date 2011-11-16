### @export "define-identity"
def identity(n):
    """return a 2d nxn array representing the identity matrix"""
    mtx = [[0]*n for col in range(n)]
    for row in range(n): mtx[row][row]=1
    return mtx

print identity(2)
print identity(4)

### @export "double-matrix-2d"
from com.opengamma.math.matrix import DoubleMatrix2D
m = DoubleMatrix2D(identity(4))
m

### @export "double-matrix-2d-operations"
m.getNumberOfElements()
m.getColumnVector(0)
m.getColumnVector(1)

### @export "double-matrix-1d"
from com.opengamma.math.matrix import DoubleMatrix1D
v = DoubleMatrix1D(range(1,5))
v

### @export "matrix-multiply"
from com.opengamma.math.matrix import ColtMatrixAlgebra
colt = ColtMatrixAlgebra()
colt.multiply(m, v)

### @export "create-poly"
from com.opengamma.math.function import RealPolynomialFunction1D
f = RealPolynomialFunction1D([-125,75,-15,1])
f

### @export "function-methods"
for c in dir(f):
    print c

### @export "derivative"
d = f.derivative()
d.getCoefficients()

### @export "known-root"
f.evaluate(5)

### @export "rootfinding"
import com.opengamma.math.rootfinding
for c in dir(com.opengamma.math.rootfinding):
    print c

### @export "cubic"
from com.opengamma.math.rootfinding import CubicRealRootFinder
cubic = CubicRealRootFinder()
cubic.getRoots(f)

### @export "brent"
from com.opengamma.math.rootfinding import BrentSingleRootFinder
brent = BrentSingleRootFinder()
for c in dir(brent):
    print c

brent.getRoot(f,-10,10)

### @export "brent-not-bracketing-root"
brent.getRoot(f,-1,1)
