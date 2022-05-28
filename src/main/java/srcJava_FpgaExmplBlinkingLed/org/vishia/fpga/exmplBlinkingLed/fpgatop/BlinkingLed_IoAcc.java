package org.vishia.fpga.exmplBlinkingLed.fpgatop;

/**This class is only for test to access Pins in {@link BlinkingLed_FpgaInOutput}
 * @author Hartmut Schorrig
 *
 */
public class BlinkingLed_IoAcc {

  /**Set the reset pin with original level, 
   * @param io FPGA io instance from the top level modules 
   * @param val high is inactive, low active for reset.
   */
  public static void setResetPin(BlinkingLed_Fpga fpga, boolean val) { fpga.ref.ioPins.input.reset_Pin = val; }
  

}
