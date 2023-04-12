package org.vishia.fpga.tmpl_J2Vhdl;

import org.vishia.fpga.Fpga;
import org.vishia.fpga.FpgaModule_ifc;

public class OtherModuleXz implements OtherModule_ifc, FpgaModule_ifc{

  
  @Fpga.VHDL_PROCESS public static class Qx {
    final boolean bit1;
    
    Qx(){
      this.bit1 = false;
    }
    @Fpga.VHDL_PROCESS Qx(int time, Qx z){
      this.bit1 = !z.bit1;
    }
  }
  
  Qx qx;

  
  
  
  @Override public boolean getSpecValue ( int time, int min) {
    return this.qx.bit1;
  }

  @Override public int getConstValue () {
    // TODO Auto-generated method stub
    return 0x3456;
  }

  @Override public void reset () {
    // TODO Auto-generated method stub
    
  }

  @Override public void step ( int time ) {
    // TODO Auto-generated method stub
    
  }

  @Override public void update () {
    // TODO Auto-generated method stub
    
  }

}
