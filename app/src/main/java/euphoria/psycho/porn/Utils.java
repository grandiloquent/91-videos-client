package euphoria.psycho.porn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Process;
import android.util.Log;
import android.util.Pair;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class Utils {

    public static String getDouYinString(String videoId) throws IOException {
        URL url = new URL("https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=" + videoId);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
        int code = urlConnection.getResponseCode();
        if (code < 400 && code >= 200) {
            return Shared.readString(urlConnection);
        } else {
            return null;
        }
    }

    public static boolean getDouYinVideo(Activity activity, String query) {
        Pattern douyin = Pattern.compile("douyin");
        Matcher matcher = douyin.matcher(query);
        if (matcher.find()) {
            ProgressDialog dialog = new ProgressDialog(activity);
            dialog.setMessage("解析中...");
            dialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    String location;
                    String response = null;
                    try {
                        location = getLocation("https://" + Shared.substring(query, "https://", " "));
                        String videoId = Shared.substring(location, "video/", "/?");
                        response = getDouYinString(videoId);
                        JSONObject object = new JSONObject(response);
                        response = object.getJSONArray("item_list").getJSONObject(0)
                                .getJSONObject("video")
                                .getJSONObject("play_addr")
                                .getJSONArray("url_list")
                                .getString(0);
                        response = response.replace("playwm", "play");


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String finalResponse = response;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            Shared.downloadFile(activity,
                                    Shared.md5(query) + ".mp4",
                                    finalResponse,
                                    Shared.USER_AGENT);
                        }
                    });
                }
            }).start();
            return true;
        }
        return false;
    }

    public static boolean getKuaiShouVideo(Activity activity, String query) {
        Pattern douyin = Pattern.compile("kuaishou");
        Matcher matcher = douyin.matcher(query);
        if (matcher.find()) {
            ProgressDialog dialog = new ProgressDialog(activity);
            dialog.setMessage("解析中...");
            dialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    String[] location;
                    String response = null;
                    try {
                        location = getLocationAddCookie("https://" + Shared.substring(query, "https://", " "), null);
                        location = getLocationAddCookie(location[0], null);
                        response = getString(location);
                        response = Shared.substring(response, "\"srcNoMark\":\"", "\"");
                    } catch (Exception e) {
                    }
                    String finalResponse = response;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Shared.downloadFile(activity,
                                    Shared.md5(query) + ".mp4",
                                    finalResponse,
                                    Shared.USER_AGENT);
                            dialog.dismiss();
                        }
                    });
                }
            }).start();
            return true;
        }
        return false;
    }

    public static String getLocation(String uri) throws IOException {
        URL url = new URL(uri);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
        urlConnection.setInstanceFollowRedirects(false);
        int code = urlConnection.getResponseCode();
        if (code < 400 && code >= 200) {
            return urlConnection.getHeaderField("Location");
        } else {
            return null;
        }
    }

    public static String[] getLocationAddCookie(String uri, String[][] headers) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Cookie", "did=web_72fb31b9cb57408aa7bd20e63183ac49; didv=1640743103000; clientid=3");
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
        if (headers != null) {
            for (String[] header : headers) {
                urlConnection.setRequestProperty(header[0], header[1]);
            }
        }
        urlConnection.setInstanceFollowRedirects(false);
        Map<String, List<String>> listMap = urlConnection.getHeaderFields();
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<String, List<String>> header : listMap.entrySet()) {
            if (header.getKey() != null && header.getKey().equalsIgnoreCase("set-cookie")) {
                for (String s : header.getValue()) {
                    stringBuilder.append(Shared.substringBefore(s, "; "))
                            .append("; ");
                }
            }
        }
        return new String[]{urlConnection.getHeaderField("Location"), stringBuilder.toString()};
    }

    public static String getString(String[] uri) throws IOException {
        URL url = new URL("https:" + uri[0]);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.addRequestProperty("Cookie", "did=web_72fb31b9cb57408aa7bd20e63183ac49; didv=1640743103000; clientid=3");
        urlConnection.setRequestProperty("User-Agent", Shared.USER_AGENT);
        int code = urlConnection.getResponseCode();
        if (code < 400 && code >= 200) {
            return Shared.readString(urlConnection);
        } else {
            return null;
        }
    }

    public static String getXVideosString(String uri) throws IOException {
        URL url = new URL(uri);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36");
        int code = urlConnection.getResponseCode();
        if (code < 400 && code >= 200) {
            return Shared.readString(urlConnection);
        } else {
            return null;
        }
    }

    public static String[] getXVideosVideoAddress(String uri) throws IOException {
        String response = getXVideosString(uri);
        if (response == null) {
            return null;
        }
        String title = Shared.substring(response, "html5player.setVideoTitle('", "');");
        String hlsAddress = Shared.substring(response, "html5player.setVideoHLS('", "');");
        String hls = getXVideosString(hlsAddress);
        if (hls == null) {
            return null;
        }
        String[] lines = hls.split("\n");
        List<Pair<Integer, String>> videos = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].contains("#EXT-X-STREAM-INF")) {
                continue;
            }
            videos.add(Pair.create(
                    Integer.parseInt(Shared.substring(lines[i], "NAME=\"", "p\"")),
                    lines[i + 1]
            ));
            i++;
        }
        Collections.sort(videos, (o1, o2) -> o2.first - o1.first);
        return new String[]{title, Shared.substringBeforeLast(hlsAddress, "/") + "/" + videos.get(0).second};
    }

    public static String getCookie(String uri, String[][] headers) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        if (headers != null) {
            for (String[] header : headers) {
                urlConnection.setRequestProperty(header[0], header[1]);
            }
        }
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
        urlConnection.setInstanceFollowRedirects(false);
        for (Entry<String, List<String>> header : urlConnection.getRequestProperties().entrySet()) {
            Log.e("B5aOx2", String.format("getCookie, %s", header.getKey()));
        }
        Map<String, List<String>> listMap = urlConnection.getHeaderFields();
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<String, List<String>> header : listMap.entrySet()) {
            Log.e("B5aOx2", String.format("getCookie, %s", header.getKey()));
            if (header.getKey() != null && header.getKey().equalsIgnoreCase("set-cookie")) {
                for (String s : header.getValue()) {
                    stringBuilder.append(Shared.substringBefore(s, "; "))
                            .append("; ");
                }
            }
        }
        return stringBuilder.toString();
    }

    public static String getString(String uri, String[][] headers) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        if (headers != null) {
            for (String[] header : headers) {
                urlConnection.setRequestProperty(header[0], header[1]);
            }
        }
        int code = urlConnection.getResponseCode();
        if (code < 400 && code >= 200) {
            return Shared.readString(urlConnection);
        } else {
            return null;
        }
    }

    public static int findRange(int mask) {
        int x = 8 - mask;
        int sum = 0;
        for (int i = 0; i < x; i++) {
            sum += Math.pow(2, i);
        }
        return sum;
    }

    public static int findFixedPart(String IPPrefix, int i) {
        String f = IPPrefix.split("\\.")[i];
        return Integer.valueOf(f);
    }

    public static String generateRandomIP(String IPPrefix, Integer mask) {
        String IP = "";
        Random r = new Random();
        if (mask < 8)
            IP = (findFixedPart(IPPrefix, 0) + r.nextInt(findRange(mask))) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
        else if (mask > 7 && mask < 16)
            IP = findFixedPart(IPPrefix, 0) + "." + (findFixedPart(IPPrefix, 1) + r.nextInt(findRange(mask - 8))) + "." + r.nextInt(256) + "." + r.nextInt(256);
        else if (mask > 15 && mask < 24)
            IP = findFixedPart(IPPrefix, 0) + "." + findFixedPart(IPPrefix, 1) + "." + (findFixedPart(IPPrefix, 2) + r.nextInt(findRange(mask - 16))) + "." + r.nextInt(256);
        else if (mask > 23 && mask < 33)
            IP = findFixedPart(IPPrefix, 0) + "." + findFixedPart(IPPrefix, 1) + "." + findFixedPart(IPPrefix, 2) + "." + (findFixedPart(IPPrefix, 3) + r.nextInt(findRange(mask - 24)));
        return IP;
    }
}
