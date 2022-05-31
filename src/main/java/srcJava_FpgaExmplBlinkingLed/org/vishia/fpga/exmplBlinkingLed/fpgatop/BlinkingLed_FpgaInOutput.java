//tag::classdef[]
package org.vishia.fpga.exmplBlinkingLed.fpgatop;

import org.vishia.fpga.Fpga;
import org.vishia.fpga.stdmodules.Reset_Inpin_ifc;

public class BlinkingLed_FpgaInOutput {
//end::classdef[]

  //tag::InOutput[]
  public static class Input {

    /**A low active reset pin, usual also the PROGR pin. 
     * Because of the specific function the access should only be done with the here defined access operation, hold it package private, not public. 
     * For test it can be accessed in a test access class in the same package.*/
    boolean reset_Pin;
  }

  
  public static class Output {
    
    /**Ordinary output pins, use public in responsible to top level design sources. */
    public boolean led1, led2, led3;
  }
  
  /**This instances are final and public accessible. 
   * The inputs should be used in the step operations as given on start of D-calculation.
   */
  public final Input input = new Input(); 
  
  /**The outputs should be set in the update() operation of the top level with the Q-Values of FlipFlops. */
  public final Output output = new Output(); 
  //end::InOutput[]

  //tag::ifcAccess[]
  /**Get the reset pin as referenced interface access from a module.
   * Using the {@link org.vishia.fpga.stdmodules.Reset} may be seen as recommended because it clarifies a longer reset signal.
   */
  @Fpga.IfcAccess public Reset_Inpin_ifc reset_Inpin = new Reset_Inpin_ifc () {
    @Override public boolean reset_Pin() { return BlinkingLed_FpgaInOutput.this.input.reset_Pin; }
  };
  //end::ifcAccess[]
  
  

  
}
