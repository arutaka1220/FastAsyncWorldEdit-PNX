/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command.argument;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.world.World;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

public class MaskConverter implements ArgumentConverter<Mask> {

    public static void register(WorldEdit worldEdit, CommandManager commandManager) {
        commandManager.registerConverter(Key.of(Mask.class), new MaskConverter(worldEdit));
    }

    private final WorldEdit worldEdit;

    private MaskConverter(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    @Override
    public ConversionResult<Mask> convert(String argument, InjectedValueAccess context) {
        Actor actor = context.injectedValue(Key.of(Actor.class))
            .orElseThrow(() -> new IllegalStateException("No actor"));
        ParserContext parserContext = new ParserContext();
        parserContext.setActor(actor);
        if (actor instanceof Entity) {
            Extent extent = ((Entity) actor).getExtent();
            if (extent instanceof World) {
                parserContext.setWorld((World) extent);
            }
        }
        parserContext.setSession(worldEdit.getSessionManager().get(actor));
        try {
            return SuccessfulConversion.fromSingle(
                worldEdit.getMaskFactory().parseFromInput(argument, parserContext)
            );
        } catch (InputParseException e) {
            return FailedConversion.from(e);
        }
    }

    @Override
    public String describeAcceptableArguments() {
        return "any mask";
    }
}
