package server;

import java.io.*;
import java.net.Socket;

import static server.Constants.*;
import static server.ServerService.*;

public class ClientHandler implements Runnable {

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String request = bufferedReader.readLine();

            if (request == null) {
                bufferedReader.close();
            } else if (request.startsWith(HTTP_GET)) {
                serveGetRequest(request);
            } else if (request.startsWith(UPLOAD)) {
                try {
                    String isValid = bufferedReader.readLine();

                    if (isValid.equals("invalid")) {
                        System.out.println(">> given file name is invalid");

                        bufferedReader.close();
                        socket.close();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                byte[] buffer = new byte[1024];

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(ROOT_DIRECTORY + "\\" + request.substring(7));
                    InputStream in = socket.getInputStream();

                    while (in.read(buffer) > 0) {
                        fileOutputStream.write(buffer);
                    }

                    in.close();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    bufferedReader.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception exception) {
            System.out.println("Error occurred while serving client, error is: " + exception.getMessage());
        }
    }

    private void serveGetRequest(String request) throws IOException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

        File fileContent;
        StringBuilder path = new StringBuilder();
        String[] array = request.split("/");

        for (int i = 1; i < array.length - 1; i++) {
            if (i == (array.length - 2)) {
                path.append(array[i].replace(" HTTP", ""));
            } else {
                path.append(array[i]).append("\\");
            }
        }

        String finalPath = path.toString();
        if (finalPath.equals("")) {
            fileContent = new File(ROOT_DIRECTORY);
        } else {
            finalPath = finalPath.replace("%20", " ") + "\\";
            fileContent = new File(ROOT_DIRECTORY + "\\" + finalPath);
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (fileContent.exists()) {
            if (fileContent.isDirectory()) {
                makeDirectoryResponse(fileContent, finalPath, stringBuilder);
            }
        } else {
            stringBuilder.append(NOT_FOUND_RESPONSE);
        }

        String httpResponse = "";

        if (fileContent.exists()) {
            if (fileContent.isDirectory()) {
                writeHeaderResponse(httpResponse, "HTTP/1.1 200 OK\r\nServer: Java HTTP Server: 1.0\r\nDate: ", stringBuilder, printWriter);
            } else if (fileContent.isFile()) {
                if (fileContent.getName().endsWith(".txt")) {
                    serveTextFile(printWriter, fileContent);
                } else if (isImageFile(fileContent)) {
                    serveImageRequest(printWriter, fileContent);
                } else {
                    serveDownloadRequest(printWriter, fileContent, httpResponse, socket);
                }
            }
        } else {
            serveNotFoundResponse(printWriter, stringBuilder, httpResponse);
        }
    }
}
