package com.xkball.wallpaper;

import com.xkball.wallpaper.utils.TheSystemTray;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(MinePaperEngine.MODID)
public class MinePaperEngine {

    public static final String MODID = "mine_paper_engine";

    private static final Logger LOGGER = LogUtils.getLogger();
    public static String currentWindowTitle = "";
    public static TheSystemTray tray;

    public MinePaperEngine(IEventBus modEventBus, ModContainer modContainer) {
        //modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }


}
