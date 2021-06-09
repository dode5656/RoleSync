package io.github.dode5656.rolesync.utilities;

public enum Message {

    PREFIX("prefix"),
    PLAYER_ONLY("messages.player-only"),
    NO_PERM_CMD("messages.no-perm-cmd"),
    VERIFY_REQUEST("messages.verifyRequest"),
    VERIFIED_MINECRAFT("messages.verifiedMinecraft"),
    VERIFIED_DISCORD("messages.verifiedDiscord"),
    DENIED_MINECRAFT("messages.deniedMinecraft"),
    DENIED_DISCORD("messages.deniedDiscord"),
    ERROR("messages.error"),
    BAD_NAME("messages.badName"),
    TOO_LONG_MC("messages.tooLongMC"),
    TOO_LONG_DISCORD("messages.tooLongDiscord"),
    ALREADY_VERIFIED("messages.alreadyVerified"),
    INVALID_SERVER_ID("messages.invalid-server-id"),
    DEFAULT_VALUE("messages.default-value"),
    CONFIG_RELOADED("messages.config-reloaded"),
    CONFIG_RELOAD_ERROR("messages.config-reload-error"),
    UPDATED_ROLES("messages.updated-roles"),
    PLUGIN_DISABLED("messages.plugin-disabled"),
    USAGE("messages.usage"),
    PLAYER_NOT_FOUND("messages.player-not-found"),
    NOT_SYNCED("messages.not-synced"),
    UNSYNCED_SUCCESSFULLY("messages.unsynced-successfully"),
    DM_FAILED("messages.dm-failed"),
    REQUEST_REPLY("messages.request-reply");
    private final String key;

    Message(String s) {
        this.key = s;
    }

    public String getMessage() {
        return key;
    }

}
