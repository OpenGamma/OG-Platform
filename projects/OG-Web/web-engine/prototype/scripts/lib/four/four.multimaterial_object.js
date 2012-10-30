/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.Four) window.Four = {};
    /**
     * Custom THREE.SceneUtils.createMultiMaterialObject, THREE's current version creates flickering
     * @param {THREE.PlaneGeometry} geometry
     * @param {Array} materials Array of THREE materials
     */
    window.Four.multimaterial_object = function (geometry, materials) {
        var i = 0, il = materials.length, group = new THREE.Object3D();
        for (; i < il; i++) {
            var object = new THREE.Mesh(geometry, materials[i]);
            object.position.y = i / 100;
            group.add(object);
        }
        return group;
    };
})();