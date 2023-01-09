package server;

public class Constants {
    public static final String HTTP_GET = "GET";
    public static final String UPLOAD = "UPLOAD";
    public static final int SERVER_PORT_NUMBER = 5056;
    public static final int NUMBER_OF_SERVER_THREAD = 100;
    public static final String ROOT_DIRECTORY = "D://";
    public static final Integer CHUNK_SIZE = 5000;
    public static final String NOT_FOUND_RESPONSE = "<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html;" +
            " charset=UTF-8\">\n<link rel=\"icon\" href=\"data:,\">\n</head>\n<body>\n <h1> 404: Page not found </h1>\n </body>\n</html>";
    public static final String TXT_HTML_RESPONSE = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>{title}</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<div style=\"display: flex; align-items: center; justify-content: center; width: 100vw; height: 100vh;\">\n" +
            "    <pre style=\"text-align: left;font-size: 1.5em;\">{src}</pre>\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>";
    public static final String IMAGE_HTML_RESPONSE = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>{title}</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "  <div style=\"display: flex; align-items: center; justify-content: center; width: 100vw; height: 100vh;\">\n" +
            "    <img style=\"max-width: 100vw; max-height: 100vh;\" src=\"{src}\">\n" +
            "  </div>\n" +
            "</body>\n" +
            "</html>";
}
