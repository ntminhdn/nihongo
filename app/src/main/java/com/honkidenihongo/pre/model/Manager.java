package com.honkidenihongo.pre.model;

/**
 * Created by datpt on 8/11/16.
 */
public class Manager {

    private long id;
    private ManagerAttr attributes;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return attributes != null ? attributes.getName() : null;
    }

    public String getEmaill() {
        return attributes != null ? attributes.getEmaill() : null;
    }


    public String getOrganization() {
        return attributes != null ? attributes.getOrganization() : null;
    }

    public ManagerAttr getAttributes() {
        return attributes;
    }

    public void setAttributes(ManagerAttr attributes) {
        this.attributes = attributes;
    }

    public class ManagerAttr {

        private String name;
        private String emaill;
        private String organization;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmaill() {
            return emaill;
        }

        public void setEmaill(String emaill) {
            this.emaill = emaill;
        }

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }
    }
}
