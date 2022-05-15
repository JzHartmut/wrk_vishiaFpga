package org.vishia.fpga.stdmodules;

import org.vishia.fpga.Fpga;
import org.vishia.fpga.FpgaModule_ifc;

//tag::classdef[]
public class Reset implements FpgaModule_ifc, Reset_ifc {
//end::classdef[]

  private static final class Ref {
    final Reset_Inpin_ifc resetInp;
    
    public Ref(Reset_Inpin_ifc resetInp) {
      this.resetInp = resetInp;
    }
  }
  
  final Ref ref;
  
  /**Inner PROCESS class builds a TYPEDEF RECORD in VHDL and a PROCESS for each (usual one) instance.
   * Note: It can be private, access via interface.
   */
  @Fpga.VHDL_PROCESS private static final class Q{

    @Fpga.STDVECTOR(4) final int resetCount;

    /**This is the variable of the record accessed from outside, but access via interface.
     * Hence it can be private (package private but the class is private). 
     */
    final boolean res;
    
    Q() {
      this.resetCount = 0;
      this.res = false;
    }
    
    @Fpga.VHDL_PROCESS Q(Q z, Ref ref) {
      if(ref.resetInp.reset_Pin() == false) {              // lo active clear pin
        this.resetCount = 0b0000;                
      }
      else if(z.res) {
        this.resetCount = z.resetCount +1;
      }
      else {
        this.resetCount = z.resetCount;
      }
      this.res = z.resetCount < 0b1110;                  // hi active internal clear signal if clrCount = 0...13
    }

    
  }

  
  
  private Q q = new Q();
  private Q d_q;
  
  
  /**Module constructor with public access to instantiate.
   * <br>
   * Note: The arguments should have the exact same name and type as in the {@link Ref#Ref(Reset_ifc, ClockDivider)} inner class.
   * @param resetInp this should be immediately the inputFpga port block.
   */
  public Reset(Reset_Inpin_ifc resetInp) {
    this.ref = new Ref(resetInp);
  }
  
  
  //public Clr_ifc q ( ) {return this.q; }
  
  @Override
  public void step(int time) {
    this.d_q = new Q(this.q, this.ref);
  }

  @Override
  public void update() {
    this.q = this.d_q;
  }

  //tag::reset()[]
  @Override public boolean reset ( ) { return this.q.res; }
  //end::reset()[]
  

}
