package dev.portero.atlas.menu.api;

@FunctionalInterface
public interface ClickHandler {

    void handle(ClickContext context);
}
