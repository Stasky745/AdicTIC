package com.adictic.admin.entity;

import com.adictic.common.entity.AdminProfile;

public class NewAdmin {
    public String username;
    public String email;
    public AdminProfile adminProfile;
    public Boolean officeAdmin;

    @Override
    public String toString() {
        return "NewAdmin{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", adminProfile=" + adminProfile +
                ", officeAdmin=" + officeAdmin +
                '}';
    }
}
