/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.Four) window.Four = {};
    window.Four.Text3D = function (matlib, settings) {
        var char_geometries = {};
        /**
         * Creates new or fetches cached 3D text geometry
         * @param {String} str
         * @param {Object} options text geometry options
         * @returns {THREE.Geometry}
         */
        var create_geometry = function (str, options) {
            var geometry;
            if (char_geometries[str]) return char_geometries[str];
            if (str === ' ') {
                geometry = new THREE.Geometry();
                geometry.boundingBox = {min: new THREE.Vector3(0, 0, 0), max: new THREE.Vector3(100, 0, 0)};
                return geometry;
            }
            geometry = new THREE.TextGeometry(str, options);
            geometry.computeBoundingBox();
            return char_geometries[str] = geometry;
        };
        /**
         * @param {String} str String you want to create
         * @param {String} color text colour in hex
         * @param {Boolean} preserve_kerning set to true to cache geometry without breaking it into characters
         * @param {Boolean} bevel
         * @returns {THREE.Mesh}
         */
        return function (str, color, preserve_kerning, bevel) {
            var object, merged = new THREE.Geometry(),
                material = matlib.get_material('phong', color), xpos = 0,
                options = {
                    size: settings.font_size, height: settings.font_height,
                    font: settings.font_face_3d, weight: 'normal', style: 'normal',
                    bevelEnabled: bevel || false, bevelSize: 0.6, bevelThickness: 0.6
                };
            if (preserve_kerning) return new THREE.Mesh(create_geometry(str, options), material);
            str.split('').forEach(function (val) {
                var text = create_geometry(val, options), mesh = new THREE.Mesh(text, material);
                mesh.position.x = xpos + (val === '.' ? 5 : 0);                                   // space before
                xpos = xpos + ((THREE.FontUtils.drawText(val).offset)) + (val === '.' ? 10 : 15); // space after
                THREE.GeometryUtils.merge(merged, mesh);
            });
            merged.computeFaceNormals();
            object = new THREE.Mesh(merged, material);
            object.matrixAutoUpdate = false;
            return object;
        };
    };
})();