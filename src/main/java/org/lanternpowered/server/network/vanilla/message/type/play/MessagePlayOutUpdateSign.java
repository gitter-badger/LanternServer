package org.lanternpowered.server.network.vanilla.message.type.play;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.lanternpowered.server.network.message.Message;
import org.spongepowered.api.text.Text;

import com.flowpowered.math.vector.Vector3i;

public final class MessagePlayOutUpdateSign implements Message {

    private final Vector3i position;
    private final Text[] lines;

    /**
     * Creates the update sign message.
     * 
     * @param position the position
     * @param lines the lines
     */
    public MessagePlayOutUpdateSign(Vector3i position, Text[] lines) {
        this.position = checkNotNull(position, "position");
        checkNotNull(lines, "lines");
        checkArgument(lines.length == 4, "lines length must be 4");
        this.lines = lines;
    }

    /**
     * Gets the sign position of this message.
     * 
     * @return the position
     */
    public Vector3i getPosition() {
        return this.position;
    }

    /**
     * Gets the lines.
     * 
     * @return the lines
     */
    public Text[] getLines() {
        return this.lines;
    }
}
