package client;

import java.io.*;
import java.net.Socket;

import static server.Constants.SERVER_PORT_NUMBER;

public class Client implements Runnable {

    private File inputFile;
    private Thread thread;

    public Client(String filename) {
        inputFile = new File(filename);
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        try (Socket socket = new Socket("localhost", SERVER_PORT_NUMBER)) {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

            printWriter.write("UPLOAD " + inputFile.getName() + "\r\n");
            printWriter.flush();

            if (!isFileExisting(printWriter)) {
                printWriter.close();
                return;
            }

            int count;
            byte[] buffer = new byte[1024];

            OutputStream out = socket.getOutputStream();
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputFile));

            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
                out.flush();
            }

            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isFileExisting(PrintWriter printWriter) {
        if (inputFile.exists()) {
            printWriter.write("valid\r\n");
            printWriter.flush();
            return true;
        } else {
            printWriter.write("invalid file\r\n");
            printWriter.flush();
            System.out.println(">> given file name is invalid");

            return false;
        }
    }
}
