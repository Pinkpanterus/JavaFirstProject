package Homeworks.Homework3;

import java.util.Arrays;
import java.util.Random;

public class TVset {
    public static int tvsetTotalCount;
    private int id;
    private final int[] CHANNELS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
    private boolean isOn = false;
    private int currentChannel = 1;

    public void switchPower() {
        this.isOn = !this.isOn;
        System.out.printf("TVset id: %d is %s!%n", this.id, isOn ? "on" : "off");
    }

    public void switchRandomChannel(){
        Random random = new Random();
        int randomInt = random.nextInt(this.CHANNELS.length);
        this.switchChannel(CHANNELS[randomInt]);
    }

    public void switchChannel(int channel) {
        if (isOn) {
            if (Arrays.stream(CHANNELS).anyMatch(ch -> ch == channel))
            {
                currentChannel = channel;
                System.out.printf("Tvset id: %d is switched to channel %d! %n", this.id, channel);
            }
            else
                System.out.printf("Tvset id: %d cant switch to channel: %d. This channel is unavailable! %n", this.id, channel);
        }
        else {
            System.out.printf("TVset id: %d is offline. Cant switch to channel: %d.%n", this.id, channel);
        }
    }

    public TVset(boolean isOn, int currentChannel) {
        ++tvsetTotalCount;
        this.id = tvsetTotalCount;
        this.isOn = isOn;
        this.currentChannel = currentChannel;
    }


    public boolean getPowerStatus() {
        return this.isOn;
    }

    public int getCurrentChannel(){
        return this.currentChannel;
    }

    public int getId(){
        return id;
    }

    public void getTVsetInfo(){
        System.out.printf("TVset id: %d, power on: %b, current channel: %d %n", getId(), this.getPowerStatus(), this.getCurrentChannel());
    }
}
