/*
 * Copyright (C) 2019 MCME
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.connect.bungee.Handler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

/**
 *
 * @author Eriol_Eandur
 */
public class TitleHandler {
    
    public static boolean handle(String server, String recipient, String title, String subtitle, 
                                 int intro, int show, int extro, int delay) {
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            Collection<ServerInfo> servers = new HashSet<>();
            if(server.equals(Channel.ALL)) {
                servers = ProxyServer.getInstance().getServers().values();
            } else {
                servers.add(ProxyServer.getInstance().getServerInfo(server));
            }
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Channel.TITLE);
            out.writeUTF(recipient);
            out.writeUTF(title);
            out.writeUTF(subtitle);
            out.writeInt(intro);
            out.writeInt(show);
            out.writeInt(extro);
            servers.forEach(info ->
                    info.sendData(Channel.MAIN, out.toByteArray(),false));
        }, delay, TimeUnit.MILLISECONDS);
        return true;
    }
}
