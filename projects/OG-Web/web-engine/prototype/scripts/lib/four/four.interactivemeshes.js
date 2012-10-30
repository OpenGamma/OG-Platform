/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.Four) window.Four = {};
    /**
     * Keeps a tally of meshes that need to support raycasting
     */
    window.Four.InteractiveMeshes = function () {
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
})();