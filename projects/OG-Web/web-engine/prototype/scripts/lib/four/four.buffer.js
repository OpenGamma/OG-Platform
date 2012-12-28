/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.Four) window.Four = {};
    if (!Detector) throw new Error('Four.Buffer requires Detector');
    /**
     * Buffer constructor
     * A buffer stores references to objects that require their webgl buffers cleared together
     */
    window.Four.Buffer = function (renderer, scene) {
        var buffer = {};
        buffer.arr = [];
        buffer.add = function (object) {
            buffer.arr.push(object);
            return object;
        };
        buffer.clear = function (custom) {
            if (!custom && !buffer.arr.length) return;
            (function dealobj (val) {
                if (typeof val === 'object') scene.remove(val);
                if ($.isArray(val)) val.forEach(function (val) {dealobj(val);});
                else if (val instanceof THREE.Mesh) {
                    if (val.geometry) val.geometry.deallocate();
                    if (val.material) {
                        if (val.material.materials) val.material.materials.forEach(function (val) {val.deallocate();});
                        else val.material.deallocate();
                    }
                }
                else if (val instanceof THREE.Texture) renderer.deallocateTexture(val);
                else if (val instanceof THREE.ParticleSystem) renderer.deallocateObject(val);
                else if (val instanceof THREE.Mesh) renderer.deallocateObject(val);
                else if (val instanceof THREE.Material) renderer.deallocateMaterial(val);
                else if (val instanceof THREE.Object3D && Detector.webgl)
                    renderer.deallocateObject(val), dealobj(val.children);
            }(custom || buffer.arr));
            if (!custom) buffer.arr = [];
        };
        return buffer;
    };
})();