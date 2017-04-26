package com.honkidenihongo.pre.model;

/**
 * Created by datpt on 8/11/16.
 */
public class Member {

    private long id;
    private MemberAttr attributes;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.attributes != null ? this.attributes.getName() : null;
    }

    public String getUsername() {
        return this.attributes != null ? this.attributes.getUsername() : null;
    }

    public String getEmail() {
        return this.attributes != null ? this.attributes.getEmail() : null;
    }

    public String getAvatar() {
        return this.attributes != null ? this.attributes.getAvatar() : null;
    }

    public String getFinished_at() {
        return this.attributes != null ? this.attributes.getFinished_at() : null;
    }

    public double getScore() {
        return this.attributes != null ? this.attributes.getScore() : 0;
    }

    public int getCoins() {
        return this.attributes != null ? this.attributes.getCoins() : 0;
    }

    public double getProgress() {
        return this.attributes != null ? this.attributes.getProgress() : 0;
    }

    public MemberAttr getAttributes() {
        return attributes;
    }

    public void setAttributes(MemberAttr attributes) {
        this.attributes = attributes;
    }

    public class MemberAttr {

        private String name;
        private String username;
        private String email;
        private String avatar;
        private String finished_at;
        private double score;
        private int coins;
        private double progress;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getFinished_at() {
            return finished_at;
        }

        public void setFinished_at(String finished_at) {
            this.finished_at = finished_at;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public int getCoins() {
            return coins;
        }

        public void setCoins(int coins) {
            this.coins = coins;
        }

        public double getProgress() {
            return progress;
        }

        public void setProgress(double progress) {
            this.progress = progress;
        }
    }
}
