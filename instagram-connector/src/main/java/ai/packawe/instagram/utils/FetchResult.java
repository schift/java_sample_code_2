package ai.packawe.instagram.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FetchResult {

    public List<Map<String, String>> usersCounts;
    public List<Map<String, String>> usersFeeds;
    public List<Map<String, String>> tags;

    public FetchResult() {
        usersCounts = new LinkedList<Map<String, String>>();
        usersFeeds = new LinkedList<Map<String, String>>();
        tags = new LinkedList<Map<String, String>>();
    }
}
