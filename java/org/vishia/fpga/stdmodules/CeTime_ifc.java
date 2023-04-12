package org.vishia.fpga.stdmodules;

import org.vishia.fpga.Fpga;

public abstract class CeTime_ifc {

  /**This operation name is detected by Java2Vhdl
   * @param time_ the current time of step(...)
   * @param min the minimum time to the last true output if it outputs true again, it is the CE minimal period 
   * @return true then CE is true, process should work.
   */
  public abstract boolean ce ();
  
  /**Returns the time where the CE was set last time.
   */
  public abstract int time ();
  
  /**Returns respectively determines the minimal length of a period  for this CE signal.
   * @return 10; or such, the return statement must contain only a simple numeric value.
   *   It is evaluated per simple syntax check.
   */
  public abstract int period ();
  
  public final void checkTime(int time, CeTime_ifc other, int min) {
    if(ce()) {               // if the own CE signal is active
      Fpga.checkTime(time, other, min);   // check the time from the other CE signal.
    }
  }
  
  /**Returns respectively determines the name of the time group for this CE signal.
   * @return "CE0" or such, the return statement must contain only a simple string literal.
   *   It is evaluated per simple syntax check.
   */
  public abstract String timeGroupName ();
}
