/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.hpc.dexter.util;

/**
 *
 * @author fnoorala
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facilities for serializing and deserializing objects on/from files.
 *
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 *
 * Created on Sep 10, 2012
 */
public class Serializer {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(Serializer.class);

    /**
     * *
     * serializes an object (serializable) on a file
     *
     * <br/>
     * It terminates the execution in case of any type of error.
     *
     * @param obj the object to serialize
     * @param file the file where to write the serialization
     */
    public void dump(Object obj, String file) {
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out;
            out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            logger.error("serializing {} object ({})", obj.getClass(),
                    e.toString());
            System.exit(-1);
        }
    }

    /**
     * deserialize an object from a file
     *
     * @param file the file containing the serialized version of the object
     * @return the deserialized version of the object
     */
    public Object load(String file) {
        Object obj = null;
        try {

            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            try {
                obj = in.readObject();
            } catch (ClassNotFoundException e) {
                logger.error("deserializing object ({})", e.toString());
                System.exit(-1);
            }
            in.close();
            fileIn.close();
        } catch (IOException e) {
            logger.error("deserializing object ({})", e.toString());
            System.exit(-1);
        }

        return obj;

    }

    /**
     * deserialize an object from a file from FTP
     *
     * @param file the file containing the serialized version of the object
     * @return the deserialized version of the object
     */
    public Object load(String file, FTPClient ftpClient) {
        Object obj = null;

        try {

            InputStream fileIn = ftpClient.retrieveFileStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            try {
                obj = in.readObject();
            } catch (ClassNotFoundException e) {
                logger.error("deserializing object ({})", e.toString());
                System.exit(-1);
            }
            in.close();
            fileIn.close();
        } catch (IOException e) {
            logger.error("deserializing object ({})", e.toString());
            System.exit(-1);
        }

        return obj;

    }
    Object obj = null;

}
