/*
 * Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

class NetworkService implements Runnable {
    private final ServerSocket serverSocket;
    private final ExecutorService pool;

    public NetworkService(int port)
    throws IOException {
        serverSocket = new ServerSocket(port);
        pool = Executors.newFixedThreadPool(1);
    }

    public void run() { // run the service
        try {
            for (;;) {
                pool.execute(new Handler(serverSocket.accept()));
            }
        } catch (IOException ex) {
            pool.shutdown();
        }
    }
}

class Handler implements Runnable {
    private final Socket socket;
    Handler(Socket socket) { this.socket = socket; }
    public void run() {
        try (
            PrintWriter out =
                new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        ) {

            String inputLine, outputLine;

            // Initiate conversation with client
            KnockKnockProtocol kkp = new KnockKnockProtocol();
            outputLine = kkp.processInput(null);

            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                System.out.println("received");
                outputLine = kkp.processInput(inputLine);
                System.out.println(outputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye."))
                    break;
            }
        } catch (IOException e) {
            System.out.println("connexion aborted");
            System.out.println(e.getMessage());
        }
    }
}

public class KnockKnockServer {

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java KnockKnockServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);


        NetworkService NS = new NetworkService(portNumber);
        NS.run();

        // try (
        //     ServerSocket serverSocket = new ServerSocket(portNumber);
        //     Socket clientSocket = serverSocket.accept();
        //     PrintWriter out =
        //         new PrintWriter(clientSocket.getOutputStream(), true);
        //     BufferedReader in = new BufferedReader(
        //         new InputStreamReader(clientSocket.getInputStream()));
        // ) {
        //
        //     String inputLine, outputLine;
        //
        //     // Initiate conversation with client
        //     KnockKnockProtocol kkp = new KnockKnockProtocol();
        //     outputLine = kkp.processInput(null);
        //     out.println(outputLine);
        //
        //     while ((inputLine = in.readLine()) != null) {
        //         System.out.println(inputLine);
        //         outputLine = kkp.processInput(inputLine);
        //         out.println(outputLine);
        //         if (outputLine.equals("Bye."))
        //             break;
        //     }
        // } catch (IOException e) {
        //     System.out.println("Exception caught when trying to listen on port "
        //         + portNumber + " or listening for a connection");
        //     System.out.println(e.getMessage());
        // }
    }
}
