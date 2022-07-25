package org.vishia.fpga.testutil;

import org.vishia.fpga.Fpga;

/**This is the abstraction of Store classes.
 * Store classes of all modules and also of the whole simulation environment can be built
 * to store the state after a determined simulation, 
 * to run different further simulations starting all exact from this state. 
 * <br>
 * <b>A Store class in a module</b> should be created in the following form
 * (example, template): <pre>  
  public static class Store extends StateStoreFpga < Reset > {
    final Q q;                         // reference to state of a PROCESS instance

    public Store(int time, Reset src) {
      super(time, src);                // stores also the src as destination used for restore
      this.q = src.q;
    }

    (at)Override public int restore() {
      super.dst.q = this.q;            // Restore exact in the used module as src
      return super.time;
    }
  }
 </pre>
 * In this example the <code>q</code> is the reference which contains the current state of the module. 
 * If the module has more as one PROCESS classes, then all of it should be stored.
 * Note: Only the reference is saved. The data are in the heap. 
 * Because of the writing style of PROCESS classes anytime a new instance will be created, 
 * hence the referenced instance for storing will not be changed. 
 * This writing style is essential also for this store operation. 
 * <br>
 * Also the time stamp is stored in this base class.
 * For restoring the instance reference of the module is used to restore exact in the same module as used for creation.
 * <br> 
 * <b>A store class for the whole FPGA and also the whole simulation envorionment </b> consisting of some more modules 
 * should be written in the form: <pre>
  public static class Store extends StateStoreFpga<MyWholeFpgaAndSimEnv.Modules>{
    final ModuleX.Store moduleX1;
    final Reset.Store reset;

    public Store(int time, MyWholeFpgaAndSimEnv thiz) {
      super(time, thiz.ref);              //stores the reference to the Ref for restoring.
      this.reset = new Reset.Store(time, thiz.ref.clr);
      this.moduleX1 = new ModuleX.Store(thiz.ref.moduleX1);
    }

    (at)Override public int restore() {
      this.moduleX1.restore();
      this.reset.restore();
      return super.time;
    }
  }
  </pre>
 * It means on creation of the Store of the whole FPGA or also the whole simulation environment 
 * a <code>Store</code> class is created which contains the reference to all Store classes of all modules,
 * whereas this Store classes are also created.
 * On {@link #restore()} the Store of the module knows the src module to restore,
 * so that no extra reference is necessary.
 * <br>
 * <b>The application pattern</b> in the simulation environment should be written as: <pre>
  // Prepare the simulation ....
  // and execute some steps to get a defined state. .....
  MyWholeFpgaAndSimEnv.Store storeAfterXyState = new MyWholeFpgaAndSimEnv.Store(this.time, this.fpgaAndSimEnv);    // ==> store after 16 sync bits
  // ... execute some simulation starting from this state ....
  this.time = storeAfterXyState.restore();
  // ... execute other simulations starting from this state ....
  this.time = storeAfterXyState.restore();
  // ... execute third other simulations starting from this state ....
 </pre>
 * For all this simulation variants you have the same start condition. 
 * The advantage is: A maybe complex simulation to get the state is not necessary.
 * It may be also possible to save a state gotten from a real hardware situation,
 * and start then some simulations. 
 * But this presumes, that you have a hardware tooling which saves the hardware state,
 * and then a software which converts to the necessary Java data.
 * This is generally possible. For example the state of all FlipFlop can be read with boundary scan
 * or a built in specific logic. The conversion is depending of the given read possibility and data store format.
 *  
 * @author Hartmut Schorrig LPGL licence
 *
 * @param <FpgaModule>
 */
public abstract class StateStoreFpga<FpgaModule> {

  /**Version, history and license.
   * <ul>
   * <li>2022-06-02 Created newly as abstraction of some Store classes in FPGA modules.
   * </ul>
   * 
   * <b>Copyright/Copyleft</b>:
   * For this source the LGPL Lesser General Public License,
   * published by the Free Software Foundation is valid.
   * It means:
   * <ol>
   * <li> You can use this source without any restriction for any desired purpose.
   * <li> You can redistribute copies of this source to everybody.
   * <li> Every user of this source, also the user of redistribute copies
   *    with or without payment, must accept this license for further using.
   * <li> But the LPGL ist not appropriate for a whole software product,
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
  public static final String sVersion = "2022-06-04";

  
  /**The current time stamp for the storing the state.
   * It can be used to assert that all stores have the same time stamp,
   * and for the whole store, to restore the simulation time stamp.
   * This is important because also the {@link Fpga#checkTime(int, int, int)} checks 
   * depends on the current time stamp (restored) in comparison to the time stamp of the stored signals.
   */
  protected int time;
  
  protected FpgaModule dst;
  
  /**The super constructor. It is also a template how the constructor of an inherited class should be written.
   * 
   * @param time necessary, see #time
   * @param src The reference to the own module which's type contains the derived class.
   *   This reference is stored in {@link #dst} (with the derived type)
   *   and it should be used on {@link #restore()} to restore the state of exact the same modules.
   * 
   */
  protected StateStoreFpga ( int time, FpgaModule src ) {
    this.time = time;
    this.dst = src;               //the src is also the dst
  }


  /**The implementing operation should restore the appropriate states of the module.
   * See application patterns in the class description.
   * @param dst the module
   * @return implementing operation should <code>return super.time</code>.
   *   See remarks on {@link #time}
   */
  public abstract int restore ( );
  
}
