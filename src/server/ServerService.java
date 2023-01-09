package server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;

import static server.Constants.*;

public class ServerService {

    public static void serveNotFoundResponse(PrintWriter printWriter, StringBuilder stringBuilder, String httpResponse) {
        writeHeaderResponse(httpResponse, "HTTP/1.1 404 NOT FOUND\r\nServer: Java HTTP Server: 1.0\r\nDate: ", stringBuilder, printWriter);
        System.out.println(">> 404: Page not found");
    }

    public static void writeHeaderResponse(String httpResponse, String x, StringBuilder stringBuilder, PrintWriter printWriter) {
        httpResponse += x + new Date() + "\r\nContent-Type: text/html\r\nContent-Length: " + stringBuilder.toString().length() + "\r\n";
        printWriter.write(httpResponse);
        printWriter.write("\r\n");
        printWriter.write(stringBuilder.toString());
        printWriter.flush();
        printWriter.close();
    }

    public static boolean isImageFile(File src) throws IOException {
        String mimetype = Files.probeContentType(src.toPath());
        return mimetype != null && mimetype.split("/")[0].equals("image");
    }

    public static byte[] getFileChunk(byte[] bytes, int startIndex, int length) {
        byte[] chunk = new byte[length];
        System.arraycopy(bytes, startIndex, chunk, 0, length);
        return chunk;
    }

    public static void makeDirectoryResponse(File fileContent, String finalPath, StringBuilder stringBuilder) {

        File[] listOfContent;
        listOfContent = fileContent.listFiles();
        stringBuilder.append("<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html;" +
                " charset=UTF-8\">\n<link rel=\"icon\" href=\"data:,\">\n</head>\n<body>\n");

        if (listOfContent != null) {
            for (File file : listOfContent) {
                if (file.isDirectory()) {
                    stringBuilder.append("<font size=\"7\"><b><a href=\"http://localhost:" + SERVER_PORT_NUMBER + "/")
                            .append(finalPath.replace("\\", "/")).append(file.getName()).append("\"> ")
                            .append(file.getName()).append(" </a></b></font><br>\n");
                } else if (file.isFile()) {
                    stringBuilder.append("<font size=\"7\"><a href=\"http://localhost:" + SERVER_PORT_NUMBER + "/")
                            .append(finalPath.replace("\\", "/")).append(file.getName()).append("\"> ")
                            .append(file.getName()).append(" </a></font><br>\n");
                }
            }
        }

        stringBuilder.append("</body>\n</html>");
    }

    public static void serveImageRequest(PrintWriter printWriter, File fileContent) throws IOException {
        FileInputStream fin = new FileInputStream(fileContent);
        byte[] imageByteArray = new byte[(int) fileContent.length()];
        fin.read(imageByteArray);
        String base64 = Base64.getEncoder().encodeToString(imageByteArray);

        String content = IMAGE_HTML_RESPONSE;

        content = content.replace("{title}", fileContent.getName());
        content = content.replace("{src}", "data:" + Files.probeContentType(fileContent.toPath()) + ";base64, " + base64);

        String htmlResponse = "HTTP/1.1 " + 200 + " OK\r\n" +
                "Server: Java HTTP Server: 1.0\r\n" +
                "Date: " + new Date() + "\r\n" +
                "Content-Type: " + "text/html" + "\r\n" +
                "Content-Length: " + content.length() + "\r\n" + "\r\n" + content;

        printWriter.write(htmlResponse);
        printWriter.flush();
        printWriter.close();
    }

    public static void serveTextFile(PrintWriter printWriter, File fileContent) throws IOException {
        String content = TXT_HTML_RESPONSE;
        File file = new File(fileContent.getPath());
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }

        content = content.replace("{title}", fileContent.getName());
        content = content.replace("{src}", sb);

        String htmlResponse = "HTTP/1.1 " + 200 + " OK\r\n" +
                "Server: Java HTTP Server: 1.0\r\n" +
                "Date: " + new Date() + "\r\n" +
                "Content-Type: " + "text/html" + "\r\n" +
                "Content-Length: " + content.length() + "\r\n" + "\r\n" + content;

        printWriter.write(htmlResponse);
        printWriter.flush();
        printWriter.close();
    }

    public static void serveDownloadRequest(PrintWriter printWriter, File fileContent, String httpResponse, Socket socket) {
        httpResponse += "HTTP/1.1 200 OK\r\nServer: Java HTTP Server: 1.0\r\nDate: " + new Date()
                + "\r\nContent-Type: application/octet-stream\r\nContent-Length: " + fileContent.length() + "\r\n"
                + "Content-Disposition: attachments\r\n";

        printWriter.write(httpResponse);
        printWriter.write("\r\n");
        printWriter.flush();

        new Thread(() -> {
            try {
                OutputStream out = socket.getOutputStream();
                FileInputStream fileInputStream = new FileInputStream(fileContent);

                byte[] bytes = fileInputStream.readAllBytes();
                try {
                    for (int i = 0; i * CHUNK_SIZE + CHUNK_SIZE <= bytes.length; i++) {
                        System.out.println("Downloading chunk: " + i);
                        out.write(getFileChunk(bytes, i * CHUNK_SIZE, CHUNK_SIZE));
                    }

                    out.write(getFileChunk(bytes, (bytes.length / CHUNK_SIZE) * CHUNK_SIZE, bytes.length % CHUNK_SIZE));
                } catch (IOException e) {
                    System.out.println("File download aborted");
                }

                out.flush();
                out.close();
                fileInputStream.close();
            } catch (IOException e) {
                System.out.println("Error occurred while serving file download request, error is: " + e.getMessage());
            }
        }).start();
    }
}
