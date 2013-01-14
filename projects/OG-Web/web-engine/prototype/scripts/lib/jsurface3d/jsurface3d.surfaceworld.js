/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.JSurface3D) throw new Error('JSurface3D.SurfaceWorld requires JSurface3D');
    var last_vertex;
    /**
     * Implements any valid hover interaction with the surface.
     * Adds vertex_sphere, lines (tubes), active axis labels
     * @private
     * @param {Object} js3d An instance of JSurface3D
     * @param {THREE.Vector3} vertex The vertex within settings.snap_distance of the mouse
     * @param {Number} index The index of the vertex
     * @param {THREE.Mesh} object The closest object to the camera that THREE.Raycaster returned
     */
    var hover = function (js3d, vertex, index, object) {
        var hover_buffer = js3d.buffers.hover, hover_group = js3d.groups.hover, matlib = js3d.matlib,
            vertex_sphere = js3d.vertex_sphere, settings = js3d.settings;
        if (vertex === last_vertex) return;
        if (hover_group) js3d.groups.surface_top.remove(hover_group), hover_buffer.clear();
        last_vertex = vertex;
        hover_group = hover_buffer.add(new THREE.Object3D());
        hover_group.name = 'Hover Group';
        vertex_sphere.position.copy(vertex);
        vertex_sphere.position.y += js3d.settings.floating_height;
        vertex_sphere.updateMatrix();
        vertex_sphere.visible = true;
        (function () {
            // [xz]from & [xz]to are the start and end vertex indexes for a vertex row or column
            var x, xvertices = [], xvertices_bottom = [], z, zvertices = [], zvertices_bottom = [],
                color = settings.interactive_surface_color,
                xfrom = index - (index % (js3d.x_segments + 1)),
                xto   = xfrom + js3d.x_segments + 1,
                zfrom = index % (js3d.x_segments + 1),
                zto   = ((js3d.x_segments + 1) * (js3d.z_segments + 1)) - (js3d.x_segments - zfrom);
            for (x = xfrom; x < xto; x++) xvertices.push(object.geometry.vertices[x]);
            for (z = zfrom; z < zto; z += js3d.x_segments + 1) zvertices.push(object.geometry.vertices[z]);
            js3d.hud.set_volatility(js3d.data.vol[index]);
            // surface lines
            (function () {
                var xlft = js3d.x_segments - js3d.slice.lft_x_handle_position,
                    xrgt = js3d.x_segments - js3d.slice.rgt_x_handle_position,
                    zlft = js3d.z_segments - js3d.slice.lft_z_handle_position,
                    zrgt = js3d.z_segments - js3d.slice.rgt_z_handle_position,
                    zmin = Math.min.apply(null, [zlft, zrgt]),
                    zmax = Math.max.apply(null, [zlft, zrgt]),
                    xmin = Math.min.apply(null, [xlft, xrgt]),
                    xmax = Math.max.apply(null, [xlft, xrgt]),
                    x_surface_lines = new Four.Tube(matlib, xvertices.slice(xmin, xmax + 1), color),
                    z_surface_lines = new Four.Tube(matlib, zvertices.slice(zmin, zmax + 1), color);
                x_surface_lines.name = 'X Surface Lines';
                z_surface_lines.name = 'Z Surface Lines';
                hover_group.add(x_surface_lines);
                hover_group.add(z_surface_lines);
            }());
            // smile z, z and y lines
            (function () {
                var yvertices = [{x: 0, y: 0, z: vertex.z}, {x: 0, y: settings.surface_y, z: vertex.z}],
                    zlines = new Four.Tube(matlib, zvertices, color), ylines = new Four.Tube(matlib, yvertices, color);
                zlines.name = 'Smile Z, Z Hover Lines';
                ylines.name = 'Smile Z, Y Hover Lines';
                zlines.position.x = Math.abs(zvertices[0].x - settings.surface_x / 2) + settings.smile_distance;
                ylines.position.x = settings.surface_x / 2 + settings.smile_distance;
                zlines.matrixAutoUpdate = false;
                ylines.matrixAutoUpdate = false;
                zlines.updateMatrix();
                ylines.updateMatrix();
                hover_group.add(zlines);
                hover_group.add(ylines);
            }());
            // smile x, x and y lines
            (function () {
                var yvertices = [{x: vertex.x, y: 0, z: 0}, {x: vertex.x, y: settings.surface_y, z: 0}],
                    xlines = new Four.Tube(matlib, xvertices, color), ylines = new Four.Tube(matlib, yvertices, color);
                xlines.name = 'Smile X, X Hover Lines';
                ylines.name = 'Smile X, Y Hover Lines';
                xlines.position.z = -((settings.surface_z / 2) + xvertices[0].z + settings.smile_distance);
                ylines.position.z = -(settings.surface_z / 2) - settings.smile_distance;
                xlines.matrixAutoUpdate = false;
                ylines.matrixAutoUpdate = false;
                xlines.updateMatrix();
                ylines.updateMatrix();
                hover_group.add(xlines);
                hover_group.add(ylines);
            }());
            // bottom grid, x and z lines
            (function () {
                var xlines, zlines;
                xvertices_bottom.push(xvertices[0].clone(), xvertices[xvertices.length-1].clone());
                xvertices_bottom[0].y = xvertices_bottom[1].y = -settings.floating_height;
                zvertices_bottom.push(zvertices[0].clone(), zvertices[zvertices.length-1].clone());
                zvertices_bottom[0].y = zvertices_bottom[1].y = -settings.floating_height;
                xlines = new Four.Tube(matlib, xvertices_bottom, color);
                zlines = new Four.Tube(matlib, zvertices_bottom, color);
                xlines.name = 'Bottom Grid X Hover lines';
                zlines.name = 'Bottom Grid Z Hover lines';
                xlines.matrixAutoUpdate = false;
                zlines.matrixAutoUpdate = false;
                hover_group.add(xlines);
                hover_group.add(zlines);
            }());
            // surface labels
            ['x', 'z'].forEach(function (val) {
                var data = js3d.data, lbl_arr = data[val + 's_labels'] || data[val + 's'], width, offset, lbl, vertices,
                    txt = val === 'x'
                        ? lbl_arr[index % (js3d.x_segments + 1)]
                        : lbl_arr[~~(index / (js3d.x_segments + 1))],
                    scale = '0.1', group = new THREE.Object3D();
                group.name = 'Surface Label ' + val;
                // create label
                lbl = js3d.text3d(txt, color);
                lbl.matrixAutoUpdate = false;
                width = lbl.geometry.boundingSphere.radius / 2;
                offset = ((width / 2) * scale) + (width * 0.05); // half the width * scale + a relative offset
                vertices = val === 'x' ? zvertices : xvertices;
                group.add(lbl);
                // create box
                (function () {
                    var height = 60,
                        box_width = (width * 2) + 20,
                        box = new THREE.CubeGeometry(box_width, height, 4, 4, 0, 0),
                        mesh = new THREE.Mesh(box, matlib.get_material('phong', 0xdddddd));
                    mesh.position.x = box_width / 2 - 10;
                    mesh.position.y = 20;
                    // create the tail by moving the 2 center vertices closes to the surface
                    mesh.geometry.vertices.filter(function (val) {
                        return (val.x === 0 && val.y === height / 2);
                    }).forEach(function (vertex) {vertex.y = height;});
                    mesh.matrixAutoUpdate = false;
                    mesh.updateMatrix();
                    group.add(mesh);
                }());
                // position / rotation
                group.scale.set(scale, scale, scale);
                group.position.y = -settings.floating_height + 0.5;
                group.rotation.x = -Math.PI * 0.5;
                if (val === 'x') {
                    group.position.x = vertices[0][val] - offset;
                    group.position.z = (settings.surface_z / 2) + 12 + settings.axis_offset;
                    group.position.z = (settings.surface_z / 2) + 12 + settings.axis_offset;
                }
                if (val === 'z') {
                    group.position.x = -((settings.surface_x / 2) + 12) - settings.axis_offset;
                    group.position.z = vertices[0][val] - offset;
                    group.rotation.z = -Math.PI * 0.5;
                }
                group.matrixAutoUpdate = false;
                group.updateMatrix();
                hover_group.matrixAutoUpdate = false;
                hover_group.updateMatrix();
                hover_group.add(group);
            });
        }());
        js3d.groups.surface_top.add(js3d.groups.hover = hover_group);
    };
    /**
     * Test if the cursor is over a mesh
     * @event {Object} mouse event object
     * @meshes {Array} meshes array of meshes to test
     * @return {Object} THREE.Raycaster intersects object
     */
    var intersects_mesh = function (js3d, event, meshes) {
        var mouse = {x: 0, y: 0}, vector, ray;
        mouse.x = ((event.pageX - js3d.sel_offset.left) / js3d.width) * 2 - 1;
        mouse.y = -((event.pageY - js3d.sel_offset.top) / js3d.height) * 2 + 1;
        vector = new THREE.Vector3(mouse.x, mouse.y, 0.5);
        js3d.projector.unprojectVector(vector, js3d.camera);
        ray = new THREE.Raycaster(js3d.camera.position, vector.subSelf(js3d.camera.position).normalize());
        return ray.intersectObjects(meshes);
    };
    /**
     * Creates all geomerty, initializes data and implements interactions
     * @name JSurface3D.SurfaceWorld
     * @namespace JSurface3D.SurfaceWorld
     * @param {Object} js3d JSurface3D instance
     * @private
     * @constructor
     */
    window.JSurface3D.SurfaceWorld = function (js3d) {
        var surface_world = this, settings = js3d.settings, matlib = js3d.matlib, webgl = js3d.webgl;
        /**
         * Creates all the geomerty and groups, removes existing items first
         * @name JSurface3D.SurfaceWorld.prototype.create_world
         * @function
         * @private
         * @return {THREE.Object3D}
         */
        surface_world.create_world = function () {
            var groups = js3d.groups, buffers = js3d.buffers;
            if (groups.surface) groups.animation.remove(groups.surface), buffers.surface.clear();
            js3d.interactive_meshes.remove('surface');
            groups.surface = new THREE.Object3D();
            groups.surface.name = 'Surface Group';
            groups.slice = buffers.slice.add(new THREE.Object3D());
            groups.slice.name = 'Slice Group';
            groups.surface_top = new THREE.Object3D();
            groups.surface_bottom = new THREE.Object3D();
            if (typeof js3d.surface_plane === 'object') js3d.surface_plane.update();
            else js3d.surface_plane.init();
            groups.slice.add(js3d.surface_plane);
            if (webgl) {
                // create bottom grid
                (function () {
                    var grid = Four.multimaterial_object(
                        JSurface3D.plane(js3d, 'surface'),
                        matlib.get_material('compound_grid_wire')
                    );
                    grid.overdraw = true;
                    grid.matrixAutoUpdate = false;
                    groups.surface_bottom.add(buffers.surface.add(grid));
                })();
                // create floor
                (function () {
                    var floor = Four.multimaterial_object(
                        new THREE.PlaneGeometry(5000, 5000, 100, 100),
                        matlib.get_material('compound_floor_wire')
                    );
                    floor.rotation.x = - Math.PI / 2;
                    floor.position.y = -0.01;
                    groups.surface_bottom.add(buffers.surface.add(floor));
                })();
                // create axes
                (function () {
                    var x_axis = JSurface3D.Axis({axis: 'x'}, js3d),
                        z_axis = JSurface3D.Axis({axis: 'z'}, js3d);
                    x_axis.position.z = settings.surface_z / 2 + settings.axis_offset;
                    z_axis.position.x = -settings.surface_x / 2 - settings.axis_offset;
                    z_axis.rotation.y = -Math.PI * 0.5;
                    groups.surface_bottom.add(x_axis);
                    groups.surface_bottom.add(z_axis);
                })();
                groups.surface_top.add(JSurface3D.smile(js3d));
                groups.animation.add(js3d.vertex_sphere);
                groups.surface_top.position.y = js3d.settings.floating_height;
                groups.surface.add(groups.surface_bottom);
            }
            groups.surface.add(buffers.surface.add(groups.surface_top));
            groups.surface.add(groups.slice);
            return groups.surface;
        };
        /**
         * Scale data to fit surface dimensions, apply Log (to x and z) if enabled
         * @name JSurface3D.SurfaceWorld.prototype.init_data
         * @param {Object} data
         * @function
         * @private
         */
        surface_world.init_data = function (data) {
            var log = function (arr) {return arr.map(function (val) {return Math.log(val);});};
            // adjusted data is the original data scaled to fit 2D grids width/length/height.
            // It is used to set the distance between plane segments. Then the real data is used as the values
            js3d.adjusted_vol = Four.scale(data.vol, 0, settings.surface_y);
            js3d.adjusted_xs = Four.scale(
                js3d.settings.log ? log(data.xs) : data.xs,
                -(settings.surface_x / 2), settings.surface_x / 2
            );
            js3d.adjusted_zs = Four.scale(
                js3d.settings.log ? log(data.zs) : data.zs,
                -(settings.surface_z / 2), settings.surface_z / 2
            );
            // data.ys doesnt exist, so we need to create js3d.adjusted_ys manualy out of the given
            // vol plane range: 0 - settings.surface_y
            js3d.adjusted_ys = (function () {
                var increment = settings.surface_y / js3d.y_segments, arr = [], i;
                for (i = 0; i < js3d.y_segments + 1; i++) arr.push(i * increment);
                return arr;
            }());
            // create data.ys out of js3d.vol_min and js3d.vol_max, call it ys not to confuse with config paramater
            // we are not using ys to create js3d.adjusted_ys as we want more control over js3d.adjusted_ys
            // than a standard Four.scale
            js3d.ys = (function () {
                var increment = (js3d.vol_max - js3d.vol_min) / js3d.y_segments, arr = [], i;
                for (i = 0; i < js3d.y_segments + 1; i++)
                    arr.push((+js3d.vol_min + (i * increment)).toFixed(settings.precision_lbl));
                return arr;
            }());
        };
        /**
         * Implement interactivity
         * @name JSurface3D.SurfaceWorld.prototype.interactive
         * @function
         * @private
         */
        surface_world.interactive = function () {
            var imeshes = js3d.interactive_meshes.meshes, groups = js3d.groups,
                $selector = js3d.selector, surface_hittest, handle_hittest, // store the return of successful raycasts
                mousedown = false, sx = 0, sy = 0, mouse_x = null, mouse_y = null,
                hit_handle = false, rotation_enabled = true, slice_enabled = false;
            /**
             * Populate surface_hittest and handle_hittest
             * Trigger rotate_world and slice_handle_drag events
             */
            $selector.on('mousemove.surface.interactive', function (event) {
                event.preventDefault();
                js3d.local_settings.play = true;
                js3d.local_settings.stopping = false;
                var xlft = intersects_mesh(js3d, event, [imeshes.lft_x_handle]),
                    xrgt = intersects_mesh(js3d, event, [imeshes.rgt_x_handle]),
                    zlft = intersects_mesh(js3d, event, [imeshes.lft_z_handle]),
                    zrgt = intersects_mesh(js3d, event, [imeshes.rgt_z_handle]);
                /**
                 * slice handle interactions
                 */
                hit_handle = false;
                if (xlft.length) handle_hittest = xlft, handle_hittest[0].lbl = 'lft_x_handle', hit_handle = true;
                if (xrgt.length) handle_hittest = xrgt, handle_hittest[0].lbl = 'rgt_x_handle', hit_handle = true;
                if (zlft.length) handle_hittest = zlft, handle_hittest[0].lbl = 'lft_z_handle', hit_handle = true;
                if (zrgt.length) handle_hittest = zrgt, handle_hittest[0].lbl = 'rgt_z_handle', hit_handle = true;
                hit_handle ? $selector.trigger('handle_over') : $selector.trigger('handle_out');
                /**
                 * surface interaction
                 */
                surface_hittest = intersects_mesh(js3d, event, [imeshes.surface]);
                if (surface_hittest.length > 0 && surface_hittest[0].face.materialIndex === 1)
                    $selector.trigger('surface_over', surface_hittest);
                else $selector.trigger('surface_out');
                /**
                 * original mouse x & y
                 */
                mouse_x = event.pageX;
                mouse_y = event.pageY;
                /**
                 * Trigger custom events
                 */
                if (mousedown && rotation_enabled) $selector.trigger('rotate_world', event);
                if (webgl && mousedown && slice_enabled) $selector.trigger('slice_handle_drag', event);
            });
            $selector.on('rotate_world', function () {
                var dx = mouse_x - sx, dy = mouse_y - sy;
                groups.animation.rotation.y += dx * 0.01;
                groups.animation.rotation.x += dy * 0.01;
                sx += dx, sy += dy;
            });
            $selector.on('mousedown.surface.interactive', function (event) {
                event.preventDefault();
                mousedown = true, sx = event.pageX, sy = event.pageY;
                if (hit_handle) $selector.trigger('slice_handle_click');
                $(document).on('mouseup.surface.interactive', function () {
                    rotation_enabled = true;
                    slice_enabled = false;
                    mousedown = false;
                    $(document).off('mouse.surface.interactive');
                });
            });
            $selector.on('mousewheel.surface.interactive', function (event, direction) {
                if (!direction) return; // jquery mousewheel plugin doesnt exist
                var camera = js3d.camera, pos = camera.position.z - (direction * settings.zoom_sensitivity);
                camera.position.z = pos < 1 ? 1 : pos;
                camera.lookAt({x: 0, y: 0, z: 0});
            });
            /**
             * Only implement hover interactivity for webgl browsers
             */
            if (!webgl) return;
            $selector.on('surface_over', function (event, intersects) {
                var faces = 'abcd', i = 0, index, vertex, vertex_world_position,
                    intersected_obj = $.isArray(intersects) ? intersects[0] : intersects,
                    object = intersected_obj.object, point = intersected_obj.point;
                for (; i < 4; i++) { // loop through vertices
                    index = intersected_obj.face[faces.charAt(i)];
                    vertex = object.geometry.vertices[index];
                    vertex_world_position = object.matrixWorld.multiplyVector3(vertex.clone());
                    if (vertex_world_position.distanceTo(point) < settings.snap_distance) {
                        hover(js3d, vertex, index, object);
                    }
                }
            });
            $selector.on('surface_out', function () {
                if (groups.hover) {
                    groups.surface_top.remove(groups.hover);
                    last_vertex = null;
                    js3d.buffers.hover.clear();
                }
                js3d.vertex_sphere.visible = false;
                js3d.hud.set_volatility();
            });
            $selector.on('handle_over', function () {
                js3d.slice.reset_handle_material();
                handle_hittest[0].object.material = matlib.get_material('phong', settings.slice_handle_color_hover);
                $selector.css({cursor: 'pointer'});
            });
            $selector.on('handle_out', function () {
                js3d.slice.reset_handle_material();
                $selector.css({cursor: 'default'});
            });
            $selector.on('slice_handle_click', function () {
                slice_enabled = true;
                rotation_enabled = false;
            });
            /**
             * Move the drag handles and update the bar
             */
            $selector.on('slice_handle_drag', function (event, original_event) {
                var handle_lbl = handle_hittest[0].lbl, axis = handle_lbl.replace(/^.*_([xz])_.*$/, '$1'),
                    particles = js3d.slice[axis + '_particles'];
                (function () {
                    var intersects = intersects_mesh(js3d, original_event, [js3d.slice.intersection_plane]),
                        vertices = particles.geometry.vertices, vertex, vertex_world_position, index,
                        i = vertices.length, dist = [] /* distances from raycast/plane intersection & particles */;
                    while (i--) {
                        vertex = vertices[i];
                        vertex_world_position = particles.matrixWorld.multiplyVector3(vertices[i].clone());
                        dist[i] = vertex_world_position.distanceTo(intersects[0].point);
                    }
                    index = dist.indexOf(Math.min.apply(null, dist));
                    if (index !== js3d.slice[handle_lbl + '_position']) {
                        js3d.slice.reset_handle_material();
                        handle_hittest[0].object.material = matlib
                            .get_material('phong', settings.slice_handle_color_hover);
                        /**
                         * move handles along particles
                         */
                        js3d.slice[handle_lbl + '_position'] = index;
                        js3d.slice[handle_lbl].position[axis] = vertices[index].x;
                        /**
                         * recreate surface plane
                         */
                        js3d.surface_plane.update();
                        /**
                         * update resize bars
                         */
                        js3d.slice.create_slice_bar(axis);
                    }
                }());
            });
        };
    };
})();