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
package psiprobe.jsp;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * The Class VisualScoreTag.
 */
public class VisualScoreTag extends BodyTagSupport {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -5653846466205838602L;


  /** The Constant WHITE_RIGHT_BORDER. */
  private static final String WHITE_RIGHT_BORDER = "b0";

  /** The Constant RED_RIGHT_BORDER. */
  private static final String RED_RIGHT_BORDER = "b1";

  /** The Constant BLUE_RIGHT_BORDER. */
  private static final String BLUE_RIGHT_BORDER = "b2";

  /** Red value. */
  private double value = 0;

  /** Blue value. */
  private double value2 = 0;

  /**
   * Number of parts in one block.<br>
   * It always must be 5 to match the 5 different gifs available at
   * /src/main/webapp/css/classic/gifs
   */
  private int partialBlocks = 1;

  /**
   * Total number of blocks.<br>
   * fullBlocks + 2 img elements will be added to the page.<br>
   * The plus 2 is due the left and right border.
   */
  private int fullBlocks = 5;

  /** The show empty blocks. */
  private boolean showEmptyBlocks;

  /** The show a. */
  private boolean showA;

  /** The show b. */
  private boolean showB;

  @Override
  public int doAfterBody() throws JspException {
    try (BodyContent bc = getBodyContent()) {
      String body = bc.getString().trim();

      String buf = calculateSuffix(body);

      bc.getEnclosingWriter().print(buf);
    } catch (IOException e) {
      throw new JspException("Exception while writing to client", e);
    }

    return SKIP_BODY;
  }

  /**
   * Calculate suffix.
   *
   * @param body the body
   *
   * @return the string buffer
   */
  String calculateSuffix(String body) {
    double minValue = 0;
    double maxValue = 100;

    // Ensure value is within bounds
    value = Math.max(minValue, Math.min(value, maxValue));
    value2 = Math.max(0, Math.min(value2, maxValue - value));

    double unitSize = (maxValue - minValue) / (fullBlocks * partialBlocks);
    double blockWidth = unitSize * partialBlocks;

    int redWhole = (int) Math.floor(value / blockWidth);
    int redPart = (int) Math.floor((value - redWhole * blockWidth) / unitSize);
    int bluePart1 = redPart > 0 ? Math.min((int) Math.floor(value2 / unitSize), partialBlocks - redPart) : 0;
    int blueWhole = (int) Math.max(0, Math.ceil(value2 / blockWidth) - (redPart > 0 ? 1 : 0));
    int bluePart2 = (int) Math.floor((value2 - blueWhole * blockWidth - bluePart1 * unitSize) / unitSize);

    StringBuilder buf = new StringBuilder();

    // Beginning
    appendFormattedBody(buf, body, showA, redWhole, redPart, blueWhole, bluePart2);

    // Full red blocks
    appendRepeatedBody(buf, body, redWhole, partialBlocks + "+0");

    // Mixed red/blue block (mid-block transition)
    if (redPart > 0) {
      appendFormattedBody(buf, body, true, 1, redPart, 0, bluePart1);
    }

    // Full blue blocks
    appendRepeatedBody(buf, body, blueWhole, "0+" + partialBlocks);

    // Partial blue block
    if (bluePart2 > 0) {
      appendFormattedBody(buf, body, true, 1, 0, 0, bluePart2);
    }

    // Empty blocks
    int emptyBlocks = 0;

    if (showEmptyBlocks) {
      int redPartResult = (redPart > 0) ? 1 : 0;
      int bluePart2Result = (bluePart2 > 0) ? 1 : 0;

      emptyBlocks = fullBlocks - (redWhole + blueWhole + redPartResult + bluePart2Result);
    }

    appendRepeatedBody(buf, body, emptyBlocks, "0+0");

    // End
    appendFormattedBody(buf, body, showB, redWhole, redPart, blueWhole, bluePart2);

    return buf.toString();
  }

  private void appendFormattedBody(StringBuilder buf, String body, boolean show, int redWhole, int redPart, int blueWhole, int bluePart) {
    if (show) {
      String format = determineBorderFormat(redWhole, redPart, blueWhole, bluePart);
      buf.append(MessageFormat.format(body, format));
    }
  }

  private void appendRepeatedBody(StringBuilder buf, String body, int repeatCount, String format) {
    String repeatedBody = MessageFormat.format(body, format);
    for (int i = 0; i < repeatCount; i++) {
      buf.append(repeatedBody);
    }
  }

  private String determineBorderFormat(int redWhole, int redPart, int blueWhole, int bluePart) {
    if (redWhole == fullBlocks) {
      return RED_RIGHT_BORDER;
    } else if (redWhole + (redPart + bluePart == partialBlocks ? 1 : 0) + blueWhole == fullBlocks) {
      return BLUE_RIGHT_BORDER;
    } else {
      return WHITE_RIGHT_BORDER;
    }
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  public double getValue() {
    return value;
  }

  /**
   * Sets the value.
   *
   * @param value the new value
   */
  public void setValue(double value) {
    this.value = value;
  }

  /**
   * Sets the value2.
   *
   * @param value2 the new value2
   */
  public void setValue2(double value2) {
    this.value2 = value2;
  }

  /**
   * Sets the partial blocks.
   *
   * @param partialBlocks the new partial blocks
   */
  public void setPartialBlocks(int partialBlocks) {
    this.partialBlocks = partialBlocks;
  }

  /**
   * Sets the full blocks.
   *
   * @param fullBlocks the new full blocks
   */
  public void setFullBlocks(int fullBlocks) {
    this.fullBlocks = fullBlocks;
  }

  /**
   * Sets the show empty blocks.
   *
   * @param showEmptyBlocks the new show empty blocks
   */
  public void setShowEmptyBlocks(boolean showEmptyBlocks) {
    this.showEmptyBlocks = showEmptyBlocks;
  }

  /**
   * Sets the show a.
   *
   * @param showA the new show a
   */
  public void setShowA(boolean showA) {
    this.showA = showA;
  }

  /**
   * Sets the show b.
   *
   * @param showB the new show b
   */
  public void setShowB(boolean showB) {
    this.showB = showB;
  }

}
