//tag::theClassDef[]
package org.vishia.fpga.exmplBlinkingLed.modules;

import org.vishia.fpga.Fpga;
import org.vishia.fpga.FpgaModule_ifc;
import org.vishia.fpga.stdmodules.Bit_ifc;
import org.vishia.fpga.stdmodules.Reset_ifc;
import org.vishia.fpga.testutil.TestSignalRecorder;
import org.vishia.util.StringFunctions_C;

import java.io.IOException;

public class BlinkingLedCt implements FpgaModule_ifc, BlinkingLed_ifc {
//end::theClassDef[]

  //tag::Ref[]  
  private static class Ref {

    /**Common module for save creation of a reset signal. */
    final Reset_ifc reset;
    
    final BlinkingLedCfg_ifc cfg;
    
    /**Specific module for clock pre-division. */
    final ClockDivider clkDiv;
    
    Ref(Reset_ifc reset, BlinkingLedCfg_ifc cfg, ClockDivider clkDiv) {
      this.reset = reset;
      this.cfg = cfg;
      this.clkDiv = clkDiv;
    }
  }
  
  private Ref ref;
  //end::Ref[]  

  //tag::ctor[]
  /**Module constructor with public access to instantiate.
   * <br>
   * Note: The arguments should have the exact same name and type as in the {@link Ref#Ref(Reset_ifc, ClockDivider)} inner class.
   *  
   * @param reset module provide the reset signal on power on and as input
   * @param clkDiv module provide a clock enable signal: {@link ClockDivider.Q#ce}
   */
  public BlinkingLedCt ( Reset_ifc reset, BlinkingLedCfg_ifc cfg, ClockDivider clkDiv) {
    this.ref = new Ref(reset, cfg, clkDiv);
  }
  
  /**Non parametrized constructor if the aggregations are not existing yet. Use {@link #init(Reset_ifc, BlinkingLedCfg_ifc, ClockDivider)}
   * for aggregation. */
  public BlinkingLedCt () {}
  //end::ctor[]

  //tag::init[]
  /**The init operation should be used instead the parameterized constructor with arguments if there are circular dependencies:
   * The modules should be known each other. Then only one module can be instantiated with the parameterized constructor.
   * The other module can only be instantiated firstly without aggregations, then get the aggregation via this init operation.
   * <br>
   * Note: The arguments should have the exact same name and type as in the {@link Ref#Ref(Reset_ifc, ClockDivider)} inner class.
   * @param reset aggregation to the reset module.
   * @param cfg aggregation to the configuration
   * @param clkDiv aggregation to the clock divider.
   */
  public void init ( Reset_ifc reset, BlinkingLedCfg_ifc cfg, ClockDivider clkDiv) {
    this.ref = new Ref(reset, cfg, clkDiv);
  }
  //end::init[]

  //tag::Prc[]
  //tag::Qdecl[]
  @Fpga.VHDL_PROCESS private static final class Q{

    @Fpga.STDVECTOR(16) final int ctLow;
    @Fpga.STDVECTOR(8) final int ct;
    final boolean led;
    int time; 
    
    //end::Qdecl[]
    Q() {
      this.ctLow = 0;
      this.ct = 0;
      this.led = false;
      this.time = 0;
    }
    
    //tag::Q-Process-ce[]
    @Fpga.VHDL_PROCESS Q(int time, Q z, Ref ref) {
      Fpga.checkTime(time, ref.clkDiv.q.time, 1);          // generate a constraint, without 1 clock access.
      if(ref.clkDiv.q.ce) {
      //end::Q-Process-ce[]
        //tag::checktime[]
        Fpga.checkTime(time, z.time, 20);     // check whether all signals are persistent since 20 time steps.
        Fpga.checkTime(time, ref.cfg.time(), 20);// check all signals from the referenced module. 
        this.time = time;                        // all variables are declared as possible set with this condition.
        //end::checktime[]
        //tag::Q-Process-ifcUsg[]
        if(ref.reset.reset(time, 20)) {          // interface access to assigned here unknown reset module
          this.ct = ref.cfg.time_BlinkingLed();
          //end::Q-Process-ifcUsg[]
          this.ctLow = 0x0000;    
        }                                     // underflow detection to 111.... as simple hardware-saving solution. The counter itself can use a carry-logic. 
        else if(Fpga.getBits(z.ctLow, 15, 13)==0b111) {    // check only 3 bits and not all bits =0
          this.ctLow = 0x61a7;  // 24999;                  // Period 25 ms, hint cannot use the range 0xe000..0xffff to prevent immediately underflow detection
          //                    // TODO should convert automatically also a given INTEGER constant to STD_LOGIC_VECTOR
          if(z.ct == 0x00) {                     // here a full 0 test with 8 bit is done. effort in hardware.
            this.ct = ref.cfg.time_BlinkingLed();// interface access to the reload value
          } else {
            this.ct = z.ct -1;                   // high counter normally count down automatically proper implemented in FPGA
          }
        }
        else {
          this.ctLow = z.ctLow -1;               // count down automatically proper implemented in FPGA
          this.ct = z.ct;                        // high counter copy the state (not generated to VHDL, it is implicitely there)
        }
        this.led = z.ct < ref.cfg.onDuration_BlinkingLed(); //set FF after comparison with 8 bit.
      }
      else {                                     // clock enable ce == false
        this.ct = z.ct;                          // copy the state (not generated in VHDL)
        this.ctLow = z.ctLow;
        this.led = z.led;
        this.time = z.time;
      }
    }
  }
  
  private Q q = new Q();
  private Q d_q;     
  //end::Prc[]
  
  
  
  //tag::step_update[]
  @Override
  public void step(int time) {
    if(this.ref.clkDiv.q.ce) {             // speed up simulation, only on ce the data are calculated new.
      this.d_q = new Q(time, this.q, this.ref);
    } else {
      this.d_q = this.q;
    }
  }

  @Override
  public void update() {
    this.q = this.d_q;
  }
  //end::step_update[]
  
  //tag::mdlifcimpl[]
  /**Implementation of a given interface which is also fulfilled by this module. 
   * @return value for a led which should be blinking.
   */
  @Override public boolean ledBlinking() { return this.q.led; }

  /**This is an example for an access operation only for this module,
   * without abstraction of using an interface.
   * Hence it is only defined for this module, not with a universal interface,
   * it should be used especially for necessary test outputs wich have only meaning using this module,
   * not for input interfaces for other modules.
   * @return the counter.
   */
  @Fpga.GetterVhdl public int ct() { return this.q.ct; }
  //@Fpga.BITVECTOR(8) //TODO evaluate it 
  //end::mdlifcimpl[]

  
  //tag::mdlifcacc[]
  public @Fpga.IfcAccess Bit_ifc getLedFast = new Bit_ifc ( ) {
    @Override public boolean getBit() {
      return Fpga.getBits(BlinkingLedCt.this.q.ct, 2,0) == 0b000;
    }
  };
  //end::mdlifcacc[]
  
  //tag::TestSignalRecorderHead[]
  public class TestSignals extends TestSignalRecorder {
  //end::TestSignalRecorderHead[]
    
    private StringBuilder sbCtLow = new StringBuilder(500);
    private StringBuilder sbCt = new StringBuilder(500);
    private StringBuilder sbtime = new StringBuilder(500);
    
    private int wrCt;
    
    public TestSignals(String moduleName) {
      super(moduleName);
    }


    @Override public void clean() {
      super.clean();
      super.startLine(this.sbCtLow, "ctLow");
      super.startLine(this.sbCt, "ct");
      super.startLine(this.sbtime, "time");
    }
    
    @Override public boolean checkAdd ( int time ) {
      return false; 
    }
    
    //tag::addSignals[]
    @Override public int addSignals ( int time, boolean bAdd ) throws IOException {
      BlinkingLedCt mdl = BlinkingLedCt.this;
      int zAdd = 0;
      if(mdl.ref.clkDiv.q.ce) {       //because the own states switches only with this ce, the signals should also recorded only then.
        if(mdl.q.ctLow == 1) {
          this.wrCt = 5;
        }
        if(--this.wrCt >0) {
          StringFunctions_C.appendHex(this.sbCtLow, mdl.q.ctLow,4).append(' ');
          StringFunctions_C.appendHex(this.sbCt, mdl.q.ct,2);
          zAdd = this.sbCtLow.length();
        } 
        else if(this.wrCt ==0) {
          zAdd = this.sbCtLow.length();  //though sbCtLow should determine the length of the output
          this.sbCtLow.append("...");
          StringFunctions_C.appendIntPict(this.sbtime, time, "33'331.111.11");
        }
      }// if ce
      return zAdd;
    }//addSignals
    //end::addSignals[]
  }// class TestSignals
  
  
}
