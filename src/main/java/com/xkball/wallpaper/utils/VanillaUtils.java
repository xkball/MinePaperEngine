package com.xkball.wallpaper.utils;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.xkball.wallpaper.MinePaperEngine;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;

import java.util.concurrent.atomic.AtomicReference;

public class VanillaUtils {
    
    
    public static class ClientHandler {
        private static boolean asBG = false;
        private static WindowState state;
        private static WinDef.HWND mcWindow;
        private static AtomicReference<WinDef.HWND> workerW = new AtomicReference<>();
        private static boolean pauseOnLoseFocusOld = false;
        
        public static void copyFrameBufferColorTo(RenderTarget from, int to) {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, from.frameBufferId);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, to);
            GL30.glBlitFramebuffer(0,0,from.width,from.height,
                    0,0,from.width,from.height,
                    GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
        }
        
        public static void setWindowAsBG(int w, int h) {
            if(workerW.get() != null){
                User32.INSTANCE.DestroyWindow(workerW.get());
                workerW.set(null);
            }
            pauseOnLoseFocusOld = Minecraft.getInstance().options.pauseOnLostFocus;
            Minecraft.getInstance().options.pauseOnLostFocus = false;
            WinDef.HWND progman = User32.INSTANCE.FindWindow("Progman", null);
            User32.INSTANCE.SendMessageTimeout(progman, 0x052C, new WinDef.WPARAM(0), new WinDef.LPARAM(0),
                    0, 1000, null);
            
            User32.INSTANCE.EnumWindows((hwnd, data) -> {
                WinDef.HWND p = User32.INSTANCE.FindWindowEx(hwnd, null, "SHELLDLL_DefView", null);
                if (p != null) {
                    workerW.set(User32.INSTANCE.FindWindowEx(null, hwnd, "WorkerW", null));
                }
                return true;
            }, null);
            
            if (workerW.get() == null) {
                return;
            }
            User32.INSTANCE.ShowWindow(workerW.get(),1);
            mcWindow = null;
            mcWindow = User32.INSTANCE.FindWindow(null, MinePaperEngine.currentWindowTitle);
            if (mcWindow == null) {
                return;
            }
            
            User32.INSTANCE.SetParent(mcWindow, workerW.get());
            
            int style = User32.INSTANCE.GetWindowLong(mcWindow, WinUser.GWL_STYLE);
            style &= ~WinUser.WS_OVERLAPPEDWINDOW;
            style |= 0x8000000;
            User32.INSTANCE.SetWindowLong(mcWindow, WinUser.GWL_STYLE, style);
            
            User32.INSTANCE.SetWindowPos(mcWindow, null, 0, 0, w, h,
                    WinUser.SWP_NOZORDER | WinUser.SWP_NOACTIVATE | WinUser.SWP_SHOWWINDOW);
            User32.INSTANCE.SetFocus(null);
            Minecraft.getInstance().setScreen(null);
            asBG = true;
        }
        
        public static boolean isWindowAsBG(){
            return asBG;
        }
        
        public static void cancelWindowAsBG(){
            if (mcWindow == null) {
                return;
            }
            Minecraft.getInstance().options.pauseOnLostFocus = pauseOnLoseFocusOld;
            int style = User32.INSTANCE.GetWindowLong(mcWindow, WinUser.GWL_STYLE);
            style |= WinUser.WS_OVERLAPPEDWINDOW;
            style &= ~0x8000000;
            User32.INSTANCE.SetWindowLong(mcWindow, WinUser.GWL_STYLE, style);
            User32.INSTANCE.SetWindowPos(mcWindow, null, 0,0, 800, 600,
                    WinUser.SWP_FRAMECHANGED | WinUser.SWP_SHOWWINDOW);
            WinDef.HWND desktop = User32.INSTANCE.GetDesktopWindow();
            User32.INSTANCE.SetParent(mcWindow, desktop);
            
            if(workerW.get() != null){
                User32.INSTANCE.ShowWindow(workerW.get(),0);
                workerW.set(null);
            }
            
            User32.INSTANCE.RedrawWindow(null, null, null,
                    new WinDef.DWORD(WinUser.RDW_INVALIDATE | WinUser.RDW_ALLCHILDREN | WinUser.RDW_UPDATENOW));
//            WinDef.HWND progman = User32.INSTANCE.FindWindow("Progman", null);
//            User32.INSTANCE.SendMessageTimeout(progman, 0x052C, new WinDef.WPARAM(0), new WinDef.LPARAM(0),
//                    WinUser.SMTO_NORMAL, 1000, null);
            asBG = false;
        }
        
        public static void switchWindowState(){
            if(state == null) state = WindowState.current();
            if(isWindowAsBG()){
                state.apply();
            }
            else {
                state = WindowState.current();
                WindowState.withASWallpaper().apply();
            }
        }
    }
}
