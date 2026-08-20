package dev.portero.atlas.bootstrap;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class ModuleRegistry {

    private final ModuleContext context;
    private final List<AtlasModule> modules = new ArrayList<>();

    public ModuleRegistry(ModuleContext context) {
        this.context = context;
    }

    public void register(AtlasModule module) {
        this.modules.add(module);
    }

    public void loadAll() {
        for (AtlasModule module : this.modules) {
            log.info("Loading module {}", module.id());
            module.load(this.context);
        }
    }

    public void enableAll() {
        for (AtlasModule module : this.modules) {
            log.info("Enabling module {}", module.id());
            module.enable(this.context);
        }
    }

    public void disableAll() {
        for (int index = this.modules.size() - 1; index >= 0; index--) {
            AtlasModule module = this.modules.get(index);
            try {
                log.info("Disabling module {}", module.id());
                module.disable(this.context);
            } catch (Exception exception) {
                log.error("Failed to disable module {}", module.id(), exception);
            }
        }
    }
}
