//tag::classDef[]
package org.vishia.fpga.exmplBlinkingLed.test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.vishia.fpga.exmplBlinkingLed.fpgatop.BlinkingLed_Fpga;
import org.vishia.fpga.exmplBlinkingLed.fpgatop.BlinkingLed_IoAcc;
import org.vishia.fpga.testutil.CheckOper;
import org.vishia.fpga.testutil.TestSignalRecorder;
import org.vishia.fpga.testutil.TestSignalRecorderSet;
import org.vishia.util.TestOrg;

public class Test_BlinkingLed {

  BlinkingLed_Fpga fpga = new BlinkingLed_Fpga();

  //end::classDef[]

  //tag::outH[]
  TestSignalRecorderSet outH;
  
  
  void test_All ( TestOrg testParent) throws IOException {
    this.outH = new TestSignalRecorderSet();
    this.outH.registerRecorder(this.fpga.ref.ct.new TestSignals("ct"));
    this.outH.registerRecorder(this.new TestSignals("io"));
    this.outH.clean();
    //end::outH[]
    //tag::TestOrg[]
    TestOrg test = new TestOrg("testTxSpe", 6, testParent);
    //end::TestOrg[]
    //tag::initStimuli[]
    BlinkingLed_IoAcc.setLowactive_reset_Inpin( this.fpga, true);           // reset inactive high as only one input
    //end::initStimuli[]
    //tag::runSimul[]
    int time = 0;
    while(++time < 120000000) {
      this.fpga.step(++time);
      this.fpga.update();
      if(this.fpga.ref.ce.q.ce) {             // speed up simulation, only on ce the data are calculated new.
        this.outH.addSignals(time);
      }
    }
    //end::runSimul[]
    //tag::outRecording[]
    this.outH.output(System.out);
    Writer fout = new FileWriter("build/test_BlinkingLed_Signals.txt");
    this.outH.output(fout);
    fout.close();
    //end::outRecording[]
    
    //tag::testEval[]
    CheckOper.CharMinMax[] checkLedA = { new CheckOper.CharMinMax('_', 200, 9999), new CheckOper.CharMinMax('A', 200, 300)};
    //Note: a shorter low phase of LedB occurs on overflow of the high bit counter.
    CheckOper.CharMinMax[] checkLedB = { new CheckOper.CharMinMax('_', 26, 200 ), new CheckOper.CharMinMax('B', 26,26)};
    String error = CheckOper.checkOutput(this.outH.getLine("io.ledA"), 20, checkLedA);
    test.expect(error == null, 5, "LedA off 200.. chars, on 200..300 chars" + (error ==null ? "" : " ERROR: " + error) );
    error = CheckOper.checkOutput(this.outH.getLine("io.ledB"), 20, checkLedB);
    test.expect(error == null, 5, "LedB off 180..200 chars, on 26 chars" + (error ==null ? "" : " ERROR: " + error));
    
    test.finish();
    //end::testEval[]
    
  }
  
  
  
  
  //tag::main[]
  public static void main(String[] args) {
    Test_BlinkingLed thiz = new Test_BlinkingLed();
    TestOrg test = new TestOrg("Test_BlinkingLed", 3, args);
    try {
      thiz.test_All(test);
    }
    catch(Exception exc) {
      System.out.println(exc.getMessage());
      exc.printStackTrace();
      test.exception(exc);
    }
    test.finish();
  }
  //end::main[]
  
  //tag::TestSignals[]
  class TestSignals extends TestSignalRecorder {

    StringBuilder sbLedA = new StringBuilder(500);
    StringBuilder sbLedB = new StringBuilder(500);
    
    private char cLedA, cLedB;
    
    /**This instance should be added on end using {@link TestSignalRecorderSet#registerRecorder(TestSignalRecorder)}
     * because it decides adding an information only depending of other SignalRecorders.
     * @param sModule name, first part of line identifier
     */
    public TestSignals(String sModule) {
      super(sModule);
    }

    /**cleans all StringBuilder line and registered it. */
    @Override public void registerLines ( ) {
      super.clean();
      super.registerLine(this.sbLedA, "ledA");
      super.registerLine(this.sbLedB, "ledB");
    }
    
    @Override public int addSignals ( int time, int lenCurr, boolean bAdd) throws IOException {
      BlinkingLed_Fpga fpga = Test_BlinkingLed.this.fpga;
      if(bAdd) {                                  //only calculate state if another line has additional information.
        if(fpga.ref.ioPins.output.led1) {
          if(fpga.ref.ioPins.output.led1) {
          }  
        }
        this.cLedA = fpga.ref.ioPins.output.led1 ? 'A': '_';
        this.cLedB = fpga.ref.ioPins.output.led2 ? 'B': '_';
        this.sbLedA.append(this.cLedA);
        this.sbLedB.append(this.cLedB);
        return this.sbLedA.length();
      } else {
        return 0;  // no own contribution to length, regard add, sub ordinate.
      }
    }
    
    
    /**This operation is here overridden to add the character of the led state instead adding spaces as separator. 
     * It will be called in {@link TestSignalRecorderSet#addSignals(int)} after info to all lines are added.
     * Which character is added, this is determined by addSignals above in this class. */
    @Override protected void endSignals ( int pos) {
      while(this.sbLedA.length() < pos) { this.sbLedA.append(this.cLedA); }
      while(this.sbLedB.length() < pos) { this.sbLedB.append(this.cLedB); }
    }
    
  }
  //end::TestSignals[]
}
