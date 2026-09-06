package vorez.mods.skins.impl;

import com.mojang.logging.LogUtils;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.gui.controllers.LabelController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import vorez.network.URLConnectionValidator;
import vorez.network.URLValidator;
import vorez.mods.skins.impl.specifications.CustomServersList;
import vorez.mods.skins.impl.specifications.URLCheck;
import vorez.mods.skins.init.fabric.FabricOfflineSkinsReloaded;
import net.minecraft.Util;
import vorez.mods.skins.providers.CachedCapeProvider;
import vorez.mods.skins.providers.CachedSkinProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

public final class YaclSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    private YaclSettings() {
    }

    private static List<String> scanCachedImages(Path directory) {
        List<String> result = new ArrayList<>();

        if (!Files.exists(directory)) {
            return result;
        }

        try (var paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(java.util.Locale.ROOT)
                            .endsWith(".png"))
                    .map(path -> directory.relativize(path).toString()
                            .replace('\\', '/'))
                    .sorted(Comparator.naturalOrder())
                    .forEach(result::add);
        } catch (IOException e) {
            LOGGER.error("[OfflineSkins Reloaded] Failed to scan cached images.", e);
        }

        return result;
    }

    public static Screen createConfigScreen(Screen parentScreen) {
        ConfigOptions options = FabricOfflineSkinsReloaded.loadConfigSnapshot();
        ConfigOptions defaults = new ConfigOptions().defaultOptions();

        if (options.customServersList != CustomServersList.CUSTOM) {
            options.linkCustomServerSkin = options.customServersList.getSkinUrl();
            options.linkCustomServerCape = options.customServersList.getCapeUrl();
        }

        Option<Boolean> disablePlayerHeads = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.DisablePlayerHeads"))
                .description(OptionDescription.of(Component.translatable("tooltip.DisablePlayerHeads")))
                .binding(
                        defaults.disablePlayerHeads,
                        () -> options.disablePlayerHeads,
                        value -> options.disablePlayerHeads = value
                )
                .controller(TickBoxControllerBuilder::create)
                .build();

        Option<Boolean> useMojang = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.Mojang"))
                .description(OptionDescription.of(Component.translatable("tooltip.use.Mojang")))
                .binding(
                        defaults.useMojang,
                        () -> options.useMojang,
                        value -> options.useMojang = value
                )
                .controller(TickBoxControllerBuilder::create)
                .build();

        Option<Boolean> useCrafatar = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.Crafatar"))
                .description(OptionDescription.of(Component.translatable("tooltip.use.Crafatar")))
                .binding(
                        defaults.useCrafatar,
                        () -> options.useCrafatar,
                        value -> options.useCrafatar = value
                )
                .controller(TickBoxControllerBuilder::create)
                .build();

        Option<Boolean> allowHdSkins = Option.<Boolean>createBuilder()
                .name(Component.translatable("allow.HD"))
                .description(OptionDescription.of(Component.translatable("tooltip.allow.HD")))
                .binding(
                        defaults.allowHdSkins,
                        () -> options.allowHdSkins,
                        value -> options.allowHdSkins = value
                )
                .controller(TickBoxControllerBuilder::create)
                .build();

        Option<Boolean> useCustomServer = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.use.CustomServer"))
                .description(OptionDescription.of(Component.translatable("tooltip.use.CustomServer")))
                .binding(
                        defaults.useCustomServer,
                        () -> options.useCustomServer,
                        value -> options.useCustomServer = value
                )
                .controller(TickBoxControllerBuilder::create)
                .build();

        Option<Boolean> allowHTTP = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.allowHTTP"))
                .description(OptionDescription.of(Component.translatable("tooltip.allowHTTP")))
                .binding(
                        defaults.allowHTTP,
                        () -> options.allowHTTP,
                        value -> options.allowHTTP = value
                )
                .controller(TickBoxControllerBuilder::create)
                .build();
        Option<Boolean> smartInternetCheck = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.SmartInternetCheck"))
                .description(OptionDescription.of(Component.translatable("tooltip.SmartInternetCheck")))
                .binding(
                        defaults.smartInternetCheck,
                        () -> options.smartInternetCheck,
                        value -> options.smartInternetCheck = value
                )
                .controller(TickBoxControllerBuilder::create)
                .build();
        ButtonOption reloadProviders = ButtonOption.createBuilder()
                .name(Component.translatable("button.offlineskins.reload"))
                .text(Component.translatable("button.offlineskins.reload.t"))
                .description(OptionDescription.of(
                        Component.translatable("tooltip.offlineskins.reload")
                ))
                .action((screen, option) -> {
                    FabricOfflineSkinsReloaded.reloadRuntime();

                    CompletableFuture.delayedExecutor(
                            1,
                            java.util.concurrent.TimeUnit.SECONDS
                    ).execute(() -> Minecraft.getInstance().execute(() ->
                            Minecraft.getInstance().getToastManager().addToast(
                                    SystemToast.multiline(
                                            Minecraft.getInstance(),
                                            SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                            Component.translatable("offlineskins.reload.notification"),
                                            Component.translatable("toast.offlineskins.reload.success")
                                    )
                            )
                    ));
                })
                .build();

        Option<String> customServerSkinUrl = Option.<String>createBuilder()
                .name(Component.translatable("option.offlineskins-reloaded.link_custom_server_skin"))
                .description(OptionDescription.of(
                        Component.translatable("tooltip.offlineskins-reloaded.link_custom_server_skin")
                ))
                .binding(
                        defaults.linkCustomServerSkin,
                        () -> options.linkCustomServerSkin,
                        value -> {
                            if (!options.customServersList.isElyBy()) {
                                options.linkCustomServerSkin = value;
                            }
                        }
                )
                .controller(StringControllerBuilder::create)
                .build();

        Option<String> customServerCapeUrl = Option.<String>createBuilder()
                .name(Component.translatable("option.offlineskins-reloaded.link_custom_server_cape"))
                .description(OptionDescription.of(
                        Component.translatable("tooltip.offlineskins-reloaded.link_custom_server_cape")
                ))
                .binding(
                        defaults.linkCustomServerCape,
                        () -> options.linkCustomServerCape,
                        value -> {
                            if (!options.customServersList.isElyBy()) {
                                options.linkCustomServerCape = value;
                            }
                        }
                )
                .controller(StringControllerBuilder::create)
                .build();

        Option<CustomServersList> customServerPreset = Option.<CustomServersList>createBuilder()
                .name(Component.translatable("option.use.server.from.list"))
                .description(OptionDescription.of(
                        Component.translatable("tooltip.use.server.from.list")
                ))
                .binding(
                        defaults.customServersList,
                        () -> options.customServersList,
                        preset -> {
                            options.customServersList = preset;

                            if (preset != CustomServersList.CUSTOM) {
                                options.linkCustomServerSkin = preset.getSkinUrl();
                                options.linkCustomServerCape = preset.getCapeUrl();

                                customServerSkinUrl.requestSet(options.linkCustomServerSkin);
                                customServerCapeUrl.requestSet(options.linkCustomServerCape);
                            }
                        }
                )
                .controller(option -> EnumControllerBuilder.create(option)
                        .enumClass(CustomServersList.class))
                .build();

        ButtonOption checkSkin = ButtonOption.createBuilder()
                .name(Component.translatable("button.offlineskins.check_skin"))
                .text(Component.translatable("button.offlineskins.check"))
                .description(OptionDescription.of(Component.translatable("tooltip.offlineskins.check_skin")))
                .action((screen, option) -> CompletableFuture
                        .supplyAsync(() -> {
                            URLCheck local = URLValidator.validate(options.linkCustomServerSkin, true);
                            if (local != URLCheck.SUCCESS) {
                                return local;
                            }
                            return URLConnectionValidator.checkConnection(
                                    options.linkCustomServerSkin,
                                    true,
                                    options.useCustomServer,
                                    false
                            );
                        })
                        .whenComplete((result, error) -> Minecraft.getInstance().execute(() -> {
                            URLCheck status = error == null && result != null
                                    ? result
                                    : URLCheck.NO_RESPONSE;

                            URLValidator.showCheckResult("Skin", status);
                        }))
                )
                .build();

        ButtonOption checkCape = ButtonOption.createBuilder()
                .name(Component.translatable("button.offlineskins.check_cape"))
                .text(Component.translatable("button.offlineskins.check"))
                .description(OptionDescription.of(Component.translatable("tooltip.offlineskins.check_cape")))
                .action((screen, option) -> CompletableFuture
                        .supplyAsync(() -> {
                            URLCheck local = URLValidator.validate(options.linkCustomServerCape, true);
                            if (local != URLCheck.SUCCESS) {
                                return local;
                            }
                            return URLConnectionValidator.checkConnection(
                                    options.linkCustomServerCape,
                                    true,
                                    options.useCustomServer,
                                    true
                            );
                        })
                        .whenComplete((result, error) -> Minecraft.getInstance().execute(() -> {
                            URLCheck status = error == null && result != null
                                    ? result
                                    : URLCheck.NO_RESPONSE;

                            URLValidator.showCheckResult("Cape", status);
                        }))
                )
                .build();

        ButtonOption openDirectoryCachedImages = ButtonOption.createBuilder()
                .name(Component.translatable("button.offlineskins.open_cachedimages"))
                .text(Component.translatable("button.offlineskins.open_cachedimages.t"))
                .description(OptionDescription.of(
                        Component.translatable("tooltip.offlineskins.open_cachedimages")
                ))
                .action((screen, option) -> {
                    Path path = Paths.get(".", "cachedImages");

                    try {
                        Files.createDirectories(path);
                        Util.getPlatform().openPath(path);
                    } catch (IOException e) {
                        LOGGER.error("[OfflineSkins Reloaded] Failed to open cached images directory.", e);
                    }
                })
                .build();

        Path cachedImages = Paths.get(".", "cachedImages");

        List<String> cachedSkins = scanCachedImages(
                cachedImages.resolve("skins")
        );

        ButtonOption recacheSkins = ButtonOption.createBuilder()
                .name(Component.translatable("button.offlineskins.recacheSkin.players"))
                .text(Component.translatable("button.offlineskins.recacheSkin.players.t"))
                .description(OptionDescription.of(
                        Component.translatable("tooltip.offlineskins.recacheSkin.players")
                ))
                .action((screen, option) -> FabricOfflineSkinsReloaded.recacheSkins())
                .build();

        ButtonOption recacheCapes = ButtonOption.createBuilder()
                .name(Component.translatable("button.offlineskins.recacheCape.players"))
                .text(Component.translatable("button.offlineskins.recacheCape.players.t"))
                .description(OptionDescription.of(
                        Component.translatable("tooltip.offlineskins.recacheCape.players")
                ))
                .action((screen, option) -> FabricOfflineSkinsReloaded.recacheCapes())
                .build();

        ButtonOption recacheSkin = ButtonOption.createBuilder()
                .name(Component.translatable("button.offlineskins.recacheSkin"))
                .text(Component.translatable("button.offlineskins.recacheSkin.t"))
                .description(OptionDescription.of(
                        Component.translatable("tooltip.offlineskins.recacheSkin")
                ))
                .action((screen, option) -> FabricOfflineSkinsReloaded.recacheSkin())
                .build();

        ButtonOption recacheCape = ButtonOption.createBuilder()
                .name(Component.translatable("button.offlineskins.recacheCape"))
                .text(Component.translatable("button.offlineskins.recacheCape.t"))
                .description(OptionDescription.of(
                        Component.translatable("tooltip.offlineskins.recacheCape")
                ))
                .action((screen, option) -> FabricOfflineSkinsReloaded.recacheCape())
                .build();

        Option<Boolean> useCachedSkin = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.use.cachedSkin.players"))
                .description(OptionDescription.of(Component.translatable("tooltip.use.cachedSkin.players")))
                .binding(
                        defaults.useCachedSkin,
                        () -> options.useCachedSkin,
                        value -> options.useCachedSkin = value
                )
                .controller(TickBoxControllerBuilder::create)
                .build();

        OptionGroup cachedSkinsOptions;

        if (!cachedSkins.isEmpty()) {
            OptionGroup.Builder cachedSkinsGroup = OptionGroup.createBuilder()
                    .name(Component.translatable("category.offlineskins-reloaded.cachedSkins"))
                    .collapsed(false)
                    .option(useCachedSkin);

            Minecraft client = Minecraft.getInstance();

            String profileName = null;
            if (client.player != null) {
                profileName = PlayerProfile
                        .wrapGameProfile(client.player.getGameProfile())
                        .getPlayerName();
            }

            Path selectedSkin = profileName != null
                    ? CachedSkinProvider.getSelectedSkin(profileName)
                    : null;

            for (String skin : cachedSkins) {
                Path skinPath = cachedImages.resolve("skins").resolve(skin);

                boolean selected = selectedSkin != null && selectedSkin.equals(skinPath);

                ButtonOption skinOption = ButtonOption.createBuilder()
                        .name(Component.literal(skin))
                        .text(Component.translatable(
                                selected
                                        ? "button.offlineskins.selected"
                                        : "button.offlineskins.select"
                        ))
                        .description(OptionDescription.of(Component.translatable(
                                "use.this.skin")))
                        .action((screen, option) -> {
                            Minecraft currentClient = Minecraft.getInstance();

                            if (currentClient.player != null) {
                                String currentProfileName = PlayerProfile
                                        .wrapGameProfile(currentClient.player.getGameProfile())
                                        .getPlayerName();

                                CachedSkinProvider.setSelectedSkin(
                                        currentProfileName,
                                        skinPath
                                );
                                FabricOfflineSkinsReloaded.recacheSkin();
                            }
                        })
                        .build();

                cachedSkinsGroup.option(skinOption);
            }

            cachedSkinsOptions = cachedSkinsGroup.build();
        } else {
            OptionGroup.Builder cachedSkinsGroup = OptionGroup.createBuilder()
                    .name(Component.translatable("cached.images.empty"))
                    .collapsed(false);

            Option<Component> emptyList = Option.<Component>createBuilder()
                    .name(Component.empty())
                    .description(OptionDescription.EMPTY)
                    .stateManager(StateManager.createImmutable(
                            Component.translatable("empty.list.skins")
                    ))
                    .customController(LabelController::new)
                    .build();

            cachedSkinsGroup.option(emptyList);
            cachedSkinsOptions = cachedSkinsGroup.build();
        }

        List<String> cachedCapes = scanCachedImages(
                cachedImages.resolve("capes")
        );

        Option<Boolean> useCachedCape = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.use.cachedCape.players"))
                .description(OptionDescription.of(Component.translatable("tooltip.use.cachedCape.players")))
                .binding(
                        defaults.useCachedCape,
                        () -> options.useCachedCape,
                        value -> options.useCachedCape = value
                )
                .controller(TickBoxControllerBuilder::create)
                .build();

        OptionGroup cachedCapesOptions;

        if (!cachedCapes.isEmpty()) {
            OptionGroup.Builder cachedCapesGroup = OptionGroup.createBuilder()
                    .name(Component.translatable("category.offlineskins-reloaded.cachedCapes"))
                    .collapsed(false)
                    .option(useCachedCape);

            Minecraft client = Minecraft.getInstance();

            String profileName = null;
            if (client.player != null) {
                profileName = PlayerProfile
                        .wrapGameProfile(client.player.getGameProfile())
                        .getPlayerName();
            }

            Path selectedCape = profileName != null
                    ? CachedCapeProvider.getSelectedCape(profileName)
                    : null;

            for (String cape : cachedCapes) {
                Path capePath = cachedImages.resolve("capes").resolve(cape);

                boolean selected = selectedCape != null && selectedCape.equals(capePath);

                ButtonOption capeOption = ButtonOption.createBuilder()
                        .name(Component.literal(cape))
                        .text(Component.translatable(
                                selected
                                        ? "button.offlineskins.selected"
                                        : "button.offlineskins.select"
                        ))
                        .description(OptionDescription.of(Component.translatable("use.this.cape")))
                        .action((screen, option) -> {
                            Minecraft currentClient = Minecraft.getInstance();

                            if (currentClient.player != null) {
                                String currentProfileName = PlayerProfile
                                        .wrapGameProfile(currentClient.player.getGameProfile())
                                        .getPlayerName();

                                CachedCapeProvider.setSelectedCape(
                                        currentProfileName,
                                        capePath
                                );
                                FabricOfflineSkinsReloaded.recacheCape();
                            }
                        })
                        .build();

                cachedCapesGroup.option(capeOption);
            }

            cachedCapesOptions = cachedCapesGroup.build();
        } else {
            OptionGroup.Builder cachedCapesGroup = OptionGroup.createBuilder()
                    .name(Component.translatable("cached.images.empty"))
                    .collapsed(false);

            Option<Component> emptyList = Option.<Component>createBuilder()
                    .name(Component.empty())
                    .description(OptionDescription.EMPTY)
                    .stateManager(StateManager.createImmutable(
                            Component.translatable("empty.list.capes")
                    ))
                    .customController(LabelController::new)
                    .build();

            cachedCapesGroup.option(emptyList);
            cachedCapesOptions = cachedCapesGroup.build();
        }

        OptionGroup generalGroup = OptionGroup.createBuilder()
                .name(Component.translatable("category.offlineskins-reloaded.general"))
                .collapsed(false)
                .option(disablePlayerHeads)
                .option(smartInternetCheck)
                .option(useMojang)
                .option(useCrafatar)
                .option(reloadProviders)
                .build();

        OptionGroup customServerGroup = OptionGroup.createBuilder()
                .name(Component.translatable("options.CustomServer"))
                .collapsed(true)
                .option(useCustomServer)
                .option(allowHTTP)
                .option(allowHdSkins)
                .option(reloadProviders)
                .option(customServerPreset)
                .option(customServerSkinUrl)
                .option(checkSkin)
                .option(customServerCapeUrl)
                .option(checkCape)
                .build();
        OptionGroup recacheGroup = OptionGroup.createBuilder()
                .name(Component.translatable("options.recache"))
                .collapsed(false)
                .option(recacheSkins)
                .option(recacheCapes)
                .build();

        ConfigCategory.Builder OfflineSkinsMain = ConfigCategory.createBuilder()
                .name(Component.translatable("menu.offlineskins-reloaded.dressing.room"))
                .group(
                        OptionGroup.createBuilder()
                                .name(Component.translatable("category.offlineskins-reloaded.dressing.room"))
                                .collapsed(false)
                                .option(openDirectoryCachedImages)
                                .option(recacheSkin)
                                .option(recacheCape)
                                .build()
                );
        ConfigCategory.Builder FAQBuilder = ConfigCategory.createBuilder()
                .name(Component.translatable("menu.faqs"))
                .group(
                        OptionGroup.createBuilder()
                                .name(Component.translatable("menu.faqs.skin.cape.title"))
                                .collapsed(true)
                                .option(
                                        Option.<Component>createBuilder()
                                                .name(Component.empty())
                                                .description(OptionDescription.EMPTY)
                                                .stateManager(StateManager.createImmutable(
                                                        Component.translatable("menu.faqs.skin.cape.answer")
                                                ))
                                                .customController(LabelController::new)
                                                .build()
                                )
                                .build()
                )
                .group(
                        OptionGroup.createBuilder()
                                .name(Component.translatable("menu.faqs.skin.cape.wrong.title"))
                                .collapsed(true)
                                .option(
                                        Option.<Component>createBuilder()
                                                .name(Component.empty())
                                                .description(OptionDescription.EMPTY)
                                                .stateManager(StateManager.createImmutable(
                                                        Component.translatable("menu.faqs.skin.cape.wrong.answer")
                                                ))
                                                .customController(LabelController::new)
                                                .build()
                                )
                                .build()
                )
                .group(
                        OptionGroup.createBuilder()
                                .name(Component.translatable("menu.faqs.default.skin.cape.title"))
                                .collapsed(true)
                                .option(
                                        Option.<Component>createBuilder()
                                                .name(Component.empty())
                                                .description(OptionDescription.EMPTY)
                                                .stateManager(StateManager.createImmutable(
                                                        Component.translatable("menu.faqs.default.skin.cape.answer")
                                                ))
                                                .customController(LabelController::new)
                                                .build()
                                )
                                .build()
                )
                .group(
                        OptionGroup.createBuilder()
                                .name(Component.translatable("menu.faqs.cached.skin.cape.can.see.others.players.title"))
                                .collapsed(true)
                                .option(
                                        Option.<Component>createBuilder()
                                                .name(Component.empty())
                                                .description(OptionDescription.EMPTY)
                                                .stateManager(StateManager.createImmutable(
                                                        Component.translatable("menu.faqs.cached.skin.cape.can.see.others.players.answer")
                                                ))
                                                .customController(LabelController::new)
                                                .build()
                                )
                                .build()
                )
                .group(
                        OptionGroup.createBuilder()
                                .name(Component.translatable("menu.faqs.red.steve.cape.title"))
                                .collapsed(true)
                                .option(
                                        Option.<Component>createBuilder()
                                                .name(Component.empty())
                                                .description(OptionDescription.EMPTY)
                                                .stateManager(StateManager.createImmutable(
                                                        Component.translatable("menu.faqs.red.steve.cape.answer")
                                                ))
                                                .customController(LabelController::new)
                                                .build()
                                )
                                .build()
                )
                .group(
                        OptionGroup.createBuilder()
                                .name(Component.translatable("menu.faqs.cache.title"))
                                .collapsed(true)
                                .option(
                                        Option.<Component>createBuilder()
                                                .name(Component.empty())
                                                .description(OptionDescription.EMPTY)
                                                .stateManager(StateManager.createImmutable(
                                                        Component.translatable("menu.faqs.cache.answer")
                                                ))
                                                .customController(LabelController::new)
                                                .build()
                                )
                                .build()
                )
                .group(
                        OptionGroup.createBuilder()
                                .name(Component.translatable("menu.faqs.server.title"))
                                .collapsed(true)
                                .option(
                                        Option.<Component>createBuilder()
                                                .name(Component.empty())
                                                .description(OptionDescription.EMPTY)
                                                .stateManager(StateManager.createImmutable(
                                                        Component.translatable("menu.faqs.server.answer")
                                                ))
                                                .customController(LabelController::new)
                                                .build()
                                )
                                .build()
                )
                .group(
                        OptionGroup.createBuilder()
                                .name(Component.translatable("menu.faqs.links.title"))
                                .collapsed(true)
                                .option(
                                        Option.<Component>createBuilder()
                                                .name(Component.empty())
                                                .description(OptionDescription.EMPTY)
                                                .stateManager(StateManager.createImmutable(
                                                        Component.translatable("menu.faqs.links.answer")
                                                ))
                                                .customController(LabelController::new)
                                                .build()
                                )
                                .option(
                                        ButtonOption.createBuilder()
                                                .name(Component.literal("Discord"))
                                                .text(Component.literal(""))
                                                .description(OptionDescription.of(
                                                        Component.translatable("menu.faqs.discord.answer")
                                                ))
                                                .action((screen, option) -> Util.getPlatform().openUri(
                                                        "https://discord.gg/pNabgQ6Bw"
                                                ))
                                                .build()
                                )
                                .option(
                                        ButtonOption.createBuilder()
                                                .name(Component.literal("Modrinth"))
                                                .text(Component.literal(""))
                                                .description(OptionDescription.of(
                                                        Component.translatable("menu.faqs.modrinth.answer")
                                                ))
                                                .action((screen, option) -> Util.getPlatform().openUri(
                                                        "https://modrinth.com/mod/offlineskins-reloaded"
                                                ))
                                                .build()
                                )
                                .option(
                                        ButtonOption.createBuilder()
                                                .name(Component.literal("GitHub"))
                                                .text(Component.literal(""))
                                                .description(OptionDescription.of(
                                                        Component.translatable("menu.faqs.github.answer")
                                                ))
                                                .action((screen, option) -> Util.getPlatform().openUri(
                                                        "https://github.com/VoreZ78/OfflineSkins-Reloaded"
                                                ))
                                                .build()
                                )
                                .build()
                )
                .group(
                        OptionGroup.createBuilder()
                                .name(Component.translatable("menu.faqs.bug.report.title"))
                                .collapsed(true)
                                .option(
                                        Option.<Component>createBuilder()
                                                .name(Component.empty())
                                                .description(OptionDescription.EMPTY)
                                                .stateManager(StateManager.createImmutable(
                                                        Component.translatable("menu.faqs.bug.report.answer")
                                                ))
                                                .customController(LabelController::new)
                                                .build()
                                )
                                .option(
                                        ButtonOption.createBuilder()
                                                .name(Component.literal(
                                                        "https://github.com/VoreZ78/OfflineSkins-Reloaded/issues"
                                                ))
                                                .text(Component.translatable("button.offlineskins.copy"))
                                                .action((screen, option) -> {
                                                    Minecraft client = Minecraft.getInstance();

                                                    client.keyboardHandler.setClipboard(
                                                            "https://github.com/VoreZ78/OfflineSkins-Reloaded/issues"
                                                    );

                                                    client.getToastManager().addToast(
                                                            SystemToast.multiline(
                                                                    client,
                                                                    SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                                                    Component.translatable("copied.issues.URL"),
                                                                    Component.literal("")
                                                            )
                                                    );
                                                })
                                                .build()
                                )
                                .option(
                                        ButtonOption.createBuilder()
                                                .name(Component.literal("GitHub"))
                                                .text(Component.literal(""))
                                                .description(OptionDescription.of(
                                                        Component.translatable("menu.faqs.github.answer")
                                                ))
                                                .action((screen, option) -> Util.getPlatform().openUri(
                                                        "https://github.com/VoreZ78/OfflineSkins-Reloaded/issues"
                                                ))
                                                .build()
                                )
                                .option(
                                        ButtonOption.createBuilder()
                                                .name(Component.literal("Discord"))
                                                .text(Component.literal(""))
                                                .description(OptionDescription.of(
                                                        Component.translatable("menu.faqs.discord.answer")
                                                ))
                                                .action((screen, option) -> Util.getPlatform().openUri(
                                                        "https://discord.gg/pNabgQ6Bw"
                                                ))
                                                .build()
                                )
                                .build()
                );

        ConfigCategory FAQ = FAQBuilder.build();

        if (cachedSkinsOptions != null)
            OfflineSkinsMain.group(cachedSkinsOptions);

        if (cachedCapesOptions != null)
            OfflineSkinsMain.group(cachedCapesOptions);

        ConfigCategory OfflineSkinsDressingRoom = OfflineSkinsMain.build();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("menu.offlineskins-reloaded.config"))
                .category(OfflineSkinsDressingRoom)
                .save(() -> {
                    if (options.customServersList.isElyBy()) {
                        options.linkCustomServerSkin = options.customServersList.getSkinUrl();
                        options.linkCustomServerCape = options.customServersList.getCapeUrl();
                    }
                    FabricOfflineSkinsReloaded.saveConfigFile(options);
                    FabricOfflineSkinsReloaded.reloadRuntime();
                })
                .category(
                        ConfigCategory.createBuilder()
                                .name(Component.translatable("menu.offlineskins-reloaded.config"))
                                .group(generalGroup)
                                .group(customServerGroup)
                                .group(recacheGroup)
                                .build()
                )
                .category(FAQ)
                .build()
                .generateScreen(parentScreen);
    }
}