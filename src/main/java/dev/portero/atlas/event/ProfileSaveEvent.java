package dev.portero.atlas.event;

import dev.portero.atlas.data.profile.Profile;

public final class ProfileSaveEvent implements AtlasEvent {

    private final Profile profile;

    public ProfileSaveEvent(Profile profile) {
        this.profile = profile;
    }

    public Profile profile() {
        return this.profile;
    }
}
