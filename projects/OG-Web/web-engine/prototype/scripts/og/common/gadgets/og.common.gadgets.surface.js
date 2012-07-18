/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.surface',
    dependencies: ['og.common.gadgets.manager', 'og.api.text'],
    obj: function () {
        var webgl = Detector.webgl ? true : false, util = {}, matlib = {}, tmp_data,
            settings = {
                floating_height: 5, // how high does the top surface float over the bottom grid
                font_face: 'Arial',  // 2D text font (glyphs for 3D fonts need to be loaded separatly)
                font_size: 40,
                font_size_axis_labels: 70,
                font_size_interactive_labels: 40,
                font_height: 2,                    // extrusion height for 3D font value labels
                font_height_axis_labels: 4,        // extrusion height for 3D font axis labels
                font_height_interactive_labels: 5, // extrusion height for 3D font interactive labels
                font_color: '0x000000',            // font color for value labels
                font_color_axis_labels: '0xcccccc',
                interactive_color_nix: '0xff0000',
                interactive_color_css: '#f00',
                log: true,          // default value for log checkbox
                precision_lbl: 2,   // floating point presions for vol display
                precision_hud: 3,   // floating point presions for vol display
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
         * Material Library
         */
        matlib.canvas = {};
        matlib.canvas.compound_surface = function () {
            return [matlib.canvas.flat('0xcccccc'), matlib.canvas.wire('0xeeeeee')];
        };
        matlib.canvas.flat = function (color) {
            return new THREE.MeshLambertMaterial({color: color, shading: THREE.FlatShading});
        };
        matlib.canvas.wire = function (color) {
            return new THREE.MeshBasicMaterial({color: color || 0xcccccc, wireframe: true});
        };
        matlib.compound_dark_wire = function () {
            return [
                new THREE.MeshPhongMaterial({
                    // color represents diffuse in THREE.MeshPhongMaterial
                    ambient: 0x000000, color: 0xeeeeee, specular: 0xdddddd, emissive: 0x000000, shininess: 0
                }),
                matlib.wire(0xcccccc)
            ]
        };
        matlib.compound_light_wire = function () {
            return [
                new THREE.MeshPhongMaterial({
                    ambient: 0x000000, color: 0xefefef, specular: 0xffffff, emissive: 0x000000, shininess: 10
                }),
                matlib.wire(0xcccccc)
            ]
        };
        matlib.compound_surface = function () {
            if (!webgl) return matlib.canvas.compound_surface();
            return [matlib.vertex(), matlib.wire('0xffffff')]
        };
        matlib.wire = function (color) {
            if (!webgl) return matlib.canvas.wire(color);
            return new THREE.MeshBasicMaterial({color: color || 0x999999, wireframe: true});
        };
        matlib.flat = function (color) {
            if (!webgl) return matlib.canvas.flat(color);
            return new THREE.MeshBasicMaterial({color: color, wireframe: false});
        };
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
         * Apply Natrual Log to each item in Array
         * @param {Array} arr
         * @returns {Array}
         */
        util.log = function (arr) {return settings.log ? arr.map(function (val) {return Math.log(val)}) : arr};
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
            var gadget = this, hud = {}, smile = {}, surface = {}, selector = config.selector, $selector = $(selector),
                animation_group = new THREE.Object3D(), // everything in animation_group rotates with mouse drag
                hover_group,          // THREE.Object3D that gets created on hover and destroyed afterward
                width, height,        // selector / canvas width and height
                surface_top_group,    // actual surface and anything that needs to be at that y pos
                surface_bottom_group, // the bottom grid, axis etc
                surface_group,        // full surface group, including axis
                vertex_sphere,        // the sphere displayed on vertex hover
                x_segments = config.xs.length - 1, z_segments = config.zs.length - 1, y_segments = settings.y_segments,
                vol_max = (Math.max.apply(null, config.vol)),
                vol_min = (Math.min.apply(null, config.vol)),
                renderer, camera, scene, backlight, keylight, filllight,
                ys, adjusted_vol, adjusted_xs, adjusted_ys, adjusted_zs; // call gadget.init_data to setup
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
             * Constructor for 3D text
             * @param {String} str String you want to create
             * @returns {THREE.TextGeometry}
             */
            var Text3D = function (str, color, size, height) {
                var text = new THREE.TextGeometry(str, {
                    size: size, height: height || 10, font: 'helvetiker', weight: 'normal', style: 'normal'
                });
                return new THREE.Mesh(text, matlib.phong(color));
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
                    ctx.font = (size + 'px ' + settings.font_face);
                    canvas.width = ctx.measureText(str).width;
                    canvas.height = Math.ceil(size * 1.25);
                    ctx.font = (size + 'px ' + settings.font_face);
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
            var Tube = function (points) {
                var mesh = new THREE.Object3D(), i = points.length - 1, line, tube;
                while (i--) {
                    line = new THREE.LineCurve3(points[i], points[i+1]);
                    tube = new THREE.TubeGeometry(line, 1, 0.2, 5, false, false);
                    mesh.add(new THREE.Mesh(tube, matlib.flat(settings.interactive_color_nix)));
                }
                return mesh;
            };
            /**
             * Tells the resize manager if the gadget is still alive
             * TODO: set this up
             */
            gadget.alive = function () {return true};
            /**
             * Rotate the (world) group on mouse drag
             */
            gadget.animate = function () {
                var mousedown = false, sx = 0, sy = 0;
                $selector
                    .on('mousedown', function (event) {
                        mousedown = true, sx = event.clientX, sy = event.clientY;
                        $(document).on('mouseup.gadget.animate', function () {
                            mousedown = false;
                            $(document).off('mouseup.gadget.animate');
                        });
                    })
                    .on('mousemove.gadget.animate', function (event) {
                        if (!mousedown) return;
                        var dx = event.clientX - sx, dy = event.clientY - sy;
                        animation_group.rotation.y += dx * 0.01;
                        animation_group.rotation.x += dy * 0.01;
                        renderer.render(scene, camera);
                        sx += dx, sy += dy;
                    });
                return gadget;
            };
            /**
             * Creates floor
             * @return {THREE.Object3D}
             */
            gadget.create_floor = function () {
                var plane = new THREE.PlaneGeometry(5000, 5000, 100, 100), floor;
                floor = THREE.SceneUtils.createMultiMaterialObject(plane, matlib.compound_light_wire());
                floor.position.y = -0.01;
                return floor;
            };
            /**
             * Scale data to fit surface dimentions, apply Log scales if enabled
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
             * Keeps a tally of meshes that need to support raycasting
             */
            gadget.interactive_meshes = {
                add: function (name, mesh) {
                    var obj = {};
                    obj[name] = mesh;
                    gadget.interactive_meshes.obj_arr.push(obj);
                    gadget.interactive_meshes.meshes.push(mesh);
                },
                meshes: [],  // meshes array
                obj_arr: [], // array of objects {name: mesh}
                remove: function (name) {
                    gadget.interactive_meshes.obj_arr.forEach(function (val, i) {
                        if (!(name in val)) return;
                        gadget.interactive_meshes.obj_arr.splice(i, 1);
                        gadget.interactive_meshes.meshes.splice(i, 1);
                    });
                }
            };
            gadget.load = function () {
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
                smile.load();
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
                renderer.render(scene, camera);
                $selector.html(renderer.domElement).find('canvas').css({position: 'relative'});
                hud.load();
                return gadget;
            };
            gadget.resize = function () {
                width = $selector.width();
                height = $selector.height();
                $selector.find('> canvas').css({width: width, height: height});
                camera.aspect = width / height;
                camera.updateProjectionMatrix();
                renderer.setSize(width, height);
                renderer.render(scene, camera);
                hud.load();
                smile.load();
            };
            /**
             * Updates without reloading everything
             */
            gadget.update = function () {
                gadget.init_data();
                animation_group.add(surface.create_surface());
                smile.load();
            };
            /**
             * Loads 2D overlay display with form
             */
            hud.load = function () {
                $.when(og.api.text({module: 'og.views.gadgets.surface.hud_tash'})).then(function (tmpl) {
                    var min = vol_min.toFixed(settings.precision_hud), max = vol_max.toFixed(settings.precision_hud),
                        html = (Handlebars.compile(tmpl))({min: min, max: max});
                    hud.vol_canvas_height = height / 2;
                    hud.volatility($(html).appendTo($selector).find('canvas')[0]);
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
            smile.load = function () {
                smile.x();
                smile.z();
            };
            smile.x = function () {
                (function () { // plane
                    var plane = new Plane('smilex'),
                        mesh = THREE.SceneUtils.createMultiMaterialObject(plane, matlib.compound_dark_wire());
                    mesh.rotation.x = Math.PI * 0.5;
                    mesh.position.y = settings.surface_y;
                    mesh.position.z = -((settings.surface_z / 2) + settings.smile_distance);
                    surface_top_group.add(mesh);
                }());
                (function () { // axis
                    var y = {axis: 'y', spacing: adjusted_ys, labels: ys, right: true}, y_axis = surface.create_axis(y);
                    y_axis.position.x = -(settings.surface_x / 2) - 25;
                    y_axis.position.y = 4;
                    y_axis.position.z = -(settings.surface_z / 2) - settings.smile_distance;
                    y_axis.rotation.y = Math.PI * .5;
                    y_axis.rotation.z = Math.PI * .5;
                    surface_top_group.add(y_axis);
                }());
            };
            smile.z = function () {
                (function () { // plane
                    var plane = new Plane('smiley'),
                        mesh = THREE.SceneUtils.createMultiMaterialObject(plane, matlib.compound_dark_wire());
                    mesh.position.x = (settings.surface_x / 2) + settings.smile_distance;
                    mesh.rotation.z = Math.PI * 0.5;
                    surface_top_group.add(mesh);
                }());
                (function () { // axis
                    var y = {axis: 'y', spacing: adjusted_ys, labels: ys}, y_axis = surface.create_axis(y);
                    y_axis.position.y = 4;
                    y_axis.position.z = (settings.surface_z / 2) + 5;
                    y_axis.position.x = (settings.surface_x / 2) + settings.smile_distance;
                    y_axis.rotation.z = Math.PI * .5;
                    surface_top_group.add(y_axis);
                }());
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
                x_axis.position.z = settings.surface_z / 2;
                z_axis.position.x = -settings.surface_x / 2;
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
                var mesh = new THREE.Object3D(), i, nth = Math.ceil(config.spacing.length / 6), scale = '0.1',
                    lbl_arr = util.thin(config.labels, nth), pos_arr = util.thin(config.spacing, nth),
                    axis_len = settings['surface_' + config.axis];
                (function () { // axis values
                    var value;
                    for (i = 0; i < lbl_arr.length; i++) {
                        value = new Text3D(lbl_arr[i], settings.font_color, settings.font_size, settings.font_height);
                        value.scale.set(scale, scale, scale);
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
                        mesh.add(value);
                    }
                }());
                (function () { // axis label
                    if (!config.label) return;
                    var label = new Text3D(config.label, settings.font_color_axis_labels,
                        settings.font_size_axis_labels, settings.font_height_axis_labels);
                    label.scale.set(scale, scale, scale);
                    label.rotation.x = -Math.PI * .5;
                    label.position.x = -(axis_len / 2) -3;
                    label.position.y = 1;
                    label.position.z = 25;
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
                    mesh = THREE.SceneUtils.createMultiMaterialObject(plane, matlib.compound_dark_wire());
                mesh.overdraw = true;
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
                surface_top_group.add(vertex_sphere);
                surface_top_group.position.y = settings.floating_height;
                surface_bottom_group.add(surface.create_bottom_grid());
                if (webgl) surface_bottom_group.add(gadget.create_floor());
                if (webgl) surface_bottom_group.add(surface.create_axes());
                surface_group.add(surface_top_group);
                surface_group.add(surface_bottom_group);
                return surface_group;
            };
            /**
             * Create the surface plane with vertex shading
             * @return {THREE.Object3D}
             */
            surface.create_surface_plane = function () {
                var plane = new Plane('surface'), group, i;
                plane.verticesNeedUpdate = true;
                for (i = 0; i < adjusted_vol.length; i++) {plane.vertices[i].y = adjusted_vol[i];} // extrude
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
                // apply surface materials,
                // actualy duplicates the geometry and adds each material separatyle, returns the group
                group = THREE.SceneUtils.createMultiMaterialObject(plane, matlib.compound_surface());
                group.children.forEach(function (mesh) {mesh.doubleSided = true;});
                gadget.interactive_meshes.add('surface', group.children[0]);
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
                if (hover_group) surface_top_group.remove(hover_group);
                hover_group = new THREE.Object3D();
                vertex_sphere.position.copy(vertex);
                vertex_sphere.visible = true;
                (function () {
                    // [xz]from & [xz]to are the start and end vertex indexes for a vertex row or column
                    var x, xvertices = [], xvertices_bottom = [], z, zvertices = [], zvertices_bottom = [],
                        xfrom = index - (index % (x_segments + 1)),
                        xto   = xfrom + x_segments + 1,
                        zfrom = index % (x_segments + 1),
                        zto   = ((x_segments + 1) * (z_segments + 1)) - (x_segments - zfrom);
                    for (x = xfrom; x < xto; x++) xvertices.push(object.geometry.vertices[x]);
                    for (z = zfrom; z < zto; z += x_segments + 1) zvertices.push(object.geometry.vertices[z]);
                    hud.set_volatility(config.vol[index]);
                    // top lines
                    hover_group.add(new Tube(xvertices));
                    hover_group.add(new Tube(zvertices));
                    // bottom lines
                    xvertices_bottom.push(xvertices[0].clone(), xvertices[xvertices.length-1].clone());
                    xvertices_bottom[0].y = xvertices_bottom[1].y = -settings.floating_height;
                    zvertices_bottom.push(zvertices[0].clone(), zvertices[zvertices.length-1].clone());
                    zvertices_bottom[0].y = zvertices_bottom[1].y = -settings.floating_height;
                    hover_group.add(new Tube(xvertices_bottom));
                    hover_group.add(new Tube(zvertices_bottom));
                    // labels
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
                        lbl = new Text3D(txt, settings.interactive_color_nix,
                            settings.font_size_interactive_labels, settings.font_height_interactive_labels);
                        vertices = val === 'x' ? zvertices : xvertices;
                        group.add(lbl);
                        // create box
                        (function () {
                            var txt_width = THREE.FontUtils.drawText(txt).offset, height = 60,
                                box_width = txt_width * 3,
                                box = new THREE.CubeGeometry(box_width, height, 4, 4, 1, 1),
                                mesh = new THREE.Mesh(box, matlib.phong('0xdddddd'));
                            mesh.position.x = (box_width / 2) - (txt_width / 2);
                            mesh.position.y = 20;
                            // create the tail by moving the 2 center vertices closes to the surface
                            mesh.geometry.vertices.filter(function (val) {
                                return (val.x === 0 && val.y === height / 2)
                            }).forEach(function (vertex) {vertex.y = height});
                            group.add(mesh);
                        }());
                        // position / rotation
                        group.scale.set(scale, scale, scale);
                        group.position.y = -settings.floating_height + 1.1;
                        group.rotation.x = -Math.PI * .5;
                        if (val === 'x') {
                            group.position.x = vertices[0][val] - offset;
                            group.position.z = (settings.surface_z / 2) + 13;
                        }
                        if (val === 'z') {
                            group.position.x = -((settings.surface_x / 2) + 12);
                            group.position.z = vertices[0][val] - offset;
                            group.rotation.z = -Math.PI * .5;
                        }
                        hover_group.add(group);
                    });
                }());
                surface_top_group.add(hover_group);
            };
            /**
             * On mouse move determin if the mouse position, translated to 3D space, is within settings.snap_distance
             * from any vertex on the surface, if so, call surface.hover, otherwise remove the hover group
             * @return {THREE.Object3D} return the surface to allow method chaining
             */
            surface.interactive = function () {
                var mouse = {x: 0, y: 0}, intersected, projector = new THREE.Projector();
                $selector.on('mousemove.surface.interactive', function (event) {
                    event.preventDefault();
                    var vector, ray, intersects, offset = $selector.offset(),
                        object, point, faces = 'abcd', i, index, vertex, vertex_world_position;
                    mouse.x = ((event.clientX - offset.left) / width) * 2 - 1;
                    mouse.y = -((event.clientY - offset.top) / height) * 2 + 1;
                    vector = new THREE.Vector3(mouse.x, mouse.y, 0.5);
                    projector.unprojectVector(vector, camera);
                    ray = new THREE.Ray(camera.position, vector.subSelf(camera.position).normalize());
                    intersects = ray.intersectObjects(gadget.interactive_meshes.meshes);
                    if (intersects.length > 0) { // intersecting at least one object
                        point = intersects[0].point, object = intersects[0].object;
                        for (i = 0; i < 4; i++) { // loop through vertices
                            index = intersects[0].face[faces.charAt(i)];
                            vertex = object.geometry.vertices[index];
                            vertex_world_position = object.matrixWorld.multiplyVector3(vertex.clone());
                            if (vertex_world_position.distanceTo(point) < settings.snap_distance) {
                                surface.hover(vertex, index, object);
                            }
                        }
                    } else { // not intersecting
                        if (hover_group) surface_top_group.remove(hover_group);
                        vertex_sphere.visible = false;
                        intersected = null;
                        hud.set_volatility();
                    }
                    renderer.render(scene, camera);
                });
                return surface;
            };
            surface.vertex_sphere = function () {
                var sphere = new THREE.Mesh(
                    new THREE.SphereGeometry(1.5, 10, 10), matlib.phong(settings.interactive_color_nix)
                );
                sphere.visible = false;
                return sphere;
            };
            if (!config.child) og.common.gadgets.manager.register(surface);
            gadget.load().animate();
            surface.interactive();
        }
    }
});