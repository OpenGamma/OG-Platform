print ' Very brief introduction to the root finding machinery \n'

import com.opengamma.math.function as funcs
print 'Another source of simple functions is math.functions \n'
dir(funcs)

print 'Let us create (x-5)^3 \n'
f = funcs.RealPolynomialFunction1D([-125,75,-15,1])

dir(f)
print 'We know the root..'
print 'f.evaluate(5) = ' + str(f.evaluate(5))

print 'Now, we can import the rootfinding library and test it'
import com.opengamma.math.rootfinding as roots

dir(roots)

brent = roots.BrentSingleRootFinder()
cubic = roots.CubicRealRootFinder()

dir(brent)
dir(cubic)

# As you can see, the interfaces are very similar, and defaults are provided

cuberoot = cubic.getRoots(f)
print cuberoot

 # Try this, too:  brent.getRoot(f,-1,1)
brentroot = brent.getRoot(f,-10,10)
print brentroot

