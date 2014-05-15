package net.therap.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sanjoy.saha
 * Date: 5/7/14
 * Time: 10:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class ServerHelper {

    private static final String BASEPATH = System.getProperty("user.home") + "/Drive/serverSocket/";
    private static final String INDEXFILE = "index.html";
    private static final String PROTOCOL = "HTTP/1.0";
    private static final String NEWLINE = "\r\n";
    private static final Map<Integer, String> STATUS_CODES_MAP = new HashMap<Integer, String>();
    private static final Map<Integer, String> CONTENT_TYPE_MAP = new HashMap<Integer, String>();

    static {
        STATUS_CODES_MAP.put(200, "200 OK");
        STATUS_CODES_MAP.put(400, "400 Bad Request");
        STATUS_CODES_MAP.put(403, "403 Forbidden");
        STATUS_CODES_MAP.put(404, "404 Not Found");
        STATUS_CODES_MAP.put(500, "500 Internal Server Error");
        STATUS_CODES_MAP.put(501, "501 Not Implemented");

        CONTENT_TYPE_MAP.put(1, "Content-Type: image/jpeg");
        CONTENT_TYPE_MAP.put(2, "Content-Type: image/gif");
        CONTENT_TYPE_MAP.put(3, "Content-Type: application/x-zip-compressed");
        CONTENT_TYPE_MAP.put(5, "Content-Type: text/html");
    }

    public void handleGET(String requestURL, DataOutputStream toClient) {
        String mBytesString = null;
        byte[] mBytesArray = null;

        try {
            mBytesArray = getFileContent(requestURL.trim());
            mBytesString = new String(mBytesArray, "UTF-8");

            if (mBytesString.startsWith("404 File Not Found"))
                new ServerHelper().writeToClient(toClient, null, 404, 5);
            else
                new ServerHelper().writeToClient(toClient, mBytesArray, 200, 5);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void handlePOST(BufferedReader inFromClient, DataOutputStream toClient) throws IOException {
        String lineString = "";
        StringBuilder postValues = null;
        Integer contentLength = null;

        while ((lineString = inFromClient.readLine()) != null) {
            String tmpContentLength = "Content-Length: ";
            System.out.println(lineString);
            if (lineString.length() < 1) break;

            if (lineString.startsWith(tmpContentLength)) {
                contentLength = Integer.parseInt(lineString.substring(tmpContentLength.length(), lineString.length()));
            }
        }
        System.out.println("out of while");

        postValues = new StringBuilder();
        while (contentLength > 0) {
            postValues.append((char) inFromClient.read());
            contentLength--;
        }

        StringBuilder postParsed = getPostReply(postValues);
        String tmpPostParsed = postParsed.toString();
        byte[] mBytesArray = tmpPostParsed.getBytes(Charset.forName("UTF-8"));
        writeToClient(toClient, mBytesArray, 200, 5);
    }

    public void handleBadRequest(DataOutputStream toClient) {
    }

    private static byte[] getFileContent(String requestURL) {

        File localFile = null;
        byte[] myByteArray = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        if (requestURL.length() > 1) {
            localFile = new File(requestURL.trim());
        } else {
            localFile = new File(BASEPATH + INDEXFILE);
        }

        myByteArray = new byte[(int) localFile.length()];
        try {
            fis = new FileInputStream(localFile);
            bis = new BufferedInputStream(fis);
            bis.read(myByteArray, 0, myByteArray.length);
        } catch (IOException e) {
            String errorString = "404 File Not Found";
            myByteArray = errorString.getBytes(Charset.forName("UTF-8"));
            return myByteArray;
        }

        return myByteArray;
    }

    private static String constructHttpHeader(int returnCode, int fileType) {
        String s = PROTOCOL + NEWLINE;
        s = s + STATUS_CODES_MAP.get(returnCode) + NEWLINE;
        s = s + "Connection: close" + NEWLINE;
        s = s + "Server: SimpleJavaServer v0" + NEWLINE;

        //Construct the right Content-Type for the header.
        //This is so the browser knows what to do with the
        //file, you may know the browser dosen't look on the file
        //extension, it is the servers job to let the browser know
        //what kind of file is being transmitted. You may have experienced
        //if the server is miss configured it may result in
        //pictures displayed as text!
        s = s + CONTENT_TYPE_MAP.get(fileType) + NEWLINE;
        s = s + NEWLINE; // Marks the end of HTTP Header
        return s;
    }

    public static String getRequestMethod(String request) {
        String[] tmpStringArray = request.split("([\\s\t])+");
        return tmpStringArray[0];
    }

    public static String getRequestURL(String request) {
        String[] tmpStringArray = request.split("([\\s\t])+");
        return tmpStringArray[1];
    }

    public static byte[] badRequestHandler() {
        String errorString = "Bad Request";
        return errorString.getBytes(Charset.forName("UTF-8"));
    }

    private void writeToClient(DataOutputStream toClient, byte[] mBytesArray, int status, int type) {
        try {
            toClient.writeBytes(constructHttpHeader(status, type));
            toClient.write(mBytesArray, 0, mBytesArray.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (toClient != null) {
                try {
                    toClient.flush();
                    toClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Sending...");

    }

    private StringBuilder getPostReply(StringBuilder postValue) {
        String postValueString = postValue.toString();
        String postParts[] = postValueString.split("&");
        StringBuilder retStr = new StringBuilder();

        retStr.append("<h2>POST Values</h2><table>");
        for (String part : postParts) {
            String tmpParts[] = part.split("=");
            String key = tmpParts[0];
            String value = tmpParts[1];

            retStr.append("<tr><td><b>").append(key).append("</b></td><td>").append(value).append("</td></tr>");
        }
        retStr.append("</table>");
        return retStr;
    }
}
