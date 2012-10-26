/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!Detector) throw new Error('JSurface3D requires Detector');
    var stylesheet,
        default_settings = {
            axis_offset: 1.7,           // X and Z axis distance from the surface
            debug: false,               // Stats.js is required for debugging (https://github.com/mrdoob/stats.js/)
            floating_height: 5,         // Height the surface floats over the floor
            font_face_3d: 'helvetiker', // 3D text font (glyphs for 3D fonts need to be loaded separately)
            font_size: 35,              // 3D text font size (not in any particular units)
            font_height: 4,             // Extrusion height for 3D text
            font_color: '0x000000',     // Font color for value labels
            font_color_axis_labels: '0xcccccc',   // Font color for axis labels
            hud: true,                            // Toggle options overlay and volatility display
            log: false,                           // Apply natural log by default
            interactive_surface_color: '0xff0000',// Highlight for interactive surface elements (in hex)
            interactive_hud_color: '#f00',        // Highlight colour for volatility display (in css)
            precision_lbl: 2,                     // Floating point precisions for labels
            precision_hud: 3,                     // Floating point precisions for vol display
            slice_handle_color: '0xbbbbbb',       // Default colour for slice handles
            slice_handle_color_hover: '0x999999', // Hover colour for slice handles
            slice_bar_color: '0xe7e7e7',          // Default slice bar colour
            slice_bar_color_active: '0xffbd00',   // Active slice bar colour
            smile_distance: 50,                   // Distance the smile planes are from the surface
            snap_distance: 3,                     // Mouse proximity to vertices before an interaction is approved
            surface_x: 100,                       // Width
            surface_z: 100,                       // Depth
            surface_y: 40,                        // The height range of the surface
            texture_size: 512,                    // Texture map size for axis ticks
            tick_length: 20,                      // Axis tick length
            y_segments: 10,                       // Number of segments to thin vol out to for smile planes
            vertex_shading_hue_min: 180,          // vertex shading hue range min value
            vertex_shading_hue_max: 0,            // vertex shading hue range max value
            zoom_default: 160,                    // Bigger numbers are further away
            zoom_sensitivity: 10                  // Mouse wheel sensitivity
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
        js3d.smile = new JSurface3D.Smile(js3d);
        js3d.text3d = new Four.Text3D(js3d.matlib, js3d.settings);
        js3d.surface_world = new JSurface3D.SurfaceWorld(js3d);
        js3d.hud = new JSurface3D.Hud(js3d, $selector, stylesheet);
        js3d.vol_max = Math.max.apply(null, js3d.data.vol);
        js3d.vol_min = Math.min.apply(null, js3d.data.vol);
        /**
         * Geometry Groups
         *
         * animation:   // everything in animation rotates on mouse drag
         * hover:             // THREE.Object3D that gets created on hover and destroyed afterward
         * surface_top:       // actual surface and anything that needs to be at that y pos
         * surface_bottom:    // the bottom grid, axis etc
         * surface:           // full surface group, including axis
         * slice:             // geometry used when slicing
         */
        js3d.groups = {animation: new THREE.Object3D()};
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
         * @param {String} str 'world' or 'surface'
         * @param {Object} data
         */
        js3d.update = function (str, data) {
            js3d.surface_world.init_data(data || js3d.data);
            if (str === 'world') {
                js3d.groups.animation.add(js3d.surface_world.create_world());
                if (js3d.webgl) js3d.slice.load();
            }
            if (str === 'surface') js3d.surface_plane.update();
        };
        js3d.load = function () {
            var animation_group = js3d.groups.animation, camera;
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
            // init data
            js3d.surface_world.init_data(js3d.data);
            // animation group
            animation_group.add(ambientlight);
            animation_group.add(backlight);
            animation_group.add(keylight);
            animation_group.add(filllight);
            animation_group.add(js3d.surface_world.create_world());
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
            js3d.surface_world.interactive();
            if (js3d.webgl) js3d.slice.load();
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