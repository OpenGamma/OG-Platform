/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.JSurface3D) throw new Error('JSSurface3D.SliceBar requires JSSurface3D');
    /**
     * Slice Bar constructor
     * @param {Object} matlib material library
     * @param {Object} settings settings
     * @param {String} orientation 'x' or 'z'
     * @return {THREE.Mesh}
     */
    window.JSurface3D.SliceBar = function (matlib, settings, orientation) {
        var geo = new THREE.CubeGeometry(settings['surface_' + orientation], 1, 2, 0, 0),
            mesh = new THREE.Mesh(geo, matlib.get_material('phong', settings.slice_bar_color));
        if (orientation === 'x') mesh.position.z = settings.surface_z / 2 + 1.5 + settings.axis_offset;
        if (orientation === 'z') {
            mesh.position.x = -(settings.surface_x / 2) - 1.5 - settings.axis_offset;
            mesh.rotation.y = -Math.PI * 0.5;
        }
        mesh.matrixAutoUpdate = false;
        mesh.updateMatrix();
        return mesh;
    };
})();