package org.rabbitstack.packetstorm.filter;

import storm.trident.operation.BaseFilter;
import storm.trident.tuple.TridentTuple;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Discards the packets whose destination IP
 * is classified as internal.
 *
 * @author Nedim Sabic
 */
public class IsInternalIp extends BaseFilter {

    @Override
    public boolean isKeep(TridentTuple tuple) {
        String ip = tuple.getString(0);
        if (ip == null || (ip != null && ip.length() == 0)) {
            return false;
        } else {
            try {
                InetAddress a = Inet4Address.getByName(ip);
                Inet4Address ip4 = (Inet4Address)Inet4Address.getByAddress(a.getAddress());
                return !ip4.isSiteLocalAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
