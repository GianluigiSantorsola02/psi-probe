export const inspect = function (useDoubleQuotes) {
    const specialChar = {
        '\\': '\\\\',
        '\b': '\\b',
        '\f': '\\f',
        '\n': '\\n',
        '\r': '\\r',
        '\t': '\\t',
        '\v': '\\v',
        '\'': '\\\'',
    };

    const escapeChar = (character) => {
        if (character in specialChar) {
            return specialChar[character];
        }
        return `\\u00${character.charCodeAt().toString(16).padStart(2, '0')}`;
    };

    const escapeQuotes = (string) => {
        return string.replace(/['"]/g, (quote) => '\\' + quote);
    };

    const escapedString = this.replace(/\\/g, escapeChar);

    if (useDoubleQuotes) {
        return `"${escapeQuotes(escapedString)}"`;
    } else {
        return `'${escapeQuotes(escapedString)}'`;
    }
};

