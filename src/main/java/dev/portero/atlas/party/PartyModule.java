package dev.portero.atlas.party;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.player.ProfileManager;

public final class PartyModule implements AtlasModule {

    @Override
    public String id() {
        return "party";
    }

    @Override
    public void load(ModuleContext context) {
        context.services().register(PartyService.class, new PartyService(
                context.service(ProfileManager.class), context.service(EventBus.class)));
    }
}
