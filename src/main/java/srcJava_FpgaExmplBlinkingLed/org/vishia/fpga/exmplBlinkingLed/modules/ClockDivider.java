package org.vishia.fpga.exmplBlinkingLed.modules;

import org.vishia.fpga.stdmodules.Reset;
import org.vishia.fpga.Fpga;
import org.vishia.fpga.FpgaModule_ifc;

public class ClockDivider implements FpgaModule_ifc {

  //tag::Q-data[]
  /**Inner PROCESS class builds a TYPEDEF RECORD in VHDL and a PROCESS for each instance.
   * Note: Need public because here the interface technology is not used (negative, but simple understandable pattern).
   * Compare with {@link Reset.Q}
   */ 
  @Fpga.VHDL_PROCESS public static final class Q{

    @Fpga.STDVECTOR(4) final int ct;

    /**This is the variable of the record accessed from outside. 
     * Note: Need public because here the interface technology is not used (negative, but simple understandable pattern).
     * Compare with {@link Reset.Q#reset}
     */
    public final boolean ce;  
  //end::Q-data[]
    
    public Q() {
      this.ct = 0;
      this.ce = false;
    }
    
    @Fpga.VHDL_PROCESS public Q(Q z) {
      if(z.ct < 0b1001) {                        // divides the system clock by 10
        this.ct = (z.ct +1);
      } else {
        this.ct = 0b0000;
      }
      this.ce = this.ct == 0b0000;               // ce activated one time in 1:10 period, usable as clock enable. 
    }

  }

  
  
  /**Instance of the current value of the PROCESS inner class.
   * Note: Should be public because here the interface technology is not used (negative, but simple understandable pattern).
   */
  public Q q = new Q();
  private Q d_q;
  
  
  /**Module constructor with public access to instantiate.
   * Without parameter, works stand alone.
   */
  public ClockDivider() {
  }
  
  
  @Override
  public void step(int time) {
    this.d_q = new Q(this.q);
  }

  @Override
  public void update() {
    this.q = this.d_q;
  }

}
