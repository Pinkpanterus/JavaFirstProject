package Homeworks.Homework3;

public class App
{
    public static void main(String[] args)
    {
        TVset firstTVset = new TVset(false, 1);
        TVset secondTVset = new TVset(true, 20);

        firstTVset.getTVsetInfo();
        secondTVset.getTVsetInfo();

        firstTVset.switchChannel(3);
        secondTVset.switchChannel(1);
        secondTVset.switchChannel(50);

        firstTVset.switchRandomChannel();
        secondTVset.switchRandomChannel();

        firstTVset.switchPower();
        firstTVset.switchChannel(4);

        firstTVset.switchPower();
        secondTVset.switchPower();
    }
 }
