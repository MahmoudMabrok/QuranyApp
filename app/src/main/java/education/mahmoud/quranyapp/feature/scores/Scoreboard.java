package education.mahmoud.quranyapp.feature.scores;

class Scoreboard {

    String name;
    long score;
    boolean isMine;

    public Scoreboard(String name, long score) {
        this.name = name;
        this.score = score;
    }

    public Scoreboard(String name, long score, boolean isMine) {
        this.name = name;
        this.score = score;
        this.isMine = isMine;
    }
}
