package euphoria.psycho.porn;

public class Native {
    static {
        System.loadLibrary("native-lib");
    }

    public native static String fetch91Porn(String url);

    public native static String fetchCk(String url, String cookie, String userAgent);

    public native static void removeDirectory(String directory);
}
