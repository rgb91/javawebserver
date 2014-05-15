package net.therap.server;


/**
 * Created with IntelliJ IDEA.
 * User: sanjoy.saha
 * Date: 5/7/14
 * Time: 9:33 AM
 * To change this template use File | Settings | File Templates.
 */

import net.therap.util.ServerHelper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Server extends Thread {

    Socket cSock;

    public Server(Socket cSock) {   //constructor
        this.cSock = cSock;
    }

    public void run() {
        BufferedReader fromClient = null;
        DataOutputStream toClient = null;
        try {
            fromClient = new BufferedReader(new InputStreamReader(cSock.getInputStream()));
            toClient = new DataOutputStream(cSock.getOutputStream());
            String request = fromClient.readLine();
            System.out.println("Received: " + request);

            String requestMethod = ServerHelper.getRequestMethod(request);
            String requestURL = ServerHelper.getRequestURL(request);

            if (requestMethod.equals("GET")) new ServerHelper().handleGET(requestURL, toClient);
            else if (requestMethod.equals("POST")) new ServerHelper().handlePOST(fromClient, toClient);
            else new ServerHelper().handleBadRequest(toClient);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (fromClient!=null) {
                try { fromClient.close(); } catch (IOException e) { e.printStackTrace(); }
            }
            if (toClient!=null) {
                try { toClient.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }


}
