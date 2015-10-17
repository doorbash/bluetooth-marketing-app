import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

public class SendFileTask extends Thread {

    private String btConnectionURL;

    public SendFileTask(String url) {
        this.btConnectionURL = url;
    }

    public void run() {
        FileInputStream stream = null;
        Connection connection = null;
        ClientSession cs = null;
        OutputStream outputStream = null;
        Operation putOperation = null;
        HeaderSet hs = null;
        File f = null;
        int retry = 5;

        synchronized (btConnectionURL) {
            for (int i = 0; i < Main.fileNames.length; i++) {
                System.out.println("sending File: " + Main.fileNames[i].getAbsolutePath());
                System.out.println("sending to: " + btConnectionURL);
                try {
                    // Get file as bytes
                    stream = new FileInputStream(Main.fileNames[i].getAbsolutePath());
                    f = Main.fileNames[i];
                    int size = (int) f.length();
                    byte[] file = new byte[size];
                    stream.read(file);
                    // Filename
                    String filename = f.getName();
                    // Trigger the task in a different thread so it won't
                    // block the
                    // UI
//                HeaderSet hsConnectReply=null;
//                if (connection == null) {
                    connection = Connector.open(btConnectionURL);
                    // connection obtained

                    // now, let's create a session and a headerset objects
                    cs = (ClientSession) connection;
//                }
                    HeaderSet hsConnectReply = cs.connect(cs.createHeaderSet());
                    if (hsConnectReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
                        System.err.println("Error while connecting device");
//                    return;
                        throw new Exception();
                    }

                    hs = cs.createHeaderSet();
                    hs.setHeader(HeaderSet.NAME, filename);
                    hs.setHeader(HeaderSet.TYPE,
                            new MimetypesFileTypeMap().getContentType(new File(filename.concat(filename))));
                    hs.setHeader(HeaderSet.LENGTH, new Long(file.length));

                    putOperation = cs.put(hs);

                    outputStream = putOperation.openOutputStream();
                    outputStream.write(file);
                    // file push complete
                } catch (Exception e) {
                    if (retry > 0) {
                        retry--;
                        i--;
                        try {
                            Thread.sleep(10000);
                        } catch (Exception ex) {
                        }
                    } else {
                        System.err.println("Exception: " + e.getMessage());
                    }
                }
                /// finish
                try {
                    if (outputStream != null) {
//                    outputStream.flush();
                        outputStream.close();
                        outputStream = null;
                    }
                } catch (Exception e) {
                }
                try {
                    if (putOperation != null) {
//                        putOperation.close();
                        putOperation = null;
                    }
                } catch (Exception e) {
                }
                try {
                    if (cs != null) {
                        if (hs != null) {
                            cs.disconnect(hs);
                            hs = null;
                        }
//                        cs.close();
                        cs = null;
                    }
                } catch (Exception e) {
                }
                try {
                    if (connection != null) {
                        connection.close();
                        connection = null;
                    }
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(3000);
                    Thread.sleep(f.length() / 100 * 3);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
