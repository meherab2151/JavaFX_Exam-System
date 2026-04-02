package org.example.demo;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
public class EduExamServer {

    private static final int MAX_CLIENTS = 50;
    private static final int BACKLOG     = 20;

    private static final Set<PrintWriter> connectedClients =
            Collections.synchronizedSet(new HashSet<>());

    public static void register(PrintWriter writer) {
        connectedClients.add(writer);
    }

    public static void unregister(PrintWriter writer) {
        connectedClients.remove(writer);
    }

    public static void broadcastExamEvent(String examJson) {
        String line = "{\"push\":\"" + Protocol.PUSH_EXAM_EVENT + "\",\"data\":" + examJson + "}";
        synchronized (connectedClients) {
            Iterator<PrintWriter> it = connectedClients.iterator();
            while (it.hasNext()) {
                PrintWriter pw = it.next();
                try {
                    pw.println(line);
                    if (pw.checkError()) it.remove(); 
                } catch (Exception e) {
                    it.remove();
                }
            }
        }
        System.out.println("[Server] Broadcast EXAM_EVENT to " + connectedClients.size() + " client(s).");
    }

    public static void main(String[] args) throws IOException {

        Database.init();
        System.out.println("[Server] Database ready.");

        AtomicInteger clientIdGen = new AtomicInteger(0);
        ExecutorService pool = new ThreadPoolExecutor(
            4, MAX_CLIENTS, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            r -> { Thread t = new Thread(r, "EduExam-client-" + clientIdGen.incrementAndGet());
                   t.setDaemon(true); return t; },
            (r, ex) -> System.err.println("[Server] Connection rejected — too many clients")
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Server] Shutting down...");
            pool.shutdownNow();
            Database.close();
        }, "EduExam-shutdown"));

        try (ServerSocket server = new ServerSocket(Protocol.PORT, BACKLOG)) {
            System.out.printf("[Server] Listening on %s:%d%n", Protocol.HOST, Protocol.PORT);
            System.out.println("[Server] Press Ctrl+C to stop.");
            while (true) {
                Socket client = server.accept();
                client.setSoTimeout(0);
                client.setTcpNoDelay(true);
                pool.execute(new RequestHandler(client, clientIdGen.get()));
            }
        }
    }
}
