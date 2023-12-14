package com.github.zly2006.reden;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;

public class Sounds {
    public final static SoundEvent THE_WORLD = new SoundEvent(Reden.identifier("the_world"));
    public final static SoundEvent MIKU_MIKU = new SoundEvent(Reden.identifier("miku_miku"));
    public static void init() {
        Registry.register(Registry.SOUND_EVENT, THE_WORLD.getId(), THE_WORLD);
        Registry.register(Registry.SOUND_EVENT, MIKU_MIKU.getId(), MIKU_MIKU);
    }
}
