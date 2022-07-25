//tag::theClassDef[]
package org.vishia.fpga.exmplBlinkingLed.fpgatop;

import org.vishia.fpga.stdmodules.Reset;
import org.vishia.fpga.Fpga;
import org.vishia.fpga.FpgaModule_ifc;
//Note: Do not use a package.path.*, not yet supported by Java2Vhdl
import org.vishia.fpga.exmplBlinkingLed.modules.BlinkingLedCfg_ifc;
import org.vishia.fpga.exmplBlinkingLed.modules.BlinkingLedCt;
import org.vishia.fpga.exmplBlinkingLed.modules.ClockDivider;

public class BlinkingLed_Fpga implements FpgaModule_ifc {
  
//end::theClassDef[]


//tag::Modules[] 
//tag::ioPins[]
  /**The modules which are part of this Fpga for test. */
  public class Modules {
    
    /**The i/o pins of the top level FPGA should have exact this name ioPins. */
    public BlinkingLed_FpgaInOutput ioPins = new BlinkingLed_FpgaInOutput();
    //end::ioPins[]
    
    /**Build a reset signal high active for reset. Initial or also with the reset_Pin. 
     * This module is immediately connected to one of the inputFpga pins
     * via specific interface, see constructor argument type.
     */
    public final Reset res = new Reset(this.ioPins.reset_Inpin);
    
    public final ClockDivider ce = new ClockDivider();
    
    public final BlinkingLedCt ct = new BlinkingLedCt(this.res, BlinkingLed_Fpga.this.blinkingLedCfg, this.ce);    //cfg implemented in extra class in this file.
    
    Modules ( ) {
      //aggregate the module afterwards
      this.ct.init(this.res, BlinkingLed_Fpga.this.blinkingLedCfg, this.ce);    //cfg implemented in extra class in this file.
    }
  }

  public final Modules ref;
  //end::Modules[]

  
  //tag::ctor[]
  public BlinkingLed_Fpga ( ) {
    this.ref = new Modules();   //hint: should be invoke after ctor parts in class members, especially interface access agents should be set.
  }
  //end::ctor[]
  
  
  //tag::step_update[]  
  @Override
  public void step(int time) {
    this.ref.res.step(time);
    this.ref.ce.step(time);
    this.ref.ct.step(time);
    
  }


  @Override
  public void update() {
    this.ref.res.update();
    this.ref.ce.update();
    this.ref.ct.update();
    //tag::outPins[]
    this.ref.ioPins.output.led1 = this.ref.ct.ledBlinking();
    this.ref.ioPins.output.led2 = this.ref.ct.getLedFast.getBit();
    this.ref.ioPins.output.led3 = Fpga.getBits(this.ref.ct.ct(), 2,0) != 0b000;
    this.ref.ioPins.output.led4 = this.ref.ct.getStateFast.getBit();
    //end::outPins[]
  }
  //end::step_update[]  
  
  
  
  //tag::time_BlinkingLed_ifcAccess[]

  /**Provides the used possibility for configuration values.
   */
  @Fpga.IfcAccess BlinkingLedCfg_ifc blinkingLedCfg = new BlinkingLedCfg_ifc ( ) {
    
    @Override @Fpga.BITVECTOR(8) public int time_BlinkingLed() {
      return 0x64;
    }

    @Override public int onDuration_BlinkingLed() {
      return 10;
    }

    @Override
    public int time() { return 0; }  // set from beginning

  };
  //end::time_BlinkingLed_ifcAccess[]


  
}


