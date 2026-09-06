package vorez.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import vorez.mods.skins.impl.specifications.URLCheck;
import net.minecraft.network.chat.Component;
import vorez.mods.skins.impl.ConfigOptions;
import vorez.mods.skins.init.fabric.FabricOfflineSkinsReloaded;

import java.net.URI;
import java.net.URISyntaxException;

public final class URLValidator {

    private static String lastCheckedUrl = "";
    private static boolean lastRequireAuto = false;
    private static URLCheck lastResult = URLCheck.INVALID_URL;
    private static long lastCheckTime = 0;

    private URLValidator() {
    }

    public static URLCheck validate(String url, boolean requireAuto) {
        if (url == null || url.isBlank() || url.matches(".*[\\p{L}&&[^a-zA-Z]].*")) {
            return URLCheck.INVALID_URL;
        }
        if (!SmartInternetCheck.isOnline()) {
            return URLCheck.NO_INTERNET;
        }
        long currentTime = System.currentTimeMillis();

        if (url.equals(lastCheckedUrl)
                && requireAuto == lastRequireAuto
                && currentTime - lastCheckTime < 150) {
            return lastResult;
        }
        lastCheckedUrl = url;
        lastRequireAuto = requireAuto;
        lastCheckTime = currentTime;

        if (requireAuto && !url.contains("%auto%")) {
            lastResult = URLCheck.FAIL;
            return lastResult;
        }
        try {
            String parsed = requireAuto
                    ? url.replace("%auto%", "testplayer")
                    : url;

            URI uri = new URI(parsed);
            String scheme = uri.getScheme();

            if (scheme == null ||
                    (!scheme.equalsIgnoreCase("http")
                            && !scheme.equalsIgnoreCase("https"))) {

                lastResult = URLCheck.INVALID_URL;
                return lastResult;
            }

            if (scheme.equalsIgnoreCase("http")) {
                ConfigOptions config = FabricOfflineSkinsReloaded.loadConfigSnapshot();

                if (config != null && !config.allowHTTP) {
                    lastResult = URLCheck.HTTP_DENIED;
                    return lastResult;
                }
            }

            String host = uri.getHost();

            if (host == null || host.isBlank()) {
                lastResult = URLCheck.INVALID_URL;
                return lastResult;
            }
            if (host.equalsIgnoreCase("example.com")
                    || host.endsWith(".example.com")) {

                lastResult = URLCheck.IS_EXAMPLE_COM;
                return lastResult;
            }
            lastResult = URLCheck.SUCCESS;
            return lastResult;

        } catch (URISyntaxException e) {

            lastResult = URLCheck.INVALID_URL;
            return lastResult;
        }
    }
    public static void showCheckResult(String title, URLCheck result) {
        Minecraft client = Minecraft.getInstance();

        Component message = switch (result) {
            case NO_INTERNET ->
                    Component.translatable("no.internet");
            case HTTP_DENIED ->
                    Component.translatable("use.of.http.is.denied");

            case CUSTOM_SERVER_DISABLE ->
                    Component.translatable("error.custom-server-disabled");

            case IS_EXAMPLE_COM ->
                    Component.translatable("toast.offlineskins.example");

            case SUCCESS, STABLE_CONNECTION ->
                    Component.translatable("toast.offlineskins.success");

            case UNSTABLE_CONNECTION ->
                    Component.translatable("toast.offlineskins.unstable");

            case FAIL ->
                    Component.translatable("toast.offlineskins.fail");

            case INVALID_URL ->
                    Component.translatable("toast.offlineskins.invalid_url");

            case ERROR_404 ->
                    Component.translatable("toast.offlineskins.error404");

            case OFFLINE ->
                    Component.translatable("toast.offlineskins.offline");

            case NO_RESPONSE ->
                    Component.translatable("toast.offlineskins.no_response");

            case INVALID_SKIN ->
                    Component.translatable("toast.offlineskins.invalid_skin");

            case INVALID_CAPE ->
                    Component.translatable("toast.offlineskins.invalid_cape");
        };

        client.getToastManager().addToast(
                net.minecraft.client.gui.components.toasts.SystemToast.multiline(
                        client,
                        SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                        Component.literal(title),
                        message
                )
        );
    }
}