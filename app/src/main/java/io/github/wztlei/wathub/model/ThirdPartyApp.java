package io.github.wztlei.wathub.model;

public class ThirdPartyApp {

    private String name;
    private String description;
    private String packageName;
    private String url;
    private int iconId;
    private int type;
    private boolean isOfficial;

    private static final int APP = 0;
    private static final int WEBSITE = 1;

    public static ThirdPartyApp buildApp(String name, String description, String packageName,
                                         int iconId, boolean isOfficial) {
        return new ThirdPartyApp(name, description, packageName, null, iconId, APP, isOfficial);
    }

    public static ThirdPartyApp buildWebsite(String name, String description, String url,
                                             int iconId, boolean isOfficial) {
        return new ThirdPartyApp(name, description, null, url, iconId, WEBSITE, isOfficial);
    }

    private ThirdPartyApp(String name, String description, String packageName, String url,
                          int iconId, int type, boolean isOfficial) {
        this.name = name;
        this.description = description;
        this.packageName = packageName;
        this.url = url;
        this.iconId = iconId;
        this.type = type;
        this.isOfficial = isOfficial;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getUrl() {
        return url;
    }

    public int getIconId() {
        return iconId;
    }

    public int getType() {
        return type;
    }

    public boolean isOfficial() {
        return isOfficial;
    }

    public boolean isApp() {
        return type == APP;
    }

    public boolean isWebsite() {
        return type == WEBSITE;
    }
}
