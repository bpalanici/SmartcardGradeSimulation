package Ops;

import Structs.Grade;
import Structs.Student;
import Structs.Subject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import static Ops.Constants.NR_SUBJECTS;
import static Ops.Globals.currentGlobalStudent;
import static Ops.Operations.getTextFromPath;

public class Database {
    public static HashMap<Byte, String> getMapObjectNames(String path) {
        // Create a HashMap object called capitalCities
        HashMap<Byte, String> map = new HashMap<>();
        for (String it : getTextFromPath(path))
            map.put(Byte.parseByte(it.split(" ")[0]), it.split(" ")[1].replace("\r", ""));
        return map;
    }

    public static Student getStudentIntel(int studentID) {
        Student tempStudent = new Student();
        int lineI = 0;
        String[] lines = getTextFromPath("Texts" + File.separator + studentID + ".txt");
        tempStudent.id = Integer.parseInt(lines[lineI].split(" ")[0]);
        lineI++;
        for (int i = 1; i <= NR_SUBJECTS; i++) {
            Subject currentSubject = new Subject(Byte.parseByte(lines[lineI].split(" ")[0]));
            lineI++;
            byte nrGrades = Byte.parseByte(lines[lineI].split(" ")[0]);
            currentSubject.nrGrades = nrGrades;
            lineI++;
            while (nrGrades > 0) {
                byte value = Byte.parseByte(lines[lineI].split(" ")[0]);
                lineI++;
                lines[lineI] = lines[lineI].replace(" ", "");
                byte day = Byte.parseByte(lines[lineI].split("/")[0]);
                byte month = Byte.parseByte(lines[lineI].split("/")[1]);
                short year = Short.parseShort(lines[lineI].split("/")[2].split(":")[0]);
                lineI++;
                currentSubject.grades.add(new Grade(value, day, month, year));
                nrGrades--;
            }
            currentSubject.isTaxPayed = Boolean.parseBoolean(lines[lineI].split(" ")[0]);
            tempStudent.subjectList[i] = currentSubject;
            lineI++;
        }
        return tempStudent;
    }

    public static void printStudentIntel() {
        StringBuilder content = new StringBuilder();

        File file = new File("Texts" + File.separator + currentGlobalStudent.id + ".txt");
        content.append(currentGlobalStudent.id).append(" : id Student\n\n");
        for (int i = 1; i <= NR_SUBJECTS; i++) {
            Subject subject = currentGlobalStudent.subjectList[i];
            content.append(subject.id).append(" : code object ").append(i).append("\n");
            content.append(subject.nrGrades).append(" : nr grades\n");
            for (int i1 = 0; i1 < subject.nrGrades; i1++) {
                Grade grade = subject.grades.get(i1);
                content.append(grade.value).append(" : grade\n");
                content.append(grade.day).append(" / ").append(grade.month).append(" / ");
                content.append(grade.year).append(" : date\n");
            }
            content.append(subject.isTaxPayed).append(" : tax\n\n");
        }

        try {
            FileWriter f2 = new FileWriter(file, false);
            f2.write(content.toString());
            f2.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addGradeToStudentDB(int subjectID, byte value, byte day, byte month, short year) {
        Subject subject = currentGlobalStudent.subjectList[subjectID];
        subject.nrGrades++;
        subject.grades.add(new Grade(value, day, month, year));
    }

    public static void payTax(byte subjectID) {
        currentGlobalStudent.subjectList[subjectID].isTaxPayed = true;
    }

}
