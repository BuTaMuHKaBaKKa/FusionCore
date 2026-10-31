package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

// Fusion Core: Purpur's /tpsbar (purpur.yml settings.command.tpsbar), Purpur's defaults with the thresholds as settings
@ConfigSerializable
public class TpsBarSpec {

    @Setting("enabled")
    private boolean enabled = true;

    @Setting("tick-interval")
    private int tickInterval = 20;

    @Setting("title")
    private String title = "<gray>TPS<yellow>:</yellow> <tps>  MSPT<yellow>:</yellow> <mspt><gray>/50</gray>  Ping<yellow>:</yellow> <ping>ms";

    @Setting("overlay")
    private String overlay = "NOTCHED_20";

    @Setting("fill-mode")
    private String fillMode = "MSPT";

    @Setting("progress-color")
    private Levels progressColor = new Levels("GREEN", "YELLOW", "RED");

    @Setting("text-color")
    private Levels textColor = new Levels("<gradient:#55ff55:#00aa00><text></gradient>",
        "<gradient:#ffff55:#ffaa00><text></gradient>", "<gradient:#ff5555:#aa0000><text></gradient>");

    @Setting("thresholds")
    private Thresholds thresholds = new Thresholds();

    public boolean isEnabled() {
        return enabled;
    }

    public int getTickInterval() {
        return Math.max(1, tickInterval);
    }

    public String getTitle() {
        return title;
    }

    public String getOverlay() {
        return overlay;
    }

    public String getFillMode() {
        return fillMode;
    }

    public Levels getProgressColor() {
        return progressColor == null ? new Levels("GREEN", "YELLOW", "RED") : progressColor;
    }

    public Levels getTextColor() {
        return textColor == null ? new Levels("<text>", "<text>", "<text>") : textColor;
    }

    public Thresholds getThresholds() {
        return thresholds == null ? new Thresholds() : thresholds;
    }

    @ConfigSerializable
    public static class Levels {

        @Setting("good")
        private String good;

        @Setting("medium")
        private String medium;

        @Setting("low")
        private String low;

        public Levels() {
        }

        Levels(String good, String medium, String low) {
            this.good = good;
            this.medium = medium;
            this.low = low;
        }

        public String getGood() {
            return good;
        }

        public String getMedium() {
            return medium;
        }

        public String getLow() {
            return low;
        }
    }

    /** Purpur's fixed limits: TPS 19 / 15, MSPT 40 / 50, ping 100 / 200 */
    @ConfigSerializable
    public static class Thresholds {

        @Setting("tps-good")
        private double tpsGood = 19;

        @Setting("tps-medium")
        private double tpsMedium = 15;

        @Setting("mspt-good")
        private double msptGood = 40;

        @Setting("mspt-medium")
        private double msptMedium = 50;

        @Setting("ping-good")
        private int pingGood = 100;

        @Setting("ping-medium")
        private int pingMedium = 200;

        public double getTpsGood() {
            return tpsGood;
        }

        public double getTpsMedium() {
            return tpsMedium;
        }

        public double getMsptGood() {
            return msptGood;
        }

        public double getMsptMedium() {
            return msptMedium;
        }

        public int getPingGood() {
            return pingGood;
        }

        public int getPingMedium() {
            return pingMedium;
        }
    }
}
