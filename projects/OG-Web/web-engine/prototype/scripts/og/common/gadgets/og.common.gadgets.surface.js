/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.surface',
    dependencies: ['og.common.gadgets.manager', 'og.api.text'],
    obj: function () {
        var webgl = Detector.webgl ? true : false, util = {}, tmp_data, prefix = 'surface_', counter = 1,
            settings = {
                axis_offset: 1.5,
                floating_height: 5,         // how high the top surface floats over the bottom grid
                font_face_2d: 'Arial',      // 2D text font
                font_face_3d: 'helvetiker', // 3D text font (glyphs for 3D fonts need to be loaded separatly)
                font_size: 40,
                font_height: 4,             // extrusion height for 3D text
                font_color: '0x000000',     // font color for value labels
                font_color_axis_labels: '0xcccccc',
                interactive_color_nix: '0xff0000',
                interactive_color_css: '#f00',
                log: true,          // default value for log checkbox
                precision_lbl: 2,   // floating point presions for labels
                precision_hud: 3,   // floating point presions for vol display
                slice_handle_color: '0xbbbbbb',
                slice_handle_color_hover: '0x999999',
                slice_bar_color: '0xe7e7e7',
                slice_bar_color_active: '0xffbd00',
                smile_distance: 50, // distance away from the surface
                snap_distance: 3,   // mouse proximity to vertices before an interaction is approved
                surface_x: 100,     // width
                surface_z: 100,     // depth
                surface_y: 40,      // the height range of the surface
                y_segments: 10,     // number of segments to thin vol out to for smile planes
                vertex_shading_hue_min: 180,
                vertex_shading_hue_max: 0
            };
        tmp_data = {
            1: {
                vol: [
                    48.91469534705754, 45.8318374211111, 40.84562019478884, 38.603102543733854, 37.46690854016287, 35.005776740495115, 34.13202365676666, 33.76207767246494, 33.441983699602794, 33.20814034731022, 33.05551662552946, 32.905785060058065, 32.83676063826475, 32.99654198084455, 33.2475096657778, 33.347737101922746, 32.86896418821408, 31.932713989042146, 31.602015558675646, 31.833582702748224,
                    44.217041253994296, 41.86518329033477, 37.90701197923069, 36.2725189417284, 35.1636403042937, 33.33766856303285, 32.662731602492826, 32.30180612251391, 32.153070995377355, 31.948365333951983, 31.80277298214066, 31.951002331221805, 32.17207912200023, 32.08364460243179, 31.729794788065284, 31.345797830181237, 30.909252869017457, 30.80651563383117, 30.91987376009676, 31.48931987987816,
                    40.56210779567678, 38.540308756951255, 34.96739424446746, 33.99394291399909, 33.07896121195465, 31.713114451086042, 31.16933216940178, 30.944819108261214, 30.824510742460337, 30.740202231473017, 30.959943505064963, 30.815824277462646, 30.419113895685886, 30.125600588949148, 29.999494037133434, 29.96559996037962, 29.98976355041636, 30.1155294541958, 30.473168069075072, 31.148646131419937,
                    36.52549992096878, 34.910625040226684, 32.54193134813007, 31.780987326158634, 31.087029309637405, 30.13411099420029, 29.719888389851267, 29.560178686437787, 29.582476764931187, 29.640662876932712, 29.296337715533404, 29.095656159297928, 29.059402587278516, 29.07816823825296, 29.110054534003954, 29.17206922576089, 29.34332461079515, 29.625338299255144, 30.052068000958016, 31.119670876356174,
                    32.58126390238792, 31.51758184839067, 30.01229955587702, 29.611974145718012, 29.422080100690597, 28.506554775684755, 28.270320764459733, 28.29888926974069, 28.241573604749043, 28.071463201358647, 28.073538454212592, 28.119185592386636, 28.210048907071588, 28.323828996415866, 28.443583153489687, 28.56788958636875, 28.795952413371577, 29.15669045953298, 29.907443606949823, 31.310755536803413,
                    28.96795814098852, 28.363269248078392, 27.539807857456616, 27.537926898694582, 27.400569893256506, 26.950592385698762, 26.921477491900653, 26.921688268390398, 26.939635881777114, 27.020674222916817, 27.16340242055404, 27.32562457745344, 27.483138561017594, 27.638465565223434, 27.813783272721775, 28.00921505059891, 28.43366377590284, 29.073300059658045, 29.947509627675274, 31.018793492421757,
                    25.349216316583963, 25.23013334560887, 25.112198511979027, 25.50300570554272, 25.524666200465333, 25.502826898940967, 25.614902417047276, 25.71001545983247, 25.813575685394007, 26.03911360417169, 26.292538504045577, 26.590098663561747, 26.909638928386798, 27.224557989267606, 27.519630119361775, 27.789127466688868, 28.251438597455415, 28.780950413571144, 29.382835969936398, 30.15491231508969,
                    21.983007812798135, 22.23055384771756, 22.69594043763327, 23.41784327090356, 23.65021079163232, 23.991546792576354, 24.21141941448089, 24.42668027518272, 24.677259757930194, 25.192529240634414, 25.651506404813436, 26.035629413225692, 26.35409398244672, 26.622106106732378, 26.85286703376402, 27.05722072397621, 27.408405233895365, 27.839154389338503, 28.4074344756339, 29.2580830212573,
                    18.811192740781223, 19.39804306082863, 20.387185039535517, 21.455535006108338, 22.068631145737616, 22.417184416881412, 22.84790201418783, 23.25841046703947, 23.612728908067506, 24.172950633092196, 24.611351525322096, 24.974869177899368, 25.28766447211651, 25.563667187161947, 25.81162347016387, 26.037394450610112, 26.43778538555933, 26.946125363776012, 27.633123203560285, 28.65996146179972,
                    15.318913801020829, 16.34314245774855, 17.945712377501696, 19.42012610495607, 20.24934435489939, 21.0254569003275, 21.61944772834452, 22.102331285602492, 22.508470202637536, 23.164541647753616, 23.686570893455837, 24.121820828253824, 24.496053232860138, 24.824917827167994, 25.118659756723744, 25.384364818392736, 25.850812375609483, 26.433378622351405, 27.203852348728162, 28.328056828093047,
                    12.622155189358896, 13.934688975458629, 15.8787895869609, 17.45131252453414, 18.588087093475377, 19.593552692426268, 20.353853267871884, 20.976407719766122, 21.496312884152736, 22.31478641447131, 22.949453163511926, 23.469204639100933, 23.908534180950593, 24.28891665765194, 24.624497105559126, 24.924900997430242, 25.445694388864084, 26.08631641432582, 26.920449248831247, 28.116597301852902,
                    13.128704354561686, 13.41733434460995, 14.276297016982948, 16.059937778230818, 17.145787346389856, 18.334635089422704, 19.210967399951436, 19.913233392003825, 20.50182335917778, 21.447150688586508, 22.183784388689897, 22.783324367784545, 23.28735081772387, 23.721150979657292, 24.101638562068448, 24.44039946978796, 25.02348612028896, 25.7334135261908, 26.649753813237183, 27.942298738317305,
                    17.37383238056344, 16.12621282750413, 13.818643694638382, 15.084132438034386, 15.734920039034533, 17.285755263078258, 18.17304287438908, 18.883102896397265, 19.52360702910723, 20.540666747474575, 21.33546873088693, 21.998462035068464, 22.565608520947436, 23.05953419719903, 23.492088956970754, 23.875928052381763, 24.532971565386557, 25.32487181274386, 26.325736262284234, 27.708349183264367,
                    21.958155349657584, 20.091990092341234, 15.461323128307114, 14.630981820175768, 15.61844643422288, 16.057735398629248, 17.278079037202883, 17.974144815982136, 18.442230833270692, 19.52431699513662, 20.48808100896847, 21.210622797959964, 21.80723664438358, 22.3236886602791, 22.786010770400114, 23.204312972695128, 23.933683532658492, 24.82332544873818, 25.936171472168358, 27.446691587027743,
                    25.93748442573735, 23.75403065757964, 17.508435011083932, 14.323371459114831, 15.812442949284216, 15.02714798009071, 16.164351014575516, 17.242194272775105, 17.882495003168025, 18.576689334665613, 19.298997452024466, 20.202283745197562, 21.00100940292126, 21.603830961203276, 22.105978767580154, 22.543557154643263, 23.294818705868803, 24.234522283686623, 25.461256148900663, 27.124320272609943,
                    29.647334484648262, 27.161943222179968, 19.330659264244954, 14.081105970891683, 15.965838476261467, 14.257734363992784, 15.248935122585266, 16.355251762796748, 17.297733580360052, 18.27054862316136, 18.751801343920167, 19.13844194141329, 19.78108426834407, 20.5382661899298, 21.248801421100715, 21.828266666185296, 22.68449940252864, 23.661818382217696, 24.92678899304387, 26.737239022099242,
                    33.12761228987634, 30.351586829308232, 20.97296338082995, 13.893401644002001, 16.090196118535943, 13.666891508446652, 14.660902937896509, 15.914827681746601, 16.740033366419095, 18.017646861826478, 18.611235572339275, 18.95677833960281, 19.18260594504985, 19.503201770847273, 20.04424906957059, 20.683537524249683, 21.893549526007984, 23.091589397121286, 24.416765895708377, 26.28921850388062,
                    36.40799328911491, 33.35041479178037, 22.466397438433226, 13.743837469531965, 16.19292512671293, 13.206220036243518, 14.05397878680784, 15.718296043266314, 16.83490678781717, 17.660491553156085, 18.547563126994277, 18.958315159097868, 19.158142019233495, 19.33899833189238, 19.485312705380277, 19.74585926866104, 20.72296472229649, 22.32053609117948, 23.92023863671903, 25.84029674421995,
                    39.51166655111355, 36.18029500558661, 23.83414572403894, 13.621865618168682, 16.279129471774965, 12.839880439763792, 13.557260495984313, 15.26460269426967, 16.92423689766326, 17.963346294432046, 18.32122930158913, 18.981398535577814, 19.31877679230343, 19.389776096806692, 19.52771451403986, 19.617605881634724, 19.919680167829547, 21.199355550038547, 23.329172991534556, 25.421574901140527,
                    42.45738031081145, 38.85913433449903, 25.094146490034564, 13.520447631949908, 16.35244060217202, 12.543160233896385, 13.151659641249115, 14.867596343136109, 16.627712492400654, 18.843342975875473, 18.76513063298974, 18.863971441873908, 19.356227076517516, 19.67335504409258, 19.673973024969282, 19.713974690152785, 19.846710760859818, 20.321422820973726, 22.40608988405136, 25.010207673279478
                ],
                xs: ['0.1', '0.25', '0.5', '0.75', '1', '1.5', '2', '2.5', '3', '4', '5', '6', '7', '8', '9', '10', '12', '15', '20', '30'],
                xs_label: 'Term (years)',
                zs: ['0.55', '0.6', '0.65', '0.7', '0.75', '0.8', '0.85', '0.9', '0.95', '1', '1.05', '1.1', '1.15', '1.2', '1.25', '1.3', '1.35', '1.4', '1.45', '1.5'],
                zs_label: 'Strike (GBP)'
            },
            2: {
                vol: [
                    0.1596, 0.3515, 0.3292, 0.297, 0.2467,
                    0.1718, 0.3342, 0.3108, 0.2732, 0.2331,
                    0.1708, 0.3173, 0.2918, 0.2588, 0.221,
                    0.1708, 0.3016, 0.2785, 0.2449, 0.211,
                    0.1686, 0.2803, 0.2586, 0.2255, 1.797,
                    0.1651, 0.26, 0.2337, 0.21, 1.797
                ],
                xs: ['0.083', '0.25', '0.5', '1', '2'],
                xs_label: 'X Axis',
                xs_labels: ['1M', '3M', '6M', '1Y', '2Y'],
                zs: ['2', '3', '4', '5', '7', '10'],
                zs_label: 'Z Axis',
                zs_labels: ['2Y', '3Y', '4Y', '5Y', '7Y', '10Y']
            }
        };
        /**
         * Apply Natrual Log to each item in Array
         * @param {Array} arr
         * @returns {Array}
         */
        util.log = function (arr) {return settings.log ? arr.map(function (val) {return Math.log(val)}) : arr};
        /**
         * Scales an Array of numbers to a new range
         * @param {Array} arr Array to be scaled
         * @param {Number} range_min New minimum range
         * @param {Number} range_max New maximum range
         * @returns {Array}
         */
        util.scale = function (arr, range_min, range_max) {
            var min = Math.min.apply(null, arr), max = Math.max.apply(null, arr);
            return arr.map(function (val) {
                return ((val - min) / (max - min) * (range_max - range_min) + range_min)
            });
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
         * @param {Object} config
         */
        return function (config) {
            /* Temp: map fake data to config */
            config.vol = tmp_data[config.id].vol;
            config.xs = tmp_data[config.id].xs;
            config.xs_labels = tmp_data[config.id].xs_labels;
            config.xs_label = tmp_data[config.id].xs_label;
            config.zs = tmp_data[config.id].zs;
            config.zs_labels = tmp_data[config.id].zs_labels;
            config.zs_label = tmp_data[config.id].zs_label;
            var gadget = this, alive = prefix + counter++, $selector = $(config.selector), width, height,
                sel_offset, // needed to calculate mouse coordinates for raycasting
                hud = {}, matlib = {}, smile = {}, surface = {}, slice = {}, char_geometries = {},
                animation_group = new THREE.Object3D(), // everything in animation_group rotates on mouse drag
                hover_group,          // THREE.Object3D that gets created on hover and destroyed afterward
                surface_top_group,    // actual surface and anything that needs to be at that y pos
                surface_bottom_group, // the bottom grid, axis etc
                surface_group,        // full surface group, including axis
                vertex_sphere,        // the sphere displayed on vertex hover
                surface_plane,        // reference to the surface mesh
                x_segments = config.xs.length - 1, z_segments = config.zs.length - 1, y_segments = settings.y_segments,
                ys, adjusted_vol, adjusted_xs, adjusted_ys, adjusted_zs, // gadget.init_data calculates these values
                vol_max = Math.max.apply(null, config.vol), vol_min = Math.min.apply(null, config.vol),
                renderer, camera, scene, backlight, keylight, filllight, projector = new THREE.Projector(),
                stats, debug = true;
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
             * @param {String} str a single character
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
            var Handle = function () {
                var geo = new THREE.CubeGeometry(3, 1.2, 3, 2, 0, 0);
                geo.vertices // move middle vertices out to a point
                    .filter(function (val) {return (val.x === 0 && val.z === -1.5)})
                    .forEach(function (vertex) {vertex.z = -2.9});
                return new THREE.Mesh(geo, matlib.get_material('phong', settings.slice_handle_color));
            };
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
             * @returns {THREE.TextGeometry}
             */
            var Text3D = function (str, color, preserve_kerning, bevel) {
                var object = new THREE.Object3D(), xpos = 0, options = {
                    size: settings.font_size, height: settings.font_height,
                    font: settings.font_face_3d, weight: 'normal', style: 'normal',
                    bevelEnabled: bevel || false, bevelSize: 0.6, bevelThickness: 0.6
                };
                if (preserve_kerning) return new THREE.Mesh(
                    new CachedTextGeometry(str, options), matlib.get_material('phong', color)
                );
                str.split('').forEach(function (val) {
                    var text = new CachedTextGeometry(val, options),
                        mesh = new THREE.Mesh(text, matlib.get_material('phong', color));
                    mesh.position.x = xpos + (val === '.' ? 5 : 0);                                   // space before
                    xpos = xpos + ((THREE.FontUtils.drawText(val).offset)) + (val === '.' ? 10 : 15); // space after
                    mesh.matrixAutoUpdate = false;
                    mesh.updateMatrix();
                    object.add(mesh);
                });
                return object;
            };
            /**
             * Constructor for 2d text on a 3d mesh. Creates a canvas texture, applies to mesh
             * @param {String} str String you want to create
             * @param {String} color CSS hex value
             * @param {Number} size Font size in px (NOTE: when used as a texture, is relative to its usage)
             * @returns {THREE.Mesh}
             */
            var Text2D = function (str, color, size) {
                var create_texture_map = function (str) {
                    var canvas = document.createElement('canvas'), ctx = canvas.getContext('2d');
                    size = size || 50;
                    ctx.font = (size + 'px ' + settings.font_face_2d);
                    canvas.width = ctx.measureText(str).width;
                    canvas.height = Math.ceil(size * 1.25);
                    ctx.font = (size + 'px ' + settings.font_face_2d);
                    ctx.fillStyle = color || '#000';
                    ctx.fillText(str, 0, size);
                    return canvas;
                },
                create_mesh = function (str) {
                    var map = create_texture_map(str),
                        plane = new THREE.PlaneGeometry(map.width, map.height),
                        mesh = new THREE.Mesh(plane, matlib.texture(new THREE.Texture(map)));
                    mesh.material.map.needsUpdate = true;
                    mesh.doubleSided = true;
                    return mesh;
                };
                return create_mesh(str);
            };
            /**
             * Constructor for a tube.
             * THREE doesnt currently support creating a tube with a line as a path
             * (Spline is supported, but we dont want that), so we create separate tubes and add them to an object.
             * Also linewidth doest seem to work for a LineBasicMaterial, thus using tube
             * @param {Array} points Array of Vector3's
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
                        group = new THREE.Mesh(merged, material);
                        group.matrixAutoUpdate = false;
                    }
                return group;
            };
            /**
             * Tells the resize manager if the gadget is still alive
             */
            gadget.alive = function () {return !!$('.' + alive).length;};
            /**
             * Creates floor
             * @return {THREE.Object3D}
             */
            gadget.create_floor = function () {
                var plane = new THREE.PlaneGeometry(5000, 5000, 100, 100), floor;
                floor = THREE.SceneUtils.createMultiMaterialObject(plane, matlib.get_material('compound_floor_wire'));
                floor.position.y = -0.01;
                return floor;
            };
            /**
             * Scale data to fit surface dimentions, apply Log scales (to x and z) if enabled
             */
            gadget.init_data = function () {
                // adjusted data is the original data scaled to fit 2D grids width/length/height.
                // It is used to set the distance between plane segments. Then the real data is used as the values
                adjusted_vol = util.scale(config.vol, 0, settings.surface_y);
                adjusted_xs = util.scale(util.log(config.xs), -(settings.surface_x / 2), settings.surface_x / 2);
                adjusted_zs = util.scale(util.log(config.zs), -(settings.surface_z / 2), settings.surface_z / 2);
                // config.ys doesnt exist, so we need to create adjusted_ys manualy out of the given
                // vol plane range: 0 - settings.surface_y
                adjusted_ys = (function () {
                    var increment = settings.surface_y / y_segments, arr = [], i;
                    for (i = 0; i < y_segments + 1; i++) arr.push(i * increment);
                    return arr;
                }());
                // create config.ys out of vol_min and vol_max, call it ys not to confuse with config paramater
                // we are not using ys to create adjusted_ys as we want more control over adjusted_ys
                // than a standard util.scale
                ys = (function () {
                    var increment = (vol_max - vol_min) / y_segments, arr = [], i;
                    for (i = 0; i < y_segments + 1; i++)
                        arr.push((+vol_min + (i * increment)).toFixed(settings.precision_lbl));
                    return arr;
                }());
            };
            /**
             * Test if the cursor is over a mesh
             * @event {Object} mouse event object
             * @meshes {Array} meshes array of meshes to test
             * @return {Object} THREE.Ray intersects object
             */
            gadget.intersects = function (event, meshes) {
                var mouse = {x: 0, y: 0}, vector, ray;
                mouse.x = ((event.clientX - sel_offset.left) / width) * 2 - 1;
                mouse.y = -((event.clientY - sel_offset.top) / height) * 2 + 1;
                vector = new THREE.Vector3(mouse.x, mouse.y, 0.5);
                projector.unprojectVector(vector, camera);
                ray = new THREE.Ray(camera.position, vector.subSelf(camera.position).normalize());
                return ray.intersectObjects(meshes);
            };
            gadget.interactive = function () {
                var imeshes = gadget.interactive_meshes.meshes,
                    surface_hittest, handle_hittest, // store the return value of successful raycasts
                    mousedown = false, sx = 0, sy = 0, mouse_x = null, mouse_y = null,
                    hit_handle = false, rotation_enabled = true, slice_enabled = false;
                /**
                 * Populate surface_hittest and handle_hittest
                 * Trigger rotate_world and slice_handle_drag events
                 */
                $selector.on('mousemove.gadget.interactive', function (event) {
                    event.preventDefault();
                    var xlft = gadget.intersects(event, [imeshes.lft_x_handle]),
                        xrgt = gadget.intersects(event, [imeshes.rgt_x_handle]),
                        zlft = gadget.intersects(event, [imeshes.lft_z_handle]),
                        zrgt = gadget.intersects(event, [imeshes.rgt_z_handle]);
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
                    surface_hittest = gadget.intersects(event, [imeshes.surface]);
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
                    if (mousedown && slice_enabled) $selector.trigger('slice_handle_drag', event);
                });
                $selector.on('mousedown.gadget.interactive', function (event) {
                    event.preventDefault();
                    mousedown = true, sx = event.clientX, sy = event.clientY;
                    if (hit_handle) $selector.trigger('slice_handle_click');
                    $(document).on('mouseup.gadget.interactive', function () {
                        rotation_enabled = true;
                        slice_enabled = false;
                        mousedown = false;
                        $(document).off('mouse.gadget.interactive');
                    });
                });
                $selector.on('surface_over', function (event, intersects) {
                    var faces = 'abcd', i, index, vertex, vertex_world_position,
                        intersected_obj = $.isArray(intersects) ? intersects[0] : intersects,
                        object = intersected_obj.object, point = intersected_obj.point;
                    for (i = 0; i < 4; i++) { // loop through vertices
                        index = intersected_obj.face[faces.charAt(i)];
                        vertex = object.geometry.vertices[index];
                        vertex_world_position = object.matrixWorld.multiplyVector3(vertex.clone());
                        if (vertex_world_position.distanceTo(point) < settings.snap_distance) {
                            surface.hover(vertex, index, object);
                        }
                    }
                });
                $selector.on('surface_out', function () {
                    if (hover_group) surface_top_group.remove(hover_group);
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
                    slice.reset_handle_material();
                    handle_hittest[0].object.material = matlib.get_material('phong', settings.slice_handle_color_hover);
                    /**
                     * recreate surface plane
                     */
                    surface_top_group.remove(surface_plane);
                    surface_top_group.add(surface.create_surface_plane());
                    /**
                     * move handles along particles
                     */
                    (function () {
                        var intersects = gadget.intersects(original_event, [slice.intersection_plane]),
                            vertices = particles.geometry.vertices, vertex, vertex_world_position, index,
                            i = vertices.length, dist = [] /* distances from raycast/plane intersection & particles */;
                        while (i--) {
                            vertex = vertices[i];
                            vertex_world_position = particles.matrixWorld.multiplyVector3(vertices[i].clone());
                            dist[i] = vertex_world_position.distanceTo(intersects[0].point);
                        }
                        index = dist.indexOf(Math.min.apply(null, dist));
                        slice[handle_lbl + '_position'] = index;
                        slice[handle_lbl].position[axis] = vertices[index].x;
                    }());
                    /**
                     * update resize bars
                     */
                    slice.create_slice_bar(axis);
                });
                $selector.on('rotate_world', function () {
                    var dx = mouse_x - sx, dy = mouse_y - sy;
                    animation_group.rotation.y += dx * 0.01;
                    animation_group.rotation.x += dy * 0.01;
                    sx += dx, sy += dy;
                });
            };
            /**
             * Keeps a tally of meshes that need to support raycasting
             */
            gadget.interactive_meshes = {
                add: function (name, mesh) {
                    if (!gadget.interactive_meshes.meshes[name]) gadget.interactive_meshes.meshes[name] = {};
                    gadget.interactive_meshes.meshes[name] = mesh;
                },
                meshes: {},
                remove: function (name) {
                    if (name in gadget.interactive_meshes.meshes) delete gadget.interactive_meshes.meshes[name];
                }
            };
            gadget.load = function () {
                sel_offset = $selector.offset();
                width = $selector.width(), height = $selector.height();
                gadget.init_data();
                vertex_sphere = surface.vertex_sphere();
                // create lights
                keylight = new THREE.DirectionalLight(0xf2f6ff, 0.7, 300); // surface light
                keylight.position.set(-80, 150, 80);
                backlight = new THREE.DirectionalLight(0xf2f6ff, 0.5, 500); // smile left
                backlight.position.set(-150, 100, 100);
                filllight = new THREE.DirectionalLight(0xf2f6ff, 0.5, 500); // smile right
                filllight.position.set(100, 100, 150);
                // setup actors / groups & create scene
                camera = new THREE.PerspectiveCamera(45, width / height, 1, 1000); /* fov, aspect, near, far */
                animation_group.add(backlight);
                animation_group.add(keylight);
                animation_group.add(filllight);
                animation_group.add(surface.create_surface());
                scene = new THREE.Scene();
                scene.add(animation_group);
                scene.add(camera);
                // positions & rotations
                animation_group.rotation.y = Math.PI * 0.25;
                camera.position.x = 0;
                camera.position.y = 125;
                camera.position.z = 160;
                camera.lookAt({x: 0, y: 0, z: 0});
                // render scene
                renderer = webgl ? new THREE.WebGLRenderer({antialias: true}) : new THREE.CanvasRenderer();
                renderer.setSize(width, height);
                $selector.html(renderer.domElement).find('canvas').css({position: 'relative'});
                hud.load();
                // stats
                if (debug) {
                    stats = new Stats();
                    stats.domElement.style.position = 'absolute';
                    stats.domElement.style.top = '40px';
                    $(stats.domElement).appendTo($selector);
                }
                return gadget;
            };
            gadget.resize = function () {
                width = $selector.width();
                height = $selector.height();
                sel_offset = $selector.offset();
                $selector.find('> canvas').css({width: width, height: height});
                camera.aspect = width / height;
                camera.updateProjectionMatrix();
                renderer.setSize(width, height);
                hud.load();
            };
            /**
             * Updates without reloading everything
             */
            gadget.update = function () {
                gadget.init_data();
                animation_group.add(surface.create_surface());
                slice.load();
            };
            /**
             * Loads 2D overlay display with form
             */
            hud.load = function () {
                $.when(og.api.text({module: 'og.views.gadgets.surface.hud_tash'})).then(function (tmpl) {
                    var min = vol_min.toFixed(settings.precision_hud), max = vol_max.toFixed(settings.precision_hud),
                        html = (Handlebars.compile(tmpl))({min: min, max: max, alive: alive}),
                        $html = $(html).appendTo($selector);
                    hud.vol_canvas_height = height / 2;
                    if (webgl) hud.volatility($html.find('canvas')[0]);
                    else $html.find('.og-volatility').hide();
                    hud.form();
                });
            };
            /**
             * Configure surface gadget display
             */
            hud.form = function () {
                $selector
                    .find('.og-options input')
                    .prop('checked', settings.log)
                    .on('change', function () {
                        settings.log = $(this).is(':checked');
                        gadget.update();
                    });
            };
            /**
             * Set a value in the 2D volatility display
             * @param {Number} value The value to set. If value is not a number the indicator is hidden
             */
            hud.set_volatility = function (value) {
                var top = (util.scale([vol_min, value, vol_max], hud.vol_canvas_height, 0))[1],
                    css = {top: top + 'px', color: settings.interactive_color_css},
                    $vol = $selector.find('.og-val-vol');
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
                return [matlib.transparent(), matlib.wire('0xffffff')]
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
                surface_bottom_group.add(slice.intersection_plane);
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
                surface_bottom_group.remove(slice[bar_lbl]);
                slice[bar_lbl] = new SliceBar(axis);
                vertices = slice[bar_lbl].geometry.vertices;
                vertices[0].x = vertices[1].x = vertices[2].x = vertices[3].x = Math.max.apply(null, [lx, rx]);
                vertices[4].x = vertices[5].x = vertices[6].x = vertices[7].x = Math.min.apply(null, [lx, rx]);
                if (!(Math.abs(lx) + Math.abs(rx) === settings['surface_' + axis])) // change color
                    slice[bar_lbl].material = matlib.get_material('phong', settings.slice_bar_color_active);
                surface_bottom_group.add(slice[bar_lbl]);
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
                    surface_bottom_group.add(slice.x_particles);
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
                surface_bottom_group.add(slice.lft_x_handle);
                surface_bottom_group.add(slice.rgt_x_handle);
                gadget.interactive_meshes.add('lft_x_handle', slice.lft_x_handle);
                gadget.interactive_meshes.add('rgt_x_handle', slice.rgt_x_handle);
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
                    surface_bottom_group.add(slice.z_particles);
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
                surface_bottom_group.add(slice.lft_z_handle);
                surface_bottom_group.add(slice.rgt_z_handle);
                gadget.interactive_meshes.add('lft_z_handle', slice.lft_z_handle);
                gadget.interactive_meshes.add('rgt_z_handle', slice.rgt_z_handle);
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
                        mesh = THREE.SceneUtils.createMultiMaterialObject(plane, material);
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
                        mesh = THREE.SceneUtils.createMultiMaterialObject(plane, material);
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
                    x = {axis: 'x', spacing: adjusted_xs, labels: config.xs_labels || config.xs, label: config.xs_label},
                    z = {axis: 'z', spacing: adjusted_zs, labels: config.zs_labels || config.zs, label: config.zs_label},
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
                    var value;
                    for (i = 0; i < lbl_arr.length; i++) {
                        value = new Text3D(lbl_arr[i], settings.font_color);
                        value.scale.set(0.1, 0.1, 0.1);
                        if (config.axis === 'y') {
                            value.position.x = pos_arr[i] - 6;
                            value.position.z = config.right
                                ? -(THREE.FontUtils.drawText(lbl_arr[i]).offset * 0.2) + 18
                                : 2;
                            value.rotation.z = -Math.PI * .5;
                            value.rotation.x = -Math.PI * .5;
                        } else {
                            value.rotation.x = -Math.PI * .5;
                            value.position.x = pos_arr[i] - ((THREE.FontUtils.drawText(lbl_arr[i]).offset * 0.2) / 2);
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
                        axis = new THREE.Mesh(plane, matlib.texture(new THREE.Texture(canvas))),
                        labels = util.thin(config.spacing.map(function (val) {
                            // if not y axis offset half. y planes start at 0, x and z start at minus half width
                            var offset = config.axis === 'y' ? 0 : axis_len / 2;
                            return (val + offset) * 5
                        }), nth);
                    canvas.width = axis_len * 5;
                    canvas.height = 50;
                    ctx.beginPath();
                    ctx.lineWidth = 2;
                    for (i = 0; i < labels.length; i++) ctx.moveTo(labels[i] + 0.5, 25), ctx.lineTo(labels[i] + 0.5, 0);
                    ctx.moveTo(0.5, 25.5);
                    ctx.lineTo(0.5, 0.5);
                    ctx.lineTo(canvas.width - .5, 0.5);
                    ctx.lineTo(canvas.width - .5, 25.5);
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
                    mesh = THREE.SceneUtils.createMultiMaterialObject(plane, matlib.get_material('compound_grid_wire'));
                mesh.overdraw = true;
                mesh.matrixAutoUpdate = false;
                return mesh;
            };
            /**
             * Create the actual full surface object with axis. Removes any existing ones first
             * @return {THREE.Object3D}
             */
            surface.create_surface = function () {
                if (surface_group) animation_group.remove(surface_group);
                gadget.interactive_meshes.remove('surface');
                surface_group = new THREE.Object3D();
                surface_top_group = new THREE.Object3D();
                surface_bottom_group = new THREE.Object3D();
                surface_top_group.add(surface.create_surface_plane());
                if (webgl) surface_top_group.add(smile.x());
                if (webgl) surface_top_group.add(smile.z());
                if (webgl) surface_top_group.add(vertex_sphere);
                if (webgl) surface_top_group.position.y = settings.floating_height;
                if (webgl) surface_bottom_group.add(surface.create_bottom_grid());
                if (webgl) surface_bottom_group.add(smile.shadows());
                if (webgl) surface_bottom_group.add(gadget.create_floor());
                if (webgl) surface_bottom_group.add(surface.create_axes());
                surface_group.add(surface_top_group);
                if (webgl) surface_group.add(surface_bottom_group);
                return surface_group;
            };
            /**
             * Create the surface plane with vertex shading
             * @return {THREE.Object3D}
             */
            surface.create_surface_plane = function () {
                var plane = new Plane('surface'), group = new THREE.Object3D(), i;
                plane.verticesNeedUpdate = true;
                for (i = 0; i < adjusted_vol.length; i++) {plane.vertices[i].y = adjusted_vol[i];} // extrude
                var wire = THREE.GeometryUtils.clone(plane);
                plane.computeCentroids();
                plane.computeFaceNormals();
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
                            color.setHSV(hue, 1, 1);
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
                group.add(new THREE.Mesh(plane, new THREE.MeshFaceMaterial()));
                group.add(new THREE.Mesh(wire, new THREE.MeshFaceMaterial()));
                group.matrixAutoUpdate = false;
                group.updateMatrix();
                group.children.forEach(function (mesh) {
                    mesh.doubleSided = true;
                    mesh.matrixAutoUpdate = false;
                });
                gadget.interactive_meshes.add('surface', group.children[0]);
                return surface_plane = group;
            };
            /**
             * Implements any valid hover interaction with the surface.
             * Adds vertex_sphere, lines (tubes), active axis labels
             * @param {THREE.Vector3} vertex The vertex within settings.snap_distance of the mouse
             * @param {Number} index The index of the vertex
             * @param {THREE.Mesh} object The closest object to the camera that THREE.Ray returned
             */
            surface.hover = function (vertex, index, object) {
                if (hover_group) surface_top_group.remove(hover_group);
                hover_group = new THREE.Object3D();
                vertex_sphere.position.copy(vertex);
                vertex_sphere.updateMatrix();
                vertex_sphere.visible = true;
                (function () {
                    // [xz]from & [xz]to are the start and end vertex indexes for a vertex row or column
                    var x, xvertices = [], xvertices_bottom = [], z, zvertices = [], zvertices_bottom = [],
                        color = settings.interactive_color_nix,
                        xfrom = index - (index % (x_segments + 1)),
                        xto   = xfrom + x_segments + 1,
                        zfrom = index % (x_segments + 1),
                        zto   = ((x_segments + 1) * (z_segments + 1)) - (x_segments - zfrom);
                    for (x = xfrom; x < xto; x++) xvertices.push(object.geometry.vertices[x]);
                    for (z = zfrom; z < zto; z += x_segments + 1) zvertices.push(object.geometry.vertices[z]);
                    hud.set_volatility(config.vol[index]);
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
                        var lbl_arr = config[val + 's_labels'] || config[val + 's'],
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
                surface_top_group.add(hover_group);
            };
            surface.vertex_sphere = function () {
                var sphere = new THREE.Mesh(
                    new THREE.SphereGeometry(1.5, 10, 10), matlib.get_material('phong', settings.interactive_color_nix)
                );
                sphere.matrixAutoUpdate = false;
                sphere.visible = false;
                return sphere;
            };
            if (!config.child) og.common.gadgets.manager.register(gadget);
            gadget.load();
            if (webgl) gadget.interactive(), slice.load();
            (function animate() {
                requestAnimationFrame(animate);
                renderer.render(scene, camera);
                if (debug) stats.update();
            }());
        }
    }
});