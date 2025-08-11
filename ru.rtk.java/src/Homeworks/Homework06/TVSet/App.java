package Homeworks.Homework06.TVSet;

public class App
{
    public static void main(String[] args)
    {
        TVSet firstTVset = new TVSet(false, 1);
        TVSet secondTVset = new TVSet(true, 20);

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
