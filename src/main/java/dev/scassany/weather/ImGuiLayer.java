package dev.scassany.weather;

import imgui.ImGui;
import imgui.flag.ImGuiSliderFlags;

public class ImGuiLayer {
    private boolean showText = false;
    float[] scale = new float[1];
    float[] transform = new float[2];
    public void imgui() {
        ImGui.begin("Cool Window");
        scale[0] = Window.scale;
        transform[0] = Window.xx;
        transform[1] = Window.yy;

        ImGui.dragFloat("Scale", scale, 0.1f, 0, 100);
        ImGui.dragFloat2("Position", transform, 0.1f);
        Window.scale = scale[0];
        Window.xx = transform[0];
        Window.yy = transform[1];
        ImGui.end();
    }
}