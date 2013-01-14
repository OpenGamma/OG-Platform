/**
 * <strong>LICENSING INFORMATION:</strong>
 * <blockquote><pre>
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </pre></blockquote>
 * @see <a href="http://www.opengamma.com/">OpenGamma</a>
 * @see <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License, Version 2.0</a>
 * @author Alan Ayoub
 * Creates a surface instance
 * @name JSurface3D
 * @namespace JSurface3D
 * @constructor
 * @param {Object} config surface configuration object
 *     @param {String} config.selector A css selector. This is where the suface will load
 *     @param {Object} config.options Optional overwrides for default values
 *     <pre>
 *         axis_offset: 1.7,           // X and Z axis distance from the surface
 *         camera_focus_y_offset: 0,   // Move the focal point of the camera up and down
 *         debug: false,               // stats.js is required for debugging (https://github.com/mrdoob/stats.js/)
 *         floating_height: 5,         // Height the surface floats over the floor
 *         font_face_3d: 'helvetiker', // 3D text font (glyphs for 3D fonts need to be loaded separately)
 *         font_size: 35,              // 3D text font size (not in any particular units)
 *         font_height: 4,             // Extrusion height for 3D text
 *         font_color: 0x000000,       // Font color for value labels
 *         font_color_axis_labels: 0xcccccc,      // Font color for axis labels
 *         hud: true,                             // Toggle options overlay and volatility display
 *         log: false,                            // Apply natural log by default
 *         interactive_surface_color: 0xff0000,   // Highlight for interactive surface elements (in hex)
 *         interactive_hud_color: '#f00',         // Highlight colour for volatility display (in css)
 *         precision_lbl: 2,                      // Floating point precisions for labels
 *         precision_hud: 3,                      // Floating point precisions for vol display
 *         slice_handle_color: 0xbbbbbb,          // Default colour for slice handles
 *         slice_handle_color_hover: 0x999999,    // Hover colour for slice handles
 *         slice_bar_color: 0xe7e7e7,             // Default slice bar colour
 *         slice_bar_color_active: 0xffbd00,      // Active slice bar colour
 *         smile_distance: 50,                    // Distance the smile planes are from the surface
 *         snap_distance: 3,                      // Mouse proximity to vertices before an interaction is approved
 *         surface_x: 100,                        // Width
 *         surface_z: 100,                        // Depth
 *         surface_y: 40,                         // The height range of the surface
 *         texture_size: 512,                     // Texture map size for axis ticks
 *         tick_length: 20,                       // Axis tick length
 *         y_segments: 10,                        // Number of segments to thin vol out to for smile planes
 *         vertex_shading_hue_min: 180,           // vertex shading hue range min value
 *         vertex_shading_hue_max: 0,             // vertex shading hue range max value
 *         zoom_default: 160,                     // Bigger numbers are further away
 *         zoom_sensitivity: 10                   // Mouse wheel sensitivity
 *     </pre>
 *     @param {Object} config.data surface data object
 *     <pre>
 *         vol       : [],  // Surface data points
 *         xs        : [],  // X axis data points
 *         xs_labels : [],  // X axis labels (optional)
 *         xs_label  : '',  // X axis label
 *         zs        : [],  // X axis data points
 *         zs_labels : [],  // X axis labels (optional)
 *         zs_label  : ''   // X axis label
 *     </pre>
 */
(function () {
    if (!Detector) throw new Error('JSurface3D requires Detector');
    var stylesheet = '[data-og=surface]',
        default_settings = {
            axis_offset: 1.7,           // X and Z axis distance from the surface
            camera_focus_y_offset: 0,   // Move the focal point of the camera up and down
            debug: false,               // stats.js is required for debugging (https://github.com/mrdoob/stats.js/)
            floating_height: 5,         // Height the surface floats over the floor
            font_face_3d: 'helvetiker', // 3D text font (glyphs for 3D fonts need to be loaded separately)
            font_size: 30,              // 3D text font size (not in any particular units)
            font_height: 4,             // Extrusion height for 3D text
            font_color: 0x555555,       // Font color for value labels
            font_color_axis_labels: 0x555555,      // Font color for axis labels
            hud: true,                             // Toggle options overlay and volatility display
            log: false,                            // Apply natural log by default
            interactive_surface_color: 0xff0000,   // Highlight for interactive surface elements (in hex)
            interactive_hud_color: '#f00',         // Highlight colour for volatility display (in css)
            precision_lbl: 2,                      // Floating point precisions for labels
            precision_hud: 3,                      // Floating point precisions for vol display
            slice_handle_color: 0xbbbbbb,          // Default colour for slice handles
            slice_handle_color_hover: 0x999999,    // Hover colour for slice handles
            slice_bar_color: 0xe7e7e7,             // Default slice bar colour
            slice_bar_color_active: 0xffbd00,      // Active slice bar colour
            smile_distance: 50,                    // Distance the smile planes are from the surface
            snap_distance: 3,                      // Mouse proximity to vertices before an interaction is approved
            surface_x: 100,                        // Width
            surface_z: 100,                        // Depth
            surface_y: 40,                         // The height range of the surface
            texture_size: 512,                     // Texture map size for axis ticks
            tick_length: 20,                       // Axis tick length
            y_segments: 10,                        // Number of segments to thin vol out to for smile planes
            vertex_shading_hue_min: 180,           // vertex shading hue range min value
            vertex_shading_hue_max: 0,             // vertex shading hue range max value
            zoom_default: 160,                     // Bigger numbers are further away
            zoom_sensitivity: 10                   // Mouse wheel sensitivity
        };
    /** @ignore */
    window.JSurface3D = function (config) {
        var js3d = this, $selector, animation_frame, timeout, buffers, settings, stats = {},
            renderer, scene, backlight, keylight, filllight, ambientlight;
        js3d.webgl = Detector.webgl ? true : false;
        js3d.data = config.data;
        js3d.selector = $selector = $(config.selector);
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
        js3d.text3d = new Four.Text3D(js3d.matlib, js3d.settings);
        js3d.surface_world = new JSurface3D.SurfaceWorld(js3d);
        js3d.hud = new JSurface3D.Hud(js3d, $selector, stylesheet);
        js3d.vol_max = Math.max.apply(null, js3d.data.vol);
        js3d.vol_min = Math.min.apply(null, js3d.data.vol);
        js3d.groups = {animation: new THREE.Object3D()};
        /**
         * Clean up after JSurface3D. Dealocate buffers, cancel animation frame, <br />
         * removes shared stylesheet (optional)
         *
         * @function
         * @name JSurface3D.prototype.die
         * @type undefined
         * @param {Boolean} all removes shared stylesheet
         */
        js3d.die = function (all) {
            buffers.load.clear(null, true);
            cancelAnimationFrame(animation_frame);
            if (all) {$(stylesheet).remove();}
            js3d.renderer.render(js3d.scene, js3d.camera);
//            $selector.remove();
            js3d = null;
        };
        /**
         * Partialy reloads the scene. Resizes the canvas, reloads the hud, rerenderes the scene
         * @function
         * @name JSurface3D.prototype.resize
         * @type undefined
         */
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
         * Call update to rerender the surface with new data or reload the whole scene
         * @function
         * @name JSurface3D.prototype.update
         * @param {String} level What to update, the whole 'world' or just the 'surface'.
         * @param {Object} data New data for the surface, if omited the surface will update with the last data it received.
         * @type undefined
         */
        js3d.update = function (level, data) {
            js3d.surface_world.init_data(data || js3d.data);
            if (level === 'world') {
                buffers.hover.clear();
                buffers.slice.clear();
                buffers.surface.clear();
                js3d.groups.animation.add(js3d.surface_world.create_world());
                if (js3d.webgl) js3d.slice.load();
            }
            if (level === 'surface') js3d.surface_plane.update();
        };
        /** @ignore */
        js3d.load = function () {
            var animation_group = js3d.groups.animation, camera;
            js3d.scene = scene = new THREE.Scene();
            js3d.sel_offset = $selector.offset();
            js3d.width = $selector.width();
            js3d.height = $selector.height();
            js3d.vertex_sphere = new THREE.Mesh(
                new THREE.SphereGeometry(1.5, 10, 10),
                js3d.matlib.get_material('phong', settings.interactive_surface_color)
            );
            js3d.vertex_sphere.matrixAutoUpdate = false;
            js3d.vertex_sphere.visible = false;
            js3d.renderer = renderer = js3d.webgl ?
                new THREE.WebGLRenderer({antialias: true}) : new THREE.CanvasRenderer();
            renderer.setSize(js3d.width, js3d.height);
            /* buffers */
            buffers.load = new Four.Buffer(renderer, scene);
            buffers.hover = new Four.Buffer(renderer, scene);
            buffers.slice = new Four.Buffer(renderer, scene);
            buffers.surface = new Four.Buffer(renderer, scene);
            buffers.load.add(animation_group);
            /* lights */
            keylight = new THREE.DirectionalLight(0xf2f6ff, 0.7, 300);  // surface light
            backlight = new THREE.DirectionalLight(0xf2f6ff, 0.5, 500); // smile left
            filllight = new THREE.DirectionalLight(0xf2f6ff, 0.5, 500); // smile right
            ambientlight = new THREE.AmbientLight(0xffffff);
            keylight.position.set(-80, 150, 80);
            backlight.position.set(-150, 100, 100);
            filllight.position.set(100, 100, 150);
            /* init data */
            js3d.surface_world.init_data(js3d.data);
            /* animation group */
            animation_group.add(ambientlight);
            animation_group.add(backlight);
            animation_group.add(keylight);
            animation_group.add(filllight);
            animation_group.add(js3d.surface_world.create_world());
            animation_group.rotation.y = Math.PI * 0.25;
            /* camera */
            camera = js3d.camera = new THREE.PerspectiveCamera(45, js3d.width / js3d.height, 1, 1000);
            camera.position.x = 0;
            camera.position.y = 125;
            camera.position.z = settings.zoom_default;
            camera.lookAt({x: 0, y: 0 + settings.camera_focus_y_offset, z: 0});
            /* scene */
            scene.add(animation_group);
            scene.add(camera);
            /* render scene */
            $selector.html(renderer.domElement).find('canvas').css({position: 'relative'});
            js3d.hud.load();
            /* stats */
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
            js3d.surface_world.interactive();
            if (js3d.webgl) js3d.slice.load();
        };
        js3d.load();
        /** @ignore */
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