__id__ = "media_folders"
__name__ = "Media Folders"
__description__ = "Adds folder tabs to GIF, Sticker, and Emoji panels for organizing your media into custom categories."
__author__ = "@itNotMax - @MaxExteraPlugins"
__version__ = "3.8.1"
__min_version__ = "12.10.6"
__app_version__ = ">=12.5.1"
__sdk_version__ = ">=1.4.3.3"
__icon__ = "MaxExteraPluginsStickers/20"

import base64
import json
import zlib

from java import jboolean, jclass, jfloat
from java.lang import String as JString

from android.content import Context

from org.telegram.messenger import ApplicationLoader

from base_plugin import BasePlugin
from ui.settings import Header, Text, Divider, Switch, Input
from ui.bulletin import BulletinHelper

PREFS_NAME = "media_folders_data"
GIF_FOLDERS_KEY = "gif_folders_json"
STICKER_FOLDERS_KEY = "sticker_folders_json"
EMOJI_FOLDERS_KEY = "emoji_folders_json"
GIF_TAB_ORDER_KEY = "gif_tab_order_json"
STICKER_TAB_ORDER_KEY = "sticker_tab_order_json"
EMOJI_TAB_ORDER_KEY = "emoji_tab_order_json"

ENTRY_CLASS = "com.maxplugins.mediafolders.Main"
DEX_BEGIN = "__DEX_BEGIN__"
DEX_END = "__DEX_END__"


class L:
    _T = {
        "info_header": {"ru": "Информация", "en": "Information"},
        "author_channel": {"ru": "Канал автора", "en": "Author's channel"},
        "documentation": {"ru": "Документация", "en": "Documentation"},

        "gif_folders_header": {"ru": "Папки GIF", "en": "GIF Folders"},
        "sticker_folders_header": {"ru": "Папки стикеров", "en": "Sticker Folders"},
        "emoji_folders_header": {"ru": "Папки эмодзи", "en": "Emoji Folders"},

        "folders_enabled_title": {"ru": "Папки на панели {0}", "en": "Folders in {0} panel"},
        "folders_enabled_sub": {"ru": "Показывать вкладки папок на панели {0}", "en": "Show folder tabs in the {0} panel"},

        "design_header": {"ru": "Оформление", "en": "Design"},
        "tab_bg_color": {"ru": "Цвет вкладок папок", "en": "Folder tab color"},
        "tab_bg_color_sub": {"ru": "Hex, например #2F80ED", "en": "Hex, e.g. #2F80ED"},
        "tab_text_color": {"ru": "Цвет текста вкладок папок", "en": "Folder tab text color"},
        "tab_text_color_sub": {"ru": "Hex, например #FFFFFF", "en": "Hex, e.g. #FFFFFF"},
        "subtab_bg_color": {"ru": "Цвет подвкладок", "en": "Sub-tab color"},
        "subtab_bg_color_sub": {"ru": "Цвет вкладок наборов внутри папок", "en": "Color for pack tabs inside folders"},
        "subtab_text_color": {"ru": "Цвет текста подвкладок", "en": "Sub-tab text color"},
        "subtab_text_color_sub": {"ru": "Цвет текста для вкладок наборов", "en": "Text color for pack tabs"},

        "glass_header": {"ru": "Жидкое стекло", "en": "Liquid Glass"},
        "glass_enabled": {"ru": "Включить Liquid Glass", "en": "Enable Liquid Glass"},
        "glass_enabled_sub": {"ru": "Эффект матового стекла для панели папок", "en": "Frosted glass effect for the folder bar"},
        "glass_tabs": {"ru": "Стеклянные вкладки", "en": "Glass tabs"},
        "glass_tabs_sub": {"ru": "Эффект стекла на отдельных вкладках", "en": "Glass effect on individual tabs"},
        "glass_alpha": {"ru": "Прозрачность стекла", "en": "Glass opacity"},
        "glass_alpha_sub": {"ru": "0.1 - 1.0", "en": "0.1 - 1.0"},
        "err_alpha_range": {"ru": "Введите число от 0.1 до 1.0", "en": "Enter a number between 0.1 and 1.0"},

        "settings_header": {"ru": "Настройки", "en": "Settings"},
        "hide_trending": {"ru": "Скрыть популярные GIF", "en": "Hide Trending GIFs"},
        "hide_trending_sub": {"ru": "Скрыть раздел популярных GIF на панели GIF", "en": "Hide popular GIFs section in GIF panel"},
        "collapse_on_scroll": {"ru": "Сворачивать панель папок при скролле", "en": "Collapse folders bar on scroll"},
        "collapse_on_scroll_sub": {"ru": "Скрывать панель папок при скролле вниз, показывать при скролле вверх", "en": "Hide the folder bar when scrolling down, show it again when scrolling up"},

        "backup_header": {"ru": "Резервное копирование", "en": "Backup"},
        "backup_saved": {"ru": "Сохранить в Избранное", "en": "Backup to Saved Messages"},
        "backup_saved_sub": {"ru": "Сохранить структуру всех папок для последующего восстановления", "en": "Save all folder structures to restore later"},
        "backup_tip": {"ru": "Нажмите на файл .mediafolder_backup в Избранном для восстановления.", "en": "Tap a .mediafolder_backup file in Saved Messages to restore."},
        "reset_all": {"ru": "Сбросить все папки", "en": "Reset All Folders"},

        "all_gifs": {"ru": "Все GIF", "en": "All GIFs"},
        "all_stickers": {"ru": "Все стикеры", "en": "All Stickers"},
        "all_emoji": {"ru": "Все эмодзи", "en": "All Emoji"},
        "all": {"ru": "Все", "en": "All"},
        "hidden_badge": {"ru": "\U0001F6AB Скрыта", "en": "\U0001F6AB Hidden"},
        "default_folder": {"ru": "Папка", "en": "Folder"},
    }

    @classmethod
    def is_ru(cls) -> bool:
        try:
            from org.telegram.messenger import LocaleController
            cur = str(LocaleController.getInstance().getCurrentLocale().getLanguage() or "").lower()
            if cur.startswith("ru") or cur.startswith("uk") or cur.startswith("be"):
                return True
            if cur:
                return False
        except Exception:
            pass

        try:
            from java.util import Locale
            code = str(Locale.getDefault().getLanguage() or "").lower()
            if code.startswith("ru") or code.startswith("uk") or code.startswith("be"):
                return True
        except Exception:
            pass

        return False

    @classmethod
    def g(cls, key: str, *args) -> str:
        lang = "ru" if cls.is_ru() else "en"
        entry = cls._T.get(key)
        if not entry:
            return key
        val = entry.get(lang, entry.get("en", key))
        if args:
            try:
                return val.format(*args)
            except Exception:
                return val
        return val


def log(msg):
    try:
        print(msg)
    except Exception:
        pass


def _get_prefs():
    return ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


def _load_folders(media_type):
    if media_type == "gif":
        key = GIF_FOLDERS_KEY
    elif media_type == "sticker":
        key = STICKER_FOLDERS_KEY
    else:
        key = EMOJI_FOLDERS_KEY
    raw = _get_prefs().getString(key, "[]")
    try:
        return json.loads(str(raw))
    except Exception:
        return []


def _load_tab_order(media_type):
    if media_type == "gif":
        key = GIF_TAB_ORDER_KEY
    elif media_type == "sticker":
        key = STICKER_TAB_ORDER_KEY
    else:
        key = EMOJI_TAB_ORDER_KEY
    raw = _get_prefs().getString(key, "null")
    try:
        order = json.loads(str(raw))
        if isinstance(order, list):
            return order
    except Exception:
        pass
    return None


def _get_ordered_tabs(media_type):
    folders = _load_folders(media_type)
    folder_ids = [f.get("id") for f in folders]
    order = _load_tab_order(media_type)
    if order is None:
        return ["all"] + folder_ids
    valid_ids = set(["all"] + folder_ids)
    result = []
    for tab_id in order:
        if tab_id in valid_ids and tab_id not in result:
            result.append(tab_id)
    for tab_id in ["all"] + folder_ids:
        if tab_id not in result:
            result.append(tab_id)
    return result


def _get_all_tab_name(media_type):
    default = {
        "gif": L.g("all_gifs"),
        "sticker": L.g("all_stickers"),
        "emoji": L.g("all_emoji"),
    }.get(media_type, L.g("all"))
    return str(_get_prefs().getString("all_tab_name_%s" % media_type, default))


def _get_all_tab_hidden(media_type):
    return bool(_get_prefs().getBoolean("all_tab_hidden_%s" % media_type, False))


def _get_color_setting(key):
    return str(_get_prefs().getString(key, ""))


def _get_glass_enabled():
    return bool(_get_prefs().getBoolean("glass_enabled", True))


def _get_glass_tabs():
    return bool(_get_prefs().getBoolean("glass_tabs", True))


def _get_glass_alpha():
    return float(_get_prefs().getFloat("glass_alpha", 0.75))


def _get_hide_trending():
    return bool(_get_prefs().getBoolean("hide_trending", False))


def _get_folders_enabled(media_type):
    return bool(_get_prefs().getBoolean("folders_enabled_%s" % media_type, True))


def _get_collapse_on_scroll():
    return bool(_get_prefs().getBoolean("collapse_on_scroll", True))


# __DEX_BEGIN__
# PLACEHOLDER
# __DEX_END__


class Dex:
    def __init__(self, ec):
        self.ec = ec
        self.clazz = None
        self.loaded = False
        self._m = {}

    def load(self):
        if self.loaded:
            return
        if self.clazz is None:
            ctx = ApplicationLoader.applicationContext
            if not ctx:
                raise RuntimeError("no ctx")
            self.clazz = jclass("dalvik.system.InMemoryDexClassLoader")(jclass("java.nio.ByteBuffer").wrap(self._rp()), ctx.getClassLoader()).loadClass(self.ec)
            for m in self.clazz.getDeclaredMethods():
                m.setAccessible(True)
                self._m[str(m.getName())] = m
        self._m["load"].invoke(None)
        self.loaded = True

    def unload(self):
        try:
            if self.loaded and "unload" in self._m:
                self._m["unload"].invoke(None)
        except Exception:
            pass
        self.loaded = False

    def call(self, name, *args):
        try:
            if self.loaded and name in self._m:
                return self._m[name].invoke(None, *args)
        except Exception as e:
            log("dex.%s: %s" % (name, e))
        return None

    def _rp(self):
        with open(__file__, "r", encoding="utf-8") as f:
            t = f.read()
        b = t.split(f"# {DEX_BEGIN}", 1)[1].split(f"# {DEX_END}", 1)[0]
        p = "".join(l.strip()[1:].strip() for l in b.splitlines() if l.strip().startswith("#"))
        if not p:
            raise RuntimeError("dex empty")
        return zlib.decompress(base64.b64decode(p))



# ============================================================
# MaxExteraPlugins Shared Author & Channel Subscription Module
# ============================================================
class AuthorChannelHelper:
    CONFIG_URL = "https://raw.githubusercontent.com/MaxExteraPlugins/config/main/links.json"

    DEFAULT_LINKS = {
        "channel_username": "MaxExteraPlugins",
        "channel_url": "https://t.me/MaxExteraPlugins",
        "header": {"ru": "MaxExteraPlugins", "en": "MaxExteraPlugins"},
        "buttons": [
            {"title": {"ru": "Канал автора", "en": "Author's Channel"}, "icon": "msg_channel", "url": "https://t.me/MaxExteraPlugins"},
            {"title": {"ru": "Поддержать автора", "en": "Support author"}, "icon": "msg_share", "url": "https://t.me/itNotMax"},
            {"title": {"ru": "Сообщить о баге", "en": "Report a bug"}, "icon": "msg_help", "url": "https://t.me/itNotMax"}
        ]
    }

    _TEXTS = {
        "install_card_title": {"ru": "Подпишитесь на канал", "en": "Subscribe to channel"},
        "install_card_msg": {
            "ru": (
                "Мы делаем плагины бесплатными и доступными для всех: пожалуйста, "
                "поддержите нас подпиской на официальный канал @MaxExteraPlugins.\n\n"
                "Там публикуются обновления, важные новости и анонсы новых плагинов."
            ),
            "en": (
                "We keep our plugins free and accessible for everyone: please "
                "support us by subscribing to our official channel @MaxExteraPlugins.\n\n"
                "Updates, news, and new plugin announcements are published there."
            )
        },
        "open_channel_btn": {"ru": "Перейти на канал", "en": "Open channel"},
        "remind_later_btn": {"ru": "Напомнить позже", "en": "Remind later"},
        "never_show_btn": {"ru": "Больше не показывать", "en": "Don't show again"},
        "sub_thank_you": {"ru": "Спасибо за подписку на наш канал!", "en": "Thank you for subscribing to our channel!"},
        "settings_channel": {"ru": "Канал автора", "en": "Author's Channel"},
        "settings_donate": {"ru": "Поддержать автора", "en": "Support author"},
        "settings_bug_report": {"ru": "Сообщить о баге", "en": "Report a bug"}
    }

    @classmethod
    def is_ru(cls) -> bool:
        try:
            from org.telegram.messenger import LocaleController
            lang = str(LocaleController.getInstance().getCurrentLocale().getLanguage() or "").lower()
            return lang in ("ru", "be", "uk", "kk")
        except Exception:
            return True

    @classmethod
    def tr(cls, key: str) -> str:
        entry = cls._TEXTS.get(key, {})
        return entry.get("ru" if cls.is_ru() else "en", entry.get("en", key))

    def __init__(self, plugin, icon_sticker=None):
        self.plugin = plugin
        self.icon_sticker = icon_sticker or getattr(plugin, "__icon__", "MaxExteraPluginsStickers/1")
        self._stopped = False
        self._active_dialog = None

    def on_load(self):
        self._stopped = False
        self._active_dialog = None
        self._load_remote_links_async()
        self._check_initial_subscription_flow()

    def on_event(self, event_type):
        try:
            name = str(event_type).upper()
            if name.endswith("START") or name.endswith("RESUME"):
                if self.is_subscribed() and not self.plugin.get_setting("was_subscribed", False):
                    self.plugin.set_setting("was_subscribed", True)
                    self._dismiss_active_dialog()
                    self._notify_subscribed()
        except Exception:
            pass

    def on_unload(self):
        self._stopped = True
        self._dismiss_active_dialog()

    def is_allowed(self) -> bool:
        return True

    def _get_links(self) -> dict:
        raw = self.plugin.get_setting("remote_links_cache", None)
        if raw:
            try:
                import json
                data = json.loads(raw)
                if isinstance(data, dict): return data
            except Exception:
                pass
        return self.DEFAULT_LINKS

    def _load_remote_links_async(self):
        def worker():
            try:
                import urllib.request, json
                req = urllib.request.Request(self.CONFIG_URL, headers={"User-Agent": "MaxExteraPlugins"})
                with urllib.request.urlopen(req, timeout=4) as resp:
                    if resp.status == 200:
                        data = json.loads(resp.read().decode("utf-8"))
                        if isinstance(data, dict):
                            self.plugin.set_setting("remote_links_cache", json.dumps(data))
                            self._resolve_channel_in_telegram(data.get("channel_username", "MaxExteraPlugins"))
            except Exception:
                pass
        import threading
        t = threading.Thread(target=worker, daemon=True)
        t.start()

    def _open_url(self, url: str, view=None):
        try:
            from org.telegram.messenger.browser import Browser
            from org.telegram.messenger import ApplicationLoader
            from client_utils import get_last_fragment
            ctx = None
            if view is not None:
                ctx = view.getContext()
            if ctx is None:
                frag = get_last_fragment()
                if frag is not None:
                    ctx = frag.getParentActivity()
            if ctx is None:
                ctx = ApplicationLoader.applicationContext
            Browser.openUrl(ctx, url)
        except Exception:
            pass

    def _resolve_channel_in_telegram(self, username: str):
        try:
            from org.telegram.messenger import UserConfig, MessagesController
            from org.telegram.tgnet import TLRPC, ConnectionsManager, RequestDelegate
            clean_user = username.lstrip("@")
            acc = int(UserConfig.selectedAccount)
            mc = MessagesController.getInstance(acc)
            if not mc: return
            req = TLRPC.TL_contacts_resolveUsername()
            req.username = clean_user
            helper_ref = self
            class Delegate(RequestDelegate):
                def run(self, response, error):
                    if error is None and response is not None:
                        chats = getattr(response, "chats", None)
                        if chats is not None and chats.size() > 0:
                            for a in range(UserConfig.MAX_ACCOUNT_COUNT):
                                try:
                                    if UserConfig.getInstance(a).isClientActivated():
                                        MessagesController.getInstance(a).putChats(chats, False)
                                except Exception:
                                    pass
                            c = chats.get(0)
                            cid = getattr(c, "id", 0)
                            if cid > 0:
                                helper_ref.plugin.set_setting("resolved_channel_id", int(cid))
            ConnectionsManager.getInstance(acc).sendRequest(req, Delegate())
        except Exception:
            pass

    def is_subscribed(self) -> bool:
        try:
            from org.telegram.messenger import UserConfig, MessagesController, MessagesStorage
            from java.lang import Long as JLong
            links = self._get_links()
            target = str(links.get("channel_username", "MaxExteraPlugins") or "MaxExteraPlugins").lower().lstrip("@")
            saved_cid = int(self.plugin.get_setting("resolved_channel_id", 0) or 0)

            for acc in range(UserConfig.MAX_ACCOUNT_COUNT):
                try:
                    uconf = UserConfig.getInstance(acc)
                    if not uconf.isClientActivated():
                        continue

                    mc = MessagesController.getInstance(acc)
                    storage = MessagesStorage.getInstance(acc)

                    # 1. In-memory check by resolved_channel_id
                    if saved_cid and mc is not None:
                        try:
                            c = mc.getChat(JLong(saved_cid))
                            if c is not None:
                                left = bool(getattr(c, "left", True))
                                kicked = bool(getattr(c, "kicked", False))
                                if not left and not kicked:
                                    return True
                        except Exception:
                            pass

                    # 2. In-memory check by username
                    if mc is not None:
                        try:
                            chat = mc.getUserOrChat(target)
                            if chat is not None:
                                cid = int(getattr(chat, "id", 0) or 0)
                                if cid and not saved_cid:
                                    saved_cid = cid
                                    self.plugin.set_setting("resolved_channel_id", cid)
                                left = bool(getattr(chat, "left", True))
                                kicked = bool(getattr(chat, "kicked", False))
                                if not left and not kicked:
                                    return True
                        except Exception:
                            pass

                    # 3. In-memory dialogs check
                    if mc is not None:
                        try:
                            dialog_lists = []
                            d_chan = getattr(mc, "dialogsChannelsOnly", None)
                            if d_chan is not None and d_chan.size() > 0:
                                dialog_lists.append(d_chan)
                            d_all = getattr(mc, "allDialogs", None)
                            if d_all is not None and d_all.size() > 0:
                                dialog_lists.append(d_all)

                            for d_list in dialog_lists:
                                limit = min(d_list.size(), 300)
                                for i in range(limit):
                                    d = d_list.get(i)
                                    did = getattr(d, "id", 0)
                                    if did < 0:
                                        cid = -did
                                        if saved_cid and cid == saved_cid:
                                            c = mc.getChat(JLong(cid))
                                            if c is not None:
                                                left = bool(getattr(c, "left", True))
                                                kicked = bool(getattr(c, "kicked", False))
                                                if not left and not kicked:
                                                    return True
                                        else:
                                            c = mc.getChat(JLong(cid))
                                            if c is not None:
                                                uname = str(getattr(c, "username", "") or "").lower()
                                                if uname == target:
                                                    if not saved_cid:
                                                        saved_cid = cid
                                                        self.plugin.set_setting("resolved_channel_id", cid)
                                                    left = bool(getattr(c, "left", True))
                                                    kicked = bool(getattr(c, "kicked", False))
                                                    if not left and not kicked:
                                                        return True
                        except Exception:
                            pass

                    # 4. Storage check (SQLite on disk)
                    if storage is not None:
                        if saved_cid:
                            try:
                                if storage.containsLocalDialog(JLong(-saved_cid)):
                                    c_sync = storage.getChatSync(JLong(saved_cid))
                                    if c_sync is not None:
                                        left = bool(getattr(c_sync, "left", True))
                                        kicked = bool(getattr(c_sync, "kicked", False))
                                        if not left and not kicked:
                                            return True
                                    else:
                                        return True
                            except Exception:
                                pass

                        try:
                            db = storage.getDatabase()
                            if db is not None:
                                if saved_cid:
                                    try:
                                        cur = db.queryFinalized("SELECT did FROM dialogs WHERE did = ?", [JLong(-saved_cid)])
                                        if cur is not None:
                                            has_row = bool(cur.next())
                                            cur.dispose()
                                            if has_row:
                                                return True
                                    except Exception:
                                        pass

                                try:
                                    cur = db.queryFinalized("SELECT uid, name FROM chats WHERE LOWER(name) LIKE ?", ["%" + target + "%"])
                                    if cur is not None:
                                        while cur.next():
                                            c_id = cur.longValue(0)
                                            if c_id > 0:
                                                if not saved_cid:
                                                    saved_cid = int(c_id)
                                                    self.plugin.set_setting("resolved_channel_id", saved_cid)
                                                cur_d = db.queryFinalized("SELECT did FROM dialogs WHERE did = ?", [JLong(-c_id)])
                                                if cur_d is not None:
                                                    in_dlg = bool(cur_d.next())
                                                    cur_d.dispose()
                                                    if in_dlg:
                                                        cur.dispose()
                                                        return True
                                        cur.dispose()
                                except Exception:
                                    pass
                        except Exception:
                            pass
                except Exception:
                    pass
        except Exception:
            pass
        return False

    def prepend_settings(self, original_items=None) -> list:
        from ui.settings import Header, Text, Divider

        if original_items and any(getattr(x, "text", "") == "MaxExteraPlugins" for x in original_items if type(x).__name__ == "Header"):
            return original_items

        links = self._get_links()
        buttons_config = links.get("buttons")

        author_items = []
        if isinstance(buttons_config, list):
            for btn in buttons_config:
                if not isinstance(btn, dict): continue
                raw_url = btn.get("url")
                if isinstance(raw_url, dict):
                    keys = [
                        getattr(self.plugin, "id", None),
                        globals().get("__id__", None),
                        globals().get("__name__", None),
                        self.plugin.__class__.__name__
                    ]
                    keys += [k.lower() for k in keys if isinstance(k, str)]
                    resolved_url = ""
                    for k in keys:
                        if k and k in raw_url:
                            resolved_url = str(raw_url[k] or "").strip()
                            if resolved_url:
                                break
                    url = resolved_url
                elif isinstance(raw_url, str):
                    url = raw_url.strip()
                else:
                    url = ""

                if not url: continue

                title_data = btn.get("title")
                if isinstance(title_data, dict):
                    title = title_data.get("ru" if self.is_ru() else "en") or title_data.get("en") or title_data.get("ru") or ""
                elif isinstance(title_data, str) and title_data:
                    title = title_data
                else:
                    title = btn.get("title_ru" if self.is_ru() else "title_en") or btn.get("title_ru") or btn.get("title_en") or ""

                if not title: continue
                icon = str(btn.get("icon") or "msg_channel").strip()

                def make_click(u):
                    return lambda v=None: self._open_url(u, v)

                author_items.append(Text(text=title, icon=icon, on_click=make_click(url)))
        else:
            for k, text_key, icon_name in [
                ("channel_url", "settings_channel", "msg_channel"),
                ("donate_url", "settings_donate", "msg_share"),
                ("bug_report_url", "settings_bug_report", "msg_help")
            ]:
                u = str(links.get(k) or "").strip()
                if u:
                    author_items.append(Text(text=self.tr(text_key), icon=icon_name, on_click=lambda v=None, target_u=u: self._open_url(target_u, v)))

        res = []
        if author_items:
            hdr = links.get("header") or "MaxExteraPlugins"
            if isinstance(hdr, dict):
                hdr_text = hdr.get("ru" if self.is_ru() else "en") or hdr.get("ru") or hdr.get("en") or "MaxExteraPlugins"
            else:
                hdr_text = str(hdr)
            res.append(Header(text=hdr_text))
            res.extend(author_items)
            res.append(Divider())

        cleaned_items = []
        if original_items:
            skipping_info = False
            for item in original_items:
                item_cls = type(item).__name__
                if item_cls == "Header":
                    txt = str(getattr(item, "text", "") or getattr(item, "title", "") or "").lower()
                    if "информ" in txt or "inform" in txt:
                        skipping_info = True
                        continue
                    else:
                        skipping_info = False
                elif skipping_info and item_cls == "Divider":
                    skipping_info = False
                    continue

                if not skipping_info:
                    cleaned_items.append(item)

        res.extend(cleaned_items)
        return res

    def _dismiss_active_dialog(self):
        try:
            if self._active_dialog is not None:
                self._active_dialog.dismiss()
        except Exception:
            pass
        self._active_dialog = None

    def _is_dialog_active(self) -> bool:
        try:
            if self._active_dialog is not None:
                return bool(self._active_dialog.isShowing())
        except Exception:
            pass
        return False

    def _notify_subscribed(self):
        try:
            from ui.bulletin import BulletinHelper
            BulletinHelper.show_success(self.tr("sub_thank_you"))
        except Exception:
            pass

    def _check_initial_subscription_flow(self):
        def flow():
            try:
                if self.plugin.get_setting("never_show_sub_prompt", False):
                    return

                current_ver = str(getattr(self.plugin, "__version__", globals().get("__version__", "1.0.0")) or "1.0.0")
                last_prompted_ver = self.plugin.get_setting("last_prompted_version", None)

                # Only show on install (last_prompted_ver is None) or update (last_prompted_ver != current_ver)
                if last_prompted_ver is not None and str(last_prompted_ver) == current_ver:
                    return

                if self.is_subscribed():
                    self.plugin.set_setting("last_prompted_version", current_ver)
                    self.plugin.set_setting("was_subscribed", True)
                    return

                self._show_subscription_prompt(current_ver)
            except Exception:
                pass
        from android_utils import run_on_ui_thread
        run_on_ui_thread(flow, 1500)

    def _create_card_content(self, act, title_text, message_text):
        try:
            from android.widget import LinearLayout, TextView, ImageView
            from android.view import Gravity
            from org.telegram.messenger import AndroidUtilities
            from org.telegram.ui.ActionBar import Theme
            from client_utils import create_sticker_drawable

            dp = AndroidUtilities.dp

            layout = LinearLayout(act)
            layout.setOrientation(LinearLayout.VERTICAL)
            layout.setGravity(Gravity.CENTER_HORIZONTAL)
            layout.setPadding(dp(24), dp(20), dp(24), dp(10))

            sticker_drawable = None
            if self.icon_sticker:
                try:
                    sticker_drawable = create_sticker_drawable(self.icon_sticker, dp(120), dp(120))
                except Exception:
                    pass

            if sticker_drawable is not None:
                iv = ImageView(act)
                iv.setImageDrawable(sticker_drawable)
                lp_iv = LinearLayout.LayoutParams(dp(120), dp(120))
                lp_iv.gravity = Gravity.CENTER_HORIZONTAL
                lp_iv.bottomMargin = dp(14)
                layout.addView(iv, lp_iv)

            if title_text:
                tt = TextView(act)
                tt.setText(title_text)
                tt.setTextSize(19.0)
                tt.setTypeface(AndroidUtilities.bold())
                tt.setTextColor(Theme.getColor(Theme.key_dialogTextBlack))
                tt.setGravity(Gravity.CENTER_HORIZONTAL)
                lp_t = LinearLayout.LayoutParams(-1, -2)
                lp_t.bottomMargin = dp(8)
                layout.addView(tt, lp_t)

            if message_text:
                mt = TextView(act)
                mt.setText(message_text)
                mt.setTextSize(15.0)
                mt.setTextColor(Theme.getColor(Theme.key_dialogTextBlack))
                mt.setLineSpacing(float(dp(3)), 1.0)
                mt.setGravity(Gravity.CENTER_HORIZONTAL)
                lp_m = LinearLayout.LayoutParams(-1, -2)
                mt.setLayoutParams(lp_m)
                layout.addView(mt)

            return layout
        except Exception:
            return None

    def _show_subscription_prompt(self, current_ver: str):
        def show():
            try:
                if self._is_dialog_active(): return
                from client_utils import get_last_fragment
                frag = get_last_fragment()
                if not frag: return
                act = frag.getParentActivity()
                if not act: return

                links = self._get_links()
                url = links.get("channel_url", "https://t.me/MaxExteraPlugins")

                from ui.alert import AlertDialogBuilder
                b = AlertDialogBuilder(act)
                content = self._create_card_content(act, self.tr("install_card_title"), self.tr("install_card_msg"))
                if content is not None:
                    b.set_view(content)
                else:
                    b.set_title(self.tr("install_card_title"))
                    b.set_message(self.tr("install_card_msg"))

                def on_open_channel(dialog, which):
                    self._dismiss_active_dialog()
                    self.plugin.set_setting("last_prompted_version", current_ver)
                    self._open_url(url)

                def on_remind_later(dialog, which):
                    self._dismiss_active_dialog()
                    self.plugin.set_setting("last_prompted_version", current_ver)

                def on_never_show(dialog, which):
                    self._dismiss_active_dialog()
                    self.plugin.set_setting("never_show_sub_prompt", True)
                    self.plugin.set_setting("last_prompted_version", current_ver)

                b.set_positive_button(self.tr("open_channel_btn"), on_open_channel)
                b.set_neutral_button(self.tr("remind_later_btn"), on_remind_later)
                b.set_negative_button(self.tr("never_show_btn"), on_never_show)

                def on_dismiss(builder):
                    self.plugin.set_setting("last_prompted_version", current_ver)
                try:
                    b.set_on_dismiss_listener(on_dismiss)
                except Exception:
                    pass

                self._active_dialog = b.show()
                self.plugin.set_setting("last_prompted_version", current_ver)
            except Exception:
                pass
        from android_utils import run_on_ui_thread
        run_on_ui_thread(show)

class Plugin(BasePlugin):

    def on_plugin_load(self):
        self._author_helper = AuthorChannelHelper(self, icon_sticker=globals().get("__icon__", "MaxExteraPluginsStickers/20"))
        self._author_helper.on_load()
        try:
            self.dex = Dex(ENTRY_CLASS)
            self.dex.load()
        except Exception as e:
            self.log("dex load error: %s" % e)

    def on_plugin_unload(self):
        if hasattr(self, "_author_helper"):
            self._author_helper.on_unload()
        try:
            if hasattr(self, "dex"):
                self.dex.unload()
        except Exception:
            pass

    def on_app_event(self, event_type):
        if hasattr(self, '_author_helper'):
            self._author_helper.on_event(event_type)

    def _call(self, name, *args):
        try:
            if hasattr(self, "dex"):
                return self.dex.call(name, *args)
        except Exception:
            pass
        return None

    def _open_url_internal(self, url):
        def handler(view):
            try:
                Browser = jclass("org.telegram.messenger.browser.Browser")
                ctx = view.getContext()
                uri = jclass("android.net.Uri").parse(url)
                Browser.openUrl(ctx, uri, True, False, None)
            except Exception:
                try:
                    Browser = jclass("org.telegram.messenger.browser.Browser")
                    ctx = view.getContext()
                    Browser.openInTelegramBrowser(ctx, url, None)
                except Exception as e:
                    self.log("open url error: %s" % e)
        return handler

    def _on_color_change(self, key):
        def cb(value):
            self._call("pySetColor", JString(key), JString(str(value).strip()))
        return cb

    def _on_glass_toggle(self, value):
        self._call("pySetGlassEnabled", jboolean(bool(value)))

    def _on_glass_tabs_toggle(self, value):
        self._call("pySetGlassTabs", jboolean(bool(value)))

    def _on_glass_alpha_change(self, value):
        try:
            self._call("pySetGlassAlpha", float(value))
        except Exception:
            BulletinHelper.show_error(L.g("err_alpha_range"))

    def _on_trending_toggle(self, value):
        self._call("pySetHideTrending", jboolean(bool(value)))

    def _on_folders_toggle(self, media_type):
        def cb(value):
            self._call("pySetFoldersEnabled", JString(media_type), jboolean(bool(value)))
        return cb

    def _on_collapse_toggle(self, value):
        self._call("pySetCollapseOnScroll", jboolean(bool(value)))

    def _get_folder_items_for_type(self, media_type):
        items = []
        plugin = self
        order = _get_ordered_tabs(media_type)
        folders = _load_folders(media_type)
        folder_map = {f.get("id"): f.get("name", L.g("default_folder")) for f in folders}
        for i, tab_id in enumerate(order):
            if tab_id == "all":
                name = _get_all_tab_name(media_type)
                is_hidden = _get_all_tab_hidden(media_type)
                sub = L.g("hidden_badge") if is_hidden else None
                items.append(Text(
                    text=name,
                    subtext=sub,
                    icon="msg_folder",
                    on_click=lambda v, mt=media_type, tid=tab_id, tname=name: plugin._call("pyShowFolderManage", JString(mt), JString(tid), JString(tname))
                ))
            elif tab_id in folder_map:
                name = folder_map[tab_id]
                items.append(Text(
                    text=name,
                    icon="msg_folder",
                    on_click=lambda v, mt=media_type, tid=tab_id, tname=name: plugin._call("pyShowFolderManage", JString(mt), JString(tid), JString(tname))
                ))
        return items

    def create_settings(self):
        try:
            items = self._inner_create_settings()
        except Exception:
            items = []
        if hasattr(self, '_author_helper'):
            return self._author_helper.prepend_settings(items)
        return items

    def _inner_create_settings(self):
        plugin = self
        items = []
        for mt, label in [("gif", "GIF"), ("sticker", "Sticker"), ("emoji", "Emoji")]:
            items.append(Header(text=L.g(f"{mt}_folders_header")))
            items.append(Switch(
                key="folders_enabled_%s" % mt,
                text=L.g("folders_enabled_title", label),
                subtext=L.g("folders_enabled_sub", label),
                default=_get_folders_enabled(mt),
                icon="msg_folders",
                on_change=plugin._on_folders_toggle(mt)
            ))
            items.extend(self._get_folder_items_for_type(mt))
            items.append(Divider())
        items.extend([
            Header(text=L.g("design_header")),
            Input(key="tab_bg_color", text=L.g("tab_bg_color"), subtext=L.g("tab_bg_color_sub"), default=_get_color_setting("tab_bg_color"), icon="msg_edit", on_change=self._on_color_change("tab_bg_color")),
            Input(key="tab_text_color", text=L.g("tab_text_color"), subtext=L.g("tab_text_color_sub"), default=_get_color_setting("tab_text_color"), icon="msg_edit", on_change=self._on_color_change("tab_text_color")),
            Input(key="subtab_bg_color", text=L.g("subtab_bg_color"), subtext=L.g("subtab_bg_color_sub"), default=_get_color_setting("subtab_bg_color"), icon="msg_edit", on_change=self._on_color_change("subtab_bg_color")),
            Input(key="subtab_text_color", text=L.g("subtab_text_color"), subtext=L.g("subtab_text_color_sub"), default=_get_color_setting("subtab_text_color"), icon="msg_edit", on_change=self._on_color_change("subtab_text_color")),
            Divider(),
            Header(text=L.g("glass_header")),
            Switch(key="glass_enabled", text=L.g("glass_enabled"), subtext=L.g("glass_enabled_sub"), default=_get_glass_enabled(), icon="msg_theme", on_change=plugin._on_glass_toggle),
            Switch(key="glass_tabs", text=L.g("glass_tabs"), subtext=L.g("glass_tabs_sub"), default=_get_glass_tabs(), icon="msg_photo_settings", on_change=plugin._on_glass_tabs_toggle),
            Input(key="glass_alpha", text=L.g("glass_alpha"), subtext=L.g("glass_alpha_sub"), default=str(_get_glass_alpha()), icon="msg_edit", on_change=plugin._on_glass_alpha_change),
            Divider(),
            Header(text=L.g("settings_header")),
            Switch(key="hide_trending_gifs", text=L.g("hide_trending"), subtext=L.g("hide_trending_sub"), default=_get_hide_trending(), icon="msg_theme", on_change=plugin._on_trending_toggle),
            Switch(key="collapse_on_scroll", text=L.g("collapse_on_scroll"), subtext=L.g("collapse_on_scroll_sub"), default=_get_collapse_on_scroll(), icon="msg_reorder", on_change=plugin._on_collapse_toggle),
            Divider(),
            Header(text=L.g("backup_header")),
            Text(text=L.g("backup_saved"), subtext=L.g("backup_saved_sub"), icon="msg_saved", on_click=lambda v: plugin._call("pyDoBackup")),
            Divider(text=L.g("backup_tip")),
            Divider(),
            Text(text=L.g("reset_all"), icon="msg_delete", red=True, on_click=lambda v: plugin._call("pyResetAll")),
        ])
        return items
