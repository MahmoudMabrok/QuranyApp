package education.mahmoud.quranyapp.Util;

import java.util.ArrayList;
import java.util.List;

public class TestText {

    private static diff_match_patch diffObj = new diff_match_patch();

    private List<Point> insertionPoints;
    private List<Point> deletionPoints;
    private List<Point> correctPoints;
    private String resString;

    private int totalScore = 0;


    public void gitDiff(String src, String dst) {

        totalScore = 0;
        insertionPoints = new ArrayList<>();
        deletionPoints = new ArrayList<>();
        correctPoints = new ArrayList<>();

        resString = new String();

        StringBuilder builder = new StringBuilder();

        int current = 0;
        Point point;
        for (diff_match_patch.Diff a : diffObj.diff_main(src, dst)) {
            if (a.operation == diff_match_patch.Operation.DELETE) {
                deletionPoints.add(new Point(current, current + a.text.length()));
            } else if (a.operation == diff_match_patch.Operation.INSERT) {
                insertionPoints.add(new Point(current, current + a.text.length()));
            } else {
                correctPoints.add(new Point(current, current + a.text.length()));
            }
            current += a.text.length();
            builder.append(a.text);
        }

        resString = builder.toString();
    }

    public List<Point> getCorrectPoints() {
        return correctPoints;
    }

    public String getResString() {
        return resString;
    }

    public List<Point> getInsertionPoints() {
        return insertionPoints;
    }

    public List<Point> getDeletionPoints() {
        return deletionPoints;
    }

    public int getTotalScore() {
        totalScore = 0; // intial value = 0
        for (Point point : insertionPoints) {
            totalScore -= (point.getEnd() - point.getStart());
        }
        for (Point point : deletionPoints) {
            totalScore -= 2 * (point.getEnd() - point.getStart());
        }
        for (Point point : correctPoints) {
            totalScore += 2 * (point.getEnd() - point.getStart());
        }

        return totalScore;
    }
}


class Point {
    private int start, end;

    public Point(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

}
