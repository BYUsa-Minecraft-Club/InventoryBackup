package edu.byu.minecraft.invbackup.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public enum LogType {
    JOIN,
    QUIT,
    DEATH,
    WORLD_CHANGE,
    FORCE;

    public static final Codec<LogType> CODEC = Codec.STRING.xmap(LogType::valueOf, LogType::name);
}
