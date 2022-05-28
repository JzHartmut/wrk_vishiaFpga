package org.vishia.fpga.exmplBlinkingLed.modules;

import org.vishia.fpga.Fpga;

/**Configuration interface for a Module which a blinking led.
 * The interface routines can implemented with a const value
 * as also an access to a appropriate BIT_VECTOR variable with 4 bits.
 * @author Hartmut Schorrig
 *
 */
public interface BlinkingLedCfg_ifc {

  /**Returns the time in 100 ms unit, max, 15 */
  int time_BlinkingLed ( );

  /**Returns the duration for on in 100 ms unit, max, 15, should be less than time. */
  int onDuration_BlinkingLed ( );
  
  /**Returns the time for the simulation when all called values were last set. */
  int time();
}
