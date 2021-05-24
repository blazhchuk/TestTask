import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String... args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter username: ");

        String username = scanner.next();

        if (username.length() == 0) {
            throw new IllegalArgumentException("Empty username provided");
        }

        OkHttpClient client = new OkHttpClient();

        Fetcher fetcher = new Fetcher(client, username);
        Reader reader = new Reader(client);

        List<String> repos = fetcher.fetchReposForUser();

        Map<String, Integer> joinedMap = new HashMap<>();

        repos.forEach(repo -> {
            List<String> paths = fetcher.fetchRepoPaths(repo);

            for (String path : paths) {
                String downloadUrl = fetcher.getDownloadUrl(repo, path);

                if (downloadUrl != null) {
                    Map<String, Integer> fileMap = reader.getWordFrequency(downloadUrl);
                    //Merge maps
                    fileMap.forEach(
                            (key, value) -> joinedMap.merge(key, value, Integer::sum)
                    );
                }
            }
        });

        Map<String, Integer> sortedWordFrequencyMap = sortByValue(joinedMap);

        int i = 0;

        Iterator<Map.Entry<String, Integer>> iterator = sortedWordFrequencyMap.entrySet().iterator();
        while (i < 3) {
            Map.Entry<String, Integer> entry = iterator.next();
            System.out.println(String.format("Place %s: word %s - %d", i, entry.getKey(), entry.getValue()));
            i++;
        }
    }

    public static Map<String, Integer> sortByValue(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue(Collections.reverseOrder()));

        Map<java.lang.String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
