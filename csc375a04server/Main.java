/*
 * Angie Graci
 * CSC 375
 * Dr. Lea
 * Assignment 04
 * 
 * Server Side
 */
package csc375a04server;

import csc375a04.InitPack;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author angie
 */
public class Main {
    private static Alloy suballoy;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        suballoy = new Alloy();
        System.out.println("Hello.");
        CalculateService cs = new CalculateService();
        ReportService rs = new ReportService();
        cs.start();
        rs.start();
        System.out.println("Goodbye.");
    }

    // Calculates the new values of the sub-mesh:
    private static class CalculateService extends Thread {
        @Override
        public void run() {
            //int portNumber = 2697;
            int portNumber = 2691;
            System.out.println("Starting calculate service...");
            try (
                // make a server socket:
                ServerSocket serverSocket = new ServerSocket(portNumber);
                // wait for a client to make a connection:
                Socket clientSocket = serverSocket.accept();
                // communication channels:
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            ) {
                Object input; // store the input
                while ((input = in.readObject()) != null) {
                    // calculate
                    if ((input.getClass() == long[].class) && suballoy.isInitialized()) {
                        out.writeObject(suballoy.Calculate((long[]) input));
                        out.flush();
                        out.reset();
                    } // initialize
                    else if (input.getClass() == InitPack.class) {
                        suballoy.Initialize((InitPack) input);
                        out.writeObject("Thanks!");
                        out.flush();
                        out.reset();
                    } // end
                    else if (input.getClass() == String.class) {
                        break;
                    } // error
                    else {
                        System.out.println("Error: unexpected object type.");
                        break;
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Ending calculate service...");
        }
    }

    // Reports the current state of the sub-mesh:
    private static class ReportService extends Thread {
        @Override
        public void run() {
            //int portNumber = 12697;
            int portNumber = 12692;
            try (
                // make a server socket:
                ServerSocket serverSocket = new ServerSocket(portNumber);
                // wait for a client to make a connection:
                Socket clientSocket = serverSocket.accept();
                // communication channels:
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            ) {
                System.out.println("Starting report service...");
                Object input; // store the input
                while ((input = in.readObject()) != null) {
                    if(((String) input).equalsIgnoreCase("Bye.")) {
                        break;
                    }
                    out.writeObject(suballoy.getMesh());
                    out.flush();
                    out.reset();
                }
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Ending report service...");
        }
    }
}
