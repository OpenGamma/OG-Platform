### @export "classpath"
import java
print java.lang.System.getProperties()['java.class.path']

### @export "import"
from com.opengamma.math.number import ComplexNumber

### @export "define"
cn = ComplexNumber(2, 4)

### @export "get-real"
print cn.getReal()

### @export "get-imaginary"
cn.getImaginary()

### @export "to-string"
cn.toString()

