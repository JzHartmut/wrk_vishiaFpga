package org.vishia.fpga.tmpl_J2Vhdl;

import org.vishia.fpga.FpgaModule_ifc;
import org.vishia.fpga.stdmodules.Reset;

public class MyFpgaTop implements FpgaModule_ifc {
  public class Modules {                                                   //  (1)
    public final MyFpgaIo ioPins = new MyFpgaIo(MyFpgaTop.this);           //  (2)
    public final Reset reset = new Reset(this.ioPins.resetPin);            //  (3)
    final ModuleXY mdl1 = new ModuleXY();                                  //  (4)
    final OtherModuleXz module2 = new OtherModuleXz();
    Modules ( ) {
      this.mdl1.init(this.reset, this.module2, this.ioPins);           //  (5)
    }
  }
  public final Modules modules = new Modules();                            //  (6)
  public MyFpgaTop() { }                                                   //  (7)

  @Override public void reset ( ) {                                        // (8)
    this.modules.reset.reset();
    this.modules.mdl1.reset ();
  }
  public void input ( ) {                                                   // (9)
    this.modules.mdl1.in.in1 = this.modules.ioPins.specificIfcAccess.getBit();
  }
  @Override public void step ( int time ) {                                 // (10)   <tag::timeCheck>
    this.modules.mdl1.srcCE0.checkTime(time, this.modules.mdl1.srcCE7, 3);
    this.modules.mdl1.srcCE7.checkTime(time, this.modules.mdl1.srcCE0, 7);         // </tag::timeCheck>
    this.modules.reset.step(time);
    this.modules.mdl1.step(time);
  }
  @Override public void update ( ) {                                        // (11)
    this.modules.reset.update();
    this.modules.mdl1.update();
  }
  public void output ( ) {                                                  // (12)
    this.modules.mdl1.output();
    //this.modules.module2.output();
    this.modules.ioPins.output.testout = this.modules.mdl1.getValxy.getBit();
  }
}
