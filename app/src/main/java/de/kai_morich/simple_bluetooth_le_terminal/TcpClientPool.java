package de.kai_morich.simple_bluetooth_le_terminal;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

public class TcpClientPool {
    private HashSet<TcpClientHandler> ThreadsHashSet = new HashSet<TcpClientHandler>();

    public void add(TcpClientHandler t) {
        this.ThreadsHashSet.add(t);
    }

    public void remove(@NotNull TcpClientHandler t) {
        this.ThreadsHashSet.remove(t);
    }

    public void write(@NotNull byte[] ba) throws IOException {
        for (TcpClientHandler tch : this.ThreadsHashSet) {
            tch.write(ba);
        }
    }
}
