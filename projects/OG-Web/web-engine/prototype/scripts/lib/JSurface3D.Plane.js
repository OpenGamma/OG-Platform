/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.JSurface3D) throw new Error('JSurface3D.Plane requires JSurface3D');
    /**
     * Constructor for a plane with correct x / y vertex position spacing
     * @param {String} type 'surface', 'smilex' or 'smiley'
     * @returns {THREE.PlaneGeometry}
     */
    window.JSurface3D.Plane = function (js3d, type) {
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