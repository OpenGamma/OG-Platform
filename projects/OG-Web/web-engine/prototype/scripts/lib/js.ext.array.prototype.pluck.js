/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * Array pluck method ... does not do any error handling right now, can be expanded if necessary
 */
Array.prototype.pluck = function (key) {return Array.prototype.map.call(this, function (val) {return val[key];});};