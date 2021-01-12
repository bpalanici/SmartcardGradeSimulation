package Structs;

import java.util.ArrayList;
import java.util.List;

public class Subject {

    public byte id;
    public List<Grade> grades;
    public byte nrGrades;
    public boolean isTaxPayed;
    public Subject(byte id) {
        nrGrades = 0;
        isTaxPayed = false;
        grades = new ArrayList<>();
        this.id = id;
    }
}
