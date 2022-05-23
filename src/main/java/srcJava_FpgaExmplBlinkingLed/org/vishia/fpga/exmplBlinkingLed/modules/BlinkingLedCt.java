package org.vishia.fpga.exmplBlinkingLed.modules;

import org.vishia.fpga.stdmodules.Reset_ifc;
import org.vishia.fpga.Fpga;
import org.vishia.fpga.FpgaModule_ifc;

public class BlinkingLedCt implements FpgaModule_ifc, BlinkingLed_ifc {

  //tag::Ref[]  
  private static class Ref {

    /**Common module for save creation of a reset signal. */
    final Reset_ifc reset;
    
    final BlinkingLedCfg_ifc cfg;
    
    /**Specific module for clock pre-division. */
    final ClockDivider clkDiv;
    
    public Ref(Reset_ifc reset, BlinkingLedCfg_ifc cfg, ClockDivider clkDiv) {
      this.reset = reset;
      this.cfg = cfg;
      this.clkDiv = clkDiv;
    }
  }
  
  private final Ref ref;
  //end::Ref[]  

  
  //tag::Qdecl[]
  @Fpga.VHDL_PROCESS private static final class Q{

    @Fpga.STDVECTOR(16) final int ctLow;
    @Fpga.STDVECTOR(8) final int ct;
    final boolean led;

    //end::Qdecl[]
    public Q() {
      this.ctLow = 0;
      this.ct = 0;
      this.led = false;
    }
    
    //tag::Q-Process-ce[]
    @Fpga.VHDL_PROCESS public Q(Q z, Ref ref) {
      if(ref.clkDiv.q.ce) {
        //end::Q-Process-ce[]
        //tag::Q-Process-ifcUsg[]
        if(ref.reset.reset()) {
          this.ct = ref.cfg.time_BlinkingLed();
          //end::Q-Process-ifcUsg[]
          this.ctLow = 0x0000;    
        } 
        else if(Fpga.getBit(z.ctLow, 15)) {      // Underflow, important: don't use bit 15 for the reload value.
          this.ctLow = 0x61a7;  //24999;         // Period 25 ms  TODO should convert INTEGER to STD_LOGIC_VECTOR
          if(z.ct == 0x00) {
            this.ct = ref.cfg.time_BlinkingLed();
          } else {
            this.ct = z.ct -1;                   //normally count down.
          }
        }
        else {
          this.ctLow = z.ctLow -1;
          this.ct = z.ct;
        }
//        else if(Fpga.getBits(z.ct, 23, 20) == ref.cfg.time_BlinkingLed()) {                        // divides the system clock by 10
//          this.ct = 0x000000;  
//        } else { 
//          this.ct = (z.ct +1);
//        }
      }
      else {
        this.ct = z.ct;                          // copy the state
        this.ctLow = z.ctLow;
      }
      this.led = z.ct < ref.cfg.onDuration_BlinkingLed();
    }

  }

  
  
  private Q q = new Q();
  private Q d_q;
  
  //tag::ctor[]
  /**Module constructor with public access to instantiate.
   * <br>
   * Note: The arguments should have the exact same name and type as in the {@link Ref#Ref(Reset_ifc, ClockDivider)} inner class.
   *  
   * @param reset module provide the reset signal on power on and as input
   * @param clkDiv module provide a clock enable signal: {@link ClockDivider.Q#ce}
   */
  public BlinkingLedCt(Reset_ifc reset, BlinkingLedCfg_ifc cfg, ClockDivider clkDiv) {
    this.ref = new Ref(reset, cfg, clkDiv);
  }
  //end::ctor[]
  
  
  //public Clr_ifc q ( ) {return this.q; }
  
  @Override
  public void step(int time) {
    this.d_q = new Q(this.q, this.ref);
  }

  @Override
  public void update() {
    this.q = this.d_q;
  }

  /**This is an example for an access operation only for this module,
   * without abstraction of using an interface.
   * @return the counter.
   */
  @Fpga.GetterVhdl public int ct() { return this.q.ct; }
  //@Fpga.BITVECTOR(24) 

  /**Implementation of a given interface which is also fulfilled by this module. 
   * @return value for a led which should be blinking.
   */
  @Override public boolean ledBlinking() { return this.q.led; }


}
