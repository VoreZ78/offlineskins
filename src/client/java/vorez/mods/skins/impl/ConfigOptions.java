package vorez.mods.skins.impl;

import vorez.mods.skins.impl.specifications.CustomServersList;

public class ConfigOptions {
    public boolean useCachedSkin;
    public boolean useCachedCape;
    public String HintMojang;
    public boolean smartInternetCheck;
    public boolean useMojang;
    public String HintCustomServer;
    public boolean useCustomServer;
    public boolean allowHTTP;
    public CustomServersList customServersList;
    public String linkCustomServerSkin;
    public String linkCustomServerCape;
    public String HintDisablePlayerHeads;
    public boolean allowHdSkins;
    public boolean disablePlayerHeads;
    public String HintCrafatar;
    public boolean useCrafatar;

    public ConfigOptions defaultOptions() {
        useCachedSkin = true;
        useCachedCape = true;

        HintMojang = "Uses the official Minecraft skin and cape provider";
        useMojang = true;

        smartInternetCheck = true;
        useCustomServer = false;
        allowHTTP = true;
        allowHdSkins = false;

        customServersList = CustomServersList.CUSTOM;
        HintCustomServer = "Custom URLs for skins and capes";

        linkCustomServerSkin = "https://example.com/skins/%auto%";
        linkCustomServerCape = "https://example.com/capes/%auto%";

        HintDisablePlayerHeads = "Disables the heads in the tab menu";
        disablePlayerHeads = false;

        HintCrafatar = "Uses Crafatar as a fallback skin/cape provider";
        useCrafatar = false;

        return this;
    }

    public boolean validate() {
        boolean any = false;

        if (customServersList == null) {
            customServersList = CustomServersList.CUSTOM;
            any = true;
        }
        if (linkCustomServerSkin == null) {
            linkCustomServerSkin = "https://example.com/skins/%auto%";
            any = true;
        }
        if (linkCustomServerCape == null) {
            linkCustomServerCape = "https://example.com/capes/%auto%";
            any = true;
        }
        return any;
    }
}
