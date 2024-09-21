package dev.scassany.weather;
import de.matthiasmann.twl.utils.PNGDecoder;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.ByteBuffer;

import static java.lang.Math.max;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memFree;



public class Window {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private static boolean mousePressed, updated = false;
    private static double smx, smy;
    private static double mx, my;


    private String glslVersion = null;
    private long windowPtr;
    private ImGuiLayer imguiLayer;

    public Window(ImGuiLayer layer) {
        imguiLayer = layer;
    }

    public void init() {
        initWindow();
        initImGui();
        imGuiGlfw.init(windowPtr, true);
        imGuiGl3.init(glslVersion);
    }

    public void destroy() {
        ImGui.destroyContext();
        Callbacks.glfwFreeCallbacks(windowPtr);
        glfwDestroyWindow(windowPtr);
        glfwTerminate();
    }

    private void initWindow() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() ) {
            System.out.println("Unable to initialize GLFW");
            System.exit(-1);
        }

        glslVersion = "#version 130";
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        windowPtr = glfwCreateWindow(800, 800, "My Window", NULL, NULL);

        if (windowPtr == NULL) {
            System.out.println("Unable to create window");
            System.exit(-1);
        }

        glfwMakeContextCurrent(windowPtr);
        glfwSwapInterval(1);
        glfwShowWindow(windowPtr);

        GL.createCapabilities();

        GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                System.out.println("Moved! " + xpos + "," + ypos);
                if(mousePressed && !updated){
                    smx = xpos;
                    smy = ypos;
                    updated = true;
                }
                mx = xpos;
                my = ypos;

            }
        };
        GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                System.out.println("Scrolled! " + yoffset);
                if(yoffset > 0){
                    scale++;
                }else{
                    scale--;
                }
            }
        };
        GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                System.out.println("Button! " + button);
                if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS){
                    mousePressed = true;
                }
                if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE){
                    mousePressed = false;
                    if(updated)updatePos();
                    updated = false;

                    smx = 0;
                    smy = 0;

                }
            }
        };

        glfwSetScrollCallback(windowPtr, scrollCallback);
        glfwSetMouseButtonCallback(windowPtr, mouseButtonCallback);
        glfwSetCursorPosCallback(windowPtr, cursorPosCallback);
    }

    private void initImGui() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
    }

    public void run() {
        Texture tex = null;
        try {
            tex = loadTexture("/usa_road_map.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!glfwWindowShouldClose(windowPtr)) {
            glClearColor(0.1f, 0.09f, 0.1f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            imGuiGlfw.newFrame();
            imGuiGl3.newFrame();
            ImGui.newFrame();

            //Base Layer Rendering (Map, etc)
            render(tex);

            //GUI Rendering
            imguiLayer.imgui();

            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
                final long backupWindowPtr = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
                ImGui.updatePlatformWindows();
                ImGui.renderPlatformWindowsDefault();
                GLFW.glfwMakeContextCurrent(backupWindowPtr);
            }

            GLFW.glfwSwapBuffers(windowPtr);
            GLFW.glfwPollEvents();
        }
        glDeleteTextures(tex.getId());
    }

    public static float scale = 0;
    public static float xx,yy,zz;

    private float normMousePos(double pos){
        return (float)(2*(pos/800) - 1);
    }


    private void updatePos(){
        xx = xx + (normMousePos(mx) - normMousePos(smx));
        yy = yy + (normMousePos(smy) - normMousePos(my));
    }

    private void render(Texture tex) {
        //GL11.glColor3ub((byte)40,(byte)200,(byte)40);
        float scaleFactor = 1.0f + scale * 0.1f;
        glEnable(GL_TEXTURE_2D);
        glPushMatrix();
        glScalef(scaleFactor, scaleFactor, 1f);
        //glTranslatef(-xx*.1f,-yy*.1f,0);

       if(mousePressed && updated){
           glTranslated(xx + (normMousePos(mx) - normMousePos(smx)) , yy+ (normMousePos(smy) - normMousePos(my)), 0);
       }else{
           glTranslated(xx , yy, 0);
       }
        glBegin(GL_QUADS);
        {
            glTexCoord2f(0.0f, 0.0f);
            glVertex2f(-1f, 1f);

            glTexCoord2f(1.0f, 0.0f);
            glVertex2f(1, 1f);

            glTexCoord2f(1.0f, 1.0f);
            glVertex2f(1, -1f);

            glTexCoord2f(0.0f, 1.0f);
            glVertex2f(-1f, -1);
        }
        glEnd();
        glPopMatrix();
        glDisable(GL_TEXTURE_2D);

    }

    public static Texture loadTexture(String fileName) throws IOException {

        //load png file
        PNGDecoder decoder = new PNGDecoder(Window.class.getResourceAsStream(fileName));

        //create a byte buffer big enough to store RGBA values
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());

        //decode
        decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);

        //flip the buffer so its ready to read
        buffer.flip();

        //create a texture
        int id = glGenTextures();

        //bind the texture
        glBindTexture(GL_TEXTURE_2D, id);

        //tell opengl how to unpack bytes
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        //set the texture parameters, can be GL_LINEAR or GL_NEAREST
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        //upload texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        // Generate Mip Map
        glGenerateMipmap(GL_TEXTURE_2D);

        return new Texture(id);
    }



}