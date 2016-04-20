/*
 * Copyright 2015 fnoorala.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.inria.wimmics.smilk.relatedness;

/**
 *
 * @author fnoorala
 */
import java.io.File;
import org.python.core.Py;
import org.python.core.PyInstance;  
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;  


public class InterpreterExample  
{  

   PythonInterpreter interpreter = null;  


   public InterpreterExample()  
   {  
      //PythonInterpreter.initialize(System.getProperties(),    System.getProperties(), new String[0]);  
                                   

      this.interpreter = new PythonInterpreter(null, new PySystemState());
      PySystemState sys = Py.getSystemState();
      sys.path.append(new PyString("/home/fnoorala/jython2.7.0/lib"));
      sys.path.append(new PyString("/user/fnoorala/home/anaconda/lib/python2.7/site-packages"));
      sys.path.append(new PyString("/user/fnoorala/home/anaconda/lib/python2.7"));
   }  

   void execfile(final String fileName )  
   {  
      this.interpreter.execfile(fileName);  
   }  

   PyInstance createClass( final String className, final String opts )  
   {  
      return (PyInstance) this.interpreter.eval(className + "(" + opts + ")");  
   }  

   public static void main( String gargs[] )  
   {  
      InterpreterExample ie = new InterpreterExample();  
      File file = new File("wiki2vec.py");
      String pythonfile = file.getAbsolutePath();
      ie.execfile(pythonfile);  
       System.out.println(pythonfile);
       
      PyInstance hello = ie.createClass("wiki2vec", "/user/fnoorala/home/NetBeansProjects/wiki2vec/resources/gensim/output");  

      hello.invoke("out");
   }  
} 
