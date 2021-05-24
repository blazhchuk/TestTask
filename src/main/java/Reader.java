import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Reader {

    private final static Logger LOGGER = Logger.getLogger(Reader.class.getName());

    private OkHttpClient client;

    public Reader(OkHttpClient client) {
        this.client = client;
    }

    public Map<String, Integer> getWordFrequency(String downloadUrl) {
        Request request = new Request.Builder().url(downloadUrl)
                .addHeader("Content-Type", "text/html")
                .build();

        try (Response response = client.newCall(request).execute()) {

            ResponseBody responseBody;

            if (response.code() != 200) {
                LOGGER.severe(String.format("Repo content fetch error. Code: %s", response.code()));
                return Collections.emptyMap();
            } else {
                responseBody = response.body();

                if (responseBody == null) {
                    LOGGER.severe("Repo fetch error. Null response");
                    return Collections.emptyMap();
                }
            }

            InputStream in = responseBody.byteStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            Map<String, Integer> wordToFrequency = new HashMap<>();

            while((line = reader.readLine()) != null) {
                String[] words = line.split(" ");
                for (String word : words) {
                    if (word.length() > 4) {
                        if (wordToFrequency.containsKey(word)) {
                            wordToFrequency.put(word, wordToFrequency.get(word) + 1);
                        } else {
                            wordToFrequency.put(word, 1);
                        }
                    }
                }
            }

            responseBody.close();
            return wordToFrequency;

        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            return Collections.emptyMap();
        }
    }
}
