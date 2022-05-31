package org.vishia.fpga.exmplBlinkingLed.fpgatop;

/**This class is only for test to access Pins in {@link BlinkingLed_FpgaInOutput}
 * @author Hartmut Schorrig
 *
 */
public class BlinkingLed_IoAcc {

  /**Operation to set the reset Inpin, with the hint that this pin is low active.
   * @param val the immediately pin value, true for high, inactive.  */
  public static void setLowactive_reset_Inpin(BlinkingLed_Fpga fpga, boolean val) { 
    fpga.ref.ioPins.input.reset_Pin = val; 
  }
  
}

