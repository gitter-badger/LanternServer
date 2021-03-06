/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://github.com/LanternPowered>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.lanternpowered.server.network.vanilla.message.type.play;

import org.lanternpowered.server.network.message.Message;

/**
 * This message is send when the player jumps with a vehicle.
 */
public class MessagePlayInPlayerVehicleJump implements Message {

    private final boolean jump;
    private final float powerProgress;

    public MessagePlayInPlayerVehicleJump(boolean jump, float powerProgress) {
        this.powerProgress = powerProgress;
        this.jump = jump;
    }

    /**
     * Gets the progress of strength (charge) bar, scales between 0 and 1.
     * 
     * <p>This value will only return something greater then 0 if the player
     * is riding a horse and the new jump state ({@link #isJumping()} returns {@code false}.
     * Which means that the player released the jump button.</p>
     * 
     * @return the power progress
     */
    public float getPowerProgress() {
        return this.powerProgress;
    }

    /**
     * Gets the jumping state.
     * 
     * @return the jumping state
     */
    public boolean isJumping() {
        return this.jump;
    }

}
