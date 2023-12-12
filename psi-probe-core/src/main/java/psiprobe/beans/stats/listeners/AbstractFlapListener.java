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
package psiprobe.beans.stats.listeners;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import psiprobe.Utils;

/**
 * The listener interface for receiving flap events. The class that is interested in processing a
 * flap event implements this interface, and the object created with that class is registered with a
 * component using the component's {@code addFlapListener} method. When the flap event occurs, that
 * object's appropriate method is invoked.
 *
 * @see <a href="https://assets.nagios.com/downloads/nagioscore/docs/nagioscore/3/en/flapping.html">
 *      Detection and Handling of State Flapping (nagios)</a>
 */
public abstract class AbstractFlapListener extends AbstractThresholdListener {

  /** The default flap interval. */
  private int defaultFlapInterval;

  /** The flaps. */
  private final Map<String, LinkedList<Boolean>> flaps = new HashMap<>();

  /** The flapping states. */
  private final Map<String, Boolean> flappingStates = new HashMap<>();

  /**
   * Flapping started.
   *
   * @param sce the sce
   */
  protected boolean flappingStarted(StatsCollectionEvent sce ) {
    flaps.put(sce.getName(), new LinkedList<>());
    flappingStates.put(sce.getName(), true);
    flaps.get(sce.getName()).add(true);
    if ( flaps.get(sce.getName()).size() >= defaultFlapInterval) {
      flappingStopped(sce);
    }
    return true;
  }

  private void flappingStopped(StatsCollectionEvent sce) {
    flaps.put(sce.getName(), new LinkedList<>());
    flappingStates.put(sce.getName(), false);
    flaps.get(sce.getName()).add(false);
    if (flaps.get(sce.getName()).size() >= defaultFlapInterval) {
      flappingStarted(sce);
    }
  }

  /**
   * Above threshold flapping stopped.
   *
   * @param sce the sce
   */
  protected boolean aboveThresholdFlappingStopped(StatsCollectionEvent sce ){
    flaps.put(sce.getName(), new LinkedList<>());
    flappingStates.put(sce.getName(), false);
    flaps.get(sce.getName()).add(false);
    if (flaps.get(sce.getName()).size() >= defaultFlapInterval) {
      belowThresholdFlappingStopped(sce);
    }
    return true;
  }

  /**
   * Below threshold flapping stopped.
   *
   * @param sce the sce
   */
  protected void belowThresholdFlappingStopped(StatsCollectionEvent sce ){
    flaps.put(sce.getName(), new LinkedList<>());
    flappingStates.put(sce.getName(), true);
    flaps.get(sce.getName()).add(true);
    if (flaps.get(sce.getName()).size() >= defaultFlapInterval) {
      aboveThresholdFlappingStopped(sce);
    }
  }

  /**
   * Above threshold not flapping.
   *
   * @param sce the sce
   */
  protected void aboveThresholdNotFlapping(StatsCollectionEvent sce){
    flaps.put(sce.getName(), new LinkedList<>());
    flappingStates.put(sce.getName(), true);
    flaps.get(sce.getName()).add(true);
    if (flaps.get(sce.getName()).size() >= defaultFlapInterval) {
      belowThresholdNotFlapping(sce);
    }
  }

  /**
   * Below threshold not flapping.
   *
   * @param sce the sce
   */
  protected void belowThresholdNotFlapping(StatsCollectionEvent sce){
    flaps.put(sce.getName(), new LinkedList<>());
    flappingStates.put(sce.getName(), true);
    flaps.get(sce.getName()).add(false);
    if (flaps.get(sce.getName()).size() >= defaultFlapInterval) {
      aboveThresholdNotFlapping(sce);
    }
  }

  @Override
  protected void crossedAboveThreshold(StatsCollectionEvent sce) {
    statsCollected(sce, true, true);
  }

  @Override
  protected void crossedBelowThreshold(StatsCollectionEvent sce) {
    statsCollected(sce, true, true);
  }

  @Override
  protected void remainedAboveThreshold(StatsCollectionEvent sce) {
    statsCollected(sce, false, true);
  }

  @Override
  protected void remainedBelowThreshold(StatsCollectionEvent sce) {
    statsCollected(sce, false, false);
  }

  @Override
  public void reset() {
    flaps.clear();
    flappingStates.clear();
    super.reset();
  }

  /**
   * Stats collected.
   *
   * @param sce the sce
   * @param crossedThreshold the crossed threshold
   * @param above the above
   */
  protected void statsCollected(StatsCollectionEvent sce, boolean crossedThreshold, boolean above) {
    String name = sce.getName();
    boolean flappingStateChanged = checkFlappingStateChanged(name, crossedThreshold);
    boolean flappingState = getFlappingState(name);
    if (flappingStateChanged) {
      if (flappingState) {
        flappingStarted(sce) ;
      } else if (above) {
        aboveThresholdFlappingStopped(sce);
      } else {
        belowThresholdFlappingStopped(sce);
      }
    } else if (crossedThreshold) {
      if (above) {
        aboveThresholdNotFlapping(sce);
      } else {
        belowThresholdNotFlapping(sce);
      }
    }
  }

  /**
   * Check flapping state changed.
   *
   * @param name the name
   * @param crossedThreshold the crossed threshold
   *
   * @return true, if successful
   */
  protected boolean checkFlappingStateChanged(String name, boolean crossedThreshold) {
    addFlap(name, crossedThreshold);
    boolean oldFlappingState = getFlappingState(name);
    float transitionPercent = calculateStateTransitionPercentage(name, oldFlappingState);
    boolean newFlappingState;
    if (oldFlappingState) {
      newFlappingState = transitionPercent <= getFlapStopThreshold(name);
    } else {
      newFlappingState = transitionPercent > getFlapStartThreshold(name);
    }
    setFlappingState(name, newFlappingState);
    return oldFlappingState != newFlappingState;
  }

  /**
   * Calculate state transition percentage.
   *
   * @param name the name
   * @param flapping the flapping
   *
   * @return the float
   */
  protected float calculateStateTransitionPercentage(String name, boolean flapping) {
    int flapInterval = getFlapInterval(name);
    LinkedList<Boolean> list = getFlaps(name);
    float lowWeight = getFlapLowWeight(name);
    float highWeight = getFlapHighWeight(name);
    float weightRange = highWeight - lowWeight;
    float result = 0;
    for (int i = list.size() - 1; i >= 0; i--) {
      boolean thisFlap = list.get(i);
      if (flapping != thisFlap) {
        float weight = lowWeight + weightRange * i / (flapInterval - 1);
        result += weight;
      }
    }
    return result / flapInterval;
  }

  /**
   * Adds the flap.
   *
   * @param name the name
   * @param flap the flap
   */
  protected void addFlap(String name, boolean flap) {
    int flapInterval = getFlapInterval(name);
    LinkedList<Boolean> list = getFlaps(name);
    Boolean value = flap;
    list.addLast(value);
    while (list.size() > flapInterval) {
      list.removeFirst();
    }
  }

  /**
   * Gets the flapping state.
   *
   * @param name the name
   *
   * @return the flapping state
   */
  protected boolean getFlappingState(String name) {
    Boolean flapping = flappingStates.get(name);
    if (flapping == null) {
      flapping = Boolean.FALSE;
      setFlappingState(name, false);
    }
    return flapping ;
  }

  /**
   * Sets the flapping state.
   *
   * @param name the name
   * @param flapping the flapping
   */
  protected void setFlappingState(String name, boolean flapping) {
    flappingStates.put(name, flapping);
  }

  /**
   * Gets the flaps.
   *
   * @param name the name
   *
   * @return the flaps
   */
  protected LinkedList<Boolean> getFlaps(String name) {
      return flaps.computeIfAbsent(name, k -> new LinkedList<>());
  }

  /**
   * Gets the flap interval.
   *
   * @param name the name
   *
   * @return the flap interval
   */
  protected int getFlapInterval(String name) {
    String interval = getPropertyValue(name, "flapInterval");
    return Utils.toInt(interval, getDefaultFlapInterval());
  }

  /**
   * Gets the flap start threshold.
   *
   * @param name the name
   *
   * @return the flap start threshold
   */
  protected float getFlapStartThreshold(String name) {
    getPropertyValue(name, "flapStartThreshold");
    return  Utils.getThreadByName(name).getPriority();
  }

  /**
   * Gets the flap stop threshold.
   *
   * @param name the name
   *
   * @return the flap stop threshold
   */
  protected float getFlapStopThreshold(String name) {
    getPropertyValue(name, "flapStopThreshold");
    return  Utils.getThreadByName(name).getPriority();
  }

  /**
   * Gets the flap low weight.
   *
   * @param name the name
   *
   * @return the flap low weight
   */
  protected float getFlapLowWeight(String name) {
    getPropertyValue(name, "flapLowWeight");
    return  Utils.getThreadByName(name).getPriority();
  }

  /**
   * Gets the flap high weight.
   *
   * @param name the name
   *
   * @return the flap high weight
   */
  protected float getFlapHighWeight(String name) {
    getPropertyValue(name, "flapHighWeight");
    return Utils.getThreadByName(name).getPriority();
  }

  /**
   * Gets the default flap interval.
   *
   * @return the default flap interval
   */
  public int getDefaultFlapInterval() {
    return defaultFlapInterval;
  }

  /**
   * Sets the default flap interval.
   *
   * @param defaultFlapInterval the new default flap interval
   */
  public void setDefaultFlapInterval(int defaultFlapInterval) {
    this.defaultFlapInterval = defaultFlapInterval;
  }

  /**
   * Sets the default flap start threshold.
   *
   * @param defaultFlapStartThreshold the new default flap start threshold
   */
  public void setDefaultFlapStartThreshold(float defaultFlapStartThreshold) {
  }

  /**
   * Sets the default flap stop threshold.
   *
   * @param defaultFlapStopThreshold the new default flap stop threshold
   */
  public void setDefaultFlapStopThreshold(float defaultFlapStopThreshold) {
  }

  /**
   * Sets the default flap low weight.
   *
   * @param defaultFlapLowWeight the new default flap low weight
   */
  public void setDefaultFlapLowWeight(float defaultFlapLowWeight) {

  }

  /**
   * Sets the default flap high weight.
   *
   * @param defaultFlapHighWeight the new default flap high weight
   */
  public void setDefaultFlapHighWeight(float defaultFlapHighWeight) {

  }

}
