package vorez.mods.skins.providers;

import com.mojang.logging.LogUtils;
import vorez.lib.HDImagesNotAllowed;
import vorez.lib.SharedPool;
import vorez.mods.skins.api.interfaces.IPlayerProfile;
import vorez.mods.skins.api.interfaces.ISkin;
import vorez.mods.skins.api.interfaces.ISkinProvider;
import vorez.mods.skins.impl.ConfigOptions;
import vorez.mods.skins.impl.Shared;
import vorez.mods.skins.impl.SkinData;
import vorez.mods.skins.impl.fabric.ImageUtils;
import vorez.mods.skins.init.fabric.FabricOfflineSkinsReloaded;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.function.Function;

import org.slf4j.Logger;

public class CustomServerSkinProvider implements ISkinProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Function<ByteBuffer, ByteBuffer> _filter;
    private String _host;
    private boolean _allowHd = false;

    @Override
    public ISkin getSkin(IPlayerProfile profile) {
        SkinData skin = new SkinData();
        if (_filter != null)
            skin.setSkinFilter(_filter);
        SharedPool.execute(() -> {
            if (_host != null && !_host.isEmpty()) {
                String url = replaceValues(_host, profile);
                if (!_host.equals(url)) {
                    if (!isHttpAllowed(url)) {
                        LOGGER.warn("[OfflineSkins-Reloaded] Blocked HTTP skin request for {}", profile.getPlayerName());
                        return;
                    }
                    Shared.downloadSkin(url, Runnable::run).thenAccept(optional -> optional.ifPresent(data -> {
                        if (!ImageUtils.validateData(data)) {
                            LOGGER.error("[OfflineSkins-Reloaded] Rejected skin for {} because it failed image validation.", profile.getPlayerName());
                            return;
                        }
                        if (!ImageUtils.validateSkin(data, _allowHd)) {
                            skin.put(HDImagesNotAllowed.skin(), "default");
                            LOGGER.warn("[OfflineSkins-Reloaded] Rejected HD skin for {} because HD skins are disabled.",  profile.getPlayerName());
                            return;
                        }
                        skin.put(data, ImageUtils.judgeSkinType(data));
                    }));
                }
            }
        });
        return skin;
    }
    private boolean isHttpAllowed(String url) {
        try {
            URI uri = URI.create(url);
            if ("http".equalsIgnoreCase(uri.getScheme())) {
                ConfigOptions config = FabricOfflineSkinsReloaded.loadConfigSnapshot();
                return config != null && config.allowHTTP;
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    private String replaceValues(String host, IPlayerProfile profile) {
        String name = profile.getPlayerName();
        return host.replace("%name%", name)
                .replace("%auto%", name + ".png");
    }

    public CustomServerSkinProvider setHost(String host) {
        _host = host;
        return this;
    }

    public CustomServerSkinProvider setAllowHd(boolean allowHd) {
        this._allowHd = allowHd;
        return this;
    }

    public CustomServerSkinProvider withFilter(Function<ByteBuffer, ByteBuffer> filter) {
        _filter = filter;
        return this;
    }
}