package org.vishia.fpga.testutil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**This is a container class for some {@link TestSignalRecorder} for horizontal test signal outputs.
 * Any module can have one or more {@link TestSignalRecorder} implementations. 
 * This class summarize all module recorders in a List container to need one simple calls for the test signal recording and output. 
 * @author Hartmut Schorrig
 *
 */
public class TestSignalRecorderSet {

  
  /**Version, history and license.
   * <ul>
   * <li>2022-06-11 Improvements.
   * <li>2022-05- Hartmut created.
   * </ul>
   * <br><br>
   * <b>Copyright/Copyleft</b>:
   * For this source the LGPL Lesser General Public License,
   * published by the Free Software Foundation is valid.
   * It means:
   * <ol>
   * <li> You can use this source without any restriction for any desired purpose.
   * <li> You can redistribute copies of this source to everybody.
   * <li> Every user of this source, also the user of redistribute copies
   *    with or without payment, must accept this license for further using.
   * <li> But the LPGL is not appropriate for a whole software product,
   *    if this source is only a part of them. It means, the user
   *    must publish this part of source,
   *    but don't need to publish the whole source of the own product.
   * <li> You can study and modify (improve) this source
   *    for own using or for redistribution, but you have to license the
   *    modified sources likewise under this LGPL Lesser General Public License.
   *    You mustn't delete this Copyright/Copyleft inscription in this source file.
   * </ol>
   * If you are intent to use this sources without publishing its usage, you can get
   * a second license subscribing a special contract with the author. 
   * 
   * @author Hartmut Schorrig = hartmut.schorrig@vishia.de
   */
  public final static String sVersion = "2022-06-11"; 

  private List<TestSignalRecorder> recs = new LinkedList<TestSignalRecorder>();
  
  private int lenCurr;
  
  /**Add one recorder (usual inside a module) to this set.
   * Note: You can immediately instantiate the recorder on this call using example/pattern:<pre>
   * this.outH = new TestSignalRecorderSet();
   * this.outH.addRecorder(this.fpga.ref.ct.new TestSignals("ct"));
   * </pre>
   * @param rec the recorder in the module.
   */
  public void registerRecorder ( TestSignalRecorder rec) {
    this.recs.add(rec);
  }
  
  
  /**Cleans all buffers in the recorders. 
   * Write only the start line information for new recording.
   * It calls the overridden {@link TestSignalRecorder#clean()} for all registered instances with {@link #registerRecorder(TestSignalRecorder)}.  
   */
  public void clean ( ) {
    for(TestSignalRecorder rec: this.recs) {
      rec.registerLines();
    }
    this.lenCurr = TestSignalRecorder.lenClean;
  }
  
  
  /**Add all signals to all recorders and equalizes the line length. 
   * It calls the overridden {@link TestSignalRecorder#addSignals(int, boolean)} for all registered instances
   * and regards successfully adding signals of a previos recorder to force add on following ones 
   * setting bAdd = true (second argument of {@link #TestSignalRecorderSet()}{@link #addSignals(int)}.
   * <br>
   * After successful adding {@link TestSignalRecorder#endSignals(int)} is set for all recorders
   * so that all lines have the same length determined by the highest return value of {@link TestSignalRecorder#addSignals(int, boolean)}.
   * It means the column position in all lines for the time related next information is the same.
   * Expect lines which are not regarded in the zAdd return value. See example on {@link TestSignalRecorder#addSignals(int, boolean)}.  
   */
  public void addSignals(int time) throws IOException {
    int zLine = 0;                               // remain 0 if no signals are added
    for(TestSignalRecorder rec: this.recs) {
      boolean bAdd = zLine >0;                   // if any recorder has added somewhat before, then true
      int zLine1 = rec.addSignals(time, this.lenCurr, bAdd);   // bAdd==false can prevent addition
      if(zLine < zLine1) {                       // if has added somewhat, zLine is the longest line
        zLine = zLine1;
      }
    }
    if(this.lenCurr < zLine) {
      this.lenCurr = zLine;
      for(TestSignalRecorder rec: this.recs) {
        rec.endSignals(zLine);
      }
    }
    
  }
  
  
  
  
  /**Outputs the lines of all registered {@link TestSignalRecorderSet#registerRecorder(TestSignalRecorder)}.
   * This should be the last action of a test run. 
   * This operation can be called more as one time for different output channels,
   * for example output to System.out and to a file. 
   * @param out Any appendable, for example System.out or an opened file
   * @throws IOException
   */
  public void output(Appendable out) throws IOException {
    for(TestSignalRecorder rec: this.recs) {
      rec.output(out);
    }
  }
  
  
  public StringBuilder getLine(String id) {
    for(TestSignalRecorder rec: this.recs) {
      StringBuilder line = rec.getLine(id);
      if(line !=null) { return line; }
    }
    return null;   //not found
  }
  
  
}
