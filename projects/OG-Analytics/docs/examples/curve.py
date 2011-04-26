### @export "import"
from com.opengamma.math.curve import NodalDoublesCurve

### @export "init-random-name"
curve = NodalDoublesCurve.from([1.0,2.0,3.0], [4.0, 5.0, 6.0])

print curve.name
print curve.getXData()
print curve.getYData()
