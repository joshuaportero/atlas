package dev.portero.atlas.bootstrap;

public interface AtlasModule {

    String id();

    default void load(ModuleContext context) {
    }

    default void enable(ModuleContext context) {
    }

    default void disable(ModuleContext context) {
    }
}
