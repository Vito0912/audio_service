package com.ryanheise.audioservice;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;

import androidx.media.utils.MediaConstants;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class CarBrowseActions {
    static final String DOWNLOAD = "aa.browse.download";
    static final String DELETE = "aa.browse.delete_download";
    static final String DOWNLOADING = "aa.browse.downloading";
    static final String LIMIT_OPTION = "aaos.browseActionLimit";

    private static final Map<String, String> pendingItems = new ConcurrentHashMap<>();

    static boolean supports(String action) {
        return DOWNLOAD.equals(action) || DELETE.equals(action) || DOWNLOADING.equals(action);
    }

    static int limit(boolean automotive, Bundle rootHints) {
        if (!automotive || rootHints == null) {
            return 0;
        }

        return Math.max(0, rootHints.getInt(MediaConstants.BROWSER_ROOT_HINTS_KEY_CUSTOM_BROWSER_ACTION_LIMIT, 0));
    }

    static void addToRoot(Context context, Bundle rootExtras, int limit) {
        if (limit <= 0) {
            return;
        }

        ArrayList<Bundle> actions = new ArrayList<>();
        actions.add(action(context, DOWNLOAD, "Download", "aaos_browse_download"));
        actions.add(action(context, DOWNLOADING, "Downloading", "aaos_browse_downloading"));
        actions.add(action(context, DELETE, "Delete download", "aaos_browse_delete_download"));

        rootExtras.putParcelableArrayList(MediaConstants.BROWSER_SERVICE_EXTRAS_KEY_CUSTOM_BROWSER_ACTION_ROOT_LIST, actions);
    }

    private static Bundle action(Context context, String id, String label, String drawable) {
        Bundle action = new Bundle();
        String icon = "android.resource://" + context.getPackageName() + "/drawable/" + drawable;

        action.putString(MediaConstants.EXTRAS_KEY_CUSTOM_BROWSER_ACTION_ID, id);
        action.putString(MediaConstants.EXTRAS_KEY_CUSTOM_BROWSER_ACTION_LABEL, label);
        action.putString(MediaConstants.EXTRAS_KEY_CUSTOM_BROWSER_ACTION_ICON_URI, icon);

        return action;
    }

    static boolean begin(String mediaId, String action) {
        return pendingItems.putIfAbsent(mediaId, DELETE.equals(action) ? DELETE : DOWNLOADING) == null;
    }

    static void finish(String mediaId) {
        pendingItems.remove(mediaId);
    }

    static void clear() {
        pendingItems.clear();
    }

    static MediaDescriptionCompat restrict(MediaDescriptionCompat description, int limit) {
        Bundle extras = description.getExtras() == null ? new Bundle() : new Bundle(description.getExtras());
        ArrayList<String> requested = extras.getStringArrayList(MediaConstants.DESCRIPTION_EXTRAS_KEY_CUSTOM_BROWSER_ACTION_ID_LIST);
        extras.remove(MediaConstants.DESCRIPTION_EXTRAS_KEY_CUSTOM_BROWSER_ACTION_ID_LIST);

        if (limit > 0 && requested != null) {
            ArrayList<String> actions = new ArrayList<>();

            String pendingAction = pendingItems.get(description.getMediaId());
            if (pendingAction != null) {
                actions.add(pendingAction);
            } else {
                for (String action : requested) {
                    if (supports(action) && actions.size() < limit) {
                        actions.add(action);
                    }
                }
            }

            extras.putStringArrayList(MediaConstants.DESCRIPTION_EXTRAS_KEY_CUSTOM_BROWSER_ACTION_ID_LIST, actions);
        }

        return new MediaDescriptionCompat.Builder()
                .setMediaId(description.getMediaId())
                .setTitle(description.getTitle())
                .setSubtitle(description.getSubtitle())
                .setDescription(description.getDescription())
                .setIconBitmap(description.getIconBitmap())
                .setIconUri(description.getIconUri())
                .setMediaUri(description.getMediaUri())
                .setExtras(extras)
                .build();
    }

    static Bundle response(String mediaId, String message) {
        Bundle response = new Bundle();
        response.putString(MediaConstants.EXTRAS_KEY_CUSTOM_BROWSER_ACTION_RESULT_REFRESH_ITEM, mediaId);

        if (message != null) {
            response.putString(MediaConstants.EXTRAS_KEY_CUSTOM_BROWSER_ACTION_RESULT_MESSAGE, message);
        }

        return response;
    }

    static Bundle fromResult(String mediaId, Map<?, ?> value) {
        Object message = value.get("message");
        return response(mediaId, message instanceof String ? (String) message : null);
    }
}
