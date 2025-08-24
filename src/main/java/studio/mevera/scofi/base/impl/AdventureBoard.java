package studio.mevera.scofi.base.impl;

import studio.mevera.scofi.Scofi;
import studio.mevera.scofi.animation.core.Animation;
import studio.mevera.scofi.base.BoardBase;
import studio.mevera.scofi.base.BoardUpdate;
import studio.mevera.scofi.base.ModernBoardAdapter;
import studio.mevera.scofi.entity.Body;
import studio.mevera.scofi.entity.Line;
import studio.mevera.scofi.entity.Title;
import studio.mevera.scofi.util.FastReflection;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static studio.mevera.scofi.Scofi.ADVENTURE_SUPPORT;

/**
 * Board implementation for modern Minecraft using Adventure {@link Component} lines.
 * <p>
 * Handles rich text, animations, and compatibility with Adventure API. Caches animations for title and lines to preserve state
 * across updates. Converts legacy text to components if needed. Used internally by Scofi for modern board rendering.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     AdventureBoard board = new AdventureBoard(scofi, player, adapter);
 *     board.updateTitle(Component.text("Title"));
 *     board.updateLine(0, Component.text("Line1"));
 * </pre>
 */
@Getter
public class AdventureBoard extends BoardBase<Component> {
    
    private static final MethodHandle COMPONENT_METHOD;
    private static final Object EMPTY_COMPONENT;
    
    static {
        
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        
        try {
            if (ADVENTURE_SUPPORT) {
                Class<?> paperAdventure = Class.forName("io.papermc.paper.adventure.PaperAdventure");
                Method method = paperAdventure.getDeclaredMethod("asVanilla", Component.class);
                COMPONENT_METHOD = lookup.unreflect(method);
                EMPTY_COMPONENT = COMPONENT_METHOD.invoke(Component.empty());
            } else {
                Class<?> craftChatMessageClass = FastReflection.obcClass("util.CraftChatMessage");
                COMPONENT_METHOD = lookup.unreflect(craftChatMessageClass.getMethod("fromString", String.class));
                EMPTY_COMPONENT = Array.get(COMPONENT_METHOD.invoke(""), 0);
            }
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }
    
    private final ModernBoardAdapter adapter;
    
    // Cache for animations to preserve their state
    private Animation<Component> cachedTitleAnimation;
    private final Map<Integer, Animation<Component>> cachedLineAnimations = new HashMap<>();
    
    /**
     * Constructs a new AdventureBoard for the given player and adapter.
     * @param scofi the Scofi manager instance
     * @param player the player
     * @param adapter the modern board adapter
     */
    public AdventureBoard(Scofi scofi, Player player, ModernBoardAdapter adapter) {
        super(scofi, player);
        this.adapter = adapter;
        
        if (!update()) {
            scofi.getLogger().warning("Hey! Looks like you're using legacy text for your board instead of components," +
                    " legacy text has been automatically converted for now. It is better that you use kyori adventure for modern minecraft.");
        }
    }
    
    /**
     * Gets the update logic from the adapter.
     * @return board update action
     */
    @Override
    public BoardUpdate getUpdate() {
        return adapter.getBoardUpdate();
    }
    
    /**
     * Sends a line change packet for the given score.
     * @param score the line score
     * @throws Throwable if packet sending fails
     */
    @Override
    protected void sendLineChange(int score) throws Throwable {
        Component line = getLineByScore(score);
        
        sendTeamPacket(score, TeamMode.UPDATE, line, null);
    }
    
    /**
     * Converts a Component to a Minecraft chat component for packets.
     * @param component the Adventure component
     * @return Minecraft chat component
     * @throws Throwable if conversion fails
     */
    @Override
    protected Object toMinecraftComponent(Component component) throws Throwable {
        if (component == null) {
            return EMPTY_COMPONENT;
        }
        
        // If the server isn't running adventure natively, we convert the component to legacy text
        // and then to a Minecraft chat component
        if (!ADVENTURE_SUPPORT) {
            String legacy = serializeLine(component);
            
            return Array.get(COMPONENT_METHOD.invoke(legacy), 0);
        }
        
        return COMPONENT_METHOD.invoke(component);
    }
    
    /**
     * Serializes a Component to legacy text.
     * @param value the Adventure component
     * @return legacy string
     */
    @Override
    protected String serializeLine(Component value) {
        return LegacyComponentSerializer.legacySection().serialize(value);
    }
    
    /**
     * Gets an empty Adventure component for blank lines.
     * @return empty component
     */
    @Override
    protected Component emptyLine() {
        return Component.empty();
    }
    
    /**
     * Updates the board, handling animations and caching for title and lines.
     * @return true if update succeeded, false if legacy fallback was used
     */
    @Override
    public boolean update() {
        try {
            // Get new title from adapter
            Title<Component> newTitle = adapter.getTitle(getPlayer());
            
            // Handle title animation caching
            if (newTitle.loadAnimation().isPresent()) {
                Animation<Component> newTitleAnimation = newTitle.loadAnimation().get();
                
                if (cachedTitleAnimation == null || !isSameAnimation(cachedTitleAnimation, newTitleAnimation)) {
                    cachedTitleAnimation = newTitleAnimation;
                } else {
                    ((Title.TitleImplementation<Component>) newTitle).setTitleAnimation(cachedTitleAnimation);
                }
            } else {
                cachedTitleAnimation = null;
            }
            
            // Mark title for update before fetching
            if (newTitle instanceof Title.TitleImplementation) {
                ((Title.TitleImplementation<Component>) newTitle).markForUpdate();
            }
            
            // Update title ONCE
            Component titleContent = newTitle.get().orElseThrow(IllegalStateException::new);
            updateTitle(titleContent);
            
            // Get body from adapter
            Body<Component> body = adapter.getBody(getPlayer());
            
            // Clear any previous lines to ensure clean state
            if (body.getLines().size() != this.getLines().size()) {
                // Size changed, need full update
                updateLines(); // Clear first
            }
            
            // Handle body/lines with animation caching
            for (Line<Component> line : body.getLines()) {
                int index = line.getIndex();
                Component content;
                
                // Handle line animation caching
                if (line.getAnimation() != null) {
                    Animation<Component> lineAnimation = line.getAnimation();
                    Animation<Component> cachedAnimation = cachedLineAnimations.get(index);
                    
                    if (cachedAnimation == null || !isSameAnimation(cachedAnimation, lineAnimation)) {
                        cachedLineAnimations.put(index, lineAnimation);
                        line.setAnimation(lineAnimation);
                    } else {
                        line.setAnimation(cachedAnimation);
                    }
                    
                    content = line.fetchContent();
                } else {
                    cachedLineAnimations.remove(index);
                    content = line.getContent();
                }
                
                updateLine(index, content);
            }
            
            // Clean up cached animations for lines that no longer exist
            int bodySize = body.getLines().size();
            cachedLineAnimations.entrySet().removeIf(entry -> entry.getKey() >= bodySize);
            
            return true;
        } catch (ClassCastException e) {
            // Fallback for legacy text
            Body<?> body = adapter.getBody(getPlayer());
            for (Line<?> line : body.getLines()) {
                updateLine(line.getIndex(), deserialize(line.fetchContent()));
            }
            updateTitle(deserialize(adapter.getTitle(getPlayer()).get().orElseThrow(IllegalStateException::new)));
            return false;
        }
    }
    
    private Component deserialize(Object o) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(o.toString());
    }
    
    /**
     * Helper method to check if two animations are the same (type and original content).
     * @param cached cached animation
     * @param newAnim new animation
     * @return true if same
     */
    private boolean isSameAnimation(Animation<Component> cached, Animation<Component> newAnim) {
        // Check if they're the same type and have the same original content
        return cached.getClass().equals(newAnim.getClass())
                && Objects.equals(cached.getOriginal(), newAnim.getOriginal());
    }
}