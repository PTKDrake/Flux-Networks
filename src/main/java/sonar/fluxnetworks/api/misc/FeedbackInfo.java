package sonar.fluxnetworks.api.misc;

import sonar.fluxnetworks.api.text.FluxTranslate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum FeedbackInfo {
    NONE(null),
    REJECT(FluxTranslate.REJECT),
    NO_OWNER(FluxTranslate.NO_OWNER),
    NO_ADMIN(FluxTranslate.NO_ADMIN),
    NO_SPACE(FluxTranslate.NO_SPACE),
    HAS_CONTROLLER(FluxTranslate.HAS_CONTROLLER),
    INVALID_USER(FluxTranslate.INVALID_USER),
    ILLEGAL_PASSWORD(FluxTranslate.ILLEGAL_PASSWORD),
    BANNED_LOADING(FluxTranslate.BANNED_LOADING),
    PASSWORD_REQUIRE(null),
    SUCCESS(null),
    SUCCESS_2(null); // Sometimes we need another success to compare to the first one

    @Nullable
    private final FluxTranslate localization;

    FeedbackInfo(@Nullable FluxTranslate localization) {
        this.localization = localization;
    }

    public boolean isValid() {
        return this != NONE;
    }

    @Nonnull
    public String getText() {
        return localization == null ? "" : localization.t();
    }
}