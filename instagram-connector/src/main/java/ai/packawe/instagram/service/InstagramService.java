package ai.packawe.instagram.service;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jinstagram.Instagram;
import org.jinstagram.entity.tags.TagInfoData;
import org.jinstagram.entity.tags.TagSearchFeed;
import org.jinstagram.entity.users.basicinfo.Counts;
import org.jinstagram.entity.users.basicinfo.UserInfo;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.entity.users.feed.UserFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.pickaxe.instagram.utils.Constants;
import ai.pickaxe.instagram.utils.FetchResult;

public class InstagramService {

    private static final Logger LOG = LoggerFactory.getLogger(InstagramService.class);

    private Properties conf;
    private String[] users;
    private String[] tags;
    private DateTimeFormatter dateFormatter;

    public InstagramService(Properties config) {
        this.conf = config;
        this.users = conf.getProperty(Constants.USERS_PROP_NAME, "").split(",");
        this.tags = conf.getProperty(Constants.TAGS_PROP_NAME, "").split(",");
        dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZoneUTC();
    }

    public FetchResult search(Instagram instagram) {
        String createdAt = dateFormatter.print(DateTime.now(DateTimeZone.UTC));
        FetchResult result = new FetchResult();
        for (String user : users) {
            if (isNotBlank(user)) {
                UserFeedData u = findUserByUsername(instagram, user);
                if (u != null) {
                    // counts
                    try {
                        UserInfo userInfo = instagram.getUserInfo(u.getId());
                        Counts counts = userInfo.getData().getCounts();
                        Map<String, String> res = new HashMap<String, String>();
                        res.put("timestamp", createdAt);
                        res.put("username", user);
                        res.put("followed_by", Integer.toString(counts.getFollowedBy()));
                        result.usersCounts.add(res);
                    } catch (InstagramException e) {
                        LOG.error("Unable to fetch counts for user: " + user, e);
                    }

                    // feed
                    try {
                        MediaFeed userMediaFeed = instagram.getRecentMediaFeed(u.getId());
                        if (userMediaFeed != null && userMediaFeed.getData() != null && !userMediaFeed.getData().isEmpty()) {
                            List<MediaFeedData> feedData = userMediaFeed.getData();
                            for (MediaFeedData mfd : feedData) {
                                Map<String, String> res = new HashMap<String, String>();
                                res.put("timestamp", createdAt);
                                res.put("username", user);
                                res.put("object_id", mfd.getId());
                                res.put("link", mfd.getLink());
                                res.put("created", mfd.getCreatedTime());
                                res.put("type", mfd.getType());
                                res.put("likes", Integer.toString(mfd.getLikes().getCount()));
                                res.put("comments", Integer.toString(mfd.getComments().getCount()));
                                result.usersFeeds.add(res);
                            }
                        } else {
                            LOG.warn("Unable to get feed for user " + user);
                        }
                    } catch (InstagramException e) {
                        LOG.error("Unable to fetch user counts", e);
                    }
                } else {
                    LOG.warn("User '" + user + "' is unknown");
                }
            }
        }

        // tags
        for (String tag : tags) {
            if (isNotBlank(tag)) {
                try {
                    TagSearchFeed searchTags = instagram.searchTags(tag);
                    for (TagInfoData tagInfoData : searchTags.getTagList()) {
                        if (tag.equalsIgnoreCase(tagInfoData.getTagName())) {
                            Map<String, String> res = new HashMap<String, String>();
                            res.put("timestamp", createdAt);
                            res.put("tag", tagInfoData.getTagName());
                            res.put("count", Long.toString(tagInfoData.getMediaCount()));
                            result.tags.add(res);
                        }
                    }
                } catch (InstagramException e) {
                    LOG.error("Unable to fetch counts for tag: " + tag, e);
                }
            }
        }
        return result;
    }

    private UserFeedData findUserByUsername(Instagram instagram, String username) {
        try {
            List<UserFeedData> userList = instagram.searchUser(username, 1).getUserList();
            if (userList != null && !userList.isEmpty()) {
                for (UserFeedData u : userList) {
                    if (username.equals(u.getUserName())) {
                        return u;
                    }
                }
            }
        } catch (InstagramException e) {
            LOG.error("Unable to search user {}, error: {}", username, e);
        }
        return null;
    }

}
