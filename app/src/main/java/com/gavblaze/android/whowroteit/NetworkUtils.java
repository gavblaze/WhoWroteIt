package com.gavblaze.android.whowroteit;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

class NetworkUtils {
    
    private static URL createUrl(String string) {
        URL url = null;
        try {
            url = new URL(string);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {

        URLConnection urlConnection = url.openConnection();
        InputStream is = urlConnection.getInputStream();

        String line = null;

        Scanner scanner = new Scanner(is);
        scanner.useDelimiter("\\A");
        if (scanner.hasNext()) {
            line = scanner.next();
        }
        scanner.close();
        return line;
    }

    static String getDataFromNetwork(String string) {
        URL url = createUrl(string);
        String jsonString = null;
        try {
            jsonString = makeHttpRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}
