/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

/**
 * Usage:
 * 
 * var p = new jpasswd();
 * 
 * p.setAlpha('23456789ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz/-.');
 * p.generate(10); // result => cfn34.AZUX
 * 
 * p.checkStrong('Te.Vh/a3'); // result => true
 */
function jpasswd() {
	'use strict';
	// Default aphabet, Excluded visual similar chars 01Ol
	var DEF_ALPHABET = ("23456789" + "ABCDEFGHIJKLMNPQRSTUVWXYZ" + "abcdefghijkmnopqrstuvwxyz" + "#$!:.=+-/_");
	this.alpha = DEF_ALPHABET;
}
jpasswd.prototype.setAlpha = function(alpha) {
	'use strict';
	this.alpha = alpha;
};
jpasswd.prototype.checkStrong = function(password) {
	'use strict';
	// US-ASCII printable special chars
	var special_chars = "<[{(#$%&*?!:.,=+-_~^)}]>";
	var countMay = 0; // Mayus
	var countMin = 0; // Minus
	var countNum = 0; // Numbers
	var countEsp = 0; // Specials
	var countInv = 0; // Invalids
	var len = password.length;
	// Reset Counters
	for ( var i = 0; i < len; i++) {
		var c = password.charAt(i);
		if ((c >= 'A') && (c <= 'Z')) {
			countMay++;
		} else if ((c >= 'a') && (c <= 'z')) {
			countMin++;
		} else if ((c >= '0') && (c <= '9')) {
			countNum++;
		} else if (special_chars.indexOf(c) >= 0) {
			countEsp++;
		} else {
			countInv++;
		}
	}
	return ((password.length >= 8) && (password.length <= 28) && (countMay > 0)
			&& (countMin > 0) && (countNum > 0) && (countEsp > 0) && (countInv == 0));
}
jpasswd.prototype.generate = function(len) {
	'use strict';
	var sb = [];
	while (true) {
		var nums = false, mayus = false, minus = false, sign = false;
		var alpha = this.alpha;
		for ( var i = 0; i < len;) {
			var j = ((Math.random() * 10000) % alpha.length);
			var c = alpha.charAt(j);
			// No char repeat
			if ((i > 0) && (c == sb[i - 1])) {
				continue;
			}
			if ((c >= '0') && (c <= '9')) {
				nums = true;
			} else if ((c >= 'a') && (c <= 'z')) {
				minus = true;
			} else if ((c >= 'A') && (c <= 'Z')) {
				mayus = true;
			} else {
				// Don't begin/end with sign
				if ((i == 0) || (i == len - 1)) {
					continue;
				}
				sign = true;
			}
			sb[i++] = c;
		}
		// At least one character of each type
		if (nums && mayus && minus && sign)
			return (sb.join(''));
	}
}
