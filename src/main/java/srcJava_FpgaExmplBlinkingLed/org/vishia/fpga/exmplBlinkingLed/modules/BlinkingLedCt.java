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

  
  
  enum State {
    fast(0x1, 1),     //The value of the state should be 1 of n, given in the correct number format as in BITVECTOR(n) necessary.
    slow(0x2, 2),     // use 0b01101 if vector size is not able to divide by 4
    off(0x4, 3),
    nonInit(0x0, 0);  // The init value = 0 should be always present.
    
    final int _val_;
    
    final int bitNr;
    
    @Fpga.BITVECTOR(4) State(int val, int bitNr){ this._val_ = val; this.bitNr = bitNr; }
    
  }
  
  
  //tag::Prc[]
  //tag::Qdecl[]
  @Fpga.VHDL_PROCESS private static final class Q{

    @Fpga.STDVECTOR(16) final int ctLow;
    @Fpga.STDVECTOR(8) final int ct;
    final boolean led;
    int time; 
    @Fpga.STDVECTOR(4) final State state;
    
    //end::Qdecl[]
    Q() {
      this.ctLow = 0;
      this.ct = 0;
      this.led = false;
      this.time = 0;
      this.state = State.nonInit;
    }
    
    //tag::Q-Process-ce[]
    @Fpga.VHDL_PROCESS Q(int time, Q z, Ref ref) {
      Fpga.checkTime(time, ref.clkDiv.q.time, 1);  // for the ce signal, constraint with 1 clock delay.
      if(ref.clkDiv.q.ce) {
      //end::Q-Process-ce[]
        //tag::checktime[]
        Fpga.checkTime(time, z.time, 20);        // check whether all own process signals are persistent since 20 time steps.
        Fpga.checkTime(time, ref.cfg.time(), 20);// check all signals from the referenced module. 
        this.time = time;                        // all variables are declared as possible set with this time stamp.
        //end::checktime[]
        //tag::Q-Process-ifcUsg[]
        if(ref.reset.reset(time, 20)) {          // interface access to assigned here unknown reset module
          this.ct = ref.cfg.time_BlinkingLed();
          //end::Q-Process-ifcUsg[]
          this.ctLow = 0x0000;
          this.state = z.state;
        }                                     // underflow detection to 111.... as simple hardware-saving solution. The counter itself can use a carry-logic. 
        else if(Fpga.getBits(z.ctLow, 15, 13)==0b111) {    // check only 3 bits and not all bits =0
          this.ctLow = 0x61a7;  // 24999;                  // Period 25 ms, hint cannot use the range 0xe000..0xffff to prevent immediately underflow detection
          //                    // TODO should convert automatically also a given INTEGER constant to STD_LOGIC_VECTOR
          if(z.ct == 0x00) {                     // here a full 0 test with 8 bit is done. effort in hardware.
            this.ct = ref.cfg.time_BlinkingLed();// interface access to the reload value
          } else {
            this.ct = z.ct -1;                   // high counter normally count down automatically proper implemented in FPGA
          }
          this.state = State.fast;
        }
        else {
          this.ctLow = z.ctLow -1;               // count down automatically proper implemented in FPGA
          this.ct = z.ct;                        // high counter copy the state (not generated to VHDL, it is implicitely there)
          this.state = z.state;
        }
        this.led = z.ct < ref.cfg.onDuration_BlinkingLed(); //set FF after comparison with 8 bit.
      }
      else {                                     // clock enable ce == false
        this.ct = z.ct;                          // copy the state (not generated in VHDL)
        this.ctLow = z.ctLow;
        this.led = z.led;
        this.state = z.state;
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
  
  public @Fpga.IfcAccess Bit_ifc getStateFast = new Bit_ifc ( ) {
    @Override public boolean getBit() {
      return BlinkingLedCt.this.q.state == State.fast;
    }
  };

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


    @Override public void registerLines ( ) {
      super.clean();
      super.registerLine(this.sbCtLow, "ctLow");
      super.registerLine(this.sbCt, "ct");
      super.registerLine(this.sbtime, "time");
    }
    
    //tag::addSignals[]
    @Override public int addSignals ( int time, int lenCurr, boolean bAdd ) throws IOException {
      BlinkingLedCt thism = BlinkingLedCt.this;
      int zCurr = this.sbCt.length(); // current length for this time
      int zAdd = 0;                   // >0 then position of new length for this time
      if(thism.ref.clkDiv.q.ce) {       // because the own states switches only with this ce, the signals should also recorded only then.
        if(thism.q.ctLow == 1) {        // on this condition
          this.wrCt = 5;              // switch on, write 5 steps info
        }
        if(--this.wrCt >0) {          // if one of the 5 infos shouls be written:
          StringFunctions_C.appendHex(this.sbCtLow, thism.q.ctLow,4).append(' ');    //append info
          StringFunctions_C.appendHex(this.sbCt, thism.q.ct,2);                      //append info
          if(checkLen(this.sbtime, zCurr)) {      // add the time information if here is space.
            StringFunctions_C.appendIntPict(this.sbtime, time, "33'331.111.11");   // append time info
          }
          zAdd = this.sbCtLow.length();  //length of buffers for new time determined by the sbCtLow, the longest entry.
        } 
        else if(this.wrCt ==0) {         // end of the 5 steps, append .... as separation
          this.sbCtLow.append("..... ");
          zAdd = this.sbCtLow.length();  //length of buffers for new time determined by the sbCtLow, the longest entry.
        }
      }// if ce
      return zAdd;       // will be used in TestSignalRecorderSet.addSignals(zAdd) to set all lines to this length
    }//addSignals
    //end::addSignals[]
  }// class TestSignals
  
  
}
