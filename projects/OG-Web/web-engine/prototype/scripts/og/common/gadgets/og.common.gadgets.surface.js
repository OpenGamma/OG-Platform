/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.surface',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        return function (config) {
            var surface = this, selector = config.selector, $selector = $(selector), group,
                x_segments = config.xs.length - 1, y_segments = config.zs.length - 1, /* surface segments */
                renderer, camera, scene, light, floor_mesh, surface_mesh;
            surface.alive = function () {return true};
            surface.resize = function () {surface.load();};
            surface.load = function () {
                var width = $selector.width(), height = $selector.height(),
                    create_floor = function () {
                    var floor = new THREE.PlaneGeometry(300, 300, x_segments, y_segments), group, materials = [
                        new THREE.MeshLambertMaterial({color: 0xffffff, shading: THREE.FlatShading}),
                        new THREE.MeshBasicMaterial({color: 0xffffff, wireframe: true, opacity: 0.5})
                    ];
                    group = THREE.SceneUtils.createMultiMaterialObject(floor, materials);
                    group.overdraw = true;
                    group.receiveShadow = true;
                    group.rotation.y = -0.7;
                    return group;
                },
                    create_surface = function () {
                    var surface = new THREE.PlaneGeometry(100, 100, x_segments, y_segments), group, materials = [
                        new THREE.MeshLambertMaterial({
                            color: 0xffffff, shading: THREE.FlatShading, vertexColors: THREE.VertexColors
                        }),
                        new THREE.MeshBasicMaterial({color: 0xffffff, wireframe: true, opacity: 0.5})
                    ];
                    for (var i = 0; i < config.vol.length; i++) surface.vertices[i].y = config.vol[i]; // extrude
                    (function () { // apply heatmap
                        var faces = 'abcd', face, color, vertex, index, i, k,
                            min = Math.min.apply(null, config.vol), max = Math.max.apply(null, config.vol),
                            color_min = 160, color_max = 0, hue;
                        for (i = 0; i < surface.faces.length; i ++) {
                            face  = surface.faces[i];
                            for (k = 0; k < 4; k++) {
                                index = face[faces.charAt(k)];
                                vertex = surface.vertices[index];
                                color = new THREE.Color(0xffffff);
                                hue = ~~((vertex.y - min) / (max - min) * (color_max - color_min) + color_min) / 360;
                                color.setHSV(hue, 1, 1);
                                face.vertexColors[k] = color;
                            }
                        }
                    }());
                    // apply surface materials,
                    // actualy duplicates the geometry and adds each material separatyle, returns the group
                    group = THREE.SceneUtils.createMultiMaterialObject(surface, materials);
                    group.children.forEach(function (mesh) {mesh.doubleSided = true;});
                    group.rotation.y = -0.7;
                    group.castShadow = true;
                    group.receiveShadow = true;
                    return group;
                };
                // create meshes
                floor_mesh = create_floor();
                surface_mesh = create_surface();
                // create light
                light = new THREE.SpotLight(0xffffff, 1);
                light.shadowCameraVisible = true;
                light.position.set(0, 500, 0);
                light.target = surface_mesh;
                // setup actors & create scene
                camera = new THREE.PerspectiveCamera(45, width / height, 1, 10000); /* fov, aspect, near, far */
                group = new THREE.Object3D();
                group.add(surface_mesh);
                group.add(floor_mesh);
                scene = new THREE.Scene();
                scene.add(group);
                scene.add(light);
                scene.add(camera);
                camera.position.x = 0;
                camera.position.y = 100;
                camera.position.z = 150;
                camera.lookAt({x: 0, y: 8, z: 0});
                // render scene
                renderer = new THREE.WebGLRenderer({antialias: true});
                renderer.setSize(width, height);
                renderer.render(scene, camera);
                $selector.html(renderer.domElement).find('canvas').css({position: 'relative'});
                return surface;
            };
            surface.animate = function () {
                var mousedown = false, sx = 0, sy = 0;
                $selector
                    .on('mousedown', function (event) {
                        mousedown = true, sx = event.clientX, sy = event.clientY;
                        $(document).on('mouseup.surface', function () {mousedown = false});
                    })
                    .on('mousemove', function (event) {
                        if (!mousedown) return;
                        var dx = event.clientX - sx, dy = event.clientY - sy;
                        group.rotation.y += dx * 0.01;
                        group.rotation.x += dy * 0.01;
                        renderer.render(scene, camera);
                        sx += dx, sy += dy;
                    });
                return surface;
            };
            if (!config.child) og.common.gadgets.manager.register(surface);
            surface.load().animate();
        }
    }
});