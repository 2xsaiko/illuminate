package net.dblsaiko.illuminate.client.api;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;

/**
 * An oriented light which projects a texture.
 */
public interface Light {
    /**
     * Identifier for the texture that should be projected.
     */
    @NotNull
    Identifier tex();

    /**
     * Returns the origin point of the light.
     */
    @NotNull
    Vector3fc pos();

    /**
     * Returns the yaw rotation of the light.
     */
    float yaw();

    /**
     * Returns the pitch rotation of the light.
     */
    default float pitch() {
        return 0f;
    }

    /**
     * Returns the roll rotation of the light.
     */
    default float roll() {
        return 0f;
    }

    /**
     * Returns the vertical field of view (FoV) of the light.
     */
    default float fov() {
        return 90f;
    }

    /**
     * Returns the aspect ratio of the light (X:Y).
     */
    default float aspect() {
        return 1f;
    }

    /**
     * Returns the distance of the near clip plane of the projection from the origin.
     */
    default float near() {
        return 0.8660254f;
    }

    /**
     * Returns the distance of the far clip plane of the projection from the origin.
     */
    default float far() {
        return 50f;
    }

    /**
     * Prepare the light for rendering. This gets called before the world gets drawn from the light's perspective.
     */
    default void prepare(float delta) {
    }
}
