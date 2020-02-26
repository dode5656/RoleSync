package io.github.dode5656.donorrole.utilities;

public enum Message {

    PREFIX("prefix"),
    PLAYERONLY("messages.player-only"),
    NOPERMCMD("messages.no-perm-cmd"),
    VERIFYREQUEST("messages.verifyRequest"),
    VERIFIEDMINECRAFT("messages.verifiedMinecraft"),
    VERIFIEDDISCORD("messages.verifiedDiscord"),
    DENIEDMINECRAFT("messages.deniedMinecraft"),
    DENIEDDISCORD("messages.deniedDiscord"),
    ERROR("messages.error"),
    BADNAME("messages.badName"),
    TOOLONGMC("messages.tooLongMC"),
    TOOLONGDISCORD("messages.tooLongDiscord"),
    ALREADYVERIFIED("messages.alreadyVerified"),
    INVALIDSERVERID("messages.invalid-server-id"),
    DEFAULTVALUE("messages.default-value"),
    CONFIGRELOADED("messages.config-reloaded"),
    CONFIGRELOADERROR("messages.config-reload-error"),
    UPDATEDROLES("messages.updated-roles");
    private String key;

    Message(String s) {
        this.key = s;
    }

    public String getMessage() {
        return key;
    }

}
