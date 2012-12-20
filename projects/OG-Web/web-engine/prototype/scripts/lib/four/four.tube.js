/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.Four) window.Four = {};
    /**
     * Constructor for a tube.
     * THREE doesnt currently support creating a tube with a line as a path
     * (Spline is supported, but we dont want that), so we create separate tubes and add them to an object.
     * Also linewidth doest seem to work for a LineBasicMaterial, thus using tube
     * @param {Array} points Array of Vector3's
     * @param {String} color hex colour
     * @return {THREE.Object3D}
     */
    window.Four.Tube = function (matlib, points, color) {
        var group, line, tube, i = points.length - 1,
            merged = new THREE.Geometry(), material = matlib.get_material('flat', color);
        while (i--) {
            line = new THREE.LineCurve3(points[i], points[i+1]);
            tube = new THREE.TubeGeometry(line, 1, 0.2, 4, false, false);
            THREE.GeometryUtils.merge(merged, tube);
            merged.computeFaceNormals();
            merged.computeBoundingSphere();
            group = new THREE.Mesh(merged, material);
            group.matrixAutoUpdate = false;
        }
        return group;
    };
})();