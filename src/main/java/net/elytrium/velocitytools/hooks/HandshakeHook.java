/*
 * Copyright (C) 2021 - 2023 Elytrium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.elytrium.velocitytools.hooks;

import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.HandshakeSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.HandshakePacket;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import net.elytrium.commons.utils.reflection.ReflectionException;
import net.elytrium.fastprepare.PreparedPacketFactory;
import net.elytrium.velocitytools.Settings;

public class HandshakeHook extends HandshakePacket implements PacketHook {

  private static Method GET_STATE_FOR_PROTOCOL;
  private static Field CONNECTION_FIELD;
  private static boolean DISABLE_INVALID_PROTOCOL;

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    try {
      int nextStatus = this.getNextStatus();
      if (DISABLE_INVALID_PROTOCOL && GET_STATE_FOR_PROTOCOL.invoke(handler, nextStatus) == null) {
        return true;
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new ReflectionException(e);
    }

    return super.handle(handler);
  }

  @Override
  public Supplier<MinecraftPacket> getHook() {
    return HandshakeHook::new;
  }

  @Override
  public Class<? extends MinecraftPacket> getType() {
    return HandshakePacket.class;
  }

  @Override
  public Class<? extends MinecraftPacket> getHookClass() {
    return this.getClass();
  }

  public static void reload(PreparedPacketFactory factory) {
    try {
      GET_STATE_FOR_PROTOCOL = HandshakeSessionHandler.class.getDeclaredMethod("getStateForProtocol", int.class);
      GET_STATE_FOR_PROTOCOL.setAccessible(true);

      CONNECTION_FIELD = HandshakeSessionHandler.class.getDeclaredField("connection");
      CONNECTION_FIELD.setAccessible(true);

      DISABLE_INVALID_PROTOCOL = Settings.IMP.TOOLS.DISABLE_INVALID_PROTOCOL;
    } catch (NoSuchMethodException | NoSuchFieldException e) {
      throw new ReflectionException(e);
    }
  }
}
