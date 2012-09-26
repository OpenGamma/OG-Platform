/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    var webgl = Detector.webgl ? true : false, util = {}, stylesheet,
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
            surface_wire_colour: '0x000000',      // Colour of surface wireframe
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
    /**
     * Custom THREE.SceneUtils.createMultiMaterialObject, THREE's current version creates flickering
     * @param {THREE.PlaneGeometry} geometry
     * @param {Array} materials Array of THREE materials
     */
    util.create_multimaterial_object = function (geometry, materials) {
        var i = 0, il = materials.length, group = new THREE.Object3D();
        for (; i < il; i++) {
            var object = new THREE.Mesh(geometry, materials[i]);
            object.position.y = i / 100;
            group.add(object);
        }
        return group;
    };
    /**
     * Apply Natrual Log to each item in Array
     * @param {Array} arr
     * @returns {Array}
     */
    util.log = function (arr) {return arr.map(function (val) {return Math.log(val);});};
    /**
     * Scales an Array of numbers to a new range
     * @param {Array} arr Array to be scaled
     * @param {Number} range_min New minimum range
     * @param {Number} range_max New maximum range
     * @returns {Array}
     */
    util.scale = function (arr, range_min, range_max) {
        var min = Math.min.apply(null, arr), max = Math.max.apply(null, arr);
        return arr.map(function (val) {return ((val - min) / (max - min) * (range_max - range_min) + range_min);});
    };
    /**
     * Remove every nth item in Array keeping the first and last,
     * also spesificaly remove the second last (as we want to keep the last)
     * @param {Array} arr
     * @param {Number} nth
     * @returns {Array}
     */
    util.thin = function (arr, nth) {
        if (!nth || nth === 1) return arr;
        var len = arr.length;
        return arr.filter(function (val, i) {
            return ((i === 0) || !(i % nth) || (i === (len -1))) || (i === (len -2)) && false
        });
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
        var gadget = this, $selector = $(config.selector), width, height, data = config.data,
            settings = $.extend({}, default_settings, config.options),
            local_settings = {log: true, play: null, stopping: false},
            sel_offset, // needed to calculate mouse coordinates for raycasting
            hud = {}, matlib = {}, smile = {}, surface = {}, slice = {}, char_geometries = {}, stats = {},
            animation_group = new THREE.Object3D(), // everything in animation_group rotates on mouse drag
            hover_group,                            // THREE.Object3D that gets created on hover and destroyed afterward
            surface_group_top,                      // actual surface and anything that needs to be at that y pos
            surface_group_bottom,                   // the bottom grid, axis etc
            surface_group,                          // full surface group, including axis
            slice_group,                            // geomerty used when slicing
            vertex_sphere,                          // the sphere displayed on vertex hover
            surface_plane,                          // reference to the surface mesh
            x_segments = data.xs.length - 1, z_segments = data.zs.length - 1, y_segments = settings.y_segments,
            ys, adjusted_vol, adjusted_xs, adjusted_ys, adjusted_zs, // surface.init_data calculates these values
            vol_max = Math.max.apply(null, data.vol), vol_min = Math.min.apply(null, data.vol),
            renderer, camera, scene, backlight, keylight, filllight, ambientlight, projector = new THREE.Projector(),
            hover_buffer, slice_buffer, load_buffer, surface_buffer, animation_frame, timeout;
        /**
         * Buffer constructor
         * Create buffers that store references to objects that requre their webgl buffers to be cleared together
         */
        var Buffer = function () {
            var buffer = {};
            buffer.arr = [];
            buffer.add = function (object) {
                buffer.arr.push(object);
                return object;
            };
            buffer.clear = function (custom) {
                if (!custom && !buffer.arr.length) return;
                (function dealobj (val) {
                    if ($.isArray(val)) val.forEach(function (val) {dealobj(val);});
                    else if (val instanceof THREE.Texture) renderer.deallocateTexture(val);
                    else if (val instanceof THREE.ParticleSystem) renderer.deallocateObject(val);
                    else if (val instanceof THREE.Mesh) renderer.deallocateObject(val);
                    else if (val instanceof THREE.Object3D && webgl)
                        renderer.deallocateObject(val), dealobj(val.children);
                }(custom || buffer.arr));
                if (!custom) buffer.arr = [];
            };
            return buffer;
        };
        /**
         * Constructor for a plane with correct x / y vertex position spacing
         * @param {String} type 'surface', 'smilex' or 'smiley'
         * @returns {THREE.PlaneGeometry}
         */
        var Plane = function (type) {
            var xlen, ylen, xseg, yseg, xoff, yoff, plane, vertex, len, i, k;
            if (type === 'surface') {
                xlen = settings.surface_x;
                ylen = settings.surface_z;
                xseg = x_segments;
                yseg = z_segments;
                xoff = adjusted_xs;
                yoff = adjusted_zs;
            }
            if (type === 'smilex') {
                xlen = settings.surface_x;
                ylen = settings.surface_y;
                xseg = x_segments;
                yseg = y_segments;
                xoff = adjusted_xs;
                yoff = adjusted_ys;
            }
            if (type === 'smiley') {
                xlen = settings.surface_y;
                ylen = settings.surface_z;
                xseg = y_segments;
                yseg = z_segments;
                xoff = adjusted_ys;
                yoff = adjusted_zs;
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
        /**
         * Constructor for 3D text geometry, also cache character geometry calculations
         * @param {String} str
         * @param {Object} options text geometry options
         * @returns {THREE.Geometry}
         */
        var CachedTextGeometry = function (str, options) {
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
         * Slice handle constructor
         * @return {THREE.Mesh}
         */
        var Handle = function () {
            var geo = new THREE.CubeGeometry(3, 1.2, 3, 2, 0, 0);
            geo.vertices // move middle vertices out to a point
                .filter(function (val) {return (val.x === 0 && val.z === -1.5)})
                .forEach(function (vertex) {vertex.z = -2.9});
            return new THREE.Mesh(geo, matlib.get_material('phong', settings.slice_handle_color));
        };
        /**
         * Slice Bar constructor
         * @param {String} orientation 'x' or 'z'
         * @return {THREE.Mesh}
         */
        var SliceBar = function (orientation) {
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
         * Constructor for 3D text
         * @param {String} str String you want to create
         * @param {String} color text colour in hex
         * @param {Boolean} preserve_kerning set to true to cache geometry without breaking it into characters
         * @param {Boolean} bevel
         * @returns {THREE.Mesh}
         */
        var Text3D = function (str, color, preserve_kerning, bevel) {
            var object, merged = new THREE.Geometry(), material = matlib.get_material('phong', color), xpos = 0,
                options = {
                    size: settings.font_size, height: settings.font_height,
                    font: settings.font_face_3d, weight: 'normal', style: 'normal',
                    bevelEnabled: bevel || false, bevelSize: 0.6, bevelThickness: 0.6
                };
            if (preserve_kerning) return new THREE.Mesh(new CachedTextGeometry(str, options), material);
            str.split('').forEach(function (val) {
                var text = new CachedTextGeometry(val, options), mesh = new THREE.Mesh(text, material);
                mesh.position.x = xpos + (val === '.' ? 5 : 0);                                   // space before
                xpos = xpos + ((THREE.FontUtils.drawText(val).offset)) + (val === '.' ? 10 : 15); // space after
                THREE.GeometryUtils.merge(merged, mesh);
            });
            merged.computeFaceNormals();
            object = new THREE.Mesh(merged, material);
            object.matrixAutoUpdate = false;
            return object;
        };
        /**
         * Constructor for a tube.
         * THREE doesnt currently support creating a tube with a line as a path
         * (Spline is supported, but we dont want that), so we create separate tubes and add them to an object.
         * Also linewidth doest seem to work for a LineBasicMaterial, thus using tube
         * @param {Array} points Array of Vector3's
         * @param {String} color hex colour
         * @return {THREE.Object3D}
         */
        var Tube = function (points, color) {
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
        /**
         * Clean up this jsurface3d instance or all instances
         * @param {Boolean} all also remove shared stylesheet
         */
        gadget.die = function (all) {
            load_buffer.clear();
            cancelAnimationFrame(animation_frame);
            if (all) $(stylesheet).remove();
        };
        gadget.resize = function () {
            width = $selector.width();
            height = $selector.height();
            sel_offset = $selector.offset();
            $selector.find('> canvas').css({width: width, height: height});
            camera.aspect = width / height;
            camera.updateProjectionMatrix();
            renderer.setSize(width, height);
            renderer.render(scene, camera);
        };
        /**
         * Updates without reloading everything
         */
        gadget.update = function () {
            surface.init_data();
            animation_group.add(surface.create_surface());
            if (webgl) slice.load();
        };
        /**
         * Loads 2D overlay display with form
         */
        hud.load = function () {
            if (!default_settings.hud) return;
            (function () {
                if ($(stylesheet).length) return;
                var css = '\
                    .OG-s {bottom: 10px; left: 10px; top: 0; position: absolute;}\
                    .OG-s .og-o {position: absolute; top: 10px; white-space: nowrap;}\
                    .OG-s .og-o input {vertical-align: top;}\
                    .OG-s .og-v {padding-top: 9px; padding-bottom: 3px; position: absolute; bottom: 0;}\
                    .OG-s .og-v canvas {border: 1px solid #ccc;}\
                    .OG-s .og-v span {position: absolute; left: 20px;}\
                    .OG-s .og-v .og-max {top: 0;}\
                    .OG-s .og-v .og-min {bottom: 0;}\
                    .OG-s .og-v .og-vol {display: none; background: #eee; border: 1px solid #ddd; padding: 0 5px;}\
                    .OG-s .og-v .og-vol:after {content: ""; display: block; position: absolute; \
                        width: 0; height: 0; left: -10px; top: 50%; margin-top: -4px; \
                        border-top: 5px solid transparent; border-right: 10px solid #ddd; \
                        border-bottom: 5px solid transparent;}',
                    head = document.querySelector('head'), style = document.createElement('style');
                style.setAttribute('data-og', 'surface');
                if (style.styleSheet) style.styleSheet.cssText = css; // IE
                else style.appendChild(document.createTextNode(css));
                head.appendChild(stylesheet = style);
            })();
            var tmpl = '\
                <div class="OG-s">\
                  <div class="og-o"><label>Log<input type="checkbox" checked="checked" /></label></div>\
                  <div class="og-v">\
                    <span class="og-max">{{max}}</span><span class="og-min">{{min}}</span><span class="og-vol"></span>\
                    <canvas>canvas</canvas>\
                  </div>\
                </div>',
                min = vol_min.toFixed(settings.precision_hud), max = vol_max.toFixed(settings.precision_hud),
                html = tmpl.replace(/{{(?:max|min)}}/g, function (m) {return m === '{{min}}' ? min : max}),
                $html = $(html).appendTo($selector);
            hud.vol_canvas_height = height / 2;
            if (webgl) hud.volatility($html.find('canvas')[0]);
            else $html.find('.og-v').hide();
            hud.form();
        };
        /**
         * Configure surface gadget display
         */
        hud.form = function () {
            $selector
                .find('.og-o input')
                .prop('checked', local_settings.log)
                .on('change', function () {
                    local_settings.log = $(this).is(':checked');
                    gadget.update();
                });
        };
        /**
         * Set a value in the 2D volatility display
         * @param {Number} value The value to set. If value is not a number the indicator is hidden
         */
        hud.set_volatility = function (value) {
            if (!default_settings.hud) return;
            var top = (util.scale([vol_min, value, vol_max], hud.vol_canvas_height, 0))[1],
                css = {top: top + 'px', color: settings.interactive_hud_color},
                $vol = $selector.find('.og-vol');
            typeof value === 'number'
                ? $vol.html(value.toFixed(settings.precision_hud)).css(css).show()
                : $vol.empty().hide();
        };
        hud.vol_canvas_height = null;
        /**
         * Volatility gradient canvas
         * @param {Object} canvas html canvas element
         */
        hud.volatility = function (canvas) {
            var ctx = canvas.getContext('2d'), gradient,
                min_hue = settings.vertex_shading_hue_min, max_hue = settings.vertex_shading_hue_max,
                steps = Math.abs(max_hue - min_hue) / 60, stop;
            gradient = ctx.createLinearGradient(0, 0, 0, hud.vol_canvas_height);
            canvas.width = 10;
            canvas.height = hud.vol_canvas_height;
            gradient.addColorStop(0, 'hsl(' + max_hue + ', 100%, 50%)');
            while (steps--) { // 60: the number of hue increaments before one color starts fading in and another out
                stop = (steps * 60 / (min_hue / 100)) / 100;
                gradient.addColorStop(stop, 'hsl(' + steps * 60 + ', 100%, 50%)');
                gradient.addColorStop(stop, 'hsl(' + steps * 60 + ', 100%, 50%)');
            }
            gradient.addColorStop(1, 'hsl(' + min_hue + ', 100%, 50%)');
            ctx.fillStyle = gradient;
            ctx.fillRect(0, 0, 10, hud.vol_canvas_height);
        };
        /**
         * Material Library
         */
        matlib.canvas = {};
        matlib.cache = {};
        matlib.get_material = function (material, color) {
            var name = material + '_' + color;
            if (matlib.cache[name]) return matlib.cache[name];
            matlib.cache[name] = matlib[material](color || void 0);
            return matlib.cache[name];
        };
        matlib.canvas.compound_surface = function () {
            return [matlib.canvas.flat('0xbbbbbb'), matlib.canvas.flat('0xbbbbbb')];
        };
        matlib.canvas.compound_surface_wire = function () {
            return [matlib.canvas.wire('0xffffff')];
        };
        matlib.canvas.flat = function (color) {
            return new THREE.MeshBasicMaterial({color: color, shading: THREE.FlatShading});
        };
        matlib.canvas.wire = function (color) {
            return new THREE.MeshBasicMaterial({color: color || 0xcccccc, wireframe: true});
        };
        matlib.compound_grid_wire = function () {
            return [
                new THREE.MeshPhongMaterial({
                    // color represents diffuse in THREE.MeshPhongMaterial
                    ambient: 0x000000, color: 0xeeeeee, specular: 0xdddddd, emissive: 0x000000, shininess: 0
                }),
                matlib.wire(0xcccccc)
            ]
        };
        matlib.compound_floor_wire = function () {
            return [
                new THREE.MeshPhongMaterial({
                    ambient: 0x000000, color: 0xefefef, specular: 0xffffff, emissive: 0x000000, shininess: 10
                }),
                matlib.wire(0xcccccc)
            ]
        };
        matlib.compound_surface = function () {
            if (!webgl) return matlib.canvas.compound_surface();
            return [matlib.transparent(), matlib.vertex()]
        };
        matlib.compound_surface_wire = function () {
            if (!webgl) return matlib.canvas.compound_surface_wire();
            return [matlib.transparent(), matlib.wire(settings.surface_wire_colour)]
        };
        matlib.wire = function (color) {
            if (!webgl) return matlib.canvas.wire(color);
            return new THREE.MeshBasicMaterial({color: color || 0x999999, wireframe: true});
        };
        matlib.transparent = function () {
            return new THREE.MeshBasicMaterial({opacity: 0, transparent: true});
        };
        matlib.flat = function (color) {
            if (!webgl) return matlib.canvas.flat(color);
            return new THREE.MeshBasicMaterial({color: color, wireframe: false});
        };
        matlib.particles = function () {return new THREE.ParticleBasicMaterial({color: '0xbbbbbb', size: 1});};
        matlib.phong = function (color) {
            if (!webgl) return matlib.canvas.flat(color);
            return new THREE.MeshPhongMaterial({
                ambient: 0x000000, color: color, specular: 0x999999, shininess: 50, shading: THREE.SmoothShading
            });
        };
        matlib.texture = function (texture) {
            return new THREE.MeshBasicMaterial({map: texture, color: 0xffffff, transparent: true});
        };
        matlib.vertex = function () {
            return new THREE.MeshPhongMaterial({shading: THREE.FlatShading, vertexColors: THREE.VertexColors})
        };
        slice.lft_x_handle_position = x_segments;
        slice.rgt_x_handle_position = 0;
        slice.lft_z_handle_position = z_segments;
        slice.rgt_z_handle_position = 0;
        slice.load = function () {
            var plane = new THREE.PlaneGeometry(5000, 5000, 0, 0),
                mesh = new THREE.Mesh(plane, matlib.get_material('wire', '0xcccccc'));
            mesh.matrixAutoUpdate = false;
            mesh.updateMatrix();
            slice.intersection_plane = mesh;
            surface_group_bottom.add(slice.intersection_plane);
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
            slice_buffer.clear(slice[bar_lbl]);
            slice_group.remove(slice[bar_lbl]);
            slice[bar_lbl] = new SliceBar(axis);
            vertices = slice[bar_lbl].geometry.vertices;
            vertices[0].x = vertices[1].x = vertices[2].x = vertices[3].x = Math.max.apply(null, [lx, rx]);
            vertices[4].x = vertices[5].x = vertices[6].x = vertices[7].x = Math.min.apply(null, [lx, rx]);
            if (!(Math.abs(lx) + Math.abs(rx) === settings['surface_' + axis])) // change color
                slice[bar_lbl].material = matlib.get_material('phong', settings.slice_bar_color_active);
            slice_group.add(slice[bar_lbl]);
        };
        slice.x = function () {
            var xpos = (settings.surface_x / 2), zpos = settings.surface_z / 2 + 1.5 + settings.axis_offset;
            /**
             * particle guide
             * (dotted lines that guide the slice handles)
             */
            (function () {
                var geo = new THREE.Geometry(), num_vertices = adjusted_xs.length;
                while (num_vertices--) geo.vertices.push(new THREE.Vector3(adjusted_xs[num_vertices], 0, 0));
                slice.x_particles = new THREE.ParticleSystem(geo, matlib.get_material('particles'));
                slice.x_particles.position.x += 0.1;
                slice.x_particles.position.z = zpos;
                surface_group_bottom.add(slice.x_particles);
            }());
            /**
             * handles
             */
            slice.lft_x_handle = new Handle();
            slice.lft_x_handle.position.x = -xpos;
            slice.lft_x_handle.position.z = zpos;
            slice.rgt_x_handle = new Handle();
            slice.rgt_x_handle.position.x = xpos;
            slice.rgt_x_handle.position.z = zpos;
            surface_group_bottom.add(slice.lft_x_handle);
            surface_group_bottom.add(slice.rgt_x_handle);
            surface.interactive_meshes.add('lft_x_handle', slice.lft_x_handle);
            surface.interactive_meshes.add('rgt_x_handle', slice.rgt_x_handle);
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
                var geo = new THREE.Geometry(), num_vertices = adjusted_zs.length;
                while (num_vertices--) geo.vertices.push(new THREE.Vector3(adjusted_zs[num_vertices], 0, 0));
                slice.z_particles = new THREE.ParticleSystem(geo, matlib.get_material('particles'));
                slice.z_particles.rotation.y = -Math.PI * 0.5;
                slice.z_particles.position.x = -xpos;
                surface_group_bottom.add(slice.z_particles);
            }());
            /**
             * handles
             */
            slice.lft_z_handle = new Handle();
            slice.lft_z_handle.position.x = -xpos;
            slice.lft_z_handle.position.z = -zpos;
            slice.lft_z_handle.rotation.y = -Math.PI * .5;
            slice.rgt_z_handle = new Handle();
            slice.rgt_z_handle.position.x = -xpos;
            slice.rgt_z_handle.position.z = zpos;
            slice.rgt_z_handle.rotation.y = -Math.PI * .5;
            surface_group_bottom.add(slice.lft_z_handle);
            surface_group_bottom.add(slice.rgt_z_handle);
            surface.interactive_meshes.add('lft_z_handle', slice.lft_z_handle);
            surface.interactive_meshes.add('rgt_z_handle', slice.rgt_z_handle);
            slice.lft_z_handle.position.z = slice.z_particles.geometry.vertices[slice.lft_z_handle_position].x;
            slice.rgt_z_handle.position.z = slice.z_particles.geometry.vertices[slice.rgt_z_handle_position].x;
            /**
             * slice bar
             */
            slice.create_slice_bar('z');
        };
        /**
         * Create x smile plane and axis
         */
        smile.x = function () {
            var obj = new THREE.Object3D();
            (function () { // plane
                var plane = new Plane('smilex'), material = matlib.get_material('compound_grid_wire'),
                    mesh = util.create_multimaterial_object(plane, material);
                mesh.rotation.x = Math.PI * 0.5;
                mesh.position.y = settings.surface_y;
                mesh.position.z = -((settings.surface_z / 2) + settings.smile_distance);
                mesh.matrixAutoUpdate = false;
                mesh.updateMatrix();
                obj.add(mesh);
            }());
            (function () { // axis
                var y = {axis: 'y', spacing: adjusted_ys, labels: ys, right: true}, y_axis = surface.create_axis(y);
                y_axis.position.x = -(settings.surface_x / 2) - 25;
                y_axis.position.y = 4;
                y_axis.position.z = -(settings.surface_z / 2) - settings.smile_distance;
                y_axis.rotation.y = Math.PI * .5;
                y_axis.rotation.z = Math.PI * .5;
                obj.add(y_axis);
            }());
            return obj;
        };
        /**
         * Create z smile plane and axis
         */
        smile.z = function () {
            var obj = new THREE.Object3D();
            (function () { // plane
                var plane = new Plane('smiley'), material = matlib.get_material('compound_grid_wire'),
                    mesh = util.create_multimaterial_object(plane, material);
                mesh.position.x = (settings.surface_x / 2) + settings.smile_distance;
                mesh.rotation.z = Math.PI * 0.5;
                mesh.matrixAutoUpdate = false;
                mesh.updateMatrix();
                obj.add(mesh);
            }());
            (function () { // axis
                var y = {axis: 'y', spacing: adjusted_ys, labels: ys}, y_axis = surface.create_axis(y);
                y_axis.position.y = 4;
                y_axis.position.z = (settings.surface_z / 2) + 5;
                y_axis.position.x = (settings.surface_x / 2) + settings.smile_distance;
                y_axis.rotation.z = Math.PI * .5;
                obj.add(y_axis);
            }());
            return obj;
        };
        /**
         * Create smile shadows, the planes float above the floor, so draw a line to show where the ground below is
         */
        smile.shadows = function () {
            var obj = new THREE.Object3D();
            (function () { // x shadow
                var z = settings.surface_z / 2 + settings.smile_distance, half_width = settings.surface_x / 2,
                    shadow = new Tube([{x: -half_width, y: 0, z: -z}, {x: half_width, y: 0, z: -z}], '0xaaaaaa');
                shadow.matrixAutoUpdate = false;
                obj.add(shadow);
            }());
            (function () { // z shadow
                var x = settings.surface_x / 2 + settings.smile_distance, half_width = settings.surface_z / 2,
                    shadow = new Tube([{x: x, y: 0, z: -half_width}, {x: x, y: 0, z: half_width}], '0xaaaaaa');
                shadow.matrixAutoUpdate = false;
                obj.add(shadow);
            }());
            return obj;
        };
        /**
         * Creates both axes
         * @return {THREE.Object3D}
         */
        surface.create_axes = function () {
            var group = new THREE.Object3D,
                x = {axis: 'x', spacing: adjusted_xs, labels: data.xs_labels || data.xs, label: data.xs_label},
                z = {axis: 'z', spacing: adjusted_zs, labels: data.zs_labels || data.zs, label: data.zs_label},
                x_axis = surface.create_axis(x),
                z_axis = surface.create_axis(z);
            x_axis.position.z = settings.surface_z / 2 + settings.axis_offset;
            z_axis.position.x = -settings.surface_x / 2 - settings.axis_offset;
            z_axis.rotation.y = -Math.PI * .5;
            group.add(x_axis);
            group.add(z_axis);
            return group;
        };
        /**
         * Creates an Axis with labels for the bottom grid
         * @param {Object} config
         * config.axis {String} x or z
         * config.spacing {Array} Array of numbers adjusted to fit units of mesh
         * config.labels {Array} Array of lables
         * config.label {String} Axis label
         * @return {THREE.Object3D}
         */
        surface.create_axis = function (config) {
            var mesh = new THREE.Object3D(), i, nth = Math.ceil(config.spacing.length / 6),
                lbl_arr = util.thin(config.labels, nth), pos_arr = util.thin(config.spacing, nth),
                axis_len = settings['surface_' + config.axis];
            (function () { // axis values
                var value, n;
                for (i = 0; i < lbl_arr.length; i++) {
                    n = lbl_arr[i];
                    n = n % 1 === 0 ? n : (+n).toFixed(settings.precision_lbl).replace(/0+$/, '');
                    value = new Text3D(n, settings.font_color);
                    value.scale.set(0.1, 0.1, 0.1);
                    if (config.axis === 'y') {
                        value.position.x = pos_arr[i] - 6;
                        value.position.z = config.right
                            ? -(THREE.FontUtils.drawText(n).offset * 0.2) + 18
                            : 2;
                        value.rotation.z = -Math.PI * .5;
                        value.rotation.x = -Math.PI * .5;
                    } else {
                        value.rotation.x = -Math.PI * .5;
                        value.position.x = pos_arr[i] - ((THREE.FontUtils.drawText(n).offset * 0.2) / 2);
                        value.position.y = 0.1;
                        value.position.z = 12;
                    }
                    value.matrixAutoUpdate = false;
                    value.updateMatrix();
                    mesh.add(value);
                }
            }());
            (function () { // axis label
                if (!config.label) return;
                var label = new Text3D(config.label, settings.font_color_axis_labels, true, true);
                label.scale.set(0.2, 0.2, 0.1);
                label.rotation.x = -Math.PI * .5;
                label.position.x = -(axis_len / 2) -3;
                label.position.y = 1;
                label.position.z = 25;
                label.matrixAutoUpdate = false;
                label.updateMatrix();
                mesh.add(label);
            }());
            (function () { // axis ticks
                var canvas = document.createElement('canvas'),
                    ctx = canvas.getContext('2d'),
                    plane = new THREE.PlaneGeometry(axis_len, 5, 0, 0),
                    axis = new THREE.Mesh(plane, matlib.texture(surface_buffer.add(new THREE.Texture(canvas)))),
                    tick_stop_pos =  settings.tick_length + 0.5,
                    labels = util.thin(config.spacing.map(function (val) {
                        // if not y axis offset half. y planes start at 0, x and z start at minus half width
                        var offset = config.axis === 'y' ? 0 : axis_len / 2;
                        return (val + offset) * (settings.texture_size / axis_len)
                    }), nth);
                canvas.width = settings.texture_size;
                canvas.height = 32;
                ctx.beginPath();
                ctx.lineWidth = 2;
                for (i = 0; i < labels.length; i++)
                    ctx.moveTo(labels[i] + 0.5, tick_stop_pos), ctx.lineTo(labels[i] + 0.5, 0);
                ctx.moveTo(0.5, tick_stop_pos);
                ctx.lineTo(0.5, 0.5);
                ctx.lineTo(canvas.width - .5, 0.5);
                ctx.lineTo(canvas.width - .5, tick_stop_pos);
                ctx.stroke();
                axis.material.map.needsUpdate = true;
                axis.doubleSided = true;
                if (config.axis === 'y') {
                    if (config.right) axis.rotation.x = Math.PI, axis.position.z = 20;
                    axis.position.x = (axis_len / 2) - 4;
                } else {
                    axis.position.z = 5;
                }
                axis.matrixAutoUpdate = false;
                axis.updateMatrix();
                mesh.add(axis);
            }());
            return mesh;
        };
        /**
         * Creates bottom grid with materials
         * @return {THREE.Mesh}
         */
        surface.create_bottom_grid = function () {
            var plane = new Plane('surface'),
                mesh = util.create_multimaterial_object(plane, matlib.get_material('compound_grid_wire'));
            mesh.overdraw = true;
            mesh.matrixAutoUpdate = false;
            return mesh;
        };
        /**
         * Creates floor
         * @return {THREE.Object3D}
         */
        surface.create_floor = function () {
            var plane = new THREE.PlaneGeometry(5000, 5000, 100, 100), floor;
            floor = util.create_multimaterial_object(plane, matlib.get_material('compound_floor_wire'));
            floor.position.y = -0.01;
            return floor;
        };
        /**
         * Create the actual full surface object with axis. Removes any existing ones first
         * @return {THREE.Object3D}
         */
        surface.create_surface = function () {
            if (surface_group) animation_group.remove(surface_group), surface_buffer.clear();
            surface.interactive_meshes.remove('surface');
            surface_group = new THREE.Object3D();
            slice_group = slice_buffer.add(new THREE.Object3D());
            surface_group_top = new THREE.Object3D();
            surface_group_bottom = new THREE.Object3D();
            slice_group.add(surface_plane = surface.create_surface_plane());
            if (webgl) surface_group_top.add(smile.x());
            if (webgl) surface_group_top.add(smile.z());
            if (webgl) animation_group.add(vertex_sphere);
            if (webgl) surface_group_top.position.y = settings.floating_height;
            if (webgl) surface_group_bottom.add(surface_buffer.add(surface.create_bottom_grid()));
            if (webgl) surface_group_bottom.add(surface_buffer.add(smile.shadows()));
            if (webgl) surface_group_bottom.add(surface_buffer.add(surface.create_floor()));
            if (webgl) surface_group_bottom.add(surface.create_axes());
            surface_group.add(surface_buffer.add(surface_group_top));
            surface_group.add(surface_buffer.add(slice_group));
            if (webgl) surface_group.add(surface_group_bottom);
            return surface_group;
        };
        /**
         * Create the surface plane with vertex shading
         * @return {THREE.Object3D}
         */
        surface.create_surface_plane = function () {
            var plane = new Plane('surface'), group = new THREE.Object3D(), i, wire, wiremesh;
            plane.verticesNeedUpdate = true;
            for (i = 0; i < adjusted_vol.length; i++) {plane.vertices[i].y = adjusted_vol[i];} // extrude
            wire = THREE.GeometryUtils.clone(plane);
            plane.computeCentroids();
            plane.computeFaceNormals();
            plane.computeBoundingSphere();
            (function () { // apply heatmap
                if (!webgl) return;
                var faces = 'abcd', face, color, vertex, index, i, k,
                    min = Math.min.apply(null, adjusted_vol), max = Math.max.apply(null, adjusted_vol),
                    hue_min = settings.vertex_shading_hue_min, hue_max = settings.vertex_shading_hue_max, hue;
                for (i = 0; i < plane.faces.length; i ++) {
                    face = plane.faces[i];
                    for (k = 0; k < 4; k++) {
                        index = face[faces.charAt(k)];
                        vertex = plane.vertices[index];
                        color = new THREE.Color(0xffffff);
                        hue = ~~((vertex.y - min) / (max - min) * (hue_max - hue_min) + hue_min) / 360;
                        color.setHSV(hue, 0.95, 0.7);
                        face.vertexColors[k] = color;
                    }
                }
            }());
            // apply surface materials
            plane.materials = matlib.get_material('compound_surface');
            wire.materials = matlib.get_material('compound_surface_wire');
            (function () { // clip/slice
                var row, i, x, l,
                    zlft = Math.abs(slice.lft_z_handle_position - z_segments) * x_segments,
                    zrgt = Math.abs(slice.rgt_z_handle_position - z_segments) * x_segments,
                    xlft = Math.abs(slice.lft_x_handle_position - x_segments),
                    xrgt = Math.abs(slice.rgt_x_handle_position - x_segments),
                    zmin = Math.min.apply(null, [zlft, zrgt]),
                    zmax = Math.max.apply(null, [zlft, zrgt]),
                    xmin = Math.min.apply(null, [xlft, xrgt]),
                    xmax = Math.max.apply(null, [xlft, xrgt]);
                for (i = 0, x = 0, row = 0, l = plane.faces.length; i < l; i++, x++) {
                    var plane_face = plane.faces[i], wire_face = wire.faces[i];
                    if (x === x_segments) x = 0, row++;
                    if (
                        (i < zmax) && (i > zmin - 1) && // z slice
                        (i > xmin + row * x_segments -1) && (i < xmax + row * x_segments)  // x slice
                    ) plane_face.materialIndex = wire_face.materialIndex = 1;
                    else plane_face.materialIndex = wire_face.materialIndex = 0;
                }
            }());
            // move wiremesh to account for Z-fighting
            wiremesh = new THREE.Mesh(wire, new THREE.MeshFaceMaterial());
            wiremesh.position.y = 0.01;
            group.add(new THREE.Mesh(plane, new THREE.MeshFaceMaterial()));
            group.add(wiremesh);
            group.position.y = settings.floating_height;
            group.matrixAutoUpdate = false;
            group.updateMatrix();
            group.children.forEach(function (mesh) {
                mesh.doubleSided = true;
                mesh.matrixAutoUpdate = false;
            });
            surface.interactive_meshes.add('surface', group.children[0]);
            return group;
        };
        /**
         * Implements any valid hover interaction with the surface.
         * Adds vertex_sphere, lines (tubes), active axis labels
         * @param {THREE.Vector3} vertex The vertex within settings.snap_distance of the mouse
         * @param {Number} index The index of the vertex
         * @param {THREE.Mesh} object The closest object to the camera that THREE.Ray returned
         */
        surface.hover = function (vertex, index, object) {
            if (hover_group) surface_group_top.remove(hover_group), hover_buffer.clear();
            hover_group = hover_buffer.add(new THREE.Object3D());
            vertex_sphere.position.copy(vertex);
            vertex_sphere.position.y += 5;
            vertex_sphere.updateMatrix();
            vertex_sphere.visible = true;
            (function () {
                // [xz]from & [xz]to are the start and end vertex indexes for a vertex row or column
                var x, xvertices = [], xvertices_bottom = [], z, zvertices = [], zvertices_bottom = [],
                    color = settings.interactive_surface_color,
                    xfrom = index - (index % (x_segments + 1)),
                    xto   = xfrom + x_segments + 1,
                    zfrom = index % (x_segments + 1),
                    zto   = ((x_segments + 1) * (z_segments + 1)) - (x_segments - zfrom);
                for (x = xfrom; x < xto; x++) xvertices.push(object.geometry.vertices[x]);
                for (z = zfrom; z < zto; z += x_segments + 1) zvertices.push(object.geometry.vertices[z]);
                hud.set_volatility(data.vol[index]);
                // surface lines
                (function () {
                    var xlft = x_segments - slice.lft_x_handle_position,
                        xrgt = x_segments - slice.rgt_x_handle_position,
                        zlft = z_segments - slice.lft_z_handle_position,
                        zrgt = z_segments - slice.rgt_z_handle_position,
                        zmin = Math.min.apply(null, [zlft, zrgt]),
                        zmax = Math.max.apply(null, [zlft, zrgt]),
                        xmin = Math.min.apply(null, [xlft, xrgt]),
                        xmax = Math.max.apply(null, [xlft, xrgt]);
                    hover_group.add(new Tube(xvertices.slice(xmin, xmax + 1), color));
                    hover_group.add(new Tube(zvertices.slice(zmin, zmax + 1), color));
                }());
                // smile z, z and y lines
                (function () {
                    var yvertices = [{x: 0, y: 0, z: vertex.z}, {x: 0, y: settings.surface_y, z: vertex.z}],
                        zlines = new Tube(zvertices, color), ylines = new Tube(yvertices, color);
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
                        xlines = new Tube(xvertices, color), ylines = new Tube(yvertices, color);
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
                    xlines = new Tube(xvertices_bottom, color);
                    zlines = new Tube(zvertices_bottom, color);
                    xlines.matrixAutoUpdate = false;
                    zlines.matrixAutoUpdate = false;
                    hover_group.add(xlines);
                    hover_group.add(zlines);
                }());
                // surface labels
                ['x', 'z'].forEach(function (val) {
                    var lbl_arr = data[val + 's_labels'] || data[val + 's'],
                        txt = val === 'x'
                            ? lbl_arr[index % (x_segments + 1)]
                            : lbl_arr[~~(index / (x_segments + 1))],
                        scale = '0.1', group = new THREE.Object3D(),
                        width = THREE.FontUtils.drawText(txt).offset,
                        offset, lbl, vertices;
                    // create label
                    offset = ((width / 2) * scale) + (width * 0.05); // half the width * scale + a relative offset
                    lbl = new Text3D(txt, color);
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
                            return (val.x === 0 && val.y === height / 2)
                        }).forEach(function (vertex) {vertex.y = height});
                        mesh.matrixAutoUpdate = false;
                        mesh.updateMatrix();
                        group.add(mesh);
                    }());
                    // position / rotation
                    group.scale.set(scale, scale, scale);
                    group.position.y = -settings.floating_height + .5;
                    group.rotation.x = -Math.PI * .5;
                    if (val === 'x') {
                        group.position.x = vertices[0][val] - offset;
                        group.position.z = (settings.surface_z / 2) + 12 + settings.axis_offset;
                        group.position.z = (settings.surface_z / 2) + 12 + settings.axis_offset;
                    }
                    if (val === 'z') {
                        group.position.x = -((settings.surface_x / 2) + 12) - settings.axis_offset;
                        group.position.z = vertices[0][val] - offset;
                        group.rotation.z = -Math.PI * .5;
                    }
                    group.matrixAutoUpdate = false;
                    group.updateMatrix();
                    hover_group.matrixAutoUpdate = false;
                    hover_group.updateMatrix();
                    hover_group.add(group);
                });
            }());
            surface_group_top.add(hover_group);
        };
        /**
         * Scale data to fit surface dimentions, apply Log (to x and z) if enabled
         */
        surface.init_data = function () {
            // adjusted data is the original data scaled to fit 2D grids width/length/height.
            // It is used to set the distance between plane segments. Then the real data is used as the values
            adjusted_vol = util.scale(data.vol, 0, settings.surface_y);
            adjusted_xs = util.scale(
                local_settings.log ? util.log(data.xs) : data.xs,
                -(settings.surface_x / 2), settings.surface_x / 2
            );
            adjusted_zs = util.scale(
                local_settings.log ? util.log(data.zs) : data.zs,
                -(settings.surface_z / 2), settings.surface_z / 2
            );
            // data.ys doesnt exist, so we need to create adjusted_ys manualy out of the given
            // vol plane range: 0 - settings.surface_y
            adjusted_ys = (function () {
                var increment = settings.surface_y / y_segments, arr = [], i;
                for (i = 0; i < y_segments + 1; i++) arr.push(i * increment);
                return arr;
            }());
            // create data.ys out of vol_min and vol_max, call it ys not to confuse with config paramater
            // we are not using ys to create adjusted_ys as we want more control over adjusted_ys
            // than a standard util.scale
            ys = (function () {
                var increment = (vol_max - vol_min) / y_segments, arr = [], i;
                for (i = 0; i < y_segments + 1; i++)
                    arr.push((+vol_min + (i * increment)).toFixed(settings.precision_lbl));
                return arr;
            }());
        };
        surface.interactive = function () {
            var imeshes = surface.interactive_meshes.meshes,
                surface_hittest, handle_hittest, // store the return value of successful raycasts
                mousedown = false, sx = 0, sy = 0, mouse_x = null, mouse_y = null,
                hit_handle = false, rotation_enabled = true, slice_enabled = false;
            /**
             * Populate surface_hittest and handle_hittest
             * Trigger rotate_world and slice_handle_drag events
             */
            $selector.on('mousemove.surface.interactive', function (event) {
                event.preventDefault();
                local_settings.play = true;
                local_settings.stopping = false;
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
                animation_group.rotation.y += dx * 0.01;
                animation_group.rotation.x += dy * 0.01;
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
                var pos = camera.position.z - (direction * default_settings.zoom_sensitivity);
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
                if (hover_group) surface_group_top.remove(hover_group), hover_buffer.clear();
                vertex_sphere.visible = false;
                hud.set_volatility();
            });
            $selector.on('handle_over', function () {
                slice.reset_handle_material();
                handle_hittest[0].object.material = matlib.get_material('phong', settings.slice_handle_color_hover);
                $selector.css({cursor: 'pointer'});
            });
            $selector.on('handle_out', function () {
                slice.reset_handle_material();
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
                    particles = slice[axis + '_particles'];
                (function () {
                    var intersects = surface.intersects(original_event, [slice.intersection_plane]),
                        vertices = particles.geometry.vertices, vertex, vertex_world_position, index,
                        i = vertices.length, dist = [] /* distances from raycast/plane intersection & particles */;
                    while (i--) {
                        vertex = vertices[i];
                        vertex_world_position = particles.matrixWorld.multiplyVector3(vertices[i].clone());
                        dist[i] = vertex_world_position.distanceTo(intersects[0].point);
                    }
                    index = dist.indexOf(Math.min.apply(null, dist));
                    if (index !== slice[handle_lbl + '_position']) {
                        slice.reset_handle_material();
                        handle_hittest[0].object.material = matlib
                            .get_material('phong', settings.slice_handle_color_hover);
                        /**
                         * move handles along particles
                         */
                        slice[handle_lbl + '_position'] = index;
                        slice[handle_lbl].position[axis] = vertices[index].x;
                        /**
                         * recreate surface plane
                         */
                        slice_group.remove(surface_plane);
                        slice_group.add(surface_plane = surface.create_surface_plane());
                        /**
                         * update resize bars
                         */
                        slice.create_slice_bar(axis);
                    }
                }());
            });
        };
        /**
         * Keeps a tally of meshes that need to support raycasting
         */
        surface.interactive_meshes = {
            add: function (name, mesh) {
                if (!surface.interactive_meshes.meshes[name]) surface.interactive_meshes.meshes[name] = {};
                surface.interactive_meshes.meshes[name] = mesh;
            },
            meshes: {},
            remove: function (name) {
                if (name in surface.interactive_meshes.meshes) delete surface.interactive_meshes.meshes[name];
            }
        };
        /**
         * Test if the cursor is over a mesh
         * @event {Object} mouse event object
         * @meshes {Array} meshes array of meshes to test
         * @return {Object} THREE.Ray intersects object
         */
        surface.intersects = function (event, meshes) {
            var mouse = {x: 0, y: 0}, vector, ray;
            mouse.x = ((event.clientX - sel_offset.left) / width) * 2 - 1;
            mouse.y = -((event.clientY - sel_offset.top) / height) * 2 + 1;
            vector = new THREE.Vector3(mouse.x, mouse.y, 0.5);
            projector.unprojectVector(vector, camera);
            ray = new THREE.Ray(camera.position, vector.subSelf(camera.position).normalize());
            return ray.intersectObjects(meshes);
        };
        surface.load = function () {
            sel_offset = $selector.offset();
            width = $selector.width(), height = $selector.height();
            surface.init_data();
            vertex_sphere = surface.vertex_sphere();
            // init buffers
            hover_buffer = new Buffer();
            slice_buffer = new Buffer();
            surface_buffer = new Buffer();
            load_buffer = new Buffer();
            // create lights
            keylight = new THREE.DirectionalLight(0xf2f6ff, 0.7, 300); // surface light
            keylight.position.set(-80, 150, 80);
            backlight = new THREE.DirectionalLight(0xf2f6ff, 0.5, 500); // smile left
            backlight.position.set(-150, 100, 100);
            filllight = new THREE.DirectionalLight(0xf2f6ff, 0.5, 500); // smile right
            filllight.position.set(100, 100, 150);
            ambientlight = new THREE.AmbientLight(0xffffff);
            // setup actors / groups & create scene
            camera = new THREE.PerspectiveCamera(45, width / height, 1, 1000); /* fov, aspect, near, far */
            animation_group.add(ambientlight);
            animation_group.add(backlight);
            animation_group.add(keylight);
            animation_group.add(filllight);
            animation_group.add(surface.create_surface());
            load_buffer.add(animation_group);
            scene = new THREE.Scene();
            scene.add(animation_group);
            scene.add(camera);
            // positions & rotations
            animation_group.rotation.y = Math.PI * 0.25;
            camera.position.x = 0;
            camera.position.y = 125;
            camera.position.z = default_settings.zoom_default;
            camera.lookAt({x: 0, y: 0, z: 0});
            // render scene
            renderer = webgl ? new THREE.WebGLRenderer({antialias: true}) : new THREE.CanvasRenderer();
            renderer.setSize(width, height);
            $selector.html(renderer.domElement).find('canvas').css({position: 'relative'});
            hud.load();
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
            surface.interactive();
            if (webgl) slice.load();
            return gadget;
        };
        surface.vertex_sphere = function () {
            var sphere = new THREE.Mesh(
                new THREE.SphereGeometry(1.5, 10, 10), matlib.get_material('phong', settings.interactive_surface_color)
            );
            sphere.matrixAutoUpdate = false;
            sphere.visible = false;
            return sphere;
        };
        surface.load();
        (function animate() {
            if (settings.debug) stats.loop.update();
            if (local_settings.play === null || local_settings.play) {
                renderer.render(scene, camera);
                if (settings.debug) stats.render.update();
                if (!local_settings.stopping)
                    clearTimeout(timeout), timeout = setTimeout(function () {local_settings.play = false;}, 5000);
                local_settings.stopping = true;
            }
            animation_frame = requestAnimationFrame(animate);
        }());
    };
})();