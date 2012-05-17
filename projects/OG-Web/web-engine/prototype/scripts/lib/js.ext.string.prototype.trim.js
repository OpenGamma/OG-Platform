/**
 * @SEE https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/String/Trim
 */

if (!String.prototype.trim) {
  String.prototype.trim = function () {
    return this.replace(/^\s+|\s+$/g,'');
  };
}