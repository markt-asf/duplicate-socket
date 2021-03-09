package org.apache.markt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkTest {

    // Use static fields so they are easy to find in the heap dump
    private static SocketChannel previousConnection = null;
    private static SocketChannel currentConnection = null;

    public static void main(String[] args) throws Exception {

        // Formatter for timestamps
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        // Create the thread pool for processing requests.
        ExecutorService executor = Executors.newFixedThreadPool(200);

        // Request ID
        long requestID = 0;

        // Create the listening server socket
        ServerSocketChannel serverSock = ServerSocketChannel.open();
        InetSocketAddress addr = new InetSocketAddress("localhost" , 8080);
        serverSock.bind(addr);

        int previousRemotePort = -1;
        int currentRemotePort = -1;

        // Accept loop
        boolean loop = true;

        while (loop) {
            previousConnection = currentConnection;
            previousRemotePort = currentRemotePort;

            currentConnection = serverSock.accept();
            currentRemotePort = ((InetSocketAddress) currentConnection.getRemoteAddress()).getPort();

            if (previousRemotePort == currentRemotePort) {
                loop = false;
            }
            if (previousConnection != null) {
                RequestProcessor rp = new RequestProcessor(previousConnection, requestID);
                executor.execute(rp);
            }

            requestID++;
        }

        System.out.println("Duplicate port detected at " + sdf.format(new Date()));

        HeapDumper.dumpHeap("/home/mark/network-test.hprof", true);

        System.out.println("Previous connection: " + previousConnection);
        System.out.println("      isConnected(): " + previousConnection.isConnected());
        System.out.println("            isOpen(: " + previousConnection.isOpen());
        System.out.println("Current  connection: " + currentConnection);
        System.out.println("      isConnected(): " + currentConnection.isConnected());
        System.out.println("            isOpen(: " + currentConnection.isOpen());

        executor.shutdown();
    }


    private static class RequestProcessor implements Runnable {

        private final SocketChannel connection;
        private final long requestID;

        public RequestProcessor(SocketChannel connection, long requestID) {
            this.connection = connection;
            this.requestID = requestID;
        }


        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                connection.close();
            } catch (IOException | InterruptedException ioe) {
                System.out.println("Request [" + requestID + "] failed");
                ioe.printStackTrace(System.out);
            }
        }
    }
}
