/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.Four) window.Four = {};
    if (!Detector) throw new Error('Four.Matlib requires Detector');
    var webgl = Detector.webgl ? true : false;
    window.Four.Matlib = function () {
        var matlib = {canvas: {}, cache: {}};
        matlib.get_material = function (material, color) {
            var name = material + '_' + color;
            if (matlib.cache[name]) return matlib.cache[name];
            matlib.cache[name] = matlib[material](typeof color === 'number' ? color : void 0);
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
            return [matlib.transparent(), matlib.wire('0x000000')];
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
            return new THREE.MeshBasicMaterial({
                map: texture, color: 0xffffff, transparent: true, side: THREE.DoubleSide
            });
        };
        matlib.vertex = function () {
            // MeshPhongMaterial Throws a WebGL Error when using VertexColors and dealocating buffers
            return new THREE.MeshBasicMaterial({
                shading: THREE.FlatShading, vertexColors: THREE.VertexColors, side: THREE.DoubleSide
            });
        };
        matlib.meshface = function () {
            return new THREE.MeshFaceMaterial();
        };
        return matlib;
    };
})();