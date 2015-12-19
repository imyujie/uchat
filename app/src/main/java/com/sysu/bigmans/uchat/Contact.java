package com.sysu.bigmans.uchat;

/**
 * Created by liyujie on 15/12/19.
 */
public class Contact {
    private String name;
    private String recent;
    private String address;
    private int role; // 0 - client, 1 - server

    public Contact(String n, String r, String a, int rr) {
        this.name = n;
        this.recent = r;
        this.address = a;
        this.role = rr;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public String getRecent() {
        return recent;
    }

    public void setRecent(String r) {
        recent = r;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String a) {
        address = a;
    }

    public void setRole(int rr) { role = rr; }

    public int getRole() {
        return role;
    }
}
