/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.JSurface3D) throw new Error('JSurface3D.Plane requires JSurface3D');
    /**
     * Creates a plane with the correct segments and spacing
     * @name JSurface3D.plane
     * @namespace JSurface3D.plane
     * @param {Object} js3d A JSurface3D instance
     * @param {String} type The type of plane to create. 'surface', 'smilex' or 'smiley'
     * @function
     * @private
     * @returns {THREE.PlaneGeometry}
     */
    window.JSurface3D.plane = function (js3d, type) {
        var xlen, ylen, xseg, yseg, xoff, yoff, plane, vertex, len, i, k, settings = js3d.settings;
        if (type === 'surface') {
            xlen = settings.surface_x;
            ylen = settings.surface_z;
            xseg = js3d.x_segments;
            yseg = js3d.z_segments;
            xoff = js3d.adjusted_xs;
            yoff = js3d.adjusted_zs;
        }
        if (type === 'smilex') {
            xlen = settings.surface_x;
            ylen = settings.surface_y;
            xseg = js3d.x_segments;
            yseg = js3d.y_segments;
            xoff = js3d.adjusted_xs;
            yoff = js3d.adjusted_ys;
        }
        if (type === 'smiley') {
            xlen = settings.surface_y;
            ylen = settings.surface_z;
            xseg = js3d.y_segments;
            yseg = js3d.z_segments;
            xoff = js3d.adjusted_ys;
            yoff = js3d.adjusted_zs;
        }
        plane = new THREE.PlaneGeometry(xlen, ylen, xseg, yseg);
        plane.applyMatrix(new THREE.Matrix4().makeRotationX(-Math.PI / 2));
        len = (xseg + 1) * (yseg + 1);
        for (i = 0, k = 0; i < len; i++, k++) {
            vertex = plane.vertices[i];
            if (typeof xoff[k] === 'undefined') k = 0;
            vertex.x = xoff[k];
            vertex.z = yoff[Math.floor(i / xoff.length)];
        }
        return plane;
    };
})();