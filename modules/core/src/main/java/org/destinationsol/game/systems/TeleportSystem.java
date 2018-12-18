/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.destinationsol.game.systems;

import org.destinationsol.game.SolGame;
import org.destinationsol.game.SolObject;
import org.destinationsol.game.UpdateAwareSystem;
import org.destinationsol.game.abilities.Teleport;
import org.destinationsol.game.ship.SolShip;

public class TeleportSystem implements UpdateAwareSystem {
    @Override
    public void update(SolGame game, float timeStep) {
        for (SolObject object : game.getObjectManager().getObjects()) {
            if (object instanceof SolShip) {
                SolShip ship = (SolShip) object;
                if (ship.getAbility() instanceof Teleport) {
                    ((Teleport)ship.getAbility()).maybeTeleport(game, ship);
                }
            }
        }
    }
}
