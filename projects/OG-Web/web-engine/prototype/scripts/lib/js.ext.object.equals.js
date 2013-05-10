/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 *
 * Checks the equality of two objects, does not explicitly deal with functions
 */
Object.equals = function (a, b) {
    var a_keys, b_keys, a_type, b_type;
    if (a === b) return true;
    if ((a_type = Object.prototype.toString.call(a)) !== (b_type = Object.prototype.toString.call(b))) return false;
    if (a === null || b === null) return false; // these should have matched in the primitive check
    if (a_type === '[object Array]') return a.length === b.length &&
        a.every(function (val, idx) {return Object.equals(val, b[idx]);});
    if (typeof a !== 'object' || typeof b !== 'object') return a === b;
    if ((a_keys = Object.keys(a).sort()).join() !== (b_keys = Object.keys(b).sort()).join()) return false;
    return a_keys.reduce(function (acc, key) {return acc && Object.equals(a[key], b[key]);}, true);
};