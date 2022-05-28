package org.vishia.fpga.stdmodules;

public interface Reset_ifc {
  
  /**Returns true for reset. false for normal operation.
   * @param time current time for the access
   * @param max check whether the time of the accessed signal was latest set to (time - max).
   * @return true then reset active, false: normal operation.
   */
  public boolean reset ( int time, int max);
}
