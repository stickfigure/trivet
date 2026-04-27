package com.voodoodyne.trivet;

import java.io.ObjectStreamClass;
import java.util.concurrent.ConcurrentHashMap;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy.Default;

class MysteryExceptionClassFactory {
    private static final ByteBuddy BUILDER = new ByteBuddy();
    private static final ClassLoader LOADER = new ClassLoader(MysteryException.class.getClassLoader()) {};

    private static ConcurrentHashMap<String, Class<?>> CACHE = new ConcurrentHashMap<>();

    public static Class<?> get(final ObjectStreamClass desc) {
        return CACHE.computeIfAbsent(desc.getName(), key -> create(desc));
    }

    private static Class<?> create(final ObjectStreamClass desc) {
        final String originalName = desc.getName();
        final long serialVersionUID = desc.getSerialVersionUID();

        return BUILDER
            .subclass(MysteryException.class, Default.DEFAULT_CONSTRUCTOR)
            .name(originalName)
            .defineField("serialVersionUID", long.class,
                Visibility.PRIVATE,
                Ownership.STATIC,
                FieldManifestation.FINAL
            )
            .value(serialVersionUID)
            .make()
            .load(LOADER, ClassLoadingStrategy.Default.INJECTION)
            .getLoaded();
    }
}