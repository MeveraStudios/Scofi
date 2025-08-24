package studio.mevera.scofi.util;

import org.bukkit.Bukkit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utility class for fast reflection and dynamic class/method access in Minecraft plugins.
 * <p>
 * Provides helpers for accessing NMS/OBC classes, handling repackaged server internals, enum values, inner classes,
 * and packet construction. Used throughout Scofi for compatibility and performance when interacting with server internals.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     Class<?> nmsClass = FastReflection.nmsClass("network.protocol", "Packet");
 *     Object enumValue = FastReflection.enumValueOf(enumClass, "VALUE");
 * </pre>
 */
public final class FastReflection {

    private static final String NM_PACKAGE = "net.minecraft";
    private static final String OBC_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS_PACKAGE = OBC_PACKAGE.replace("org.bukkit.craftbukkit", NM_PACKAGE + ".server");

    private static final MethodType VOID_METHOD_TYPE = MethodType.methodType(void.class);
    private static final boolean NMS_REPACKAGED = optionalClass(NM_PACKAGE + ".network.protocol.Packet").isPresent();
    private static final boolean MOJANG_MAPPINGS = optionalClass(NM_PACKAGE + ".network.chat.Component").isPresent();

    private static volatile Object theUnsafe;

    private FastReflection() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if the server is using repackaged NMS classes (1.17+).
     * @return true if repackaged
     */
    public static boolean isRepackaged() {
        return NMS_REPACKAGED;
    }

    /**
     * Gets the fully qualified NMS class name for a given package and class.
     * @param post1_17package package name (post 1.17)
     * @param className class name
     * @return fully qualified class name
     */
    public static String nmsClassName(String post1_17package, String className) {
        if (NMS_REPACKAGED) {
            String classPackage = post1_17package == null ? NM_PACKAGE : NM_PACKAGE + '.' + post1_17package;

            return classPackage + '.' + className;
        }

        return NMS_PACKAGE + '.' + className;
    }

    /**
     * Gets the NMS class for a given package and class name.
     * @param post1_17package package name (post 1.17)
     * @param className class name
     * @return NMS class
     * @throws ClassNotFoundException if not found
     */
    public static Class<?> nmsClass(String post1_17package, String className) throws ClassNotFoundException {
        return Class.forName(nmsClassName(post1_17package, className));
    }

    /**
     * Gets the NMS class for Spigot/Mojang mappings.
     * @param post1_17package package name
     * @param spigotClass Spigot class name
     * @param mojangClass Mojang class name
     * @return NMS class
     * @throws ClassNotFoundException if not found
     */
    public static Class<?> nmsClass(String post1_17package, String spigotClass, String mojangClass) throws ClassNotFoundException {
        return nmsClass(post1_17package, MOJANG_MAPPINGS ? mojangClass : spigotClass);
    }

    /**
     * Gets an optional NMS class for a given package and class name.
     * @param post1_17package package name
     * @param className class name
     * @return optional NMS class
     */
    public static Optional<Class<?>> nmsOptionalClass(String post1_17package, String className) {
        return optionalClass(nmsClassName(post1_17package, className));
    }

    /**
     * Gets the fully qualified OBC class name for a given class.
     * @param className class name
     * @return fully qualified OBC class name
     */
    public static String obcClassName(String className) {
        return OBC_PACKAGE + '.' + className;
    }

    /**
     * Gets the OBC class for a given class name.
     * @param className class name
     * @return OBC class
     * @throws ClassNotFoundException if not found
     */
    public static Class<?> obcClass(String className) throws ClassNotFoundException {
        return Class.forName(obcClassName(className));
    }

    /**
     * Gets an optional OBC class for a given class name.
     * @param className class name
     * @return optional OBC class
     */
    public static Optional<Class<?>> obcOptionalClass(String className) {
        return optionalClass(obcClassName(className));
    }

    /**
     * Gets an optional class by name.
     * @param className class name
     * @return optional class
     */
    public static Optional<Class<?>> optionalClass(String className) {
        try {
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets an enum value by name from a class.
     * @param enumClass enum class
     * @param enumName enum name
     * @return enum value
     */
    public static Object enumValueOf(Class<?> enumClass, String enumName) {
        return Enum.valueOf(enumClass.asSubclass(Enum.class), enumName);
    }

    /**
     * Gets an enum value by name or fallback ordinal from a class.
     * @param enumClass enum class
     * @param enumName enum name
     * @param fallbackOrdinal fallback ordinal
     * @return enum value
     */
    public static Object enumValueOf(Class<?> enumClass, String enumName, int fallbackOrdinal) {
        try {
            return enumValueOf(enumClass, enumName);
        } catch (IllegalArgumentException e) {
            Object[] constants = enumClass.getEnumConstants();
            if (constants.length > fallbackOrdinal) {
                return constants[fallbackOrdinal];
            }
            throw e;
        }
    }

    /**
     * Gets an inner class from a parent class matching a predicate.
     * @param parentClass parent class
     * @param classPredicate predicate to match
     * @return inner class
     * @throws ClassNotFoundException if not found
     */
    public static Class<?> innerClass(Class<?> parentClass, Predicate<Class<?>> classPredicate) throws ClassNotFoundException {
        for (Class<?> innerClass : parentClass.getDeclaredClasses()) {
            if (classPredicate.test(innerClass)) {
                return innerClass;
            }
        }
        throw new ClassNotFoundException("No class in " + parentClass.getCanonicalName() + " matches the predicate.");
    }

    /**
     * Gets an optional constructor MethodHandle for a class and method type.
     * @param declaringClass class
     * @param lookup method handles lookup
     * @param type method type
     * @return optional MethodHandle
     * @throws IllegalAccessException if not accessible
     */
    public static Optional<MethodHandle> optionalConstructor(Class<?> declaringClass, MethodHandles.Lookup lookup, MethodType type) throws IllegalAccessException {
        try {
            return Optional.of(lookup.findConstructor(declaringClass, type));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    /**
     * Finds a packet constructor for a class using MethodHandles or Unsafe.
     * @param packetClass packet class
     * @param lookup method handles lookup
     * @return PacketConstructor instance
     * @throws Exception if not found
     */
    public static PacketConstructor findPacketConstructor(Class<?> packetClass, MethodHandles.Lookup lookup) throws Exception {
        try {
            MethodHandle constructor = lookup.findConstructor(packetClass, VOID_METHOD_TYPE);
            return constructor::invoke;
        } catch (NoSuchMethodException | IllegalAccessException e) {
            // try below with Unsafe
        }

        if (theUnsafe == null) {
            synchronized (FastReflection.class) {
                if (theUnsafe == null) {
                    Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                    Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                    theUnsafeField.setAccessible(true);
                    theUnsafe = theUnsafeField.get(null);
                }
            }
        }

        MethodType allocateMethodType = MethodType.methodType(Object.class, Class.class);
        MethodHandle allocateMethod = lookup.findVirtual(theUnsafe.getClass(), "allocateInstance", allocateMethodType);
        return () -> allocateMethod.invoke(theUnsafe, packetClass);
    }

    /**
     * Functional interface for packet construction.
     */
    @FunctionalInterface
    public interface PacketConstructor {
        Object invoke() throws Throwable;
    }
}