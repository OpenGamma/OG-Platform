/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.Four) window.Four = {};
    /**
     * Scales an Array of numbers to a new range
     * @param {Array} arr Array to be scaled
     * @param {Number} range_min New minimum range
     * @param {Number} range_max New maximum range
     * @returns {Array}
     */
    window.Four.scale = function (arr, range_min, range_max) {
        var min = Math.min.apply(null, arr), max = Math.max.apply(null, arr);
        return arr.map(function (val) {return ((val - min) / (max - min) * (range_max - range_min) + range_min);});
    };
})();