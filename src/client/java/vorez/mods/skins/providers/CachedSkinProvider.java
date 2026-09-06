package vorez.mods.skins.providers;

import vorez.lib.SharedPool;
import vorez.mods.skins.api.interfaces.IPlayerProfile;
import vorez.mods.skins.api.interfaces.ISkin;
import vorez.mods.skins.api.interfaces.ISkinProvider;
import vorez.mods.skins.impl.Shared;
import vorez.mods.skins.impl.SkinData;
import vorez.mods.skins.impl.fabric.ImageUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CachedSkinProvider implements ISkinProvider {
    private final File _dirN;
    private final File _dirU;
    private static final Map<String, Path> selectedSkins = new ConcurrentHashMap<>();
    private Function<ByteBuffer, ByteBuffer> _filter;

    public CachedSkinProvider(Path workDir) {
        _dirN = new File(workDir.toFile(), "skins");
        if (!_dirN.isDirectory() && !_dirN.mkdirs())
            throw new IllegalStateException("Failed to create directory: " + _dirN);

        _dirU = new File(_dirN, "uuid");
        if (!_dirU.isDirectory() && !_dirU.mkdirs())
            throw new IllegalStateException("Failed to create directory: " + _dirU);
    }

    @Override
    public ISkin getSkin(IPlayerProfile profile) {
        SkinData skin = new SkinData();
        if (_filter != null) {
            skin.setSkinFilter(_filter);
        }
        String playerName = profile.getPlayerName();
        String playerUUID= String.valueOf(profile.getPlayerUUID());

        SharedPool.execute(() -> {
            byte[] data = null;
            Path selected = getSelectedSkin(playerName);

            if (selected != null)
                data = readFile(selected);

            if (data == null && !Shared.isOfflinePlayer(profile.getPlayerUUID(), playerName))
                data = readFile(_dirU, "%s.png", playerUUID.replaceAll("-", ""));

            if (data == null && !Shared.isBlank(playerName))
                data = readFile(_dirN, "%s.png", playerName);

            if (data != null)
                skin.put(data, ImageUtils.judgeSkinType(data));
        });
        return skin;
    }

    private byte[] readFile(Path file) {
        byte[] contents;
        if ((contents = Shared.readFile(file.toFile(), null, null)) != null && ImageUtils.validateData(contents))
            return contents;
        return null;
    }

    private byte[] readFile(File dir, String filename) {
        byte[] contents;
        if ((contents = Shared.readFile(new File(dir, filename), null, null)) != null && ImageUtils.validateData(contents))
            return contents;
        return null;
    }

    public static void setSelectedSkin(String playerName, Path skinPath) {
        selectedSkins.put(playerName, skinPath);
    }

    public static Path getSelectedSkin(String playerName) {
        Path path = selectedSkins.get(playerName);
        if (path != null)
            return path;
        if (!Shared.isBlank(playerName)) {
            Path defaultSkin = Path.of(".", "cachedImages", "skins", playerName + ".png");
            if (defaultSkin.toFile().isFile())
                return defaultSkin;
        }
        return null;
    }
    private byte[] readFile(File dir, String filename, Object... args) {
        return readFile(dir, String.format(filename, args));
    }
    public CachedSkinProvider withFilter(Function<ByteBuffer, ByteBuffer> filter) {
        _filter = filter;
        return this;
    }
}