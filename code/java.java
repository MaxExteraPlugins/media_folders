package com.maxplugins.mediafolders;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.recyclerview.widget.RecyclerView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DocumentObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_stories;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.EmojiView;
import org.telegram.ui.ContentPreviewViewer;
import org.telegram.ui.LaunchActivity;

/* JADX INFO: loaded from: C:\Users\max\Desktop\ETG\PluginSystem\projects\media_folders\extracted_classes.dex */
public class Main {

    public static boolean isRussian() {
        try {
            Locale loc = LocaleController.getInstance().getCurrentLocale();
            if (loc != null) {
                String l = loc.getLanguage();
                if (l != null && (l.startsWith("ru") || l.startsWith("uk") || l.startsWith("be"))) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        try {
            Locale loc = Locale.getDefault();
            if (loc != null) {
                String l = loc.getLanguage();
                if (l != null && (l.startsWith("ru") || l.startsWith("uk") || l.startsWith("be"))) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static String tr(String ru, String en) {
        return isRussian() ? ru : en;
    }

    public static int getDrawableRes(String name) {
        try {
            Context ctx = ApplicationLoader.applicationContext;
            if (ctx != null) {
                int id = ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
                if (id != 0) return id;
            }
        } catch (Throwable ignored) {}
        try {
            Class<?> clazz = Class.forName("org.telegram.messenger.R$drawable");
            Field f = clazz.getField(name);
            return f.getInt(null);
        } catch (Throwable ignored) {}
        return 0;
    }

    private static void sendBackupDocument(File file, String caption, long dialogId) {
        try {
            AccountInstance ai = AccountInstance.getInstance(UserConfig.selectedAccount);
            String path = file.getAbsolutePath();
            for (java.lang.reflect.Method m : SendMessagesHelper.class.getMethods()) {
                if ("prepareSendingDocument".equals(m.getName())) {
                    Class<?>[] types = m.getParameterTypes();
                    if (types.length >= 7 && types[0] == AccountInstance.class) {
                        try {
                            Object[] args = new Object[types.length];
                            args[0] = ai;
                            args[1] = path;
                            args[2] = path;
                            args[3] = null;
                            args[4] = caption;
                            args[5] = null;
                            args[6] = Long.valueOf(dialogId);
                            for (int i = 7; i < types.length; i++) {
                                if (types[i] == Boolean.TYPE) {
                                    args[i] = Boolean.FALSE;
                                } else if (types[i] == Integer.TYPE) {
                                    args[i] = Integer.valueOf(0);
                                } else if (types[i] == Long.TYPE) {
                                    args[i] = Long.valueOf(0L);
                                } else {
                                    args[i] = null;
                                }
                            }
                            m.invoke(null, args);
                            return;
                        } catch (Throwable t) {
                            err(t, "prepareSendingDocument invoke");
                        }
                    }
                }
            }
        } catch (Throwable t) {
            err(t, "sendBackupDocument");
        }
    }

    static final int BAR_HEIGHT_DP = 44;
    static final String E = "emoji";
    static final String EMOJI_FOLDERS_KEY = "emoji_folders_json";
    static final String EMOJI_TAB_ORDER_KEY = "emoji_tab_order_json";
    static final String G = "gif";
    static final String GIF_FOLDERS_KEY = "gif_folders_json";
    static final String GIF_TAB_ORDER_KEY = "gif_tab_order_json";
    static Handler H = null;
    public static final String PLUGIN_ID = "media_folders";
    static final String PREFS_NAME = "media_folders_data";
    static final String S = "sticker";
    static final String STICKER_FOLDERS_KEY = "sticker_folders_json";
    static final String STICKER_TAB_ORDER_KEY = "sticker_tab_order_json";
    public static final String TAG = "MediaFolders";
    static boolean loaded;
    static final List<XC_MethodHook.Unhook> hooks = new ArrayList();
    static final Set<Class<?>> hookedGifAdapterClasses = new HashSet();
    static final Map<Object, FolderBarManager> GIF_MANAGERS = new IdentityHashMap();
    static final Map<Object, FolderBarManager> STICKER_MANAGERS = new IdentityHashMap();
    static final Map<Object, FolderBarManager> EMOJI_MANAGERS = new IdentityHashMap();
    static final Set<Object> scrollHookedGrids = Collections.newSetFromMap(new IdentityHashMap());
    static final XC_MethodHook attachHook = new XC_MethodHook() { // from class: com.maxplugins.mediafolders.Main.1
        public void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
            try {
                final Object obj = methodHookParam.thisObject;
                if (obj == null) {
                    return;
                }
                Main.ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.1.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        Main.injectAll(obj);
                    }
                }, 150L);
            } catch (Throwable th) {
            }
        }
    };
    static final XC_MethodHook updateGifTabsHook = new XC_MethodHook() { // from class: com.maxplugins.mediafolders.Main.2
        public void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
            Object obj;
            try {
                if (Main.hideTrending() && (obj = methodHookParam.thisObject) != null) {
                    Object objGf = Main.gf(obj, "gifTabs");
                    if (objGf instanceof View) {
                        ((View) objGf).setVisibility(8);
                    }
                    FolderBarManager folderBarManagerFindGifManager = Main.findGifManager(obj);
                    if (folderBarManagerFindGifManager != null) {
                        folderBarManagerFindGifManager.applyFolderFilter();
                    }
                }
            } catch (Throwable th) {
            }
        }
    };
    static final XC_MethodHook recentGifsHook = new XC_MethodHook() { // from class: com.maxplugins.mediafolders.Main.3
        public void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
            FolderBarManager folderBarManagerFindGifManager;
            try {
                Object obj = methodHookParam.thisObject;
                if (obj != null && (folderBarManagerFindGifManager = Main.findGifManager(obj)) != null) {
                    folderBarManagerFindGifManager.allRecentGifs = null;
                    folderBarManagerFindGifManager.applyFolderFilter();
                }
            } catch (Throwable th) {
            }
        }
    };
    static final XC_MethodHook updateStickerTabsHook = new XC_MethodHook() { // from class: com.maxplugins.mediafolders.Main.4
        public void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
            FolderBarManager folderBarManagerFindStickerManager;
            try {
                Object obj = methodHookParam.thisObject;
                if (obj != null && (folderBarManagerFindStickerManager = Main.findStickerManager(obj)) != null) {
                    folderBarManagerFindStickerManager.applyFolderFilter();
                }
            } catch (Throwable th) {
            }
        }
    };
    static final XC_MethodHook searchHook = new XC_MethodHook() { // from class: com.maxplugins.mediafolders.Main.5
        public void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
            Object[] objArr;
            try {
                if (Main.hideTrending() && (objArr = methodHookParam.args) != null && objArr.length != 0) {
                    Object obj = objArr[0];
                    if (obj == null || String.valueOf(obj).trim().isEmpty()) {
                        methodHookParam.setResult((Object) null);
                    }
                }
            } catch (Throwable th) {
            }
        }
    };
    static final XC_MethodHook processResponseHook = new XC_MethodHook() { // from class: com.maxplugins.mediafolders.Main.6
        public void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
            try {
                if (Main.hideTrending()) {
                    methodHookParam.setResult((Object) null);
                }
            } catch (Throwable th) {
            }
        }
    };
    static final XC_MethodHook popupShowHook = new XC_MethodHook() { // from class: com.maxplugins.mediafolders.Main.7
        public void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
            Object objGf;
            final String str;
            Object objGf2;
            try {
                final ContentPreviewViewer contentPreviewViewer = ContentPreviewViewer.getInstance();
                if (contentPreviewViewer == null || (objGf = Main.gf(contentPreviewViewer, "currentContentType")) == null) {
                    return;
                }
                int iIntValue = ((Number) objGf).intValue();
                if (iIntValue == 0) {
                    str = Main.S;
                } else if (iIntValue == 1) {
                    str = Main.G;
                } else if (iIntValue != 2) {
                    return;
                } else {
                    str = Main.E;
                }
                Object objGf3 = Main.gf(contentPreviewViewer, "menuVisible");
                if (!(objGf3 instanceof Boolean) || !((Boolean) objGf3).booleanValue() || (objGf2 = Main.gf(contentPreviewViewer, "currentDocument")) == null || Main.gf(contentPreviewViewer, "popupWindow") == null) {
                    return;
                }
                final Object objGf4 = Main.gf(contentPreviewViewer, "resourcesProvider");
                final String strValueOf = String.valueOf(Main.gf(objGf2, "id"));
                Main.ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.7.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        Main.addPopupMenuItem(contentPreviewViewer, str, strValueOf, objGf4);
                    }
                }, 50L);
            } catch (Throwable th) {
            }
        }
    };
    static final XC_MethodHook isInternalUriHook = new XC_MethodHook() {
        @Override
        public void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
            try {
                Object[] objArr = methodHookParam.args;
                if (objArr != null && objArr.length > 0 && objArr[0] != null) {
                    if (objArr[0] instanceof Uri) {
                        String path = ((Uri) objArr[0]).getPath();
                        if (path != null && (path.endsWith(".mediafolder_backup") || path.endsWith(".giffolder_backup"))) {
                            methodHookParam.setResult(Boolean.FALSE);
                        }
                    }
                }
            } catch (Throwable th) {
            }
        }
    };
    static final XC_MethodHook openForViewHook = new XC_MethodHook() { // from class: com.maxplugins.mediafolders.Main.8
        @Override
        public void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
            try {
                Object[] objArr = methodHookParam.args;
                if (objArr == null || objArr.length == 0 || objArr[0] == null) {
                    return;
                }
                if (objArr[0] instanceof MessageObject) {
                    final MessageObject messageObject = (MessageObject) objArr[0];
                    String fileName = messageObject.getFileName();
                    if (fileName != null && (fileName.endsWith(".mediafolder_backup") || fileName.endsWith(".giffolder_backup"))) {
                        methodHookParam.setResult(true);
                        Main.ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.8.0
                            @Override // java.lang.Runnable
                            public final void run() {
                                Main.handleBackupFile(messageObject);
                            }
                        });
                    }
                } else if (objArr[0] instanceof File) {
                    final File file = (File) objArr[0];
                    String fileName = (objArr.length > 1 && (objArr[1] instanceof String)) ? (String) objArr[1] : file.getName();
                    if (fileName != null && (fileName.endsWith(".mediafolder_backup") || fileName.endsWith(".giffolder_backup"))) {
                        methodHookParam.setResult(true);
                        Main.ui(new Runnable() {
                            @Override // java.lang.Runnable
                            public final void run() {
                                Main.handleBackupFromFile(file);
                            }
                        });
                    }
                }
            } catch (Throwable th) {
            }
        }
    };

    static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, 0);
    }

    public static void load() {
        try {
            if (loaded) {
                return;
            }
            H = new Handler(Looper.getMainLooper());
            setupHooks();
            loaded = true;
        } catch (Throwable th) {
            err(th, "load");
        }
    }

    public static void unload() {
        try {
            loaded = false;
            if (H == null) {
                return;
            }
            H.post(new Runnable() { // from class: com.maxplugins.mediafolders.Main.20
                @Override // java.lang.Runnable
                public final void run() {
                    Main.lambda$unload$0();
                }
            });
        } catch (Throwable th) {
            err(th, "unload");
        }
    }

    static /* synthetic */ void lambda$unload$0() {
        try {
            Iterator it = new ArrayList(hooks).iterator();
            while (it.hasNext()) {
                ((XC_MethodHook.Unhook) it.next()).unhook();
            }
            hooks.clear();
            hookedGifAdapterClasses.clear();
            Iterator it2 = new ArrayList(GIF_MANAGERS.values()).iterator();
            while (it2.hasNext()) {
                ((FolderBarManager) it2.next()).removeFolderBar();
            }
            GIF_MANAGERS.clear();
            Iterator it3 = new ArrayList(STICKER_MANAGERS.values()).iterator();
            while (it3.hasNext()) {
                ((FolderBarManager) it3.next()).removeFolderBar();
            }
            STICKER_MANAGERS.clear();
            Iterator it4 = new ArrayList(EMOJI_MANAGERS.values()).iterator();
            while (it4.hasNext()) {
                ((FolderBarManager) it4.next()).removeFolderBar();
            }
            EMOJI_MANAGERS.clear();
        } catch (Throwable th) {
            err(th, "unload");
        }
    }

    static void err(Throwable th, String str) {
        try {
            Log.e(TAG, "[" + str + "]: " + Log.getStackTraceString(th));
        } catch (Throwable th2) {
        }
    }

    static void ui(Runnable runnable) {
        if (H == null) {
            return;
        }
        H.post(runnable);
    }

    static void ui(Runnable runnable, long j) {
        if (H == null) {
            return;
        }
        H.postDelayed(runnable, j);
    }

    static Object gf(Object obj, String str) {
        if (obj == null) {
            return null;
        }
        for (Class<?> superclass = obj.getClass(); superclass != null && superclass != Object.class; superclass = superclass.getSuperclass()) {
            try {
                Field declaredField = superclass.getDeclaredField(str);
                declaredField.setAccessible(true);
                return declaredField.get(obj);
            } catch (NoSuchFieldException e) {
            } catch (Throwable th) {
                return null;
            }
        }
        return null;
    }

    static void sf(Object obj, String str, Object obj2) {
        if (obj == null) {
            return;
        }
        for (Class<?> superclass = obj.getClass(); superclass != null && superclass != Object.class; superclass = superclass.getSuperclass()) {
            try {
                Field declaredField = superclass.getDeclaredField(str);
                declaredField.setAccessible(true);
                declaredField.set(obj, obj2);
                return;
            } catch (NoSuchFieldException e) {
            } catch (Throwable th) {
                return;
            }
        }
    }

    static void notifyAdapter(Object obj) {
        if (obj == null) {
            return;
        }
        try {
            obj.getClass().getMethod("notifyDataSetChanged", new Class[0]).invoke(obj, new Object[0]);
        } catch (Throwable th) {
        }
    }

    static void hookMethodsByName(Class<?> cls, String str, XC_MethodHook xC_MethodHook) {
        try {
            for (Method method : cls.getDeclaredMethods()) {
                if (method.getName().equals(str)) {
                    try {
                        method.setAccessible(true);
                        hooks.add(XposedBridge.hookMethod(method, xC_MethodHook));
                    } catch (Throwable th) {
                    }
                }
            }
        } catch (Throwable th2) {
            err(th2, "hookMethodsByName " + str);
        }
    }

    static String keyFor(String str, String str2) {
        return str2 + "_" + str;
    }

    static String foldersKey(String str) {
        if (G.equals(str)) {
            return GIF_FOLDERS_KEY;
        }
        return S.equals(str) ? STICKER_FOLDERS_KEY : EMOJI_FOLDERS_KEY;
    }

    static String orderKey(String str) {
        if (G.equals(str)) {
            return GIF_TAB_ORDER_KEY;
        }
        return S.equals(str) ? STICKER_TAB_ORDER_KEY : EMOJI_TAB_ORDER_KEY;
    }

    static JSONArray folders(String str) {
        try {
            return new JSONArray(prefs().getString(foldersKey(str), "[]"));
        } catch (Throwable th) {
            return new JSONArray();
        }
    }

    static void saveFolders(String str, JSONArray jSONArray) {
        prefs().edit().putString(foldersKey(str), jSONArray.toString()).commit();
    }

    static JSONArray tabOrder(String str) {
        String string = prefs().getString(orderKey(str), "null");
        if (string == null || "null".equals(string.trim())) {
            return null;
        }
        try {
            return new JSONArray(string);
        } catch (Throwable th) {
            return null;
        }
    }

    static void saveTabOrder(String str, JSONArray jSONArray) {
        prefs().edit().putString(orderKey(str), jSONArray == null ? "null" : jSONArray.toString()).commit();
    }

    static ArrayList<String> orderedTabs(String str) {
        JSONArray jSONArrayFolders = folders(str);
        ArrayList<String> arrayList = new ArrayList();
        for (int i = 0; i < jSONArrayFolders.length(); i++) {
            JSONObject jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i);
            if (jSONObjectOptJSONObject != null) {
                arrayList.add(jSONObjectOptJSONObject.optString("id", ""));
            }
        }
        JSONArray jSONArrayTabOrder = tabOrder(str);
        ArrayList<String> arrayList2 = new ArrayList<>();
        HashSet hashSet = new HashSet();
        hashSet.add("all");
        hashSet.addAll(arrayList);
        if (jSONArrayTabOrder != null) {
            for (int i2 = 0; i2 < jSONArrayTabOrder.length(); i2++) {
                String strOptString = jSONArrayTabOrder.optString(i2);
                if (hashSet.contains(strOptString) && !arrayList2.contains(strOptString)) {
                    arrayList2.add(strOptString);
                }
            }
        }
        for (String str2 : arrayList) {
            if (!arrayList2.contains(str2)) {
                arrayList2.add(str2);
            }
        }
        if (!arrayList2.contains("all")) {
            arrayList2.add(0, "all");
        }
        return arrayList2;
    }

    static String activeFolderId(String str) {
        return prefs().getString(keyFor(str, "active_folder"), "all");
    }

    static void setActiveFolderId(String str, String str2) {
        prefs().edit().putString(keyFor(str, "active_folder"), str2).commit();
    }

    static boolean hideTrending() {
        return prefs().getBoolean("hide_trending", false);
    }

    static void setHideTrending(boolean z) {
        prefs().edit().putBoolean("hide_trending", z).commit();
    }

    static String foldersEnabledKey(String str) {
        return "folders_enabled_" + str;
    }

    static boolean foldersEnabled(String str) {
        return prefs().getBoolean(foldersEnabledKey(str), true);
    }

    static void setFoldersEnabled(String str, boolean z) {
        prefs().edit().putBoolean(foldersEnabledKey(str), z).commit();
    }

    static boolean collapseOnScroll() {
        return prefs().getBoolean("collapse_on_scroll", true);
    }

    static void setCollapseOnScroll(boolean z) {
        prefs().edit().putBoolean("collapse_on_scroll", z).commit();
    }

    static boolean glassEnabled() {
        return prefs().getBoolean("glass_enabled", true);
    }

    static void setGlassEnabled(boolean z) {
        prefs().edit().putBoolean("glass_enabled", z).commit();
    }

    static boolean glassTabs() {
        return prefs().getBoolean("glass_tabs", true);
    }

    static void setGlassTabs(boolean z) {
        prefs().edit().putBoolean("glass_tabs", z).commit();
    }

    static float glassAlpha() {
        return prefs().getFloat("glass_alpha", 0.75f);
    }

    static void setGlassAlpha(float f) {
        float fMax;
        try {
            fMax = Math.max(0.1f, Math.min(1.0f, f));
        } catch (Throwable th) {
            fMax = 0.75f;
        }
        prefs().edit().putFloat("glass_alpha", fMax).commit();
    }

    static String getAllTabName(String str) {
        String str2;
        if (G.equals(str)) {
            str2 = "All GIFs";
        } else {
            str2 = S.equals(str) ? "All Stickers" : "All Emoji";
        }
        return prefs().getString(keyFor(str, "all_tab_name"), str2);
    }

    static void setAllTabName(String str, String str2) {
        prefs().edit().putString(keyFor(str, "all_tab_name"), str2).commit();
    }

    static boolean getAllTabHidden(String str) {
        return prefs().getBoolean(keyFor(str, "all_tab_hidden"), false);
    }

    static void setAllTabHidden(String str, boolean z) {
        prefs().edit().putBoolean(keyFor(str, "all_tab_hidden"), z).commit();
    }

    static Integer parseColor(String str) {
        String strTrim;
        if (str == null) {
            strTrim = "";
        } else {
            try {
                strTrim = str.trim();
            } catch (Throwable th) {
                return null;
            }
        }
        if (strTrim.isEmpty()) {
            return null;
        }
        if (!strTrim.startsWith("#")) {
            strTrim = "#" + strTrim;
        }
        if (strTrim.length() == 4) {
            strTrim = "#" + strTrim.charAt(1) + strTrim.charAt(1) + strTrim.charAt(2) + strTrim.charAt(2) + strTrim.charAt(3) + strTrim.charAt(3);
        }
        return Integer.valueOf(Color.parseColor(strTrim));
    }

    static String colorSetting(String str) {
        return prefs().getString(str, "");
    }

    static void setColorSetting(String str, String str2) {
        prefs().edit().putString(str, str2 == null ? "" : str2.trim()).apply();
    }

    static Integer tabBgColor() {
        return parseColor(colorSetting("tab_bg_color"));
    }

    static Integer tabTextColor() {
        return parseColor(colorSetting("tab_text_color"));
    }

    static Integer subtabBgColor() {
        return parseColor(colorSetting("subtab_bg_color"));
    }

    static Integer subtabTextColor() {
        return parseColor(colorSetting("subtab_text_color"));
    }

    static JSONObject findFolder(String str, String str2) {
        JSONArray jSONArrayFolders = folders(str);
        for (int i = 0; i < jSONArrayFolders.length(); i++) {
            JSONObject jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i);
            if (jSONObjectOptJSONObject != null && str2.equals(jSONObjectOptJSONObject.optString("id"))) {
                return jSONObjectOptJSONObject;
            }
        }
        return null;
    }

    static String itemId(JSONObject jSONObject) {
        return jSONObject == null ? "" : jSONObject.optString("id", "");
    }

    public static String mediaLabel(String str) {
        if (G.equals(str)) return "GIF";
        if (S.equals(str)) return isRussian() ? "\u0441\u0442\u0438\u043a\u0435\u0440\u043e\u0432" : "Sticker";
        return isRussian() ? "\u044d\u043c\u043e\u0434\u0437\u0438" : "Emoji";
    }

    public static String mediaLabelSingular(String str) {
        if (G.equals(str)) return "GIF";
        if (S.equals(str)) return isRussian() ? "\u0441\u0442\u0438\u043a\u0435\u0440" : "sticker";
        return isRussian() ? "\u044d\u043c\u043e\u0434\u0437\u0438" : "emoji";
    }

    public static String mediaLabelPlural(String str) {
        if (G.equals(str)) return "GIFs";
        if (S.equals(str)) return isRussian() ? "\u0441\u0442\u0438\u043a\u0435\u0440\u044b" : "Stickers";
        return isRussian() ? "\u044d\u043c\u043e\u0434\u0437\u0438" : "Emoji";
    }

    static String emptyIcon(String str) {
        if (G.equals(str)) {
            return "\ud83d\udcc2";
        }
        return S.equals(str) ? "\ud83c\udfad" : "\ud83d\ude00";
    }

    static int packTypeKey(String str) {
        return S.equals(str) ? 0 : 5;
    }

    static long getSelfUserId() {
        try {
            return UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
        } catch (Throwable th) {
            return 0L;
        }
    }

    static Activity getActivity() {
        try {
            BaseFragment lastFragment = LaunchActivity.getLastFragment();
            if (lastFragment == null) {
                return null;
            }
            return lastFragment.getParentActivity();
        } catch (Throwable th) {
            return null;
        }
    }

    static List<Map<String, Object>> installedPacks(String str) {
        Object objGf;
        ArrayList arrayList = new ArrayList();
        try {
            ArrayList stickerSets = MediaDataController.getInstance(UserConfig.selectedAccount).getStickerSets(packTypeKey(str));
            if (stickerSets instanceof List) {
                Iterator it = stickerSets.iterator();
                while (it.hasNext()) {
                    try {
                        Object objGf2 = gf(it.next(), "set");
                        if (objGf2 != null && (objGf = gf(objGf2, "id")) != null) {
                            Object objGf3 = gf(objGf2, "title");
                            Object objGf4 = gf(objGf2, "count");
                            HashMap map = new HashMap();
                            map.put("id", String.valueOf(objGf));
                            map.put("name", objGf3 != null ? String.valueOf(objGf3) : "");
                            map.put("count", objGf4 != null ? String.valueOf(objGf4) : "0");
                            arrayList.add(map);
                        }
                    } catch (Throwable th) {
                    }
                }
            }
        } catch (Throwable th2) {
        }
        return arrayList;
    }

    static ArrayList<TLRPC.Document> packDocuments(String str, String str2) {
        ArrayList<TLRPC.Document> arrayList = new ArrayList<>();
        try {
            ArrayList stickerSets = MediaDataController.getInstance(UserConfig.selectedAccount).getStickerSets(packTypeKey(str));
            if (stickerSets instanceof List) {
                for (Object obj : stickerSets) {
                    Object objGf = gf(obj, "set");
                    Object objGf2 = objGf != null ? gf(objGf, "id") : null;
                    if (objGf2 != null && String.valueOf(objGf2).equals(str2)) {
                        Object objGf3 = gf(obj, "documents");
                        if (objGf3 instanceof List) {
                            for (Object obj2 : (List) objGf3) {
                                if (obj2 instanceof TLRPC.Document) {
                                    arrayList.add((TLRPC.Document) obj2);
                                }
                            }
                        }
                        return arrayList;
                    }
                }
            }
        } catch (Throwable th) {
        }
        return arrayList;
    }

    static void collectPackDocs(Object obj, Map<String, TLRPC.Document> map) {
        if (obj instanceof List) {
            Iterator it = ((List) obj).iterator();
            while (it.hasNext()) {
                Object objGf = gf(it.next(), "documents");
                if (objGf instanceof List) {
                    for (Object obj2 : (List) objGf) {
                        if (obj2 instanceof TLRPC.Document) {
                            TLRPC.Document document = (TLRPC.Document) obj2;
                            map.put(String.valueOf(document.id), document);
                        }
                    }
                }
            }
        }
    }

    static ColorStateList safeColorStateList(int i) {
        if (i == 0) {
            try {
                i = Color.argb(30, 128, 128, 128);
            } catch (Throwable th) {
                return ColorStateList.valueOf(Color.argb(30, 128, 128, 128));
            }
        }
        return new ColorStateList(new int[][]{new int[0]}, new int[]{i});
    }

    static Drawable createRippleDrawable(int i, int i2) {
        try {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(0);
            gradientDrawable.setCornerRadius(AndroidUtilities.dp(i2));
            gradientDrawable.setColor(-1);
            int color = Theme.getColor(Theme.key_listSelector);
            if (color == 0) {
                color = Color.argb(30, 128, 128, 128);
            }
            return new RippleDrawable(safeColorStateList(color), null, gradientDrawable);
        } catch (Throwable th) {
            return new RippleDrawable(safeColorStateList(Color.argb(30, 128, 128, 128)), null, createRippleMask(i2));
        }
    }

    static GradientDrawable createRippleMask(int i) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(0);
        gradientDrawable.setCornerRadius(AndroidUtilities.dp(i));
        gradientDrawable.setColor(-1);
        return gradientDrawable;
    }

    static Drawable createGlassBarDrawable(float f) {
        boolean zIsCurrentThemeDark = Theme.isCurrentThemeDark();
        int color = Theme.getColor(Theme.key_chat_emojiPanelBackground);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(0);
        gradientDrawable.setColor(Color.argb((int) (f * 255.0f), (color >> 16) & 255, (color >> 8) & 255, color & 255));
        gradientDrawable.setStroke(AndroidUtilities.dp(0.5f), zIsCurrentThemeDark ? Color.argb(40, 255, 255, 255) : Color.argb(30, 0, 0, 0));
        int iArgb = Color.argb(zIsCurrentThemeDark ? 15 : 20, 255, 255, 255);
        GradientDrawable gradientDrawable2 = new GradientDrawable();
        gradientDrawable2.setShape(0);
        gradientDrawable2.setColor(iArgb);
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{gradientDrawable, gradientDrawable2});
        layerDrawable.setLayerInset(1, 0, 0, 0, AndroidUtilities.dp(43.0f));
        return layerDrawable;
    }

    static Drawable createGlassTabDrawable(boolean z, float f, int i) {
        boolean zIsCurrentThemeDark = Theme.isCurrentThemeDark();
        int iDp = AndroidUtilities.dp(i);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(0);
        gradientDrawable.setCornerRadius(iDp);
        if (z) {
            int color = Theme.getColor(Theme.key_chat_emojiPanelStickerPackSelectorLine);
            gradientDrawable.setColor(Color.argb((int) (f * 255.0f), (color >> 16) & 255, (color >> 8) & 255, color & 255));
            gradientDrawable.setStroke(AndroidUtilities.dp(0.5f), zIsCurrentThemeDark ? Color.argb(60, 255, 255, 255) : Color.argb(40, 0, 0, 0));
        } else if (!zIsCurrentThemeDark) {
            gradientDrawable.setColor(Color.argb((int) (f * 25.0f), 0, 0, 0));
            gradientDrawable.setStroke(AndroidUtilities.dp(0.5f), Color.argb(20, 0, 0, 0));
        } else {
            gradientDrawable.setColor(Color.argb((int) (f * 35.0f), 255, 255, 255));
            gradientDrawable.setStroke(AndroidUtilities.dp(0.5f), Color.argb(30, 255, 255, 255));
        }
        return gradientDrawable;
    }

    static void nukeTrendingFromAdapter(Object obj) {
        if (obj == null) {
            return;
        }
        sf(obj, "showTrendingWhenSearchEmpty", false);
        sf(obj, "showTrendingWhenSearching", false);
        sf(obj, "trendingSectionItem", -1);
        try {
            Object objGf = gf(obj, "results");
            if (objGf instanceof ArrayList) {
                ((ArrayList) objGf).clear();
            }
        } catch (Throwable th) {
        }
        sf(obj, "nextSearchOffset", null);
        sf(obj, "searchingUser", false);
        sf(obj, "lastSearchIsEmoji", false);
    }

    static void syncAdapterCounts(Object obj, Object obj2) {
        if (obj2 == null || obj == null) {
            return;
        }
        try {
            Object objGf = gf(obj, "recentGifs");
            sf(obj2, "recentItemsCount", Integer.valueOf(objGf instanceof ArrayList ? ((ArrayList) objGf).size() : 0));
        } catch (Throwable th) {
        }
    }

    static void bulletinSuccess(String str) {
        try {
            BulletinFactory.global().createSimpleBulletin(str, str).show();
        } catch (Throwable th) {
        }
    }

    static void bulletinError(String str) {
        try {
            BulletinFactory.global().createErrorBulletin(str).show();
        } catch (Throwable th) {
        }
    }

    static void bulletinInfo(String str) {
        try {
            BulletinFactory.global().createSimpleBulletin(str, str).show();
        } catch (Throwable th) {
        }
    }

    static class FolderBarManager {
        String activeFolderId;
        ArrayList<TLRPC.Document> allRecentGifs;
        boolean barCollapsed;
        ValueAnimator collapseAnim;
        ViewGroup container;
        LinearLayout customGrid;
        View customOuter;
        View customScroll;
        Map<String, TLRPC.Document> docMapCache;
        long docMapCacheAt;
        final Object emojiView;
        View emptyOverlay;
        View folderBar;
        boolean gridTranslated;
        boolean layoutWatcherAttached;
        final String mt;
        int originalGridMargin;
        int originalSearchMargin;
        LinearLayout subTabsContainer;
        LinearLayout tabsContainer;
        String activeSubTab = "custom";
        int lastTabsHeight = -1;
        final Map<String, View> tabViews = new HashMap();

        FolderBarManager(String str, Object obj) {
            this.mt = str;
            this.emojiView = obj;
            this.activeFolderId = Main.activeFolderId(str);
        }

        String containerField() {
            if (Main.G.equals(this.mt)) {
                return "gifContainer";
            }
            return Main.S.equals(this.mt) ? "stickersContainer" : "emojiContainer";
        }

        String gridField() {
            if (Main.G.equals(this.mt)) {
                return "gifGridView";
            }
            return Main.S.equals(this.mt) ? "stickersGridView" : "emojiGridView";
        }

        String searchField() {
            if (Main.G.equals(this.mt)) {
                return "gifSearchField";
            }
            return Main.S.equals(this.mt) ? "stickersSearchField" : "emojiSearchField";
        }

        String tabsField() {
            if (Main.G.equals(this.mt)) {
                return "gifTabs";
            }
            return Main.S.equals(this.mt) ? "stickersTab" : "emojiTabs";
        }

        String barTag() {
            if (Main.G.equals(this.mt)) {
                return "media_folders_gif_bar";
            }
            return Main.S.equals(this.mt) ? "media_folders_sticker_bar" : "media_folders_emoji_bar";
        }

        String contentTag() {
            return Main.S.equals(this.mt) ? "media_folders_sticker_content" : "media_folders_emoji_content";
        }

        String emptyTag() {
            return "media_folders_gif_empty";
        }

        int tabsHeightDefault() {
            if (Main.G.equals(this.mt)) {
                return 0;
            }
            return Main.S.equals(this.mt) ? 48 : 36;
        }

        int cellDp() {
            if (Main.S.equals(this.mt)) {
                return 76;
            }
            return Main.BAR_HEIGHT_DP;
        }

        String imageSize() {
            return Main.S.equals(this.mt) ? "66_66" : "36_36";
        }

        void injectFolderBar() {
            try {
                Object objGf = Main.gf(this.emojiView, containerField());
                if (objGf == null) {
                    return;
                }
                this.container = (ViewGroup) objGf;
                View viewFindViewWithTag = this.container.findViewWithTag(barTag());
                if (viewFindViewWithTag != null) {
                    this.container.removeView(viewFindViewWithTag);
                }
                View viewFindViewWithTag2 = this.container.findViewWithTag(contentTag());
                if (viewFindViewWithTag2 != null) {
                    this.container.removeView(viewFindViewWithTag2);
                }
                View viewFindViewWithTag3 = this.container.findViewWithTag(emptyTag());
                if (viewFindViewWithTag3 != null) {
                    this.container.removeView(viewFindViewWithTag3);
                }
                resetOffsets();
                ensureLayoutWatcher();
                if (!Main.foldersEnabled(this.mt)) {
                    this.barCollapsed = false;
                    return;
                }
                int iDp = AndroidUtilities.dp(44.0f);
                int iTabsHeightDefault = tabsHeightDefault();
                Context context = this.container.getContext();
                View view = Main.gf(this.emojiView, tabsField()) instanceof View ? (View) Main.gf(this.emojiView, tabsField()) : null;
                if (view != null && !Main.G.equals(this.mt)) {
                    try {
                        int measuredHeight = view.getMeasuredHeight();
                        if (measuredHeight > 0) {
                            iTabsHeightDefault = measuredHeight;
                        }
                    } catch (Throwable th) {
                    }
                }
                this.folderBar = createFolderBar(context);
                this.folderBar.setTag(barTag());
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, iDp);
                layoutParams.gravity = 51;
                if (!Main.G.equals(this.mt)) {
                    layoutParams.topMargin = iTabsHeightDefault;
                }
                this.container.addView(this.folderBar, layoutParams);
                this.folderBar.bringToFront();
                if (Main.G.equals(this.mt)) {
                    this.emptyOverlay = createEmptyOverlay(context);
                    this.emptyOverlay.setTag(emptyTag());
                    this.emptyOverlay.setVisibility(8);
                    FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, -1);
                    layoutParams2.topMargin = iDp;
                    this.container.addView(this.emptyOverlay, layoutParams2);
                } else {
                    this.customScroll = createCustomContent(context);
                    this.customScroll.setTag(contentTag());
                    this.customScroll.setVisibility(8);
                    FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(-1, -1);
                    layoutParams3.topMargin = iTabsHeightDefault + iDp;
                    this.container.addView(this.customScroll, layoutParams3);
                }
                View view2 = Main.gf(this.emojiView, gridField()) instanceof View ? (View) Main.gf(this.emojiView, gridField()) : null;
                if (view2 != null) {
                    ViewGroup.LayoutParams layoutParams4 = view2.getLayoutParams();
                    if (layoutParams4 instanceof ViewGroup.MarginLayoutParams) {
                        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams4;
                        if (Main.G.equals(this.mt)) {
                            marginLayoutParams.topMargin = iDp;
                        } else {
                            this.originalGridMargin = marginLayoutParams.topMargin;
                            marginLayoutParams.topMargin += iDp;
                        }
                        view2.setLayoutParams(marginLayoutParams);
                        this.gridTranslated = true;
                    }
                }
                View view3 = Main.gf(this.emojiView, searchField()) instanceof View ? (View) Main.gf(this.emojiView, searchField()) : null;
                if (view3 != null) {
                    try {
                        ViewGroup.LayoutParams layoutParams5 = view3.getLayoutParams();
                        if (layoutParams5 instanceof ViewGroup.MarginLayoutParams) {
                            ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) layoutParams5;
                            if (Main.G.equals(this.mt)) {
                                marginLayoutParams2.topMargin = iDp;
                            } else {
                                this.originalSearchMargin = marginLayoutParams2.topMargin;
                                marginLayoutParams2.topMargin += iDp;
                            }
                            view3.setLayoutParams(marginLayoutParams2);
                        }
                    } catch (Throwable th2) {
                    }
                }
                if (Main.G.equals(this.mt)) {
                    View view4 = Main.gf(this.emojiView, "gifTabs") instanceof View ? (View) Main.gf(this.emojiView, "gifTabs") : null;
                    if (view4 != null) {
                        if (Main.hideTrending()) {
                            view4.setVisibility(8);
                        } else {
                            view4.setTranslationY(iDp);
                        }
                    }
                    ensureGifAdapterHooks();
                }
                if (Main.collapseOnScroll()) {
                    ensureScrollCollapse();
                }
                rebuildTabs();
                applyFolderFilter();
                applyCollapseInstant();
            } catch (Throwable th3) {
                Main.err(th3, "inject " + this.mt + " folder bar");
            }
        }

        void ensureLayoutWatcher() {
            try {
                if (!this.layoutWatcherAttached && this.container != null) {
                    this.layoutWatcherAttached = true;
                    final View view = Main.gf(this.emojiView, tabsField()) instanceof View ? (View) Main.gf(this.emojiView, tabsField()) : null;
                    this.container.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.1
                        @Override // android.view.View.OnLayoutChangeListener
                        public void onLayoutChange(View view2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                            int iTabsHeightDefault;
                            try {
                                if (Main.G.equals(FolderBarManager.this.mt)) {
                                    return;
                                }
                                if (view != null && view.getMeasuredHeight() > 0) {
                                    iTabsHeightDefault = view.getMeasuredHeight();
                                } else {
                                    iTabsHeightDefault = FolderBarManager.this.tabsHeightDefault();
                                }
                                if (iTabsHeightDefault != FolderBarManager.this.lastTabsHeight) {
                                    FolderBarManager.this.lastTabsHeight = iTabsHeightDefault;
                                    FolderBarManager.this.injectFolderBar();
                                }
                            } catch (Throwable th) {
                            }
                        }
                    });
                }
            } catch (Throwable th) {
            }
        }

        void ensureScrollCollapse() {
            try {
                if (Main.collapseOnScroll()) {
                    View view = Main.gf(this.emojiView, gridField()) instanceof View ? (View) Main.gf(this.emojiView, gridField()) : null;
                    if ((view instanceof RecyclerView) && !Main.scrollHookedGrids.contains(view)) {
                        Main.scrollHookedGrids.add(view);
                        ((RecyclerView) view).addOnScrollListener(new RecyclerView.OnScrollListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.2
                            float accum;
                            long lastToggleAt;

                            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                                try {
                                    if (Main.collapseOnScroll() && FolderBarManager.this.collapseAnim == null) {
                                        this.accum += i2;
                                        if (!FolderBarManager.this.barCollapsed && this.accum >= 48.0f) {
                                            this.accum = 0.0f;
                                            long jCurrentTimeMillis = System.currentTimeMillis();
                                            if (jCurrentTimeMillis - this.lastToggleAt >= 300) {
                                                this.lastToggleAt = jCurrentTimeMillis;
                                                FolderBarManager.this.collapseBar(true);
                                            }
                                            return;
                                        }
                                        if (FolderBarManager.this.barCollapsed && this.accum <= -48.0f) {
                                            this.accum = 0.0f;
                                            long jCurrentTimeMillis2 = System.currentTimeMillis();
                                            if (jCurrentTimeMillis2 - this.lastToggleAt >= 300) {
                                                this.lastToggleAt = jCurrentTimeMillis2;
                                                FolderBarManager.this.collapseBar(false);
                                            }
                                            return;
                                        }
                                        if (FolderBarManager.this.barCollapsed && this.accum > 0.0f) {
                                            this.accum = 0.0f;
                                        } else if (!FolderBarManager.this.barCollapsed && this.accum < 0.0f) {
                                            this.accum = 0.0f;
                                        }
                                    }
                                } catch (Throwable th) {
                                }
                            }

                            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                                if (i == 0) {
                                    this.accum = 0.0f;
                                }
                            }
                        });
                    }
                }
            } catch (Throwable th) {
            }
        }

        int slideTotal() {
            int iDp = AndroidUtilities.dp(44.0f);
            if (Main.G.equals(this.mt)) {
                return iDp;
            }
            View view = Main.gf(this.emojiView, tabsField()) instanceof View ? (View) Main.gf(this.emojiView, tabsField()) : null;
            int measuredHeight = view != null ? view.getMeasuredHeight() : 0;
            if (measuredHeight <= 0) {
                measuredHeight = tabsHeightDefault();
            }
            return measuredHeight + iDp;
        }

        void collapseBar(boolean z) {
            try {
                if (this.folderBar == null) {
                    return;
                }
                if (this.collapseAnim != null) {
                    this.collapseAnim.cancel();
                    this.collapseAnim = null;
                }
                final int iDp = AndroidUtilities.dp(44.0f);
                final int iSlideTotal = slideTotal() - iDp;
                final int iSlideTotal2 = slideTotal();
                final View view = this.folderBar;
                final View view2 = Main.gf(this.emojiView, gridField()) instanceof View ? (View) Main.gf(this.emojiView, gridField()) : null;
                final View view3 = Main.gf(this.emojiView, searchField()) instanceof View ? (View) Main.gf(this.emojiView, searchField()) : null;
                final View view4 = this.customScroll;
                final View view5 = this.emptyOverlay;
                final int i = Main.G.equals(this.mt) ? 0 : this.originalGridMargin;
                final int i2 = Main.G.equals(this.mt) ? 0 : this.originalSearchMargin;
                float f = iSlideTotal2 > 0 ? (-view.getTranslationY()) / iSlideTotal2 : 0.0f;
                if (f < 0.0f) {
                    f = 0.0f;
                }
                if (f > 1.0f) {
                    f = 1.0f;
                }
                this.barCollapsed = z;
                ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(f, z ? 1.0f : 0.0f);
                valueAnimatorOfFloat.setDuration(200L);
                valueAnimatorOfFloat.setInterpolator(new DecelerateInterpolator());
                valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.27
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        FolderBarManager.lambda$collapseBar$0(view, iSlideTotal2, view2, i, iDp, view3, i2, view4, iSlideTotal, view5, valueAnimator);
                    }
                });
                valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.3
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        FolderBarManager.this.collapseAnim = null;
                    }
                });
                this.collapseAnim = valueAnimatorOfFloat;
                valueAnimatorOfFloat.start();
                this.barCollapsed = z;
            } catch (Throwable th) {
            }
        }

        static /* synthetic */ void lambda$collapseBar$0(View view, int i, View view2, int i2, int i3, View view3, int i4, View view4, int i5, View view5, ValueAnimator valueAnimator) {
            try {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                view.setTranslationY((-i) * fFloatValue);
                if (view2 != null) {
                    ViewGroup.LayoutParams layoutParams = view2.getLayoutParams();
                    if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) layoutParams).topMargin = (int) (i2 + (i3 * (1.0f - fFloatValue)));
                        view2.setLayoutParams(layoutParams);
                    }
                }
                if (view3 != null) {
                    ViewGroup.LayoutParams layoutParams2 = view3.getLayoutParams();
                    if (layoutParams2 instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) layoutParams2).topMargin = (int) (i4 + (i3 * (1.0f - fFloatValue)));
                        view3.setLayoutParams(layoutParams2);
                    }
                }
                if (view4 != null) {
                    ViewGroup.LayoutParams layoutParams3 = view4.getLayoutParams();
                    if (layoutParams3 instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) layoutParams3).topMargin = (int) (i5 + (i3 * (1.0f - fFloatValue)));
                        view4.setLayoutParams(layoutParams3);
                    }
                }
                if (view5 != null) {
                    ViewGroup.LayoutParams layoutParams4 = view5.getLayoutParams();
                    if (layoutParams4 instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) layoutParams4).topMargin = (int) (i3 * (1.0f - fFloatValue));
                        view5.setLayoutParams(layoutParams4);
                    }
                }
            } catch (Throwable th) {
            }
        }

        void applyCollapseInstant() {
            try {
                if (this.folderBar == null) {
                    return;
                }
                if (this.collapseAnim != null) {
                    this.collapseAnim.cancel();
                    this.collapseAnim = null;
                }
                int iDp = AndroidUtilities.dp(44.0f);
                int iSlideTotal = slideTotal();
                int i = iSlideTotal - iDp;
                View view = Main.gf(this.emojiView, gridField()) instanceof View ? (View) Main.gf(this.emojiView, gridField()) : null;
                View view2 = Main.gf(this.emojiView, searchField()) instanceof View ? (View) Main.gf(this.emojiView, searchField()) : null;
                int i2 = Main.G.equals(this.mt) ? 0 : this.originalGridMargin;
                int i3 = Main.G.equals(this.mt) ? 0 : this.originalSearchMargin;
                boolean z = this.barCollapsed;
                View view3 = this.folderBar;
                if (z) {
                    view3.setTranslationY(-iSlideTotal);
                    if (view != null) {
                        setMarginTop(view, i2);
                    }
                    if (view2 != null) {
                        setMarginTop(view2, i3);
                    }
                    if (this.customScroll != null) {
                        setMarginTop(this.customScroll, i);
                    }
                    if (this.emptyOverlay != null) {
                        setMarginTop(this.emptyOverlay, 0);
                        return;
                    }
                    return;
                }
                view3.setTranslationY(0.0f);
                if (view != null) {
                    setMarginTop(view, i2 + iDp);
                }
                if (view2 != null) {
                    setMarginTop(view2, i3 + iDp);
                }
                if (this.customScroll != null) {
                    setMarginTop(this.customScroll, i + iDp);
                }
                if (this.emptyOverlay != null) {
                    setMarginTop(this.emptyOverlay, iDp);
                }
            } catch (Throwable th) {
            }
        }

        static void setMarginTop(View view, int i) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) layoutParams).topMargin = i;
                view.setLayoutParams(layoutParams);
            }
        }

        void ensureGifAdapterHooks() {
            if (Main.G.equals(this.mt)) {
                try {
                    Object objGf = Main.gf(this.emojiView, "gifAdapter");
                    if (objGf == null) {
                        return;
                    }
                    Class<?> cls = objGf.getClass();
                    if (Main.hookedGifAdapterClasses.contains(cls)) {
                        return;
                    }
                    Main.hookedGifAdapterClasses.add(cls);
                    Main.hookMethodsByName(cls, "search", Main.searchHook);
                    Main.hookMethodsByName(cls, "processResponse", Main.processResponseHook);
                } catch (Throwable th) {
                }
            }
        }

        void clearTrendingResults(final Object obj, Object obj2) {
            if (obj == null) {
                return;
            }
            final View view = Main.gf(obj2, "gifGridView") instanceof View ? (View) Main.gf(obj2, "gifGridView") : null;
            final int[] iArr = {0};
            Main.ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.4
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        if (iArr[0] > 10) {
                            return;
                        }
                        if (view instanceof RecyclerView) {
                            RecyclerView recyclerView = (RecyclerView) view;
                            try {
                                recyclerView.stopScroll();
                            } catch (Throwable ignored) {}
                            if (recyclerView.isComputingLayout()) {
                                if (iArr[0] <= 10) {
                                    iArr[0]++;
                                    Main.ui(this, 50L);
                                }
                                return;
                            }
                        }
                        Main.nukeTrendingFromAdapter(obj);
                        Main.notifyAdapter(obj);
                    } catch (Throwable th3) {
                    }
                }
            }, 200L);
        }

        void resetOffsets() {
            View view = Main.gf(this.emojiView, gridField()) instanceof View ? (View) Main.gf(this.emojiView, gridField()) : null;
            if (view != null && this.gridTranslated) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                    marginLayoutParams.topMargin = Main.G.equals(this.mt) ? 0 : this.originalGridMargin;
                    view.setLayoutParams(marginLayoutParams);
                }
                this.gridTranslated = false;
            }
            View view2 = Main.gf(this.emojiView, searchField()) instanceof View ? (View) Main.gf(this.emojiView, searchField()) : null;
            if (view2 != null) {
                try {
                    ViewGroup.LayoutParams layoutParams2 = view2.getLayoutParams();
                    if (layoutParams2 instanceof ViewGroup.MarginLayoutParams) {
                        ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) layoutParams2;
                        marginLayoutParams2.topMargin = Main.G.equals(this.mt) ? 0 : this.originalSearchMargin;
                        view2.setLayoutParams(marginLayoutParams2);
                    }
                } catch (Throwable th) {
                }
            }
            if (Main.G.equals(this.mt)) {
                View view3 = Main.gf(this.emojiView, "gifTabs") instanceof View ? (View) Main.gf(this.emojiView, "gifTabs") : null;
                if (view3 != null) {
                    try {
                        view3.setVisibility(0);
                        view3.setTranslationY(0.0f);
                    } catch (Throwable th2) {
                    }
                }
            }
        }

        void removeFolderBar() {
            final Object objGf;
            final ArrayList recentGifs;
            try {
                if (this.container != null && this.folderBar != null) {
                    this.container.removeView(this.folderBar);
                    this.folderBar = null;
                }
                if (this.container != null && this.emptyOverlay != null) {
                    this.container.removeView(this.emptyOverlay);
                    this.emptyOverlay = null;
                }
                if (this.container != null && this.customScroll != null) {
                    this.container.removeView(this.customScroll);
                    this.customScroll = null;
                    this.customGrid = null;
                }
                resetOffsets();
                View view = Main.gf(this.emojiView, gridField()) instanceof View ? (View) Main.gf(this.emojiView, gridField()) : null;
                if (view != null) {
                    view.setVisibility(0);
                }
                if (Main.G.equals(this.mt) && (objGf = Main.gf(this.emojiView, "gifAdapter")) != null && (recentGifs = MediaDataController.getInstance(UserConfig.selectedAccount).getRecentGifs()) != null) {
                    Main.ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.31
                        @Override // java.lang.Runnable
                        public final void run() {
                            FolderBarManager.this.m15x7d4ba519(recentGifs, objGf);
                        }
                    });
                }
            } catch (Throwable th) {
                Main.err(th, "remove " + this.mt + " folder bar");
            }
        }

        /* JADX INFO: renamed from: lambda$removeFolderBar$1$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m15x7d4ba519(ArrayList arrayList, Object obj) {
            try {
                Object objGf = Main.gf(this.emojiView, "recentGifs");
                if (!(objGf instanceof ArrayList)) {
                    Main.sf(this.emojiView, "recentGifs", arrayList);
                } else {
                    ArrayList arrayList2 = (ArrayList) objGf;
                    arrayList2.clear();
                    arrayList2.addAll(arrayList);
                }
                Main.notifyAdapter(obj);
            } catch (Throwable th) {
            }
        }

        View createEmptyOverlay(Context context) {
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
            frameLayout.setClickable(true);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setGravity(17);
            TextView textView = new TextView(context);
            textView.setText(Main.emptyIcon(this.mt));
            textView.setTextSize(1, 48.0f);
            textView.setGravity(17);
            linearLayout.addView(textView, new LinearLayout.LayoutParams(-2, -2));
            TextView textView2 = new TextView(context);
            textView2.setText(Main.isRussian() ? ("\u0412 \u044d\u0442\u043e\u0439 \u043f\u0430\u043f\u043a\u0435 \u043d\u0435\u0442 " + Main.mediaLabel(this.mt)) : ("No " + Main.mediaLabelPlural(this.mt) + " in this folder"));
            textView2.setTextSize(1, 15.0f);
            textView2.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelEmptyText));
            textView2.setGravity(17);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.topMargin = AndroidUtilities.dp(12.0f);
            linearLayout.addView(textView2, layoutParams);
            TextView textView3 = new TextView(context);
            textView3.setText(Main.isRussian() ? ("\u0417\u0430\u0436\u043c\u0438\u0442\u0435 " + Main.mediaLabelSingular(this.mt) + ", \u0447\u0442\u043e\u0431\u044b \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0441\u044e\u0434\u0430") : ("Long press a " + Main.mediaLabel(this.mt).toLowerCase() + " to add it here"));
            textView3.setTextSize(1, 13.0f);
            textView3.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelEmptyText));
            textView3.setAlpha(0.6f);
            textView3.setGravity(17);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams2.topMargin = AndroidUtilities.dp(4.0f);
            linearLayout.addView(textView3, layoutParams2);
            frameLayout.addView(linearLayout, new FrameLayout.LayoutParams(-2, -2, 17));
            return frameLayout;
        }

        View createFolderBar(Context context) {
            FrameLayout frameLayout = new FrameLayout(context);
            boolean zGlassEnabled = Main.glassEnabled();
            if (zGlassEnabled) {
                frameLayout.setBackground(Main.createGlassBarDrawable(Main.glassAlpha()));
            } else {
                frameLayout.setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
            }
            HorizontalScrollView horizontalScrollView = new HorizontalScrollView(context);
            horizontalScrollView.setHorizontalScrollBarEnabled(false);
            horizontalScrollView.setOverScrollMode(2);
            this.tabsContainer = new LinearLayout(context);
            this.tabsContainer.setOrientation(0);
            this.tabsContainer.setGravity(16);
            int iDp = AndroidUtilities.dp(6.0f);
            int iDp2 = AndroidUtilities.dp(6.0f);
            this.tabsContainer.setPadding(iDp, iDp2, iDp, iDp2);
            horizontalScrollView.addView(this.tabsContainer, new FrameLayout.LayoutParams(-2, -1));
            frameLayout.addView(horizontalScrollView, new FrameLayout.LayoutParams(-1, -1));
            View view = new View(context);
            if (!zGlassEnabled) {
                view.setBackgroundColor(Theme.getColor(Theme.key_divider));
            } else {
                view.setBackgroundColor(Theme.isCurrentThemeDark() ? Color.argb(40, 255, 255, 255) : Color.argb(25, 0, 0, 0));
            }
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, AndroidUtilities.dp(0.5f));
            layoutParams.gravity = 80;
            frameLayout.addView(view, layoutParams);
            return frameLayout;
        }

        void rebuildTabs() {
            if (this.tabsContainer == null) {
                return;
            }
            this.tabsContainer.removeAllViews();
            this.tabViews.clear();
            Context context = this.tabsContainer.getContext();
            this.tabsContainer.addView(createEnableToggle(context));
            if (!Main.foldersEnabled(this.mt)) {
                updateSelection();
                return;
            }
            this.tabsContainer.addView(createSettingsButton(context));
            this.tabsContainer.addView(createAddButton(context));
            JSONArray jSONArrayFolders = Main.folders(this.mt);
            HashMap map = new HashMap();
            for (int i = 0; i < jSONArrayFolders.length(); i++) {
                JSONObject jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i);
                if (jSONObjectOptJSONObject != null) {
                    map.put(jSONObjectOptJSONObject.optString("id"), jSONObjectOptJSONObject.optString("name", "Folder"));
                }
            }
            ArrayList<String> arrayListOrderedTabs = Main.orderedTabs(this.mt);
            boolean allTabHidden = Main.getAllTabHidden(this.mt);
            for (String str : arrayListOrderedTabs) {
                if ("all".equals(str)) {
                    if (!allTabHidden) {
                        View viewCreateTab = createTab(context, "all", Main.getAllTabName(this.mt));
                        this.tabsContainer.addView(viewCreateTab);
                        this.tabViews.put("all", viewCreateTab);
                    }
                } else if (map.containsKey(str)) {
                    View viewCreateTab2 = createTab(context, str, (String) map.get(str));
                    this.tabsContainer.addView(viewCreateTab2);
                    this.tabViews.put(str, viewCreateTab2);
                }
            }
            updateSelection();
        }

        View createEnableToggle(Context context) {
            int color;
            FrameLayout frameLayout = new FrameLayout(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, AndroidUtilities.dp(30.0f));
            layoutParams.setMargins(AndroidUtilities.dp(3.0f), 0, AndroidUtilities.dp(3.0f), 0);
            frameLayout.setLayoutParams(layoutParams);
            int iDp = AndroidUtilities.dp(10.0f);
            frameLayout.setPadding(iDp, 0, iDp, 0);
            boolean zFoldersEnabled = Main.foldersEnabled(this.mt);
            if (Main.glassEnabled() && Main.glassTabs()) {
                frameLayout.setBackground(Main.createGlassTabDrawable(zFoldersEnabled, Main.glassAlpha(), 10));
            } else {
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setShape(0);
                gradientDrawable.setCornerRadius(AndroidUtilities.dp(10.0f));
                if (zFoldersEnabled) {
                    Integer numTabBgColor = Main.tabBgColor();
                    gradientDrawable.setColor(numTabBgColor != null ? numTabBgColor.intValue() : Theme.getColor(Theme.key_chat_emojiPanelStickerPackSelectorLine));
                } else {
                    gradientDrawable.setColor(Theme.getColor(Theme.key_divider));
                }
                frameLayout.setBackground(gradientDrawable);
            }
            frameLayout.setForeground(Main.createRippleDrawable(0, 10));
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(getDrawableRes("msg_folders"));
            Integer numTabTextColor = Main.tabTextColor();
            if (numTabTextColor != null) {
                color = numTabTextColor.intValue();
            } else if (zFoldersEnabled) {
                color = -1;
            } else {
                color = Theme.getColor(Theme.key_chat_emojiPanelIcon);
            }
            imageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            frameLayout.addView(imageView, new FrameLayout.LayoutParams(-1, -1, 17));
            frameLayout.setOnClickListener(new View.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    FolderBarManager.this.m6xc57200e6(view);
                }
            });
            return frameLayout;
        }

        /* JADX INFO: renamed from: lambda$createEnableToggle$2$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m6xc57200e6(View view) {
            onToggleFoldersEnabled();
        }

        void onToggleFoldersEnabled() {
            Main.setFoldersEnabled(this.mt, !Main.foldersEnabled(this.mt));
            Main.ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.19
                @Override // java.lang.Runnable
                public final void run() {
                    FolderBarManager.this.m13x7ed8c303();
                }
            }, 120L);
        }

        /* JADX INFO: renamed from: lambda$onToggleFoldersEnabled$3$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m13x7ed8c303() {
            Iterator<FolderBarManager> it = Main.managersForType(this.mt).values().iterator();
            while (it.hasNext()) {
                it.next().injectFolderBar();
            }
        }

        View createSettingsButton(Context context) {
            FrameLayout frameLayout = new FrameLayout(context);
            int iDp = AndroidUtilities.dp(32.0f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iDp, iDp);
            layoutParams.setMargins(AndroidUtilities.dp(2.0f), 0, AndroidUtilities.dp(2.0f), 0);
            frameLayout.setLayoutParams(layoutParams);
            if (Main.glassEnabled() && Main.glassTabs()) {
                frameLayout.setBackground(Main.createGlassTabDrawable(true, Main.glassAlpha(), 10));
            } else {
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setShape(0);
                gradientDrawable.setCornerRadius(AndroidUtilities.dp(10.0f));
                Integer numTabBgColor = Main.tabBgColor();
                gradientDrawable.setColor(numTabBgColor != null ? numTabBgColor.intValue() : Theme.getColor(Theme.key_chat_emojiPanelStickerPackSelectorLine));
                frameLayout.setBackground(gradientDrawable);
            }
            frameLayout.setForeground(Main.createRippleDrawable(0, 10));
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(getDrawableRes("msg_settings"));
            Integer numTabTextColor = Main.tabTextColor();
            imageView.setColorFilter(numTabTextColor != null ? numTabTextColor.intValue() : -1, PorterDuff.Mode.SRC_ATOP);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            frameLayout.addView(imageView, new FrameLayout.LayoutParams(-1, -1, 17));
            frameLayout.setOnClickListener(new View.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.24
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Main.pyOpenSettings();
                }
            });
            return frameLayout;
        }

        View createAddButton(Context context) {
            FrameLayout frameLayout = new FrameLayout(context);
            int iDp = AndroidUtilities.dp(32.0f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iDp, iDp);
            layoutParams.setMargins(AndroidUtilities.dp(2.0f), 0, AndroidUtilities.dp(4.0f), 0);
            frameLayout.setLayoutParams(layoutParams);
            if (Main.glassEnabled() && Main.glassTabs()) {
                frameLayout.setBackground(Main.createGlassTabDrawable(true, Main.glassAlpha(), 10));
            } else {
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setShape(0);
                gradientDrawable.setCornerRadius(AndroidUtilities.dp(10.0f));
                Integer numTabBgColor = Main.tabBgColor();
                gradientDrawable.setColor(numTabBgColor != null ? numTabBgColor.intValue() : Theme.getColor(Theme.key_chat_emojiPanelStickerPackSelectorLine));
                frameLayout.setBackground(gradientDrawable);
            }
            frameLayout.setForeground(Main.createRippleDrawable(0, 10));
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(getDrawableRes("msg_add"));
            Integer numTabTextColor = Main.tabTextColor();
            imageView.setColorFilter(numTabTextColor != null ? numTabTextColor.intValue() : -1, PorterDuff.Mode.SRC_IN);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            frameLayout.addView(imageView, new FrameLayout.LayoutParams(-1, -1, 17));
            frameLayout.setOnClickListener(new View.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.11
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    FolderBarManager.this.m2x543d7213(view);
                }
            });
            return frameLayout;
        }

        /* JADX INFO: renamed from: lambda$createAddButton$5$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m2x543d7213(View view) {
            showCreateFolderDialog(this.mt);
        }

        View createTab(Context context, final String str, final String str2) {
            FrameLayout frameLayout = new FrameLayout(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, AndroidUtilities.dp(32.0f));
            layoutParams.setMargins(AndroidUtilities.dp(3.0f), 0, AndroidUtilities.dp(3.0f), 0);
            frameLayout.setLayoutParams(layoutParams);
            int iDp = AndroidUtilities.dp(14.0f);
            frameLayout.setPadding(iDp, 0, iDp, 0);
            if (Main.glassEnabled() && Main.glassTabs()) {
                frameLayout.setBackground(Main.createGlassTabDrawable(str.equals(this.activeFolderId), Main.glassAlpha(), 10));
            } else {
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setShape(0);
                gradientDrawable.setCornerRadius(AndroidUtilities.dp(10.0f));
                frameLayout.setBackground(gradientDrawable);
            }
            frameLayout.setForeground(Main.createRippleDrawable(0, 10));
            TextView textView = new TextView(context);
            textView.setText(str2);
            textView.setTextSize(1, 13.0f);
            textView.setGravity(17);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            frameLayout.addView(textView, new FrameLayout.LayoutParams(-2, -1, 17));
            frameLayout.setOnClickListener(new View.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.14
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    FolderBarManager.this.m11xc6194476(str, view);
                }
            });
            frameLayout.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.15
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return FolderBarManager.this.m12x9a46237(str, str2, view);
                }
            });
            frameLayout.setTag(str);
            return frameLayout;
        }

        /* JADX INFO: renamed from: lambda$createTab$6$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m11xc6194476(String str, View view) {
            onFolderSelected(str);
        }

        /* JADX INFO: renamed from: lambda$createTab$7$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ boolean m12x9a46237(String str, String str2, View view) {
            showTabContextMenu(str, str2);
            return true;
        }

        void updateSelection() {
            int color = Theme.getColor(Theme.key_chat_emojiPanelStickerPackSelectorLine);
            Integer numTabBgColor = Main.tabBgColor();
            Integer numTabTextColor = Main.tabTextColor();
            boolean z = Main.glassEnabled() && Main.glassTabs();
            for (Map.Entry<String, View> entry : this.tabViews.entrySet()) {
                String key = entry.getKey();
                View value = entry.getValue();
                if (value instanceof FrameLayout) {
                    TextView textView = (TextView) ((FrameLayout) value).getChildAt(0);
                    if (z) {
                        value.setBackground(Main.createGlassTabDrawable(key.equals(this.activeFolderId), Main.glassAlpha(), 10));
                        if (numTabTextColor != null) {
                            textView.setTextColor(numTabTextColor.intValue());
                        } else if (key.equals(this.activeFolderId)) {
                            textView.setTextColor(-1);
                        } else {
                            textView.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelIcon));
                        }
                    } else {
                        try {
                            GradientDrawable gradientDrawable = (GradientDrawable) value.getBackground();
                            if (numTabBgColor != null) {
                                gradientDrawable.setColor(numTabBgColor.intValue());
                            } else {
                                gradientDrawable.setColor(key.equals(this.activeFolderId) ? color : 0);
                            }
                            if (numTabTextColor != null) {
                                textView.setTextColor(numTabTextColor.intValue());
                            } else if (key.equals(this.activeFolderId)) {
                                textView.setTextColor(-1);
                            } else {
                                textView.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelIcon));
                            }
                        } catch (Throwable th) {
                        }
                    }
                }
            }
        }

        void onFolderSelected(String str) {
            this.activeFolderId = str;
            this.activeSubTab = "custom";
            this.docMapCache = null;
            Main.setActiveFolderId(this.mt, str);
            updateSelection();
            applyFolderFilter();
        }

        void showTabContextMenu(final String str, final String str2) {
            Activity activity = Main.getActivity();
            if (activity == null) {
                return;
            }
            ArrayList<String> arrayListOrderedTabs = Main.orderedTabs(this.mt);
            int iIndexOf = arrayListOrderedTabs.indexOf(str);
            ArrayList arrayList = new ArrayList();
            final ArrayList arrayList2 = new ArrayList();
            if (iIndexOf > 0) {
                arrayList.add(Main.tr("\u2b06\ufe0f  \u0421\u0434\u0432\u0438\u043d\u0443\u0442\u044c \u0432\u043b\u0435\u0432\u043e", "\u2b06\ufe0f  Move left"));
                arrayList2.add("up");
            }
            if (iIndexOf >= 0 && iIndexOf < arrayListOrderedTabs.size() - 1) {
                arrayList.add(Main.tr("\u2b07\ufe0f  \u0421\u0434\u0432\u0438\u043d\u0443\u0442\u044c \u0432\u043f\u0440\u0430\u0432\u043e", "\u2b07\ufe0f  Move right"));
                arrayList2.add("down");
            }
            arrayList.add(Main.tr("\u270f\ufe0f  \u041f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u0442\u044c", "\u270f\ufe0f  Rename"));
            arrayList2.add("rename");
            if ("all".equals(str)) {
                if (!Main.getAllTabHidden(this.mt)) {
                    arrayList.add("\ud83d\udeab  Hide");
                    arrayList2.add("hide");
                }
            } else {
                arrayList.add(Main.tr("\ud83d\uddd1  \u0423\u0434\u0430\u043b\u0438\u0442\u044c", "\ud83d\uddd1  Delete"));
                arrayList2.add("delete");
            }
            new AlertDialog.Builder(activity).setTitle(str2).setItems((CharSequence[]) arrayList.toArray(new String[0]), new DialogInterface.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.25
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    FolderBarManager.this.m21x91675f4f(arrayList2, str, str2, dialogInterface, i);
                }
            }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.26
                public final void onClick(AlertDialog alertDialog, int i) {
                    alertDialog.dismiss();
                }
            }).show();
        }

        /* JADX INFO: renamed from: lambda$showTabContextMenu$8$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m21x91675f4f(List list, String str, String str2, DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            String str3 = (String) list.get(i);
            if ("up".equals(str3) || "down".equals(str3)) {
                int i2 = "up".equals(str3) ? -1 : 1;
                ArrayList<String> arrayListOrderedTabs = Main.orderedTabs(this.mt);
                int iIndexOf = arrayListOrderedTabs.indexOf(str);
                int i3 = i2 + iIndexOf;
                if (iIndexOf >= 0 && i3 >= 0 && i3 < arrayListOrderedTabs.size()) {
                    String str4 = arrayListOrderedTabs.get(iIndexOf);
                    arrayListOrderedTabs.set(iIndexOf, arrayListOrderedTabs.get(i3));
                    arrayListOrderedTabs.set(i3, str4);
                    Main.saveTabOrder(this.mt, new JSONArray((Collection) arrayListOrderedTabs));
                    rebuildTabs();
                    applyFolderFilter();
                    return;
                }
                return;
            }
            if ("rename".equals(str3)) {
                boolean zEquals = "all".equals(str);
                String str5 = this.mt;
                if (zEquals) {
                    Main.showRenameAllDialog(str5, str2);
                    return;
                } else {
                    Main.showRenameFolderDialog(str5, str, str2);
                    return;
                }
            }
            if ("hide".equals(str3)) {
                Main.setAllTabHidden(this.mt, true);
                rebuildTabs();
                applyFolderFilter();
            } else if ("delete".equals(str3)) {
                Main.confirmDeleteFolder(this.mt, str, str2);
            }
        }

        void showCreateFolderDialog(final String str) {
            Activity activity = Main.getActivity();
            if (activity == null) {
                return;
            }
            final EditText editText = new EditText(activity);
            editText.setHint(Main.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043f\u0430\u043f\u043a\u0438", "Folder name"));
            editText.setTextSize(1, 16.0f);
            editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            editText.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
            editText.setPadding(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(12.0f), AndroidUtilities.dp(24.0f), AndroidUtilities.dp(12.0f));
            editText.setSingleLine(true);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(0);
            gradientDrawable.setCornerRadius(AndroidUtilities.dp(12.0f));
            gradientDrawable.setColor(Theme.getColor(Theme.key_dialogBackground));
            gradientDrawable.setStroke(AndroidUtilities.dp(1.0f), Theme.getColor(Theme.key_divider));
            editText.setBackground(gradientDrawable);
            FrameLayout frameLayout = new FrameLayout(activity);
            frameLayout.setPadding(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(24.0f), AndroidUtilities.dp(4.0f));
            frameLayout.addView(editText, new FrameLayout.LayoutParams(-1, -2));
            AlertDialog alertDialogCreate = new AlertDialog.Builder(activity).setTitle(Main.isRussian() ? ("\u041d\u043e\u0432\u0430\u044f \u043f\u0430\u043f\u043a\u0430 " + Main.mediaLabel(str)) : ("New " + Main.mediaLabel(str) + " Folder")).setView(frameLayout).setPositiveButton(Main.tr("\u0421\u043e\u0437\u0434\u0430\u0442\u044c", "Create"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.12
                public final void onClick(AlertDialog alertDialog, int i) {
                    FolderBarManager.this.m17x2462063f(str, editText, alertDialog, i);
                }
            }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.13
                public final void onClick(AlertDialog alertDialog, int i) {
                    alertDialog.dismiss();
                }
            }).create();
            alertDialogCreate.show();
            try {
                alertDialogCreate.getWindow().setSoftInputMode(36);
                editText.requestFocus();
            } catch (Throwable th) {
            }
        }

        /* JADX INFO: renamed from: lambda$showCreateFolderDialog$10$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m17x2462063f(String str, EditText editText, AlertDialog alertDialog, int i) {
            createFolder(str, String.valueOf(editText.getText()).trim());
            alertDialog.dismiss();
        }

        void createFolder(final String str, String str2) {
            if (str2 == null || str2.isEmpty()) {
                return;
            }
            JSONArray jSONArrayFolders = Main.folders(str);
            String str3 = str + "_folder_" + System.currentTimeMillis();
            JSONObject jSONObject = new JSONObject();
            try {
                jSONObject.put("id", str3);
                jSONObject.put("name", str2);
                jSONObject.put("items", new JSONArray());
            } catch (Throwable th) {
            }
            jSONArrayFolders.put(jSONObject);
            Main.saveFolders(str, jSONArrayFolders);
            ArrayList<String> arrayListOrderedTabs = Main.orderedTabs(str);
            arrayListOrderedTabs.add(str3);
            Main.saveTabOrder(str, new JSONArray((Collection) arrayListOrderedTabs));
            Main.ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.34
                @Override // java.lang.Runnable
                public final void run() {
                    Main.refreshBarsForType(str);
                }
            });
            Main.bulletinSuccess(Main.isRussian() ? ("\u041f\u0430\u043f\u043a\u0430 \"" + str2 + "\" \u0441\u043e\u0437\u0434\u0430\u043d\u0430") : ("Folder \"" + str2 + "\" created"));
        }

        View createCustomContent(Context context) {
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
            HorizontalScrollView horizontalScrollView = new HorizontalScrollView(context);
            horizontalScrollView.setHorizontalScrollBarEnabled(false);
            horizontalScrollView.setOverScrollMode(2);
            this.subTabsContainer = new LinearLayout(context);
            this.subTabsContainer.setOrientation(0);
            this.subTabsContainer.setGravity(16);
            int iDp = AndroidUtilities.dp(4.0f);
            this.subTabsContainer.setPadding(iDp, iDp, iDp, iDp);
            horizontalScrollView.addView(this.subTabsContainer, new LinearLayout.LayoutParams(-2, -2));
            linearLayout.addView(horizontalScrollView, new LinearLayout.LayoutParams(-1, AndroidUtilities.dp(36.0f)));
            ScrollView scrollView = new ScrollView(context);
            scrollView.setFillViewport(true);
            this.customGrid = new LinearLayout(context);
            this.customGrid.setOrientation(1);
            this.customGrid.setGravity(49);
            int iDp2 = AndroidUtilities.dp(8.0f);
            this.customGrid.setPadding(iDp2, iDp2, iDp2, iDp2);
            scrollView.addView(this.customGrid, new LinearLayout.LayoutParams(-1, -2));
            linearLayout.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
            return linearLayout;
        }

        void buildSubTabs(JSONObject jSONObject) {
            if (this.subTabsContainer == null) {
                return;
            }
            this.subTabsContainer.removeAllViews();
            Context context = this.subTabsContainer.getContext();
            JSONArray jSONArrayOptJSONArray = jSONObject.optJSONArray("packs");
            if (jSONArrayOptJSONArray == null) {
                jSONArrayOptJSONArray = new JSONArray();
            }
            this.subTabsContainer.setVisibility(0);
            this.subTabsContainer.addView(createAddPackButton(context));
            this.subTabsContainer.addView(createSubTab(context, "custom", "Added"));
            for (int i = 0; i < jSONArrayOptJSONArray.length(); i++) {
                JSONObject jSONObjectOptJSONObject = jSONArrayOptJSONArray.optJSONObject(i);
                if (jSONObjectOptJSONObject != null) {
                    this.subTabsContainer.addView(createSubTab(context, jSONObjectOptJSONObject.optString("id", ""), jSONObjectOptJSONObject.optString("name", "Pack")));
                }
            }
        }

        View createAddPackButton(Context context) {
            FrameLayout frameLayout = new FrameLayout(context);
            int iDp = AndroidUtilities.dp(28.0f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iDp, iDp);
            layoutParams.setMargins(AndroidUtilities.dp(3.0f), 0, AndroidUtilities.dp(3.0f), 0);
            frameLayout.setLayoutParams(layoutParams);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(0);
            gradientDrawable.setCornerRadius(AndroidUtilities.dp(8.0f));
            gradientDrawable.setColor(Theme.getColor(Theme.key_chat_emojiPanelStickerPackSelectorLine));
            frameLayout.setBackground(gradientDrawable);
            frameLayout.setForeground(Main.createRippleDrawable(0, 8));
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(getDrawableRes("msg_add"));
            imageView.setColorFilter(-1, PorterDuff.Mode.SRC_ATOP);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            frameLayout.addView(imageView, new FrameLayout.LayoutParams(-1, -1, 17));
            frameLayout.setOnClickListener(new View.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.8
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    FolderBarManager.this.m3x341ed7b(view);
                }
            });
            return frameLayout;
        }

        /* JADX INFO: renamed from: lambda$createAddPackButton$13$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m3x341ed7b(View view) {
            showAddPackDialog();
        }

        void showAddPackDialog() {
            Activity activity = Main.getActivity();
            if (activity == null) {
                return;
            }
            final String str = this.activeFolderId;
            final List<Map<String, Object>> listInstalledPacks = Main.installedPacks(this.mt);
            boolean zIsEmpty = listInstalledPacks.isEmpty();
            String str2 = Main.E;
            String str3 = Main.S;
            if (zIsEmpty) {
                StringBuilder sbAppend = new StringBuilder().append("No ");
                if (Main.S.equals(this.mt)) {
                    str2 = Main.S;
                }
                Main.bulletinError(Main.isRussian() ? ("\u0423\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e \u043d\u0430\u0431\u043e\u0440\u043e\u0432: " + str2) : (str2 + " packs installed"));
                return;
            }
            JSONObject jSONObjectFindFolder = Main.findFolder(this.mt, str);
            HashSet hashSet = new HashSet();
            if (jSONObjectFindFolder != null && jSONObjectFindFolder.optJSONArray("packs") != null) {
                JSONArray jSONArrayOptJSONArray = jSONObjectFindFolder.optJSONArray("packs");
                for (int i = 0; i < jSONArrayOptJSONArray.length(); i++) {
                    JSONObject jSONObjectOptJSONObject = jSONArrayOptJSONArray.optJSONObject(i);
                    if (jSONObjectOptJSONObject != null) {
                        hashSet.add(jSONObjectOptJSONObject.optString("id"));
                    }
                }
            }
            ArrayList arrayList = new ArrayList();
            for (Map<String, Object> map : listInstalledPacks) {
                if (hashSet.contains(String.valueOf(map.get("id")))) {
                    arrayList.add("\u2705  " + map.get("name") + " (" + map.get("count") + ")");
                } else {
                    arrayList.add("\u2b1c  " + map.get("name") + " (" + map.get("count") + ")");
                }
                str2 = str2;
            }
            String str4 = str2;
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            StringBuilder sbAppend2 = new StringBuilder().append("Add ");
            if (!Main.S.equals(this.mt)) {
                str3 = str4;
            }
            builder.setTitle(Main.isRussian() ? ("\u041d\u0430\u0431\u043e\u0440 " + str3) : (str3 + " pack")).setItems((CharSequence[]) arrayList.toArray(new String[0]), new DialogInterface.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.35
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i2) {
                    FolderBarManager.this.m16xe9ff7e7(listInstalledPacks, str, dialogInterface, i2);
                }
            }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.36
                public final void onClick(AlertDialog alertDialog, int i2) {
                    alertDialog.dismiss();
                }
            }).show();
        }

        /* JADX INFO: renamed from: lambda$showAddPackDialog$14$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m16xe9ff7e7(List list, String str, DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            Map<String, Object> map = (Map) list.get(i);
            JSONArray jSONArrayFolders = Main.folders(this.mt);
            int i2 = 0;
            for (int i3 = 0; i3 < jSONArrayFolders.length(); i3++) {
                JSONObject jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i3);
                if (jSONObjectOptJSONObject != null && str.equals(jSONObjectOptJSONObject.optString("id"))) {
                    JSONArray jSONArrayOptJSONArray = jSONObjectOptJSONObject.optJSONArray("packs");
                    if (jSONArrayOptJSONArray == null) {
                        jSONArrayOptJSONArray = new JSONArray();
                        try {
                            jSONObjectOptJSONObject.put("packs", jSONArrayOptJSONArray);
                        } catch (Throwable th) {
                        }
                    }
                    for (int i4 = 0; i4 < jSONArrayOptJSONArray.length(); i4++) {
                        JSONObject jSONObjectOptJSONObject2 = jSONArrayOptJSONArray.optJSONObject(i4);
                        if (jSONObjectOptJSONObject2 != null && String.valueOf(map.get("id")).equals(jSONObjectOptJSONObject2.optString("id"))) {
                            JSONArray jSONArray = new JSONArray();
                            while (i2 < jSONArrayOptJSONArray.length()) {
                                if (i2 != i4) {
                                    jSONArray.put(jSONArrayOptJSONArray.optJSONObject(i2));
                                }
                                i2++;
                            }
                            try {
                                jSONObjectOptJSONObject.put("packs", jSONArray);
                            } catch (Throwable th2) {
                            }
                            i2 = 1;
                            break;
                        }
                    }
                    Main.saveFolders(this.mt, jSONArrayFolders);
                    if (i2 != 0) {
                        Main.bulletinSuccess(Main.isRussian() ? ("\u0423\u0434\u0430\u043b\u0435\u043d\u043e \"" + map.get("name") + "\"") : ("Removed \"" + map.get("name") + "\""));
                        applyFolderFilter();
                        return;
                    } else {
                        promptPackName(this.mt, str, map);
                        return;
                    }
                }
            }
        }

        void promptPackName(final String str, final String str2, final Map<String, Object> map) {
            Activity activity = Main.getActivity();
            if (activity == null) {
                return;
            }
            final EditText editText = new EditText(activity);
            editText.setText(String.valueOf(map.get("name")));
            editText.setHint(Main.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0434\u043b\u044f \u0432\u043a\u043b\u0430\u0434\u043a\u0438 \u043d\u0430\u0431\u043e\u0440\u0430", "Name for this pack tab"));
            editText.setTextSize(1, 16.0f);
            editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            editText.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
            editText.setPadding(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(12.0f), AndroidUtilities.dp(24.0f), AndroidUtilities.dp(12.0f));
            editText.setSingleLine(true);
            editText.setSelection(String.valueOf(map.get("name")).length());
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(0);
            gradientDrawable.setCornerRadius(AndroidUtilities.dp(12.0f));
            gradientDrawable.setColor(Theme.getColor(Theme.key_dialogBackground));
            gradientDrawable.setStroke(AndroidUtilities.dp(1.0f), Theme.getColor(Theme.key_divider));
            editText.setBackground(gradientDrawable);
            FrameLayout frameLayout = new FrameLayout(activity);
            frameLayout.setPadding(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(24.0f), AndroidUtilities.dp(4.0f));
            frameLayout.addView(editText, new FrameLayout.LayoutParams(-1, -2));
            new AlertDialog.Builder(activity).setTitle(Main.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043d\u0430\u0431\u043e\u0440\u0430", "Pack name")).setView(frameLayout).setPositiveButton(Main.tr("\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c", "Add"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.16
                public final void onClick(AlertDialog alertDialog, int i) {
                    FolderBarManager.this.m14x7e3ad7ec(editText, map, str, str2, alertDialog, i);
                }
            }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.17
                public final void onClick(AlertDialog alertDialog, int i) {
                    alertDialog.dismiss();
                }
            }).show();
        }

        /* JADX INFO: renamed from: lambda$promptPackName$16$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m14x7e3ad7ec(EditText editText, Map map, String str, String str2, AlertDialog alertDialog, int i) {
            String strTrim = String.valueOf(editText.getText()).trim();
            if (strTrim.isEmpty()) {
                strTrim = String.valueOf(map.get("name"));
            }
            JSONArray jSONArrayFolders = Main.folders(str);
            for (int i2 = 0; i2 < jSONArrayFolders.length(); i2++) {
                JSONObject jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i2);
                if (jSONObjectOptJSONObject != null && str2.equals(jSONObjectOptJSONObject.optString("id"))) {
                    JSONArray jSONArrayOptJSONArray = jSONObjectOptJSONObject.optJSONArray("packs");
                    if (jSONArrayOptJSONArray == null) {
                        jSONArrayOptJSONArray = new JSONArray();
                    }
                    JSONObject jSONObject = new JSONObject();
                    try {
                        jSONObject.put("id", String.valueOf(map.get("id")));
                        jSONObject.put("name", strTrim);
                    } catch (Throwable th) {
                    }
                    jSONArrayOptJSONArray.put(jSONObject);
                    try {
                        jSONObjectOptJSONObject.put("packs", jSONArrayOptJSONArray);
                        break;
                    } catch (Throwable th2) {
                        break;
                    }
                }
            }
            Main.saveFolders(str, jSONArrayFolders);
            Main.bulletinSuccess(Main.isRussian() ? ("\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u043e \"" + strTrim + "\"") : ("Added \"" + strTrim + "\""));
            applyFolderFilter();
            alertDialog.dismiss();
        }

        View createSubTab(Context context, final String str, final String str2) {
            FrameLayout frameLayout = new FrameLayout(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, AndroidUtilities.dp(28.0f));
            layoutParams.setMargins(AndroidUtilities.dp(3.0f), 0, AndroidUtilities.dp(3.0f), 0);
            frameLayout.setLayoutParams(layoutParams);
            int iDp = AndroidUtilities.dp(10.0f);
            frameLayout.setPadding(iDp, 0, iDp, 0);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(0);
            gradientDrawable.setCornerRadius(AndroidUtilities.dp(8.0f));
            boolean zEquals = str.equals(this.activeSubTab);
            Integer numSubtabBgColor = Main.subtabBgColor();
            Integer numSubtabTextColor = Main.subtabTextColor();
            if (zEquals) {
                gradientDrawable.setColor(numSubtabBgColor != null ? numSubtabBgColor.intValue() : Theme.getColor(Theme.key_chat_emojiPanelStickerPackSelectorLine));
            } else {
                gradientDrawable.setColor(0);
            }
            frameLayout.setBackground(gradientDrawable);
            frameLayout.setForeground(Main.createRippleDrawable(0, 8));
            TextView textView = new TextView(context);
            textView.setText(str2);
            textView.setTextSize(1, 12.0f);
            textView.setGravity(17);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setMaxWidth(AndroidUtilities.dp(120.0f));
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            if (numSubtabTextColor != null) {
                textView.setTextColor(numSubtabTextColor.intValue());
            } else if (zEquals) {
                textView.setTextColor(-1);
            } else {
                textView.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelIcon));
            }
            frameLayout.addView(textView, new FrameLayout.LayoutParams(-2, -1, 17));
            frameLayout.setOnClickListener(new View.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.6
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    FolderBarManager.this.m9xa1b7b845(str, view);
                }
            });
            frameLayout.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.7
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return FolderBarManager.this.m10xe542d606(str, str2, view);
                }
            });
            return frameLayout;
        }

        /* JADX INFO: renamed from: lambda$createSubTab$18$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m9xa1b7b845(String str, View view) {
            this.activeSubTab = str;
            applyFolderFilter();
        }

        /* JADX INFO: renamed from: lambda$createSubTab$19$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ boolean m10xe542d606(String str, String str2, View view) {
            showSubTabContextMenu(str, str2);
            return true;
        }

        void showSubTabContextMenu(final String str, final String str2) {
            Activity activity;
            if ("custom".equals(str) || (activity = Main.getActivity()) == null) {
                return;
            }
            final String str3 = this.activeFolderId;
            JSONObject jSONObjectFindFolder = Main.findFolder(this.mt, str3);
            JSONArray jSONArrayOptJSONArray = jSONObjectFindFolder != null ? jSONObjectFindFolder.optJSONArray("packs") : null;
            if (jSONArrayOptJSONArray == null) {
                jSONArrayOptJSONArray = new JSONArray();
            }
            int i = 0;
            while (true) {
                if (i >= jSONArrayOptJSONArray.length()) {
                    i = -1;
                    break;
                }
                JSONObject jSONObjectOptJSONObject = jSONArrayOptJSONArray.optJSONObject(i);
                if (jSONObjectOptJSONObject != null && str.equals(jSONObjectOptJSONObject.optString("id"))) {
                    break;
                } else {
                    i++;
                }
            }
            ArrayList arrayList = new ArrayList();
            int i2 = i;
            final ArrayList arrayList2 = new ArrayList();
            if (i2 > 0) {
                arrayList.add(Main.tr("\u2b06\ufe0f  \u0421\u0434\u0432\u0438\u043d\u0443\u0442\u044c \u0432\u043b\u0435\u0432\u043e", "\u2b06\ufe0f  Move left"));
                arrayList2.add("left");
            }
            if (i2 >= 0 && i2 < jSONArrayOptJSONArray.length() - 1) {
                arrayList.add(Main.tr("\u2b07\ufe0f  \u0421\u0434\u0432\u0438\u043d\u0443\u0442\u044c \u0432\u043f\u0440\u0430\u0432\u043e", "\u2b07\ufe0f  Move right"));
                arrayList2.add("right");
            }
            arrayList.add(Main.tr("\u270f\ufe0f  \u041f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u0442\u044c", "\u270f\ufe0f  Rename"));
            arrayList2.add("rename");
            arrayList.add(Main.tr("\ud83d\uddd1  \u0423\u0434\u0430\u043b\u0438\u0442\u044c", "\ud83d\uddd1  Remove"));
            arrayList2.add("delete");
            new AlertDialog.Builder(activity).setTitle(str2).setItems((CharSequence[]) arrayList.toArray(new String[0]), new DialogInterface.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.32
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i3) {
                    FolderBarManager.this.m20x5f46bab(arrayList2, str3, str, str2, dialogInterface, i3);
                }
            }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.33
                public final void onClick(AlertDialog alertDialog, int i3) {
                    alertDialog.dismiss();
                }
            }).show();
        }

        /* JADX INFO: renamed from: lambda$showSubTabContextMenu$20$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m20x5f46bab(List list, String str, String str2, String str3, DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            String str4 = (String) list.get(i);
            JSONArray jSONArrayFolders = Main.folders(this.mt);
            int i2 = 0;
            for (int i3 = 0; i3 < jSONArrayFolders.length(); i3++) {
                JSONObject jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i3);
                if (jSONObjectOptJSONObject != null && str.equals(jSONObjectOptJSONObject.optString("id"))) {
                    JSONArray jSONArrayOptJSONArray = jSONObjectOptJSONObject.optJSONArray("packs");
                    if (jSONArrayOptJSONArray == null) {
                        jSONArrayOptJSONArray = new JSONArray();
                    }
                    if ("left".equals(str4) || "right".equals(str4)) {
                        while (true) {
                            if (i2 >= jSONArrayOptJSONArray.length()) {
                                i2 = -1;
                                break;
                            }
                            JSONObject jSONObjectOptJSONObject2 = jSONArrayOptJSONArray.optJSONObject(i2);
                            if (jSONObjectOptJSONObject2 != null && str2.equals(jSONObjectOptJSONObject2.optString("id"))) {
                                break;
                            } else {
                                i2++;
                            }
                        }
                        int i4 = ("left".equals(str4) ? -1 : 1) + i2;
                        if (i2 >= 0 && i4 >= 0 && i4 < jSONArrayOptJSONArray.length()) {
                            JSONObject jSONObjectOptJSONObject3 = jSONArrayOptJSONArray.optJSONObject(i2);
                            try {
                                jSONArrayOptJSONArray.put(i2, jSONArrayOptJSONArray.optJSONObject(i4));
                                jSONArrayOptJSONArray.put(i4, jSONObjectOptJSONObject3);
                            } catch (Throwable th) {
                            }
                            Main.saveFolders(this.mt, jSONArrayFolders);
                            applyFolderFilter();
                            return;
                        }
                        return;
                    }
                    if ("rename".equals(str4)) {
                        showRenamePackDialog(this.mt, str, str2, str3);
                        return;
                    }
                    if ("delete".equals(str4)) {
                        JSONArray jSONArray = new JSONArray();
                        while (i2 < jSONArrayOptJSONArray.length()) {
                            JSONObject jSONObjectOptJSONObject4 = jSONArrayOptJSONArray.optJSONObject(i2);
                            if (jSONObjectOptJSONObject4 == null || !str2.equals(jSONObjectOptJSONObject4.optString("id"))) {
                                jSONArray.put(jSONObjectOptJSONObject4);
                            }
                            i2++;
                        }
                        try {
                            jSONObjectOptJSONObject.put("packs", jSONArray);
                        } catch (Throwable th2) {
                        }
                        Main.saveFolders(this.mt, jSONArrayFolders);
                        if (str2.equals(this.activeSubTab)) {
                            this.activeSubTab = "custom";
                        }
                        applyFolderFilter();
                        return;
                    }
                    return;
                }
            }
        }

        void showRenamePackDialog(final String str, final String str2, final String str3, String str4) {
            Activity activity = Main.getActivity();
            if (activity == null) {
                return;
            }
            final EditText editText = new EditText(activity);
            editText.setText(str4);
            editText.setTextSize(1, 16.0f);
            editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            editText.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
            editText.setPadding(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(12.0f), AndroidUtilities.dp(24.0f), AndroidUtilities.dp(12.0f));
            editText.setSingleLine(true);
            editText.setSelection(str4.length());
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(0);
            gradientDrawable.setCornerRadius(AndroidUtilities.dp(12.0f));
            gradientDrawable.setColor(Theme.getColor(Theme.key_dialogBackground));
            gradientDrawable.setStroke(AndroidUtilities.dp(1.0f), Theme.getColor(Theme.key_divider));
            editText.setBackground(gradientDrawable);
            FrameLayout frameLayout = new FrameLayout(activity);
            frameLayout.setPadding(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(24.0f), AndroidUtilities.dp(4.0f));
            frameLayout.addView(editText, new FrameLayout.LayoutParams(-1, -2));
            new AlertDialog.Builder(activity).setTitle(Main.tr("\u041f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u0442\u044c", "Rename")).setView(frameLayout).setPositiveButton(Main.tr("\u041f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u0442\u044c", "Rename"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.29
                public final void onClick(AlertDialog alertDialog, int i) {
                    FolderBarManager.this.m19x36cb4393(editText, str, str2, str3, alertDialog, i);
                }
            }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.30
                public final void onClick(AlertDialog alertDialog, int i) {
                    alertDialog.dismiss();
                }
            }).show();
        }

        /* JADX INFO: renamed from: lambda$showRenamePackDialog$22$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m19x36cb4393(EditText editText, String str, String str2, String str3, AlertDialog alertDialog, int i) {
            String strTrim = String.valueOf(editText.getText()).trim();
            if (!strTrim.isEmpty()) {
                JSONArray jSONArrayFolders = Main.folders(str);
                for (int i2 = 0; i2 < jSONArrayFolders.length(); i2++) {
                    JSONObject jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i2);
                    if (jSONObjectOptJSONObject != null && str2.equals(jSONObjectOptJSONObject.optString("id"))) {
                        JSONArray jSONArrayOptJSONArray = jSONObjectOptJSONObject.optJSONArray("packs");
                        if (jSONArrayOptJSONArray == null) {
                            break;
                        }
                        for (int i3 = 0; i3 < jSONArrayOptJSONArray.length(); i3++) {
                            JSONObject jSONObjectOptJSONObject2 = jSONArrayOptJSONArray.optJSONObject(i3);
                            if (jSONObjectOptJSONObject2 != null && str3.equals(jSONObjectOptJSONObject2.optString("id"))) {
                                try {
                                    jSONObjectOptJSONObject2.put("name", strTrim);
                                    break;
                                } catch (Throwable th) {
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
                Main.saveFolders(str, jSONArrayFolders);
                applyFolderFilter();
            }
            alertDialog.dismiss();
        }

        Map<String, TLRPC.Document> allDocsMap() {
            if (this.docMapCache != null && System.currentTimeMillis() - this.docMapCacheAt < 30000) {
                return this.docMapCache;
            }
            HashMap map = new HashMap();
            try {
                MediaDataController mediaDataController = MediaDataController.getInstance(UserConfig.selectedAccount);
                if (Main.S.equals(this.mt)) {
                    try {
                        ArrayList<TLRPC.Document> recentStickers = mediaDataController.getRecentStickers(0);
                        if (recentStickers != null) {
                            for (TLRPC.Document document : recentStickers) {
                                map.put(String.valueOf(document.id), document);
                            }
                        }
                    } catch (Throwable th) {
                    }
                    ArrayList<TLRPC.Document> recentStickers2 = mediaDataController.getRecentStickers(2);
                    if (recentStickers2 != null) {
                        for (TLRPC.Document document2 : recentStickers2) {
                            map.put(String.valueOf(document2.id), document2);
                        }
                    }
                    try {
                        Main.collectPackDocs(mediaDataController.getStickerSets(0), map);
                    } catch (Throwable th2) {
                    }
                } else {
                    try {
                        Main.collectPackDocs(mediaDataController.getStickerSets(5), map);
                    } catch (Throwable th3) {
                    }
                    try {
                        Main.collectPackDocs(Main.gf(this.emojiView, "installedEmojiSets"), map);
                    } catch (Throwable th4) {
                    }
                }
            } catch (Throwable th5) {
            }
            this.docMapCache = map;
            this.docMapCacheAt = System.currentTimeMillis();
            Main.ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.28
                @Override // java.lang.Runnable
                public final void run() {
                    FolderBarManager.this.m0x1be87cd1();
                }
            }, 30000L);
            return map;
        }

        /* JADX INFO: renamed from: lambda$allDocsMap$24$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m0x1be87cd1() {
            this.docMapCache = null;
        }

        void populateCustomGrid(List<TLRPC.Document> list) {
            if (this.customGrid == null) {
                return;
            }
            this.customGrid.removeAllViews();
            if (list.isEmpty()) {
                addEmptyMessage();
                return;
            }
            Context context = this.customGrid.getContext();
            int i = AndroidUtilities.displaySize.x;
            int iMax = Math.max(1, i / AndroidUtilities.dp(cellDp()));
            int iDp = (i - AndroidUtilities.dp(16.0f)) / iMax;
            LinearLayout linearLayout = null;
            for (int i2 = 0; i2 < list.size(); i2++) {
                if (i2 % iMax == 0) {
                    linearLayout = new LinearLayout(context);
                    linearLayout.setOrientation(0);
                    linearLayout.setGravity(3);
                    this.customGrid.addView(linearLayout, new LinearLayout.LayoutParams(-1, -2));
                }
                linearLayout.addView(Main.S.equals(this.mt) ? createStickerCell(context, list.get(i2)) : createEmojiCell(context, list.get(i2)), new LinearLayout.LayoutParams(iDp, iDp));
            }
        }

        void addEmptyMessage() {
            Context context = this.customGrid.getContext();
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setGravity(17);
            linearLayout.setPadding(0, AndroidUtilities.dp(60.0f), 0, 0);
            TextView textView = new TextView(context);
            textView.setText(Main.emptyIcon(this.mt));
            textView.setTextSize(1, 48.0f);
            textView.setGravity(17);
            linearLayout.addView(textView);
            TextView textView2 = new TextView(context);
            textView2.setText(Main.isRussian() ? ("\u0412 \u044d\u0442\u043e\u0439 \u043f\u0430\u043f\u043a\u0435 \u043d\u0435\u0442 " + Main.mediaLabel(this.mt)) : ("No " + Main.mediaLabelPlural(this.mt) + " in this folder"));
            textView2.setTextSize(1, 15.0f);
            textView2.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelEmptyText));
            textView2.setGravity(17);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.topMargin = AndroidUtilities.dp(12.0f);
            linearLayout.addView(textView2, layoutParams);
            TextView textView3 = new TextView(context);
            textView3.setText(Main.isRussian() ? ("\u0417\u0430\u0436\u043c\u0438\u0442\u0435 " + Main.mediaLabelSingular(this.mt) + ", \u0447\u0442\u043e\u0431\u044b \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0441\u044e\u0434\u0430") : ("Long press a " + Main.mediaLabel(this.mt).toLowerCase() + " to add it here"));
            textView3.setTextSize(1, 13.0f);
            textView3.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelEmptyText));
            textView3.setAlpha(0.6f);
            textView3.setGravity(17);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams2.topMargin = AndroidUtilities.dp(4.0f);
            linearLayout.addView(textView3, layoutParams2);
            this.customGrid.addView(linearLayout, new LinearLayout.LayoutParams(-1, -2));
        }

        View createStickerCell(Context context, TLRPC.Document document) {
            final TLRPC.Document document2 = document;
            Drawable svgThumb;
            BackupImageView backupImageView = new BackupImageView(context);
            backupImageView.setRoundRadius(AndroidUtilities.dp(6.0f));
            backupImageView.setAspectFit(true);
            try {
                TLRPC.PhotoSize photoSize = (document.thumbs == null || document.thumbs.isEmpty()) ? null : (TLRPC.PhotoSize) document.thumbs.get(0);
                try {
                    svgThumb = DocumentObject.getSvgThumb(document, Theme.key_chat_emojiPanelBackground, 0.2f);
                } catch (Throwable th) {
                    svgThumb = null;
                }
                ImageLocation forDocument = null;
                ImageLocation forDocument2 = ImageLocation.getForDocument(document);
                if (photoSize != null) {
                    forDocument = ImageLocation.getForDocument(photoSize, document);
                }
                if (forDocument != null) {
                    try {
                        backupImageView.setImage(forDocument2, "66_66", forDocument, (String) null, svgThumb, document);
                    } catch (Throwable th2) {
                        try {
                            backupImageView.setImage(ImageLocation.getForDocument(document2), "66_66", (String) null, (Drawable) null, document2);
                        } catch (Throwable th3) {
                        }
                    }
                } else {
                    Drawable drawable = svgThumb;
                        try {
                        backupImageView.setImage(forDocument2, "66_66", drawable, document2);
                    } catch (Throwable th4) {
                        backupImageView.setImage(ImageLocation.getForDocument(document2), "66_66", (String) null, (Drawable) null, document2);
                    }
                }
            } catch (Throwable th5) {
            }
            final String strValueOf = String.valueOf(document2.id);
            backupImageView.setOnClickListener(new View.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.9
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    FolderBarManager.this.m7x5a58969(document2, view);
                }
            });
            backupImageView.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.10
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return FolderBarManager.this.m8x4930a72a(strValueOf, view);
                }
            });
            return backupImageView;
        }

        /* JADX INFO: renamed from: lambda$createStickerCell$25$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m7x5a58969(TLRPC.Document document, View view) {
            try {
                Object objGf = Main.gf(this.emojiView, "delegate");
                if (objGf instanceof EmojiView.EmojiViewDelegate) {
                    
                    for (Method mSelect : objGf.getClass().getMethods()) {
                        if ("onStickerSelected".equals(mSelect.getName())) {
                            Class<?>[] p = mSelect.getParameterTypes();
                            Object[] args = new Object[p.length];
                            args[0] = view;
                            args[1] = document;
                            if (args.length > 2) args[2] = null;
                            if (args.length > 3) args[3] = null;
                            if (args.length > 4) args[4] = null;
                            if (args.length > 5) args[5] = Boolean.TRUE;
                            if (args.length > 6) args[6] = Integer.valueOf(0);
                            if (args.length > 7) args[7] = Integer.valueOf(0);
                            mSelect.invoke(objGf, args);
                            break;
                        }
                    }

                }
            } catch (Throwable th) {
            }
        }

        /* JADX INFO: renamed from: lambda$createStickerCell$26$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ boolean m8x4930a72a(String str, View view) {
            showRemoveDialog(str);
            return true;
        }

        View createEmojiCell(Context context, TLRPC.Document document) {
            final TLRPC.Document document2 = document;
            Drawable svgThumb;
            BackupImageView backupImageView = new BackupImageView(context);
            backupImageView.setAspectFit(true);
            try {
                TLRPC.PhotoSize photoSize = (document.thumbs == null || document.thumbs.isEmpty()) ? null : (TLRPC.PhotoSize) document.thumbs.get(0);
                try {
                    svgThumb = DocumentObject.getSvgThumb(document, Theme.key_chat_emojiPanelBackground, 0.2f);
                } catch (Throwable th) {
                    svgThumb = null;
                }
                ImageLocation forDocument = null;
                ImageLocation forDocument2 = ImageLocation.getForDocument(document);
                if (photoSize != null) {
                    forDocument = ImageLocation.getForDocument(photoSize, document);
                }
                if (forDocument != null) {
                    try {
                        backupImageView.setImage(forDocument2, "36_36", forDocument, (String) null, svgThumb, document);
                    } catch (Throwable th2) {
                        try {
                            backupImageView.setImage(ImageLocation.getForDocument(document2), "36_36", (String) null, (Drawable) null, document2);
                        } catch (Throwable th3) {
                        }
                    }
                } else {
                    Drawable drawable = svgThumb;
                        try {
                        backupImageView.setImage(forDocument2, "36_36", drawable, document2);
                    } catch (Throwable th4) {
                        backupImageView.setImage(ImageLocation.getForDocument(document2), "36_36", (String) null, (Drawable) null, document2);
                    }
                }
            } catch (Throwable th5) {
            }
            final String strValueOf = String.valueOf(document2.id);
            backupImageView.setOnClickListener(new View.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.22
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    FolderBarManager.this.m4xc255042(document2, view);
                }
            });
            backupImageView.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.23
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return FolderBarManager.this.m5x4fb06e03(strValueOf, view);
                }
            });
            return backupImageView;
        }

        /* JADX INFO: renamed from: lambda$createEmojiCell$27$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m4xc255042(TLRPC.Document document, View view) {
            try {
                Object objGf = Main.gf(this.emojiView, "delegate");
                if (objGf instanceof EmojiView.EmojiViewDelegate) {
                    ((EmojiView.EmojiViewDelegate) objGf).onCustomEmojiSelected(document.id, document, (String) null, false);
                }
            } catch (Throwable th) {
            }
        }

        /* JADX INFO: renamed from: lambda$createEmojiCell$28$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ boolean m5x4fb06e03(String str, View view) {
            showRemoveDialog(str);
            return true;
        }

        void showRemoveDialog(final String str) {
            Activity activity = Main.getActivity();
            if (activity == null) {
                return;
            }
            final String str2 = this.activeFolderId;
            AlertDialog.Builder title = new AlertDialog.Builder(activity).setTitle(Main.tr("\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0438\u0437 \u043f\u0430\u043f\u043a\u0438", "Remove from folder"));
            StringBuilder sbAppend = new StringBuilder().append("Remove this ");
            String str3 = this.mt;
            String str4 = Main.S;
            if (!Main.S.equals(str3)) {
                str4 = Main.E;
            }
            title.setMessage(Main.isRussian() ? ("\u0423\u0434\u0430\u043b\u0438\u0442\u044c " + str4 + " \u0438\u0437 \u0442\u0435\u043a\u0443\u0449\u0435\u0439 \u043f\u0430\u043f\u043a\u0438?") : ("Remove " + str4 + " from the current folder?")).setPositiveButton(Main.tr("\u0423\u0434\u0430\u043b\u0438\u0442\u044c", "Remove"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.20
                public final void onClick(AlertDialog alertDialog, int i) {
                    FolderBarManager.this.m18x5d6b90ad(str2, str, alertDialog, i);
                }
            }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.21
                public final void onClick(AlertDialog alertDialog, int i) {
                    alertDialog.dismiss();
                }
            }).show();
        }

        /* JADX INFO: renamed from: lambda$showRemoveDialog$29$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m18x5d6b90ad(String str, String str2, AlertDialog alertDialog, int i) {
            alertDialog.dismiss();
            JSONArray jSONArrayFolders = Main.folders(this.mt);
            for (int i2 = 0; i2 < jSONArrayFolders.length(); i2++) {
                JSONObject jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i2);
                if (jSONObjectOptJSONObject != null && str.equals(jSONObjectOptJSONObject.optString("id"))) {
                    JSONArray jSONArrayOptJSONArray = jSONObjectOptJSONObject.optJSONArray("items");
                    if (jSONArrayOptJSONArray == null) {
                        jSONArrayOptJSONArray = new JSONArray();
                    }
                    JSONArray jSONArray = new JSONArray();
                    for (int i3 = 0; i3 < jSONArrayOptJSONArray.length(); i3++) {
                        if (!str2.equals(Main.itemId(jSONArrayOptJSONArray.optJSONObject(i3)))) {
                            jSONArray.put(jSONArrayOptJSONArray.optJSONObject(i3));
                        }
                    }
                    try {
                        jSONObjectOptJSONObject.put("items", jSONArray);
                        break;
                    } catch (Throwable th) {
                        break;
                    }
                }
            }
            Main.saveFolders(this.mt, jSONArrayFolders);
            applyFolderFilter();
        }

        void applyFolderFilter() {
            if (!Main.foldersEnabled(this.mt)) {
                try {
                    if (this.customScroll != null) {
                        this.customScroll.setVisibility(8);
                    }
                    if (this.emptyOverlay != null) {
                        this.emptyOverlay.setVisibility(8);
                    }
                    View view = Main.gf(this.emojiView, gridField()) instanceof View ? (View) Main.gf(this.emojiView, gridField()) : null;
                    if (view != null) {
                        view.setVisibility(0);
                    }
                    View view2 = Main.gf(this.emojiView, searchField()) instanceof View ? (View) Main.gf(this.emojiView, searchField()) : null;
                    if (view2 != null) {
                        view2.setVisibility(0);
                        return;
                    }
                    return;
                } catch (Throwable th) {
                    return;
                }
            }
            if (Main.G.equals(this.mt)) {
                applyGifFilter();
            } else {
                applyStickerEmojiFilter();
            }
        }

        void applyGifFilter() {
            try {
                final View view = Main.gf(this.emojiView, "gifGridView") instanceof View ? (View) Main.gf(this.emojiView, "gifGridView") : null;
                final Object objGf = Main.gf(this.emojiView, "gifAdapter");
                if (view == null || objGf == null) {
                    return;
                }

                int account = UserConfig.selectedAccount;
                try {
                    Object acc = Main.gf(this.emojiView, "currentAccount");
                    if (acc instanceof Integer) {
                        account = ((Integer) acc).intValue();
                    }
                } catch (Throwable ignored) {}

                ArrayList<TLRPC.Document> mdRecent = MediaDataController.getInstance(account).getRecentGifs();
                if (this.allRecentGifs == null || "all".equals(this.activeFolderId)) {
                    if (mdRecent != null && !mdRecent.isEmpty()) {
                        this.allRecentGifs = new ArrayList<>(mdRecent);
                    } else {
                        Object evRecent = Main.gf(this.emojiView, "recentGifs");
                        if (evRecent instanceof ArrayList && !((ArrayList) evRecent).isEmpty()) {
                            this.allRecentGifs = new ArrayList<>((ArrayList<TLRPC.Document>) evRecent);
                        }
                    }
                }

                ArrayList<TLRPC.Document> sourceGifs = this.allRecentGifs != null ? this.allRecentGifs : (mdRecent != null ? mdRecent : new ArrayList<TLRPC.Document>());

                final String str = this.activeFolderId;
                final View view2 = this.emptyOverlay;
                ArrayList arrayList;
                TLRPC.Document document;

                if ("all".equals(str)) {
                    arrayList = new ArrayList(sourceGifs);
                } else {
                    JSONObject jSONObjectFindFolder = Main.findFolder(Main.G, str);
                    JSONArray jSONArrayOptJSONArray = jSONObjectFindFolder != null ? jSONObjectFindFolder.optJSONArray("items") : null;
                    if (jSONArrayOptJSONArray == null) {
                        jSONArrayOptJSONArray = new JSONArray();
                    }
                    HashMap map = new HashMap();
                    for (TLRPC.Document document2 : sourceGifs) {
                        map.put(String.valueOf(document2.id), document2);
                    }
                    if (mdRecent != null) {
                        for (TLRPC.Document document2 : mdRecent) {
                            map.put(String.valueOf(document2.id), document2);
                        }
                    }
                    ArrayList arrayList2 = new ArrayList();
                    HashSet hashSet = new HashSet();
                    for (int i = 0; i < jSONArrayOptJSONArray.length(); i++) {
                        String strItemId = Main.itemId(jSONArrayOptJSONArray.optJSONObject(i));
                        if (!hashSet.contains(strItemId) && (document = (TLRPC.Document) map.get(strItemId)) != null) {
                            arrayList2.add(document);
                            hashSet.add(strItemId);
                        }
                    }
                    if (!arrayList2.isEmpty()) {
                        try {
                            arrayList2.add((TLRPC.Document) arrayList2.get(arrayList2.size() - 1));
                        } catch (Throwable th) {
                        }
                    }
                    arrayList = arrayList2;
                }

                final boolean isEmpty = !"all".equals(str) && arrayList.isEmpty();
                final ArrayList arrayList3 = arrayList;
                final int[] iArr = {0};

                Main.ui(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (view instanceof RecyclerView) {
                                try {
                                    RecyclerView recyclerView = (RecyclerView) view;
                                    recyclerView.stopScroll();
                                    if (recyclerView.isComputingLayout()) {
                                        if (iArr[0] <= 10) {
                                            iArr[0]++;
                                            Main.ui(this, 16L);
                                            return;
                                        }
                                    }
                                } catch (Throwable ignored) {}
                            }

                            Main.sf(FolderBarManager.this.emojiView, "recentGifs", new ArrayList(arrayList3));

                            Object gifSearchAdapter = Main.gf(FolderBarManager.this.emojiView, "gifSearchAdapter");
                            Object currentAdapter = (view instanceof RecyclerView) ? ((RecyclerView) view).getAdapter() : null;

                            boolean isAll = "all".equals(str);
                            boolean zHideTrending = Main.hideTrending();

                            Object[] adaptersToUpdate = new Object[]{objGf, gifSearchAdapter, currentAdapter};
                            for (Object ad : adaptersToUpdate) {
                                if (ad == null) continue;
                                if (!isAll || zHideTrending) {
                                    Main.sf(ad, "trendingSectionItem", -1);
                                    Main.sf(ad, "firstResultItem", -1);
                                    Main.sf(ad, "showTrendingWhenSearchEmpty", false);
                                    Main.sf(ad, "showTrendingWhenSearching", false);
                                    try {
                                        Object resultsObj = Main.gf(ad, "results");
                                        if (resultsObj instanceof ArrayList) {
                                            ((ArrayList) resultsObj).clear();
                                        }
                                    } catch (Throwable ignored) {}
                                    Main.sf(ad, "nextSearchOffset", null);
                                    Main.sf(ad, "searchingUser", false);
                                    Main.sf(ad, "lastSearchIsEmoji", false);
                                } else {
                                    Main.sf(ad, "showTrendingWhenSearchEmpty", true);
                                    Main.sf(ad, "showTrendingWhenSearching", true);
                                }
                                Main.sf(ad, "recentItemsCount", Integer.valueOf(arrayList3.size()));
                                Main.notifyAdapter(ad);
                            }

                            if (view instanceof RecyclerView) {
                                RecyclerView recyclerView = (RecyclerView) view;
                                if (objGf instanceof RecyclerView.Adapter && recyclerView.getAdapter() != objGf) {
                                    recyclerView.setAdapter((RecyclerView.Adapter) objGf);
                                }
                                try {
                                    recyclerView.scrollToPosition(0);
                                } catch (Throwable ignored) {}
                            }

                            if (isEmpty) {
                                view.setVisibility(4);
                                if (view2 != null) {
                                    view2.setVisibility(0);
                                    view2.bringToFront();
                                }
                            } else {
                                view.setVisibility(0);
                                if (view2 != null) {
                                    view2.setVisibility(8);
                                }
                                view.bringToFront();
                            }
                            if (FolderBarManager.this.folderBar != null) {
                                FolderBarManager.this.folderBar.bringToFront();
                            }
                        } catch (Throwable th4) {
                            Main.err(th4, "gif ui run filter");
                        }
                    }
                });
            } catch (Throwable th2) {
                Main.err(th2, "gif apply folder filter");
            }
        }

        void applyStickerEmojiFilter() {
            final ArrayList<TLRPC.Document> arrayListPackDocuments;
            try {
                View view = Main.gf(this.emojiView, gridField()) instanceof View ? (View) Main.gf(this.emojiView, gridField()) : null;
                if (view == null) {
                    return;
                }
                String str = this.activeFolderId;
                View view2 = Main.gf(this.emojiView, searchField()) instanceof View ? (View) Main.gf(this.emojiView, searchField()) : null;
                if ("all".equals(str)) {
                    if (view2 != null) {
                        view2.setVisibility(0);
                    }
                    view.setVisibility(0);
                    if (this.customScroll != null) {
                        this.customScroll.setVisibility(8);
                    }
                    if (this.folderBar != null) {
                        this.folderBar.bringToFront();
                        return;
                    }
                    return;
                }
                if (view2 != null) {
                    view2.setVisibility(8);
                }
                view.setVisibility(8);
                JSONObject jSONObjectFindFolder = Main.findFolder(this.mt, str);
                if (jSONObjectFindFolder == null) {
                    jSONObjectFindFolder = new JSONObject();
                }
                buildSubTabs(jSONObjectFindFolder);
                String str2 = this.activeSubTab;
                if ("custom".equals(str2)) {
                    JSONArray jSONArrayOptJSONArray = jSONObjectFindFolder.optJSONArray("items");
                    if (jSONArrayOptJSONArray == null) {
                        jSONArrayOptJSONArray = new JSONArray();
                    }
                    Map<String, TLRPC.Document> mapAllDocsMap = allDocsMap();
                    arrayListPackDocuments = new ArrayList<>();
                    for (int i = 0; i < jSONArrayOptJSONArray.length(); i++) {
                        TLRPC.Document document = mapAllDocsMap.get(Main.itemId(jSONArrayOptJSONArray.optJSONObject(i)));
                        if (document != null) {
                            arrayListPackDocuments.add(document);
                        }
                    }
                } else {
                    arrayListPackDocuments = Main.packDocuments(this.mt, str2);
                }
                Main.ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.FolderBarManager.18
                    @Override // java.lang.Runnable
                    public final void run() {
                        FolderBarManager.this.m1x349c9238(arrayListPackDocuments);
                    }
                });
            } catch (Throwable th) {
                Main.err(th, this.mt + " apply folder filter");
            }
        }

        /* JADX INFO: renamed from: lambda$applyStickerEmojiFilter$31$com-maxplugins-mediafolders-Main$FolderBarManager, reason: not valid java name */
        /* synthetic */ void m1x349c9238(List list) {
            populateCustomGrid(list);
            if (this.customScroll != null) {
                this.customScroll.setVisibility(0);
                this.customScroll.bringToFront();
            }
            if (this.folderBar != null) {
                this.folderBar.bringToFront();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void injectAll(Object obj) {
        try {
            if (gf(obj, "gifContainer") != null) {
                FolderBarManager folderBarManager = GIF_MANAGERS.get(obj);
                if (folderBarManager == null) {
                    folderBarManager = new FolderBarManager(G, obj);
                    GIF_MANAGERS.put(obj, folderBarManager);
                }
                folderBarManager.injectFolderBar();
            }
        } catch (Throwable th) {
            err(th, "gif inject");
        }
        try {
            if (gf(obj, "stickersContainer") != null) {
                FolderBarManager folderBarManager2 = STICKER_MANAGERS.get(obj);
                if (folderBarManager2 == null) {
                    folderBarManager2 = new FolderBarManager(S, obj);
                    STICKER_MANAGERS.put(obj, folderBarManager2);
                }
                folderBarManager2.injectFolderBar();
            }
        } catch (Throwable th2) {
            err(th2, "sticker inject");
        }
        try {
            if (gf(obj, "emojiContainer") != null) {
                FolderBarManager folderBarManager3 = EMOJI_MANAGERS.get(obj);
                if (folderBarManager3 == null) {
                    folderBarManager3 = new FolderBarManager(E, obj);
                    EMOJI_MANAGERS.put(obj, folderBarManager3);
                }
                folderBarManager3.injectFolderBar();
            }
        } catch (Throwable th3) {
            err(th3, "emoji inject");
        }
    }

    static FolderBarManager findGifManager(Object obj) {
        return GIF_MANAGERS.get(obj);
    }

    static FolderBarManager findStickerManager(Object obj) {
        return STICKER_MANAGERS.get(obj);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void addPopupMenuItem(final ContentPreviewViewer contentPreviewViewer, final String str, final String str2, Object obj) {
        View contentView;
        try {
            Object objGf = gf(contentPreviewViewer, "popupWindow");
            if (objGf == null || (contentView = ((PopupWindow) objGf).getContentView()) == null) {
                return;
            }
            if (!contentView.getClass().getName().contains("ActionBarPopupWindowLayout") && (contentView instanceof ViewGroup)) {
                ViewGroup viewGroup = (ViewGroup) contentView;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View childAt = viewGroup.getChildAt(i);
                    if (childAt.getClass().getName().contains("ActionBarPopupWindowLayout")) {
                        contentView = childAt;
                        break;
                    }
                }
            }
            if (contentView instanceof ViewGroup) {
                ActionBarMenuItem.addItem((ViewGroup) contentView, getDrawableRes("msg_fave"), Main.isRussian() ? ("\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0432 \u043f\u0430\u043f\u043a\u0443 " + mediaLabel(str)) : ("Add to " + mediaLabel(str) + " folder"), false, (Theme.ResourcesProvider) obj).setOnClickListener(new View.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.17
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        Main.lambda$addPopupMenuItem$2(contentPreviewViewer, str, str2, view);
                    }
                });
            }
        } catch (Throwable th) {
            err(th, "addPopupMenuItem");
        }
    }

    static /* synthetic */ void lambda$addPopupMenuItem$2(ContentPreviewViewer contentPreviewViewer, final String str, final String str2, View view) {
        try {
            Object objGf = gf(contentPreviewViewer, "popupWindow");
            if (objGf instanceof PopupWindow) {
                ((PopupWindow) objGf).dismiss();
            }
        } catch (Throwable th) {
        }
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.15
            @Override // java.lang.Runnable
            public final void run() {
                Main.showAddToFolderDialog(str, str2);
            }
        });
    }

    static void setupHooks() {
        try {
            Class<?> cls = Class.forName("org.telegram.ui.Components.EmojiView");
            hookMethodsByName(cls, "onAttachedToWindow", attachHook);
            hookMethodsByName(cls, "setAllow", attachHook);
            hookMethodsByName(cls, "updateGifTabs", updateGifTabsHook);
            hookMethodsByName(cls, "updateRecentGifs", recentGifsHook);
            hookMethodsByName(cls, "updateStickerTabs", updateStickerTabsHook);
        } catch (Throwable th) {
            err(th, "setupHooks emojiview");
        }
        try {
            hookMethodsByName(Class.forName("org.telegram.ui.ActionBar.ActionBarPopupWindow"), "showAtLocation", popupShowHook);
        } catch (Throwable th2) {
            err(th2, "setupHooks popup");
        }
        try {
            hookMethodsByName(Class.forName("org.telegram.messenger.AndroidUtilities"), "openForView", openForViewHook);
        } catch (Throwable th3) {
            err(th3, "setupHooks openforview");
        }
        try {
            hookMethodsByName(Class.forName("org.telegram.messenger.AndroidUtilities"), "isInternalUri", isInternalUriHook);
        } catch (Throwable th4) {
            err(th4, "setupHooks isinternaluri");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void showAddToFolderDialog(final String str, final String str2) {
        boolean z;
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        JSONArray jSONArrayFolders = folders(str);
        if (jSONArrayFolders.length() == 0) {
            bulletinError(isRussian() ? ("\u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u0441\u043e\u0437\u0434\u0430\u0439\u0442\u0435 \u043f\u0430\u043f\u043a\u0443, \u043d\u0430\u0436\u0430\u0432 + \u043d\u0430 \u043f\u0430\u043d\u0435\u043b\u0438 " + mediaLabel(str)) : ("Create a folder first by tapping + in the " + mediaLabel(str) + " panel"));
            return;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jSONArrayFolders.length(); i++) {
            JSONObject jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i);
            if (jSONObjectOptJSONObject != null) {
                String strOptString = jSONObjectOptJSONObject.optString("name", "Folder");
                JSONArray jSONArrayOptJSONArray = jSONObjectOptJSONObject.optJSONArray("items");
                if (jSONArrayOptJSONArray != null) {
                    int i2 = 0;
                    while (true) {
                        if (i2 < jSONArrayOptJSONArray.length()) {
                            if (!str2.equals(itemId(jSONArrayOptJSONArray.optJSONObject(i2)))) {
                                i2++;
                            } else {
                                z = true;
                                break;
                            }
                        } else {
                            z = false;
                            break;
                        }
                    }
                } else {
                    z = false;
                    break;
                }
                arrayList.add((z ? "\u2705  " : "\u2b1c  ") + strOptString);
            }
        }
        new AlertDialog.Builder(activity).setTitle(Main.isRussian() ? ("\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0432 \u043f\u0430\u043f\u043a\u0443 " + mediaLabel(str)) : (Main.isRussian() ? ("\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0432 \u043f\u0430\u043f\u043a\u0443 " + mediaLabel(str)) : ("Add to " + mediaLabel(str) + " folder"))).setItems((CharSequence[]) arrayList.toArray(new String[0]), new DialogInterface.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.13
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i3) {
                Main.lambda$showAddToFolderDialog$4(str, str2, dialogInterface, i3);
            }
        }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.14
            public final void onClick(AlertDialog alertDialog, int i3) {
                alertDialog.dismiss();
            }
        }).show();
    }

    static /* synthetic */ void lambda$showAddToFolderDialog$4(final String str, String str2, DialogInterface dialogInterface, int i) {
        JSONObject jSONObjectOptJSONObject;
        boolean z;
        dialogInterface.dismiss();
        JSONArray jSONArrayFolders = folders(str);
        if (i < jSONArrayFolders.length() && (jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i)) != null) {
            JSONArray jSONArrayOptJSONArray = jSONObjectOptJSONObject.optJSONArray("items");
            if (jSONArrayOptJSONArray == null) {
                jSONArrayOptJSONArray = new JSONArray();
                try {
                    jSONObjectOptJSONObject.put("items", jSONArrayOptJSONArray);
                } catch (Throwable th) {
                }
            }
            int i2 = 0;
            while (true) {
                if (i2 < jSONArrayOptJSONArray.length()) {
                    if (!str2.equals(itemId(jSONArrayOptJSONArray.optJSONObject(i2)))) {
                        i2++;
                    } else {
                        z = true;
                        break;
                    }
                } else {
                    z = false;
                    break;
                }
            }
            if (z) {
                JSONArray jSONArray = new JSONArray();
                for (int i3 = 0; i3 < jSONArrayOptJSONArray.length(); i3++) {
                    if (!str2.equals(itemId(jSONArrayOptJSONArray.optJSONObject(i3)))) {
                        jSONArray.put(jSONArrayOptJSONArray.optJSONObject(i3));
                    }
                }
                try {
                    jSONObjectOptJSONObject.put("items", jSONArray);
                } catch (Throwable th2) {
                }
                saveFolders(str, jSONArrayFolders);
                bulletinSuccess(isRussian() ? ("\u0423\u0434\u0430\u043b\u0435\u043d\u043e \u0438\u0437 \"" + jSONObjectOptJSONObject.optString("name") + "\"") : ("Removed from \"" + jSONObjectOptJSONObject.optString("name") + "\""));
            } else {
                JSONObject jSONObject = new JSONObject();
                try {
                    jSONObject.put("id", str2);
                } catch (Throwable th3) {
                }
                jSONArrayOptJSONArray.put(jSONObject);
                saveFolders(str, jSONArrayFolders);
                bulletinSuccess(isRussian() ? ("\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u043e \u0432 \"" + jSONObjectOptJSONObject.optString("name") + "\"") : ("Added to \"" + jSONObjectOptJSONObject.optString("name") + "\""));
            }
            ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.26
                @Override // java.lang.Runnable
                public final void run() {
                    Main.refreshBarsForType(str);
                }
            }, 100L);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void showFolderManageDialog(final String str, final String str2, final String str3) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        ArrayList<String> arrayListOrderedTabs = orderedTabs(str);
        int iIndexOf = arrayListOrderedTabs.indexOf(str2);
        ArrayList arrayList = new ArrayList();
        final ArrayList arrayList2 = new ArrayList();
        if (iIndexOf > 0) {
            arrayList.add(Main.tr("\u2b06\ufe0f  \u041f\u043e\u0434\u043d\u044f\u0442\u044c \u0432\u044b\u0448\u0435", "\u2b06\ufe0f  Move up"));
            arrayList2.add("up");
        }
        if (iIndexOf >= 0 && iIndexOf < arrayListOrderedTabs.size() - 1) {
            arrayList.add(Main.tr("\u2b07\ufe0f  \u041e\u043f\u0443\u0441\u0442\u0438\u0442\u044c \u043d\u0438\u0436\u0435", "\u2b07\ufe0f  Move down"));
            arrayList2.add("down");
        }
        arrayList.add(Main.tr("\u270f\ufe0f  \u041f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u0442\u044c", "\u270f\ufe0f  Rename"));
        arrayList2.add("rename");
        if ("all".equals(str2)) {
            if (getAllTabHidden(str)) {
                arrayList.add(Main.tr("\ud83d\udc41  \u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0432\u043a\u043b\u0430\u0434\u043a\u0443", "\ud83d\udc41  Show tab"));
                arrayList2.add("show");
            } else {
                arrayList.add(Main.tr("\ud83d\udeab  \u0421\u043a\u0440\u044b\u0442\u044c \u0432\u043a\u043b\u0430\u0434\u043a\u0443", "\ud83d\udeab  Hide tab"));
                arrayList2.add("hide");
            }
        } else {
            arrayList.add(Main.tr("\ud83d\uddd1  \u0423\u0434\u0430\u043b\u0438\u0442\u044c", "\ud83d\uddd1  Delete"));
            arrayList2.add("delete");
        }
        new AlertDialog.Builder(activity).setTitle(str3).setItems((CharSequence[]) arrayList.toArray(new String[0]), new DialogInterface.OnClickListener() { // from class: com.maxplugins.mediafolders.Main.9
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                Main.lambda$showFolderManageDialog$6(arrayList2, str, str2, str3, dialogInterface, i);
            }
        }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.10
            public final void onClick(AlertDialog alertDialog, int i) {
                alertDialog.dismiss();
            }
        }).show();
    }

    static /* synthetic */ void lambda$showFolderManageDialog$6(List list, String str, String str2, String str3, DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
        String str4 = (String) list.get(i);
        if ("up".equals(str4)) {
            moveFolder(str, str2, -1);
            return;
        }
        if ("down".equals(str4)) {
            moveFolder(str, str2, 1);
            return;
        }
        if ("rename".equals(str4)) {
            if ("all".equals(str2)) {
                showRenameAllDialog(str, str3);
                return;
            } else {
                showRenameFolderDialog(str, str2, str3);
                return;
            }
        }
        if ("hide".equals(str4)) {
            setAllTabHidden(str, true);
            refreshBarsForType(str);
            bulletinSuccess(tr("\u0412\u043a\u043b\u0430\u0434\u043a\u0430 \u0441\u043a\u0440\u044b\u0442\u0430", "Tab hidden"));
        } else if ("show".equals(str4)) {
            setAllTabHidden(str, false);
            refreshBarsForType(str);
            bulletinSuccess(tr("\u0412\u043a\u043b\u0430\u0434\u043a\u0430 \u043e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0435\u0442\u0441\u044f", "Tab shown"));
        } else if ("delete".equals(str4)) {
            confirmDeleteFolder(str, str2, str3);
        }
    }

    static void moveFolder(String str, String str2, int i) {
        int i2;
        ArrayList<String> arrayListOrderedTabs = orderedTabs(str);
        int iIndexOf = arrayListOrderedTabs.indexOf(str2);
        if (iIndexOf < 0 || (i2 = i + iIndexOf) < 0 || i2 >= arrayListOrderedTabs.size()) {
            return;
        }
        String str3 = arrayListOrderedTabs.get(iIndexOf);
        arrayListOrderedTabs.set(iIndexOf, arrayListOrderedTabs.get(i2));
        arrayListOrderedTabs.set(i2, str3);
        saveTabOrder(str, new JSONArray((Collection) arrayListOrderedTabs));
        refreshBarsForType(str);
        bulletinSuccess(tr("\u041f\u043e\u0440\u044f\u0434\u043e\u043a \u043e\u0431\u043d\u043e\u0432\u043b\u0451\u043d", "Order updated"));
    }

    static void showRenameAllDialog(final String str, String str2) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        final EditText editText = new EditText(activity);
        editText.setText(str2);
        editText.setTextSize(1, 16.0f);
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        editText.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        editText.setPadding(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(12.0f), AndroidUtilities.dp(24.0f), AndroidUtilities.dp(12.0f));
        editText.setSingleLine(true);
        editText.setSelection(str2.length());
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(0);
        gradientDrawable.setCornerRadius(AndroidUtilities.dp(12.0f));
        gradientDrawable.setColor(Theme.getColor(Theme.key_dialogBackground));
        gradientDrawable.setStroke(AndroidUtilities.dp(1.0f), Theme.getColor(Theme.key_divider));
        editText.setBackground(gradientDrawable);
        FrameLayout frameLayout = new FrameLayout(activity);
        frameLayout.setPadding(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(24.0f), AndroidUtilities.dp(4.0f));
        frameLayout.addView(editText, new FrameLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity).setTitle(Main.tr("\u041f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u0442\u044c \u0432\u043a\u043b\u0430\u0434\u043a\u0443", "Rename Tab")).setView(frameLayout).setPositiveButton(Main.tr("\u041f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u0442\u044c", "Rename"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.27
            public final void onClick(AlertDialog alertDialog, int i) {
                Main.lambda$showRenameAllDialog$8(editText, str, alertDialog, i);
            }
        }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.28
            public final void onClick(AlertDialog alertDialog, int i) {
                alertDialog.dismiss();
            }
        }).show();
    }

    static /* synthetic */ void lambda$showRenameAllDialog$8(EditText editText, String str, AlertDialog alertDialog, int i) {
        String strTrim = String.valueOf(editText.getText()).trim();
        if (!strTrim.isEmpty()) {
            setAllTabName(str, strTrim);
            refreshBarsForType(str);
            bulletinSuccess(isRussian() ? ("\u041f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u043d\u043e \u0432 \"" + strTrim + "\"") : ("Renamed to \"" + strTrim + "\""));
        }
        alertDialog.dismiss();
    }

    static void showRenameFolderDialog(final String str, final String str2, String str3) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        final EditText editText = new EditText(activity);
        editText.setText(str3);
        editText.setTextSize(1, 16.0f);
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        editText.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        editText.setPadding(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(12.0f), AndroidUtilities.dp(24.0f), AndroidUtilities.dp(12.0f));
        editText.setSingleLine(true);
        editText.setSelection(str3.length());
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(0);
        gradientDrawable.setCornerRadius(AndroidUtilities.dp(12.0f));
        gradientDrawable.setColor(Theme.getColor(Theme.key_dialogBackground));
        gradientDrawable.setStroke(AndroidUtilities.dp(1.0f), Theme.getColor(Theme.key_divider));
        editText.setBackground(gradientDrawable);
        FrameLayout frameLayout = new FrameLayout(activity);
        frameLayout.setPadding(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(24.0f), AndroidUtilities.dp(4.0f));
        frameLayout.addView(editText, new FrameLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(activity).setTitle(Main.tr("\u041f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u0442\u044c \u043f\u0430\u043f\u043a\u0443", "Rename Folder")).setView(frameLayout).setPositiveButton(Main.tr("\u041f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u0442\u044c", "Rename"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.29
            public final void onClick(AlertDialog alertDialog, int i) {
                Main.lambda$showRenameFolderDialog$10(str, str2, editText, alertDialog, i);
            }
        }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.30
            public final void onClick(AlertDialog alertDialog, int i) {
                alertDialog.dismiss();
            }
        }).show();
    }

    static /* synthetic */ void lambda$showRenameFolderDialog$10(String str, String str2, EditText editText, AlertDialog alertDialog, int i) {
        renameFolder(str, str2, String.valueOf(editText.getText()).trim());
        alertDialog.dismiss();
    }

    static void renameFolder(String str, String str2, String str3) {
        if (str3 == null || str3.isEmpty()) {
            return;
        }
        JSONArray jSONArrayFolders = folders(str);
        for (int i = 0; i < jSONArrayFolders.length(); i++) {
            JSONObject jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i);
            if (jSONObjectOptJSONObject != null && str2.equals(jSONObjectOptJSONObject.optString("id"))) {
                try {
                    jSONObjectOptJSONObject.put("name", str3);
                    break;
                } catch (Throwable th) {
                }
            }
        }
        saveFolders(str, jSONArrayFolders);
        refreshBarsForType(str);
        bulletinSuccess(isRussian() ? ("\u041f\u0430\u043f\u043a\u0430 \u043f\u0435\u0440\u0435\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u043d\u0430 \u0432 \"" + str3 + "\"") : ("Folder renamed to \"" + str3 + "\""));
    }

    static void confirmDeleteFolder(final String str, final String str2, final String str3) {
        String str4;
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (G.equals(str)) {
            str4 = "Are you sure you want to delete \"" + str3 + "\"?\n\nYour saved GIFs will not be removed from Telegram.";
        } else if (S.equals(str)) {
            str4 = "Are you sure you want to delete \"" + str3 + "\"?\n\nYour sticker packs will not be removed from Telegram.";
        } else {
            str4 = "Are you sure you want to delete \"" + str3 + "\"?\n\nYour emoji packs will not be removed from Telegram.";
        }
        new AlertDialog.Builder(activity).setTitle(Main.tr("\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u043f\u0430\u043f\u043a\u0443", "Delete Folder")).setMessage(str4).setPositiveButton(Main.tr("\u0423\u0434\u0430\u043b\u0438\u0442\u044c", "Delete"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.38
            public final void onClick(AlertDialog alertDialog, int i) {
                Main.lambda$confirmDeleteFolder$12(str, str2, str3, alertDialog, i);
            }
        }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.39
            public final void onClick(AlertDialog alertDialog, int i) {
                alertDialog.dismiss();
            }
        }).show();
    }

    static /* synthetic */ void lambda$confirmDeleteFolder$12(String str, String str2, String str3, AlertDialog alertDialog, int i) {
        deleteFolder(str, str2, str3);
        alertDialog.dismiss();
    }

    static void deleteFolder(String str, String str2, String str3) {
        JSONArray jSONArrayFolders = folders(str);
        JSONArray jSONArray = new JSONArray();
        for (int i = 0; i < jSONArrayFolders.length(); i++) {
            JSONObject jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i);
            if (jSONObjectOptJSONObject != null && !str2.equals(jSONObjectOptJSONObject.optString("id"))) {
                jSONArray.put(jSONObjectOptJSONObject);
            }
        }
        saveFolders(str, jSONArray);
        ArrayList<String> arrayListOrderedTabs = orderedTabs(str);
        ArrayList arrayList = new ArrayList();
        for (String str4 : arrayListOrderedTabs) {
            if (!str2.equals(str4)) {
                arrayList.add(str4);
            }
        }
        saveTabOrder(str, new JSONArray((Collection) arrayList));
        if (str2.equals(activeFolderId(str))) {
            setActiveFolderId(str, "all");
        }
        for (FolderBarManager folderBarManager : managersForType(str).values()) {
            if (str2.equals(folderBarManager.activeFolderId)) {
                folderBarManager.activeFolderId = "all";
            }
        }
        refreshBarsForType(str);
        bulletinSuccess(isRussian() ? ("\u041f\u0430\u043f\u043a\u0430 \"" + str3 + "\" \u0443\u0434\u0430\u043b\u0435\u043d\u0430") : ("Folder \"" + str3 + "\" deleted"));
    }

    static Map<Object, FolderBarManager> managersForType(String str) {
        if (G.equals(str)) {
            return GIF_MANAGERS;
        }
        return S.equals(str) ? STICKER_MANAGERS : EMOJI_MANAGERS;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void refreshBarsForType(String str) {
        for (FolderBarManager folderBarManager : managersForType(str).values()) {
            folderBarManager.rebuildTabs();
            folderBarManager.applyFolderFilter();
        }
    }

    static void refreshAllBars() {
        refreshBarsForType(G);
        refreshBarsForType(S);
        refreshBarsForType(E);
        applyTrendingVisibility();
    }

    static void applyTrendingVisibility() {
        boolean zHideTrending = hideTrending();
        Iterator<FolderBarManager> it = GIF_MANAGERS.values().iterator();
        while (it.hasNext()) {
            try {
                Object objGf = gf(it.next().emojiView, "gifTabs");
                if (objGf instanceof View) {
                    int iDp = foldersEnabled(G) ? AndroidUtilities.dp(44.0f) : 0;
                    if (zHideTrending) {
                        ((View) objGf).setVisibility(8);
                    } else {
                        ((View) objGf).setVisibility(0);
                        ((View) objGf).setTranslationY(iDp);
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void doBackup() {
        int i;
        String str;
        String str2 = "name";
        String str3 = "";
        String str4 = "id";
        String str5 = E;
        String str6 = S;
        String str7 = G;
        try {
            long selfUserId = getSelfUserId();
            if (selfUserId == 0) {
                bulletinError(tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u043e\u043b\u0443\u0447\u0438\u0442\u044c ID \u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044f", "Failed to get user ID"));
                return;
            }
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("type", "media_folders_backup");
            jSONObject.put("version", 1);
            jSONObject.put("user_id", selfUserId);
            jSONObject.put("timestamp", System.currentTimeMillis() / 1000);
            jSONObject.put("hide_trending", hideTrending());
            int length = 0;
            String[] strArr = {G, S, E};
            int i2 = 0;
            int i3 = 0;
            for (int i4 = 3; i2 < i4; i4 = 3) {
                String str8 = strArr[i2];
                String str9 = str5;
                JSONArray jSONArrayFolders = folders(str8);
                String str10 = str6;
                JSONArray jSONArray = new JSONArray();
                String str11 = str7;
                int i5 = 0;
                while (true) {
                    i = i2;
                    if (i5 < jSONArrayFolders.length()) {
                        JSONObject jSONObjectOptJSONObject = jSONArrayFolders.optJSONObject(i5);
                        if (jSONObjectOptJSONObject == null) {
                            str = str4;
                        } else {
                            JSONObject jSONObject2 = new JSONObject();
                            jSONObject2.put(str4, jSONObjectOptJSONObject.optString(str4, str3));
                            jSONObject2.put(str2, jSONObjectOptJSONObject.optString(str2, str3));
                            JSONArray jSONArray2 = new JSONArray();
                            JSONArray jSONArrayOptJSONArray = jSONObjectOptJSONObject.optJSONArray("items");
                            if (jSONArrayOptJSONArray == null) {
                                jSONArrayOptJSONArray = new JSONArray();
                            }
                            int i6 = 0;
                            while (true) {
                                str = str4;
                                if (i6 >= jSONArrayOptJSONArray.length()) {
                                    break;
                                }
                                jSONArray2.put(itemId(jSONArrayOptJSONArray.optJSONObject(i6)));
                                i6++;
                                str4 = str;
                            }
                            jSONObject2.put("item_ids", jSONArray2);
                            JSONArray jSONArrayOptJSONArray2 = jSONObjectOptJSONObject.optJSONArray("packs");
                            if (jSONArrayOptJSONArray2 == null) {
                                jSONArrayOptJSONArray2 = new JSONArray();
                            }
                            jSONObject2.put("packs", jSONArrayOptJSONArray2);
                            jSONArray.put(jSONObject2);
                            i3++;
                            length += jSONArray2.length();
                        }
                        i5++;
                        i2 = i;
                        jSONArrayFolders = jSONArrayFolders;
                        str2 = str2;
                        str3 = str3;
                        str4 = str;
                    } else {
                        break;
                    }
                }
                jSONObject.put(str8 + "_folders", jSONArray);
                jSONObject.put(str8 + "_tab_order", new JSONArray((Collection) orderedTabs(str8)));
                i2 = i + 1;
                str5 = str9;
                str6 = str10;
                str7 = str11;
                str2 = str2;
                str3 = str3;
                str4 = str4;
            }
            jSONObject.put("folders_enabled_gif", foldersEnabled(str7));
            jSONObject.put("folders_enabled_sticker", foldersEnabled(str6));
            jSONObject.put("folders_enabled_emoji", foldersEnabled(str5));
            File file = getBackupDir();
            File file2 = new File(file, "media_folders.mediafolder_backup");
            writeText(file2, jSONObject.toString());
            String caption = isRussian() ? ("\ud83d\udcbe \u0420\u0435\u0437\u0435\u0440\u0432\u043d\u0430\u044f \u043a\u043e\u043f\u0438\u044f Media Folders\n\ud83d\udcc1 " + i3 + " \u043f\u0430\u043f\u043e\u043a \u00b7 " + length + " \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u043e\u0432\n\ud83d\udd52 " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date())) : ("\ud83d\udcbe Media Folders Backup\n\ud83d\udcc1 " + i3 + " folders \u00b7 " + length + " items\n\ud83d\udd52 " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date()));
            sendBackupDocument(file2, caption, selfUserId);
            bulletinSuccess(tr("\u0411\u044d\u043a\u0430\u043f \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d \u0432 \u0418\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0435", "Backup sent to Saved Messages"));
        } catch (Throwable th) {
            err(th, "backup");
            bulletinError(tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0437\u0434\u0430\u0442\u044c \u0431\u044d\u043a\u0430\u043f", "Failed to create backup"));
        }
    }

    static File getBackupDir() {
        File dir = null;
        try {
            dir = ApplicationLoader.applicationContext.getExternalCacheDir();
        } catch (Throwable t) {}
        if (dir == null) {
            try {
                dir = AndroidUtilities.getCacheDir();
            } catch (Throwable t) {}
        }
        if (dir == null) {
            try {
                dir = FileLoader.getDirectory(4);
            } catch (Throwable t) {}
        }
        if (dir == null) {
            dir = ApplicationLoader.applicationContext.getCacheDir();
        }
        File backupDir = new File(dir, "media_folders_backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        return backupDir;
    }

    static void downloadBackup(FileLoader fileLoader, TLRPC.Document document) {
        try {
            fileLoader.loadFile(document, null, 1, 0);
        } catch (Throwable th) {
            err(th, "downloadBackup");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void handleBackupFile(MessageObject messageObject) {
        try {
            TLRPC.Document document = messageObject.getDocument();
            if (document == null) {
                return;
            }
            FileLoader fileLoader = FileLoader.getInstance(UserConfig.selectedAccount);
            File pathToAttach = fileLoader.getPathToAttach(document);
            if (pathToAttach == null || !pathToAttach.exists()) {
                try {
                    if (messageObject.messageOwner != null && messageObject.messageOwner.attachPath != null) {
                        File f = new File(messageObject.messageOwner.attachPath);
                        if (f.exists()) {
                            pathToAttach = f;
                        }
                    }
                } catch (Throwable th) {}
            }
            if (pathToAttach == null || !pathToAttach.exists()) {
                try {
                    pathToAttach = fileLoader.getPathToMessage(messageObject.messageOwner);
                } catch (Throwable th) {
                    pathToAttach = null;
                }
            }
            if (pathToAttach != null && pathToAttach.exists()) {
                String text = readText(pathToAttach);
                if (text == null) {
                    bulletinError(tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u0444\u0430\u0439\u043b \u0431\u044d\u043a\u0430\u043f\u0430", "Failed to read backup file"));
                    return;
                }
                try {
                    JSONObject jSONObject = new JSONObject(text);
                    String strOptString = jSONObject.optString("type", "");
                    long selfUserId = getSelfUserId();
                    if ("media_folders_backup".equals(strOptString)) {
                        if (jSONObject.optLong("user_id", 0L) != selfUserId) {
                            bulletinError(tr("\u042d\u0442\u043e\u0442 \u0431\u044d\u043a\u0430\u043f \u043f\u0440\u0438\u043d\u0430\u0434\u043b\u0435\u0436\u0438\u0442 \u0434\u0440\u0443\u0433\u043e\u043c\u0443 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u0443", "This backup belongs to a different account"));
                            return;
                        }
                        showRestoreDialog(jSONObject);
                    } else if (!"gif_folders_backup".equals(strOptString)) {
                        bulletinError(tr("\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 \u0444\u0430\u0439\u043b \u0431\u044d\u043a\u0430\u043f\u0430", "Invalid backup file"));
                    } else {
                        if (jSONObject.optLong("user_id", 0L) != selfUserId) {
                            bulletinError(tr("\u042d\u0442\u043e\u0442 \u0431\u044d\u043a\u0430\u043f \u043f\u0440\u0438\u043d\u0430\u0434\u043b\u0435\u0436\u0438\u0442 \u0434\u0440\u0443\u0433\u043e\u043c\u0443 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u0443", "This backup belongs to a different account"));
                            return;
                        }
                        showRestoreGifFoldersDialog(jSONObject);
                    }
                    return;
                } catch (Throwable th2) {
                    bulletinError(tr("\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 \u0444\u0430\u0439\u043b \u0431\u044d\u043a\u0430\u043f\u0430", "Invalid backup file"));
                    return;
                }
            }
            downloadBackup(fileLoader, document);
            bulletinInfo(tr("\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0431\u044d\u043a\u0430\u043f\u0430... \u043f\u043e\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 \u0447\u0435\u0440\u0435\u0437 \u043f\u0430\u0440\u0443 \u0441\u0435\u043a\u0443\u043d\u0434", "Downloading backup... try again in a moment"));
        } catch (Throwable th3) {
            err(th3, "handleBackupFile");
            bulletinError(tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0444\u0430\u0439\u043b \u0431\u044d\u043a\u0430\u043f\u0430", "Failed to open backup file"));
        }
    }

    static void handleBackupFromFile(File file) {
        try {
            if (file == null || !file.exists()) {
                bulletinError(tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u0444\u0430\u0439\u043b \u0431\u044d\u043a\u0430\u043f\u0430", "Failed to read backup file"));
                return;
            }
            String text = readText(file);
            if (text == null) {
                bulletinError(tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u0444\u0430\u0439\u043b \u0431\u044d\u043a\u0430\u043f\u0430", "Failed to read backup file"));
                return;
            }
            JSONObject jSONObject = new JSONObject(text);
            String strOptString = jSONObject.optString("type", "");
            long selfUserId = getSelfUserId();
            if ("media_folders_backup".equals(strOptString)) {
                if (jSONObject.optLong("user_id", 0L) != selfUserId) {
                    bulletinError(tr("\u042d\u0442\u043e\u0442 \u0431\u044d\u043a\u0430\u043f \u043f\u0440\u0438\u043d\u0430\u0434\u043b\u0435\u0436\u0438\u0442 \u0434\u0440\u0443\u0433\u043e\u043c\u0443 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u0443", "This backup belongs to a different account"));
                    return;
                }
                showRestoreDialog(jSONObject);
            } else if ("gif_folders_backup".equals(strOptString)) {
                if (jSONObject.optLong("user_id", 0L) != selfUserId) {
                    bulletinError(tr("\u042d\u0442\u043e\u0442 \u0431\u044d\u043a\u0430\u043f \u043f\u0440\u0438\u043d\u0430\u0434\u043b\u0435\u0436\u0438\u0442 \u0434\u0440\u0443\u0433\u043e\u043c\u0443 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u0443", "This backup belongs to a different account"));
                    return;
                }
                showRestoreGifFoldersDialog(jSONObject);
            } else {
                bulletinError(tr("\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 \u0444\u0430\u0439\u043b \u0431\u044d\u043a\u0430\u043f\u0430", "Invalid backup file"));
            }
        } catch (Throwable t) {
            err(t, "handleBackupFromFile");
            bulletinError(tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0444\u0430\u0439\u043b \u0431\u044d\u043a\u0430\u043f\u0430", "Failed to open backup file"));
        }
    }

    static void writeText(File file, String str) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(str.getBytes("UTF-8"));
            fileOutputStream.close();
        } catch (Throwable th) {
            err(th, "writeText");
        }
    }

    static String readText(File file) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = bufferedReader.readLine();
                if (line != null) {
                    sb.append(line).append("\n");
                } else {
                    bufferedReader.close();
                    return sb.toString();
                }
            }
        } catch (Throwable th) {
            err(th, "readText");
            return null;
        }
    }

    static void showRestoreGifFoldersDialog(final JSONObject jSONObject) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        JSONArray jSONArrayOptJSONArray = jSONObject.optJSONArray("folders");
        if (jSONArrayOptJSONArray == null) {
            jSONArrayOptJSONArray = new JSONArray();
        }
        int length = 0;
        for (int i = 0; i < jSONArrayOptJSONArray.length(); i++) {
            JSONObject jSONObjectOptJSONObject = jSONArrayOptJSONArray.optJSONObject(i);
            if (jSONObjectOptJSONObject != null) {
                JSONArray jSONArrayOptJSONArray2 = jSONObjectOptJSONObject.optJSONArray("gif_ids");
                if (jSONArrayOptJSONArray2 == null) {
                    JSONArray jSONArrayOptJSONArray3 = jSONObjectOptJSONObject.optJSONArray("gifs");
                    if (jSONArrayOptJSONArray3 != null) {
                        length += jSONArrayOptJSONArray3.length();
                    }
                } else {
                    length += jSONArrayOptJSONArray2.length();
                }
            }
        }
        long jOptLong = jSONObject.optLong("timestamp", 0L);
        new AlertDialog.Builder(activity).setTitle(Main.tr("\u0412\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435 \u043f\u0430\u043f\u043e\u043a GIF", "Restore GIF Folders")).setMessage(isRussian() ? ("\u0420\u0435\u0437\u0435\u0440\u0432\u043d\u0430\u044f \u043a\u043e\u043f\u0438\u044f \u043f\u0430\u043f\u043e\u043a GIF \u043e\u0442 " + (jOptLong > 0 ? new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(jOptLong * 1000)) : "\u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u043e") + "\n\n\ud83d\udcc1 " + jSONArrayOptJSONArray.length() + " \u043f\u0430\u043f\u043e\u043a GIF \u00b7 " + length + " GIF\n\n\u042d\u0442\u043e \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435 \u0437\u0430\u043c\u0435\u043d\u0438\u0442 \u0432\u0430\u0448\u0438 \u0442\u0435\u043a\u0443\u0449\u0438\u0435 \u043f\u0430\u043f\u043a\u0438 GIF.") : ("GIF Folders backup from " + (jOptLong > 0 ? new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(jOptLong * 1000)) : "unknown") + "\n\n\ud83d\udcc1 " + jSONArrayOptJSONArray.length() + " GIF folders \u00b7 " + length + " GIFs\n\nThis will replace your current GIF folders.")).setPositiveButton(Main.tr("\u0412\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c", "Restore"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.11
            public final void onClick(AlertDialog alertDialog, int i2) {
                Main.lambda$showRestoreGifFoldersDialog$14(jSONObject, alertDialog, i2);
            }
        }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.12
            public final void onClick(AlertDialog alertDialog, int i2) {
                alertDialog.dismiss();
            }
        }).show();
    }

    static /* synthetic */ void lambda$showRestoreGifFoldersDialog$14(JSONObject jSONObject, AlertDialog alertDialog, int i) {
        doRestoreGifFolders(jSONObject);
        alertDialog.dismiss();
    }

    static void doRestoreGifFolders(JSONObject jSONObject) {
        try {
            JSONArray jSONArrayOptJSONArray = jSONObject.optJSONArray("folders");
            if (jSONArrayOptJSONArray == null) {
                jSONArrayOptJSONArray = new JSONArray();
            }
            JSONArray jSONArray = new JSONArray();
            for (int i = 0; i < jSONArrayOptJSONArray.length(); i++) {
                JSONObject jSONObjectOptJSONObject = jSONArrayOptJSONArray.optJSONObject(i);
                if (jSONObjectOptJSONObject != null) {
                    ArrayList<String> arrayList = new ArrayList();
                    JSONArray jSONArrayOptJSONArray2 = jSONObjectOptJSONObject.optJSONArray("gif_ids");
                    if (jSONArrayOptJSONArray2 != null) {
                        for (int i2 = 0; i2 < jSONArrayOptJSONArray2.length(); i2++) {
                            arrayList.add(jSONArrayOptJSONArray2.optString(i2));
                        }
                    } else {
                        JSONArray jSONArrayOptJSONArray3 = jSONObjectOptJSONObject.optJSONArray("gifs");
                        if (jSONArrayOptJSONArray3 != null) {
                            for (int i3 = 0; i3 < jSONArrayOptJSONArray3.length(); i3++) {
                                JSONObject jSONObjectOptJSONObject2 = jSONArrayOptJSONArray3.optJSONObject(i3);
                                arrayList.add(jSONObjectOptJSONObject2 != null ? jSONObjectOptJSONObject2.optString("id") : jSONArrayOptJSONArray3.optString(i3));
                            }
                        }
                    }
                    JSONArray jSONArray2 = new JSONArray();
                    for (String str : arrayList) {
                        if (str != null && !str.isEmpty()) {
                            JSONObject jSONObject2 = new JSONObject();
                            jSONObject2.put("id", str);
                            jSONArray2.put(jSONObject2);
                        }
                    }
                    JSONObject jSONObject3 = new JSONObject();
                    jSONObject3.put("id", jSONObjectOptJSONObject.optString("id", "gif_folder_" + System.currentTimeMillis()));
                    jSONObject3.put("name", jSONObjectOptJSONObject.optString("name", "Folder"));
                    jSONObject3.put("items", jSONArray2);
                    jSONObject3.put("packs", new JSONArray());
                    jSONArray.put(jSONObject3);
                }
            }
            saveFolders(G, jSONArray);
            saveTabOrder(G, jSONObject.optJSONArray("tab_order"));
            if (jSONObject.has("hide_trending")) {
                setHideTrending(jSONObject.optBoolean("hide_trending", false));
            }
            setActiveFolderId(G, "all");
            for (FolderBarManager folderBarManager : GIF_MANAGERS.values()) {
                folderBarManager.activeFolderId = "all";
                folderBarManager.rebuildTabs();
                folderBarManager.applyFolderFilter();
            }
            bulletinSuccess(isRussian() ? ("\u041f\u0430\u043f\u043a\u0438 GIF \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u044b: " + jSONArray.length() + " \u043f\u0430\u043f\u043e\u043a") : ("GIF Folders restored: " + jSONArray.length() + " folders"));
        } catch (Throwable th) {
            err(th, "restore gif folders");
            bulletinError(tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u0431\u044d\u043a\u0430\u043f \u043f\u0430\u043f\u043e\u043a GIF", "Failed to restore GIF Folders backup"));
        }
    }

    static void showRestoreDialog(final JSONObject jSONObject) {
        JSONArray jSONArrayOptJSONArray;
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        String[] strArr = {G, S, E};
        int length = 0;
        int length2 = 0;
        for (int i = 0; i < 3; i++) {
            JSONArray jSONArrayOptJSONArray2 = jSONObject.optJSONArray(strArr[i] + "_folders");
            if (jSONArrayOptJSONArray2 != null) {
                length += jSONArrayOptJSONArray2.length();
                for (int i2 = 0; i2 < jSONArrayOptJSONArray2.length(); i2++) {
                    JSONObject jSONObjectOptJSONObject = jSONArrayOptJSONArray2.optJSONObject(i2);
                    if (jSONObjectOptJSONObject != null && (jSONArrayOptJSONArray = jSONObjectOptJSONObject.optJSONArray("item_ids")) != null) {
                        length2 += jSONArrayOptJSONArray.length();
                    }
                }
            }
        }
        long jOptLong = jSONObject.optLong("timestamp", 0L);
        new AlertDialog.Builder(activity).setTitle(Main.tr("\u0412\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435 \u043f\u0430\u043f\u043e\u043a \u043c\u0435\u0434\u0438\u0430", "Restore Media Folders")).setMessage(isRussian() ? ("\u0420\u0435\u0437\u0435\u0440\u0432\u043d\u0430\u044f \u043a\u043e\u043f\u0438\u044f \u043e\u0442 " + (jOptLong > 0 ? new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(jOptLong * 1000)) : "\u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u043e") + "\n\n\ud83d\udcc1 " + length + " \u043f\u0430\u043f\u043e\u043a\n\ud83c\udfac " + length2 + " \u0441\u0441\u044b\u043b\u043e\u043a \u043d\u0430 \u043c\u0435\u0434\u0438\u0430\n\n\u042d\u0442\u043e \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435 \u0437\u0430\u043c\u0435\u043d\u0438\u0442 \u0432\u0430\u0448\u0438 \u0442\u0435\u043a\u0443\u0449\u0438\u0435 \u043f\u0430\u043f\u043a\u0438.") : ("Backup from " + (jOptLong > 0 ? new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(jOptLong * 1000)) : "unknown") + "\n\n\ud83d\udcc1 " + length + " folders\n\ud83c\udfac " + length2 + " item references\n\nThis will replace your current folders.")).setPositiveButton(Main.tr("\u0412\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c", "Restore"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.23
            public final void onClick(AlertDialog alertDialog, int i3) {
                Main.lambda$showRestoreDialog$16(jSONObject, alertDialog, i3);
            }
        }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.24
            public final void onClick(AlertDialog alertDialog, int i3) {
                alertDialog.dismiss();
            }
        }).show();
    }

    static /* synthetic */ void lambda$showRestoreDialog$16(JSONObject jSONObject, AlertDialog alertDialog, int i) {
        doRestore(jSONObject);
        alertDialog.dismiss();
    }

    static void doRestore(JSONObject jSONObject) {
        int i;
        String str;
        String str2 = "folders_enabled_emoji";
        String str3 = "folders_enabled_sticker";
        String str4 = "folders_enabled_gif";
        String str5 = E;
        String str6 = S;
        try {
            String[] strArr = {G, S, E};
            int i2 = 0;
            for (int i3 = 3; i2 < i3; i3 = 3) {
                String str7 = strArr[i2];
                JSONArray jSONArrayOptJSONArray = jSONObject.optJSONArray(str7 + "_folders");
                if (jSONArrayOptJSONArray == null) {
                    jSONArrayOptJSONArray = new JSONArray();
                }
                JSONArray jSONArray = new JSONArray();
                String[] strArr2 = strArr;
                int i4 = 0;
                while (true) {
                    i = i2;
                    if (i4 < jSONArrayOptJSONArray.length()) {
                        JSONObject jSONObjectOptJSONObject = jSONArrayOptJSONArray.optJSONObject(i4);
                        if (jSONObjectOptJSONObject == null) {
                            str2 = str2;
                            str = str6;
                        } else {
                            JSONArray jSONArrayOptJSONArray2 = jSONObjectOptJSONObject.optJSONArray("item_ids");
                            JSONArray jSONArray2 = new JSONArray();
                            if (jSONArrayOptJSONArray2 != null) {
                                int i5 = 0;
                                while (true) {
                                    str = str6;
                                    if (i5 >= jSONArrayOptJSONArray2.length()) {
                                        break;
                                    }
                                    JSONObject jSONObject2 = new JSONObject();
                                    jSONObject2.put("id", String.valueOf(jSONArrayOptJSONArray2.opt(i5)));
                                    jSONArray2.put(jSONObject2);
                                    i5++;
                                    str6 = str;
                                }
                            } else {
                                str = str6;
                            }
                            JSONObject jSONObject3 = new JSONObject();
                            jSONObject3.put("id", jSONObjectOptJSONObject.optString("id", str7 + "_folder_" + System.currentTimeMillis()));
                            jSONObject3.put("name", jSONObjectOptJSONObject.optString("name", "Folder"));
                            jSONObject3.put("items", jSONArray2);
                            JSONArray jSONArrayOptJSONArray3 = jSONObjectOptJSONObject.optJSONArray("packs");
                            if (jSONArrayOptJSONArray3 == null) {
                                jSONArrayOptJSONArray3 = new JSONArray();
                            }
                            jSONObject3.put("packs", jSONArrayOptJSONArray3);
                            jSONArray.put(jSONObject3);
                        }
                        str3 = str3;
                        i2 = i;
                        jSONArrayOptJSONArray = jSONArrayOptJSONArray;
                        str5 = str5;
                        str6 = str;
                        str4 = str4;
                        i4++;
                        str2 = str2;
                    } else {
                        break;
                    }
                }
                saveFolders(str7, jSONArray);
                saveTabOrder(str7, jSONObject.optJSONArray(str7 + "_tab_order"));
                setActiveFolderId(str7, "all");
                i2 = i + 1;
                str3 = str3;
                strArr = strArr2;
                str5 = str5;
                str2 = str2;
                str6 = str6;
                str4 = str4;
            }
            String str8 = str2;
            String str9 = str3;
            String str10 = str4;
            String str11 = str5;
            String str12 = str6;
            if (jSONObject.has("hide_trending")) {
                setHideTrending(jSONObject.optBoolean("hide_trending", false));
            }
            if (jSONObject.has(str10)) {
                setFoldersEnabled(G, jSONObject.optBoolean(str10, true));
            }
            if (jSONObject.has(str9)) {
                setFoldersEnabled(str12, jSONObject.optBoolean(str9, true));
            }
            if (jSONObject.has(str8)) {
                setFoldersEnabled(str11, jSONObject.optBoolean(str8, true));
            }
            Map[] mapArr = {GIF_MANAGERS, STICKER_MANAGERS, EMOJI_MANAGERS};
            for (int i6 = 0; i6 < 3; i6++) {
                for (Object objFbm : mapArr[i6].values()) { FolderBarManager folderBarManager = (FolderBarManager) objFbm;
                    folderBarManager.activeFolderId = "all";
                    folderBarManager.rebuildTabs();
                    folderBarManager.applyFolderFilter();
                }
            }
            bulletinSuccess(tr("\u0411\u044d\u043a\u0430\u043f \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d", "Backup restored successfully"));
        } catch (Throwable th) {
            err(th, "restore");
            bulletinError(tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u0431\u044d\u043a\u0430\u043f", "Failed to restore backup"));
        }
    }

    public static void pyOpenSettings() {
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.36
            @Override // java.lang.Runnable
            public final void run() {
                Main.lambda$pyOpenSettings$18();
            }
        });
    }

    static /* synthetic */ void lambda$pyOpenSettings$18() {
        BaseFragment lastFragment;
        try {
            Class<?> cls = Class.forName("com.exteragram.messenger.plugins.PluginsController");
            Object objInvoke = cls.getMethod("getPluginEngine", String.class).invoke(cls.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]), PLUGIN_ID);
            if (objInvoke == null || (lastFragment = LaunchActivity.getLastFragment()) == null) {
                return;
            }
            objInvoke.getClass().getMethod("openPluginSettings", String.class, BaseFragment.class).invoke(objInvoke, PLUGIN_ID, lastFragment);
        } catch (Throwable th) {
            err(th, "pyOpenSettings");
        }
    }

    public static void pyShowFolderManage(final String str, final String str2, final String str3) {
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.37
            @Override // java.lang.Runnable
            public final void run() {
                Main.showFolderManageDialog(str, str2, str3);
            }
        });
    }

    public static void pyNewFolder(final String str) {
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.40
            @Override // java.lang.Runnable
            public final void run() {
                Main.lambda$pyNewFolder$20(str);
            }
        });
    }

    static /* synthetic */ void lambda$pyNewFolder$20(String str) {
        new FolderBarManager(str, null).showCreateFolderDialog(str);
    }

    public static void pySetColor(final String str, final String str2) {
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.18
            @Override // java.lang.Runnable
            public final void run() {
                Main.lambda$pySetColor$21(str2, str);
            }
        });
    }

    static /* synthetic */ void lambda$pySetColor$21(String str, String str2) {
        String str3;
        String strTrim = str == null ? "" : str.trim();
        if (!strTrim.isEmpty() && parseColor(strTrim) == null) {
            if (str2 != null && str2.contains("text")) {
                str3 = "Use color format like #FFFFFF";
            } else {
                str3 = "Use color format like #2F80ED";
            }
            bulletinError(str3);
            return;
        }
        setColorSetting(str2, strTrim);
        refreshAllBars();
    }

    public static void pySetGlassEnabled(final boolean z) {
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.25
            @Override // java.lang.Runnable
            public final void run() {
                Main.lambda$pySetGlassEnabled$22(z);
            }
        });
    }

    static /* synthetic */ void lambda$pySetGlassEnabled$22(boolean z) {
        setGlassEnabled(z);
        refreshAllBars();
    }

    public static void pySetGlassTabs(final boolean z) {
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.35
            @Override // java.lang.Runnable
            public final void run() {
                Main.lambda$pySetGlassTabs$23(z);
            }
        });
    }

    static /* synthetic */ void lambda$pySetGlassTabs$23(boolean z) {
        setGlassTabs(z);
        refreshAllBars();
    }

    public static void pySetGlassAlpha(final double d) {
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.34
            @Override // java.lang.Runnable
            public final void run() {
                Main.lambda$pySetGlassAlpha$24(d);
            }
        });
    }

    static /* synthetic */ void lambda$pySetGlassAlpha$24(double d) {
        float f = (float) d;
        try {
            if (f < 0.1f || f > 1.0f) {
                bulletinError(tr("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0434\u043e\u043b\u0436\u043d\u0430 \u0431\u044b\u0442\u044c \u043e\u0442 0.1 \u0434\u043e 1.0", "Alpha must be between 0.1 and 1.0"));
            } else {
                setGlassAlpha(f);
                refreshAllBars();
            }
        } catch (Throwable th) {
            bulletinError(tr("\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0447\u0438\u0441\u043b\u043e \u043e\u0442 0.1 \u0434\u043e 1.0", "Enter a number between 0.1 and 1.0"));
        }
    }

    public static void pySetHideTrending(final boolean z) {
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.19
            @Override // java.lang.Runnable
            public final void run() {
                Main.lambda$pySetHideTrending$25(z);
            }
        });
    }

    static /* synthetic */ void lambda$pySetHideTrending$25(boolean z) {
        setHideTrending(z);
        applyTrendingVisibility();
        for (FolderBarManager folderBarManager : GIF_MANAGERS.values()) {
            if (z) {
                try {
                    Object objGf = gf(folderBarManager.emojiView, "gifAdapter");
                    if (objGf != null) {
                        folderBarManager.clearTrendingResults(objGf, folderBarManager.emojiView);
                    }
                } catch (Throwable th) {
                }
            }
            folderBarManager.applyFolderFilter();
        }
    }

    public static void pySetFoldersEnabled(final String str, final boolean z) {
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.32
            @Override // java.lang.Runnable
            public final void run() {
                Main.lambda$pySetFoldersEnabled$26(str, z);
            }
        });
    }

    static /* synthetic */ void lambda$pySetFoldersEnabled$26(String str, boolean z) {
        setFoldersEnabled(str, z);
        Iterator<FolderBarManager> it = managersForType(str).values().iterator();
        while (it.hasNext()) {
            it.next().injectFolderBar();
        }
    }

    public static void pySetCollapseOnScroll(final boolean z) {
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.16
            @Override // java.lang.Runnable
            public final void run() {
                Main.lambda$pySetCollapseOnScroll$27(z);
            }
        });
    }

    static /* synthetic */ void lambda$pySetCollapseOnScroll$27(boolean z) {
        setCollapseOnScroll(z);
        Map[] mapArr = {GIF_MANAGERS, STICKER_MANAGERS, EMOJI_MANAGERS};
        for (int i = 0; i < 3; i++) {
            for (Object objFbm : mapArr[i].values()) { FolderBarManager folderBarManager = (FolderBarManager) objFbm;
                if (!z) {
                    try {
                        folderBarManager.barCollapsed = false;
                    } catch (Throwable th) {
                    }
                }
                folderBarManager.injectFolderBar();
            }
        }
    }

    public static void pyDoBackup() {
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.33
            @Override // java.lang.Runnable
            public final void run() {
                Main.doBackup();
            }
        });
    }

    public static void pyResetAll() {
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.31
            @Override // java.lang.Runnable
            public final void run() {
                Main.lambda$pyResetAll$31();
            }
        });
    }

    static /* synthetic */ void lambda$pyResetAll$31() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        new AlertDialog.Builder(activity).setTitle(Main.tr("\u0421\u0431\u0440\u043e\u0441\u0438\u0442\u044c \u0432\u0441\u0435 \u043f\u0430\u043f\u043a\u0438", "Reset All Folders")).setMessage(tr("\u042d\u0442\u043e \u0443\u0434\u0430\u043b\u0438\u0442 \u0432\u0441\u0435 \u0432\u0430\u0448\u0438 \u043f\u0430\u043f\u043a\u0438 GIF, \u0441\u0442\u0438\u043a\u0435\u0440\u043e\u0432 \u0438 \u044d\u043c\u043e\u0434\u0437\u0438 \u0438 \u0441\u0431\u0440\u043e\u0441\u0438\u0442 \u043f\u043e\u0440\u044f\u0434\u043e\u043a \u0432\u043a\u043b\u0430\u0434\u043e\u043a. \u0412\u0430\u0448\u0438 \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d\u043d\u044b\u0435 \u043c\u0435\u0434\u0438\u0430 \u043d\u0435 \u0431\u0443\u0434\u0443\u0442 \u0443\u0434\u0430\u043b\u0435\u043d\u044b \u0438\u0437 Telegram.", "This will delete all your GIF, Sticker, and Emoji folders and reset tab order. Your saved media will not be removed from Telegram.")).setPositiveButton(Main.tr("\u0421\u0431\u0440\u043e\u0441\u0438\u0442\u044c", "Reset"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.21
            public final void onClick(AlertDialog alertDialog, int i) {
                Main.lambda$pyResetAll$29(alertDialog, i);
            }
        }).setNegativeButton(Main.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), new AlertDialog.OnButtonClickListener() { // from class: com.maxplugins.mediafolders.Main.22
            public final void onClick(AlertDialog alertDialog, int i) {
                alertDialog.dismiss();
            }
        }).show();
    }

    static /* synthetic */ void lambda$pyResetAll$29(AlertDialog alertDialog, int i) {
        doReset();
        alertDialog.dismiss();
    }

    static void doReset() {
        String[] strArr = {G, S, E};
        for (int i = 0; i < 3; i++) {
            String str = strArr[i];
            saveFolders(str, new JSONArray());
            saveTabOrder(str, null);
            setActiveFolderId(str, "all");
        }
        bulletinSuccess(tr("\u0412\u0441\u0435 \u043f\u0430\u043f\u043a\u0438 \u0441\u0431\u0440\u043e\u0448\u0435\u043d\u044b", "All folders have been reset"));
        ui(new Runnable() { // from class: com.maxplugins.mediafolders.Main.0
            @Override // java.lang.Runnable
            public final void run() {
                Main.lambda$doReset$32();
            }
        });
    }

    static /* synthetic */ void lambda$doReset$32() {
        Map[] mapArr = {GIF_MANAGERS, STICKER_MANAGERS, EMOJI_MANAGERS};
        for (int i = 0; i < 3; i++) {
            for (Object objFbm : mapArr[i].values()) { FolderBarManager folderBarManager = (FolderBarManager) objFbm;
                folderBarManager.activeFolderId = "all";
                folderBarManager.rebuildTabs();
                folderBarManager.applyFolderFilter();
            }
        }
    }
}
