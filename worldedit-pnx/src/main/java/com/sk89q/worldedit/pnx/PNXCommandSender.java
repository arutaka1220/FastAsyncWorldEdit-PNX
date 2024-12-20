/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.pnx;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.AbstractNonPlayerActor;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class PNXCommandSender extends AbstractNonPlayerActor {

    /**
     * One time generated ID.
     */
    private static final UUID DEFAULT_ID = UUID.fromString("a233eb4b-4cab-42cd-9fd9-7e7b9a3f74be");

    private final CommandSender sender;
    private final PNXWorldEditPlugin plugin;

    public PNXCommandSender(PNXWorldEditPlugin plugin, CommandSender sender) {
        checkNotNull(plugin);
        checkNotNull(sender);
        checkArgument(!(sender instanceof Player), "Cannot wrap a player");

        this.plugin = plugin;
        this.sender = sender;
    }

    @Override
    public UUID getUniqueId() {
        return DEFAULT_ID;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    @Deprecated
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            sender.sendMessage(part);
        }
    }

    @Override
    @Deprecated
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            sender.sendMessage("§d" + part);
        }
    }

    @Override
    @Deprecated
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            sender.sendMessage("§7" + part);
        }
    }

    @Override
    @Deprecated
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            sender.sendMessage("§c" + part);
        }
    }

    @Override
    public void print(Component component) {
        sender.sendMessage(WorldEditText.format(component, getLocale()).toString());
    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public boolean hasPermission(String perm) {
        return (!plugin.getLocalConfiguration().noOpPermissions && sender.isOp())
                || this.sender.hasPermission(perm);
    }

    //FAWE start
    @Override
    public void setPermission(String permission, boolean value) {
        this.sender.addAttachment(PNXWorldEditPlugin.getInstance(), permission, value);
    }
    //FAWE end

    @Override
    public void checkPermission(String permission) throws AuthorizationException {
        if (!hasPermission(permission)) {
            throw new AuthorizationException(permission);
        }
    }

    @Override
    public Locale getLocale() {
        return WorldEdit.getInstance().getConfiguration().defaultLocale;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKey() {
            @Nullable
            @Override
            public String getName() {
                return sender.getName();
            }

            @Override
            public boolean isActive() {
                //FAWE start - check if sender instanceof Entity, before returning true
                if (sender instanceof Entity) {
                    Entity entity = (Entity) sender;
                    return entity.isValid() && entity.isAlive();
                }
                //FAWE end
                return true;
            }

            @Override
            public boolean isPersistent() {
                return true;
            }

            @Override
            public UUID getUniqueId() {
                return DEFAULT_ID;
            }
        };
    }

}
