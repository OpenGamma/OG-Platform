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
        buffer.clear = function (custom, hard) {
            if (!custom && !buffer.arr.length) return;
            (function dealobj (val) {
                if (typeof val === 'object') scene.remove(val);
                if ($.isArray(val)) val.forEach(function (val) {dealobj(val);});
                else if (val instanceof THREE.ParticleSystem || val instanceof THREE.Mesh) {
                    dealobj(val.geometry);
                    // dealobj(val.texture); //  only clear on die
                    if (hard) dealobj(val.material);
                }
                else if (
                    val instanceof THREE.Geometry ||
                    val instanceof THREE.Material ||
                    val instanceof THREE.Texture
                ) val.dispose();
                else if (val instanceof THREE.Object3D && Detector.webgl) dealobj(val.children);
            scene.remove(val);
            }(custom || buffer.arr));
            if (!custom) buffer.arr = [];
        };
        return buffer;
    };
})();