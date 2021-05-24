import com.google.gson.Gson;
import okhttp3.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Fetcher {

    private final static Logger LOGGER = Logger.getLogger(Fetcher.class.getName());

    private Gson gson = new Gson();

    private OkHttpClient client;
    private String user;

    public Fetcher(OkHttpClient client, String user) {
        this.client = client;
        this.user = user;
    }

    public List<String> fetchReposForUser() throws IOException {
        // https://api.github.com/users/{user}/repos
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "/users/" + user + "/repos")
                .build();

        try (Response response = client.newCall(request).execute()) {

            String respBodyStr = getRespBodyString(response);

            if (respBodyStr == null) {
                throw new RuntimeException(String.format("Repos fetch error. Code: %s", response.code()));
            }

            List<Map<String, ?>> repoList = gson.fromJson(respBodyStr, List.class);

            return repoList.stream()
                    .filter(r -> Boolean.FALSE.equals(r.get("private")))
                    .map(r -> (String) r.get("name"))
                    .collect(Collectors.toList());
        }
    }

    public List<String> fetchRepoPaths(String repo) {
        // https://api.github.com/repos/{user}/{repo}/git/trees/{branch}?recursive=1
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "/repos/" + user + "/" + repo + "/git/trees/master?recursive=1")
                .build();

        try (Response response = client.newCall(request).execute()) {

            String respBodyStr = getRespBodyString(response);

            if (respBodyStr == null) {
                return Collections.emptyList();
            }

            Map<String, ?> tree = gson.fromJson(respBodyStr, Map.class);

            List<Map<String, ?>> trees = (List<Map<String, ?>>) tree.get("tree");

            return trees.stream()
                    .map(r -> (String) r.get("path"))
                    .filter(p -> p.toLowerCase().endsWith("readme.md"))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            return Collections.emptyList();
        }
    }

    public String getDownloadUrl(String repo, String path) {
        // https://api.github.com/repos/{user}/{repo}/contents/{path}
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "/repos/" + user + "/" + repo + "/contents/" + path)
                .build();

        try (Response response = client.newCall(request).execute()) {

            String respBodyStr = getRespBodyString(response);

            if (respBodyStr == null) {
                return null;
            }

            Map<String, ?> content = gson.fromJson(respBodyStr, Map.class);

            return Optional.ofNullable((String) content.get("download_url")).orElse(null);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            return null;
        }
    }

    @Nullable
    private String getRespBodyString(Response response) throws IOException {
        String respBodyStr;

        if (response.code() != 200) {
            LOGGER.severe(String.format("Repo content fetch error. Code: %s", response.code()));
            return null;
        } else {
            ResponseBody responseBody = response.body();

            if (responseBody == null) {
                LOGGER.severe("Repo fetch error. Null response");
                return null;
            }

            respBodyStr = responseBody.string();
        }
        return respBodyStr;
    }
}
