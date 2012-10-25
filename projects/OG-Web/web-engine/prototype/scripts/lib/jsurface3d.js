/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    var webgl = Detector.webgl ? true : false, stylesheet,
        default_settings = {
            axis_offset: 1.7,           // X and Z axis distance from the surface
            debug: false,               // Stats.js is required for debugging (https://github.com/mrdoob/stats.js/)
            floating_height: 5,         // Height the surface floats over the floor
            font_face_3d: 'helvetiker', // 3D text font (glyphs for 3D fonts need to be loaded separatly)
            font_size: 35,              // 3D text font size (not in any particular units)
            font_height: 4,             // Extrusion height for 3D text
            font_color: '0x000000',     // Font color for value labels
            font_color_axis_labels: '0xcccccc',   // Font color for axis labels
            hud: true,                            // Toggle options overlay and volatility display
            log: false,                           // Apply natural log by default
            interactive_surface_color: '0xff0000',// Highlight for interactive surface elements (in hex)
            interactive_hud_color: '#f00',        // Highlight colour for volatility display (in css)
            precision_lbl: 2,                     // Floating point presions for labels
            precision_hud: 3,                     // Floating point presions for vol display
            slice_handle_color: '0xbbbbbb',       // Default colour for slice handles
            slice_handle_color_hover: '0x999999', // Hover colour for slice handles
            slice_bar_color: '0xe7e7e7',          // Default slice bar colour
            slice_bar_color_active: '0xffbd00',   // Active slice bar colour
            smile_distance: 50,                   // Distance the smile planes are from the surface
            snap_distance: 3,                     // Mouse proximit to vertices before an interaction is approved
            surface_x: 100,                       // Width
            surface_z: 100,                       // Depth
            surface_y: 40,                        // The height range of the surface
            texture_size: 512,                    // Texture map size for axis ticks
            tick_length: 20,                      // Axis tick lenght
            y_segments: 10,                       // Number of segments to thin vol out to for smile planes
            vertex_shading_hue_min: 180,          // vertex shading hue range min value
            vertex_shading_hue_max: 0,            // vertex shading hue range max value
            zoom_default: 160,                    // Bigger numbers are further away
            zoom_sensitivity: 10                  // Mouse wheel sensitivity
        };
    var Surface = function (js3d) {
        var surface = this, settings = js3d.settings, matlib = js3d.matlib;
        /**
         * Create 3D world, removes any existing geomerty first
         * @return {THREE.Object3D}
         */
        surface.create_world = function () {
            var groups = js3d.geometry_groups, buffers = js3d.buffers;
            if (groups.surface) groups.animation_group.remove(groups.surface), buffers.surface.clear();
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
                        new JSurface3D.Plane(js3d, 'surface'),
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
                groups.surface_top.add(js3d.smile());
                groups.animation_group.add(js3d.vertex_sphere);
                groups.surface_top.position.y = js3d.settings.floating_height;
                groups.surface.add(groups.surface_bottom);
            }
            groups.surface.add(buffers.surface.add(groups.surface_top));
            groups.surface.add(groups.slice);
            return groups.surface;
        };
        /**
         * Implements any valid hover interaction with the surface.
         * Adds vertex_sphere, lines (tubes), active axis labels
         * @param {THREE.Vector3} vertex The vertex within settings.snap_distance of the mouse
         * @param {Number} index The index of the vertex
         * @param {THREE.Mesh} object The closest object to the camera that THREE.Ray returned
         */
        surface.hover = function (vertex, index, object) {
            var hover_buffer = js3d.buffers.hover, hover_group = js3d.geometry_groups.hover,
                vertex_sphere = js3d.vertex_sphere, settings = js3d.settings;
            if (hover_group) {
                js3d.geometry_groups.surface_top.remove(hover_group);
                hover_buffer.clear();
            }
            hover_group = hover_buffer.add(new THREE.Object3D());
            vertex_sphere.position.copy(vertex);
            vertex_sphere.position.y += 5;
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
                        xmax = Math.max.apply(null, [xlft, xrgt]);
                    hover_group.add(new Four.Tube(matlib, xvertices.slice(xmin, xmax + 1), color));
                    hover_group.add(new Four.Tube(matlib, zvertices.slice(zmin, zmax + 1), color));
                }());
                // smile z, z and y lines
                (function () {
                    var yvertices = [{x: 0, y: 0, z: vertex.z}, {x: 0, y: settings.surface_y, z: vertex.z}],
                        zlines = new Four.Tube(matlib, zvertices, color), ylines = new Four.Tube(matlib, yvertices, color);
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
                    xlines.matrixAutoUpdate = false;
                    zlines.matrixAutoUpdate = false;
                    hover_group.add(xlines);
                    hover_group.add(zlines);
                }());
                // surface labels
                ['x', 'z'].forEach(function (val) {
                    var data = js3d.data, lbl_arr = data[val + 's_labels'] || data[val + 's'],
                        txt = val === 'x'
                            ? lbl_arr[index % (js3d.x_segments + 1)]
                            : lbl_arr[~~(index / (js3d.x_segments + 1))],
                        scale = '0.1', group = new THREE.Object3D(),
                        width = THREE.FontUtils.drawText(txt).offset,
                        offset, lbl, vertices;
                    // create label
                    offset = ((width / 2) * scale) + (width * 0.05); // half the width * scale + a relative offset
                    lbl = js3d.text3d(txt, color);
                    lbl.matrixAutoUpdate = false;
                    vertices = val === 'x' ? zvertices : xvertices;
                    group.add(lbl);
                    // create box
                    (function () {
                        var txt_width = THREE.FontUtils.drawText(txt).offset, height = 60,
                            box_width = txt_width * 3,
                            box = new THREE.CubeGeometry(box_width, height, 4, 4, 0, 0),
                            mesh = new THREE.Mesh(box, matlib.get_material('phong', '0xdddddd'));
                        mesh.position.x = (box_width / 2) - (txt_width / 2);
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
            js3d.geometry_groups.surface_top.add(js3d.geometry_groups.hover = hover_group);
        };
        /**
         * Scale data to fit surface dimentions, apply Log (to x and z) if enabled
         */
        surface.init_data = function (new_data) {
            var settings = js3d.settings, data = new_data || js3d.data,
                log = function (arr) {return arr.map(function (val) {return Math.log(val);});};
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
         *
         */
        surface.interactive = function () {
            var settings = js3d.settings, imeshes = js3d.interactive_meshes.meshes, groups = js3d.geometry_groups,
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
                var xlft = surface.intersects(event, [imeshes.lft_x_handle]),
                    xrgt = surface.intersects(event, [imeshes.rgt_x_handle]),
                    zlft = surface.intersects(event, [imeshes.lft_z_handle]),
                    zrgt = surface.intersects(event, [imeshes.rgt_z_handle]);
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
                surface_hittest = surface.intersects(event, [imeshes.surface]);
                if (surface_hittest.length > 0 && surface_hittest[0].face.materialIndex === 1)
                    $selector.trigger('surface_over', surface_hittest);
                else $selector.trigger('surface_out');
                /**
                 * original mouse x & y
                 */
                mouse_x = event.clientX;
                mouse_y = event.clientY;
                /**
                 * Trigger custom events
                 */
                if (mousedown && rotation_enabled) $selector.trigger('rotate_world', event);
                if (webgl && mousedown && slice_enabled) $selector.trigger('slice_handle_drag', event);
            });
            $selector.on('rotate_world', function () {
                var dx = mouse_x - sx, dy = mouse_y - sy;
                groups.animation_group.rotation.y += dx * 0.01;
                groups.animation_group.rotation.x += dy * 0.01;
                sx += dx, sy += dy;
            });
            $selector.on('mousedown.surface.interactive', function (event) {
                event.preventDefault();
                mousedown = true, sx = event.clientX, sy = event.clientY;
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
                        surface.hover(vertex, index, object);
                    }
                }
            });
            $selector.on('surface_out', function () {
                if (groups.hover) {
                    groups.surface_top.remove(groups.hover);
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
                    var intersects = surface.intersects(original_event, [js3d.slice.intersection_plane]),
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
        /**
         * Test if the cursor is over a mesh
         * @event {Object} mouse event object
         * @meshes {Array} meshes array of meshes to test
         * @return {Object} THREE.Ray intersects object
         */
        surface.intersects = function (event, meshes) {
            var mouse = {x: 0, y: 0}, vector, ray;
            mouse.x = ((event.clientX - js3d.sel_offset.left) / js3d.width) * 2 - 1;
            mouse.y = -((event.clientY - js3d.sel_offset.top) / js3d.height) * 2 + 1;
            vector = new THREE.Vector3(mouse.x, mouse.y, 0.5);
            js3d.projector.unprojectVector(vector, js3d.camera);
            ray = new THREE.Ray(js3d.camera.position, vector.subSelf(js3d.camera.position).normalize());
            return ray.intersectObjects(meshes);
        };
    };
    /**
     * Creates a surface gadget
     * @param {Object} config surface configuration object
     *     @param {String} config.selector css selector, location to load surface
     *     @param {Object} config.options override for default_settings
     *     @param {Object} config.data surface data object
     *         @param {Array} config.data.vol surface data points
     *         @param {Array} config.data.xs x axis data points
     *         @param {Array} config.data.zs z axis data points
     *         @param {String} config.data.xs_label x axis label
     *         @param {String} config.data.zs_label z axis label
     */
    window.JSurface3D = function (config) {
        var js3d = this, $selector = $(config.selector), animation_frame, timeout, buffers, settings, stats = {},
            renderer, scene, backlight, keylight, filllight, ambientlight;
        js3d.data = config.data;
        js3d.selector = $selector;
        js3d.settings = settings = $.extend({}, default_settings, config.options);
        js3d.local_settings = {play: null, stopping: false};
        js3d.matlib = new Four.Matlib();
        js3d.projector = new THREE.Projector();
        js3d.x_segments = js3d.data.xs.length - 1;
        js3d.z_segments = js3d.data.zs.length - 1;
        js3d.y_segments = settings.y_segments;
        js3d.interactive_meshes = new Four.InteractiveMeshes();
        js3d.buffers = buffers = {};
        js3d.width = null;
        js3d.height = null;
        js3d.camera = null;
        js3d.sel_offset = null;
        js3d.surface_plane = new JSurface3D.SurfacePlane(js3d);
        js3d.slice = new JSurface3D.Slice(js3d);
        js3d.smile = new JSurface3D.Smile(js3d);
        js3d.text3d = new Four.Text3D(js3d.matlib, js3d.settings);
        js3d.surface = new Surface(js3d);
        /* Hud */
        js3d.hud = new JSurface3D.Hud(js3d, $selector, stylesheet);
        js3d.vol_max = Math.max.apply(null, js3d.data.vol);
        js3d.vol_min = Math.min.apply(null, js3d.data.vol);
        /**
         * Geometry Groups
         *
         * animation_group:   // everything in animation_group rotates on mouse drag
         * hover:             // THREE.Object3D that gets created on hover and destroyed afterward
         * surface_top:       // actual surface and anything that needs to be at that y pos
         * surface_bottom:    // the bottom grid, axis etc
         * surface:           // full surface group, including axis
         * slice:             // geomerty used when slicing
         */
        js3d.geometry_groups = {animation_group: new THREE.Object3D()};
        /**
         * Clean up this js3d instance or all instances
         * @param {Boolean} all also remove shared stylesheet
         */
        js3d.die = function (all) {
            buffers.load.clear();
            cancelAnimationFrame(animation_frame);
            if (all) $(stylesheet).remove();
        };
        js3d.resize = function () {
            var width = js3d.width = $selector.width(), height = js3d.height = $selector.height(), camera = js3d.camera;
            js3d.sel_offset = $selector.offset();
            $selector.find('> canvas').css({width: width, height: height});
            camera.aspect = width / height;
            camera.updateProjectionMatrix();
            renderer.setSize(width, height);
            renderer.render(scene, camera);
            js3d.hud.load();
        };
        /**
         * Updates without reloading everything
         */
        js3d.update = function () {
            js3d.surface.init_data();
            js3d.geometry_groups.animation_group.add(js3d.surface.create_world());
            if (webgl) js3d.slice.load();
        };
        js3d.update_surface_plane = function (new_data) {
            js3d.surface.init_data(new_data);
            js3d.surface_plane.update();
        };
        js3d.load = function () {
            var animation_group = js3d.geometry_groups.animation_group, camera;
            js3d.sel_offset = $selector.offset();
            js3d.width = $selector.width(), js3d.height = $selector.height();
            js3d.surface.init_data();
            js3d.vertex_sphere = new THREE.Mesh(
                new THREE.SphereGeometry(1.5, 10, 10),
                js3d.matlib.get_material('phong', settings.interactive_surface_color)
            );
            js3d.vertex_sphere.matrixAutoUpdate = false;
            js3d.vertex_sphere.visible = false;
            js3d.renderer = renderer = webgl ? new THREE.WebGLRenderer({antialias: true}) : new THREE.CanvasRenderer();
            renderer.setSize(js3d.width, js3d.height);
            // buffers
            buffers.load = new Four.Buffer(renderer);
            buffers.hover = new Four.Buffer(renderer);
            buffers.slice = new Four.Buffer(renderer);
            buffers.surface = new Four.Buffer(renderer);
            buffers.load.add(animation_group);
            // lights
            keylight = new THREE.DirectionalLight(0xf2f6ff, 0.7, 300);  // surface light
            backlight = new THREE.DirectionalLight(0xf2f6ff, 0.5, 500); // smile left
            filllight = new THREE.DirectionalLight(0xf2f6ff, 0.5, 500); // smile right
            ambientlight = new THREE.AmbientLight(0xffffff);
            keylight.position.set(-80, 150, 80);
            backlight.position.set(-150, 100, 100);
            filllight.position.set(100, 100, 150);
            // animation group
            animation_group.add(ambientlight);
            animation_group.add(backlight);
            animation_group.add(keylight);
            animation_group.add(filllight);
            animation_group.add(js3d.surface.create_world());
            animation_group.rotation.y = Math.PI * 0.25;
            // camera
            camera = js3d.camera = new THREE.PerspectiveCamera(45, js3d.width / js3d.height, 1, 1000);
            camera.position.x = 0;
            camera.position.y = 125;
            camera.position.z = settings.zoom_default;
            camera.lookAt({x: 0, y: 0, z: 0});
            // scene
            scene = new THREE.Scene();
            scene.add(animation_group);
            scene.add(camera);
            // render scene
            $selector.html(renderer.domElement).find('canvas').css({position: 'relative'});
            js3d.hud.load();
            // stats
            if (settings.debug) {
                stats.loop = new Stats();
                stats.loop.domElement.style.position = 'absolute';
                stats.loop.domElement.style.top = '0';
                stats.loop.domElement.style.right = '0';
                $(stats.loop.domElement).appendTo($selector);
                stats.render = new Stats();
                stats.render.domElement.style.position = 'absolute';
                stats.render.domElement.style.top = '50px';
                stats.render.domElement.style.right = '0';
                $(stats.render.domElement).appendTo($selector);
            }
            js3d.surface.interactive();
            if (webgl) js3d.slice.load();
        };
        js3d.load();
        (function animate() {
            if (settings.debug) stats.loop.update();
            if (js3d.local_settings.play === null || js3d.local_settings.play) {
                renderer.render(scene, js3d.camera);
                if (settings.debug) stats.render.update();
                if (!js3d.local_settings.stopping)
                    clearTimeout(timeout), timeout = setTimeout(function () {js3d.local_settings.play = false;}, 5000);
                js3d.local_settings.stopping = true;
            }
            animation_frame = requestAnimationFrame(animate);
        }());
    };
})();