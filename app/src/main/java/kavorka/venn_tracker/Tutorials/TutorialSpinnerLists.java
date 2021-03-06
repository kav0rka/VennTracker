package kavorka.venn_tracker.Tutorials;


public class TutorialSpinnerLists {

    private static final String[] mCirclesTutorialMenuList = {
            "What are circles?",
            "Green Circles",
            "Red Circles",
            "Improving performance",
            "< Back"
    };

    private static final String[] mSpawnPointsMenuList = {
            "What are spawn points?",
            "Adding spawn point markers",
            "Removing spawn point markers",
            "Tracking with spawn points",
            "Tips",
            "< Back"
    };

    private static final String[] mSpawnTimesMenuList = {
            "What are spawn times?",
            "Setting a spawn time",
            "< Back"
    };

    public static String[] getCirclesList() {
        return mCirclesTutorialMenuList;
    }
    public static String[] getSpawnPointsList() {
        return mSpawnPointsMenuList;
    }
    public static String[] getSpawnTimesList() {
        return mSpawnTimesMenuList;
    }
}
