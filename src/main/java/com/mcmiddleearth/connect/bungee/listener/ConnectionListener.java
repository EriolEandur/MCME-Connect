/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.connect.bungee.listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.bungee.vanish.VanishHandler;
import github.scarsz.discordsrv.dependencies.google.common.collect.Iterables;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 *
 * @author Eriol_Eandur
 */
public class ConnectionListener implements Listener {

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            ProxiedPlayer player = event.getPlayer();
            if(!VanishHandler.isPvSupport()) {
                sendJoinMessage(player,false);
            } else {
                VanishHandler.join(player);
            }
        }, 5, TimeUnit.SECONDS);
    }
    
    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if(!VanishHandler.isPvSupport()) {
            sendLeaveMessage(player,false);
        } else {
            VanishHandler.quit(player);
        }
    }
    
    @EventHandler
    public void handleLegacyPlayers(ServerConnectEvent event) {
        if(event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            if(!ConnectBungeePlugin.isLegacyRedirectEnabled()) {
                return;
            }
            if(ConnectBungeePlugin.getLegacyPlayers().contains(event.getPlayer().getUniqueId())
                    && event.getTarget().getName().equals(ConnectBungeePlugin.getLegacyRedirectFrom())) {
                event.setTarget(ProxyServer.getInstance().getServerInfo(ConnectBungeePlugin.getLegacyRedirectTo()));
            }
        }
    }
    
    public static void sendJoinMessage(ProxiedPlayer player, boolean fake) {
        ProxyServer.getInstance().getPlayers().stream()
                .filter(p -> !VanishHandler.isPvSupport() 
                          || !fake 
                          || !p.hasPermission(Permission.VANISH_SEE))
                .forEach(p -> {
                p.sendMessage(new ComponentBuilder(player.getName()+" joined the MCME-Network.")
                                            .color(ChatColor.YELLOW).create());
        });
        ProxiedPlayer other = ProxyServer.getInstance().getPlayers().iterator().next();
Logger.getGlobal().info("send Discord join Message to: "+other);
        if(other != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Discord");
            out.writeUTF(player.getName());
            out.writeUTF("join");
            other.getServer().getInfo().sendData(Channel.MAIN, out.toByteArray(),true);
        }
    }
    
    public static void sendLeaveMessage(ProxiedPlayer player, boolean fake) {
        ProxyServer.getInstance().getPlayers().stream()
                .filter(p -> !VanishHandler.isPvSupport() 
                          || !fake 
                          || !p.hasPermission(Permission.VANISH_SEE))
                .forEach(p -> {
                p.sendMessage(new ComponentBuilder(player.getName()+" left the MCME-Network.")
                                            .color(ChatColor.YELLOW).create());
        });
        ProxiedPlayer other = getOtherPlayer(player); 
Logger.getGlobal().info("send Discord leave Message to: "+other);
            if(other != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Channel.DISCORD);
            out.writeUTF(player.getName());
            out.writeUTF("leave");
            other.getServer().getInfo().sendData(Channel.MAIN, out.toByteArray(),false);
        }
    }

    private static ProxiedPlayer getOtherPlayer(ProxiedPlayer player) {
        Iterator<ProxiedPlayer> iterator = ProxyServer.getInstance().getPlayers().iterator();
        if(!iterator.hasNext()) return null;
        ProxiedPlayer other = iterator.next();
        if(other.equals(player)) {
            if(!iterator.hasNext()) return null;
            other = iterator.next();
        }
        return other;
    }
    

}
