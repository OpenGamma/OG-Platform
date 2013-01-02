/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.JSurface3D) throw new Error('JSurface3D.Slice requires JSurface3D');
    /**
     * Slice handle constructor
     * @return {THREE.Mesh}
     */
    var Handle = function (matlib, settings) {
        var geo = new THREE.CubeGeometry(3, 1.2, 3, 2, 0, 0);
        geo.vertices // move middle vertices out to a point
            .filter(function (val) {return (val.x === 0 && val.z === -1.5);})
            .forEach(function (vertex) {vertex.z = -2.9;});
        return new THREE.Mesh(geo, matlib.get_material('phong', settings.slice_handle_color));
    };
    /**
     * Slice Bar constructor
     * @param {Object} matlib material library
     * @param {Object} settings settings
     * @param {String} orientation 'x' or 'z'
     * @return {THREE.Mesh}
     */
    var SliceBar = function (matlib, settings, orientation) {
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
    /**
     * Create slicing functionality
     * @param {Object} js3d An instance of JSurface3D
     * @name JSurface3D.Slice
     * @mamespace JSurface3D.Slice
     * @private
     * @constructor
     */
    window.JSurface3D.Slice = function (js3d) {
        var slice = this, matlib = js3d.matlib, settings = js3d.settings;
        slice.lft_x_handle_position = js3d.x_segments;
        slice.rgt_x_handle_position = 0;
        slice.lft_z_handle_position = js3d.z_segments;
        slice.rgt_z_handle_position = 0;
        slice.load = function () {
            var plane = new THREE.PlaneGeometry(5000, 5000, 0, 0), mesh;
            plane.applyMatrix(new THREE.Matrix4().makeRotationX(-Math.PI / 2));
            mesh = new THREE.Mesh(plane, matlib.get_material('wire', 0xcccccc));
            mesh.matrixAutoUpdate = false;
            mesh.updateMatrix();
            slice.intersection_plane = mesh;
            js3d.groups.surface_bottom.add(slice.intersection_plane);
            slice.x();
            slice.z();
        };
        slice.reset_handle_material = function () {
            slice.lft_x_handle.material = slice.rgt_x_handle.material = slice.lft_z_handle.material =
            slice.rgt_z_handle.material = matlib.get_material('phong', settings.slice_handle_color);
        };
        slice.create_slice_bar = function (axis) {
            var vertices, bar_lbl = axis + '_bar',
                lx = slice['lft_' + axis + '_handle'].position[axis],
                rx = slice['rgt_' + axis + '_handle'].position[axis];
            if (js3d.buffers.slice) js3d.buffers.slice.clear(slice[bar_lbl]);
            js3d.groups.slice.remove(slice[bar_lbl]);
            slice[bar_lbl] = new SliceBar(matlib, settings, axis);
            vertices = slice[bar_lbl].geometry.vertices;
            vertices[0].x = vertices[1].x = vertices[2].x = vertices[3].x = Math.max.apply(null, [lx, rx]);
            vertices[4].x = vertices[5].x = vertices[6].x = vertices[7].x = Math.min.apply(null, [lx, rx]);
            if (!(Math.abs(lx) + Math.abs(rx) === settings['surface_' + axis])) // change color
                slice[bar_lbl].material = matlib.get_material('phong', settings.slice_bar_color_active);
            js3d.groups.slice.add(js3d.buffers.slice.add(slice[bar_lbl]));
        };
        slice.x = function () {
            var xpos = (settings.surface_x / 2), zpos = settings.surface_z / 2 + 1.5 + settings.axis_offset;
            /**
             * particle guide
             * (dotted lines that guide the slice handles)
             */
            (function () {
                var geo = new THREE.Geometry(), num_vertices = js3d.adjusted_xs.length, xparticles;
                while (num_vertices--) geo.vertices.push(new THREE.Vector3(js3d.adjusted_xs[num_vertices], 0, 0));
                xparticles = new THREE.ParticleSystem(geo, matlib.get_material('particles'));
                xparticles.position.x += 0.1;
                xparticles.position.z = zpos;
                js3d.groups.surface_bottom.add(slice.x_particles = xparticles);
            }());
            /**
             * handles
             */
            if (!slice.lft_x_handle) slice.lft_x_handle = new Handle(matlib, settings);
            slice.lft_x_handle.position.x = -xpos;
            slice.lft_x_handle.position.z = zpos;
            if (!slice.rgt_x_handle) slice.rgt_x_handle = new Handle(matlib, settings);
            slice.rgt_x_handle.position.x = xpos;
            slice.rgt_x_handle.position.z = zpos;
            js3d.groups.surface_bottom.add(slice.lft_x_handle);
            js3d.groups.surface_bottom.add(slice.rgt_x_handle);
            js3d.interactive_meshes.add('lft_x_handle', slice.lft_x_handle);
            js3d.interactive_meshes.add('rgt_x_handle', slice.rgt_x_handle);
            slice.lft_x_handle.position.x = slice.x_particles.geometry.vertices[slice.lft_x_handle_position].x;
            slice.rgt_x_handle.position.x = slice.x_particles.geometry.vertices[slice.rgt_x_handle_position].x;
            /**
             * slice bar
             */
            slice.create_slice_bar('x');
        };
        slice.z = function () {
            var xpos = (settings.surface_x / 2) + 1.5 + settings.axis_offset, zpos = settings.surface_z / 2;
            /**
             * particle guide
             * (dotted lines that guide the slice handles)
             */
            (function () {
                var geo = new THREE.Geometry(), num_vertices = js3d.adjusted_zs.length, zparticles;
                while (num_vertices--) geo.vertices.push(new THREE.Vector3(js3d.adjusted_zs[num_vertices], 0, 0));
                zparticles = new THREE.ParticleSystem(geo, matlib.get_material('particles'));
                zparticles.rotation.y = -Math.PI * 0.5;
                zparticles.position.x = -xpos;
                js3d.groups.surface_bottom.add(slice.z_particles = zparticles);
            }());
            /**
             * handles
             */
            if (!slice.lft_z_handle) slice.lft_z_handle = new Handle(matlib, settings);
            slice.lft_z_handle.position.x = -xpos;
            slice.lft_z_handle.position.z = -zpos;
            slice.lft_z_handle.rotation.y = -Math.PI * 0.5;
            if (!slice.rgt_z_handle) slice.rgt_z_handle = new Handle(matlib, settings);
            slice.rgt_z_handle.position.x = -xpos;
            slice.rgt_z_handle.position.z = zpos;
            slice.rgt_z_handle.rotation.y = -Math.PI * 0.5;
            js3d.groups.surface_bottom.add(slice.lft_z_handle);
            js3d.groups.surface_bottom.add(slice.rgt_z_handle);
            js3d.interactive_meshes.add('lft_z_handle', slice.lft_z_handle);
            js3d.interactive_meshes.add('rgt_z_handle', slice.rgt_z_handle);
            slice.lft_z_handle.position.z = slice.z_particles.geometry.vertices[slice.lft_z_handle_position].x;
            slice.rgt_z_handle.position.z = slice.z_particles.geometry.vertices[slice.rgt_z_handle_position].x;
            /**
             * slice bar
             */
            slice.create_slice_bar('z');
        };
    };
})();