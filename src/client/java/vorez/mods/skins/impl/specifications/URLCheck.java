package vorez.mods.skins.impl.specifications;

public enum URLCheck {
    // fails
    FAIL,
    OFFLINE,
    NO_RESPONSE,
    INVALID_URL,
    ERROR_404,
    UNSTABLE_CONNECTION,
    HTTP_DENIED,
    NO_INTERNET,

    // success
    STABLE_CONNECTION,
    SUCCESS,

    // invalid
    INVALID_SKIN,
    INVALID_CAPE,

    // default
    IS_EXAMPLE_COM,
    CUSTOM_SERVER_DISABLE;
}
