package org.vishia.fpga.test;

public class ParseExample {

  public class Prc {
    
    float a=1, b=2, c=3, d=4, e=5, f=6, g=7 ,h=8 ,i=9;
    float j=10 ,k=11 ,l=12 ,m=13 ,n=14 ,o=15 ,p=16 ,q=17 ,r=18 ,s=19;
    float t=20 ,u=0 ,v=0 ,w=0 ;
    float x, y, z;
    
    boolean q1 = false, q2 = true, q3= false, q4 = true, q5 = false;
    boolean q6, q7, q8, q9; 
    
    //tag::exmplOperation[]
    /**This operation is used to parse, and then execute the expression via {@link org.vishia.util.CalculatorExpr}
     * inside {@link TestParseJava}. There also this operation is called to compare the self executed result
     * after conversion to RPN with the result calculated from Java execution itself.
     */
    @SuppressWarnings("unqualified-field-access") //because the fields are used for independent calculation is simple names too.
    public void exmplOperation ( ) {
      x = a + b * c;                    // u#; a#; b#; #+; c*; #=; d-; 
      
      y = (a + b) * c;
      
      z = (a + b) * (c + d);
      
      m = a + (b + c);
      
      q6 = q1 || q2 && q4;
      
      q7 = c < d || b >a && c >b; 
      
      u = (a + b) * c -d;                    // u#; a#; b#; #+; c*; #=; d-; 
      v = a * -(d+g) / e + (a+b);         // v#; a#; d#; g+; #*; e/; a#; b+; #+; #=; 
      //this.x = (a ==0) ? c+d : d+e;
      // w#; a#; b#; c#; f+; a#; b+; #/; #*; #+; e-; m#; d#; #=; g*; #+; #=; e/; 
      
      //w#; a#; b#; c#; f+; a#; b+; #/; #*; #+; e-; m#; d#; g*; #=; e/; #+; #=; 
      //[set "w" [22], set "a" [0], set "b" [1], set "c" [2], + "f" [5], set "a" [0], + "b" [1], /  stack , *  stack , +  stack , - "e" [4], set "m" [12], set "d" [3], * "g" [6], / "e" [4], +  stack ]
  //    w = a + b * (c + f) / (a+b) - e + (m = d * g) / e;     
  
      //@ w; @ a; @ b; @ c; + f; @ a; + b; / @; * - @; + @; - e; @ d; * g; / e; + @; = @; 
      //[set "w" [22], set "a" [0], set "b" [1], set "c" [2], + "f" [5], set "a" [0], + "b" [1], /  stack , *  stack , +  stack , - "e" [4], set "d" [3], * "g" [6], / "e" [4], +  stack ]
      w = a + b * -(c + f) / (a+b) - e + (d * g) / e;
      
      
      p = q1 || q2 && q4 ? a+b : e+c*d;

      a = a;
      
      s = a + (r = b +c);
      
      return;
    }
    //end::exmplOperation[]
    
  } //class Prc
  
  public final Prc prc = new Prc();
}
