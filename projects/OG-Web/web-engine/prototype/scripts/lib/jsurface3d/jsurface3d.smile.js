/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.JSurface3D) throw new Error('JSurface3D.Smile requires JSurface3D');
    /**
     * Creates new smile planes
     * @name JSurface3D.smile
     * @namespace JSurface3D.smile
     * @param {Object} js3d JSurface3D instance
     * @function
     * @private
     * @returns {THREE.Object3D}
     */
    window.JSurface3D.smile = function (js3d) {
        var settings = js3d.settings, matlib = js3d.matlib, smile = new THREE.Object3D();
        var x = function () {
            var obj = new THREE.Object3D();
            (function () { // plane
                var plane = JSurface3D.plane(js3d, 'smilex'),
                    material = matlib.get_material('compound_grid_wire'),
                    mesh = Four.multimaterial_object(plane, material);
                mesh.rotation.x = Math.PI * 0.5;
                mesh.position.y = settings.surface_y;
                mesh.position.z = -((settings.surface_z / 2) + settings.smile_distance);
                mesh.matrixAutoUpdate = false;
                mesh.updateMatrix();
                obj.add(mesh);
            }());
            (function () { // axis
                var y_axis = JSurface3D.Axis({axis: 'y', right: true}, js3d);
                y_axis.position.x = -(settings.surface_x / 2) - 25;
                y_axis.position.y = 4;
                y_axis.position.z = -(settings.surface_z / 2) - settings.smile_distance;
                y_axis.rotation.y = Math.PI * 0.5;
                y_axis.rotation.z = Math.PI * 0.5;
                obj.add(y_axis);
            }());
            return obj;
        };
        var z = function () {
            var obj = new THREE.Object3D();
            (function () { // plane
                var plane = JSurface3D.plane(js3d, 'smiley'),
                    material = matlib.get_material('compound_grid_wire'),
                    mesh = Four.multimaterial_object(plane, material);
                mesh.position.x = (settings.surface_x / 2) + settings.smile_distance;
                mesh.rotation.z = Math.PI * 0.5;
                mesh.matrixAutoUpdate = false;
                mesh.updateMatrix();
                obj.add(mesh);
            }());
            (function () { // axis
                var y_axis = JSurface3D.Axis({axis: 'y'}, js3d);
                y_axis.position.y = 4;
                y_axis.position.z = (settings.surface_z / 2) + 5;
                y_axis.position.x = (settings.surface_x / 2) + settings.smile_distance;
                y_axis.rotation.z = Math.PI * 0.5;
                obj.add(y_axis);
            }());
            return obj;
        };
        var shadows = function () {
            var obj = new THREE.Object3D();
            (function () { // x shadow
                var z = settings.surface_z / 2 + settings.smile_distance, half_width = settings.surface_x / 2,
                    points = [{x: -half_width, y: 0, z: -z}, {x: half_width, y: 0, z: -z}],
                    shadow = new Four.Tube(matlib, points, 0xaaaaaa);
                shadow.matrixAutoUpdate = false;
                obj.add(shadow);
            }());
            (function () { // z shadow
                var x = settings.surface_x / 2 + settings.smile_distance, half_width = settings.surface_z / 2,
                    points = [{x: x, y: 0, z: -half_width}, {x: x, y: 0, z: half_width}],
                    shadow = new Four.Tube(matlib, points, 0xaaaaaa);
                shadow.matrixAutoUpdate = false;
                obj.add(shadow);
            }());
            obj.position.y -= settings.floating_height;
            return obj;
        };
        smile.add(x());
        smile.add(z());
        smile.add(shadows());
        return smile;
    };
})();