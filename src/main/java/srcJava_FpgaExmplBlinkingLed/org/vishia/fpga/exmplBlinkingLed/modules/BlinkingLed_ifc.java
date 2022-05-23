package org.vishia.fpga.exmplBlinkingLed.modules;

/**Interface for any module which offers a signal for the "blinking led". 
 * It can be implmented also as part of any module. 
 * @author Hartmut Schorrig
 *
 */
public interface BlinkingLed_ifc {

  
  /**Interface operation 
   * @return value for a led which should be blinking.
   */
  boolean ledBlinking ( );
}
