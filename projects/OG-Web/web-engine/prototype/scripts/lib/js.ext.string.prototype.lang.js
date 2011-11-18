/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */

String.prototype.lang = function (bool) {
    return (og && og.common && og.common.lang) ? og.common.lang(this, bool) : this;
};