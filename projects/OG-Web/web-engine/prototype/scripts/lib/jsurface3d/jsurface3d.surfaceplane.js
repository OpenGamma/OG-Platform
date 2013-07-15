/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.JSurface3D) throw new Error('JSurface3D.SurfacePlane requires JSurface3D');
    /**
     * Creates a surface plane with extrutions and vertex shading.
     * Clips plane if required
     * Adds update method
     * @name JSurface3D.SurfacePlane
     * @namespace JSurface3D.SurfacePlane
     * @param {Object} js3d JSurface3D instance
     * @private
     * @returns {Function} {THREE.Object3D} A surface plane object
     * @constructor
     */
    window.JSurface3D.SurfacePlane = function (js3d) {
        var surfaceplane = new THREE.Object3D(), wiremesh, planemesh;
        surfaceplane.name = 'SurfacePlane';
        surfaceplane.init = function () {
            var settings = js3d.settings, matlib = js3d.matlib, plane = JSurface3D.plane(js3d, 'surface'), i, wire;
            plane.verticesNeedUpdate = true;
            for (i = 0; i < js3d.adjusted_vol.length; i++) {plane.vertices[i].y = js3d.adjusted_vol[i];} // extrude
            wire = plane.clone();
            plane.computeCentroids();
            plane.computeFaceNormals();
            plane.computeBoundingSphere();
            (function () { // apply heatmap
                if (!Detector.webgl) return;
                var faces = 'abcd', face, color, vertex, index, i, k,
                    min = Math.min.apply(null, js3d.adjusted_vol), max = Math.max.apply(null, js3d.adjusted_vol),
                    hue_min = settings.vertex_shading_hue_min, hue_max = settings.vertex_shading_hue_max, hue;
                for (i = 0; i < plane.faces.length; i ++) {
                    face = plane.faces[i];
                    for (k = 0; k < 4; k++) {
                        index = face[faces.charAt(k)];
                        vertex = plane.vertices[index];
                        color = new THREE.Color(0xffffff);
                        hue = ~~((vertex.y - min) / (max - min) * (hue_max - hue_min) + hue_min) / 360;
                        color.setHSV(hue, 0.9, 1);
                        face.vertexColors[k] = color;
                    }
                }
            }());
            /* apply surface materials */
            (function () { // clip/slice
                var row, i, x, l,
                    zlft = Math.abs(js3d.slice.lft_z_handle_position - js3d.z_segments) * js3d.x_segments,
                    zrgt = Math.abs(js3d.slice.rgt_z_handle_position - js3d.z_segments) * js3d.x_segments,
                    xlft = Math.abs(js3d.slice.lft_x_handle_position - js3d.x_segments),
                    xrgt = Math.abs(js3d.slice.rgt_x_handle_position - js3d.x_segments),
                    zmin = Math.min.apply(null, [zlft, zrgt]),
                    zmax = Math.max.apply(null, [zlft, zrgt]),
                    xmin = Math.min.apply(null, [xlft, xrgt]),
                    xmax = Math.max.apply(null, [xlft, xrgt]);
                for (i = 0, x = 0, row = 0, l = plane.faces.length; i < l; i++, x++) {
                    var plane_face = plane.faces[i], wire_face = wire.faces[i];
                    if (x === js3d.x_segments) x = 0, row++;
                    if (
                        (i < zmax) && (i > zmin - 1) && // z slice
                        (i > xmin + row * js3d.x_segments -1) && (i < xmax + row * js3d.x_segments)  // x slice
                    ) plane_face.materialIndex = wire_face.materialIndex = 1;
                    else plane_face.materialIndex = wire_face.materialIndex = 0;
                }
            }());
            wiremesh = new THREE.Mesh(wire, new THREE.MeshFaceMaterial(matlib.get_material('compound_surface_wire')));
            wiremesh.position.y = 0.01; // move wiremesh to account for Z-fighting
            planemesh = new THREE.Mesh(plane, new THREE.MeshFaceMaterial(matlib.get_material('compound_surface')));
            surfaceplane.add(planemesh);
            surfaceplane.add(wiremesh);
            surfaceplane.position.y = settings.floating_height;
            surfaceplane.matrixAutoUpdate = false;
            surfaceplane.updateMatrix();
            surfaceplane.children.forEach(function (mesh) {
                mesh.doubleSided = true;
                mesh.matrixAutoUpdate = false;
            });
            js3d.interactive_meshes.add('surface', surfaceplane.children[0]);
            if (js3d.buffers.surface) js3d.buffers.surface.add(planemesh);
            if (js3d.buffers.surface) js3d.buffers.surface.add(wiremesh);
        };
        surfaceplane.update = function () {
            if (js3d.buffers.surface) js3d.buffers.surface.clear(planemesh);
            if (js3d.buffers.surface) js3d.buffers.surface.clear(wiremesh);
            surfaceplane.remove(wiremesh);
            surfaceplane.remove(planemesh);
            surfaceplane.init(js3d);
        };
        return surfaceplane
    };
})();