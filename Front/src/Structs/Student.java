package Structs;

import static Ops.Constants.NR_SUBJECTS;

public class Student {
    public int id;
    public Subject[] subjectList;
    public Student() {
        id = 0;
        subjectList = new Subject[NR_SUBJECTS + 1];
    }
}
