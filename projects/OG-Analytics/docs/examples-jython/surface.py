import com.opengamma.math.surface as surf
dir(surf)
csurf = surf.ConstantDoublesSurface.from(0.2,"flatSurf")
print csurf
print csurf.getName()
print csurf.getZData()
print csurf.getZValue(-99.9,-99.9)
print csurf.getZValue(0,0)




