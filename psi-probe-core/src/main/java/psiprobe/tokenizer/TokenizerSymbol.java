/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package psiprobe.tokenizer;

import java.util.Objects;

/**
 * The Class TokenizerSymbol.
 */
public class TokenizerSymbol implements Comparable<Object> {

  /** The name. */
  final String name;

  /** The start text. */
  final String startText;

  /** The tail text. */
  final String tailText;

  /** The hidden. */
  final boolean hidden;

  /** To decode paired. */
  final boolean decodePaired;

  /** The enabled. */
  final boolean enabled;

  /** They can be nested. */
  final boolean canBeNested;

  /**
   * Instantiates a new tokenizer symbol.
   *
   * @param name the name
   * @param startText the start text
   * @param tailText the tail text
   * @param hidden the hidden
   * @param decodePaired to decode paired
   * @param enabled the enabled
   * @param canBeNested they can be nested
   */
  public TokenizerSymbol(String name, String startText, String tailText, boolean hidden,
      boolean decodePaired, boolean enabled, boolean canBeNested) {

    this.name = name;
    this.startText = startText;
    this.tailText = tailText;
    this.hidden = hidden;
    this.decodePaired = decodePaired;
    this.enabled = enabled;
    this.canBeNested = canBeNested;
  }

  @Override
  public int compareTo(Object obj) {
    if (obj instanceof Character) {
      return compareTo(obj);
    }
    if (obj instanceof TokenizerSymbol) {
      return compareTo(obj);
    }
    throw new IllegalArgumentException("Invalid argument type");
  }


  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.startText, this.tailText, this.hidden, this.decodePaired,
        this.enabled, this.canBeNested);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final TokenizerSymbol other = (TokenizerSymbol) obj;
    return Objects.equals(this.name, other.name) && Objects.equals(this.startText, other.startText)
        && Objects.equals(this.tailText, other.tailText) && this.hidden == other.hidden
        && this.decodePaired == other.decodePaired && this.enabled == other.enabled
        && this.canBeNested == other.canBeNested;
  }

}
