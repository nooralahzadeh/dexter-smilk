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
package fr.inria.wimmics.smilk.renco;

//import java.io.File;
//import java.io.FileOutputStream;
//
//import org.jdom2.Document;
//import org.jdom2.Element;
//import org.jdom2.output.XMLOutputter;
//import org.jdom2.output.Format;
//
//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.WebResource;
//
// 
//public class JerseyClientPost {
// 
//  public static void main(String[] args) {
// 
//    try {
// 
//        Client client = Client.create();
//        String input = "{\"message\":\"La gamme Elsève est excellente.\"}";
//        
//        WebResource webResource = client.resource("https://demo-innovation-projets-groupe.viseo.net/renco-rest/rest/rencoLight/getLightAnalyze"); 
//        ClientResponse response = webResource.type("text/plain")
//           .post(ClientResponse.class, input);   
// 
//        System.out.println("Output from Server test 1.... ");
//        String output = response.getEntity(String.class);
//        System.out.println(output);
//        
//   
//        /*Avec une sortie de type String XML*/  
//        
//		Client client2 = Client.create();		
//	    WebResource webResource2 = client2.resource("https://demo-innovation-projets-groupe.viseo.net/renco-rest/rest/renco/getRenco");	 	      	    
//		
//		ClientResponse response2 = webResource2.type("text/plain").post(ClientResponse.class, input);   
//	    
//	    System.out.println("Output from Server test 2....");	    
//	     
//	    String sortie = response2.getEntity(String.class);
//	    
//	    System.out.println(sortie);      
// 
//      } catch (Exception e) {
// 
//        e.printStackTrace();
// 
//      }
// 
//    }
//}