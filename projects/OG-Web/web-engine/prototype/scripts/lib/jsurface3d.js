/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    var webgl = Detector.webgl ? true : false, stylesheet, util = {},
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
     * Buffer constructor
     * A buffer stores references to objects that requre their webgl buffers cleared together
     */
    var Buffer = function (renderer) {
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
    var Plane = function (js3d, type) {
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
     * Material Library
     */
    var Matlib = function (settings) {
        var matlib = {canvas: {}, cache: {}};
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
            ];
        };
        matlib.compound_floor_wire = function () {
            return [
                new THREE.MeshPhongMaterial({
                    ambient: 0x000000, color: 0xefefef, specular: 0xffffff, emissive: 0x000000, shininess: 10
                }),
                matlib.wire(0xcccccc)
            ];
        };
        matlib.compound_surface = function () {
            if (!webgl) return matlib.canvas.compound_surface();
            return [matlib.transparent(), matlib.vertex()];
        };
        matlib.compound_surface_wire = function () {
            if (!webgl) return matlib.canvas.compound_surface_wire();
            return [matlib.transparent(), matlib.wire(settings.surface_wire_colour)];
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
            return new THREE.MeshPhongMaterial({shading: THREE.FlatShading, vertexColors: THREE.VertexColors});
        };
        return matlib;
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
    var Text3D = function (matlib, settings) {
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
    /**
     * Constructor for a tube.
     * THREE doesnt currently support creating a tube with a line as a path
     * (Spline is supported, but we dont want that), so we create separate tubes and add them to an object.
     * Also linewidth doest seem to work for a LineBasicMaterial, thus using tube
     * @param {Array} points Array of Vector3's
     * @param {String} color hex colour
     * @return {THREE.Object3D}
     */
    var Tube = function (matlib, points, color) {
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
    var Hud = function (js3d, $selector) {
        var hud = this, settings = js3d.settings;
        /**
         * Loads 2D overlay display with form
         */
        hud.load = function () {
            if (!settings.hud) return;
            $selector.find('.js3d').remove();
            (function () {
                if ($(stylesheet).length) return;
                var css = '\
                    .js3d {bottom: 10px; left: 10px; top: 0; position: absolute;}\
                    .js3d .j-o {position: absolute; top: 10px; white-space: nowrap;}\
                    .js3d .j-o input {vertical-align: top;}\
                    .js3d .j-v {padding-top: 9px; padding-bottom: 3px; position: absolute; bottom: 0;}\
                    .js3d .j-v canvas {border: 1px solid #ccc;}\
                    .js3d .j-v span {position: absolute; left: 20px;}\
                    .js3d .j-v .j-max {top: 0;}\
                    .js3d .j-v .j-min {bottom: 0;}\
                    .js3d .j-v .j-vol {display: none; background: #eee; border: 1px solid #ddd; padding: 0 5px;}\
                    .js3d .j-v .j-vol:after {content: ""; display: block; position: absolute; \
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
                <div class="js3d">\
                  <div class="j-o"><label>Log<input type="checkbox" checked="checked" /></label></div>\
                  <div class="j-v">\
                    <span class="j-max">{{max}}</span><span class="j-min">{{min}}</span><span class="j-vol"></span>\
                    <canvas>canvas</canvas>\
                  </div>\
                </div>',
                min = js3d.vol_min.toFixed(settings.precision_hud), max = js3d.vol_max.toFixed(settings.precision_hud),
                html = tmpl.replace(/\{\{(?:max|min)\}\}/g, function (m) {return m === '{{min}}' ? min : max;}),
                $html = $(html).appendTo($selector);
            hud.vol_canvas_height = js3d.height / 2;
            if (webgl) hud.volatility($html.find('canvas')[0]);
            else $html.find('.j-v').hide();
            hud.form();
        };
        /**
         * Configure surface gadget display
         */
        hud.form = function () {
            $selector.find('.j-o input').prop('checked', js3d.local_settings.log).on('change', function () {
                js3d.local_settings.log = $(this).is(':checked');
                js3d.update();
            });
        };
        /**
         * Set a value in the 2D volatility display
         * @param {Number} value The value to set. If value is not a number the indicator is hidden
         */
        hud.set_volatility = function (value) {
            if (!settings.hud) return;
            var top = (util.scale([js3d.vol_min, value, js3d.vol_max], hud.vol_canvas_height, 0))[1],
                css = {top: top + 'px', color: settings.interactive_hud_color},
                $vol = $selector.find('.j-vol');
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
    };
    var Slice = function (js3d) {
        var slice = this, matlib = js3d.matlib, settings = js3d.settings;
        slice.lft_x_handle_position = js3d.x_segments;
        slice.rgt_x_handle_position = 0;
        slice.lft_z_handle_position = js3d.z_segments;
        slice.rgt_z_handle_position = 0;
        slice.load = function () {
            var plane = new THREE.PlaneGeometry(5000, 5000, 0, 0),
                mesh = new THREE.Mesh(plane, matlib.get_material('wire', '0xcccccc'));
            mesh.matrixAutoUpdate = false;
            mesh.updateMatrix();
            slice.intersection_plane = mesh;
            js3d.geometry_groups.surface_bottom.add(slice.intersection_plane);
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
            js3d.geometry_groups.slice.remove(slice[bar_lbl]);
            slice[bar_lbl] = new SliceBar(matlib, settings, axis);
            vertices = slice[bar_lbl].geometry.vertices;
            vertices[0].x = vertices[1].x = vertices[2].x = vertices[3].x = Math.max.apply(null, [lx, rx]);
            vertices[4].x = vertices[5].x = vertices[6].x = vertices[7].x = Math.min.apply(null, [lx, rx]);
            if (!(Math.abs(lx) + Math.abs(rx) === settings['surface_' + axis])) // change color
                slice[bar_lbl].material = matlib.get_material('phong', settings.slice_bar_color_active);
            js3d.geometry_groups.slice.add(slice[bar_lbl]);
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
                js3d.geometry_groups.surface_bottom.add(slice.x_particles = xparticles);
            }());
            /**
             * handles
             */
            slice.lft_x_handle = new Handle(matlib, settings);
            slice.lft_x_handle.position.x = -xpos;
            slice.lft_x_handle.position.z = zpos;
            slice.rgt_x_handle = new Handle(matlib, settings);
            slice.rgt_x_handle.position.x = xpos;
            slice.rgt_x_handle.position.z = zpos;
            js3d.geometry_groups.surface_bottom.add(slice.lft_x_handle);
            js3d.geometry_groups.surface_bottom.add(slice.rgt_x_handle);
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
                js3d.geometry_groups.surface_bottom.add(slice.z_particles = zparticles);
            }());
            /**
             * handles
             */
            slice.lft_z_handle = new Handle(matlib, settings);
            slice.lft_z_handle.position.x = -xpos;
            slice.lft_z_handle.position.z = -zpos;
            slice.lft_z_handle.rotation.y = -Math.PI * 0.5;
            slice.rgt_z_handle = new Handle(matlib, settings);
            slice.rgt_z_handle.position.x = -xpos;
            slice.rgt_z_handle.position.z = zpos;
            slice.rgt_z_handle.rotation.y = -Math.PI * 0.5;
            js3d.geometry_groups.surface_bottom.add(slice.lft_z_handle);
            js3d.geometry_groups.surface_bottom.add(slice.rgt_z_handle);
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
    var Smile = function (js3d) {
        var settings = js3d.settings, matlib = js3d.matlib;
        return {
            /**
             * Create x smile plane and axis
             */
            x: function () {
                var obj = new THREE.Object3D();
                (function () { // plane
                    var plane = new Plane(js3d, 'smilex'),
                        material = matlib.get_material('compound_grid_wire'),
                        mesh = util.create_multimaterial_object(plane, material);
                    mesh.rotation.x = Math.PI * 0.5;
                    mesh.position.y = settings.surface_y;
                    mesh.position.z = -((settings.surface_z / 2) + settings.smile_distance);
                    mesh.matrixAutoUpdate = false;
                    mesh.updateMatrix();
                    obj.add(mesh);
                }());
                (function () { // axis
                    var y = {axis: 'y', spacing: js3d.adjusted_ys, labels: js3d.ys, right: true},
                        y_axis = js3d.surface.create_axis(y);
                    y_axis.position.x = -(settings.surface_x / 2) - 25;
                    y_axis.position.y = 4;
                    y_axis.position.z = -(settings.surface_z / 2) - settings.smile_distance;
                    y_axis.rotation.y = Math.PI * 0.5;
                    y_axis.rotation.z = Math.PI * 0.5;
                    obj.add(y_axis);
                }());
                return obj;
            },
            /**
             * Create z smile plane and axis
             */
            z: function () {
                var obj = new THREE.Object3D();
                (function () { // plane
                    var plane = new Plane(js3d, 'smiley'),
                        material = matlib.get_material('compound_grid_wire'),
                        mesh = util.create_multimaterial_object(plane, material);
                    mesh.position.x = (settings.surface_x / 2) + settings.smile_distance;
                    mesh.rotation.z = Math.PI * 0.5;
                    mesh.matrixAutoUpdate = false;
                    mesh.updateMatrix();
                    obj.add(mesh);
                }());
                (function () { // axis
                    var y = {axis: 'y', spacing: js3d.adjusted_ys, labels: js3d.ys},
                        y_axis = js3d.surface.create_axis(y);
                    y_axis.position.y = 4;
                    y_axis.position.z = (settings.surface_z / 2) + 5;
                    y_axis.position.x = (settings.surface_x / 2) + settings.smile_distance;
                    y_axis.rotation.z = Math.PI * 0.5;
                    obj.add(y_axis);
                }());
                return obj;
            },
            /**
             * Create smile shadows, the planes float above the floor, so draw a line to show where the ground below is
             */
            shadows: function () {
                var obj = new THREE.Object3D();
                (function () { // x shadow
                    var z = settings.surface_z / 2 + settings.smile_distance, half_width = settings.surface_x / 2,
                        points = [{x: -half_width, y: 0, z: -z}, {x: half_width, y: 0, z: -z}],
                        shadow = new Tube(matlib, points, '0xaaaaaa');
                    shadow.matrixAutoUpdate = false;
                    obj.add(shadow);
                }());
                (function () { // z shadow
                    var x = settings.surface_x / 2 + settings.smile_distance, half_width = settings.surface_z / 2,
                        points = [{x: x, y: 0, z: -half_width}, {x: x, y: 0, z: half_width}],
                        shadow = new Tube(matlib, points, '0xaaaaaa');
                    shadow.matrixAutoUpdate = false;
                    obj.add(shadow);
                }());
                return obj;
            }
        };
    };
    var Surface = function (js3d) {
        var surface = this, settings = js3d.settings, data = js3d.data, matlib = js3d.matlib;
        /**
         * Creates both axes
         * @return {THREE.Object3D}
         */
        surface.create_axes = function () {
            var group = new THREE.Object3D,
                x = {axis: 'x', spacing: js3d.adjusted_xs, labels: data.xs_labels || data.xs, label: data.xs_label},
                z = {axis: 'z', spacing: js3d.adjusted_zs, labels: data.zs_labels || data.zs, label: data.zs_label},
                x_axis = surface.create_axis(x),
                z_axis = surface.create_axis(z);
            x_axis.position.z = settings.surface_z / 2 + settings.axis_offset;
            z_axis.position.x = -settings.surface_x / 2 - settings.axis_offset;
            z_axis.rotation.y = -Math.PI * 0.5;
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
                    value = js3d.text3d(n, settings.font_color);
                    value.scale.set(0.1, 0.1, 0.1);
                    if (config.axis === 'y') {
                        value.position.x = pos_arr[i] - 6;
                        value.position.z = config.right
                            ? -(THREE.FontUtils.drawText(n).offset * 0.2) + 18
                            : 2;
                        value.rotation.z = -Math.PI * 0.5;
                        value.rotation.x = -Math.PI * 0.5;
                    } else {
                        value.rotation.x = -Math.PI * 0.5;
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
                var label = js3d.text3d(config.label, settings.font_color_axis_labels, true, true);
                label.scale.set(0.2, 0.2, 0.1);
                label.rotation.x = -Math.PI * 0.5;
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
                    axis = new THREE.Mesh(plane, matlib.texture(js3d.buffers.surface.add(new THREE.Texture(canvas)))),
                    tick_stop_pos =  settings.tick_length + 0.5,
                    labels = util.thin(config.spacing.map(function (val) {
                        // if not y axis offset half. y planes start at 0, x and z start at minus half width
                        var offset = config.axis === 'y' ? 0 : axis_len / 2;
                        return (val + offset) * (settings.texture_size / axis_len);
                    }), nth);
                canvas.width = settings.texture_size;
                canvas.height = 32;
                ctx.beginPath();
                ctx.lineWidth = 2;
                for (i = 0; i < labels.length; i++)
                    ctx.moveTo(labels[i] + 0.5, tick_stop_pos), ctx.lineTo(labels[i] + 0.5, 0);
                ctx.moveTo(0.5, tick_stop_pos);
                ctx.lineTo(0.5, 0.5);
                ctx.lineTo(canvas.width - 0.5, 0.5);
                ctx.lineTo(canvas.width - 0.5, tick_stop_pos);
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
            var plane = new Plane(js3d, 'surface'),
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
            var groups = js3d.geometry_groups, buffers = js3d.buffers;
            if (groups.surface) groups.animation_group.remove(groups.surface), buffers.surface.clear();
            js3d.interactive_meshes.remove('surface');
            groups.surface = new THREE.Object3D();
            groups.slice = buffers.slice.add(new THREE.Object3D());
            groups.surface_top = new THREE.Object3D();
            groups.surface_bottom = new THREE.Object3D();
            groups.slice.add(js3d.surface_plane = surface.create_surface_plane());
            if (webgl) {
                groups.surface_top.add(js3d.smile.x());
                groups.surface_top.add(js3d.smile.z());
                groups.animation_group.add(js3d.vertex_sphere);
                groups.surface_top.position.y = js3d.settings.floating_height;
                groups.surface_bottom.add(buffers.surface.add(surface.create_bottom_grid()));
                groups.surface_bottom.add(buffers.surface.add(js3d.smile.shadows()));
                groups.surface_bottom.add(buffers.surface.add(surface.create_floor()));
                groups.surface_bottom.add(surface.create_axes());
                groups.surface.add(groups.surface_bottom);
            }
            groups.surface.add(buffers.surface.add(groups.surface_top));
            groups.surface.add(groups.slice);
            return groups.surface;
        };
        /**
         * Create the surface plane with vertex shading
         * @return {THREE.Object3D}
         */
        surface.create_surface_plane = function () {
            var settings = js3d.settings,
                plane = new Plane(js3d, 'surface'),
                group = new THREE.Object3D(), i, wire, wiremesh;
            plane.verticesNeedUpdate = true;
            for (i = 0; i < js3d.adjusted_vol.length; i++) {plane.vertices[i].y = js3d.adjusted_vol[i];} // extrude
            wire = THREE.GeometryUtils.clone(plane);
            plane.computeCentroids();
            plane.computeFaceNormals();
            plane.computeBoundingSphere();
            (function () { // apply heatmap
                if (!webgl) return;
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
            js3d.interactive_meshes.add('surface', group.children[0]);
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
                    hover_group.add(new Tube(matlib, xvertices.slice(xmin, xmax + 1), color));
                    hover_group.add(new Tube(matlib, zvertices.slice(zmin, zmax + 1), color));
                }());
                // smile z, z and y lines
                (function () {
                    var yvertices = [{x: 0, y: 0, z: vertex.z}, {x: 0, y: settings.surface_y, z: vertex.z}],
                        zlines = new Tube(matlib, zvertices, color), ylines = new Tube(matlib, yvertices, color);
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
                        xlines = new Tube(matlib, xvertices, color), ylines = new Tube(matlib, yvertices, color);
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
                    xlines = new Tube(matlib, xvertices_bottom, color);
                    zlines = new Tube(matlib, zvertices_bottom, color);
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
        surface.init_data = function () {
            var settings = js3d.settings, data = js3d.data;
            // adjusted data is the original data scaled to fit 2D grids width/length/height.
            // It is used to set the distance between plane segments. Then the real data is used as the values
            js3d.adjusted_vol = util.scale(data.vol, 0, settings.surface_y);
            js3d.adjusted_xs = util.scale(
                js3d.local_settings.log ? util.log(data.xs) : data.xs,
                -(settings.surface_x / 2), settings.surface_x / 2
            );
            js3d.adjusted_zs = util.scale(
                js3d.local_settings.log ? util.log(data.zs) : data.zs,
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
            // than a standard util.scale
            js3d.ys = (function () {
                var increment = (js3d.vol_max - js3d.vol_min) / js3d.y_segments, arr = [], i;
                for (i = 0; i < js3d.y_segments + 1; i++)
                    arr.push((+js3d.vol_min + (i * increment)).toFixed(settings.precision_lbl));
                return arr;
            }());
        };
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
                        groups.slice.remove(js3d.surface_plane);
                        groups.slice.add(js3d.surface_plane = surface.create_surface_plane());
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
        surface.vertex_sphere = function () {
            var sphere = new THREE.Mesh(
                new THREE.SphereGeometry(1.5, 10, 10), matlib.get_material('phong', settings.interactive_surface_color)
            );
            sphere.matrixAutoUpdate = false;
            sphere.visible = false;
            return sphere;
        };
    };
    /**
     * Keeps a tally of meshes that need to support raycasting
     */
    var InteractiveMeshes = function () {
        return {
            add: function (name, mesh) {
                if (!this.meshes[name]) this.meshes[name] = {};
                this.meshes[name] = mesh;
            },
            meshes: {},
            remove: function (name) {
                if (name in this.meshes) delete this.meshes[name];
            }
        };
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
        var js3d = this, $selector = $(config.selector), stats = {}, animation_frame, timeout, buffers, settings,
            renderer, scene, backlight, keylight, filllight, ambientlight;
        js3d.data = config.data;
        js3d.vol_max = Math.max.apply(null, js3d.data.vol);
        js3d.vol_min = Math.min.apply(null, js3d.data.vol);
        js3d.selector = $selector;
        js3d.settings = settings = $.extend({}, default_settings, config.options);
        js3d.hud = new Hud(js3d, $selector);
        js3d.matlib = new Matlib(settings);
        js3d.projector = new THREE.Projector();
        js3d.x_segments = js3d.data.xs.length - 1;
        js3d.z_segments = js3d.data.zs.length - 1;
        js3d.y_segments = settings.y_segments;
        js3d.local_settings = {log: true, play: null, stopping: false};
        js3d.interactive_meshes = new InteractiveMeshes();
        js3d.buffers = buffers = {};
        js3d.width = null;
        js3d.height = null;
        js3d.camera = null;
        js3d.sel_offset = null;
        js3d.surface_plane = null;
        js3d.slice = new Slice(js3d);
        js3d.smile = new Smile(js3d);
        js3d.text3d = new Text3D(js3d.matlib, js3d.settings);
        js3d.surface = new Surface(js3d);
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
            js3d.geometry_groups.animation_group.add(js3d.surface.create_surface());
            if (webgl) js3d.slice.load();
        };
        js3d.load = function () {
            var animation_group = js3d.geometry_groups.animation_group, camera;
            js3d.sel_offset = $selector.offset();
            js3d.width = $selector.width(), js3d.height = $selector.height();
            js3d.surface.init_data();
            js3d.vertex_sphere = js3d.surface.vertex_sphere();
            renderer = webgl ? new THREE.WebGLRenderer({antialias: true}) : new THREE.CanvasRenderer();
            renderer.setSize(js3d.width, js3d.height);
            // buffers
            buffers.load = new Buffer(renderer);
            buffers.hover = new Buffer(renderer);
            buffers.slice = new Buffer(renderer);
            buffers.surface = new Buffer(renderer);
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
            animation_group.add(js3d.surface.create_surface());
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