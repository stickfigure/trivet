package com.voodoodyne.trivet;

import java.io.ObjectStreamClass;
import java.util.concurrent.ConcurrentHashMap;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy.Default;

class MysteryExceptionClassFactory {
    private static final ByteBuddy BUILDER = new ByteBuddy();

    private static ConcurrentHashMap<String, Class<?>> CACHE = new ConcurrentHashMap<>();

    public static Class<?> get(final ObjectStreamClass desc) {
        return CACHE.computeIfAbsent(desc.getName() + "#" + desc.getSerialVersionUID(), key -> create(desc));
    }

    private static Class<?> create(final ObjectStreamClass desc) {
        final String originalName = desc.getName();
        final long serialVersionUID = desc.getSerialVersionUID();

        final ClassLoader loader = new ClassLoader(Thread.currentThread().getContextClassLoader()) {};

        return BUILDER
            .subclass(MysteryException.class, Default.DEFAULT_CONSTRUCTOR)
            .name(originalName)
            .defineField("serialVersionUID", long.class,
                Visibility.PRIVATE,
                net.bytebuddy.description.modifier.Ownership.STATIC,
                net.bytebuddy.description.modifier.FieldManifestation.FINAL
            )
            .value(serialVersionUID)
            .make()
            .load(loader, ClassLoadingStrategy.Default.INJECTION)
            .getLoaded();
    }
}