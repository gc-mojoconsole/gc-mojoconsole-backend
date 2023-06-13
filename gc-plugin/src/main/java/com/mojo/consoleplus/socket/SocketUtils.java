package com.mojo.consoleplus.socket;

import com.mojo.consoleplus.ConsolePlus;
import com.mojo.consoleplus.socket.packet.BasePacket;
import com.mojo.consoleplus.socket.packet.Packet;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.utils.JsonUtils;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Socket 工具类
public class SocketUtils {

    /**
     * 获取打包后的数据包
     * @param bPacket 数据包
     * @return 打包后的数据包
     */
    public static String getPacket(BasePacket bPacket) {
        Packet packet = new Packet();
        packet.type = bPacket.getType();
        packet.data = bPacket.getPacket();
        packet.packetID = UUID.randomUUID().toString();
        return JsonUtils.encode(packet);
    }

    /**
     * 获取打包后的数据包
     * @param bPacket BasePacket
     * @return list[0] 是包ID, list[1] 是数据包
     */
    public static List<String> getPacketAndPackID(BasePacket bPacket) {
        Packet packet = new Packet();
        packet.type = bPacket.getType();
        packet.data = bPacket.getPacket();
        packet.packetID = UUID.randomUUID().toString();

        List<String> list = new ArrayList<>();
        list.add(packet.packetID);
        list.add(JsonUtils.encode(packet));
        return list;
    }

    /**
     * 获取打包后的数据包
     * @param bPacket 数据包
     * @param packetID 数据包ID
     * @return 打包后的数据包
     */
    public static String getPacketAndPackID(BasePacket bPacket, String packetID) {
        Packet packet = new Packet();
        packet.type = bPacket.getType();
        packet.data = bPacket.getPacket();
        packet.packetID = packetID;
        return JsonUtils.encode(packet);
    }

    /**
     * 读整数
     * @param is 输入流
     * @return 整数
     */
    public static int readInt(InputStream is) {
        int[] values = new int[4];
        try {
            for (int i = 0; i < 4; i++) {
                values[i] = is.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return values[0]<<24 | values[1]<<16 | values[2]<<8 | values[3];
    }

    /**
     * 写整数
     * @param os 输出流
     * @param value 整数
     */
    public static void writeInt(OutputStream os, int value) {
        int[] values = new int[4];
        values[0] = (value>>24)&0xFF;
        values[1] = (value>>16)&0xFF;
        values[2] = (value>>8)&0xFF;
        values[3] = (value)&0xFF;

        try{
            for (int i = 0; i < 4; i++) {
                os.write(values[i]);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 读字符串
     * @param is 输入流
     * @return 字符串
     */
    public static String readString(InputStream is) {
        int len = readInt(is);
        byte[] sByte = new byte[len];
        try {
            is.read(sByte);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String s = new String(sByte);
        return s;
    }

    /**
     * 写字符串
     * @param os 输出流
     * @param s 字符串
     * @return 是否成功
     */
    public static boolean writeString(OutputStream os,String s) {
        try {
            byte[] bytes = s.getBytes();
            int len = bytes.length;
            writeInt(os,len);
            os.write(bytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
