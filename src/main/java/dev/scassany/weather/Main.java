package dev.scassany.weather;

import imgui.ImDrawData;
import imgui.ImGui;
import imgui.app.Application;

public class Main {
    public static void main(String[] args) {
        Window window = new Window(new ImGuiLayer());
        window.init();
        window.run();
        window.destroy();
    }


}
